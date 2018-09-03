package esa.s1pdgs.cpoc.jobgenerator.tasks.dispatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMaxNumberTaskTablesReachException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.tasks.generator.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.generator.JobsGeneratorFactory;

/**
 * Job dispatcher<br/>
 * <ul>
 * At application starting:
 * <li>a job generator is initialized per task tables present in a
 * directory</li>
 * <li>for each current generating job for this pod (in applicative data), their
 * message is dispatched again to consider any modification done on the list of
 * task tables</li>
 * <li>the job generator initialized are started</li>
 * </ul>
 * When a message is read and can be processing, it will be dispatch to one or
 * several task tables according the product category (the list of task tables
 * is given by the abstract method {@link #getTaskTables(AppDataJobDto)}
 * 
 * @param <T>
 */
public abstract class AbstractJobsDispatcher<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(AbstractJobsDispatcher.class);

    /**
     * Job Generator settings
     */
    protected final JobGeneratorSettings settings;

    /**
     * Process settings
     */
    protected final ProcessSettings processSettings;

    /**
     * Available job generators (one per task tables)
     */
    protected Map<String, AbstractJobsGenerator<T>> generators;

    /**
     * Scheduler containing the job generation processors
     */
    protected final ThreadPoolTaskScheduler taskScheduler;

    /**
     * Job generator factory
     */
    protected final JobsGeneratorFactory factory;

    /**
     * Applicative data service
     */
    protected final AbstractAppCatalogJobService<T> appDataService;

    /**
     * Constructor
     * 
     * @param settings
     * @param factory
     * @param taskScheduler
     */
    public AbstractJobsDispatcher(final JobGeneratorSettings settings,
            final ProcessSettings processSettings,
            final JobsGeneratorFactory factory,
            final ThreadPoolTaskScheduler taskScheduler,
            final AbstractAppCatalogJobService<T> appDataService) {
        this.factory = factory;
        this.settings = settings;
        this.processSettings = processSettings;
        this.generators = new HashMap<>(this.settings.getMaxnboftasktable());
        this.taskScheduler = taskScheduler;
        this.appDataService = appDataService;
    }

    /**
     * Initialize the task table processors
     * <li>a job generator is initialized per task tables present in a
     * directory</li>
     * <li>for each current generating job for this pod (in applicative data),
     * their message is dispatched again to consider any modification done on
     * the list of task tables</li>
     * <li>the job generator initialized are started</li>
     * 
     * @throws AbstractCodedException
     */
    protected void initTaskTables() throws AbstractCodedException {
        // Retrieve list of XML files in the directory
        File directoryXml = new File(this.settings.getDiroftasktables());
        if (directoryXml != null && directoryXml.isDirectory()) {
            File[] taskTableFiles =
                    directoryXml.listFiles(parameter -> parameter.isFile());
            if (taskTableFiles != null) {
                if (taskTableFiles.length > this.settings
                        .getMaxnboftasktable()) {
                    throw new JobGenMaxNumberTaskTablesReachException(
                            String.format("Too much task tables %d",
                                    taskTableFiles.length));
                }
                for (File taskTableFile : taskTableFiles) {
                    AbstractJobsGenerator<T> jobGenerator =
                            this.createJobGenerator(taskTableFile);
                    generators.put(taskTableFile.getName(), jobGenerator);
                }
            }
        }

        // Dispatch existing job with current task table configuration
        if (processSettings.getMode() != ApplicationMode.TEST) {
            List<AppDataJobDto<T>> generatingJobs = appDataService
                    .findByPodAndState(processSettings.getHostname(),
                            AppDataJobDtoState.GENERATING);
            if (CollectionUtils.isEmpty(generatingJobs)) {
                for (AppDataJobDto<T> generation : generatingJobs) {
                    // TODO ask if bypass error
                    dispatch(generation);
                }
            }
        }

        // Launch generators
        for (String taskTable : generators.keySet()) {
            taskScheduler.scheduleWithFixedDelay(generators.get(taskTable),
                    settings.getJobgenfixedrate());
        }
    }

    /**
     * Create a job generator from the task table XML file
     * 
     * @param xmlFile
     * @return
     * @throws AbstractCodedException
     */
    protected abstract AbstractJobsGenerator<T> createJobGenerator(
            final File xmlFile) throws AbstractCodedException;

    /**
     * Dispatch a job to one or several task tables <br/>
     * A generation per task table will be created/update/removed in the
     * applicative data catalog for this job
     * 
     * @param job
     * @throws AbstractCodedException
     */
    public void dispatch(final AppDataJobDto<T> job)
            throws AbstractCodedException {
        String productName = job.getProduct().getProductName();
        try {
            LOGGER.info(
                    "[REPORT] [productName {}] [s1pdgsTask {}] [subTask Dispatch] [START] Dispatching product",
                    productName,
                    getTaskForFunctionalLog());

            List<String> taskTables = getTaskTables(job);
            List<String> notDealTaskTables = new ArrayList<>(taskTables);
            List<AppDataJobGenerationDto> jobGens = job.getGenerations();

            // Build the new job generations
            boolean needUpdate = false;
            if (CollectionUtils.isEmpty(jobGens)) {
                // No current generation, add the new ones
                for (String table : taskTables) {
                    needUpdate = true;
                    AppDataJobGenerationDto jobGen =
                            new AppDataJobGenerationDto();
                    jobGen.setTaskTable(table);
                    job.getGenerations().add(jobGen);
                }
            } else {
                // Some generation already exists, update or delete the useless
                // ones
                for (Iterator<AppDataJobGenerationDto> iterator =
                        jobGens.iterator(); iterator.hasNext();) {
                    AppDataJobGenerationDto jobGen = iterator.next();
                    if (taskTables.contains(jobGen.getTaskTable())) {
                        notDealTaskTables.remove(jobGen.getTaskTable());
                    } else {
                        needUpdate = true;
                        iterator.remove();
                    }
                }
                // Create the new ones
                for (String taskTable : notDealTaskTables) {
                    needUpdate = true;
                    AppDataJobGenerationDto jobGen =
                            new AppDataJobGenerationDto();
                    jobGen.setTaskTable(taskTable);
                    job.getGenerations().add(jobGen);
                }
            }

            // Update task tables
            if (needUpdate) {
                job.setState(AppDataJobDtoState.GENERATING);
                appDataService.patchJob(job.getIdentifier(), job, false, false,
                        true);
            }

            LOGGER.info(
                    "[REPORT] [productName {}] [s1pdgsTask {}] [subTask Dispatch] [STOP OK] [outputs {}] Product dispatched",
                    productName,
                    getTaskForFunctionalLog(), taskTables);

        } catch (AbstractCodedException ace) {
            LOGGER.error(
                    "[REPORT] [productName {}] [s1pdgsTask {}] [subTask Dispatch] [STOP KO] {} Dispatching product failed ",
                    productName,
                    getTaskForFunctionalLog(), ace.getLogMessage());
            throw ace;
        }
    }

    /**
     * Get task tables to generate for given job
     */
    protected abstract List<String> getTaskTables(final AppDataJobDto<T> job)
            throws AbstractCodedException;

    protected abstract String getTaskForFunctionalLog();
}
