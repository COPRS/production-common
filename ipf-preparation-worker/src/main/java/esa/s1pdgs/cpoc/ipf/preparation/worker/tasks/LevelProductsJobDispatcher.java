package esa.s1pdgs.cpoc.ipf.preparation.worker.tasks;

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

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerMissingRoutingEntryException;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.routing.LevelProductsRouting;
import esa.s1pdgs.cpoc.ipf.preparation.worker.service.XmlConverter;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

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
public class LevelProductsJobDispatcher extends AbstractJobsDispatcher {
    private static final Logger LOGGER = LogManager.getLogger(LevelProductsJobDispatcher.class);

    private final XmlConverter xmlConverter;
    protected final Map<Pattern, List<String>> routingMap;
    protected final String pathRoutingXmlFile;

    public LevelProductsJobDispatcher(
    		final IpfPreparationWorkerSettings settings,
            final ProcessSettings processSettings,
            final JobsGeneratorFactory factory,
            final ThreadPoolTaskScheduler taskScheduler,
            final XmlConverter xmlConverter,
            final String pathRoutingXmlFile,
            final AppCatalogJobClient<CatalogEvent> appDataService
    ) {
        super(settings, processSettings, factory, taskScheduler, appDataService);
        this.xmlConverter = xmlConverter;
        this.routingMap = new HashMap<Pattern, List<String>>();
        this.pathRoutingXmlFile = pathRoutingXmlFile;
    }

    @PostConstruct
    public void initialize() throws Exception {

        // Init the routing map from XML file located in the task table folder
        try {
            final LevelProductsRouting routing = (LevelProductsRouting) xmlConverter
                    .convertFromXMLToObject(this.pathRoutingXmlFile);
            if (routing != null && routing.getRoutes() != null) {
                routing.getRoutes().stream().forEach(route -> {
                    final String key = route.getRouteFrom().getAcquisition() + "_"
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

    @Override
    protected AbstractJobsGenerator createJobGenerator(
            final File xmlFile
    ) throws AbstractCodedException {
        return this.factory.createJobGeneratorForL0Slice(xmlFile, appDataService);
    }

    @Override
    protected List<String> getTaskTables(final AppDataJob<CatalogEvent> job)
            throws IpfPrepWorkerMissingRoutingEntryException {
        final List<String> taskTables = new ArrayList<>();
        final String key = job.getProduct().getAcquisition() + "_"
                + job.getProduct().getSatelliteId();
        LOGGER.debug("Searching tasktable for {}", key);
        routingMap.forEach((k,v) -> {
            if (k.matcher(key).matches()) {
                for (final String taskTable : v) {
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
            LOGGER.warn("No tasktable found for {} in: {}", key, routingMap);
        }        
        return taskTables;
    }
}
