package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;

/**
 * Filter products with the same start and stop time and product type. Choose
 * the product with the latest creation time
 * 
 * @author Julian Kaping
 *
 */
public class DuplicateProductFilter {
	private static final Logger LOGGER = LogManager.getLogger(DuplicateProductFilter.class);
	
	private static int CREATION_TIME_BEGIN_INDEX = 48;
	private static int CREATION_TIME_END_INDEX = 63;

	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

	/**
	 * filter duplicate products from the job order
	 * 
	 * duplicates are products with the same product type, start time and stop time,
	 * but different creation times. This filter deletes duplicates and keeps the
	 * latest one (most recent creation time)
	 * 
	 * @param jobOrderInput input to check for duplicates
	 * @return new JobOrderInput without duplicates
	 */
	public static JobOrderInput filterJobOrderInput(final JobOrderInput jobOrderInput) {
		final List<JobOrderTimeInterval> newIntervals = new ArrayList<>();
		final List<String> newFileNameStrings = new ArrayList<>();

		for (final JobOrderTimeInterval interval : jobOrderInput.getTimeIntervals()) {
			if (!containsNewerProduct(interval, jobOrderInput.getTimeIntervals())) {
				newIntervals.add(interval);
				newFileNameStrings.add(interval.getFileName());
			}
		}

		final List<JobOrderInputFile> newFileNames = jobOrderInput.getFilenames().stream()
				.filter(file -> newFileNameStrings.contains(file.getFilename())).collect(Collectors.toList());

		return new JobOrderInput(jobOrderInput.getFileType(), jobOrderInput.getFileNameType(), newFileNames,
				newIntervals, jobOrderInput.getFamily());
	}

	/**
	 * filter duplicate products from list of metadata
	 * 
	 * duplicates are products with the same product type, start time and stop time,
	 * but different creation times. This filter deletes duplicates and keeps the
	 * latest one (most recent creation time)
	 * 
	 * @return new list of metadata without duplicates
	 */
	public static List<S3Metadata> filterS3Metadata(final List<S3Metadata> products) {
		final List<S3Metadata> newList = new ArrayList<>();

		for (final S3Metadata product : products) {
			if (!containsNewerProduct(product, products)) {
				newList.add(product);
			}
		}

		return newList;
	}

	/**
	 * Checks if there are other intervals with the same start and stop time and a
	 * newer creation time
	 * 
	 * @return true, if newer product exists
	 */
	private static boolean containsNewerProduct(final JobOrderTimeInterval interval, final List<JobOrderTimeInterval> intervals) {
		final String startTime = interval.getStart();
		final String stopTime = interval.getStop();
		final LocalDateTime creationTime = getCreationTimeFromFileName(interval.getFileName());

		boolean returnValue = false;

		for (final JobOrderTimeInterval other : intervals) {
			if (other.getStart().equals(startTime) && other.getStop().equals(stopTime)) {
				// We found a duplicate. Determine if the duplicate is newer than this product
				final LocalDateTime otherCreationTime = getCreationTimeFromFileName(other.getFileName());

				returnValue = returnValue || creationTime.isBefore(otherCreationTime);
			}
		}

		return returnValue;
	}

	/**
	 * Checks if there are other products with the same validityStart and
	 * validityStop and a newer creationTime
	 * 
	 * @return true, if newer product exists
	 */
	private static boolean containsNewerProduct(final S3Metadata product, final List<S3Metadata> products) {
		boolean returnValue = false;
		final LocalDateTime creationTime = DateUtils.parse(product.getCreationTime());

		for (final S3Metadata other : products) {
			if (other.getValidityStart().equals(product.getValidityStart())
					&& other.getValidityStop().equals(product.getValidityStop())) {
				final LocalDateTime otherCreationTime = DateUtils.parse(other.getCreationTime());
				returnValue = returnValue || creationTime.isBefore(otherCreationTime);
			}
		}

		return returnValue;
	}

	/**
	 * Creates a LocalDateTime object for the creation time based on the filename
	 * (absolute path)
	 * 
	 * @param fileName file name (absolute path) ex.
	 *                 JobOrderTimeInterval#getFileName()
	 * @return LocalDateTime object with creationTime
	 */
	private static LocalDateTime getCreationTimeFromFileName(final String fileName) {
		LOGGER.debug("Extract creationTime from filename \"{}\"", fileName);
		final File file = new File(fileName);
		LOGGER.debug("Reduced path to filename. Result: {}", file.getName());
		final String creationTime = file.getName().substring(CREATION_TIME_BEGIN_INDEX, CREATION_TIME_END_INDEX);

		return LocalDateTime.parse(creationTime, formatter);
	}
}
