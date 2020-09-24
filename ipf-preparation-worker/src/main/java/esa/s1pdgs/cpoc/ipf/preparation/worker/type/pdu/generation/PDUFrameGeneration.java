package esa.s1pdgs.cpoc.ipf.preparation.worker.type.pdu.generation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.PDUSettings.PDUTypeSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.TimeInterval;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.pdu.PDUProduct;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;

public class PDUFrameGeneration {

	private static final Logger LOGGER = LogManager.getLogger(PDUFrameGeneration.class);

	private final PDUTypeSettings settings;
	private final MetadataClient mdClient;

	public PDUFrameGeneration(final PDUTypeSettings settings, final MetadataClient mdClient) {
		this.settings = settings;
		this.mdClient = mdClient;
	}

	public List<AppDataJob> generateAppDataJobs(final IpfPreparationJob job) throws MetadataQueryException {
		// Get metadata for product
		S3Metadata metadata = mdClient.getS3MetadataForProduct(job.getProductFamily(),
				job.getEventMessage().getBody().getProductName());
		
		if (metadata == null) {
			// It may be that the elastic search is not updated yet. Refresh the index and try again
			mdClient.refreshIndex(job.getProductFamily(), job.getEventMessage().getBody().getProductType());
			metadata = mdClient.getS3MetadataForProduct(job.getProductFamily(),
					job.getEventMessage().getBody().getProductName());
			if (metadata == null) {
				// If metadata is still null, there may be an inconsistency with the es index - abort!
				throw new MetadataQueryException("Could not retrieve metadata information for product of IpfPreparationJob");
			}
		}

		final S3Metadata firstOfOrbit = mdClient.getFirstProductForOrbit(job.getProductFamily(),
				job.getEventMessage().getBody().getProductType(), metadata.getSatelliteId(),
				Long.parseLong(metadata.getAbsoluteStartOrbit()));

		// Check if this product is the first of its orbit
		if (firstOfOrbit != null) {
			if (firstOfOrbit.getInsertionTime().equals(metadata.getInsertionTime())) {
				// Product is first of orbit, generate PDU-Jobs
				LOGGER.debug("Product is first in orbit - generate PDUs with type FRAME");
				final S3Metadata firstOfLastOrbit = mdClient.getFirstProductForOrbit(job.getProductFamily(),
						job.getEventMessage().getBody().getProductType(), metadata.getSatelliteId(),
						Long.parseLong(metadata.getAbsoluteStartOrbit()) - 1);

				String startTime = metadata.getAnxTime();
				if (firstOfLastOrbit != null) {
					startTime = firstOfLastOrbit.getAnx1Time();
				}

				List<TimeInterval> timeIntervals = generateTimeIntervals(startTime, metadata.getAnx1Time(),
						settings.getLengthInS());

				List<AppDataJob> jobs = new ArrayList<>();
				Integer frameNumber = 1;
				for (TimeInterval interval : timeIntervals) {
					LOGGER.debug("Create AppDataJob for PDU time interval: [{}; {}]",
							DateUtils.formatToMetadataDateTimeFormat(interval.getStart()),
							DateUtils.formatToMetadataDateTimeFormat(interval.getStop()));
					AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);
					appDataJob.setStartTime(DateUtils.formatToMetadataDateTimeFormat(interval.getStart()));
					appDataJob.setStopTime(DateUtils.formatToMetadataDateTimeFormat(interval.getStop()));

					appDataJob.getProduct().getMetadata().put(PDUProduct.FRAME_NUMBER, frameNumber.toString());
					jobs.add(appDataJob);

					frameNumber++;
				}

				return jobs;
			}
		}

		LOGGER.debug("Product is not first in orbit - skip PDU generation");
		return Collections.emptyList();
	}

	private List<TimeInterval> generateTimeIntervals(final String start, final String stop, final long length) {
		List<TimeInterval> intervals = new ArrayList<>();

		LocalDateTime currentStop = DateUtils.parse(start);
		final LocalDateTime finalStop = DateUtils.parse(stop);

		while (currentStop.isBefore(finalStop)) {
			LocalDateTime newStop = currentStop.plusSeconds(length);
			if (newStop.isAfter(finalStop)) {
				intervals.add(new TimeInterval(currentStop, finalStop));
			} else {
				intervals.add(new TimeInterval(currentStop, newStop));
			}
			currentStop = newStop;
		}

		return intervals;
	}
}
