package fr.viveris.s1pdgs.scaler.scaling;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.scaler.k8s.K8SAdministration;
import fr.viveris.s1pdgs.scaler.kafka.KafkaMonitoring;
import fr.viveris.s1pdgs.scaler.kafka.model.KafkaPerGroupPerTopicMonitor;
import fr.viveris.s1pdgs.scaler.openstack.OpenStackAdministration;

/**
 * L1 resources scaler
 * 
 * @author Cyrielle Gailliard
 *
 */
@Component
public class Scaler {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Scaler.class);

	/**
	 * Service for monitoring KAFKA
	 */
	private final KafkaMonitoring kafkaMonitoring;

	private final K8SAdministration k8SAdministration;

	private final OpenStackAdministration osAdministration;

	public enum ScalingAction {
		ALLOC, FREE, NOTHING, ERROR
	};

	/**
	 * Constructor
	 * 
	 * @param kafkaMonitoring
	 * @param properties
	 */
	@Autowired
	public Scaler(final KafkaMonitoring kafkaMonitoring, final K8SAdministration k8SAdministration,
			final OpenStackAdministration osAdministration) {
		this.kafkaMonitoring = kafkaMonitoring;
		this.k8SAdministration = k8SAdministration;
		this.osAdministration = osAdministration;
	}

	/**
	 * <ul>
	 * Scaling:
	 * <li>1: Monitor topic of L1 jobs</li>
	 * <li>2: Monitor L1 wrappers</li>
	 * <li>3: Calculate the value</li>
	 * <li>4: Scales the L1 resources</li>
	 * <ul>
	 */
	@Scheduled(fixedDelayString = "${scaler.fixed-delay-ms}")
	public void scale() {
		LOGGER.info("[MONITOR] [Step 0] Starting scaling");

		try {

			// Delete unused resources
			LOGGER.info("[MONITOR] [Step 1] Starting removing unused resources");
			this.deleteUnusedResources();

			// Monitor KAFKA
			LOGGER.info("[MONITOR] [Step 2] Starting monitoring KAFKA");
			KafkaPerGroupPerTopicMonitor monitorKafka = this.kafkaMonitoring.monitorL1Jobs();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[MONITOR] [Step 2] Monitored information {}", monitorKafka);
			}

			// Monitor K8S
			// Listing all the L1 wrappers pods
			LOGGER.info("[MONITOR] [Step 3] Starting monitoring K8S");

			// Calculate value for scaling
			LOGGER.info("[MONITOR] [Step 5] Starting determinating scaling action");
			long monitoredValue = this.calculateMonitoredValue();
			ScalingAction scalingAction = this.needScaling(monitoredValue);

			// Scale
			LOGGER.info("[MONITOR] [Step 6] Starting applying scaling action {}", scalingAction.name());
			switch (scalingAction) {
			case ALLOC:
				this.addRessources();
				break;
			case FREE:
				this.freeRessources();
				break;
			case NOTHING:
				LOGGER.debug("");
				break;
			default:
				LOGGER.error("");
				break;
			}

		} catch (Exception e) {
			LOGGER.error("Error during scaling: {}", e);
		}
		LOGGER.info("[MONITOR] [Step 0] End");
	}

	private long calculateMonitoredValue() {
		return 0;
	}

	private ScalingAction needScaling(long monitoredValue) {
		return ScalingAction.NOTHING;
	}

	private void addRessources() {

		// Create VM

		// All labels

		// Launchs pods
	}

	private void freeRessources() {

	}

	private void deleteUnusedResources() {
		// Retrieve K8S workers set in pause with no active pods
		List<String> serverIdsToDelete = this.k8SAdministration.getExternalIdsOfWrapperNodesToDelete();

		// Remove the corresponding VM
		if (serverIdsToDelete != null && !CollectionUtils.isEmpty(serverIdsToDelete)) {
			serverIdsToDelete.forEach(serverId -> {
				this.osAdministration.deleteServer(serverId);
			});
		}
	}
}
