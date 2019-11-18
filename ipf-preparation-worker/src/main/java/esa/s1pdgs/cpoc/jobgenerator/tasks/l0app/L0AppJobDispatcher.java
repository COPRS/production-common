package esa.s1pdgs.cpoc.jobgenerator.tasks.l0app;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionEvent;

/**
 * Dispatcher of EdrsSession product<br/>
 * Only one task table
 * 
 * @author Cyrielle Gailliard
 */
public class L0AppJobDispatcher extends AbstractJobsDispatcher<IngestionEvent> {

    /**
     * Task table
     */
    private static final String TASK_TABLE_NAME = "TaskTable.AIOP.xml";
    
    /**
     * 
     */
    private String taskForFunctionalLog;

    /**
     * Constructor
     * 
     * @param taskTablesSettings
     * @param jobsGeneratorFactory
     * @param jobGenerationTaskScheduler
     */
    public L0AppJobDispatcher(final JobGeneratorSettings settings,
            final ProcessSettings processSettings,
            final JobsGeneratorFactory factory,
            final ThreadPoolTaskScheduler taskScheduler,
            final AppCatalogJobClient<IngestionEvent> appDataService) {
        super(settings, processSettings, factory, taskScheduler,
                appDataService);
    }

    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @PostConstruct
    public void initialize() throws AbstractCodedException {
        // Init job generators from task tables
        super.initTaskTables();
    }

    /**
     * 
     */
    @Override
    protected AbstractJobsGenerator<IngestionEvent> createJobGenerator(
            final File xmlFile) throws AbstractCodedException {
        return this.factory.createJobGeneratorForEdrsSession(xmlFile, appDataService);
    }

    /**
     * Get task tables to generate for given job
     */
    @Override
    protected List<String> getTaskTables(final AppDataJob<IngestionEvent> job) {
        return Arrays.asList(TASK_TABLE_NAME);
    }

    @Override
    protected String getTaskForFunctionalLog() {
    	return this.taskForFunctionalLog;
    }
    
    @Override
    public void setTaskForFunctionalLog(String taskForFunctionalLog) {
    	this.taskForFunctionalLog = taskForFunctionalLog; 
    }
}
