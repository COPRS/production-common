package esa.s1pdgs.cpoc.preparation.worker.type.pdu.generator;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.time.TimeInterval;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;

public abstract class AbstractPDUGenerator {

	/**
	 * Check if the product of the job is the first product of the orbit. In this
	 * case "first" does not mean in regards to validityTime but in regards to
	 * insertionTime (first in the system)
	 * 
	 * @param metadata metadata of the product belonging to the job
	 * @param mdClient MetadataClient instance to query metadata
	 * @param job      job to check if its product is the first inserted of the
	 *                 orbit
	 * @return true if its the first, false if not
	 * @throws MetadataQueryException When an exception occurs while querying, or we
	 *                                can't find any product in the orbit
	 */
	protected boolean checkIfFirstInOrbit(final S3Metadata metadata, final MetadataClient mdClient,
			final IpfPreparationJob job) throws MetadataQueryException {
		final S3Metadata firstOfOrbit = mdClient.performWithReindexOnNull(
				() -> mdClient.getFirstProductForOrbit(job.getProductFamily(),
						job.getCatalogEvent().getMetadataProductType(), metadata.getSatelliteId(),
						Long.parseLong(metadata.getAbsoluteStartOrbit())),
				job.getCatalogEvent().getMetadataProductType(), job.getProductFamily());

		if (firstOfOrbit != null) {
			return firstOfOrbit.getInsertionTime().equals(metadata.getInsertionTime());
		}

		// We have at least one product in the orbit: The product of the job itself. If
		// we get here our elastic search is inconsistent - abort!
		throw new MetadataQueryException("Inconsistent elastic search state: Found no product in orbit of job");
	}

	/**
	 * Generate time intervals of length "length" for the given start and stop time
	 * 
	 * @param start  start time for the intervals
	 * @param stop   stop time for the intervals
	 * @param length maximum length for the intervals
	 * @param fill   flag to add remaining time to last interval
	 * @return list of created intervals (may be empty if start = stop)
	 */
	List<TimeInterval> generateTimeIntervals(final String start, final String stop, final double length, final double minLength) {
		List<TimeInterval> intervals = new ArrayList<>();

		LocalDateTime currentStop = DateUtils.parse(start);
		final LocalDateTime finalStop = DateUtils.parse(stop);
		
		long lengthInNanos = (long) (length * 1000000000L);
		long minLengthInNanos = (long) (minLength * 1000000000L);

		while (currentStop.isBefore(finalStop)) {
			LocalDateTime newStop = currentStop.plusNanos(lengthInNanos);			
			if (newStop.isAfter(finalStop)) {
				intervals.add(new TimeInterval(currentStop, finalStop));
			} else {
				// Check if last interval would be too small. If yes, merge it with predecessor
				long restInterval = newStop.until(finalStop, ChronoUnit.NANOS);
				if (restInterval < minLengthInNanos) {
					newStop = finalStop;
				}
				intervals.add(new TimeInterval(currentStop, newStop));
			}
			currentStop = newStop;
		}

		return intervals;
	}

	/**
	 * Get the metadata for the product belonging to the given job
	 * 
	 * This method refreshes the elastic search index once, if it can't find the
	 * metadata on its first try
	 * 
	 * @param mdClient MetadataClient instance to query metadata
	 * @param job      job to determine which product to extract
	 * @return metadata for product of job, can not be null
	 * @throws MetadataQueryException on error in MetadataClient, or when we can't
	 *                                find the metadata even after a refresh
	 */
	protected S3Metadata getMetadataForJobProduct(final MetadataClient mdClient, IpfPreparationJob job)
			throws MetadataQueryException {
		// Get metadata for product
		S3Metadata metadata = mdClient.performWithReindexOnNull(
				() -> mdClient.getS3MetadataForProduct(job.getProductFamily(),
						job.getCatalogEvent().getProductName()),
				job.getCatalogEvent().getMetadataProductType(), job.getProductFamily());

		if (metadata == null) {
			// If metadata is still null, there may be an inconsistency with the es index -
			// abort!
			throw new MetadataQueryException(
					"Could not retrieve metadata information for product of IpfPreparationJob");
		}

		return metadata;
	}
}
