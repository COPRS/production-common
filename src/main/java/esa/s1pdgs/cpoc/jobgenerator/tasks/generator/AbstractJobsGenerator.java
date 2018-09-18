package esa.s1pdgs.cpoc.jobgenerator.tasks.generator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.JobGeneration;
import esa.s1pdgs.cpoc.jobgenerator.model.ProductMode;
import esa.s1pdgs.cpoc.jobgenerator.model.converter.TaskTableToJobOrderConverter;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrder;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderSensingTime;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.enums.JobOrderDestination;
import esa.s1pdgs.cpoc.jobgenerator.model.joborder.enums.JobOrderFileNameType;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataQuery;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataResult;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableInputAlternative;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableInputOrigin;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableMandatoryEnum;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.service.metadata.MetadataService;
import esa.s1pdgs.cpoc.jobgenerator.service.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobPoolDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobTaskDto;

/**
 * Class for processing product for a given task table
 * 
 * @author Cyrielle Gailliard
 */
public abstract class AbstractJobsGenerator<T> implements Runnable {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(AbstractJobsGenerator.class);

    /**
     * Use to generate an incremental id for locally upload session files
     */
    private static final AtomicInteger INCREMENT_JOB = new AtomicInteger(0);

    /**
     * Producer in topic
     */
    private final OutputProducerFactory outputFactory;

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
     * Applicative data service
     */
    private final AbstractAppCatalogJobService<T> appDataService;

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
     * Template of job order. Contains all information except ones specific to
     * the session:
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
    public AbstractJobsGenerator(final XmlConverter xmlConverter,
            final MetadataService metadataService,
            final ProcessSettings l0ProcessSettings,
            final JobGeneratorSettings taskTablesSettings,
            final OutputProducerFactory outputFactory,
            final AbstractAppCatalogJobService<T> appDataService) {
        this.xmlConverter = xmlConverter;
        this.metadataService = metadataService;
        this.l0ProcessSettings = l0ProcessSettings;
        this.jobGeneratorSettings = taskTablesSettings;
        this.metadataSearchQueries = new HashMap<>();
        this.outputFactory = outputFactory;
        this.tasks = new ArrayList<>();
        this.mode = ProductMode.BLANK;
        this.appDataService = appDataService;
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
    public void initialize(File xmlFile) throws JobGenBuildTaskTableException {

        // Build task table
        this.taskTableXmlName = xmlFile.getName();
        this.buildTaskTable(xmlFile);
        this.prefixLogMonitor =
                "[MONITOR] [step 3] [taskTable " + this.taskTableXmlName + "]";
        this.prefixLogMonitorRemove =
                "[MONITOR] [step 4] [taskTable " + this.taskTableXmlName + "]";

        // Build jobOrder
        this.buildJobOrderTemplate();

        // Build list of metadata search query and linked to task table input
        // alternative
        this.buildMetadataSearchQuery();

        // Build the tasks
        this.buildTasks();

        // Retrieve list of inputs
        LOGGER.info(String.format("TaskTable %s initialized",
                taskTable.getProcessorName()));
    }

    /**
     * Build the object TaskTable from XML file
     * 
     * @param xmlFile
     * @throws BuildTaskTableException
     */
    private void buildTaskTable(File xmlFile)
            throws JobGenBuildTaskTableException {
        // Retrieve task table
        try {
            this.taskTable = (TaskTable) xmlConverter
                    .convertFromXMLToObject(xmlFile.getAbsolutePath());
            this.taskTable.setLevel(this.l0ProcessSettings.getLevel());
        } catch (IOException | JAXBException e) {
            throw new JobGenBuildTaskTableException(this.taskTableXmlName,
                    e.getMessage(), e);
        }
    }

    private void buildJobOrderTemplate() {
        // Build from task table
        TaskTableToJobOrderConverter converter =
                new TaskTableToJobOrderConverter();
        this.jobOrderTemplate = converter.apply(this.taskTable);

        // Update values from configuration file
        this.jobOrderTemplate.getConf().getProcParams().forEach(item -> {
            if (this.l0ProcessSettings.getParams()
                    .containsKey(item.getName())) {
                item.setValue(
                        this.l0ProcessSettings.getParams().get(item.getName()));
            }
        });
        this.jobOrderTemplate.getConf()
                .setStdoutLogLevel(this.l0ProcessSettings.getLoglevelstdout());
        this.jobOrderTemplate.getConf()
                .setStderrLogLevel(this.l0ProcessSettings.getLoglevelstderr());
        this.jobOrderTemplate.getConf().setProcessingStation(
                this.l0ProcessSettings.getProcessingstation());

        // Update outputs from configuration file
        this.jobOrderTemplate.getProcs().stream()
                .filter(proc -> !proc.getOutputs().isEmpty())
                .flatMap(proc -> proc.getOutputs().stream())
                .filter(output -> output
                        .getFileNameType() == JobOrderFileNameType.REGEXP)
                .forEach(output -> {
                    if (this.l0ProcessSettings.getOutputregexps()
                            .containsKey(output.getFileType())) {
                        output.setFileName(this.l0ProcessSettings
                                .getOutputregexps().get(output.getFileType()));
                    } else {
                        output.setFileName(
                                "^.*" + output.getFileType() + ".*$");
                    }
                });

        // Update the output family according configuration file
        this.jobOrderTemplate.getProcs().stream()
                .filter(proc -> !proc.getOutputs().isEmpty())
                .flatMap(proc -> proc.getOutputs().stream()).forEach(output -> {
                    if (this.jobGeneratorSettings.getOutputfamilies()
                            .containsKey(output.getFileType())) {
                        output.setFamily(this.jobGeneratorSettings
                                .getOutputfamilies().get(output.getFileType()));
                    } else {
                        output.setFamily(ProductFamily.fromValue(
                                this.jobGeneratorSettings.getDefaultfamily()));
                    }
                });
    }

    private void buildMetadataSearchQuery() {
        final AtomicInteger counter = new AtomicInteger(0);
        this.taskTable.getPools().stream()
                .filter(pool -> !CollectionUtils.isEmpty(pool.getTasks()))
                .flatMap(pool -> pool.getTasks().stream())
                .filter(task -> !CollectionUtils.isEmpty(task.getInputs()))
                .flatMap(task -> task.getInputs().stream())
                .filter(input -> !CollectionUtils
                        .isEmpty(input.getAlternatives()))
                .flatMap(input -> input.getAlternatives().stream())
                .filter(alt -> alt.getOrigin() == TaskTableInputOrigin.DB)
                .collect(Collectors.groupingBy(
                        TaskTableInputAlternative::getTaskTableInputAltKey))
                .forEach((k, v) -> {
                    String fileType = k.getFileType();
                    if (this.jobGeneratorSettings.getMapTypeMeta()
                            .containsKey(k.getFileType())) {
                        fileType = this.jobGeneratorSettings.getMapTypeMeta()
                                .get(k.getFileType());
                    }
                    SearchMetadataQuery query = new SearchMetadataQuery(
                            counter.incrementAndGet(), k.getRetrievalMode(),
                            k.getDeltaTime0(), k.getDeltaTime1(), fileType);
                    this.metadataSearchQueries.put(counter.get(), query);
                    v.forEach(alt -> {
                        alt.setIdSearchMetadataQuery(counter.get());
                    });
                });
    }

    protected void buildTasks() {
        this.taskTable.getPools().forEach(pool -> {
            this.tasks.add(
                    pool.getTasks().stream().map(TaskTableTask::getFileName)
                            .collect(Collectors.toList()));
        });
    }

    // ----------------------------------------------------
    // JOB GENERATION
    // ----------------------------------------------------

    @Override
    public void run() {
        JobGeneration<T> job = null;

        // Get a job to generate
        try {
            List<AppDataJobDto<T>> jobs = appDataService
                    .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                            l0ProcessSettings.getHostname(), taskTableXmlName);
            // Determine job to process
            if (CollectionUtils.isEmpty(jobs)) {
                job = null;
            } else {
                for (AppDataJobDto<T> appDataJob : jobs) {
                    // Check if we can do a loop
                    long currentTimestamp = System.currentTimeMillis();
                    boolean todo = false;
                    job = new JobGeneration<>(appDataJob, taskTableXmlName);
                    switch (job.getGeneration().getState()) {
                        case INITIAL:
                            if (job.getGeneration().getLastUpdateDate() == null
                                    || job.getGeneration().getLastUpdateDate()
                                            .getTime() < currentTimestamp
                                                    - jobGeneratorSettings
                                                            .getWaitprimarycheck()
                                                            .getTempo()) {
                                todo = true;
                            }
                            break;
                        case PRIMARY_CHECK:
                            if (job.getGeneration().getLastUpdateDate()
                                    .getTime() < currentTimestamp
                                            - jobGeneratorSettings
                                                    .getWaitmetadatainput()
                                                    .getTempo()) {
                                todo = true;
                            }
                            break;
                        default:
                            todo = true;
                            break;
                    }
                    if (todo) {
                        job.setJobOrder(new JobOrder(this.jobOrderTemplate,
                                this.l0ProcessSettings.getLevel()));
                        for (Integer key : metadataSearchQueries.keySet()) {
                            SearchMetadataQuery query =
                                    metadataSearchQueries.get(key);
                            job.getMetadataQueries().put(key,
                                    new SearchMetadataResult(
                                            new SearchMetadataQuery(query)));
                        }
                        break;
                    } else {
                        job = null;
                    }
                }
            }
        } catch (AbstractCodedException ace) {
            LOGGER.error("{} CAnnot retrieve the current jobs: {}",
                    this.prefixLogMonitor, ace.getLogMessage());
        }

        if (job != null) {
            String productName =
                    job.getAppDataJob().getProduct().getProductName();

            try {
                LOGGER.debug(
                        "{} [productName {}] [status {}] Trying job generation",
                        this.prefixLogMonitor, productName,
                        job.getGeneration().getState());

                // Check primary input
                if (job.getGeneration()
                        .getState() == AppDataJobGenerationDtoState.INITIAL) {
                    try {
                        if (job.getGeneration().getNbErrors() == 0) {
                            LOGGER.info(
                                    "{} [REPORT] [s1pdgsTask {}JobGeneration] [subTask Generation] [START] [productName {}] [inputs {}] Trying job generation",
                                    this.prefixLogMonitor,
                                    this.taskTable.getLevel(), productName,
                                    job.getGeneration().getTaskTable());
                        }
                        LOGGER.info(
                                "{} [productName {}] 1 - Checking the pre-requirements",
                                this.prefixLogMonitor, productName);
                        this.preSearch(job);
                        AppDataJobDto<T> modifiedJob = appDataService.patchJob(
                                job.getAppDataJob().getIdentifier(),
                                job.getAppDataJob(), false, true, false);
                        job.setAppDataJob(modifiedJob);
                        updateState(job,
                                AppDataJobGenerationDtoState.PRIMARY_CHECK);
                    } catch (AbstractCodedException e) {
                        updateState(job, AppDataJobGenerationDtoState.INITIAL);
                        LOGGER.error(
                                "{} [productName {}] 1 - Pre-requirements not checked: {}",
                                this.prefixLogMonitor, productName,
                                e.getLogMessage());
                    }
                }

                // Search input
                if (job.getGeneration()
                        .getState() == AppDataJobGenerationDtoState.PRIMARY_CHECK) {
                    try {
                        LOGGER.info("{} [productName {}] 2 - Searching inputs",
                                this.prefixLogMonitor, job.getAppDataJob()
                                        .getProduct().getProductName());
                        this.inputsSearch(job);
                        updateState(job, AppDataJobGenerationDtoState.READY);
                    } catch (AbstractCodedException e) {
                        updateState(job,
                                AppDataJobGenerationDtoState.PRIMARY_CHECK);
                        LOGGER.error(
                                "{} [productName {}] 2 - Inputs not found: {}",
                                this.prefixLogMonitor, productName,
                                e.getLogMessage());
                    }
                }

                // Prepare and send job if ready
                if (job.getGeneration()
                        .getState() == AppDataJobGenerationDtoState.READY) {
                    try {
                        LOGGER.info("{} [productName {}] 2 - Searching inputs",
                                this.prefixLogMonitor, job.getAppDataJob()
                                        .getProduct().getProductName());
                        this.inputsSearch(job);
                        LOGGER.info("{} [productName {}] 3 - Sending job",
                                this.prefixLogMonitor, job.getAppDataJob()
                                        .getProduct().getProductName());
                        this.send(job);
                        updateState(job, AppDataJobGenerationDtoState.SENT);
                    } catch (AbstractCodedException e) {
                        updateState(job, AppDataJobGenerationDtoState.READY);
                        LOGGER.error("{} [productName {}] 3 - Job not send: {}",
                                this.prefixLogMonitor, productName,
                                e.getLogMessage());
                    }
                }
            } catch (AbstractCodedException ace) {
                LOGGER.error(
                        "{} [productName {}] [code ] Cannot generate job: {}",
                        this.prefixLogMonitor, productName,
                        ace.getCode().getCode(), ace.getLogMessage());
            }

            LOGGER.info("{} End", this.prefixLogMonitor);
        }
    }

    protected void updateState(JobGeneration<T> job,
            AppDataJobGenerationDtoState newState)
            throws AbstractCodedException {
        AppDataJobDto<T> modifiedJob = appDataService.patchTaskTableOfJob(
                job.getAppDataJob().getIdentifier(),
                job.getGeneration().getTaskTable(), newState);
        job.updateAppDataJob(modifiedJob, taskTableXmlName);

        // Log functional logs
        if (job.getGeneration()
                .getState() == AppDataJobGenerationDtoState.SENT) {
            if (newState == AppDataJobGenerationDtoState.SENT) {
                // TODO addoutputs
                LOGGER.info(
                        "{} [REPORT] [s1pdgsTask {}JobGeneration] [subTask Generation] [STOP OK] [productName {}] Job generation successfully finished",
                        this.prefixLogMonitor, this.taskTable.getLevel(),
                        job.getAppDataJob().getProduct().getProductName());
            } else {
                LOGGER.error(
                        "{} [REPORT] [s1pdgsTask {}JobGeneration] [subTask Generation] [STOP KO] [productName {}] Job generation finished but job not sent",
                        this.prefixLogMonitor, this.taskTable.getLevel(),
                        job.getAppDataJob().getProduct().getProductName());
            }
        }
        if (job.getAppDataJob().getState() == AppDataJobDtoState.TERMINATED) {
            List<String> taskTables = new ArrayList<>();
            job.getAppDataJob().getGenerations().stream().forEach(gen -> {
                taskTables.add(gen.getTaskTable());
            });
            LOGGER.info(
                    "{} [REPORT] [s1pdgsTask {}JobGeneration] [STOP OK] [productName {}] [outputs {}] Job finished",
                    this.prefixLogMonitor, this.taskTable.getLevel(),
                    job.getAppDataJob().getProduct().getProductName(),
                    taskTables);
        }
    }

    protected abstract void preSearch(JobGeneration<T> job)
            throws JobGenInputsMissingException;

    protected void inputsSearch(JobGeneration<T> job)
            throws JobGenInputsMissingException {
        // First, we evaluate each input query with no found file
        LOGGER.info("{} [productName {}] 2a - Requesting metadata",
                this.prefixLogMonitor,
                job.getAppDataJob().getProduct().getProductName());
        job.getMetadataQueries().forEach((k, v) -> {
            if (v != null && v.getResult() == null) {
                try {
                    SearchMetadata file = this.metadataService.search(
                            v.getQuery(),
                            job.getAppDataJob().getProduct().getStartTime(),
                            job.getAppDataJob().getProduct().getStopTime(),
                            job.getAppDataJob().getProduct().getSatelliteId(),
                            job.getAppDataJob().getProduct().getInsConfId());
                    if (file != null) {
                        v.setResult(file);
                    }
                } catch (JobGenMetadataException me) {
                    LOGGER.warn(
                            "{} [productName {}] [alternative {}] Exception occurred when searching alternative: {}",
                            this.prefixLogMonitor,
                            job.getAppDataJob().getProduct().getProductName(),
                            v.getQuery().toLogMessage(), me.getMessage());
                }
            }
        });

        // Second, for each task check if input is mandatory and if a file exist
        LOGGER.info("{} [productName {}] 2b - Try building inputs",
                this.prefixLogMonitor,
                job.getAppDataJob().getProduct().getProductName());
        int counterProc = 0;
        Map<String, JobOrderInput> referenceInputs = new HashMap<>();
        for (TaskTablePool pool : this.taskTable.getPools()) {
            for (TaskTableTask task : pool.getTasks()) {
                Map<String, String> missingMetadata = new HashMap<>();
                List<JobOrderInput> futureInputs = new ArrayList<>();
                for (TaskTableInput input : task.getInputs()) {
                    // If it is a reference
                    if (StringUtils.isEmpty(input.getReference())) {

                        if (ProductMode.isCompatibleWithTaskTableMode(this.mode,
                                input.getMode())) {
                            int currentOrder = 99;
                            List<JobOrderInput> inputsToAdd = new ArrayList<>();
                            for (TaskTableInputAlternative alt : input
                                    .getAlternatives()) {
                                // We ignore input not DB
                                if (alt.getOrigin() == TaskTableInputOrigin.DB) {
                                    if (job.getMetadataQueries()
                                            .get(alt.getIdSearchMetadataQuery())
                                            .getResult() != null) {
                                        SearchMetadata file = job
                                                .getMetadataQueries()
                                                .get(alt.getIdSearchMetadataQuery())
                                                .getResult();

                                        JobOrderFileNameType type =
                                                JobOrderFileNameType.BLANK;
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
                                        ProductFamily family =
                                                ProductFamily.fromValue(
                                                        this.jobGeneratorSettings
                                                                .getDefaultfamily());
                                        if (this.jobGeneratorSettings
                                                .getOutputfamilies()
                                                .containsKey(
                                                        alt.getFileType())) {
                                            family = this.jobGeneratorSettings
                                                    .getOutputfamilies()
                                                    .get(alt.getFileType());
                                        }

                                        // Check order
                                        if (currentOrder == alt.getOrder()) {
                                            inputsToAdd.add(new JobOrderInput(
                                                    alt.getFileType(), type,
                                                    Arrays.asList(
                                                            new JobOrderInputFile(
                                                                    file.getProductName(),
                                                                    file.getKeyObjectStorage())),
                                                    Arrays.asList(
                                                            new JobOrderTimeInterval(
                                                                    file.getValidityStart(),
                                                                    file.getValidityStop(),
                                                                    file.getProductName(),
                                                                    SearchMetadata.DATE_FORMATTER)),
                                                    family));
                                        } else if (currentOrder > alt
                                                .getOrder()) {
                                            inputsToAdd = new ArrayList<>();
                                            inputsToAdd.add(new JobOrderInput(
                                                    alt.getFileType(), type,
                                                    Arrays.asList(
                                                            new JobOrderInputFile(
                                                                    file.getProductName(),
                                                                    file.getKeyObjectStorage())),
                                                    Arrays.asList(
                                                            new JobOrderTimeInterval(
                                                                    file.getValidityStart(),
                                                                    file.getValidityStop(),
                                                                    file.getProductName(),
                                                                    SearchMetadata.DATE_FORMATTER)),
                                                    family));
                                        }
                                        break;
                                    }
                                } else {
                                    // TODO set this general
                                    SimpleDateFormat format =
                                            new SimpleDateFormat(
                                                    "yyyyMMdd_HHmmssSSSSSS");
                                    String startDate = format.format(
                                            job.getAppDataJob().getProduct()
                                                    .getStartTime());
                                    String stopDate = format.format(
                                            job.getAppDataJob().getProduct()
                                                    .getStopTime());
                                    inputsToAdd.add(new JobOrderInput(
                                            alt.getFileType(),
                                            JobOrderFileNameType.REGEXP,
                                            Arrays.asList(new JobOrderInputFile(
                                                    alt.getFileType(), "")),
                                            Arrays.asList(
                                                    new JobOrderTimeInterval(
                                                            startDate, stopDate,
                                                            alt.getFileType(),
                                                            DateTimeFormatter
                                                                    .ofPattern(
                                                                            "yyyyMMdd_HHmmssSSSSSS"))),
                                            ProductFamily.BLANK));
                                }
                            }
                            if (!inputsToAdd.isEmpty()) {
                                // We take a random one
                                int indexToTake = ThreadLocalRandom.current()
                                        .nextInt(0, inputsToAdd.size());
                                futureInputs.add(inputsToAdd.get(indexToTake));
                                if (!StringUtils.isEmpty(input.getId())) {
                                    referenceInputs.put(input.getId(),
                                            inputsToAdd.get(indexToTake));
                                }

                            } else {
                                if (input
                                        .getMandatory() == TaskTableMandatoryEnum.YES) {
                                    missingMetadata.put(input.toLogMessage(),
                                            "");
                                }
                            }
                        }
                    } else {
                        // We shall add inputs of the reference
                        if (referenceInputs.containsKey(input.getReference())) {
                            futureInputs.add(new JobOrderInput(
                                    referenceInputs.get(input.getReference())));
                        }
                    }
                }
                counterProc++;
                if (missingMetadata.isEmpty()) {
                    job.getJobOrder().getProcs().get(counterProc - 1)
                            .setInputs(futureInputs);
                } else {
                    throw new JobGenInputsMissingException(missingMetadata);
                }
            }
        }
    }

    protected void send(JobGeneration<T> job) throws AbstractCodedException {
        LOGGER.info("{} [productName {}] 3a - Building common job",
                this.prefixLogMonitor,
                job.getAppDataJob().getProduct().getProductName());
        int inc = INCREMENT_JOB.incrementAndGet();
        String workingDir = "/data/localWD/" + inc + "/";

        // For each input and output of the job order, prefix by the working
        // directory
        job.getJobOrder().getProcs().stream()
                .filter(proc -> proc != null
                        && !CollectionUtils.isEmpty(proc.getInputs()))
                .flatMap(proc -> proc.getInputs().stream()).forEach(input -> {
                    input.getFilenames().forEach(filename -> {
                        filename.setFilename(
                                workingDir + filename.getFilename());
                    });
                    input.getTimeIntervals().forEach(interval -> {
                        interval.setFileName(
                                workingDir + interval.getFileName());
                    });
                });
        job.getJobOrder().getProcs().stream()
                .filter(proc -> proc != null
                        && !CollectionUtils.isEmpty(proc.getOutputs()))
                .flatMap(proc -> proc.getOutputs().stream()).forEach(output -> {
                    output.setFileName(workingDir + output.getFileName());
                });

        // Apply implementation build job
        SimpleDateFormat dateFormat =
                new SimpleDateFormat(JobOrderSensingTime.DATE_FORMAT);
        job.getJobOrder().getConf().setSensingTime(new JobOrderSensingTime(
                dateFormat.format(
                        job.getAppDataJob().getProduct().getStartTime()),
                dateFormat.format(
                        job.getAppDataJob().getProduct().getStopTime())));

        // Custom Job order according implementation
        this.customJobOrder(job);

        // Second, build the DTO
        String jobOrder = "/data/localWD/" + inc + "/JobOrder." + inc + ".xml";
        ProductFamily family = ProductFamily.L0_JOB;
        if (l0ProcessSettings.getLevel() == ApplicationLevel.L1) {
            family = ProductFamily.L1_JOB;
        }
        final LevelJobDto r = new LevelJobDto(family,
                job.getAppDataJob().getProduct().getProductName(), workingDir,
                jobOrder);

        try {

            // Add jobOrder inputs to the DTO
            List<JobOrderInput> distinctInputJobOrder = job.getJobOrder()
                    .getProcs().stream()
                    .filter(proc -> proc != null
                            && !CollectionUtils.isEmpty(proc.getInputs()))
                    .flatMap(proc -> proc.getInputs().stream()).distinct()
                    .collect(Collectors.toList());
            r.addInputs(distinctInputJobOrder.stream()
                    .map(input -> new LevelJobInputDto(input.getFamily().name(),
                            input.getFilenames().get(0).getFilename(),
                            input.getFilenames().get(0).getKeyObjectStorage()))
                    .collect(Collectors.toList()));

            // Add the jobOrder itself in inputs
            r.addInput(new LevelJobInputDto(ProductFamily.JOB_ORDER.name(),
                    jobOrder, xmlConverter
                            .convertFromObjectToXMLString(job.getJobOrder())));

            // Add joborder output to the DTO
            List<JobOrderOutput> distinctOutputJobOrder = job.getJobOrder()
                    .getProcs().stream()
                    .filter(proc -> proc != null
                            && !CollectionUtils.isEmpty(proc.getOutputs()))
                    .flatMap(proc -> proc.getOutputs().stream())
                    .filter(output -> output
                            .getFileNameType() == JobOrderFileNameType.REGEXP
                            && output
                                    .getDestination() == JobOrderDestination.DB)
                    .distinct().collect(Collectors.toList());
            r.addOutputs(distinctOutputJobOrder.stream()
                    .map(output -> new LevelJobOutputDto(
                            output.getFamily().name(), output.getFileName()))
                    .collect(Collectors.toList()));
            List<JobOrderOutput> distinctOutputJobOrderNotRegexp = job
                    .getJobOrder().getProcs().stream()
                    .filter(proc -> proc != null
                            && !CollectionUtils.isEmpty(proc.getOutputs()))
                    .flatMap(proc -> proc.getOutputs().stream())
                    .filter(output -> output
                            .getFileNameType() == JobOrderFileNameType.DIRECTORY
                            && output
                                    .getDestination() == JobOrderDestination.DB)
                    .distinct().collect(Collectors.toList());
            r.addOutputs(distinctOutputJobOrderNotRegexp
                    .stream().map(
                            output -> new LevelJobOutputDto(
                                    output.getFamily().name(),
                                    output.getFileName() + "^.*"
                                            + output.getFileType() + ".*$"))
                    .collect(Collectors.toList()));

            // Add the tasks
            this.tasks.forEach(pool -> {
                LevelJobPoolDto poolDto = new LevelJobPoolDto();
                pool.forEach(task -> {
                    poolDto.addTask(new LevelJobTaskDto(task));
                });
                r.addPool(poolDto);
            });

            // Apply implementation build job
            LOGGER.info("{} [productName {}] 3b - Building custom job",
                    this.prefixLogMonitor,
                    job.getAppDataJob().getProduct().getProductName());
            this.customJobDto(job, r);

        } catch (IOException | JAXBException e) {
            throw new InternalErrorException("Cannot send the job", e);
        }

        // Thrid, send the job
        LOGGER.info("{} [productName {}] 3c - Publishing job",
                this.prefixLogMonitor,
                job.getAppDataJob().getProduct().getProductName());

        this.outputFactory.sendJob(job.getAppDataJob().getMessages().get(0), r);
    }

    protected abstract void customJobOrder(JobGeneration<T> job);

    protected abstract void customJobDto(JobGeneration<T> job, LevelJobDto dto);
}
