package esa.s1pdgs.cpoc.preparation.worker.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import esa.s1pdgs.cpoc.common.MaskType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.TaskTableMappingProperties;
import esa.s1pdgs.cpoc.preparation.worker.report.DispatchReportInput;
import esa.s1pdgs.cpoc.preparation.worker.report.L0EWSliceMaskCheckReportingOutput;
import esa.s1pdgs.cpoc.preparation.worker.report.SeaCoverageCheckReportingOutput;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.mapper.TasktableMapper;
import esa.s1pdgs.cpoc.preparation.worker.util.GeoIntersection;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class TaskTableMapperService {

	static final Logger LOGGER = LogManager.getLogger(TaskTableMapperService.class);

	private TasktableMapper ttMapper;
	private ProcessProperties processProperties;
	private Map<String, TaskTableAdapter> ttAdapters;
	
	private GeoIntersection ewSlcMaskIntersection;
	private GeoIntersection landMaskIntersection;

	
	public TaskTableMapperService(final TasktableMapper ttMapper, final ProcessProperties processProperties,
			final Map<String, TaskTableAdapter> ttAdapters) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
		this.ttMapper = ttMapper;
		this.processProperties = processProperties;
		this.ttAdapters = ttAdapters;
		
		if (processProperties.getL0EwSlcMaskFilePath() != null && !processProperties.getL0EwSlcMaskFilePath().isEmpty()) {
			ewSlcMaskIntersection = new GeoIntersection(new File(processProperties.getL0EwSlcMaskFilePath()), MaskType.EW_SLC);
			ewSlcMaskIntersection.loadMaskFile();
		}
		
		if (processProperties.getLandMaskFilePath() != null && !processProperties.getLandMaskFilePath().isEmpty()) {
			landMaskIntersection = new GeoIntersection(new File(processProperties.getLandMaskFilePath()), MaskType.LAND);
			landMaskIntersection.loadMaskFile();
		}
			
	}

	/**
	 * For a given CatalogEvent find all matching tasktables based on the given
	 * Configuration ({@link TaskTableMappingProperties}).
	 * 
	 * @param event     CatalogEvent to find matching tasktables
	 * @param reporting Reporting object to save ReportingOutputs
	 * @return List of PreparationJobs. Each Job contains the information of the
	 *         CatalogEvent and one matched tasktable
	 */
	public List<IpfPreparationJob> mapEventToTaskTables(final CatalogEvent event, final Reporting reporting)
			throws Exception {
		final String productName = event.getProductName();
		final ProductFamily family = event.getProductFamily();

		LOGGER.debug("Handling consumption of product {}", productName);

		reporting.begin(ReportingUtils.newFilenameReportingInputFor(event.getProductFamily(), productName),
				new ReportingMessage("Received CatalogEvent for %s", productName));

		if (isAllowed(event, productName, family, reporting)) {
			final List<String> taskTableNames = ttMapper.tasktableFor(event);
			final List<IpfPreparationJob> preparationJobs = new ArrayList<>(taskTableNames.size());

			for (final String taskTableName : taskTableNames) {
				if (l0EwSliceMaskCheck(event, productName, family, taskTableName, reporting)) {
					LOGGER.debug("Tasktable for {} is {}", productName, taskTableName);
					IpfPreparationJob job = dispatch(event, reporting, taskTableName);
					
					if (job != null) {
						preparationJobs.add(job);
					}
				}
			}
			if (preparationJobs.isEmpty()) {
				reporting.end(
						new ReportingMessage("Product %s is not intersecting EW slice mask, skipping", productName));
			}
			LOGGER.info("Dispatching product {}", productName);
			return preparationJobs;
		} else {
			reporting.end(new ReportingMessage("Product %s is not over sea, skipping", productName));
			LOGGER.debug("CatalogEvent for {} is ignored", productName);
		}
		LOGGER.debug("Done handling consumption of product {}", productName);
		return Collections.emptyList();
	}

	private final boolean isAllowed(final CatalogEvent catalogEvent, final String productName, final ProductFamily family,
			final ReportingFactory reporting) throws Exception {
		// S1PRO-483: check for matching products if they are over sea. If not, simply
		// skip the
		// production
		final Reporting seaReport = reporting.newReporting("SeaCoverageCheck");
		Pattern seaCoverageCheckPattern = Pattern.compile(processProperties.getSeaCoverageCheckPattern());
		try {
			if (seaCoverageCheckPattern.matcher(productName).matches()) {
				seaReport.begin(ReportingUtils.newFilenameReportingInputFor(family, productName),
						new ReportingMessage("Checking sea coverage"));
				if (landMaskIntersection != null && landMaskIntersection.getCoverage(catalogEvent) <= processProperties
						.getMinSeaCoveragePercentage()) {
					seaReport.end(new SeaCoverageCheckReportingOutput(false),
							new ReportingMessage("Product %s is not over sea", productName));
					LOGGER.warn("Skipping job generation for product {} because it is not over sea", productName);
					return false;
				} else {
					seaReport.end(new SeaCoverageCheckReportingOutput(true),
							new ReportingMessage("Product %s is over sea", productName));
				}
			}
		} catch (final Exception e) {
			seaReport.error(new ReportingMessage("SeaCoverage check failed: %s", LogUtils.toString(e)));
			throw e;
		}
		return true;
	}

	private boolean l0EwSliceMaskCheck(final CatalogEvent catalogEvent, final String productName,
			final ProductFamily family, final String taskTableName, final ReportingFactory reporting) throws Exception {
		// S1PRO-2320: check if EW_SLC products matches a specific mask. If not, simply
		// skip the production
		final Reporting ewSlcReport = reporting.newReporting("L0EWSliceMaskCheck");
		Pattern l0EwSlcCheckPattern = Pattern.compile(processProperties.getL0EwSlcCheckPattern());
		try {
			if (l0EwSlcCheckPattern.matcher(productName).matches()
					&& taskTableName.contains(processProperties.getL0EwSlcTaskTableName())) {
				ewSlcReport.begin(ReportingUtils.newFilenameReportingInputFor(family, productName),
						new ReportingMessage("Checking if L0 EW slice %s is intersecting mask", productName));
				if (ewSlcMaskIntersection != null && !ewSlcMaskIntersection.isIntersecting(catalogEvent)) {
					ewSlcReport.end(new L0EWSliceMaskCheckReportingOutput(false),
							new ReportingMessage("L0 EW slice %s is not intersecting mask", productName));
					LOGGER.warn("Skipping job generation for product {} because it is not intersecting mask",
							productName);
					return false;
				} else {
					ewSlcReport.end(new L0EWSliceMaskCheckReportingOutput(true),
							new ReportingMessage("L0 EW slice %s is intersecting mask", productName));
				}
			}
		} catch (final Exception e) {
			ewSlcReport.error(new ReportingMessage("L0 EW slice check failed: %s", LogUtils.toString(e)));
			throw e;
		}
		return true;
	}

	private final IpfPreparationJob dispatch(final CatalogEvent event, final ReportingFactory reportingFactory,
			final String taskTableName) {
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(event);
		final TaskTableAdapter ttAdapter = ttAdapters.get(taskTableName);
		
		if (ttAdapter == null) {
			LOGGER.warn("CatalogEvent got mapped to unknown TaskTable \"{}\" - Please update your TaskTableMapping!", taskTableName);
			return null;
		}

		// FIXME reporting of AppDataJob doesn't make sense here any more, needs to be
		// replaced by something
		// meaningful
		final int appDataJobId = 0;

		final Reporting reporting = reportingFactory.newReporting("Dispatch");
		reporting.begin(
				DispatchReportInput.newInstance(appDataJobId, event.getProductName(),
						processProperties.getProductType()),
				new ReportingMessage("Dispatching AppDataJob %s for %s %s", appDataJobId,
						processProperties.getProductType(), event.getProductName()));
		final IpfPreparationJob job = new IpfPreparationJob();
		job.setLevel(processProperties.getLevel());
		job.setPodName(processProperties.getHostname());
		job.setCatalogEvent(event);
		job.setTaskTableName(taskTableName);
		job.setTriggerProducts(ttAdapter.getAllPossibleFileTypes());
		// S1PRO-1772: user productSensing accessors here to make start/stop optional
		// here (RAWs don't have them)
		job.setStartTime(eventAdapter.productSensingStartDate());
		job.setStopTime(eventAdapter.productSensingStopDate());
		job.setProductFamily(event.getProductFamily());
		job.setKeyObjectStorage(event.getProductName());
		job.setUid(reporting.getUid());
		job.setDebug(event.isDebug());

		reporting.end(new ReportingMessage("AppDataJob %s for %s %s dispatched", appDataJobId,
				processProperties.getProductType(), event.getProductName()));
		return job;
	}
}
