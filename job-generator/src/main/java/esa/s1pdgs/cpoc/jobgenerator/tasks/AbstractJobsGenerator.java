package esa.s1pdgs.cpoc.jobgenerator.tasks;

import java.io.File;
import java.io.IOException;
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

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenBuildTaskTableException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenInputsMissingException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMetadataException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
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
import esa.s1pdgs.cpoc.metadata.model.AbstractMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
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
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

/**
 * Class for processing product for a given task table
 * 
 * @author Cyrielle Gailliard
 */
public abstract class AbstractJobsGenerator<T> implements Runnable {

    /**
     * Logger
     */
    protected static final Logger LOGGER =
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
    private final AppCatalogJobClient appDataService;

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
            final AppCatalogJobClient appDataService) {
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
                    ProductFamily family = ProductFamily.BLANK;
                    if (this.jobGeneratorSettings.getInputfamilies()
                            .containsKey(fileType)) {
                        family = this.jobGeneratorSettings.getInputfamilies()
                                .get(fileType);
                    }
                    SearchMetadataQuery query =
                            new SearchMetadataQuery(counter.incrementAndGet(),
                                    k.getRetrievalMode(), k.getDeltaTime0(),
                                    k.getDeltaTime1(), fileType, family);
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
        JobGeneration job = null;
        // Get a job to generate
        final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "JobGenerator");
        final Reporting reporting = reportingFactory.newReporting(0);
        
        try {
        	
            List<AppDataJobDto> jobs = appDataService
                    .findNByPodAndGenerationTaskTableWithNotSentGeneration(
                            l0ProcessSettings.getHostname(), taskTableXmlName);
            // Determine job to process
            if (CollectionUtils.isEmpty(jobs)) {
                job = null;
            } else {
                for (AppDataJobDto appDataJob : jobs) {
                    // Check if we can do a loop
                    long currentTimestamp = System.currentTimeMillis();
                    boolean todo = false;
                    job = new JobGeneration(appDataJob, taskTableXmlName);
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
                if (job.getGeneration().getState() == AppDataJobGenerationDtoState.INITIAL) {
                 	final Reporting reportInit = reportingFactory
                			.product(null, productName)                        			
                			.newReporting(1);
                 	
                    try {                    	
                        if (job.getGeneration().getNbErrors() == 0) { 
                            reporting.reportStart("Start job generation");
                        	reportInit.reportStart("Start init job generation");
                        }                        
                        LOGGER.info(
                                "{} [productName {}] 1 - Checking the pre-requirements",
                                this.prefixLogMonitor, productName);
                        this.preSearch(job);
                        AppDataJobDto modifiedJob = appDataService.patchJob(
                                job.getAppDataJob().getIdentifier(),
                                job.getAppDataJob(), false, true, false);
                        job.setAppDataJob(modifiedJob);
                        updateState(job, AppDataJobGenerationDtoState.PRIMARY_CHECK, reportInit);
                        reportInit.reportStop("End init job generation");
                    } catch (AbstractCodedException e) {
                        LOGGER.error(
                                "{} [productName {}] 1 - Pre-requirements not checked: {}",
                                this.prefixLogMonitor, productName,
                                e.getLogMessage());                      
                        updateState(job, AppDataJobGenerationDtoState.INITIAL, reportInit);
                        reportInit.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
                    }
                }

                // Search input
                if (job.getGeneration().getState() == AppDataJobGenerationDtoState.PRIMARY_CHECK) {
                	
                	final Reporting reportInputs = reportingFactory
                			.product(null, productName)                        			
                			.newReporting(2);
                	
                	reportInputs.reportStart("Start searching inputs");
                	
                    try {
                        LOGGER.info("{} [productName {}] 2 - Searching inputs",
                                this.prefixLogMonitor, job.getAppDataJob()
                                        .getProduct().getProductName());
                        this.inputsSearch(job);
                        updateState(job, AppDataJobGenerationDtoState.READY, reportInputs);
                        reportInputs.reportStop("End searching inputs");
                        
                    } catch (AbstractCodedException e) {
                        LOGGER.error(
                                "{} [productName {}] 2 - Inputs not found: {}",
                                this.prefixLogMonitor, productName,
                                e.getLogMessage());
                        updateState(job,AppDataJobGenerationDtoState.PRIMARY_CHECK, reportInputs);
                        reportInputs.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
                    }
                }

                // Prepare and send job if ready
                if (job.getGeneration().getState() == AppDataJobGenerationDtoState.READY) {
                	
                  	final Reporting reportPrep = reportingFactory
                			.product(null, productName)                        			
                			.newReporting(3);
                  	
                  	reportPrep.reportStart("Start job preparation and sending");
                	
                    try {
                        LOGGER.info("{} [productName {}] 2 - Searching inputs",
                                this.prefixLogMonitor, job.getAppDataJob()
                                        .getProduct().getProductName());
                        this.inputsSearch(job);
                        LOGGER.info("{} [productName {}] 3 - Sending job",
                                this.prefixLogMonitor, job.getAppDataJob()
                                        .getProduct().getProductName());
                        this.send(job);
                
                        updateState(job, AppDataJobGenerationDtoState.SENT, reportPrep);
                       
						if (job.getGeneration().getState() == AppDataJobGenerationDtoState.SENT) {
							reportPrep.reportStop("End job preparation and sending");

						} else {
							reportPrep.reportError("Job generation finished but job not sent");
						}
                    } catch (AbstractCodedException e) {
                        LOGGER.error("{} [productName {}] 3 - Job not send: {}",
                                this.prefixLogMonitor, productName,
                                e.getLogMessage());
                        updateState(job, AppDataJobGenerationDtoState.READY, reportPrep);
                        reportPrep.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
                    }
                }
                reporting.reportStop("End job generation");
            } catch (AbstractCodedException ace) {
                LOGGER.error(
                        "{} [productName {}] [code ] Cannot generate job: {}",
                        this.prefixLogMonitor, productName,
                        ace.getCode().getCode(), ace.getLogMessage());
                reporting.reportError("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage());
            }        
        }
    }

    private void updateState(JobGeneration job,
            AppDataJobGenerationDtoState newState,
            Reporting report
    )
        throws AbstractCodedException {
    	
    	report.reportDebug("Job generation before update: {} - {} - {} - {}", 
    			job.getAppDataJob().getIdentifier(),
                job.getGeneration().getTaskTable(), 
                newState,
                job.getGeneration()
        );
        AppDataJobDto modifiedJob = appDataService.patchTaskTableOfJob(
                job.getAppDataJob().getIdentifier(),
                job.getGeneration().getTaskTable(), newState);
        
        if (modifiedJob == null)
        {
        	throw new InternalErrorException("Catalog query returned null");
        }       
        
    	report.reportDebug("Modified job generations: {}",  modifiedJob.getGenerations());
        job.updateAppDataJob(modifiedJob, taskTableXmlName);        
    	report.reportDebug("Job generation after update: {}", job.getGeneration());

        // Log functional logs, not clear when this is called
        if (job.getAppDataJob().getState() == AppDataJobDtoState.TERMINATED) {
            final List<String> taskTables = job.getAppDataJob().getGenerations().stream()
            	.map(g -> g.getTaskTable())
            	.collect(Collectors.toList());

            LOGGER.info(
                    "{} [s1pdgsTask {}JobGeneration] [STOP OK] [productName {}] [outputs {}] Job finished",
                    this.prefixLogMonitor, this.taskTable.getLevel(),
                    job.getAppDataJob().getProduct().getProductName(),
                    taskTables);
        }
    }

    protected abstract void preSearch(JobGeneration job)
            throws JobGenInputsMissingException;

    protected void inputsSearch(JobGeneration job)
            throws JobGenInputsMissingException {
        // First, we evaluate each input query with no found file
        LOGGER.info("{} [productName {}] 2a - Requesting metadata",
                this.prefixLogMonitor,
                job.getAppDataJob().getProduct().getProductName());
        job.getMetadataQueries().forEach((k, v) -> {
            if (v != null && v.getResult() == null) {
                try {
                    List<SearchMetadata> file =
                            this.metadataService.search(v.getQuery(),
                                    DateUtils.convertToAnotherFormat(
                                            job.getAppDataJob().getProduct()
                                                    .getStartTime(),
                                            AppDataJobProductDto.TIME_FORMATTER,
                                            AbstractMetadata.DATE_FORMATTER),
                                    DateUtils.convertToAnotherFormat(
                                            job.getAppDataJob().getProduct()
                                                    .getStopTime(),
                                            AppDataJobProductDto.TIME_FORMATTER,
                                            AbstractMetadata.DATE_FORMATTER),
                                    job.getAppDataJob().getProduct()
                                            .getSatelliteId(),
                                    job.getAppDataJob().getProduct()
                                            .getInsConfId(),
                                    job.getAppDataJob().getProduct()
                                            .getProcessMode());
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Search metadata queries {}",
                    job.getMetadataQueries());
        }

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
                                    if (!CollectionUtils.isEmpty(job
                                            .getMetadataQueries()
                                            .get(alt.getIdSearchMetadataQuery())
                                            .getResult())) {

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
                                                .getInputfamilies().containsKey(
                                                        alt.getFileType())) {
                                            family = this.jobGeneratorSettings
                                                    .getInputfamilies()
                                                    .get(alt.getFileType());
                                        }

                                        // Check order
                                        List<JobOrderInputFile> jobOrderInputFiles =
                                                job.getMetadataQueries().get(alt
                                                        .getIdSearchMetadataQuery())
                                                        .getResult().stream()
                                                        .map(file -> new JobOrderInputFile(
                                                                file.getProductName(),
                                                                file.getKeyObjectStorage()))
                                                        .collect(Collectors
                                                                .toList());
                                        List<JobOrderTimeInterval> jobOrderTimeIntervals =
                                                job.getMetadataQueries().get(alt
                                                        .getIdSearchMetadataQuery())
                                                        .getResult().stream()
                                                        .map(file -> new JobOrderTimeInterval(
                                                                DateUtils
                                                                        .convertToAnotherFormat(
                                                                                file.getValidityStart(),
                                                                                file.getStartTimeFormatter(),
                                                                                JobOrderTimeInterval.DATE_FORMATTER),
                                                                DateUtils
                                                                        .convertToAnotherFormat(
                                                                                file.getValidityStop(),
                                                                                file.getStopTimeFormatter(),
                                                                                JobOrderTimeInterval.DATE_FORMATTER),
                                                                file.getProductName()))
                                                        .collect(Collectors
                                                                .toList());

                                        if (currentOrder == alt.getOrder()) {

                                            inputsToAdd.add(new JobOrderInput(
                                                    alt.getFileType(), type,
                                                    jobOrderInputFiles,
                                                    jobOrderTimeIntervals,
                                                    family));
                                        } else if (currentOrder > alt
                                                .getOrder()) {
                                            inputsToAdd = new ArrayList<>();
                                            inputsToAdd.add(new JobOrderInput(
                                                    alt.getFileType(), type,
                                                    jobOrderInputFiles,
                                                    jobOrderTimeIntervals,
                                                    family));
                                        }
                                        break;
                                    }
                                } else {
                                    DateTimeFormatter outFormatter =
                                            DateTimeFormatter.ofPattern(
                                                    "yyyyMMdd_HHmmssSSSSSS");
                                    String startDate =
                                            DateUtils.convertToAnotherFormat(
                                                    job.getAppDataJob()
                                                            .getProduct()
                                                            .getStartTime(),
                                                    AppDataJobProductDto.TIME_FORMATTER,
                                                    outFormatter);
                                    String stopDate =
                                            DateUtils.convertToAnotherFormat(
                                                    job.getAppDataJob()
                                                            .getProduct()
                                                            .getStopTime(),
                                                    AppDataJobProductDto.TIME_FORMATTER,
                                                    outFormatter);
                                    String filename = alt.getFileType();
                                    if (this.jobGeneratorSettings
                                            .getMapTypeMeta()
                                            .containsKey(alt.getFileType())) {
                                        filename = this.jobGeneratorSettings
                                                .getMapTypeMeta()
                                                .get(alt.getFileType());
                                    }
                                    inputsToAdd.add(new JobOrderInput(
                                            alt.getFileType(),
                                            JobOrderFileNameType.REGEXP,
                                            Arrays.asList(new JobOrderInputFile(
                                                    filename, "")),
                                            Arrays.asList(
                                                    new JobOrderTimeInterval(
                                                            startDate, stopDate,
                                                            filename,
                                                            outFormatter)),
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

    protected void send(JobGeneration job) throws AbstractCodedException {
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
        job.getJobOrder().getConf()
                .setSensingTime(new JobOrderSensingTime(
                        DateUtils.convertToAnotherFormat(
                                job.getAppDataJob().getProduct().getStartTime(),
                                AppDataJobProductDto.TIME_FORMATTER,
                                JobOrderSensingTime.DATETIME_FORMATTER),

                        DateUtils.convertToAnotherFormat(
                                job.getAppDataJob().getProduct().getStopTime(),
                                AppDataJobProductDto.TIME_FORMATTER,
                                JobOrderSensingTime.DATETIME_FORMATTER)));

        // Custom Job order according implementation
        this.customJobOrder(job);

        // Second, build the DTO
        String jobOrder = "/data/localWD/" + inc + "/JobOrder." + inc + ".xml";
        ProductFamily family = ProductFamily.L0_JOB;
        switch (l0ProcessSettings.getLevel()) {
            case L0:
                family = ProductFamily.L0_JOB;
                break;
            case L0_SEGMENT:
                family = ProductFamily.L0_SEGMENT_JOB;
                break;
            case L1:
                family = ProductFamily.L1_JOB;
                break;
            case L2:
                family = ProductFamily.L2_JOB;
                break;
        }
        final LevelJobDto r = new LevelJobDto(family,
                job.getAppDataJob().getProduct().getProductName(),
                job.getAppDataJob().getProduct().getProcessMode(), workingDir,
                jobOrder);

        try {

            // Add jobOrder inputs to the DTO
            List<JobOrderInput> distinctInputJobOrder = job.getJobOrder()
                    .getProcs().stream()
                    .filter(proc -> proc != null
                            && !CollectionUtils.isEmpty(proc.getInputs()))
                    .flatMap(proc -> proc.getInputs().stream()).distinct()
                    .collect(Collectors.toList());
            distinctInputJobOrder.forEach(input -> {
                for (JobOrderInputFile file : input.getFilenames()) {
                    r.addInput(new LevelJobInputDto(input.getFamily().name(),
                            file.getFilename(), file.getKeyObjectStorage()));
                }
            });

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

    protected abstract void customJobOrder(JobGeneration job);

    protected abstract void customJobDto(JobGeneration job, LevelJobDto dto);
}
