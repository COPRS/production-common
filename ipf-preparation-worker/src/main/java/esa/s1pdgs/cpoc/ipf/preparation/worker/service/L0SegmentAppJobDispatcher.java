package esa.s1pdgs.cpoc.ipf.preparation.worker.service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerMissingRoutingEntryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.JobsGeneratorFactory.JobGenType;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

/**
 * Dispatcher of L0 slice product<br/>
 * 1 product to 1 or several task table<br/>
 * The routing is given in a XML file and is done by mapping the acquisition and
 * the mission and satellite identifier to a list of task tables
 * 
 * @author Cyrielle Gailliard
 */
public class L0SegmentAppJobDispatcher extends AbstractJobsDispatcher {
    private static final String TASK_TABLE_NAME = "TaskTable.L0ASP.xml";

    public L0SegmentAppJobDispatcher(
    		final IpfPreparationWorkerSettings settings,
            final ProcessSettings processSettings,
            final JobsGeneratorFactory factory,
            final ThreadPoolTaskScheduler taskScheduler,
            final AppCatalogJobClient<CatalogEvent> appDataService
    ) {
        super(settings, processSettings, factory, taskScheduler, appDataService);
    }

    @PostConstruct
    public void initialize() throws Exception {
        super.initTaskTables();
    }

    @Override
    protected AbstractJobsGenerator createJobGenerator(
            final File xmlFile
    ) throws AbstractCodedException {
        return factory.newJobGenerator(xmlFile, appDataService, JobGenType.LEVEL_SEGMENT);
    }

    /**
     * Get task tables to generate for given job
     * 
     * @throws IpfPrepWorkerMissingRoutingEntryException
     */
    @Override
    protected List<String> getTaskTables(final AppDataJob<CatalogEvent> job)
            throws IpfPrepWorkerMissingRoutingEntryException {
        return Arrays.asList(TASK_TABLE_NAME);
    }
}
