package esa.s1pdgs.cpoc.ipf.preparation.worker.type.pdu;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.PDUSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.PDUSettings.PDUTypeSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.DiscardedException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.pdu.PDUType;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.QueryUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3.MultipleProductCoverSearch;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputOrigin;

public class PDUTypeAdapter extends AbstractProductTypeAdapter {

	private static final Logger LOGGER = LogManager.getLogger(PDUTypeAdapter.class);

	private MetadataClient metadataClient;
	private TaskTableFactory ttFactory;
	private ElementMapper elementMapper;
	private ProcessSettings processSettings;
	private IpfPreparationWorkerSettings workerSettings;
	private PDUSettings settings;

	public PDUTypeAdapter(final MetadataClient metadataClient, final TaskTableFactory ttFactory,
			final ElementMapper elementMapper, final ProcessSettings processSettings,
			final IpfPreparationWorkerSettings workerSettings, final PDUSettings settings) {
		this.metadataClient = metadataClient;
		this.ttFactory = ttFactory;
		this.elementMapper = elementMapper;
		this.processSettings = processSettings;
		this.workerSettings = workerSettings;
		this.settings = settings;
	}

	@Override
	public List<AppDataJob> createAppDataJobs(IpfPreparationJob job) throws Exception {
		PDUTypeSettings typeSettings = settings.getConfig().get(job.getEventMessage().getBody().getProductType());

		if (typeSettings != null) {
			if (typeSettings.getType() == PDUType.FRAME) {
				PDUFrameGeneration jobGenerator = new PDUFrameGeneration(typeSettings, metadataClient);
				return jobGenerator.generateAppDataJobs(job);
			}
		}

		return Collections.emptyList();
	}

	@Override
	public void customJobOrder(AppDataJob job, JobOrder jobOrder) {
		String frameNumber = (String) job.getProduct().getMetadata().get(PDUProduct.FRAME_NUMBER);
		if (frameNumber != null) {
			updateProcParam(jobOrder, "MtdPDUFrameNumbers", frameNumber);
		}
	}

	/**
	 * Remove Inputs of ExecutionJob that are referring to a
	 * TaskTableInputAlternative of origin PROC.
	 * 
	 * If those aren't removed, the InputDownloader of the ExecutionWorker will try
	 * to download a file from the OBS but exits with an IllegalArgumentException
	 */
	@Override
	public void customJobDto(AppDataJob job, IpfExecutionJob dto) {
		final TaskTableAdapter ttAdapter = getTTAdapterForTaskTableName(job.getTaskTableName());

		// Create list of all FileTypes of TaskTableInputAlternatives of Origin PROC
		List<String> procAlternatives = ttAdapter.getAllAlternatives().stream()
				.filter(alternative -> alternative.getOrigin() == TaskTableInputOrigin.PROC)
				.map(alternative -> alternative.getFileType()).collect(toList());

		List<LevelJobInputDto> newInputs = new ArrayList<>();
		for (LevelJobInputDto input : dto.getInputs()) {
			File file = new File(input.getLocalPath());
			if (!procAlternatives.contains(file.getName())) {
				newInputs.add(input);
			}
		}

		dto.setInputs(newInputs);
	}

	@Override
	public Product mainInputSearch(AppDataJob job, TaskTableAdapter tasktableAdapter)
			throws IpfPrepWorkerInputsMissingException, DiscardedException {
		final PDUProduct returnValue = PDUProduct.of(job);

		// Get inputs of tasks
		List<AppDataJobTaskInputs> tasks;
		if (isEmpty(job.getAdditionalInputs())) {
			tasks = QueryUtils.buildInitialInputs(tasktableAdapter);
		} else {
			tasks = job.getAdditionalInputs();
		}

		// Extract a list of all inputs without complete result sets
		final List<AppDataJobInput> inputsWithNoResults = tasks.stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives of those inputs
		final List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithNoResults,
				tasktableAdapter, workerSettings.getProductMode());

		// Loop over alternatives to execute additional logic per product type
		for (final TaskTableInputAlternative alternative : alternatives) {
			try {
				PDUTypeSettings typeSettings = settings.getConfig().get(alternative.getFileType());

				if (typeSettings != null) {
					LOGGER.debug("Use additional logic 'MultipleProductCoverSearch (PDU)' for product type {}",
							alternative.getFileType());
					final MultipleProductCoverSearch mpcSearch = new MultipleProductCoverSearch(tasktableAdapter,
							elementMapper, metadataClient, workerSettings);
					tasks = mpcSearch.updateTaskInputs(tasks, alternative, returnValue.getSatelliteId(),
							job.getStartTime(), job.getStopTime(), "NRT");

					/*
					 * In a following step the start and stop time of the job will be set to the
					 * start and stop time of the product. That step is ruining the
					 * RangeSearch-logic so we anticipate that update and preemptively set the
					 * products start and stop time to the ones from the job
					 * 
					 * @see AppDataJobService#updateProduct()
					 */
					returnValue.setStartTime(job.getStartTime());
					returnValue.setStopTime(job.getStopTime());
				}
			} catch (final MetadataQueryException me) {
				LOGGER.error("Error on query execution, retrying next time", me);
			}
		}

		returnValue.setAdditionalInputs(tasks);
		return returnValue;
	}

	@Override
	public void validateInputSearch(final AppDataJob job, final TaskTableAdapter taskTableAdapter)
			throws IpfPrepWorkerInputsMissingException {
		// Check if timeout is reached -> start job with current input
		if (workerSettings.getWaitprimarycheck().getMaxTimelifeS() != 0) {
			final long startTime = job.getGeneration().getCreationDate().toInstant().toEpochMilli();
			final long timeoutTime = startTime + (workerSettings.getWaitprimarycheck().getMaxTimelifeS() * 1000);

			if (Instant.now().toEpochMilli() > timeoutTime) {
				// Timeout reached
				LOGGER.info("Timeout reached for job {}. Continue without missing products...", job.getId());
				setHasResultsToTrueForTimeoutReached(job);
				return;
			}
		}

		// Extract a list of all inputs from the tasks
		final List<AppDataJobInput> inputsWithNoResults = job.getAdditionalInputs().stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives
		final List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithNoResults,
				taskTableAdapter, workerSettings.getProductMode());

		// Check if there is an alternative which should have been filled by additional
		// logic
		final Map<String, String> missingAlternatives = new HashMap<>();
		for (final TaskTableInputAlternative alternative : alternatives) {
			PDUTypeSettings typeSettings = settings.getConfig().get(alternative.getFileType());

			if (typeSettings != null) {
				missingAlternatives.put(alternative.getFileType(),
						"Incomplete result set for " + alternative.getFileType());
			}
		}

		// If there is at least one input incomplete, try again
		if (!missingAlternatives.isEmpty()) {
			throw new IpfPrepWorkerInputsMissingException(missingAlternatives);
		}
	}

	/**
	 * When the timeout was reached set the boolean "hasResults" for all input to
	 * true, that contain at least one product. The boolean "hasResults" is kept at
	 * false from the additional logic, to determine whether or not the list of
	 * products is enough to satisfy the additional constraints
	 * 
	 * @param job job to update
	 */
	private void setHasResultsToTrueForTimeoutReached(final AppDataJob job) {
		for (final AppDataJobTaskInputs task : job.getAdditionalInputs()) {
			for (final AppDataJobInput input : task.getInputs()) {
				if (!isEmpty(input.getFiles())) {
					input.setHasResults(true);
				}
			}
		}
	}
	
	/**
	 * Create a TaskTableAdapter for the given tasktable
	 * 
	 * @param taskTable name of the taskTable
	 * @return TaskTableAdapter to access the tasktable information
	 */
	private TaskTableAdapter getTTAdapterForTaskTableName(final String taskTable) {
		final File ttFile = new File(workerSettings.getDiroftasktables(), taskTable);
		final TaskTableAdapter tasktableAdapter = new TaskTableAdapter(ttFile,
				ttFactory.buildTaskTable(ttFile, processSettings.getLevel()), elementMapper,
				workerSettings.getProductMode());

		return tasktableAdapter;
	}
}
