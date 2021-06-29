package esa.s1pdgs.cpoc.ipf.preparation.worker.type.slice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobPreselectedInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.util.AppDataJobProductAdapter;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableInputAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.timeout.InputTimeoutChecker;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.client.SearchMetadataQuery;
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;

public final class LevelSliceTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {
	private static final String[] AUX_ORB_TYPES = new String[] {
			"AUX_POEORB",
			"AUX_RESORB",
			"AUX_PREORB"
	};
	
	private final MetadataClient metadataClient;
	private final Map<String, Float> sliceOverlap;
	private final Map<String, Float> sliceLength;
	private final Map<String,String> timelinessMapping;
	private final Function<TaskTable, InputTimeoutChecker> timeoutCheckerF;
	
	public LevelSliceTypeAdapter(
			final MetadataClient metadataClient,
			final Map<String, Float> sliceOverlap,
			final Map<String, Float> sliceLength,
			final Map<String,String> timelinessMapping,
			final Function<TaskTable, InputTimeoutChecker> timeoutChecker
	) {
		this.metadataClient = metadataClient;
		this.sliceOverlap = sliceOverlap;
		this.sliceLength = sliceLength;
		this.timelinessMapping = timelinessMapping;
		this.timeoutCheckerF = timeoutChecker;
	}

	@Override
	public final Product mainInputSearch(final AppDataJob job, final TaskTableAdapter taskTableAdapter) 
			throws IpfPrepWorkerInputsMissingException {		
		final LevelSliceProduct product = LevelSliceProduct.of(job);
		
		// Retrieve instrument configuration id and slice number
		try {
			final L0SliceMetadata file = metadataClient.getL0Slice(product.getProductName());
			product.setInsConfId(file.getInstrumentConfigurationId());
			product.setNumberSlice(file.getNumberSlice());
			product.setDataTakeId(file.getDatatakeId());
			product.addSlice(file);
			
			final TaskTableInputAdapter ttInput = taskTableAdapter
					.firstInputContainingOneOf(product.getProductType())
					.orElseThrow(() -> new RuntimeException(
							String.format(
									"Could not find input SLICE using type %s in %s", 
									product.getProductType(), 
									taskTableAdapter
							)
					));
			
			final TaskTableInputAlternative alt = ttInput
					.getAlternativeForType(product.getProductType())
					.orElseThrow(() -> new RuntimeException(
							String.format(
									"Could not find SLICE alternative type %s (input %s) in %s", 
									product.getProductType(), 
									ttInput.getReference(),
									taskTableAdapter
							)
					));
			
			final AppDataJobPreselectedInput preselected = new AppDataJobPreselectedInput();
			preselected.setTaskTableInputReference(ttInput.getReference());
			preselected.setFileType(alt.getFileType());		
			preselected.setFileNameType(alt.getFileNameType().toString());
			
			final AppDataJobFile appJobFile = new AppDataJobFile(
					file.getProductName(), 
					file.getKeyObjectStorage(), 
					TaskTableAdapter.convertDateToJobOrderFormat(file.getValidityStart()),
					TaskTableAdapter.convertDateToJobOrderFormat(file.getValidityStop()),
					file.getAdditionalProperties()
			);
			preselected.setFiles(Collections.singletonList(appJobFile));

			LOGGER.debug("Adding preselected SLICE input: {}", preselected);			
			product.addPreselectedInputs(preselected);
			
		} catch (final MetadataQueryException e) {
			LOGGER.debug("L0 slice for {} not found in MDC (error was {}). Trying next time...", product.getProductName(), 
					Exceptions.messageOf(e));
		}
		// Retrieve Total_Number_Of_Slices
		try {
			final L0AcnMetadata acn = metadataClient.getFirstACN(
					product.getProductName(),
					product.getProcessMode()
			);
			product.setTotalNbOfSlice(acn.getNumberOfSlices());
			product.setSegmentStartDate(acn.getValidityStart());
			product.setSegmentStopDate(acn.getValidityStop());
			product.addAcn(acn);		
			
		} catch (final MetadataQueryException e) {
			LOGGER.debug("L0 acn for {} not found in MDC (error was {}). Trying next time...", product.getProductName(), 
					Exceptions.messageOf(e));
			
			// S1PRO-2476: omit querying for AUX_RES & Co. ...
			return product;
		}
				
		// S1PRO-2476: For AUX_RESORB, POE or PREORB, start-/stop times from ACN shall be used
		// i.e. the "normal" LatestValCover query will not work. So we are adding these files here and not in AuxQuery
		final Optional<TaskTableInputAdapter> opt = taskTableAdapter.firstInputContainingOneOf(AUX_ORB_TYPES);
		
		if (opt.isPresent()) {			
			LOGGER.debug("Performing 'special' query on products {} to use different start/stop times "
					+ "(segStart:{}, segStop:{})", AUX_ORB_TYPES, product.getSegmentStartDate(), 
					product.getSegmentStopDate());
			
			final TaskTableInputAdapter ttInput = opt.get();
			
			final AppDataJobProductAdapter productAdapter = new AppDataJobProductAdapter(job.getProduct());
			
			final InputTimeoutChecker timeoutChecker = timeoutCheckerF.apply(taskTableAdapter.taskTable());	
			
			boolean foundAux = false;
						
			for (final TaskTableInputAlternative alternative : ttInput.getInput().getAlternatives()) {		
				final SearchMetadataQuery query = taskTableAdapter.metadataSearchQueryFor(alternative);
								
				try {	
					LOGGER.debug("Checking for {} using  (start:{}, stop:{})", alternative.getFileType(), 
							product.getSegmentStartDate(), product.getSegmentStopDate());
					
					final List<SearchMetadata> queryResults = Retries.performWithRetries(
							() -> metadataClient.search(
									query,
									sanitizeDateString(product.getSegmentStartDate()),
									sanitizeDateString(product.getSegmentStopDate()),
									productAdapter.getSatelliteId(),
									productAdapter.getInsConfId(),
									productAdapter.getProcessMode(),
									"NONE" // AUX_RES doesn't have a polarisation (i hope)
							), 
							"Query " + query, 
							3, // TODO /FIXME make configurable
							5000L // TODO /FIXME make configurable
					);
								
					if (!queryResults.isEmpty()) {									
						final AppDataJobPreselectedInput preselected = new AppDataJobPreselectedInput();
						preselected.setTaskTableInputReference(ttInput.getReference());
						preselected.setFileType(alternative.getFileType());		
						preselected.setFileNameType(alternative.getFileNameType().toString());
						
						final List<AppDataJobFile> files = new ArrayList<>();
						for (final SearchMetadata meta : queryResults) {														
							files.add(new AppDataJobFile(
									meta.getProductName(), 
									meta.getKeyObjectStorage(), 
									TaskTableAdapter.convertDateToJobOrderFormat(meta.getValidityStart()),
									TaskTableAdapter.convertDateToJobOrderFormat(meta.getValidityStop()),
									meta.getAdditionalProperties()
							));
						}
						preselected.setFiles(files);
						
						LOGGER.debug("Adding preselected inputs: {}", preselected);
						product.addPreselectedInputs(preselected);
						
						foundAux = true;
						break; // don't query any further
					}			
				} catch (final InterruptedException e) {
					LOGGER.error(e);
					throw new IpfPrepWorkerInputsMissingException(Collections.emptyMap());
				}
			}	
			// S1PRO-2600: check timeout AFTER querying all alternatives...
			// make the timeout check here because here we still got the TaskTableInput
			if (!foundAux && !timeoutChecker.isTimeoutExpiredFor(job, ttInput.getInput())) {
				LOGGER.debug("Waiting for timeout on {}", ttInput.getInput());
				throw new IpfPrepWorkerInputsMissingException(
						Collections.singletonMap(
								ttInput.getReference() + " is missing", 
								ttInput.getInput().toLogMessage()
						)
				);
			}
		}
		return product;
	}
	
	private String sanitizeDateString(final String metadataFormat) {
		return DateUtils.convertToAnotherFormat(metadataFormat, AppDataJobProduct.TIME_FORMATTER,
				AbstractMetadata.METADATA_DATE_FORMATTER);
	}

	@Override
	public void validateInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdpter) throws IpfPrepWorkerInputsMissingException {
		final LevelSliceProduct product = LevelSliceProduct.of(job);
		
		// there needs to be a slice
		if (product.getSlices().isEmpty()) {
			throw new IpfPrepWorkerInputsMissingException(
					Collections.singletonMap(
							product.getProductName(), 
							"No Slice: " + 	product.getProductName()
					)
			);
		}
		
		// and there needs to be an ACN
		if (product.getAcns().isEmpty()) {
			throw new IpfPrepWorkerInputsMissingException(	
					Collections.singletonMap(
							product.getProductName(), 
							"No ACNs: " + product.getProductName()
					)
			);
		}
		// if both are there, job creation can proceed
		LOGGER.info("Found slice {} and ACN {}", product.getSlices(), product.getAcns());
	}

	@Override
	public List<AppDataJob> createAppDataJobs(final IpfPreparationJob job) {
		final AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);
		
		final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(appDataJob);
		final LevelSliceProduct product = LevelSliceProduct.of(appDataJob);		
		product.setAcquisition(eventAdapter.swathType()); 
        product.setPolarisation(eventAdapter.polarisation());
        
        return Collections.singletonList(appDataJob);
	}

	@Override
	public final void customJobOrder(final AppDataJob job, final JobOrder jobOrder) {
		final LevelSliceProduct product = LevelSliceProduct.of(job);
		
		// Rewrite job order sensing time
		final String jobOrderStart = DateUtils.convertToAnotherFormat(product.getSegmentStartDate(),
				AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER);
		final String jobOrderStop = DateUtils.convertToAnotherFormat(product.getSegmentStopDate(),
				AppDataJobProduct.TIME_FORMATTER, JobOrderSensingTime.DATETIME_FORMATTER);
		
		jobOrder.getConf().setSensingTime(new JobOrderSensingTime(jobOrderStart, jobOrderStop));

		updateProcParam(
				jobOrder, 
				"Mission_Id",
				product.getMissionId() + product.getSatelliteId());
		updateProcParam(
				jobOrder, 
				"Slice_Number", 
				String.valueOf(product.getNumberSlice()));
		updateProcParam(
				jobOrder, 
				"Total_Number_Of_Slices",
				String.valueOf(product.getTotalNbOfSlice()));
		updateProcParam(
				jobOrder, 
				"Slice_Overlap",
				String.valueOf(sliceOverlap.get(product.getAcquisition())));
		updateProcParam(
				jobOrder, 
				"Slice_Length",
				String.valueOf(sliceLength.get(product.getAcquisition())));
		
		// S1PRO-2459: check if providing default values is sufficient
//		updateProcParam(
//				jobOrder, 
//				"Slicing_Flag", 
//				"TRUE"
//		);
		
		// S1PRO-2194: evaluate mapped timeliness value. Keep default, if there is no
		// corresponding mapping configured;
		final String inputTimeliness = product.getTimeliness(); // is an empty string if not defined
		
		final String timelinessInJoborder = timelinessMapping.get(inputTimeliness);
		if (timelinessInJoborder != null) {
			LOGGER.debug("Adding 'Timeliness_Category' value '{}' to joborder of job {} (input was: {})", 
					timelinessInJoborder, job.getId(), inputTimeliness);
			updateProcParamIfDefined(
					jobOrder, 
					"Timeliness_Category", 
					timelinessInJoborder
			);
		}
		else {
			LOGGER.trace("Omitting provision of timeliness value {} (no mapping defined in {})",  
					inputTimeliness, timelinessMapping);
		}
	}

	@Override
	public final void customJobDto(final AppDataJob job, final IpfExecutionJob dto) {
		// NOTHING TO DO
		
	}	
}
