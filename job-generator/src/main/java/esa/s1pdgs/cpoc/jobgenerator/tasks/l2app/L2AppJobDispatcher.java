package esa.s1pdgs.cpoc.jobgenerator.tasks.l2app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMissingRoutingEntryException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.l2routing.L2Routing;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;

@Service
@ConditionalOnProperty(name = "process.level", havingValue = "L2")
public class L2AppJobDispatcher
        extends AbstractJobsDispatcher<LevelProductDto> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(L2AppJobDispatcher.class);

    /**
     * XML converter
     */
    private final XmlConverter xmlConverter;

    /**
     * Routing table<br/>
     * key = {acquisition_satelliteId} 'IW_A' -> {taskTable1.xml;
     * TaskTable2.xml}
     */
    protected final Map<Pattern, List<String>> routingMap;

    /**
     * Path of the routinh XML file
     */
    protected final String pathRoutingXmlFile;

    /**
     * @param settings
     * @param factory
     * @param taskScheduler
     * @param xmlConverter
     * @param pathRoutingXmlFile
     */
    @Autowired
    public L2AppJobDispatcher(final JobGeneratorSettings settings,
            final ProcessSettings processSettings,
            final JobsGeneratorFactory factory,
            final ThreadPoolTaskScheduler taskScheduler,
            final XmlConverter xmlConverter,
            @Value("${level2.pathroutingxmlfile}") String pathRoutingXmlFile,
            @Qualifier("appCatalogServiceForLevelProducts") final AbstractAppCatalogJobService<LevelProductDto> appDataService) {
        super(settings, processSettings, factory, taskScheduler,
                appDataService);
        this.xmlConverter = xmlConverter;
        this.routingMap = new HashMap<Pattern, List<String>>();
        this.pathRoutingXmlFile = pathRoutingXmlFile;
    }

    /**
     * @throws AbstractCodedException
     */
    @PostConstruct
    public void initialize() throws AbstractCodedException {

        // Init the routing map from XML file located in the task table folder
        try {
            L2Routing routing = (L2Routing) xmlConverter
                    .convertFromXMLToObject(this.pathRoutingXmlFile);
            if (routing != null && routing.getRoutes() != null) {
                routing.getRoutes().stream().forEach(route -> {
                    String key = route.getRouteFrom().getAcquisition() + "_"
                            + route.getRouteFrom().getSatelliteId();
                    this.routingMap.put(Pattern.compile(key, Pattern.CASE_INSENSITIVE),
                            route.getRouteTo().getTaskTables());
                });
            }
        } catch (IOException | JAXBException e) {
            throw new InternalErrorException(
                    String.format("Cannot parse routing XML file located in %s",
                            this.pathRoutingXmlFile),
                    e);
        }
        
        // Init job generators from task tables
        super.initTaskTables();
    }

    /**
     * 
     */
    @Override
    protected AbstractJobsGenerator<LevelProductDto> createJobGenerator(
			final File xmlFile) throws AbstractCodedException {
		return this.factory.createJobGeneratorForL0Slice(xmlFile, ApplicationLevel.L2, appDataService);
	}

    /**
     * Get task tables to generate for given job
     * 
     * @throws JobGenMissingRoutingEntryException
     */
    @Override
    protected List<String> getTaskTables(
            final AppDataJobDto<LevelProductDto> job)
            throws JobGenMissingRoutingEntryException {
        List<String> taskTables = new ArrayList<>();
        String key = job.getProduct().getAcquisition() + "_"
                + job.getProduct().getSatelliteId();
        routingMap.forEach((k,v) -> {
            if (k.matcher(key).matches()) {
                for (String taskTable : v) {
                    if (generators.containsKey(taskTable)) {
                        taskTables.add(taskTable);
                    } else {
                        LOGGER.warn(
                                "[MONITOR] [Step 2] [productName {}] Task table {} not found",
                                job.getProduct().getProductName(), taskTable);
                    }
                }
            }
        });
        if (taskTables.isEmpty()) {
//            throw new JobGenMissingRoutingEntryException(
//                    String.format("No found routing entries for %s", key));
        }
        return taskTables;
    }

    @Override
    protected String getTaskForFunctionalLog() {
        return "L2JobGeneration";
    }

}