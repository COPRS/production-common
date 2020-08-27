package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	public static JobOrderInput filter(final JobOrderInput jobOrderInput) {
		List<JobOrderTimeInterval> newIntervals = new ArrayList<>();
		List<String> newFileNameStrings = new ArrayList<>();

		for (JobOrderTimeInterval interval : jobOrderInput.getTimeIntervals()) {
			if (!containsNewerProduct(interval, jobOrderInput.getTimeIntervals())) {
				newIntervals.add(interval);
				newFileNameStrings.add(interval.getFileName());
			}
		}

		List<JobOrderInputFile> newFileNames = jobOrderInput.getFilenames().stream()
				.filter(file -> newFileNameStrings.contains(file.getFilename())).collect(Collectors.toList());

		return new JobOrderInput(jobOrderInput.getFileType(), jobOrderInput.getFileNameType(), newFileNames,
				newIntervals, jobOrderInput.getFamily());
	}

	/**
	 * Checks if there are other intervals with the same start and stop time and a
	 * newer creation time
	 * 
	 * @param interval interval for which should be checked if there are any newer
	 *                 versions
	 * @return true, if newer product exists
	 */
	private static boolean containsNewerProduct(JobOrderTimeInterval interval, List<JobOrderTimeInterval> intervals) {
		String startTime = interval.getStart();
		String stopTime = interval.getStop();

		boolean returnValue = false;
		
		for (JobOrderTimeInterval other : intervals) {
			if (other.getStart().equals(startTime) && other.getStop().equals(stopTime)) {
				// We found a duplicate. Determine if the duplicate is newer than this product
				LocalDateTime creationTime = getCreationTimeFromFileName(interval.getFileName());
				LocalDateTime otherCreationTime = getCreationTimeFromFileName(other.getFileName());

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
	private static LocalDateTime getCreationTimeFromFileName(String fileName) {
		Path path = Paths.get(fileName);
		Path fName = path.getFileName();
		String name = fName.toString();
		String creationTime = name.substring(CREATION_TIME_BEGIN_INDEX, CREATION_TIME_END_INDEX);

		return LocalDateTime.parse(creationTime, formatter);
	}
}
