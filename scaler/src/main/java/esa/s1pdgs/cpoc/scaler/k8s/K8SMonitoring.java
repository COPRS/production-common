package esa.s1pdgs.cpoc.scaler.k8s;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.k8s.WrapperStatusException;
import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;
import esa.s1pdgs.cpoc.scaler.k8s.model.NodeDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodLogicalStatus;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodStatus;
import esa.s1pdgs.cpoc.scaler.k8s.model.WrapperDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.WrapperNodeMonitor;
import esa.s1pdgs.cpoc.scaler.k8s.model.WrapperPodMonitor;
import esa.s1pdgs.cpoc.scaler.k8s.services.NodeService;
import esa.s1pdgs.cpoc.scaler.k8s.services.PodService;
import esa.s1pdgs.cpoc.scaler.k8s.services.WrapperService;
import esa.s1pdgs.cpoc.scaler.kafka.KafkaMonitoringProperties;
import esa.s1pdgs.cpoc.scaler.kafka.model.SpdgsTopic;

@Service
public class K8SMonitoring {
    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(K8SMonitoring.class);
    
    private final WrapperProperties wrapperProperties;

    private final NodeService nodeService;

    private final PodService podService;

    private final WrapperService wrapperService;

    private final GenericAppCatalogMqiService appCatalogService;

    /**
     * Kafka properties
     */
    private final KafkaMonitoringProperties kafkaProperties;

    @Autowired
    public K8SMonitoring(final WrapperProperties wrapperProperties,
            final NodeService nodeService, final PodService podService,
            final WrapperService wrapperService,
            @Qualifier("persistenceServiceForLevelJobs") final GenericAppCatalogMqiService appCatalogService,
            final KafkaMonitoringProperties kafkaProperties) {
        this.wrapperProperties = wrapperProperties;
        this.nodeService = nodeService;
        this.podService = podService;
        this.wrapperService = wrapperService;
        this.appCatalogService = appCatalogService;
        this.kafkaProperties = kafkaProperties;
    }

    public List<WrapperNodeMonitor> monitorNodesToDelete() {
        List<WrapperNodeMonitor> monitors = new ArrayList<>();

        // Get nodes unused
        Map<String, String> labels = new HashMap<>();
        labels.put(wrapperProperties.getLabelWrapperConfig().getLabel(),
                wrapperProperties.getLabelWrapperConfig().getValue());
        labels.put(wrapperProperties.getLabelWrapperStateUnused().getLabel(),
                wrapperProperties.getLabelWrapperStateUnused().getValue());
        List<NodeDesc> unusedNodes =
                this.nodeService.getNodesWithLabels(labels);

        if (!CollectionUtils.isEmpty(unusedNodes)) {
            // Get pods
            List<PodDesc> pods = this.podService.getPodsWithLabel(
                    wrapperProperties.getLabelWrapperApp().getLabel(),
                    wrapperProperties.getLabelWrapperApp().getValue());

            // Reorganize pods per nodes
            Map<String, List<PodDesc>> podsPerNodes = new HashMap<>();
            if (!CollectionUtils.isEmpty(pods)) {
                pods.forEach(pod -> {
                    if (!podsPerNodes.containsKey(pod.getNodeName())) {
                        podsPerNodes.put(pod.getNodeName(), new ArrayList<>());
                    }
                    podsPerNodes.get(pod.getNodeName()).add(pod);
                });
            }

            // Assign pod to nodes
            for (NodeDesc node : unusedNodes) {
                WrapperNodeMonitor nodeMonitor = new WrapperNodeMonitor(node);
                if (podsPerNodes.containsKey(node.getName())) {
                    for (PodDesc pod : podsPerNodes.get(node.getName())) {
                        WrapperPodMonitor podMonitor =
                                new WrapperPodMonitor(pod);
                        nodeMonitor.addWrapperPod(podMonitor);
                    }
                }
                monitors.add(nodeMonitor);
            }
        }

        return monitors.stream()
                .filter(monitor -> monitor
                        .getNbPodsPerK8SStatus(PodStatus.Running) == 0)
                .collect(Collectors.toList());
    }

    public List<WrapperNodeMonitor> monitorL1Wrappers()
            throws WrapperStatusException, AbstractCodedException {
        List<WrapperNodeMonitor> monitors = new ArrayList<>();

        // Retrieve nodes dedicated to L1
        LOGGER.debug("Retrieve nodes dedicated to L1");
        List<NodeDesc> nodes = this.nodeService.getNodesWithLabel(
                wrapperProperties.getLabelWrapperConfig().getLabel(),
                wrapperProperties.getLabelWrapperConfig().getValue());

        // Retrieve pods dedicated to L1
        LOGGER.debug("Retrieve pods dedicated to L1");
        List<PodDesc> pods = this.podService.getPodsWithLabel(
                wrapperProperties.getLabelWrapperApp().getLabel(),
                wrapperProperties.getLabelWrapperApp().getValue());

        // Reorganize pods per nodes
        LOGGER.debug("Reorganize pods per nodes");
        Map<String, List<PodDesc>> podsPerNodes = new HashMap<>();
        if (!CollectionUtils.isEmpty(pods)) {
            pods.forEach(pod -> {
                if (!podsPerNodes.containsKey(pod.getNodeName())) {
                    podsPerNodes.put(pod.getNodeName(), new ArrayList<>());
                }
                podsPerNodes.get(pod.getNodeName()).add(pod);
            });
        }

        // build monitor
        LOGGER.debug("Build monitor");
        if (!CollectionUtils.isEmpty(nodes)) {
            for (NodeDesc node : nodes) {
                WrapperNodeMonitor nodeMonitor = new WrapperNodeMonitor(node);
                if (podsPerNodes.containsKey(node.getName())) {
                    for (PodDesc pod : podsPerNodes.get(node.getName())) {
                        WrapperPodMonitor podMonitor =
                                new WrapperPodMonitor(pod);
                        if (pod.getStatus() == PodStatus.Running) {
                            WrapperDesc wrapper = this.wrapperService
                                    .getWrapperStatus(pod.getName(),
                                            pod.getAddresses().get(
                                                    AddressType.INTERNAL_IP));
                            long nbReadingMessage = getNbReadingMessage(pod.getName());
                            podMonitor.setLogicalStatus(wrapper.getStatus());
                            if (wrapper.getStatus()
                                    .equals(PodLogicalStatus.PROCESSING)) {
                                podMonitor.setPassedExecutionTime(
                                        wrapper.getTimeSinceLastChange());
                                podMonitor.setRemainingExecutionTime(
                                        wrapperProperties.getExecutionTime()
                                                .getAverageS() * 1000
                                                - wrapper
                                                        .getTimeSinceLastChange());
                            }
                            long remTime = podMonitor
                                    .getRemainingExecutionTime()
                                    + nbReadingMessage * wrapperProperties
                                            .getExecutionTime().getAverageS()
                                            * 1000;
                            podMonitor.setRemainingExecutionTime(remTime);
                        }
                        nodeMonitor.addWrapperPod(podMonitor);
                    }
                }
                monitors.add(nodeMonitor);
            }
        }

        return monitors;
    }
    
    private long getNbReadingMessage(String podName) throws AbstractCodedException {
        long ret = 0;
        for (String topicName : kafkaProperties.getTopics().get(SpdgsTopic.L1_JOBS)) {
            ret += this.appCatalogService.getNbReadingMessages(topicName, podName);
        }
        return ret;
    }
}
