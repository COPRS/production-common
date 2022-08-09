package esa.s1pdgs.cpoc.preparation.worker.type.segment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.DiscardedException;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.TimedOutException;
import esa.s1pdgs.cpoc.preparation.worker.service.AppCatJobService;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.util.QueryUtils;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableFileNameType;

public final class L0SegmentTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {	
	private final MetadataClient metadataClient;
	private final AspPropertiesAdapter aspPropertiesAdapter;

	public L0SegmentTypeAdapter(
			final MetadataClient metadataClient,
			final AspPropertiesAdapter aspPropertiesAdapter
	) {
		this.metadataClient = metadataClient;
		this.aspPropertiesAdapter = aspPropertiesAdapter;
	}
	
	@Override
	public final Optional<AppDataJob> findAssociatedJobFor(final AppCatJobService appCat,
			final CatalogEventAdapter catEvent, final AppDataJob job) throws AbstractCodedException {

		final List<AppDataJob> jobForDataTakeId = appCat.findByProductDataTakeId(catEvent.productType(), catEvent.datatakeId());

		if(jobForDataTakeId == null || jobForDataTakeId.isEmpty()) {
			return Optional.empty();
		}

		if(L0SegmentProduct.of(job).isRfc()) {
			return withSameStartTime(catEvent, jobForDataTakeId);
		}

		return Optional.of(jobForDataTakeId.get(0));
	}

	// S1PRO-2175 also check start time to create different jobs for RFC segments with different
	// start times but same data take id
	private Optional<AppDataJob> withSameStartTime(final CatalogEventAdapter catEvent, final List<AppDataJob> jobsForDatatakeId) {
		
		for(final AppDataJob job: jobsForDatatakeId) {
			if(job.getStartTime().equals(catEvent.validityStartTime())) {
				return  Optional.of(job);
			}
		}
		return Optional.empty();
	}

	@Override
	public final Product mainInputSearch(final AppDataJob job, final TaskTableAdapter taskTableAdapter) throws IpfPrepWorkerInputsMissingException {
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

		// S1PRO-2175 overriding inputs have to be added to the product here as this is the object which will
		// actually update the job later
		final Map<String, List<LevelSegmentMetadata>> segmentsForPolaristions = product.segmentsForPolaristions();
		final List<LevelSegmentMetadata> inputSegmentData = new ArrayList<>();
		segmentsForPolaristions.forEach((key, value) -> inputSegmentData.addAll(value));
		product.overridingInputs(
				createOverridingInputs(
						taskTableAdapter,
						inputSegmentData,
						Collections.emptyList(),
						getInputReferences(taskTableAdapter, product.getProductType()),
						product.getProductType()));
		return product;
	}
	
	@Override
	public void validateInputSearch(final AppDataJob job, final TaskTableAdapter taskTableAdapter) throws IpfPrepWorkerInputsMissingException {
		final L0SegmentProduct product = L0SegmentProduct.of(job);

		if (product.isRfc()) {
			handleRfcSegments(product, taskTableAdapter);
		}
		else {
			handleNonRfSegments(job, product, taskTableAdapter);
		}
	}
	
	// S1PRO-1851: Handling of RFC product 
	private void handleRfcSegments(final L0SegmentProduct product, final TaskTableAdapter taskTableAdapter) {
		final Map<String, List<LevelSegmentMetadata>> segmentsGroupByPol = product.segmentsForPolaristions();

		final List<String> pols = segmentsGroupByPol.entrySet().stream()
				.filter(e -> !e.getValue().isEmpty())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		
		if (pols.size() == 1) {
			throw new DiscardedException(
					String.format("Discarding single RFC product %s", product.getProductName())
			);
		}
		else if (pols.size() != 2) {
			throw new DiscardedException(
					String.format("Discarding due to unexpected number or polarisations for RFC product %s",
							product.getProductName())
			);
		}

		final String polA = pols.get(0);
		final String polB = pols.get(1);
		final List<LevelSegmentMetadata> segmentsA = segmentsGroupByPol.get(polA);
		final List<LevelSegmentMetadata> segmentsB = segmentsGroupByPol.get(polB);
		// Check polarisations
		if (!isDoublePolarisation(polA, polB)) {
			throw new DiscardedException(
					String.format(
							"Discarding RFC %s due to polarisation mismatch: %s", 
							product.getProductName(),
							pols
					)
			);
		}			
		final DateTimeFormatter formatter = AppDataJobProduct.TIME_FORMATTER;
		final String sensingStart = least(getStartSensingDate(segmentsA, formatter), getStartSensingDate(segmentsB, formatter),
				formatter);
		final String sensingStop = more(getStopSensingDate(segmentsA, formatter), getStopSensingDate(segmentsB, formatter),
				formatter);
		product.setStartTime(sensingStart);
		product.setStopTime(sensingStop);
	}

	private List<AppDataJobTaskInputs> createOverridingInputs(final TaskTableAdapter taskTableAdapter,
															  final List<LevelSegmentMetadata> segmentsA,
															  final List<LevelSegmentMetadata> segmentsB,
															  final List<String> references,
															  final String productType) {

		// FIXME these overriding inputs are added as additional inputs to job later
		// AuxQuery however expects a full ist of inputs (those with results and those without results yet)
		// It would perhaps be better to have e.g. a field "preselectedInputs" at the job
		// and leave additionalInputs empty, as it is used by AuxQuery only
		final List<AppDataJobTaskInputs> appDataJobTaskInputs = QueryUtils.buildInitialInputs(taskTableAdapter);

		for(final AppDataJobTaskInputs taskInputs : appDataJobTaskInputs) {
			final List<AppDataJobInput> mergedInputs = new ArrayList<>();

			for(final AppDataJobInput input : taskInputs.getInputs()) {

				final Optional<QueryUtils.TaskAndInput> optionalTask = QueryUtils.getTaskForReference(
						input.getTaskTableInputReference(),
						taskTableAdapter
				);

				final String inputReference = input.getTaskTableInputReference();
				
				final AppDataJobInput mergedInput;
								
				if(references.contains(inputReference) && optionalTask.isPresent()) {
					final TaskTableFileNameType fileNameType = optionalTask.get().getInput().alternativesOrdered()
							.filter(a -> a.getFileType().equals(productType))
							.findAny()
							.map(TaskTableInputAlternative::getFileNameType).orElse(TaskTableFileNameType.BLANK);

					mergedInput = new AppDataJobInput(
						inputReference,
						productType,
						fileNameType.toString(),
						input.isMandatory(),
						toAppDataJobFiles(segmentsA, segmentsB)							
					);
				} else {
					mergedInput = input;
				}
				mergedInputs.add(mergedInput);
			}

			taskInputs.setInputs(mergedInputs);
		}

		return appDataJobTaskInputs;
	}

	private List<String> getInputReferences(final TaskTableAdapter taskTableAdapter, final String forType) {
		// dirty workaround to find the first input as the inputs are not ordered.
		// Assume we are searching for Inputs with FILE_TYPE = RFC in TaskTable:

		// we have to search through all Inputs and find these Inputs which have an alternative with
		// FILE_TYPE = RFC

		// we need the inputReference String for these Inputs
		// so we can use QueryUtils.taskTableTasksAndInputsMappedTo to get these references:

		// for each taskTableInput which has an alternative with FILE_TYPE = RFC
		// => return the referenceString
		// if the taskTableInput has no such Alternative
		// => return an empty String

		// for each task collect all nonEmpty reference strings and return a List of them

		// at the end we have a List<List<String>> which we convert to List<String> using flatMap
		final BiFunction<List<String>, TaskTableTask, List<String>> toReferenceList = (references, task) -> references.stream()
				.filter(StringUtils::hasText)
				.collect(Collectors.toList());

		final BiFunction<String, TaskTableInput, String> toReference = (reference, input) -> {
			if(input.alternativesOrdered().anyMatch(alt -> alt.getFileType().equals(forType))) {
				return reference;
			}
			return "";
		};

		return QueryUtils.taskTableTasksAndInputsMappedTo(
				toReferenceList,
				toReference,
				taskTableAdapter)
				.stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private List<AppDataJobFile> toAppDataJobFiles(final List<LevelSegmentMetadata> segmentsA, final List<LevelSegmentMetadata> segmentsB) {
		final List<AppDataJobFile> files = new ArrayList<>();
		for (final LevelSegmentMetadata segment : segmentsA) {
			files.add(toAppDataJobFile(segment));
		}
		for (final LevelSegmentMetadata segment : segmentsB) {
			files.add(toAppDataJobFile(segment));
		}
		return files;
	}

	private final AppDataJobFile toAppDataJobFile(final LevelSegmentMetadata segment) {
		// Extract t0_pdgs_date if possible to determine when all inputs where ready
		Date t0 = null;
		if (segment.getAdditionalProperties().containsKey("t0_pdgs_date")) {
			t0 = DateUtils.toDate(segment.getAdditionalProperties().get("t0_pdgs_date"));
		}
		
		return new AppDataJobFile(
				segment.getProductName(),
				segment.getKeyObjectStorage(),
				TaskTableAdapter.convertDateToJobOrderFormat(segment.getValidityStart()),
				TaskTableAdapter.convertDateToJobOrderFormat(segment.getValidityStop()),
				t0
		);
	} 

	private void handleNonRfSegments(final AppDataJob job, final L0SegmentProduct product, final TaskTableAdapter taskTableAdapter)
			throws IpfPrepWorkerInputsMissingException {
		// Retrieve the segments
		final Map<String, String> missingMetadata = new HashMap<>();
		final Map<String, List<LevelSegmentMetadata>> segmentsGroupByPol = product.segmentsForPolaristions();

		final List<String> pols = segmentsGroupByPol.entrySet().stream()
				.filter(e -> !e.getValue().isEmpty())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		
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
		
		
		boolean fullCoverage = false;
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
			if (this.aspPropertiesAdapter.isTimeoutReached(job, sensingStop, LocalDateTime.now(ZoneId.of("UTC")))) {
				LOGGER.warn("Continue generation of {} {} even if sensing gaps", product.getProductName(),
						job.getGeneration());
				product.setStartTime(sensingStart);
				product.setStopTime(sensingStop);
				throw new TimedOutException();
			} else {
				throw new IpfPrepWorkerInputsMissingException(missingMetadata);
			}
		} 
		else {
			product.setStartTime(sensingStart);
			product.setStopTime(sensingStop);
		}
		LOGGER.debug("== preSearch: performed lastName: {}, fullCoverage=true ", product.getProductName());
	}

	@Override
	public List<AppDataJob> createAppDataJobs(final IpfPreparationJob job) {
		final AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);
		appDataJob.setTimeoutDate(aspPropertiesAdapter.calculateTimeout(appDataJob));
		
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(appDataJob);
		final L0SegmentProduct product = L0SegmentProduct.of(appDataJob);
		product.setAcquistion(eventAdapter.swathType());
		product.setDataTakeId(eventAdapter.datatakeId());
		product.setProductName("l0_segments_for_" + eventAdapter.datatakeId());
		
		return Collections.singletonList(appDataJob);
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

	private void sortSegmentsPerStartDate(final List<LevelSegmentMetadata> list) {
		list.sort((final LevelSegmentMetadata s1, final LevelSegmentMetadata s2) -> {
			final LocalDateTime startDate1 = LocalDateTime.parse(s1.getValidityStart(),
					AbstractMetadata.METADATA_DATE_FORMATTER);
			final LocalDateTime startDate2 = LocalDateTime.parse(s2.getValidityStart(),
					AbstractMetadata.METADATA_DATE_FORMATTER);
			return startDate1.compareTo(startDate2);
		});
	}

	private boolean isSinglePolarisation(final String polA) {
		return "SH".equals(polA) || "SV".equals(polA);
	}

	private boolean isDoublePolarisation(final String polA, final String polB) {
		if (("VH".equals(polA) && "VV".equals(polB)) || ("VV".equals(polA) && "VH".equals(polB))) {
			return true;
		} 
		return ("HH".equals(polA) && "HV".equals(polB)) || ("HV".equals(polA) && "HH".equals(polB));
	}
	


	private boolean isCovered(final List<LevelSegmentMetadata> sortedSegments) {
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

	private String getStartSensingDate(final List<LevelSegmentMetadata> sortedSegments,
									   final DateTimeFormatter outFormatter) {
		if (CollectionUtils.isEmpty(sortedSegments)) {
			return null;
		}
		final LevelSegmentMetadata segment = sortedSegments.get(0);
		return DateUtils.convertToAnotherFormat(segment.getValidityStart(), AbstractMetadata.METADATA_DATE_FORMATTER,
				outFormatter);
	}

	private String getStopSensingDate(final List<LevelSegmentMetadata> sortedSegments,
									  final DateTimeFormatter outFormatter) {
		if (CollectionUtils.isEmpty(sortedSegments)) {
			return null;
		}
		final LevelSegmentMetadata segment = sortedSegments.get(sortedSegments.size() - 1);
		return DateUtils.convertToAnotherFormat(segment.getValidityStop(), AbstractMetadata.METADATA_DATE_FORMATTER,
				outFormatter);
	}

	private String least(final String a, final String b, final DateTimeFormatter formatter) {
		final LocalDateTime timeA = LocalDateTime.parse(a, formatter);
		final LocalDateTime timeB = LocalDateTime.parse(b, formatter);
		return timeA.isBefore(timeB) ? a : b;
	}

	private String more(final String a, final String b, final DateTimeFormatter formatter) {
		final LocalDateTime timeA = LocalDateTime.parse(a, formatter);
		final LocalDateTime timeB = LocalDateTime.parse(b, formatter);
		return timeA.isAfter(timeB) ? a : b;
	}

	private String extractProductSensingConsolidation(final List<LevelSegmentMetadata> sortedSegments) {
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
