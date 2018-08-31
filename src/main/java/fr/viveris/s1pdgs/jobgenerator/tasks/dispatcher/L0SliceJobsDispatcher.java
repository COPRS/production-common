package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMissingRoutingEntryException;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.model.l1routing.L1Routing;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.AbstractJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;

/**
 * Dispatcher of L0 slice product<br/>
 * 1 product to 1 or several task table<br/>
 * The routing is given in a XML file and is done by mapping the acquisition and
 * the mission and satellite identifier to a list of task tables
 * 
 * @author Cyrielle Gailliard
 */
@Service
@ConditionalOnProperty(name = "process.level", havingValue = "L1")
public class L0SliceJobsDispatcher
        extends AbstractJobsDispatcher<LevelProductDto> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(L0SliceJobsDispatcher.class);

    /**
     * XML converter
     */
    private final XmlConverter xmlConverter;

    /**
     * Routing table<br/>
     * key = {acquisition_satelliteId} 'IW_A' -> {taskTable1.xml;
     * TaskTable2.xml}
     */
    protected final Map<String, List<String>> routingMap;

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
    public L0SliceJobsDispatcher(final JobGeneratorSettings settings,
            final ProcessSettings processSettings,
            final JobsGeneratorFactory factory,
            final ThreadPoolTaskScheduler taskScheduler,
            final XmlConverter xmlConverter,
            @Value("${level1.pathroutingxmlfile}") String pathRoutingXmlFile,
            @Qualifier("appCatalogServiceForLevelProducts") final AbstractAppCatalogJobService<LevelProductDto> appDataService) {
        super(settings, processSettings, factory, taskScheduler,
                appDataService);
        this.xmlConverter = xmlConverter;
        this.routingMap = new HashMap<String, List<String>>();
        this.pathRoutingXmlFile = pathRoutingXmlFile;
    }

    /**
     * @throws AbstractCodedException
     */
    @PostConstruct
    public void initialize() throws AbstractCodedException {
        // Init job generators from task tables
        super.initTaskTables();

        // Init the routing map from XML file located in the task table folder
        try {
            L1Routing routing = (L1Routing) xmlConverter
                    .convertFromXMLToObject(this.pathRoutingXmlFile);
            if (routing != null && routing.getRoutes() != null) {
                routing.getRoutes().stream().forEach(route -> {
                    String key = route.getRouteFrom().getAcquisition() + "_"
                            + route.getRouteFrom().getSatelliteId();
                    this.routingMap.put(key,
                            route.getRouteTo().getTaskTables());
                });
            }
        } catch (IOException | JAXBException e) {
            throw new InternalErrorException(
                    String.format("Cannot parse routing XML file located in %s",
                            this.pathRoutingXmlFile),
                    e);
        }
    }

    /**
     * 
     */
    @Override
    protected AbstractJobsGenerator<LevelProductDto> createJobGenerator(
            final File xmlFile) throws AbstractCodedException {
        return this.factory.createJobGeneratorForL0Slice(xmlFile,
                appDataService);
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
        if (this.routingMap.containsKey(key)) {
            for (String taskTable : this.routingMap.get(key)) {
                if (this.generators.containsKey(taskTable)) {
                    taskTables.add(taskTable);
                } else {
                    LOGGER.warn(
                            "[MONITOR] [Step 2] [productName {}] Task table {} not found",
                            job.getProduct().getProductName(), taskTable);
                }
            }
        } else {
            throw new JobGenMissingRoutingEntryException(
                    String.format("No found routing entries for %s", key));
        }
        return taskTables;
    }

}
