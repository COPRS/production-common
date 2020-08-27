package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.File;
import java.time.Instant;
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
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.S3TypeAdapterSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.QueryUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderProc;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;

public class S3TypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

	private static final Logger LOGGER = LogManager.getLogger(S3TypeAdapter.class);

	private MetadataClient metadataClient;
	private TaskTableFactory ttFactory;
	private ElementMapper elementMapper;
	private ProcessSettings processSettings;
	private IpfPreparationWorkerSettings workerSettings;
	private S3TypeAdapterSettings settings;

	public S3TypeAdapter(final MetadataClient metadataClient, final TaskTableFactory ttFactory,
			final ElementMapper elementMapper, final ProcessSettings processSettings,
			final IpfPreparationWorkerSettings workerSettings, final S3TypeAdapterSettings settings) {
		this.metadataClient = metadataClient;
		this.ttFactory = ttFactory;
		this.elementMapper = elementMapper;
		this.processSettings = processSettings;
		this.workerSettings = workerSettings;
		this.settings = settings;
	}

	@Override
	public Product mainInputSearch(AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		S3Product returnValue = S3Product.of(job);

		// Create tasktable Adapter for tasktable defined by Job
		final File ttFile = new File(workerSettings.getDiroftasktables(), job.getTaskTableName());
		final TaskTableAdapter tasktableAdapter = new TaskTableAdapter(ttFile,
				ttFactory.buildTaskTable(ttFile, processSettings.getLevel()), elementMapper);

		// Get inputs of tasks
		List<AppDataJobTaskInputs> tasks;
		if (isEmpty(job.getAdditionalInputs())) {
			tasks = QueryUtils.buildInitialInputs(workerSettings.getProductMode(), tasktableAdapter);
		} else {
			tasks = job.getAdditionalInputs();
		}

		// Extract a list of all inputs without complete result sets
		List<AppDataJobInput> inputsWithNoResults = tasks.stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives of those inputs
		List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithNoResults, tasktableAdapter,
				workerSettings.getProductMode());

		// Loop over alternatives to execute additional logic per product type
		for (TaskTableInputAlternative alternative : alternatives) {
			try {
				if (settings.getMarginProductTypes().contains(alternative.getFileType())) {
					LOGGER.debug("Use additional logic 'MultipleProductCoverSearch' for product type {}",
							alternative.getFileType());
					MultipleProductCoverSearch mpcSearch = new MultipleProductCoverSearch(tasktableAdapter,
							elementMapper, metadataClient, workerSettings);
					tasks = mpcSearch.updateTaskInputsByAlternative(tasks, alternative, returnValue);
				}
			} catch (final MetadataQueryException me) {
				LOGGER.error("Error on query execution, retrying next time", me);
			}
		}

		returnValue.setAdditionalInputs(tasks);
		return returnValue;
	}

	@Override
	public void customAppDataJob(AppDataJob job) {
		// Nothing to do currently
	}

	@Override
	public void customJobOrder(AppDataJob job, JobOrder jobOrder) {
		LOGGER.debug("Customize Job order for S3...");

		for (JobOrderProc proc : jobOrder.getProcs()) {
			for (int i = 0; i < proc.getInputs().size(); i++) {
				JobOrderInput input = proc.getInputs().get(i);
				if (settings.getMarginProductTypes().contains(input.getFileType())) {
					JobOrderInput newInput = DuplicateProductFilter.filterJobOrderInput(input);
					LOGGER.debug("Update JobOrderInput {}. New Input: {}", input.getFileType(), newInput.toString());

					proc.updateInput(i, newInput);
				}
			}
		}
	}

	@Override
	public void customJobDto(AppDataJob job, IpfExecutionJob dto) {
		// Nothing to do currently
	}

	@Override
	public void validateInputSearch(AppDataJob job) throws IpfPrepWorkerInputsMissingException {
		// Check if timeout is reached -> start job with current input
		if (workerSettings.getWaitprimarycheck().getMaxTimelifeS() != 0) {
			long startTime = job.getGeneration().getCreationDate().toInstant().toEpochMilli();
			long timeoutTime = startTime + (workerSettings.getWaitprimarycheck().getMaxTimelifeS() * 1000);

			if (Instant.now().toEpochMilli() > timeoutTime) {
				// Timeout reached
				LOGGER.info("Timeout reached for job {}. Continue without missing products...");
				setHasResultsToTrueForTimeoutReached(job);
				return;
			}
		}

		// Check that each Input which should have results contains results or that
		// timeout is expired
		final File ttFile = new File(workerSettings.getDiroftasktables(), job.getTaskTableName());
		final TaskTableAdapter taskTableAdapter = new TaskTableAdapter(ttFile,
				ttFactory.buildTaskTable(ttFile, processSettings.getLevel()), elementMapper);

		// Extract a list of all inputs from the tasks
		List<AppDataJobInput> inputsWithNoResults = job.getAdditionalInputs().stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives
		List<TaskTableInputAlternative> alternatives = QueryUtils.alternativesOf(inputsWithNoResults, taskTableAdapter,
				workerSettings.getProductMode());

		// Check if there is an alternative which should have been filled by additional
		// logic
		Map<String, String> missingAlternatives = new HashMap<>();
		for (TaskTableInputAlternative alternative : alternatives) {
			if (settings.getMarginProductTypes().contains(alternative.getFileType())) {
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
	private void setHasResultsToTrueForTimeoutReached(AppDataJob job) {
		for (AppDataJobTaskInputs task : job.getAdditionalInputs()) {
			for (AppDataJobInput input : task.getInputs()) {
				if (!isEmpty(input.getFiles())) {
					input.setHasResults(true);
				}
			}
		}
	}
}
