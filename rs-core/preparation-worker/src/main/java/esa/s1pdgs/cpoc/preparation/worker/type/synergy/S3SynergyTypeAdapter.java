package esa.s1pdgs.cpoc.preparation.worker.type.synergy;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobInput;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobTaskInputs;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.type.S3SynergyProperties;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.DiscardedException;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.TimedOutException;
import esa.s1pdgs.cpoc.preparation.worker.service.AppCatJobService;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.ElementMapper;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableFactory;
import esa.s1pdgs.cpoc.preparation.worker.type.AbstractProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.Product;
import esa.s1pdgs.cpoc.preparation.worker.type.ProductTypeAdapter;
import esa.s1pdgs.cpoc.preparation.worker.type.s3.DuplicateProductFilter;
import esa.s1pdgs.cpoc.preparation.worker.util.QueryUtils;
import esa.s1pdgs.cpoc.xml.model.joborder.AbstractJobOrderProc;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableMandatoryEnum;

public class S3SynergyTypeAdapter extends AbstractProductTypeAdapter implements ProductTypeAdapter {

	private static final Logger LOGGER = LogManager.getLogger(S3SynergyTypeAdapter.class);

	private MetadataClient metadataClient;
	private ElementMapper elementMapper;
	private TaskTableFactory ttFactory;
	private ProcessProperties processSettings;
	private PreparationWorkerProperties workerSettings;
	private S3SynergyProperties settings;

	public S3SynergyTypeAdapter(final MetadataClient metadataClient, final ElementMapper elementMapper,
			final TaskTableFactory ttFactory, final ProcessProperties processSettings,
			final PreparationWorkerProperties workerSettings, final S3SynergyProperties settings) {
		this.metadataClient = metadataClient;
		this.elementMapper = elementMapper;
		this.ttFactory = ttFactory;
		this.processSettings = processSettings;
		this.workerSettings = workerSettings;
		this.settings = settings;
	}

	@Override
	public List<AppDataJob> createAppDataJobs(IpfPreparationJob job) throws Exception {
		AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);
		if (processSettings.getProcessingGroup() != null) {
			appDataJob.setProcessingGroup(processSettings.getProcessingGroup());
		}

		// Add more metadata to AppDataJob
		appDataJob.getProduct().getMetadata().putAll(job.getCatalogEvent().getMetadata());

		// Calculate interval for job
		calculateJobInterval(appDataJob);

		LOGGER.info("Try to create AppDataJob for product {} with interval [{}, {}]", appDataJob.getProductName(),
				appDataJob.getStartTime(), appDataJob.getStopTime());

		return Collections.singletonList(appDataJob);
	}

	@Override
	public Optional<AppDataJob> findAssociatedJobFor(AppCatJobService appCat, CatalogEventAdapter catEvent,
			AppDataJob job) throws AbstractCodedException {
		// Extract if there already is a Job in the database for the given start and
		// stop time for this productType
		final String productType = job.getProductName().substring(4, 15);

		final List<AppDataJob> jobsInDatabase = appCat.findByProductType(productType);

		for (AppDataJob databaseJob : jobsInDatabase) {
			if (job.getStartTime().equals(databaseJob.getStartTime())
					&& job.getStopTime().equals(databaseJob.getStopTime())) {
				LOGGER.info("Found a matching job in database: AppDataJob {}", databaseJob.getId());

				return Optional.of(databaseJob);
			}
		}

		return Optional.empty();
	}

	@Override
	public Product mainInputSearch(AppDataJob job, TaskTableAdapter tasktableAdapter)
			throws IpfPrepWorkerInputsMissingException, DiscardedException {
		final S3SynergyProduct product = S3SynergyProduct.of(job);

		List<AppDataJobTaskInputs> tasks = QueryUtils.buildInitialInputs(tasktableAdapter);
		List<S3Metadata> synergyProducts = new ArrayList<>();

		// Search all products in the specified time range of the job
		try {
			synergyProducts = metadataClient.getProductsInRange(product.getProductType(),
					elementMapper.inputFamilyOf(product.getProductType()), product.getSatelliteId(), job.getStartTime(),
					job.getStopTime(), 0, 0);

			synergyProducts = DuplicateProductFilter.filterS3Metadata(synergyProducts);
		} catch (MetadataQueryException e) {
			LOGGER.error("Error while querying metadata: {}", e.getMessage());
		}

		final Map<String, TaskTableInput> taskTableInputs = QueryUtils
				.taskTableTasksAndInputsMappedTo((list, task) -> list, Collections::singletonMap, tasktableAdapter)
				.stream().flatMap(Collection::stream).flatMap(map -> map.entrySet().stream())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Add all found inputs to task
		for (final AppDataJobTaskInputs task : tasks) {
			for (final AppDataJobInput input : task.getInputs()) {
				final TaskTableInput ttInput = taskTableInputs.get(input.getTaskTableInputReference());
				for (final TaskTableInputAlternative alternative : ttInput.getAlternatives()) {
					if (alternative.getFileType().equals(product.getProductType())) {
						input.setHasResults(true);
						input.setFileNameType(alternative.getFileNameType().toString());
						input.setFileType(alternative.getFileType());
						input.setMandatory(TaskTableMandatoryEnum.YES.equals(ttInput.getMandatory()));
						input.setFiles(convertMetadataToAppDataJobFiles(synergyProducts));
					}
				}
			}
		}

		// In a following step, the job gets its start and stop time overwritten with
		// the ones of the product
		product.setStartTime(job.getStartTime());
		product.setStopTime(job.getStopTime());

		product.setAdditionalInputs(tasks);
		return product;
	}

	@Override
	public void validateInputSearch(AppDataJob job, TaskTableAdapter tasktableAdpter)
			throws IpfPrepWorkerInputsMissingException, DiscardedException, TimedOutException {
		// No validation happening here. There will always be at least one input (the
		// one triggering the job), and we can't expect to have all products at any
		// time. We will produce what we got and send the job to the execution worker.
	}

	@Override
	public void customJobOrder(AppDataJob job, JobOrder jobOrder) {
		TaskTableAdapter taskTableAdapter = getTTAdapterForTaskTableName(job.getTaskTableName());

		LOGGER.debug("Fill dynamic process parameters based on tasktable {}", job.getTaskTableName());
		/*
		 * For each dynamic process parameter defined in the tasktable do the following:
		 * 
		 * 1. Extract the default value 2. If we have a static configuration for this
		 * parameter name in the s3 type settings, use that value 3. If the parameter
		 * name is part of the main product metadata, use the value of the metadata
		 * 
		 * If the resulting value is not null, write the parameter on the job order
		 */
		taskTableAdapter.taskTable().getDynProcParams().forEach(dynProcParam -> {
			LOGGER.trace("Handle dynamic process parameter \"{}\"", dynProcParam.getName());
			String result = dynProcParam.getDefaultValue();

			if (this.settings.getDynProcParams().containsKey(dynProcParam.getName())) {
				result = this.settings.getDynProcParams().get(dynProcParam.getName());
			}

			if (job.getProduct().getMetadata().containsKey(dynProcParam.getName())) {
				result = job.getProduct().getMetadata().get(dynProcParam.getName()).toString();
			}

			if (result != null) {
				LOGGER.trace("Dynamic process parameter got value {}", result);
				updateProcParam(jobOrder, dynProcParam.getName(), result);
			}
		});

		/*
		 * Remove optional outputs from last proc, except for configured additional
		 * outputs
		 */
		if (!jobOrder.getProcs().isEmpty()) {
			AbstractJobOrderProc proc = jobOrder.getProcs().get(jobOrder.getProcs().size() - 1);
			String outputFileType = (job.getProductName().matches(".*SY_2_VGK___.*") ? "SY_2_VG1___" : "SY_2_V10___");

			proc.setOutputs(proc.getOutputs().stream()
					.filter(output -> output.isMandatory() || output.getFileType().equals(outputFileType))
					.collect(toList()));
		}
	}

	@Override
	public void customJobDto(AppDataJob job, IpfExecutionJob dto) {
		// Nothing to do currently
	}

	/*
	 * Calculate the start and stop boundaries for the job. This is necessary as
	 * those differ from the ones of the product
	 * 
	 * VGK -> VG1. VG1 shall have a duration of 1 day
	 * 
	 * VG1 -> V10. V10 shall be from 1. to 10. of month, 11. to 20. and 21. to end
	 * of month (28.-31. depending on month)
	 */
	private void calculateJobInterval(AppDataJob job) {
		LocalDateTime basicStopTime = DateUtils.parse(job.getStopTime());

		String productName = job.getProductName();

		LocalDateTime intervalStart = DateUtils.parse(job.getStartTime());
		LocalDateTime intervalStop = basicStopTime;

		LocalTime startTime = LocalTime.of(0, 0, 0, 0);
		LocalTime stopTime = startTime.minusMinutes(1);

		if (productName.matches(".*SY_2_VGK___.*")) {
			// VGK -> VG1
			LocalDate date = basicStopTime.toLocalDate();
			intervalStart = LocalDateTime.of(date, startTime);
			intervalStop = LocalDateTime.of(date, stopTime);

			LOGGER.debug("Calculated interval for VG1 AppDataJob: [{}, {}]",
					intervalStart.format(DateUtils.METADATA_DATE_FORMATTER),
					intervalStop.format(DateUtils.METADATA_DATE_FORMATTER));
		} else if (productName.matches(".*SY_2_VG1___.*")) {
			// VG1 -> V10
			LocalDate date = basicStopTime.toLocalDate();
			int dayOfMonth = date.getDayOfMonth();

			if (dayOfMonth >= 1 && dayOfMonth <= 10) {
				LocalDate startDate = date.withDayOfMonth(1);
				LocalDate stopDate = date.withDayOfMonth(10);

				intervalStart = LocalDateTime.of(startDate, startTime);
				intervalStop = LocalDateTime.of(stopDate, stopTime);
			} else if (dayOfMonth >= 11 && dayOfMonth <= 20) {
				LocalDate startDate = date.withDayOfMonth(11);
				LocalDate stopDate = date.withDayOfMonth(20);

				intervalStart = LocalDateTime.of(startDate, startTime);
				intervalStop = LocalDateTime.of(stopDate, stopTime);
			} else if (dayOfMonth >= 21 && dayOfMonth <= 31) {
				LocalDate startDate = date.withDayOfMonth(21);
				LocalDate stopDate = date.plusMonths(1).withDayOfMonth(1).minusDays(1);

				intervalStart = LocalDateTime.of(startDate, startTime);
				intervalStop = LocalDateTime.of(stopDate, stopTime);
			}

			LOGGER.debug("Calculated interval for V10 AppDataJob: [{}, {}]",
					intervalStart.format(DateUtils.METADATA_DATE_FORMATTER),
					intervalStop.format(DateUtils.METADATA_DATE_FORMATTER));
		}

		job.setStartTime(DateUtils.formatToMetadataDateTimeFormat(intervalStart));
		job.setStopTime(DateUtils.formatToMetadataDateTimeFormat(intervalStop));
	}

	/**
	 * Convert list of metadata to needed objects
	 * 
	 * @return list of AppDataJobFile
	 */
	private List<AppDataJobFile> convertMetadataToAppDataJobFiles(final List<S3Metadata> products) {
		final List<AppDataJobFile> files = new ArrayList<>();

		for (final S3Metadata product : products) {
			// Extract t0PdgsDate if possible to determine when all inputs where ready
			Date t0 = null;
			if (product.getAdditionalProperties().containsKey("t0PdgsDate")) {
				t0 = DateUtils.toDate(product.getAdditionalProperties().get("t0PdgsDate"));
			}

			files.add(new AppDataJobFile(product.getProductName(), product.getKeyObjectStorage(),
					product.getValidityStart(), product.getValidityStop(), t0));
		}
		return files;
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
				ttFactory.buildTaskTable(ttFile, processSettings.getLevel(), workerSettings.getPathTaskTableXslt()),
				elementMapper, workerSettings.getProductMode());

		return tasktableAdapter;
	}
}
