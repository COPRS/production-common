package esa.s1pdgs.cpoc.ipf.preparation.worker.type.pdu;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.PDUSettings.PDUTypeSettings;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class PDUFrameGeneration {

	private static final Logger LOGGER = LogManager.getLogger(PDUFrameGeneration.class);

	private static class TimeInterval {
		private LocalDateTime start;
		private LocalDateTime stop;

		public TimeInterval(LocalDateTime start, LocalDateTime stop) {
			this.start = start;
			this.stop = stop;
		}

		public LocalDateTime getStart() {
			return start;
		}

		public LocalDateTime getStop() {
			return stop;
		}
	}

	private final PDUTypeSettings settings;
	private final MetadataClient mdClient;

	public PDUFrameGeneration(final PDUTypeSettings settings, final MetadataClient mdClient) {
		this.settings = settings;
		this.mdClient = mdClient;
	}

	public List<AppDataJob> generateAppDataJobs(final IpfPreparationJob job) throws MetadataQueryException {
		// Get metadata for product
		final S3Metadata metadata = mdClient.getS3MetadataForProduct(job.getProductFamily(),
				job.getEventMessage().getBody().getProductName());

		final List<S3Metadata> productsOfThisOrbit = mdClient.getProductsForOrbit(job.getProductFamily(),
				job.getEventMessage().getBody().getProductType(), metadata.getSatelliteId(),
				Long.parseLong(metadata.getAbsoluteStartOrbit()));

		// Check if this product is the first of its orbit
		if (productsOfThisOrbit != null && !productsOfThisOrbit.isEmpty()) {
			final S3Metadata firstOfOrbit = productsOfThisOrbit.get(0);
			if (firstOfOrbit.getInsertionTime().equals(metadata.getInsertionTime())) {
				// Product is first of orbit, generate PDU-Jobs
				LOGGER.debug("Product is first in orbit - generate PDUs with type FRAME");
				final List<S3Metadata> productsOfLastOrbit = mdClient.getProductsForOrbit(job.getProductFamily(),
						job.getEventMessage().getBody().getProductType(), metadata.getSatelliteId(),
						Long.parseLong(metadata.getAbsoluteStartOrbit()) - 1);

				String startTime = metadata.getAnxTime();
				if (productsOfLastOrbit != null && !productsOfLastOrbit.isEmpty()) {
					startTime = productsOfLastOrbit.get(0).getAnx1Time();
				}

				List<TimeInterval> timeIntervals = generateTimeIntervals(startTime, metadata.getAnx1Time(),
						settings.getLengthInS());

				List<AppDataJob> jobs = new ArrayList<>();
				Integer frameNumber = 1;
				for (TimeInterval interval : timeIntervals) {
					LOGGER.debug("Create AppDataJob for PDU time interval: [{}; {}]",
							DateUtils.formatToMetadataDateTimeFormat(interval.getStart()),
							DateUtils.formatToMetadataDateTimeFormat(interval.getStop()));
					AppDataJob appDataJob = toAppDataJob(job);
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

	private final AppDataJobProduct newProductFor(final GenericMessageDto<CatalogEvent> mqiMessage) {
		final CatalogEvent event = mqiMessage.getBody();
		final AppDataJobProduct productDto = new AppDataJobProduct();

		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(mqiMessage);
		productDto.getMetadata().put("productName", event.getProductName());
		productDto.getMetadata().put("productType", event.getProductType());
		productDto.getMetadata().put("satelliteId", eventAdapter.satelliteId());
		productDto.getMetadata().put("missionId", eventAdapter.missionId());
		productDto.getMetadata().put("processMode", eventAdapter.processMode());
		productDto.getMetadata().put("startTime", eventAdapter.productSensingStartDate());
		productDto.getMetadata().put("stopTime", eventAdapter.productSensingStopDate());
		productDto.getMetadata().put("timeliness", eventAdapter.timeliness());
		productDto.getMetadata().put("acquistion", eventAdapter.swathType());
		return productDto;
	}

	private final AppDataJob toAppDataJob(final IpfPreparationJob prepJob) {
		final AppDataJob job = new AppDataJob();
		job.setLevel(prepJob.getLevel());
		job.setPod(prepJob.getHostname());
		job.getMessages().add(prepJob.getEventMessage());
		job.setProduct(newProductFor(prepJob.getEventMessage()));
		job.setTaskTableName(prepJob.getTaskTableName());
		job.setStartTime(prepJob.getStartTime());
		job.setStopTime(prepJob.getStopTime());
		job.setProductName(prepJob.getKeyObjectStorage());
		return job;
	}
}
