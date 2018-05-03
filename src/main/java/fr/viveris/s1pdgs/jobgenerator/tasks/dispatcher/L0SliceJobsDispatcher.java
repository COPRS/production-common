package fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.jobgenerator.config.JobGeneratorSettings;
import fr.viveris.s1pdgs.jobgenerator.exception.AbstractCodedException;
import fr.viveris.s1pdgs.jobgenerator.exception.InternalErrorException;
import fr.viveris.s1pdgs.jobgenerator.exception.MissingRoutingEntryException;
import fr.viveris.s1pdgs.jobgenerator.model.Job;
import fr.viveris.s1pdgs.jobgenerator.model.l1routing.L1Routing;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0Slice;
import fr.viveris.s1pdgs.jobgenerator.model.product.L0SliceProduct;
import fr.viveris.s1pdgs.jobgenerator.service.XmlConverter;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.AbstractJobsGenerator;
import fr.viveris.s1pdgs.jobgenerator.tasks.generator.JobsGeneratorFactory;

@Service
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "l0-slices")
public class L0SliceJobsDispatcher extends AbstractJobsDispatcher<L0Slice> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(L0SliceJobsDispatcher.class);

	private final XmlConverter xmlConverter;

	/**
	 * Routing table<br/>
	 * key = {acquisition_satelliteId} 'IW_A' -> {taskTable1.xml; TaskTable2.xml}
	 */
	protected final Map<String, List<String>> routingMap;

	protected final String pathRoutingXmlFile;

	@Autowired
	public L0SliceJobsDispatcher(JobGeneratorSettings taskTablesSettings, JobsGeneratorFactory jobsGeneratorFactory,
			ThreadPoolTaskScheduler jobGenerationTaskScheduler, final XmlConverter xmlConverter,
			@Value("${level1.pathroutingxmlfile}") String pathRoutingXmlFile) {
		super(taskTablesSettings, jobsGeneratorFactory, jobGenerationTaskScheduler);
		this.xmlConverter = xmlConverter;
		this.routingMap = new HashMap<String, List<String>>();
		this.pathRoutingXmlFile = pathRoutingXmlFile;
	}

	@PostConstruct
	public void initialize() throws AbstractCodedException {
		// Init job generators from task tables
		super.initTaskTables();

		// Init the routing map from XML file located in the task table folder
		try {
			L1Routing routing = (L1Routing) xmlConverter.convertFromXMLToObject(this.pathRoutingXmlFile);
			if (routing != null && routing.getRoutes() != null) {
				routing.getRoutes().stream().forEach(route -> {
					String key = route.getFrom().getAcquisition() + "_" + route.getFrom().getSatelliteId();
					this.routingMap.put(key, route.getTo().getTaskTables());
				});
			}
		} catch (IOException | JAXBException e) {
			throw new InternalErrorException(
					String.format("Cannot parse routing XML file located in %s", this.pathRoutingXmlFile));
		}
	}

	@Override
	protected AbstractJobsGenerator<L0Slice> createJobGenerator(File xmlFile) throws AbstractCodedException {
		return this.jobsGeneratorFactory.createJobGeneratorForL0Slice(xmlFile);
	}

	@Override
	public void dispatch(Job<L0Slice> job) throws AbstractCodedException {
		String key = job.getProduct().getObject().getAcquisition() + "_" + job.getProduct().getSatelliteId();
		if (this.routingMap.containsKey(key)) {
			for (String taskTable : this.routingMap.get(key)) {
				if (this.generators.containsKey(taskTable)) {
					L0SliceProduct p = (L0SliceProduct) job.getProduct();
					L0SliceProduct pClone = new L0SliceProduct(p.getIdentifier(), p.getSatelliteId(), p.getMissionId(),
							p.getStartTime(), p.getStopTime(), new L0Slice(p.getObject().getAcquisition()));
					Job<L0Slice> cloneJob = new Job<>(pClone);
					LOGGER.info("[MONITOR] [Step 2] [productName {}] [taskTable {}] Caching job",
							job.getProduct().getIdentifier(), taskTable);
					this.generators.get(taskTable).addJob(cloneJob);
				} else {
					LOGGER.warn("[MONITOR] [Step 2] [productName {}] Task table {} not found",
							job.getProduct().getIdentifier(), taskTable);
				}
			}
		} else {
			throw new MissingRoutingEntryException(String.format("No found routing entries for %s", key));
		}
	}

}
