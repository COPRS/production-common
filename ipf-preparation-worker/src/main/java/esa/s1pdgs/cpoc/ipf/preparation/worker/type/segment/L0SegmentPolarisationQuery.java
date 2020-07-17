package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;

final class L0SegmentPolarisationQuery implements Callable<Void> {
	static final Logger LOGGER = LogManager.getLogger(L0SegmentPolarisationQuery.class); 
	
	private final AppDataJob job;
	private final MetadataClient metadataClient;
	private final long timeoutInputSearchMs;

	public L0SegmentPolarisationQuery(
			final AppDataJob job, 
			final MetadataClient metadataClient,
			final long timeoutInputSearchMs
	) {
		this.job = job;
		this.metadataClient = metadataClient;
		this.timeoutInputSearchMs = timeoutInputSearchMs;
	}

	@Override
	public Void call() throws Exception {
		boolean fullCoverage = false;

		// Retrieve the segments
		final Map<String, String> missingMetadata = new HashMap<>();
		final List<String> pols = new ArrayList<>();
		final Map<String, List<LevelSegmentMetadata>> segmentsGroupByPol = new HashMap<>();

		final L0SegmentProduct product = L0SegmentProduct.of(job);
		final String lastName = product.getProductName();
		final String dataTakeId = product.getDataTakeId();
		
		
		try {			
			for (final LevelSegmentMetadata metadata : metadataClient.getLevelSegments(dataTakeId)) {
				if (!segmentsGroupByPol.containsKey(metadata.getPolarisation())) {
					pols.add(metadata.getPolarisation());
					segmentsGroupByPol.put(metadata.getPolarisation(), new ArrayList<>());
				}
				segmentsGroupByPol.get(metadata.getPolarisation()).add(metadata);
			}			
		}
		catch (final MetadataQueryException e) {
			LOGGER.debug("== preSearch: Exception- Missing segment for lastname {}", lastName);
			missingMetadata.put(lastName, "Missing segment: " + e.getMessage());
		}

		// If missing one segment
		if (!missingMetadata.isEmpty()) {
			LOGGER.debug("== preSearch: Missing other segment for lastname{}", lastName);
			throw new IpfPrepWorkerInputsMissingException(missingMetadata);
		}

		LOGGER.debug("== preSearch00 -segment  {}", segmentsGroupByPol);
		// Check polarisation right
		String sensingStart = null;
		String sensingStop = null;
		if (pols.size() <= 0 || pols.size() > 2) {
			missingMetadata.put(product.getProductName(), "Invalid number of polarisation " + pols.size());
		} else if (pols.size() == 1) {
			// Sort segments
			final String polA = pols.get(0);
			final List<LevelSegmentMetadata> segmentsA = segmentsGroupByPol.get(polA);
			// Check coverage ok
			if (isSinglePolarisation(polA)) {
				sortSegmentsPerStartDate(segmentsA);
				if (isCovered(segmentsA)) {
					fullCoverage = true;
				} else {
					fullCoverage = false;
					missingMetadata.put(product.getProductName(), "Missing segments for the coverage of polarisation " + polA
							+ ": " + extractProductSensingConsolidation(segmentsA));
				}
			} else {
				fullCoverage = false;
				missingMetadata.put(product.getProductName(), "Missing the other polarisation of " + polA);
			}
			// Get sensing start and stop
			sensingStart = getStartSensingDate(segmentsA, AppDataJobProduct.TIME_FORMATTER);
			sensingStop = getStopSensingDate(segmentsA, AppDataJobProduct.TIME_FORMATTER);

		} else {
			final String polA = pols.get(0);
			final String polB = pols.get(1);
			// Sort segments
			final List<LevelSegmentMetadata> segmentsA = segmentsGroupByPol.get(polA);
			final List<LevelSegmentMetadata> segmentsB = segmentsGroupByPol.get(polB);
			// Check coverage ok
			if (isDoublePolarisation(polA, polB)) {
				final boolean fullCoverageA;
				sortSegmentsPerStartDate(segmentsA);
				if (isCovered(segmentsA)) {
					fullCoverageA = true;
				} else {
					fullCoverageA = false;
					missingMetadata.put(product.getProductName(), "Missing segments for the coverage of polarisation " + polA
							+ ": " + extractProductSensingConsolidation(segmentsA));
				}
				final boolean fullCoverageB;
				sortSegmentsPerStartDate(segmentsB);
				if (isCovered(segmentsB)) {
					fullCoverageB = true;
				} else {
					fullCoverageB = false;
					missingMetadata.put(product.getProductName(), "Missing segments for the coverage of polarisation " + polB
							+ ": " + extractProductSensingConsolidation(segmentsB));
				}
				fullCoverage = fullCoverageA && fullCoverageB;
			} else {
				fullCoverage = false;
				missingMetadata.put(product.getProductName(), "Invalid double polarisation " + polA + " - " + polB);
			}
			// Get sensing start and stop
			final DateTimeFormatter formatter = AppDataJobProduct.TIME_FORMATTER;
			sensingStart = least(getStartSensingDate(segmentsA, formatter), getStartSensingDate(segmentsB, formatter),
					formatter);
			sensingStop = more(getStopSensingDate(segmentsA, formatter), getStopSensingDate(segmentsB, formatter),
					formatter);
		}
		
		// Check if we add the coverage
		if (!fullCoverage) {
			final Date currentDate = new Date();
			if (job.getGeneration().getCreationDate().getTime() < currentDate.getTime() - timeoutInputSearchMs) {
				LOGGER.warn("Continue generation of {} {} even if sensing gaps", product.getProductName(),
						job.getGeneration());
				job.setStartTime(sensingStart);
				job.setStopTime(sensingStop);
			} else {
				throw new IpfPrepWorkerInputsMissingException(missingMetadata);
			}
		} 
		else {
			job.setStartTime(sensingStart);
			job.setStopTime(sensingStop);
		}
		LOGGER.debug("== preSearch: performed lastName: {},fullCoverage= {} ", lastName, fullCoverage);
				
		return null;
	}

	private final void sortSegmentsPerStartDate(final List<LevelSegmentMetadata> list) {
		list.sort((final LevelSegmentMetadata s1, final LevelSegmentMetadata s2) -> {
			final LocalDateTime startDate1 = LocalDateTime.parse(s1.getValidityStart(),
					AbstractMetadata.METADATA_DATE_FORMATTER);
			final LocalDateTime startDate2 = LocalDateTime.parse(s2.getValidityStart(),
					AbstractMetadata.METADATA_DATE_FORMATTER);
			return startDate1.compareTo(startDate2);
		});
	}

	private final boolean isSinglePolarisation(final String polA) {
		return "SH".equals(polA) || "SV".equals(polA);
	}

	private final boolean isDoublePolarisation(final String polA, final String polB) {
		if (("VH".equals(polA) && "VV".equals(polB)) || ("VV".equals(polA) && "VH".equals(polB))) {
			return true;
		} 
		return ("HH".equals(polA) && "HV".equals(polB)) || ("HV".equals(polA) && "HH".equals(polB));
	}

	private final boolean isCovered(final List<LevelSegmentMetadata> sortedSegments) {
		if (CollectionUtils.isEmpty(sortedSegments)) {
			return false;
		} else if (sortedSegments.size() == 1) {
			return "FULL".equals(sortedSegments.get(0).getConsolidation());
		} else {
			// Check consolidation first
			// S1PRO-1135 BEGIN instead of START
			if ("BEGIN".equals(sortedSegments.get(0).getProductSensingConsolidation())
					&& "END".equals(sortedSegments.get(sortedSegments.size() - 1).getProductSensingConsolidation())) { // S1PRO-1333
				LocalDateTime previousStopDate = LocalDateTime.parse(sortedSegments.get(0).getValidityStop(),
						AbstractMetadata.METADATA_DATE_FORMATTER);
				for (final LevelSegmentMetadata segment : sortedSegments.subList(1, sortedSegments.size())) {
					final LocalDateTime startDate = LocalDateTime.parse(segment.getValidityStart(),
							AbstractMetadata.METADATA_DATE_FORMATTER);
					if (startDate.isAfter(previousStopDate)) {
						return false;
					}
					previousStopDate = LocalDateTime.parse(segment.getValidityStop(),
							AbstractMetadata.METADATA_DATE_FORMATTER);
				}
				return true;
			} else {
				return false;
			}
		}
	}

	private final String getStartSensingDate(final List<LevelSegmentMetadata> sortedSegments,
			final DateTimeFormatter outFormatter) {
		if (CollectionUtils.isEmpty(sortedSegments)) {
			return null;
		}
		final LevelSegmentMetadata segment = sortedSegments.get(0);
		return DateUtils.convertToAnotherFormat(segment.getValidityStart(), AbstractMetadata.METADATA_DATE_FORMATTER,
				outFormatter);
	}

	private final String getStopSensingDate(final List<LevelSegmentMetadata> sortedSegments,
			final DateTimeFormatter outFormatter) {
		if (CollectionUtils.isEmpty(sortedSegments)) {
			return null;
		}
		final LevelSegmentMetadata segment = sortedSegments.get(sortedSegments.size() - 1);
		return DateUtils.convertToAnotherFormat(segment.getValidityStop(), AbstractMetadata.METADATA_DATE_FORMATTER,
				outFormatter);
	}

	private final String least(final String a, final String b, final DateTimeFormatter formatter) {
		final LocalDateTime timeA = LocalDateTime.parse(a, formatter);
		final LocalDateTime timeB = LocalDateTime.parse(b, formatter);
		return timeA.isBefore(timeB) ? a : b;
	}

	private final String more(final String a, final String b, final DateTimeFormatter formatter) {
		final LocalDateTime timeA = LocalDateTime.parse(a, formatter);
		final LocalDateTime timeB = LocalDateTime.parse(b, formatter);
		return timeA.isAfter(timeB) ? a : b;
	}

	private final String extractProductSensingConsolidation(final List<LevelSegmentMetadata> sortedSegments) {
		final StringBuilder ret = new StringBuilder();
		for (final LevelSegmentMetadata segment : sortedSegments) {
			ret.append(segment.getProductSensingConsolidation());
			ret.append(" ");
			ret.append(segment.getValidityStart());
			ret.append(" ");
			ret.append(segment.getValidityStop());
			ret.append(" | ");
		}
		return ret.toString();
	}
}