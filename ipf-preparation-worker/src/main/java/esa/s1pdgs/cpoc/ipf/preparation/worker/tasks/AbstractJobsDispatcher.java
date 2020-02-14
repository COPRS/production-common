package esa.s1pdgs.cpoc.ipf.preparation.worker.tasks;

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

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.ApplicationMode;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerMaxNumberTaskTablesReachException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

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
public abstract class AbstractJobsDispatcher {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(AbstractJobsDispatcher.class);

    /**
     * Job Generator settings
     */
    protected final IpfPreparationWorkerSettings settings;

    /**
     * Process settings
     */
    protected final ProcessSettings processSettings;

    /**
     * Available job generators (one per task tables)
     */
    protected Map<String, AbstractJobsGenerator> generators;

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
    protected final AppCatalogJobClient<CatalogEvent> appDataService;

    /**
     * Constructor
     * 
     * @param settings
     * @param factory
     * @param taskScheduler
     */
    public AbstractJobsDispatcher(final IpfPreparationWorkerSettings settings,
            final ProcessSettings processSettings,
            final JobsGeneratorFactory factory,
            final ThreadPoolTaskScheduler taskScheduler,
            final AppCatalogJobClient<CatalogEvent> appDataService) {
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
        final File directoryXml = new File(this.settings.getDiroftasktables());
        if (directoryXml != null && directoryXml.isDirectory()) {
            final File[] taskTableFiles =
                    directoryXml.listFiles(parameter -> parameter.isFile());
            if (taskTableFiles != null) {
                if (taskTableFiles.length > this.settings
                        .getMaxnboftasktable()) {
                    throw new IpfPrepWorkerMaxNumberTaskTablesReachException(
                            String.format("Too much task tables %d",
                                    taskTableFiles.length));
                }
                for (final File taskTableFile : taskTableFiles) {
                    final AbstractJobsGenerator jobGenerator = createJobGenerator(taskTableFile);
                    generators.put(taskTableFile.getName(), jobGenerator);
                }
            }
        }

        // Dispatch existing job with current task table configuration
        if (processSettings.getMode() != ApplicationMode.TEST) {
            final List<AppDataJob<CatalogEvent>> generatingJobs = appDataService
                    .findByPodAndState(processSettings.getTriggerHostname(), AppDataJobState.GENERATING);
            if (!CollectionUtils.isEmpty(generatingJobs)) {
                for (final AppDataJob<CatalogEvent> generation : generatingJobs) {
                    // TODO ask if bypass error
                    dispatch(generation);
                }
            }
        }

        // Launch generators
        for (final String taskTable : generators.keySet()) {
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
    protected abstract AbstractJobsGenerator createJobGenerator(
            final File xmlFile) throws AbstractCodedException;

    /**
     * Dispatch a job to one or several task tables <br/>
     * A generation per task table will be created/update/removed in the
     * applicative data catalog for this job
     * 
     * @param job
     * @throws AbstractCodedException
     */
    public void dispatch(final AppDataJob<CatalogEvent> job)
            throws AbstractCodedException {
    	LOGGER.debug ("== dispatch job {}", job.toString());
        final String productName = job.getProduct().getProductName();
        final Reporting reporting = ReportingUtils.newReportingBuilder().newReporting("Dispatch");

    	reporting.begin(new ReportingMessage("Start dispatching product"));
    	
        try {
            final List<String> taskTables = getTaskTables(job);
            if (taskTables.isEmpty())
            {
             LOGGER.info("Nothing to do as no TaskTables found for {}.",productName);
            }
            final List<String> notDealTaskTables = new ArrayList<>(taskTables);
            final List<AppDataJobGeneration> jobGens = job.getGenerations();
            LOGGER.debug ("== job.getGenerations() {}", jobGens.toString());

            // Build the new job generations
            boolean needUpdate = false;
            if (CollectionUtils.isEmpty(jobGens)) {
                // No current generation, add the new ones
                for (final String table : taskTables) {
                    needUpdate = true;
                    final AppDataJobGeneration jobGen =
                            new AppDataJobGeneration();
                    jobGen.setTaskTable(table);
                    job.getGenerations().add(jobGen);
                }
            } else {
                // Some generation already exists, update or delete the useless
                // ones
                for (final Iterator<AppDataJobGeneration> iterator =
                        jobGens.iterator(); iterator.hasNext();) {
                    final AppDataJobGeneration jobGen = iterator.next();
                    if (taskTables.contains(jobGen.getTaskTable())) {
                        notDealTaskTables.remove(jobGen.getTaskTable());
                    } else {
                        needUpdate = true;
                        iterator.remove();
                    }
                }
                // Create the new ones
                for (final String taskTable : notDealTaskTables) {
                    needUpdate = true;
                    final AppDataJobGeneration jobGen =
                            new AppDataJobGeneration();
                    jobGen.setTaskTable(taskTable);
                    job.getGenerations().add(jobGen);
                }
            }

            // Update task tables
            if (needUpdate) {
                job.setState(AppDataJobState.GENERATING);
                appDataService.patchJob(job.getId(), job, false, false, true);
            }
            LOGGER.debug ("== dispatched job {}", job.toString());
            reporting.end(new ReportingMessage("End dispatching product"));


        } catch (final AbstractCodedException ace) {
        	reporting.error(new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage()));
            throw ace;
        }
    }

    /**
     * Get task tables to generate for given job
     */
    protected abstract List<String> getTaskTables(final AppDataJob<CatalogEvent> job)
            throws AbstractCodedException;

    /**
     * @return the generators
     */
    public Map<String, AbstractJobsGenerator> getGenerators() {
        return generators;
    }
    
}
