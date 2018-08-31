package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.AbstractJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;

/**
 * Dispatcher of EdrsSession product<br/>
 * Only one task table
 * 
 * @author Cyrielle Gailliard
 */
@Service
@ConditionalOnProperty(name = "process.level", havingValue = "L0")
public class EdrsSessionJobDispatcher
        extends AbstractJobsDispatcher<EdrsSessionDto> {

    /**
     * Task table
     */
    private static final String TASK_TABLE_NAME = "TaskTable.AIOP.xml";

    /**
     * Constructor
     * 
     * @param taskTablesSettings
     * @param jobsGeneratorFactory
     * @param jobGenerationTaskScheduler
     */
    @Autowired
    public EdrsSessionJobDispatcher(final JobGeneratorSettings settings,
            final ProcessSettings processSettings,
            final JobsGeneratorFactory factory,
            final ThreadPoolTaskScheduler taskScheduler,
            @Qualifier("appCatalogServiceForEdrsSessions") final AbstractAppCatalogJobService<EdrsSessionDto> appDataService) {
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
    protected AbstractJobsGenerator<EdrsSessionDto> createJobGenerator(
            final File xmlFile) throws AbstractCodedException {
        return this.factory.createJobGeneratorForEdrsSession(xmlFile, appDataService);
    }

    /**
     * Get task tables to generate for given job
     */
    @Override
    protected List<String> getTaskTables(
            final AppDataJobDto<EdrsSessionDto> job) {
        return Arrays.asList(TASK_TABLE_NAME);
    }

}
