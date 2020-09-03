package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import static java.util.stream.Collectors.toMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
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

	public static class Range {
		private LocalDateTime start;
		private LocalDateTime stop;

		public Range(LocalDateTime start, LocalDateTime stop) {
			this.start = start;
			this.stop = stop;
		}

		public LocalDateTime getStart() {
			return start;
		}

		public void setStart(LocalDateTime start) {
			this.start = start;
		}

		public LocalDateTime getStop() {
			return stop;
		}

		public void setStop(LocalDateTime stop) {
			this.stop = stop;
		}
		
		@Override
		public String toString() {
			return "Range [start=" + start + ", stop=" + stop + "]";
		}

		/**
		 * Checks if this range intersects with the given range
		 * 
		 * @param other range to check whether this range intersects with
		 * @return true, if ranges intersect
		 */
		public boolean intersects(Range other) {
			return !other.getStart().isAfter(stop) && !other.getStop().isBefore(start);
		}
	}

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
		// TODO: Allow other timeliness
		String timeliness = "NRT";
		List<S3Metadata> products = metadataClient.getProductsInRange(alternative.getFileType(),
				elementMapper.inputFamilyOf(alternative.getFileType()), product.getSatelliteId(),
				product.getStartTime(), product.getStopTime(), alternative.getDeltaTime0(), alternative.getDeltaTime1(),
				timeliness);

		// Filter products for duplicates
		products = DuplicateProductFilter.filterS3Metadata(products);

		// Check coverage
		if (!products.isEmpty()) {
			boolean intervalCovered = checkCoverage(product.getStartTime(), product.getStopTime(),
					alternative.getDeltaTime0(), alternative.getDeltaTime1(), timeliness, products);

			// Set results on matching tasks
			tasks = updateAppDataJobTaskInputs(tasks, products, intervalCovered, alternative,
					prepSettings.getProductMode(), ttAdapter);
		}

		return tasks;
	}

	/**
	 * Search for products to cover the interval of the AppDataJob
	 * 
	 * @param tasks       List of tasks which should be updated
	 * @param alternative TaskTableInputAlternative for which products should be
	 *                    searched
	 * @param job         object which holds information to the needed interval
	 * @param product     object to transmit data back to the job.
	 * @return List of tasks, updated with products for the interval.
	 * @throws MetadataQueryException On error retrieving products from the
	 *                                metadataClient
	 */
	public List<AppDataJobTaskInputs> updateTaskInputsForViscal(List<AppDataJobTaskInputs> tasks,
			final TaskTableInputAlternative alternative, final AppDataJob job, final S3Product product)
			throws MetadataQueryException {
		// TODO: Allow other timeliness
		String timeliness = "NRT";
		List<S3Metadata> products = metadataClient.getProductsInRange(alternative.getFileType(),
				elementMapper.inputFamilyOf(alternative.getFileType()), product.getSatelliteId(), job.getStartTime(),
				job.getStopTime(), 0.0, 0.0, timeliness);

		// Filter products for duplicates
		products = DuplicateProductFilter.filterS3Metadata(products);

		// Check coverage
		if (!products.isEmpty()) {
			boolean intervalCovered = checkCoverage(product.getStartTime(), product.getStopTime(), 0.0, 0.0, timeliness,
					products);

			// Set results on matching tasks
			tasks = updateAppDataJobTaskInputs(tasks, products, intervalCovered, alternative,
					prepSettings.getProductMode(), ttAdapter);
		}

		return tasks;
	}

	/**
	 * Retrieve the intersecting anxRange for the product, if there exists one.
	 * 
	 * This method checks if the anxTime of the product (modified with anxOffset and
	 * rangeLength) creates a range that intersects with the validity time of the
	 * product itself. The same is done for the anx1Time. If one of the two
	 * intersects (anxTime has a greater priority), that range is returned. If no
	 * range is intersecting null is returned.
	 * 
	 * @param productName    product to check for intersecting anx-ranges
	 * @param anxOffsetInS   offset in seconds for range start
	 * @param rangeLengthInS length in seconds of range
	 * @return ANX-range that intersects with product validity, null if none exists
	 * @throws MetadataQueryException on error in metadata query
	 */
	public Range getIntersectingANXRange(final String productName, final long anxOffsetInS, final long rangeLengthInS)
			throws MetadataQueryException {
		String productType = productName.substring(4, 15);

		S3Metadata metadata = metadataClient.getS3MetadataForProduct(elementMapper.inputFamilyOf(productType),
				productName);

		Range productRange = new Range(DateUtils.parse(metadata.getValidityStart()),
				DateUtils.parse(metadata.getValidityStop()));

		if (metadata.getAnxTime() != null) {
			Range result = getIntersectingRange(productRange, metadata.getAnxTime(), anxOffsetInS, rangeLengthInS);
			if (result != null) {
				return result;
			}
		}

		if (metadata.getAnx1Time() != null) {
			Range result = getIntersectingRange(productRange, metadata.getAnx1Time(), anxOffsetInS, rangeLengthInS);
			if (result != null) {
				return result;
			}
		}
		return null;
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
		final LocalDateTime coverageMin = time.minusSeconds(Math.round(t0));

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
			files.add(new AppDataJobFile(product.getProductName(), product.getKeyObjectStorage(),
					product.getValidityStart(), product.getValidityStop()));
		}
		return files;
	}

	/**
	 * Returns the resulting range of anx + anxOffset (length rangeLength) if the
	 * productRange intersects with that range
	 * 
	 * @return Range(anx + anxOffset, anx + anxOffset + rangeLength) if productRange
	 *         intersects with that range
	 */
	private Range getIntersectingRange(Range productRange, String anxTime, long anxOffsetInS, long rangeLengthInS) {
		LocalDateTime anx = DateUtils.parse(anxTime);

		LocalDateTime rangeStart = anx.plus(anxOffsetInS, ChronoUnit.SECONDS);
		LocalDateTime rangeStop = rangeStart.plus(rangeLengthInS, ChronoUnit.SECONDS);
		Range anxRange = new Range(rangeStart, rangeStop);

		LOGGER.debug("Check if range {} intersects with anxRange {}", productRange, anxRange);
		
		if (productRange.intersects(anxRange)) {
			LOGGER.debug("Ranges intersect!");
			return anxRange;
		}

		LOGGER.debug("Ranges do not intersect!");
		return null;
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
					LOGGER.info("Successor to LAST was not FIRST (actual: {}). List of products is not continuous.",
							successor.getGranulePosition());
					return false;
				}
			} else {
				if (product.getGranuleNumber() + 1 != successor.getGranuleNumber()) {
					LOGGER.info("Granule number not continuous. Expected {}, actual {}", product.getGranuleNumber() + 1,
							successor.getGranuleNumber());
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
