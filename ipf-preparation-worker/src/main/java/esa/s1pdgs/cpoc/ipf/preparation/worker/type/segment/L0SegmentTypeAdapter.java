package esa.s1pdgs.cpoc.ipf.preparation.worker.type.segment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.appcat.AppCatJobService;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

public final class L0SegmentTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {
	private final MetadataClient metadataClient;
	private final long timeoutInputSearchMs;

	public L0SegmentTypeAdapter(
			final MetadataClient metadataClient,
			final long timeoutInputSearchMs
	) {
		this.metadataClient = metadataClient;
		this.timeoutInputSearchMs = timeoutInputSearchMs;
	}
	
	@Override
	public final Optional<AppDataJob> findAssociatedJobFor(final AppCatJobService appCat, final CatalogEventAdapter catEvent)
			throws AbstractCodedException {
		return appCat.findJobForDatatakeId(catEvent.datatakeId());
	}

	@Override
	public final Product mainInputSearch(final AppDataJob job) throws IpfPrepWorkerInputsMissingException {	
		final L0SegmentProduct product = L0SegmentProduct.of(job);
		
		try {			
			for (final LevelSegmentMetadata metadata : metadataClient.getLevelSegments(product.getDataTakeId())) {				
				LOGGER.debug("Found {} in MDC for {}", metadata.getProductName(),  product.getProductName());
				product.addSegmentMetadata(metadata);
			}			
		}
		catch (final MetadataQueryException e) {
			LOGGER.debug("== preSearch: Exception- Missing segment for lastname {}. Trying next time...", product.getProductName());
		}				
		return product;
	}	
	
	@Override
	public void validateInputSearch(final AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		final L0SegmentProduct product = L0SegmentProduct.of(job);
		
		boolean fullCoverage = false;

		// Retrieve the segments
		final Map<String, String> missingMetadata = new HashMap<>();
		final Map<String, List<LevelSegmentMetadata>> segmentsGroupByPol = product.segmentsForPolaristions();
		final List<String> pols = new ArrayList<>(segmentsGroupByPol.keySet());
		
		// If missing input segment
		if (segmentsGroupByPol.isEmpty()) {
			LOGGER.debug("== preSearch: Missing other segment for lastname {}", product.getProductName());
			throw new IpfPrepWorkerInputsMissingException(
					Collections.singletonMap(product.getProductName(), "Missing product in MDC ")
			);
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
		LOGGER.debug("== preSearch: performed lastName: {},fullCoverage= {} ", product.getProductName(), fullCoverage);
	}

	@Override
	public final void customAppDataJob(final AppDataJob job) {
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(job);
		final L0SegmentProduct product = L0SegmentProduct.of(job);
		product.setAcquistion(eventAdapter.swathType());
		product.setDataTakeId(eventAdapter.datatakeId());
		product.setProductName("l0_segments_for_" + eventAdapter.datatakeId());
	}

	@Override
	public final void customJobOrder(final AppDataJob job, final JobOrder jobOrder) {
		final AppDataJobProductAdapter product = new AppDataJobProductAdapter(job.getProduct());		
        updateProcParam(
        		jobOrder, 
        		"Mission_Id",
        		product.getMissionId()+ product.getSatelliteId()
        );		
	}

	@Override
	public final void customJobDto(final AppDataJob job, final IpfExecutionJob dto) {
        // NOTHING TO DO		
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
