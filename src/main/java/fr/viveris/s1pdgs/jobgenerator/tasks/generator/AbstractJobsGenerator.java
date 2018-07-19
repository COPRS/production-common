package fr.viveris.s1pdgs.jobgenerator.tasks.generator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.controller.JobsProducer;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobInputDto;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobOutputDto;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobPoolDto;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobTaskDto;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException.ErrorCode;
import fr.viveris.s1pdgs.jobgenerator.exception.BuildTaskTableException;
import fr.viveris.s1pdgs.jobgenerator.exception.InputsMissingException;
import fr.viveris.s1pdgs.jobgenerator.exception.InternalErrorException;
import fr.viveris.s1pdgs.jobgenerator.exception.MaxNumberCachedJobsReachException;
import fr.viveris.s1pdgs.jobgenerator.exception.MetadataException;
import fr.viveris.s1pdgs.jobgenerator.model.GenerationStatusEnum;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.JobGenerationStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import fr.viveris.s1pdgs.jobgenerator.model.ProductMode;
import fr.viveris.s1pdgs.jobgenerator.model.converter.TaskTableToJobOrderConverter;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrder;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderInput;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderInputFile;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderOutput;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderSensingTime;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.JobOrderTimeInterval;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderDestination;
import fr.viveris.s1pdgs.jobgenerator.model.joborder.enums.JobOrderFileNameType;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadata;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadataQuery;
import fr.viveris.s1pdgs.jobgenerator.model.metadata.SearchMetadataResult;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTable;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableInput;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableInputAlternative;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTablePool;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.TaskTableTask;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableInputOrigin;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableMandatoryEnum;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.service.metadata.MetadataService;

/**
 * Class for processing product for a given task table
 * 
 * @author Cyrielle Gailliard
 *
 */
public abstract class AbstractJobsGenerator<T> implements Runnable {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(AbstractJobsGenerator.class);

	/**
	 * Use to generate an incremental id for locally upload session files
	 */
	private static final AtomicInteger INCREMENT_JOB = new AtomicInteger(0);

	/**
	 * Producer in KAFKA topic
	 */
	private final JobsProducer kafkaJobsSender;

	/**
	 * XML converter
	 */
	protected final XmlConverter xmlConverter;

	/**
	 * 
	 */
	protected final MetadataService metadataService;

	/**
	 * 
	 */
	protected final ProcessSettings l0ProcessSettings;

	protected final JobGeneratorSettings jobGeneratorSettings;

	/**
	 * Session waiting for beeing processing by a task table
	 */
	protected Map<String, Job<T>> cachedJobs;

	/**
	 * Task table
	 */
	protected String taskTableXmlName;
	protected TaskTable taskTable;
	protected List<List<String>> tasks;
	protected ProductMode mode;
	protected String prefixLogMonitor;
	protected String prefixLogMonitorRemove;

	/**
	 * Template of job order. Contains all information except ones specific to the
	 * session:
	 * <ul>
	 * <li>Inputs</li>
	 * <li>Configuration > Sensing time</li>
	 * <li>Partial outputs: the work directory shall be put in front of each
	 * filename</li>
	 * </ul>
	 */
	protected JobOrder jobOrderTemplate;

	/**
	 * List of queries for metadata
	 */
	protected final Map<Integer, SearchMetadataQuery> metadataSearchQueries;

	/**
	 * Constructor
	 * 
	 * @param xmlConverter
	 */
	public AbstractJobsGenerator(final XmlConverter xmlConverter, final MetadataService metadataService,
			final ProcessSettings l0ProcessSettings, final JobGeneratorSettings taskTablesSettings,
			final JobsProducer kafkaJobsSender) {
		this.xmlConverter = xmlConverter;
		this.metadataService = metadataService;
		this.l0ProcessSettings = l0ProcessSettings;
		this.jobGeneratorSettings = taskTablesSettings;
		this.cachedJobs = new ConcurrentHashMap<>(this.jobGeneratorSettings.getMaxnumberofjobs());
		this.metadataSearchQueries = new HashMap<>();
		this.kafkaJobsSender = kafkaJobsSender;
		this.tasks = new ArrayList<>();
		this.mode = ProductMode.BLANK;
	}

	// ----------------------------------------------------
	// INITIALIZATION
	// ----------------------------------------------------

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(ProductMode mode) {
		this.mode = mode;
	}

	/**
	 * Initialize the processor from the tasktable XML file
	 * 
	 * @param xmlFile
	 */
	public void initialize(File xmlFile) throws BuildTaskTableException {

		// Build task table
		this.taskTableXmlName = xmlFile.getName();
		this.buildTaskTable(xmlFile);
		this.prefixLogMonitor = "[MONITOR] [step 3] [taskTable " + this.taskTableXmlName + "]";
		this.prefixLogMonitorRemove = "[MONITOR] [step 4] [taskTable " + this.taskTableXmlName + "]";

		// Build jobOrder
		this.buildJobOrderTemplate();

		// Build list of metadata search query and linked to task table input
		// alternative
		this.buildMetadataSearchQuery();

		// Build the tasks
		this.buildTasks();

		// Retrieve list of inputs
		LOGGER.info(String.format("TaskTable %s initialized", taskTable.getProcessorName()));
	}

	/**
	 * Build the object TaskTable from XML file
	 * 
	 * @param xmlFile
	 * @throws BuildTaskTableException
	 */
	private void buildTaskTable(File xmlFile) throws BuildTaskTableException {
		// Retrieve task table
		try {
			this.taskTable = (TaskTable) xmlConverter.convertFromXMLToObject(xmlFile.getAbsolutePath());
			this.taskTable.setLevel(this.l0ProcessSettings.getLevel());
		} catch (IOException | JAXBException e) {
			throw new BuildTaskTableException(this.taskTableXmlName, e.getMessage(), e);
		}
	}

	private void buildJobOrderTemplate() {
		// Build from task table
		TaskTableToJobOrderConverter converter = new TaskTableToJobOrderConverter();
		this.jobOrderTemplate = converter.apply(this.taskTable);

		// Update values from configuration file
		this.jobOrderTemplate.getConf().getProcParams().forEach(item -> {
			if (this.l0ProcessSettings.getParams().containsKey(item.getName())) {
				item.setValue(this.l0ProcessSettings.getParams().get(item.getName()));
			}
		});
		this.jobOrderTemplate.getConf().setStdoutLogLevel(this.l0ProcessSettings.getLoglevelstdout());
		this.jobOrderTemplate.getConf().setStderrLogLevel(this.l0ProcessSettings.getLoglevelstderr());
		this.jobOrderTemplate.getConf().setProcessingStation(this.l0ProcessSettings.getProcessingstation());

		// Update outputs from configuration file
		this.jobOrderTemplate.getProcs().stream().filter(proc -> !proc.getOutputs().isEmpty())
				.flatMap(proc -> proc.getOutputs().stream())
				.filter(output -> output.getFileNameType() == JobOrderFileNameType.REGEXP).forEach(output -> {
					if (this.l0ProcessSettings.getOutputregexps().containsKey(output.getFileType())) {
						output.setFileName(this.l0ProcessSettings.getOutputregexps().get(output.getFileType()));
					} else {
						output.setFileName("^.*" + output.getFileType() + ".*$");
					}
				});

		// Update the output family according configuration file
		this.jobOrderTemplate.getProcs().stream().filter(proc -> !proc.getOutputs().isEmpty())
				.flatMap(proc -> proc.getOutputs().stream()).forEach(output -> {
					if (this.jobGeneratorSettings.getOutputfamilies().containsKey(output.getFileType())) {
						output.setFamily(this.jobGeneratorSettings.getOutputfamilies().get(output.getFileType()));
					} else {
						output.setFamily(ProductFamily.fromValue(this.jobGeneratorSettings.getDefaultfamily()));
					}
				});
	}

	private void buildMetadataSearchQuery() {
		final AtomicInteger counter = new AtomicInteger(0);
		this.taskTable.getPools().stream().filter(pool -> !CollectionUtils.isEmpty(pool.getTasks()))
				.flatMap(pool -> pool.getTasks().stream()).filter(task -> !CollectionUtils.isEmpty(task.getInputs()))
				.flatMap(task -> task.getInputs().stream())
				.filter(input -> !CollectionUtils.isEmpty(input.getAlternatives()))
				.flatMap(input -> input.getAlternatives().stream())
				.filter(alt -> alt.getOrigin() == TaskTableInputOrigin.DB)
				.collect(Collectors.groupingBy(TaskTableInputAlternative::getTaskTableInputAltKey)).forEach((k, v) -> {
					String fileType = k.getFileType();
					if (this.jobGeneratorSettings.getMapTypeMeta().containsKey(k.getFileType())) {
						fileType = this.jobGeneratorSettings.getMapTypeMeta().get(k.getFileType());
					}
					SearchMetadataQuery query = new SearchMetadataQuery(counter.incrementAndGet(), k.getRetrievalMode(),
							k.getDeltaTime0(), k.getDeltaTime1(), fileType);
					this.metadataSearchQueries.put(counter.get(), query);
					v.forEach(alt -> {
						alt.setIdSearchMetadataQuery(counter.get());
					});
				});
	}

	protected void buildTasks() {
		this.taskTable.getPools().forEach(pool -> {
			this.tasks.add(pool.getTasks().stream().map(TaskTableTask::getFileName).collect(Collectors.toList()));
		});
	}

	// ----------------------------------------------------
	// JOB CACHING
	// ----------------------------------------------------

	/**
	 * 
	 * @param session
	 * @throws MaxNumberCachedJobsReachException
	 */
	public void addJob(Job<T> job) throws MaxNumberCachedJobsReachException {
		if (!this.cachedJobs.containsKey(job.getProduct().getIdentifier())) {
			if (this.cachedJobs.size() >= this.jobGeneratorSettings.getMaxnumberofjobs()) {
				throw new MaxNumberCachedJobsReachException(this.taskTableXmlName, "Too much jobs in progress");
			}
			job.setTaskTableName(this.taskTableXmlName);
			job.setJobOrder(new JobOrder(this.jobOrderTemplate, this.l0ProcessSettings.getLevel()));
			this.metadataSearchQueries.forEach((k, v) -> {
				job.getMetadataQueries().put(k, new SearchMetadataResult(new SearchMetadataQuery(v)));
			});
			this.cachedJobs.put(job.getProduct().getIdentifier(), job);
		}
	}

	// ----------------------------------------------------
	// JOB GENERATION
	// ----------------------------------------------------

	@Override
	public void run() {

		// Process jobs
		List<String> keysJobs = new ArrayList<>(this.cachedJobs.keySet());
		if (!CollectionUtils.isEmpty(keysJobs)) {
			LOGGER.debug("{} Trying job generation for cached products", this.prefixLogMonitor);

			keysJobs.forEach(k -> {
				if (this.cachedJobs.containsKey(k)) {
					Job<T> v = this.cachedJobs.get(k);
					JobGenerationStatus status = v.getStatus();
					LOGGER.debug("{} [productName {}] [status {}] Trying job generation", this.prefixLogMonitor,
							v.getProduct().getIdentifier(), status);

					// Check if we can do a loop
					long currentTimestamp = System.currentTimeMillis();
					boolean todo = false;
					switch (status.getStatus()) {
					case NOT_READY:
						if (status.getLastModifiedTime() < currentTimestamp
								- jobGeneratorSettings.getWaitprimarycheck().getTempo()) {
							todo = true;
						}
						break;
					case PRIMARY_CHECK:
						if (status.getLastModifiedTime() < currentTimestamp
								- jobGeneratorSettings.getWaitmetadatainput().getTempo()) {
							todo = true;
						}
						break;
					default:
						todo = true;
						break;
					}

					if (todo) {
						LOGGER.info("{} [productName {}] [status {}] Trying job generation", this.prefixLogMonitor,
								v.getProduct().getIdentifier(), status);

						// Check primary input
						if (status.getStatus() == GenerationStatusEnum.NOT_READY) {
							try {
								LOGGER.info("{} [productName {}] 1 - Checking the pre-requirements",
										this.prefixLogMonitor, v.getProduct().getIdentifier());
								this.preSearch(v);
								status.updateStatus(GenerationStatusEnum.PRIMARY_CHECK);
							} catch (InputsMissingException e) {
								status.updateStatus(GenerationStatusEnum.NOT_READY);
								LOGGER.warn("{} [productName {}] 1 - Pre-requirements not checked: {}",
										this.prefixLogMonitor, v.getProduct().getIdentifier(), e.getLogMessage());
							}
						}

						// Search input
						if (status.getStatus() == GenerationStatusEnum.PRIMARY_CHECK) {
							try {
								LOGGER.info("{} [productName {}] 2 - Searching inputs", this.prefixLogMonitor,
										v.getProduct().getIdentifier());
								this.inputsSearch(v);
								status.updateStatus(GenerationStatusEnum.READY);
							} catch (InputsMissingException e) {
								status.updateStatus(GenerationStatusEnum.PRIMARY_CHECK);
								LOGGER.warn("{} [productName {}] 2 - Inputs not found: {}", this.prefixLogMonitor,
										v.getProduct().getIdentifier(), e.getLogMessage());
							}
						}

						// Prepare and send job if ready
						if (status.getStatus() == GenerationStatusEnum.READY) {
							try {
								LOGGER.info("{} [productName {}] 3 - Sending job", this.prefixLogMonitor,
										v.getProduct().getIdentifier());
								this.send(v);
								this.cachedJobs.remove(k);
							} catch (AbstractCodedException e) {
								status.updateStatus(GenerationStatusEnum.READY);
								LOGGER.warn("{} [productName {}] 3 - Job not send: {}", this.prefixLogMonitor,
										v.getProduct().getIdentifier(), e.getLogMessage());
							}
						}
					}
				}
			});

			// Remove invalid jobs
			LOGGER.info("{} 4 - Removing old products", this.prefixLogMonitor);
			this.removeNotReadyJobsForToolLong();

			LOGGER.info("{} End", this.prefixLogMonitor);
		}
	}

	protected abstract void preSearch(Job<T> job) throws InputsMissingException;

	protected void inputsSearch(Job<T> job) throws InputsMissingException {
		// First, we evaluate each input query with no found file
		LOGGER.info("{} [productName {}] 2a - Requesting metadata", this.prefixLogMonitor,
				job.getProduct().getIdentifier());
		job.getMetadataQueries().forEach((k, v) -> {
			if (v != null && v.getResult() == null) {
				try {
					SearchMetadata file = this.metadataService.search(v.getQuery(), job.getProduct().getStartTime(),
							job.getProduct().getStopTime(), job.getProduct().getSatelliteId(),
							job.getProduct().getInsConfId());
					if (file != null) {
						v.setResult(file);
					}
				} catch (MetadataException me) {
					LOGGER.warn(
							"{} [productName {}] [alternative {}] Exception occurred when searching alternative: {}",
							this.prefixLogMonitor, job.getProduct().getIdentifier(), v.getQuery().toLogMessage(),
							me.getMessage());
				}
			}
		});

		// Second, for each task check if input is mandatory and if a file exist
		LOGGER.info("{} [productName {}] 2b - Try building inputs", this.prefixLogMonitor,
				job.getProduct().getIdentifier());
		int counterProc = 0;
		Map<String, JobOrderInput> referenceInputs = new HashMap<>();
		for (TaskTablePool pool : this.taskTable.getPools()) {
			for (TaskTableTask task : pool.getTasks()) {
				Map<String, String> missingMetadata = new HashMap<>();
				List<JobOrderInput> futureInputs = new ArrayList<>();
				for (TaskTableInput input : task.getInputs()) {
					// If it is a reference
					if (StringUtils.isEmpty(input.getReference())) {

						if (ProductMode.isCompatibleWithTaskTableMode(this.mode, input.getMode())) {
							int currentOrder = 99;
							List<JobOrderInput> inputsToAdd = new ArrayList<>();
							for (TaskTableInputAlternative alt : input.getAlternatives()) {
								// We ignore input not DB
								if (alt.getOrigin() == TaskTableInputOrigin.DB) {
									if (job.getMetadataQueries().get(alt.getIdSearchMetadataQuery())
											.getResult() != null) {
										SearchMetadata file = job.getMetadataQueries()
												.get(alt.getIdSearchMetadataQuery()).getResult();

										JobOrderFileNameType type = JobOrderFileNameType.BLANK;
										switch (alt.getFileNameType()) {
										case PHYSICAL:
											type = JobOrderFileNameType.PHYSICAL;
											break;
										case DIRECTORY:
											type = JobOrderFileNameType.DIRECTORY;
											break;
										case REGEXP:
											type = JobOrderFileNameType.REGEXP;
											break;
										default:
											break;
										}

										// Retrieve family
										ProductFamily family = ProductFamily
												.fromValue(this.jobGeneratorSettings.getDefaultfamily());
										if (this.jobGeneratorSettings.getOutputfamilies()
												.containsKey(alt.getFileType())) {
											family = this.jobGeneratorSettings.getOutputfamilies()
													.get(alt.getFileType());
										}

										// Check order
										if (currentOrder == alt.getOrder()) {
											inputsToAdd.add(new JobOrderInput(alt.getFileType(), type,
													Arrays.asList(new JobOrderInputFile(file.getProductName(),
															file.getKeyObjectStorage())),
													Arrays.asList(new JobOrderTimeInterval(file.getValidityStart(),
															file.getValidityStop(), file.getProductName(),
															SearchMetadata.DATE_FORMATTER)),
													family));
										} else if (currentOrder > alt.getOrder()) {
											inputsToAdd = new ArrayList<>();
											inputsToAdd.add(new JobOrderInput(alt.getFileType(), type,
													Arrays.asList(new JobOrderInputFile(file.getProductName(),
															file.getKeyObjectStorage())),
													Arrays.asList(new JobOrderTimeInterval(file.getValidityStart(),
															file.getValidityStop(), file.getProductName(),
															SearchMetadata.DATE_FORMATTER)),
													family));
										}
										break;
									}
								} else {
									// TODO set this general
									SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmssSSSSSS");
									String startDate = format.format(job.getProduct().getStartTime());
									String stopDate = format.format(job.getProduct().getStopTime());
									inputsToAdd
											.add(new JobOrderInput(alt.getFileType(), JobOrderFileNameType.REGEXP,
													Arrays.asList(new JobOrderInputFile(alt.getFileType(), "")),
													Arrays.asList(new JobOrderTimeInterval(startDate, stopDate,
															alt.getFileType(),
															DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSSSSS"))),
													ProductFamily.BLANK));
								}
							}
							if (!inputsToAdd.isEmpty()) {
								// We take a random one
								int indexToTake = ThreadLocalRandom.current().nextInt(0, inputsToAdd.size());
								futureInputs.add(inputsToAdd.get(indexToTake));
								if (!StringUtils.isEmpty(input.getId())) {
									referenceInputs.put(input.getId(), inputsToAdd.get(indexToTake));
								}

							} else {
								if (input.getMandatory() == TaskTableMandatoryEnum.YES) {
									missingMetadata.put(input.toLogMessage(), "");
								}
							}
						}
					} else {
						// We shall add inputs of the reference
						if (referenceInputs.containsKey(input.getReference())) {
							futureInputs.add(new JobOrderInput(referenceInputs.get(input.getReference())));
						}
					}
				}
				counterProc++;
				if (missingMetadata.isEmpty()) {
					job.getJobOrder().getProcs().get(counterProc - 1).setInputs(futureInputs);
				} else {
					throw new InputsMissingException(missingMetadata);
				}
			}
		}
	}

	protected void send(Job<T> job) throws AbstractCodedException {

		// First step: update job if not already done
		if (StringUtils.isEmpty(job.getWorkDirectory())) {
			LOGGER.info("{} [productName {}] 3a - Building common job", this.prefixLogMonitor,
					job.getProduct().getIdentifier());
			int inc = INCREMENT_JOB.incrementAndGet();
			job.setWorkDirectory("/data/localWD/" + inc + "/");
			job.setWorkDirectoryInc(inc);

			// For each input and output of the job order, prefix by the working directory
			job.getJobOrder().getProcs().stream()
					.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getInputs()))
					.flatMap(proc -> proc.getInputs().stream()).forEach(input -> {
						input.getFilenames().forEach(filename -> {
							filename.setFilename(job.getWorkDirectory() + filename.getFilename());
						});
						input.getTimeIntervals().forEach(interval -> {
							interval.setFileName(job.getWorkDirectory() + interval.getFileName());
						});
					});
			job.getJobOrder().getProcs().stream()
					.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getOutputs()))
					.flatMap(proc -> proc.getOutputs().stream()).forEach(output -> {
						output.setFileName(job.getWorkDirectory() + output.getFileName());
					});

			// Apply implementation build job
			SimpleDateFormat dateFormat = new SimpleDateFormat(JobOrderSensingTime.DATE_FORMAT);
			job.getJobOrder().getConf()
					.setSensingTime(new JobOrderSensingTime(dateFormat.format(job.getProduct().getStartTime()),
							dateFormat.format(job.getProduct().getStopTime())));

			// Custom Job order according implementation
			this.customJobOrder(job);
		} else {
			LOGGER.info("{} [productName {}] 3a - Common job already built", this.prefixLogMonitor,
					job.getProduct().getIdentifier());
		}

		// Second, build the DTO
		String jobOrder = "/data/localWD/" + job.getWorkDirectoryInc() + "/JobOrder." + job.getWorkDirectoryInc()
				+ ".xml";
		final JobDto r = new JobDto(job.getProduct().getIdentifier(), job.getWorkDirectory(), jobOrder);

		try {

			// Add jobOrder inputs to the DTO
			List<JobOrderInput> distinctInputJobOrder = job.getJobOrder().getProcs().stream()
					.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getInputs()))
					.flatMap(proc -> proc.getInputs().stream()).distinct().collect(Collectors.toList());
			r.addInputs(distinctInputJobOrder.stream().map(input -> new JobInputDto(input.getFamily().name(),
					input.getFilenames().get(0).getFilename(), input.getFilenames().get(0).getKeyObjectStorage()))
					.collect(Collectors.toList()));

			// Add the jobOrder itself in inputs
			r.addInput(new JobInputDto(ProductFamily.JOB_ORDER.name(), jobOrder,
					xmlConverter.convertFromObjectToXMLString(job.getJobOrder())));

			// Add joborder output to the DTO
			List<JobOrderOutput> distinctOutputJobOrder = job.getJobOrder().getProcs().stream()
					.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getOutputs()))
					.flatMap(proc -> proc.getOutputs().stream())
					.filter(output -> output.getFileNameType() == JobOrderFileNameType.REGEXP
							&& output.getDestination() == JobOrderDestination.DB)
					.distinct().collect(Collectors.toList());
			r.addOutputs(distinctOutputJobOrder.stream()
					.map(output -> new JobOutputDto(output.getFamily().name(), output.getFileName()))
					.collect(Collectors.toList()));
			List<JobOrderOutput> distinctOutputJobOrderNotRegexp = job.getJobOrder().getProcs().stream()
					.filter(proc -> proc != null && !CollectionUtils.isEmpty(proc.getOutputs()))
					.flatMap(proc -> proc.getOutputs().stream())
					.filter(output -> output.getFileNameType() == JobOrderFileNameType.DIRECTORY
							&& output.getDestination() == JobOrderDestination.DB)
					.distinct().collect(Collectors.toList());
			r.addOutputs(
					distinctOutputJobOrderNotRegexp.stream()
							.map(output -> new JobOutputDto(output.getFamily().name(),
									output.getFileName() + "^.*" + output.getFileType() + ".*$"))
							.collect(Collectors.toList()));

			// Add the tasks
			this.tasks.forEach(pool -> {
				JobPoolDto poolDto = new JobPoolDto();
				pool.forEach(task -> {
					poolDto.addTask(new JobTaskDto(task));
				});
				r.addPool(poolDto);
			});

			// Apply implementation build job
			LOGGER.info("{} [productName {}] 3b - Building custom job", this.prefixLogMonitor,
					job.getProduct().getIdentifier());
			this.customJobDto(job, r);

		} catch (IOException | JAXBException e) {
			throw new InternalErrorException("Cannot send the job", e);
		}

		// Thrid, send the job
		LOGGER.info("{} [productName {}] 3c - Publishing job", this.prefixLogMonitor, job.getProduct().getIdentifier());
		this.kafkaJobsSender.send(r);
	}

	protected abstract void customJobOrder(Job<T> job);

	protected abstract void customJobDto(Job<T> job, JobDto dto);

	protected void removeNotReadyJobsForToolLong() {
		cachedJobs.entrySet().removeIf(entry -> {
			JobGenerationStatus status = entry.getValue().getStatus();
			if (status.getStatus() == GenerationStatusEnum.NOT_READY
					&& status.getNbRetries() >= this.jobGeneratorSettings.getWaitprimarycheck().getRetries()) {
				LOGGER.error("{} [step 4] [productName {}] [resuming {}] [code {}] [msg {}] [retries {}/{}]",
						this.prefixLogMonitorRemove, entry.getValue().getProduct().getIdentifier(),
						entry.getValue().getResumeDetails(), ErrorCode.MAX_AGE_CACHED_JOB_REACH.getCode(),
						"Waiting for primary check since too long", status.getNbRetries(),
						this.jobGeneratorSettings.getWaitprimarycheck().getRetries());
				return true;
			}
			if (status.getStatus() == GenerationStatusEnum.PRIMARY_CHECK
					&& status.getNbRetries() >= this.jobGeneratorSettings.getWaitmetadatainput().getRetries()) {
				LOGGER.error("{} [step 4] [productName {}] [resuming {}] [code {}] [msg {}] [retries {}/{}]",
						this.prefixLogMonitorRemove, entry.getValue().getProduct().getIdentifier(),
						entry.getValue().getResumeDetails(), ErrorCode.MAX_AGE_CACHED_JOB_REACH.getCode(),
						"Waiting for input check since too long", status.getNbRetries(),
						this.jobGeneratorSettings.getWaitmetadatainput().getRetries());
				return true;
			}
			return false;
		});
	}
}
