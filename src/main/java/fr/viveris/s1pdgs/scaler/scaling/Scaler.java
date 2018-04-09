package fr.viveris.s1pdgs.scaler.scaling;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.scaler.k8s.K8SAdministration;
import fr.viveris.s1pdgs.scaler.k8s.K8SMonitoring;
import fr.viveris.s1pdgs.scaler.k8s.WrapperProperties;
import fr.viveris.s1pdgs.scaler.k8s.model.PodLogicalStatus;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperNodeMonitor;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperPodMonitor;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.PodResourceException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.UnknownKindExecption;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.UnknownVolumeNameException;
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

	private final K8SMonitoring k8SMonitoring;

	private final K8SAdministration k8SAdministration;

	private final OpenStackAdministration osAdministration;

	private final WrapperProperties wrapperProperties;

	private long lastScalingTimestamp = 0;
	private long lastDeletingResourcesTimestamp = 0;

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
	public Scaler(final KafkaMonitoring kafkaMonitoring, final K8SMonitoring k8SMonitoring,
			final K8SAdministration k8SAdministration, final OpenStackAdministration osAdministration,
			final WrapperProperties wrapperProperties) {
		this.kafkaMonitoring = kafkaMonitoring;
		this.k8SMonitoring = k8SMonitoring;
		this.k8SAdministration = k8SAdministration;
		this.osAdministration = osAdministration;
		this.wrapperProperties = wrapperProperties;
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
	@Scheduled(fixedDelayString = "${wrapper.scaler.fixed-delay-ms}")
	public void scale() {
		LOGGER.info("[MONITOR] [Step 0] Starting scaling");

		try {

			long currentTimestamp = System.currentTimeMillis();

			// Monitor KAFKA
			LOGGER.info("[MONITOR] [Step 1] Starting monitoring KAFKA");
			KafkaPerGroupPerTopicMonitor monitorKafka = this.kafkaMonitoring.monitorL1Jobs();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[MONITOR] [Step 1] Monitored information {}", monitorKafka);
			}

			// Monitor K8S
			// Listing all the L1 wrappers pods
			LOGGER.info("[MONITOR] [Step 2] Starting monitoring K8S");
			List<WrapperNodeMonitor> wrapperNodeMonitors = this.k8SMonitoring.monitorL1Wrappers();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[MONITOR] [Step 2] Monitored information {}", wrapperNodeMonitors);
			}

			// Calculate value for scaling
			LOGGER.info("[MONITOR] [Step 3] Starting determinating scaling action");
			double monitoredValue = this.calculateMonitoredValue(monitorKafka, wrapperNodeMonitors);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[MONITOR] [Step 3] Monitored value {}", monitoredValue);
			}
			ScalingAction scalingAction = this.needScaling(monitoredValue, this.lastScalingTimestamp, currentTimestamp);

			// Scale
			LOGGER.info("[MONITOR] [Step 4] Starting applying scaling action {}", scalingAction.name());
			switch (scalingAction) {
			case ALLOC:
				this.addRessources(wrapperNodeMonitors);
				break;
			case FREE:
				this.freeRessources(wrapperNodeMonitors);
				break;
			case NOTHING:
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[MONITOR] [Step 4] Scaling bypassed this loop");
				}
				break;
			default:
				LOGGER.error("");
				break;
			}

			// Delete unused resources
			LOGGER.info("[MONITOR] [Step 5] Starting removing unused resources");
			if (!this.deleteUnusedResources(this.lastDeletingResourcesTimestamp, currentTimestamp)) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[MONITOR] [Step 5] Removing unused resources bypassed this loop");
				}
			}

		} catch (Exception e) {
			LOGGER.error("Error during scaling: {}", e);
		}
		LOGGER.info("[MONITOR] [Step 0] End");
	}

	private double calculateMonitoredValue(KafkaPerGroupPerTopicMonitor monitorKafka,
			List<WrapperNodeMonitor> wrapperNodeMonitors) {
		long totalLag = monitorKafka.getLagPerPartition().values().stream().mapToLong(Long::longValue).sum();
		long averageExecutionTime = this.wrapperProperties.getExecutionTime().getAverageS();
		Stream<WrapperPodMonitor> activeWrapperPods = wrapperNodeMonitors.stream()
				.filter(nodeMonitor -> nodeMonitor != null && !CollectionUtils.isEmpty(nodeMonitor.getWrapperPods()))
				.flatMap(nodeMonitor -> nodeMonitor.getWrapperPods().stream())
				.filter(wrapperPod -> wrapperPod.getLogicalStatus() == PodLogicalStatus.WAITING
						|| wrapperPod.getLogicalStatus() == PodLogicalStatus.PROCESSING);
		long totalRemainingTime = activeWrapperPods.mapToLong(wrapperPod -> wrapperPod.getRemainingExecutionTime())
				.sum();
		long numberWrappers = activeWrapperPods.count();

		double monitoredValue = ((totalLag * averageExecutionTime) + totalRemainingTime) / numberWrappers;

		return monitoredValue;
	}

	private ScalingAction needScaling(double monitoredValue, long lastScalingTimestamp, long currentTimestamp) {
		long tempoScaling = wrapperProperties.getTempoScalingS();
		// Check if period is OK
		boolean periodOk = true;
		if (tempoScaling != 0) {
			if (lastScalingTimestamp + tempoScaling > currentTimestamp) {
				periodOk = false;
			}
		}
		if (periodOk) {
			if (monitoredValue < this.wrapperProperties.getExecutionTime().getMinThresholdS()) {
				return ScalingAction.FREE;
			}
			if (monitoredValue > this.wrapperProperties.getExecutionTime().getMaxThresholdS()) {
				return ScalingAction.ALLOC;
			}
		}
		return ScalingAction.NOTHING;
	}

	private void addRessources(List<WrapperNodeMonitor> wrapperNodeMonitors) throws FileNotFoundException, PodResourceException, UnknownKindExecption, UnknownVolumeNameException {
		int nbPoolingPods = this.wrapperProperties.getNbPoolingPods();
		int nbPodsPerServer = this.wrapperProperties.getNbPodsPerServer();
		int maxNbServers = this.wrapperProperties.getNbMaxServers();
		int nbServers = wrapperNodeMonitors.size();
		float div = nbPoolingPods / nbPodsPerServer;
		int nbNeededServer = Math.round(div);
		int nbAllocatedServer = 0;

		// Check if one or several of our nodes can be reaffected
		LOGGER.info("[MONITOR] [Step 4] 1 - Starting setting reusable nodes");
		List<WrapperNodeMonitor> reusableNodes = new ArrayList<>();
		for (WrapperNodeMonitor nodeMonitor : wrapperNodeMonitors) {
			String valueWrapperConfig = nodeMonitor.getDescription().getLabels()
					.get(this.wrapperProperties.getLabelWrapperStateUnused().getLabel());
			if (valueWrapperConfig != null
					&& valueWrapperConfig.equals(this.wrapperProperties.getLabelWrapperStateUnused().getValue())) {
				reusableNodes.add(nodeMonitor);
			}
		}
		int nbReusableNodes = reusableNodes.size();
		if (nbReusableNodes > 0) {
			int nbNodesToReused = Math.min(nbReusableNodes, nbNeededServer);
			for (int i = 0; i < nbNodesToReused; i++) {
				String nodeName = reusableNodes.get(i).getDescription().getName();
				LOGGER.info("[MONITOR] [Step 4] 1 - Starting setting reusable for node {}", nodeName);
				this.k8SAdministration.setWrapperNodeUsable(nodeName);
				nbAllocatedServer++;
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[MONITOR] [Step 4] 1 - No unused nodes to reuse");
			}
		}

		// Create VM
		LOGGER.info("[MONITOR] [Step 4] 2 - Starting creating servers");
		if (nbNeededServer > nbAllocatedServer) {
			if (nbServers >= maxNbServers) {
				LOGGER.warn("[MONITOR] [Step 4] 2 - Maximal number of servers reached, cannot create another one");
			} else {
				int nbCreatedServer = 0;
				while ((nbNeededServer > nbAllocatedServer + nbCreatedServer)
						&& (nbServers + nbCreatedServer < maxNbServers)) {
					this.osAdministration.createServerForL1Wrappers("[MONITOR] [Step 4] 2 - ");
					nbCreatedServer++;
				}
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[MONITOR] [Step 4] 2 - No need to create new servers");
			}
		}

		// Launchs pods
		LOGGER.info("[MONITOR] [Step 4] 3 - Starting launching pods");
		this.k8SAdministration.launchWrapperPodsPool();
		LOGGER.info("[MONITOR] [Step 4] 3 - All pods launched");
	}

	private void freeRessources(List<WrapperNodeMonitor> wrapperNodeMonitors) {

	}

	private boolean deleteUnusedResources(long lastDeletingResourcesTimestamp, long currentTimestamp) {
		long tempoDeletingResources = wrapperProperties.getTempoDeleteResourcesS();
		// Check if period is OK
		boolean periodOk = true;
		if (tempoDeletingResources != 0) {
			if (lastDeletingResourcesTimestamp + tempoDeletingResources > currentTimestamp) {
				periodOk = false;
			}
		}

		if (periodOk) {
			// Retrieve K8S workers set in pause with no active pods
			List<String> serverIdsToDelete = this.k8SAdministration.getExternalIdsOfWrapperNodesToDelete();

			// Remove the corresponding VM
			if (serverIdsToDelete != null && !CollectionUtils.isEmpty(serverIdsToDelete)) {
				serverIdsToDelete.forEach(serverId -> {
					LOGGER.info("[MONITOR] [Step 5] [serverId {}] Starting removing server", serverId);
					this.osAdministration.deleteServer(serverId);
				});
			}

			// Set last timestamp
			this.lastDeletingResourcesTimestamp = currentTimestamp;

			return true;
		}

		return false;
	}
}
