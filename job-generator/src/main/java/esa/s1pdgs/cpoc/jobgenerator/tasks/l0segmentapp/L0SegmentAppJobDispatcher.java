package esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMissingRoutingEntryException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * Dispatcher of L0 slice product<br/>
 * 1 product to 1 or several task table<br/>
 * The routing is given in a XML file and is done by mapping the acquisition and
 * the mission and satellite identifier to a list of task tables
 * 
 * @author Cyrielle Gailliard
 */
public class L0SegmentAppJobDispatcher extends AbstractJobsDispatcher<ProductDto> {

    /**
     * Task table
     */
    private static final String TASK_TABLE_NAME = "TaskTable.L0ASP.xml";
    
    /**
     * 
     */
    private String taskForFunctionalLog;

    /**
     * @param settings
     * @param factory
     * @param taskScheduler
     * @param xmlConverter
     * @param pathRoutingXmlFile
     */
    public L0SegmentAppJobDispatcher(final JobGeneratorSettings settings,
            final ProcessSettings processSettings,
            final JobsGeneratorFactory factory,
            final ThreadPoolTaskScheduler taskScheduler,
            final AppCatalogJobClient appDataService) {
        super(settings, processSettings, factory, taskScheduler,
                appDataService);
    }

    /**
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
    protected AbstractJobsGenerator<ProductDto> createJobGenerator(
            final File xmlFile) throws AbstractCodedException {
        return this.factory.createJobGeneratorForL0Segment(xmlFile,
                appDataService);
    }

    /**
     * Get task tables to generate for given job
     * 
     * @throws JobGenMissingRoutingEntryException
     */
    @Override
    protected List<String> getTaskTables(final AppDataJobDto job)
            throws JobGenMissingRoutingEntryException {
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
