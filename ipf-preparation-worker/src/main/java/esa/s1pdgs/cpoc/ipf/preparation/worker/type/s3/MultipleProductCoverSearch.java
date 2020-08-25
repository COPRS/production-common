package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import static java.util.stream.Collectors.toMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.QueryUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;

/**
 * Enables the ProductTypeAdapter for the Sentinel 3 mission to search for
 * multiple products of the same type that in conjunction cover a given
 * interval.
 * 
 * Provides methods to check whether a list of products is enough to cover an
 * interval either based on the granule numbers or based on a gap threshold.
 * 
 * @author Julian Kaping
 *
 */
public class MultipleProductCoverSearch {

	private static final Logger LOGGER = LogManager.getLogger(MultipleProductCoverSearch.class);

	private final TaskTableAdapter ttAdapter;
	private final ElementMapper elementMapper;
	private final MetadataClient metadataClient;
	private final IpfPreparationWorkerSettings prepSettings;

	public MultipleProductCoverSearch(TaskTableAdapter ttAdapter, ElementMapper elementMapper,
			MetadataClient metadataClient, IpfPreparationWorkerSettings prepSettings) {
		this.ttAdapter = ttAdapter;
		this.elementMapper = elementMapper;
		this.metadataClient = metadataClient;
		this.prepSettings = prepSettings;
	}

	/**
	 * Search for products to cover the interval startTime - t0 -> stopTime + t1
	 * 
	 * To check whether the found products are enough to cover the interval, either
	 * check the granule numbers (NRT) or check the gaps between the products
	 * 
	 * @param tasks       List of tasks which should be updated
	 * @param alternative TaskTableInputAlternative for which products should be
	 *                    searched
	 * @param product     object to transmit data back to the job. Used here to
	 *                    extract information to start and stop time
	 * @return List of tasks, updated with products for the interval.
	 * @throws MetadataQueryException On error retrieving products from the
	 *                                metadataClient
	 */
	public List<AppDataJobTaskInputs> updateTaskInputsByAlternative(List<AppDataJobTaskInputs> tasks,
			final TaskTableInputAlternative alternative, final S3Product product) throws MetadataQueryException {
		String timeliness = "NRT";
		List<S3Metadata> products = metadataClient.getProductsForMarginWFX(alternative.getFileType(),
				elementMapper.inputFamilyOf(alternative.getFileType()), product.getSatelliteId(),
				product.getStartTime(), product.getStopTime(), alternative.getDeltaTime0(), alternative.getDeltaTime1(),
				timeliness);

		// Check coverage
		if (!products.isEmpty()) {
			boolean intervalCovered = checkCoverage(product.getStartTime(), product.getStopTime(),
					alternative.getDeltaTime0(), alternative.getDeltaTime1(), "NRT", products);

			// Set results on matching tasks
			tasks = updateAppDataJobTaskInputs(tasks, products, intervalCovered, alternative,
					prepSettings.getProductMode(), ttAdapter);
		}

		return tasks;
	}

	/**
	 * Check if the given list of products is enough to cover the interval
	 */
	private boolean checkCoverage(final String startTime, final String stopTime, final double t0, final double t1,
			final String timeliness, final List<S3Metadata> products) {
		if (timeliness.equals("NRT")) {
			return checkCoverageNRT(startTime, stopTime, t0, t1, products);
		}

		return true;
	}

	/**
	 * For NRT the start and stop time should be covered (aka. the earliest start
	 * Time is before startTime - t0 and the latest stop time is after stopTime +
	 * t1) and the granule numbers have to be continuous
	 */
	private boolean checkCoverageNRT(final String startTime, final String stopTime, final double t0, final double t1,
			final List<S3Metadata> products) {
		final S3Metadata first = products.get(0);
		final S3Metadata last = products.get(products.size() - 1);

		LocalDateTime time = LocalDateTime.parse(startTime,
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		final LocalDateTime coverageMin = time.plusSeconds(Math.round(-t0));

		time = LocalDateTime.parse(stopTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		final LocalDateTime coverageMax = time.plusSeconds(Math.round(t1));

		final LocalDateTime firstStart = LocalDateTime.parse(first.getValidityStart(),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		final LocalDateTime lastStop = LocalDateTime.parse(last.getValidityStop(),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));

		if (firstStart.isAfter(coverageMin)) {
			LOGGER.info("CheckCoverage: First start time is after interval beginning. Interval is not covered");
			return false;
		}

		if (lastStop.isBefore(coverageMax)) {
			LOGGER.info("CheckCoverage: Last stop time is before interval ending. Interval is not covered");
			return false;
		}

		return isGranuleContinuous(products);
	}
	
	/**
	 * Convert list of metadata to needed objects
	 * 
	 * @return list of AppDataJobFile
	 */
	private List<AppDataJobFile> convertMetadataToAppDataJobFiles(final List<S3Metadata> products) {
		List<AppDataJobFile> files = new ArrayList<>();

		for (S3Metadata product : products) {
			files.add(new AppDataJobFile(product.getProductType(), product.getKeyObjectStorage(),
					product.getValidityStart(), product.getValidityStop()));
		}
		return files;
	}

	/**
	 * Check if the granule numbers are continuous.
	 * 
	 * Edge case: if the granule position is LAST the position of the successor has
	 * to be FIRST
	 * 
	 * @param products List of products which should be checked for continuity
	 * @return true if list is continuous, false if not
	 */
	private boolean isGranuleContinuous(List<S3Metadata> products) {

		for (int i = 0; i < products.size() - 1; i++) {
			S3Metadata product = products.get(i);
			S3Metadata successor = products.get(i + 1);

			if (product.getGranulePosition().equals("LAST")) {
				if (!successor.getGranulePosition().equals("FIRST")) {
					return false;
				}
			} else {
				if (product.getGranuleNumber() + 1 != successor.getGranuleNumber()) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Distributes the products to the list of tasks based on the alternative the
	 * products are retrieved for.
	 * 
	 * @param tasks       list of tasks
	 * @param products    products, that should be distributed
	 * @param complete    flag if the interval specified by the tasktable is covered
	 * @param alternative tasktable alternative to determine the correct tasks
	 * @return updated list of tasks
	 */
	private List<AppDataJobTaskInputs> updateAppDataJobTaskInputs(List<AppDataJobTaskInputs> tasks,
			List<S3Metadata> products, boolean complete, TaskTableInputAlternative alternative, ProductMode mode,
			TaskTableAdapter taskTableAdapter) {
		Map<String, TaskTableInput> taskTableInputs = QueryUtils
				.taskTableTasksAndInputsMappedTo(Collections::singletonMap, (list, task) -> list, mode,
						taskTableAdapter)
				.stream().flatMap(Collection::stream).flatMap(map -> map.entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		for (AppDataJobTaskInputs task : tasks) {
			for (AppDataJobInput input : task.getInputs()) {
				TaskTableInput ttInput = taskTableInputs.get(input.getTaskTableInputReference());
				if (ttInput.getAlternatives().contains(alternative)) {
					input.setHasResults(complete);
					input.setFileNameType(alternative.getFileNameType().toString());
					input.setFileType(alternative.getFileType());
					input.setMandatory(TaskTableMandatoryEnum.YES.equals(ttInput.getMandatory()));
					input.setFiles(convertMetadataToAppDataJobFiles(products));
				}
			}
		}

		return tasks;
	}
}
