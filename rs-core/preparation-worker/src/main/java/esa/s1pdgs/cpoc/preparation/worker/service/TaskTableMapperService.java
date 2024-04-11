/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
			final Map<String, TaskTableAdapter> ttAdapters)
			throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
		this.ttMapper = ttMapper;
		this.processProperties = processProperties;
		this.ttAdapters = ttAdapters;

		if (processProperties.getL0EwSlcMaskFilePath() != null
				&& !processProperties.getL0EwSlcMaskFilePath().isEmpty()) {
			ewSlcMaskIntersection = new GeoIntersection(new File(processProperties.getL0EwSlcMaskFilePath()),
					MaskType.EW_SLC);
			ewSlcMaskIntersection.loadMaskFile();
		}

		if (processProperties.getLandMaskFilePath() != null && !processProperties.getLandMaskFilePath().isEmpty()) {
			landMaskIntersection = new GeoIntersection(new File(processProperties.getLandMaskFilePath()),
					MaskType.LAND);
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
		boolean reportingFinished = false;

		final List<IpfPreparationJob> preparationJobs = new ArrayList<>();

		if (isAllowed(event, productName, family, reporting)) {
			final List<String> taskTableNames = ttMapper.tasktableFor(event);

			final Reporting taskTableLookupReporting = reporting.newReporting("TaskTableLookup");
			taskTableLookupReporting.begin(
					ReportingUtils.newFilenameReportingInputFor(event.getProductFamily(), productName),
					new ReportingMessage("Start associating TaskTables to CatalogEvent %s", productName));
			try {
				for (final String taskTableName : taskTableNames) {
					if (l0EwSliceMaskCheck(event, productName, family, taskTableName, reporting)) {
						LOGGER.debug("Tasktable for {} is {}", productName, taskTableName);
						IpfPreparationJob job = dispatch(event, reporting, taskTableName);
	
						if (job != null) {
							preparationJobs.add(job);
						}
					} else {
						LOGGER.info("EW slice is not intersecting slice, skip IpfPreparationJob generation");
						reporting.end(null, new ReportingMessage("Product %s is not intersecting EW slice mask, skipping",
								productName));
						reportingFinished = true;
					}
				}
				
				taskTableLookupReporting.end(
						new ReportingMessage("End associating TaskTables to CatalogEvent %s", productName));
			} catch (Exception e) {
				taskTableLookupReporting.error(new ReportingMessage("Error on associating TaskTables to CatalogEvent %s: %s", productName, LogUtils.toString(e)));
				throw e;
			}
			
		} else {
			reporting.end(null, new ReportingMessage("Product %s is not over sea, skipping", productName));
			reportingFinished = true;
		}

		if (preparationJobs.isEmpty()) {
			LOGGER.debug("CatalogEvent for {} is ignored", productName);
			if (!reportingFinished) {
				reporting.end(null,
						new ReportingMessage("Product %s did not match any tasktables, skipping", productName));
			}
		} else {
			LOGGER.info("Created IpfPreparationJobs for product {}", productName);
		}
		return preparationJobs;
	}

	private final boolean isAllowed(final CatalogEvent catalogEvent, final String productName,
			final ProductFamily family, final ReportingFactory reporting) throws Exception {
		// S1PRO-483: check for matching products if they are over sea. If not, simply
		// skip the
		// production

		if (landMaskIntersection == null) {
			return true;
		}
		Pattern seaCoverageCheckPattern = Pattern.compile(processProperties.getSeaCoverageCheckPattern());
		if (!seaCoverageCheckPattern.matcher(productName).matches()) {
			return true;
		}

		final Reporting seaReport = reporting.newReporting("SeaCoverageCheck");
		try {
			seaReport.begin(ReportingUtils.newFilenameReportingInputFor(family, productName),
					new ReportingMessage("Checking sea coverage"));

			long coverage = landMaskIntersection.getCoverage(catalogEvent);

			LOGGER.info("Got sea coverage {} for product {}", coverage, productName);

			if (coverage == 0 || (coverage < 100 && coverage < processProperties.getMinSeaCoveragePercentage())) {
				seaReport.end(new SeaCoverageCheckReportingOutput(false),
						new ReportingMessage("Product %s is not over sea", productName));
				LOGGER.warn("Skipping job generation for product {} because it is not over sea", productName);
				return false;
			} else {
				seaReport.end(new SeaCoverageCheckReportingOutput(true),
						new ReportingMessage("Product %s is over sea", productName));
				return true;
			}

		} catch (final Exception e) {
			seaReport.error(new ReportingMessage("SeaCoverage check failed: %s", LogUtils.toString(e)));
			throw e;
		}
	}

	private boolean l0EwSliceMaskCheck(final CatalogEvent catalogEvent, final String productName,
			final ProductFamily family, final String taskTableName, final ReportingFactory reporting) throws Exception {
		// S1PRO-2320: check if EW_SLC products matches a specific mask. If not, simply
		// skip the production
		if (ewSlcMaskIntersection == null) {
			return true;
		}
		Pattern l0EwSlcCheckPattern = Pattern.compile(processProperties.getL0EwSlcCheckPattern());
		if (!l0EwSlcCheckPattern.matcher(productName).matches()
				|| !taskTableName.contains(processProperties.getL0EwSlcTaskTableName())) {
			return true;
		}

		final Reporting ewSlcReport = reporting.newReporting("L0EWSliceMaskCheck");
		try {
			ewSlcReport.begin(ReportingUtils.newFilenameReportingInputFor(family, productName),
					new ReportingMessage("Checking if L0 EW slice %s is intersecting mask", productName));
			if (!ewSlcMaskIntersection.isIntersecting(catalogEvent)) {
				ewSlcReport.end(new L0EWSliceMaskCheckReportingOutput(false),
						new ReportingMessage("L0 EW slice %s is not intersecting mask", productName));
				LOGGER.warn("Skipping job generation for product {} because it is not intersecting mask", productName);
				return false;
			} else {
				ewSlcReport.end(new L0EWSliceMaskCheckReportingOutput(true),
						new ReportingMessage("L0 EW slice %s is intersecting mask", productName));
				return true;
			}

		} catch (final Exception e) {
			ewSlcReport.error(new ReportingMessage("L0 EW slice check failed: %s", LogUtils.toString(e)));
			throw e;
		}
	}

	private final IpfPreparationJob dispatch(final CatalogEvent event, final Reporting reporting,
			final String taskTableName) {
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(event);
		final TaskTableAdapter ttAdapter = ttAdapters.get(taskTableName);

		if (ttAdapter == null) {
			LOGGER.warn("CatalogEvent got mapped to unknown TaskTable \"{}\" - Please update your TaskTableMapping!",
					taskTableName);
			return null;
		}

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

		return job;
	}
}
