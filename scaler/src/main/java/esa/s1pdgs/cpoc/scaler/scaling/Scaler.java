package esa.s1pdgs.cpoc.scaler.scaling;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.k8s.K8sUnknownResourceException;
import esa.s1pdgs.cpoc.common.errors.k8s.PodResourceException;
import esa.s1pdgs.cpoc.common.errors.k8s.WrapperStopException;
import esa.s1pdgs.cpoc.common.errors.os.OsEntityException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.scaler.DevProperties;
import esa.s1pdgs.cpoc.scaler.k8s.K8SAdministration;
import esa.s1pdgs.cpoc.scaler.k8s.K8SMonitoring;
import esa.s1pdgs.cpoc.scaler.k8s.WrapperProperties;
import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;
import esa.s1pdgs.cpoc.scaler.k8s.model.WrapperNodeMonitor;
import esa.s1pdgs.cpoc.scaler.k8s.model.WrapperPodMonitor;
import esa.s1pdgs.cpoc.scaler.kafka.KafkaMonitoring;
import esa.s1pdgs.cpoc.scaler.kafka.model.KafkaPerGroupMonitor;
import esa.s1pdgs.cpoc.scaler.openstack.OpenStackAdministration;

/**
 * L1 resources scaler
 * 
 * @author Cyrielle Gailliard
 */
@Component
public class Scaler {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(Scaler.class);

    /**
     * Service for monitoring KAFKA
     */
    private final KafkaMonitoring kafkaMonitoring;

    private final K8SMonitoring k8SMonitoring;

    private final K8SAdministration k8SAdministration;

    private final OpenStackAdministration osAdministration;

    private final WrapperProperties wrapperProperties;

    private final DevProperties devProperties;

    private AtomicInteger uniqueVMID = new AtomicInteger(0);
    private AtomicInteger uniquePODID = new AtomicInteger(0);

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
    public Scaler(final KafkaMonitoring kafkaMonitoring,
            final K8SMonitoring k8SMonitoring,
            final K8SAdministration k8SAdministration,
            final OpenStackAdministration osAdministration,
            final WrapperProperties wrapperProperties,
            final DevProperties devProperties) {
        this.kafkaMonitoring = kafkaMonitoring;
        this.k8SMonitoring = k8SMonitoring;
        this.k8SAdministration = k8SAdministration;
        this.osAdministration = osAdministration;
        this.wrapperProperties = wrapperProperties;
        this.devProperties = devProperties;
    }

    @PostConstruct
    public void initscale() {
        try {
            // Listing all the L1 wrappers pods
            List<WrapperNodeMonitor> wrapperNodeMonitors = new ArrayList<>();
            int initpoolpod;
            int nbPodsPerServer = this.wrapperProperties.getNbPodsPerServer();
            if (devProperties.getActivations().get("init-scaling")) {
                LOGGER.info("[INIT] Starting monitoring K8S");
                wrapperNodeMonitors = this.k8SMonitoring.monitorL1Wrappers();
                LOGGER.debug("[INIT] Monitored information {}",
                        wrapperNodeMonitors);
                List<WrapperPodMonitor> activeWrapperPods = wrapperNodeMonitors
                        .stream()
                        .filter(nodeMonitor -> nodeMonitor != null
                                && !CollectionUtils
                                        .isEmpty(nodeMonitor.getWrapperPods()))
                        .flatMap(nodeMonitor -> nodeMonitor.getActivesPods()
                                .stream())
                        .collect(Collectors.toList());

                long numberWrappers = activeWrapperPods.stream().count();
                if (numberWrappers < this.wrapperProperties.getNbMinServers()
                        * nbPodsPerServer) {
                    initpoolpod =
                            (int) (this.wrapperProperties.getNbMinServers()
                                    - numberWrappers);
                    LOGGER.info("[INIT] Create {} missing pods", initpoolpod);
                    this.addRessources(wrapperNodeMonitors, initpoolpod);
                }
                if (numberWrappers > this.wrapperProperties.getNbMaxServers()
                        * nbPodsPerServer) {
                    initpoolpod = (int) (numberWrappers
                            - this.wrapperProperties.getNbMaxServers());
                    LOGGER.info("[INIT] Delete {} pods", initpoolpod);
                    this.freeRessources(wrapperNodeMonitors, initpoolpod);
                }
            } else {
                LOGGER.info("[INIT] Starting monitoring K8S bypassed");
            }

        } catch (AbstractCodedException e) {
            LOGGER.error("[INIT] [code {}] {}", e.getCode().getCode(),
                    e.getLogMessage());
        } catch (Exception e) {
            LOGGER.error("[INIT] [code {}] [msg {}]",
                    ErrorCode.INTERNAL_ERROR.getCode(), LogUtils.toString(e));
        }

    }

    @Scheduled(fixedRateString = "${wrapper.tempo-integrity-volumeserver-ms}")
    public void deleteinvalidressources() {
        LOGGER.debug("[INTEGRITY] Check for invalid server or volumes");
        try {
            this.osAdministration.deleteInvalidServers();
        } catch (OsEntityException e) {
            LOGGER.error("[INTEGRITY] [code {}] {}", e.getCode().getCode(),
                    e.getLogMessage());
        }
        try {
            this.osAdministration.deleteInvalidVolumes();
        } catch (OsEntityException e2) {
            LOGGER.error("[INTEGRITY] [code {}] {}", e2.getCode().getCode(),
                    e2.getLogMessage());
        }
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
    @Scheduled(fixedRateString = "${wrapper.tempo-pooling-ms}", initialDelayString = "${wrapper.tempo-initial-delay-ms}")
    public void scale() {
        LOGGER.info("[MONITOR] [step 0] Starting scaling");
        int step = 0;

        try {

            // Delete pod in succeeded state
            step++;
            if (devProperties.getActivations().get("pod-deletion")) {
                LOGGER.info(
                        "[MONITOR] [step 1] Deleting L1 wrapper pods in K8S succeeded phase");
                List<String> deletedPods = this.removeSucceededPods();
                LOGGER.debug("[MONITOR] [Step 1] Delete pods {}", deletedPods);
            } else {
                LOGGER.info(
                        "[MONITOR] [step 1] Deleting L1 wrapper pods in K8S succeeded phase bypassed");
            }

            // Monitor KAFKA
            step++;
            KafkaPerGroupMonitor monitorKafka = null;
            int nbPartitions = 0;
            int counter = 0;
            if (devProperties.getActivations().get("kafka-monitoring")) {
                LOGGER.info("[MONITOR] [step 2] Starting monitoring KAFKA");
                while (nbPartitions == 0 && counter < 2) {
                    monitorKafka = this.kafkaMonitoring.monitorL1Jobs();
                    LOGGER.debug("[MONITOR] [step 2] Monitored information {}",
                            monitorKafka);
                    if (monitorKafka.getNbPartitions() == 0) {
                        LOGGER.debug(
                                "[MONITOR] [step 2] Nb partitions null retry");
                        Thread.sleep(15000);
                    }
                    nbPartitions = monitorKafka.getNbPartitions();
                    counter++;
                }
            } else {
                LOGGER.info(
                        "[MONITOR] [step 2] Starting monitoring KAFKA bypassed");
            }
            if (nbPartitions == 0) {
                throw new InternalErrorException(
                        "Cannot retrieve Kafka monitors");
            }

            // Monitor K8S
            // Listing all the L1 wrappers pods
            step++;
            List<WrapperNodeMonitor> wrapperNodeMonitors = new ArrayList<>();
            if (devProperties.getActivations().get("k8s-monitoring")) {
                LOGGER.info("[MONITOR] [step 3] Starting monitoring K8S");
                wrapperNodeMonitors = this.k8SMonitoring.monitorL1Wrappers();
                wrapperNodeMonitors.forEach(monitor -> {
                    LOGGER.debug("[MONITOR] [step 3] Monitored information {}",
                            monitor);
                });

            } else {
                LOGGER.info(
                        "[MONITOR] [step 3] Starting monitoring K8S bypassed");
            }

            // Calculate value for scaling
            step++;
            double monitoredValue = -1;
            if (devProperties.getActivations().get("value-monitored")) {
                LOGGER.info(
                        "[MONITOR] [step 4] Starting determinating scaling action");
                monitoredValue = this.calculateMonitoredValue(monitorKafka,
                        wrapperNodeMonitors);
                LOGGER.debug("[MONITOR] [step 4] Monitored value {}",
                        monitoredValue);
            } else {
                LOGGER.info(
                        "[MONITOR] [step 4] Starting determinating scaling action bypassed");
            }

            // Scale
            step++;
            if (devProperties.getActivations().get("scaling")) {
                ScalingAction scalingAction = this.needScaling(monitoredValue);
                LOGGER.info(
                        "[MONITOR] [step 5] Starting applying scaling action {}",
                        scalingAction.name());
                switch (scalingAction) {
                    case ALLOC:
                        this.addRessources(wrapperNodeMonitors,
                                this.wrapperProperties.getNbPoolingPods());
                        break;
                    case FREE:
                        this.freeRessources(wrapperNodeMonitors,
                                this.wrapperProperties.getNbPoolingPods());
                        break;
                    default:
                        LOGGER.debug(
                                "[MONITOR] [step 5] Scaling bypassed this loop");
                        break;
                }
            } else {
                LOGGER.info(
                        "[MONITOR] [step 5] Starting applying scaling action bypassed");
            }

            // Delete unused resources
            step++;
            if (devProperties.getActivations()
                    .get("unused-ressources-deletion")) {
                LOGGER.info(
                        "[MONITOR] [step 6] Starting removing unused resources");
                this.deleteUnusedResources();
            } else {
                LOGGER.info(
                        "[MONITOR] [step 6] Starting removing unused resources bypassed");
            }
        } catch (AbstractCodedException e) {
            LOGGER.error("[MONITOR] [step {}] [code {}] {}", step,
                    e.getCode().getCode(), e.getLogMessage());
        } catch (Exception e) {
            LOGGER.error("[MONITOR] [step {}] [code {}] [msg {}]", step,
                    ErrorCode.INTERNAL_ERROR.getCode(), LogUtils.toString(e));
        }
        LOGGER.info("[MONITOR] [step 0] End");
    }

    protected List<String> removeSucceededPods()
            throws InternalErrorException, FileNotFoundException,
            PodResourceException, K8sUnknownResourceException {
        List<String> deletedPods =
                this.k8SAdministration.deleteTerminatedWrapperPods();
        if (!CollectionUtils.isEmpty(deletedPods)) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new InternalErrorException(
                        "Interrupted exception occurred", e);
            }
        } else {
            LOGGER.debug("No succeeded pod to delete");
        }
        return deletedPods;
    }

    protected double calculateMonitoredValue(
            KafkaPerGroupMonitor monitorKafka,
            List<WrapperNodeMonitor> wrapperNodeMonitors) {
        long totalLag = monitorKafka.getLagPerPartition().values().stream()
                .mapToLong(Long::longValue).sum();
        long averageExecutionTime =
                this.wrapperProperties.getExecutionTime().getAverageS();
        List<WrapperPodMonitor> activeWrapperPods = wrapperNodeMonitors.stream()
                .filter(nodeMonitor -> nodeMonitor != null && !CollectionUtils
                        .isEmpty(nodeMonitor.getWrapperPods()))
                .flatMap(nodeMonitor -> nodeMonitor.getActivesPods().stream())
                .collect(Collectors.toList());
        long totalRemainingTime = activeWrapperPods.stream()
                .mapToLong(wrapperPod -> wrapperPod.getRemainingExecutionTime())
                .sum();
        long numberWrappers = activeWrapperPods.stream().count();

        double monitoredValue = 0;
        if (numberWrappers > 0) {
            monitoredValue = ((totalLag * averageExecutionTime)
                    + (totalRemainingTime / 1000)) / numberWrappers;
        }
        LOGGER.debug(
                "[MONITOR] [Step 4] [totalLag {}] [averageExecutionTime {}] [totalRemainingTime {}] [numberWrappers {}] [monitoredValue {}]",
                totalLag, averageExecutionTime, totalRemainingTime / 1000,
                numberWrappers, monitoredValue);

        return monitoredValue;
    }

    protected ScalingAction needScaling(double monitoredValue) {
        if (monitoredValue < this.wrapperProperties.getExecutionTime()
                .getMinThresholdS()) {
            return ScalingAction.FREE;
        }
        if (monitoredValue > this.wrapperProperties.getExecutionTime()
                .getMaxThresholdS()) {
            return ScalingAction.ALLOC;
        }
        return ScalingAction.NOTHING;
    }

    protected void addRessources(List<WrapperNodeMonitor> wrapperNodeMonitors,
            int nbPoolingPods) throws AbstractCodedException {
        int nbPodsPerServer = this.wrapperProperties.getNbPodsPerServer();
        int maxNbServers = this.wrapperProperties.getNbMaxServers();
        int nbServers = wrapperNodeMonitors.size();
        float div = nbPoolingPods / nbPodsPerServer;
        int nbNeededServer = Math.round(div);

        // Check if one or several of our nodes can be reaffected
        LOGGER.info("[MONITOR] [step 5] 1 - Starting setting reusable nodes");
        List<WrapperNodeMonitor> reusableNodes = new ArrayList<>();
        for (WrapperNodeMonitor nodeMonitor : wrapperNodeMonitors) {
            String valueWrapperConfig = nodeMonitor.getDescription().getLabels()
                    .get(this.wrapperProperties.getLabelWrapperStateUnused()
                            .getLabel());
            if (valueWrapperConfig != null
                    && valueWrapperConfig.equals(this.wrapperProperties
                            .getLabelWrapperStateUnused().getValue())) {
                reusableNodes.add(nodeMonitor);
            }
        }
        int nbAllocatedServer = 0;
        int nbReusableNodes = reusableNodes.size();
        if (nbReusableNodes > 0) {
            int nbNodesToReused = Math.min(nbReusableNodes, nbNeededServer);
            for (int i = 0; i < nbNodesToReused; i++) {
                String nodeName =
                        reusableNodes.get(i).getDescription().getName();
                LOGGER.info(
                        "[MONITOR] [step 5] 1 - Starting setting reusable for node {}",
                        nodeName);
                this.k8SAdministration.setWrapperNodeUsable(nodeName);
                this.k8SAdministration.launchWrapperPodsPool(1, uniquePODID);
                nbAllocatedServer++;
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[MONITOR] [step 5] 1 - No unused nodes to reuse");
            }
        }

        // Create VM and POD
        LOGGER.info("[MONITOR] [step 5] 2 - Starting creating servers");
        int nbCreatedServer = 0;
        if (nbNeededServer > nbAllocatedServer) {
            if (nbServers >= maxNbServers) {
                LOGGER.warn(
                        "[MONITOR] [step 5] 2 - Maximal number of servers reached, cannot create another one");
            } else {
                while ((nbNeededServer > nbAllocatedServer + nbCreatedServer)
                        && (nbServers + nbCreatedServer < maxNbServers)) {
                    nbCreatedServer++;
                }
                ExecutorService createResoucesExecutorService =
                        Executors.newFixedThreadPool(nbCreatedServer);
                CompletionService<String> createResoucesCompletionServices =
                        new ExecutorCompletionService<>(
                                createResoucesExecutorService);
                for (int i = 0; i < nbCreatedServer; i++) {
                    createResoucesCompletionServices
                            .submit(new CreateResources(k8SAdministration,
                                    osAdministration, uniqueVMID, uniquePODID));
                }
                waitForServerCreationcompletion(
                        createResoucesCompletionServices, nbCreatedServer);
            }

        } else {
            LOGGER.debug(
                    "[MONITOR] [step 5] 2 - No need to create new servers");
        }
    }

    private void waitForServerCreationcompletion(
            CompletionService<String> completionService, int nbCreatedServer)
            throws AbstractCodedException {
        try {
            for (int i = 0; i < nbCreatedServer; i++) {
                String result = completionService.take().get();
                if (result != null) {
                    LOGGER.info(
                            "[MONITOR] [step 5] 3 - Volume, server and pod {} launched",
                            result);
                } else {
                    LOGGER.error(
                            "[MONITOR] [step 5] 3 - Volume, server and pod {} fail to create",
                            result);
                }
            }
        } catch (ExecutionException execE) {
            if (execE.getCause() instanceof AbstractCodedException) {
                throw (AbstractCodedException) execE.getCause();
            } else {
                throw new InternalErrorException(execE.getMessage(), execE);
            }
        } catch (InterruptedException intE) {
            throw new InternalErrorException(intE.getMessage(), intE);
        }
    }

    protected void freeRessources(List<WrapperNodeMonitor> wrapperNodeMonitors,
            int nbPoolingPods) throws WrapperStopException {
        int minNbServers = this.wrapperProperties.getNbMinServers();
        List<WrapperNodeMonitor> localWrapperNodeMonitors = wrapperNodeMonitors
                .stream()
                .filter(node -> hasLabels(node.getDescription().getLabels()))
                .collect(Collectors.toList());
        int nbServers = localWrapperNodeMonitors.size();
        int nbFreeServer = 0;
        int nbFreePods = 0;

        LOGGER.info("[MONITOR] [step 5] 1 - Starting freeing ressources");
        while ((nbServers - nbFreeServer > minNbServers)
                && (nbFreePods < nbPoolingPods)) {
            // We determine the VM to free
            WrapperNodeMonitor nodeToFree = localWrapperNodeMonitors.stream()
                    .collect(Collectors.minBy((n1, n2) -> Long.compare(
                            n1.getMaxRemainingExecTimeForActivesPods(),
                            n2.getMaxRemainingExecTimeForActivesPods())))
                    .get();
            List<WrapperPodMonitor> activePods = nodeToFree.getActivesPods();
            int nbVmActivesPods = activePods.size();
            LOGGER.info(
                    "[MONITOR] [step 5] 1 - Starting freeing ressources of node {} with {} active pods",
                    nodeToFree.getDescription().getName(), nbVmActivesPods);
            if (nbVmActivesPods + nbFreePods > nbPoolingPods) {
                // We stop nbPoolingPods pods by settings their status to
                // STOPPING
                this.k8SAdministration.stopWrapperPods(activePods.stream()
                        .filter(pod -> pod.getDescription() != null
                                && !CollectionUtils.isEmpty(
                                        pod.getDescription().getAddresses()))
                        .map(pod -> pod.getDescription().getAddresses()
                                .get(AddressType.INTERNAL_IP))
                        .collect(Collectors.toList())
                        .subList(0, nbPoolingPods - nbFreePods));
                nbFreePods = nbPoolingPods;
            } else {
                // We stop nbVmActivesPods pods by settings their status to
                // STOPPING
                this.k8SAdministration.stopWrapperPods(activePods.stream()
                        .filter(pod -> pod.getDescription() != null
                                && !CollectionUtils.isEmpty(
                                        pod.getDescription().getAddresses()))
                        .map(pod -> pod.getDescription().getAddresses()
                                .get(AddressType.INTERNAL_IP))
                        .collect(Collectors.toList()));
                nbFreePods += nbVmActivesPods;
                // We deactivate the server by setting its label wrapperstate to
                // unused
                this.k8SAdministration.setWrapperNodeUnusable(
                        nodeToFree.getDescription().getName());
                nbFreeServer++;
            }
            localWrapperNodeMonitors = localWrapperNodeMonitors.stream()
                    .filter(node -> !node.getDescription().getName()
                            .equals(nodeToFree.getDescription().getName()))
                    .collect(Collectors.toList());
        }

        if (nbFreePods < nbPoolingPods) {
            int t = nbPoolingPods - nbFreePods;
            int s = nbServers - nbFreeServer;
            LOGGER.warn(
                    "[MONITOR] [step 5] 1 - Cannot stop {} pods because minimal number of servers {} reached",
                    t, s);
        }
    }

    protected boolean hasLabels(Map<String, String> labels) {
        return labels.containsKey(
                this.wrapperProperties.getLabelWrapperStateUsed().getLabel())
                && labels.containsValue(this.wrapperProperties
                        .getLabelWrapperStateUsed().getValue());
    }

    protected void deleteUnusedResources()
            throws InternalErrorException, FileNotFoundException,
            PodResourceException, K8sUnknownResourceException {
        // Remove pods in succeeded state if necessary
        LOGGER.info(
                "[MONITOR] [step 6] 1 - Starting removing pods in succeeded phase");
        this.removeSucceededPods();

        // Retrieve K8S workers set in pause with no active pods
        LOGGER.info(
                "[MONITOR] [step 6] 2 - Starting retrieving nodes to delete");
        List<WrapperNodeMonitor> nodesToDelete =
                this.k8SMonitoring.monitorNodesToDelete();

        // Remove the corresponding VM
        if (!CollectionUtils.isEmpty(nodesToDelete)) {
            nodesToDelete.forEach(node -> {
                LOGGER.info(
                        "[MONITOR] [step 6] [serverId {}] 3 - Starting removing server",
                        node.getDescription().getExternalId());
                try {
                	LOGGER.info("### node name"+node.getDescription().getName());
                	LOGGER.info("### externalId"+node.getDescription().getExternalId());
                    this.osAdministration.deleteServer(                    		
                            node.getDescription().getExternalId());
                } catch (OsEntityException e) {
                    LOGGER.error("[MONITOR] [step 6] [code {}] {}",
                            e.getCode().getCode(), e.getLogMessage());
                }
            });
        }
    }
}
