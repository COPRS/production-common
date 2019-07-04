package esa.s1pdgs.cpoc.jobgenerator.tasks.levelproducts;

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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.JobGenMissingRoutingEntryException;
import esa.s1pdgs.cpoc.jobgenerator.config.JobGeneratorSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.routing.LevelProductsRouting;
import esa.s1pdgs.cpoc.jobgenerator.service.XmlConverter;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsGenerator;
import esa.s1pdgs.cpoc.jobgenerator.tasks.JobsGeneratorFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

/**
 * 
 * Dispatcher of L1 and L2 slice products.
 * 
 * The routing is given in a XML file and is done by mapping the acquisition and
 * the mission and satellite identifier to a list of task tables.
 * 
 * @author birol_colak@net.werum
 *
 */
public class LevelProductsJobDispatcher extends AbstractJobsDispatcher<ProductDto> {

	 /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(LevelProductsJobDispatcher.class);

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
    public LevelProductsJobDispatcher(final JobGeneratorSettings settings,
            final ProcessSettings processSettings,
            final JobsGeneratorFactory factory,
            final ThreadPoolTaskScheduler taskScheduler,
            final XmlConverter xmlConverter,
            final String pathRoutingXmlFile,
            final AppCatalogJobClient appDataService) {
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
            LevelProductsRouting routing = (LevelProductsRouting) xmlConverter
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
    protected AbstractJobsGenerator<ProductDto> createJobGenerator(
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
    protected List<String> getTaskTables(final AppDataJobDto job)
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
            throw new JobGenMissingRoutingEntryException(
                    String.format("No found routing entries for %s", key));
        }
        return taskTables;
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
