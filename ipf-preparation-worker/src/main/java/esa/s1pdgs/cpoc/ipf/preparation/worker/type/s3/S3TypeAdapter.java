package esa.s1pdgs.cpoc.ipf.preparation.worker.type.s3;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.S3TypeAdapterSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.ProductMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.ElementMapper;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableFactory;
import esa.s1pdgs.cpoc.ipf.preparation.worker.query.QueryUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.ipf.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputOrigin;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;

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

		// Workaround to implement MarginTTWFX

		// Create tasktable Adapter
		final File ttFile = new File(workerSettings.getDiroftasktables(), job.getTaskTableName());
		final TaskTableAdapter tasktableAdapter = new TaskTableAdapter(ttFile,
				ttFactory.buildTaskTable(ttFile, processSettings.getLevel()), elementMapper);

		// Get Inputs for WFX
		List<AppDataJobTaskInputs> tasks;
		if (isEmpty(job.getAdditionalInputs())) {
			tasks = QueryUtils.buildInitialInputs(settings.getMode(), tasktableAdapter);
		} else {
			tasks = job.getAdditionalInputs();
		}

		// Extract a list of all inputs from the tasks
		List<AppDataJobInput> inputsWithNoResults = tasks.stream()
				.flatMap(taskInputs -> taskInputs.getInputs().stream().filter(input -> !input.getHasResults()))
				.collect(toList());

		// Create list of alternatives
		List<TaskTableInputAlternative> alternatives = alternativesOf(inputsWithNoResults, tasktableAdapter);

		// Check if there is an alternative which should be filled by this workaround
		for (TaskTableInputAlternative alternative : alternatives) {
			if (settings.getMarginProductTypes().contains(alternative.getFileType())) {
				try {
					// WFX Logic
					// TODO: Currently only NRT, is timeliness part of AppDataJobProductMetadata?
					String timeliness = "NRT";
					List<S3Metadata> products = metadataClient.getProductsForMarginWFX(alternative.getFileType(),
							elementMapper.inputFamilyOf(alternative.getFileType()), returnValue.getSatelliteId(),
							returnValue.getStartTime(), returnValue.getStopTime(), alternative.getDeltaTime0(),
							alternative.getDeltaTime1(), timeliness);

					// Check coverage
					if (!products.isEmpty()) {
						boolean intervalCovered = checkCoverage(returnValue.getStartTime(), returnValue.getStopTime(),
								alternative.getDeltaTime0(), alternative.getDeltaTime1(), "NRT", products);

						// Set results on matching tasks
						tasks = updateAppDataJobTaskInputs(tasks, products, intervalCovered, alternative,
								settings.getMode(), tasktableAdapter);
					}

				} catch (final MetadataQueryException me) {
					LOGGER.error("Error on query execution, retrying next time", me);
				}
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
		// Nothing to do currently
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
		List<TaskTableInputAlternative> alternatives = alternativesOf(inputsWithNoResults, taskTableAdapter);

		// Check if there is an alternative which should have been filled by this workaround
		Map<String, String> missingAlternatives = new HashMap<>();
		for (TaskTableInputAlternative alternative : alternatives) {
			if (settings.getMarginProductTypes().contains(alternative.getFileType())) {
				missingAlternatives.put(alternative.getFileType(), "Incomplete result set for " + alternative.getFileType());
			}
		}
		
		// If there is at least one input incomplete, try again
		if (!missingAlternatives.isEmpty()) {
			throw new IpfPrepWorkerInputsMissingException(missingAlternatives);
		}
	}

	/**
	 * Creates a list of alternatives for the given list of inputs
	 * 
	 * @param inputs           list of inputs for which the alternatives should be
	 *                         extracted
	 * @param taskTableAdapter adapter to tasktable
	 * @return list of alternatives
	 */
	private List<TaskTableInputAlternative> alternativesOf(final List<AppDataJobInput> inputs,
			final TaskTableAdapter taskTableAdapter) {

		final List<String> inputReferences = inputs.stream().map(AppDataJobInput::getTaskTableInputReference)
				.collect(toList());

		final Map<String, List<TaskTableInputAlternative>> taskTableAlternativesMappedToReferences = QueryUtils
				.taskTableTasksAndInputsMappedTo((reference, input) -> singletonMap(reference, input.getAlternatives()),
						(list, task) -> list, settings.getMode(), taskTableAdapter)
				.stream().flatMap(Collection::stream).collect(toList()).stream().flatMap(map -> map.entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		return taskTableAlternativesMappedToReferences.entrySet().stream()
				.filter(entry -> inputReferences.contains(entry.getKey())).flatMap(entry -> entry.getValue().stream())
				.filter(alt -> alt.getOrigin() == TaskTableInputOrigin.DB).distinct().collect(toList());
	}

	/**
	 * Check if the given list of products is enough to cover the interval
	 */
	private boolean checkCoverage(final String startTime, final String stopTime, final double t0, final double t1,
			final String timeliness, final List<S3Metadata> products) {
		if (timeliness.equals("NRT")) {
			return checkCoverageNRT(startTime, stopTime, t0, t1, products);
		}

		return true;
	}

	/**
	 * For NRT the start and stop time should be covered (aka. the earliest start
	 * Time is before startTime - t0 and the latest stop time is after stopTime +
	 * t1) and the granule numbers have to be continuous
	 */
	private boolean checkCoverageNRT(final String startTime, final String stopTime, final double t0, final double t1,
			final List<S3Metadata> products) {
		final S3Metadata first = products.get(0);
		final S3Metadata last = products.get(products.size() - 1);

		LocalDateTime time = LocalDateTime.parse(startTime,
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		final LocalDateTime coverageMin = time.plusSeconds(Math.round(-t0));

		time = LocalDateTime.parse(stopTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		final LocalDateTime coverageMax = time.plusSeconds(Math.round(t1));

		final LocalDateTime firstStart = LocalDateTime.parse(first.getValidityStart(),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));
		final LocalDateTime lastStop = LocalDateTime.parse(last.getValidityStop(),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"));

		if (firstStart.isAfter(coverageMin)) {
			LOGGER.info("CheckCoverage: First start time is after interval beginning. Interval is not covered");
			return false;
		}

		if (lastStop.isBefore(coverageMax)) {
			LOGGER.info("CheckCoverage: Last stop time is before interval ending. Interval is not covered");
			return false;
		}

		return isGranuleContinuous(products);
	}

	/**
	 * Check if the granule numbers are continuous.
	 * 
	 * Edge case: if the granule position is LAST the position of the successor has
	 * to be FIRST
	 * 
	 * @param products List of products which should be checked for continuity
	 * @return true if list is continuous, false if not
	 */
	private boolean isGranuleContinuous(List<S3Metadata> products) {

		for (int i = 0; i < products.size() - 1; i++) {
			S3Metadata product = products.get(i);
			S3Metadata successor = products.get(i + 1);

			if (product.getGranulePosition().equals("LAST")) {
				if (!successor.getGranulePosition().equals("FIRST")) {
					return false;
				}
			} else {
				if (product.getGranuleNumber() + 1 != successor.getGranuleNumber()) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Distributes the products to the list of tasks based on the alternative the
	 * products are retrieved for.
	 * 
	 * @param tasks       list of tasks
	 * @param products    products, that should be distributed
	 * @param complete    flag if the interval specified by the tasktable is covered
	 * @param alternative tasktable alternative to determine the correct tasks
	 * @return updated list of tasks
	 */
	private List<AppDataJobTaskInputs> updateAppDataJobTaskInputs(List<AppDataJobTaskInputs> tasks,
			List<S3Metadata> products, boolean complete, TaskTableInputAlternative alternative, ProductMode mode,
			TaskTableAdapter taskTableAdapter) {
		Map<String, TaskTableInput> taskTableInputs = QueryUtils
				.taskTableTasksAndInputsMappedTo(Collections::singletonMap, (list, task) -> list, mode,
						taskTableAdapter)
				.stream().flatMap(Collection::stream).flatMap(map -> map.entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		for (AppDataJobTaskInputs task : tasks) {
			for (AppDataJobInput input : task.getInputs()) {
				TaskTableInput ttInput = taskTableInputs.get(input.getTaskTableInputReference());
				if (ttInput.getAlternatives().contains(alternative)) {
					input.setHasResults(complete);
					input.setFileNameType(alternative.getFileNameType().toString());
					input.setFileType(alternative.getFileType());
					input.setMandatory(TaskTableMandatoryEnum.YES.equals(ttInput.getMandatory()));
					input.setFiles(convertMetadataToAppDataJobFiles(products));
				}
			}
		}

		return tasks;
	}

	/**
	 * Convert list of metadata to needed objects
	 * 
	 * @return list of AppDataJobFile
	 */
	private List<AppDataJobFile> convertMetadataToAppDataJobFiles(final List<S3Metadata> products) {
		List<AppDataJobFile> files = new ArrayList<>();

		for (S3Metadata product : products) {
			files.add(new AppDataJobFile(product.getProductType(), product.getKeyObjectStorage(),
					product.getValidityStart(), product.getValidityStop()));
		}
		return files;
	}

}
