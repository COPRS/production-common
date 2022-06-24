package esa.s1pdgs.cpoc.preparation.worker.type.pdu.generator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.time.TimeInterval;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.type.PDUProperties.PDUTypeProperties;
import esa.s1pdgs.cpoc.preparation.worker.model.pdu.PDUReferencePoint;

public class PDUStripeGenerator extends AbstractPDUGenerator implements PDUGenerator {
	private static final Logger LOGGER = LogManager.getLogger(PDUStripeGenerator.class);

	private final PDUTypeProperties settings;
	private final ProcessProperties processSettings;
	private final MetadataClient mdClient;

	public PDUStripeGenerator(final ProcessProperties processSettings, final PDUTypeProperties settings,
			final MetadataClient mdClient) {
		this.settings = settings;
		this.processSettings = processSettings;
		this.mdClient = mdClient;
	}

	@Override
	public List<AppDataJob> generateAppDataJobs(IpfPreparationJob job) throws MetadataQueryException {
		if (settings.getReference() == PDUReferencePoint.ORBIT) {
			final S3Metadata metadata = getMetadataForJobProduct(this.mdClient, job);

			// Check if this product is the first of its orbit
			if (checkIfFirstInOrbit(metadata, this.mdClient, job)) {
				List<TimeInterval> timeIntervals;

				// Product is first of orbit, generate PDU-Jobs
				LOGGER.debug("Product is first in orbit - generate PDUs with type STRIPE (Reference: Orbit)");
				final S3Metadata firstOfLastOrbit = mdClient.performWithReindexOnNull(
						() -> mdClient.getFirstProductForOrbit(job.getProductFamily(),
								job.getCatalogEvent().getMetadataProductType(), metadata.getSatelliteId(),
								Long.parseLong(metadata.getAbsoluteStartOrbit()) - 1),
						job.getCatalogEvent().getMetadataProductType(), job.getProductFamily());

				// Offset calculation
				if (settings.getOffsetInS() > 0) {

					final S3Metadata firstOfSecondLastOrbit = mdClient.performWithReindexOnNull(
							() -> mdClient.getFirstProductForOrbit(job.getProductFamily(),
									job.getCatalogEvent().getMetadataProductType(), metadata.getSatelliteId(),
									Long.parseLong(metadata.getAbsoluteStartOrbit()) - 2),
							job.getCatalogEvent().getMetadataProductType(), job.getProductFamily());

					// Priority: ANX1Time > ANXTime of next orbit > Estimate
					String orbitANX1 = metadata.getAnx1Time();
					String orbit1ANX1 = metadata.getAnxTime();
					if (firstOfLastOrbit != null) {
						orbit1ANX1 = firstOfLastOrbit.getAnx1Time();
					}
					String orbit2ANX1 = estimateANX1BeforeLastOrbit(metadata.getAnxTime());
					if (firstOfLastOrbit != null) {
						orbit2ANX1 = firstOfLastOrbit.getAnxTime();
					}
					if (firstOfSecondLastOrbit != null) {
						orbit2ANX1 = firstOfSecondLastOrbit.getAnx1Time();
					}

					// apply offsets
					orbitANX1 = addOffset(orbitANX1, (long) settings.getOffsetInS() * 1000000000L);
					orbit1ANX1 = addOffset(orbit1ANX1, (long) settings.getOffsetInS() * 1000000000L);
					orbit2ANX1 = addOffset(orbit2ANX1, (long) settings.getOffsetInS() * 1000000000L);

					// Generate all timeIntervals that are in between those two intervals
					timeIntervals = generateTimeIntervals(orbit2ANX1, orbit1ANX1, settings.getLengthInS());
					timeIntervals.addAll(generateTimeIntervals(orbit1ANX1, orbitANX1, settings.getLengthInS()));

					// only use timeIntervals with stopTime in between Anx1 from orbit -1 and anx1
					// from orbit
					TimeInterval orbitInterval = new TimeInterval(DateUtils.parse(orbit1ANX1),
							DateUtils.parse(orbitANX1));
					timeIntervals = timeIntervals.stream()
							.filter(each -> orbitInterval.intersects(new TimeInterval(each.getStop(), each.getStop())))
							.collect(Collectors.toList());
				} else {
					// No offset
					String startTime = metadata.getAnxTime();
					if (firstOfLastOrbit != null) {
						startTime = firstOfLastOrbit.getAnx1Time();
					}

					timeIntervals = generateTimeIntervals(startTime, metadata.getAnx1Time(), settings.getLengthInS());
				}

				return createJobsFromTimeIntervals(timeIntervals, job);
			}

			LOGGER.debug("Product is not first in orbit - skip PDU generation");
			return Collections.emptyList();
		} else if (settings.getReference() == PDUReferencePoint.DUMP) {
			S3Metadata metadata = getMetadataForJobProduct(mdClient, job);

			List<TimeInterval> intervals = findTimeIntervalsForMetadata(metadata, settings.getLengthInS());

			return createJobsFromTimeIntervals(intervals, job);
		}

		LOGGER.warn("Invalid reference point for pdu type STRIPE");
		return Collections.emptyList();
	}

	/**
	 * Create time intervals for PDU, so that the validityTime is inside the PDU
	 * intervals
	 */
	private List<TimeInterval> findTimeIntervalsForMetadata(final S3Metadata metadata, final double length) {
		List<TimeInterval> intervals = new ArrayList<>();

		LocalDateTime intervalStart = DateUtils.parse(metadata.getDumpStart());
		final LocalDateTime finalStop = DateUtils.parse(metadata.getValidityStop());

		final TimeInterval validityInterval = new TimeInterval(DateUtils.parse(metadata.getValidityStart()), finalStop);

		// Create next possible PDU interval and check if product is inside that
		// interval, if it is, add interval to list
		while (intervalStart.isBefore(finalStop)) {
			long lengthInNanos = (long) (length * 1000000000L);
			final LocalDateTime nextStop = intervalStart.plusNanos(lengthInNanos);
			final TimeInterval interval = new TimeInterval(intervalStart, nextStop);

			if (validityInterval.intersects(interval)) {
				intervals.add(interval);
			}
			intervalStart = nextStop;
		}

		return intervals;
	}

	/**
	 * Create a list of AppDataJobs from the given list of time intervals
	 */
	private List<AppDataJob> createJobsFromTimeIntervals(final List<TimeInterval> intervals,
			IpfPreparationJob preparationJob) {
		List<AppDataJob> jobs = new ArrayList<>();

		for (TimeInterval interval : intervals) {
			LOGGER.debug("Create AppDataJob for PDU time interval: [{}; {}]",
					DateUtils.formatToMetadataDateTimeFormat(interval.getStart()),
					DateUtils.formatToMetadataDateTimeFormat(interval.getStop()));
			AppDataJob appDataJob = AppDataJob.fromPreparationJob(preparationJob);
			appDataJob.setStartTime(DateUtils.formatToMetadataDateTimeFormat(interval.getStart()));
			appDataJob.setStopTime(DateUtils.formatToMetadataDateTimeFormat(interval.getStop()));

			if (processSettings.getProcessingGroup() != null) {
				appDataJob.setProcessingGroup(processSettings.getProcessingGroup());
			}

			jobs.add(appDataJob);
		}

		return jobs;
	}

	/*
	 * Add a given offset to a timestamp that is given in string format
	 */
	private String addOffset(String timestamp, long offsetInNanos) {
		LocalDateTime tmp = DateUtils.parse(timestamp);
		final LocalDateTime timestampWithOffset = tmp.plusNanos(offsetInNanos);

		return DateUtils.formatToMetadataDateTimeFormat(timestampWithOffset);
	}

	/*
	 * Calculates an estimate for the second last orbits ANX1 time, based on the
	 * currents orbit ANX time
	 */
	private String estimateANX1BeforeLastOrbit(final String orbitANX) {
		LocalDateTime tmp = DateUtils.parse(orbitANX);
		final LocalDateTime estimate = tmp.minusMinutes(101L);

		return DateUtils.formatToMetadataDateTimeFormat(estimate);
	}
}
