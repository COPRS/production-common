package esa.s1pdgs.cpoc.scaler.k8s;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiService;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.k8s.WrapperStatusException;
import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;
import esa.s1pdgs.cpoc.scaler.k8s.model.NodeDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodLogicalStatus;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodStatus;
import esa.s1pdgs.cpoc.scaler.k8s.model.VolumeDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.WrapperDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.WrapperNodeMonitor;
import esa.s1pdgs.cpoc.scaler.k8s.model.WrapperPodMonitor;
import esa.s1pdgs.cpoc.scaler.k8s.services.NodeService;
import esa.s1pdgs.cpoc.scaler.k8s.services.PodService;
import esa.s1pdgs.cpoc.scaler.k8s.services.WrapperService;
import esa.s1pdgs.cpoc.scaler.kafka.KafkaMonitoringProperties;
import esa.s1pdgs.cpoc.scaler.kafka.model.SpdgsTopic;

public class K8SMonitoringTest {

    /**
     * Kafka properties
     */
    @Mock
    private KafkaMonitoringProperties kafkaProperties;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WrapperProperties wrapperProperties;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NodeService nodeService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PodService podService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WrapperService wrapperService;

    @Mock
    private AppCatalogMqiService appCatalogService;

    @InjectMocks
    private K8SMonitoring k8SMonitoring;

    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);
        when(wrapperProperties.getLabelWrapperConfig().getLabel())
                .thenReturn("wrapperconfig");
        when(wrapperProperties.getLabelWrapperConfig().getValue())
                .thenReturn("l1");
        when(wrapperProperties.getLabelWrapperStateUnused().getLabel())
                .thenReturn("wrapperstate");
        when(wrapperProperties.getLabelWrapperStateUnused().getValue())
                .thenReturn("unused");
        when(wrapperProperties.getLabelWrapperApp().getLabel())
                .thenReturn("app");
        when(wrapperProperties.getLabelWrapperApp().getValue())
                .thenReturn("l1-wrapper");
        Map<SpdgsTopic, List<String>> topics = new HashMap<>();
        topics.put(SpdgsTopic.L1_JOBS, Arrays.asList("topic"));
        Map<SpdgsTopic, String> groups = new HashMap<>();
        groups.put(SpdgsTopic.L1_JOBS, "group");
        doReturn(topics).when(kafkaProperties).getTopics();
        doReturn(groups).when(kafkaProperties).getGroupIdPerTopic();

        doReturn(0).when(appCatalogService)
                .getNbReadingMessages(Mockito.anyString(), Mockito.anyString());

    }

    private void mockMonitorNodesToDelete() {
        NodeDesc nodeDesc = new NodeDesc("nodeName");
        nodeDesc.setExternalId("externalId");
        Map<AddressType, String> addresses = new HashMap<AddressType, String>();
        addresses.put(AddressType.fromLabel("InternalIP"), "internalIP");
        addresses.put(AddressType.fromLabel("ExternalIP"), "externalIP");
        nodeDesc.addAddresses(addresses);
        nodeDesc.addAddress(AddressType.fromLabel("Hostname"), "hostname");
        Map<String, String> labels = new HashMap<String, String>();
        labels.put("wrapperconfig", "l1");
        labels.put("wrapperstate", "unused");
        nodeDesc.addLabels(labels);
        nodeDesc.addVolume(new VolumeDesc("VolumeName1"));
        List<NodeDesc> unusedNodes = new ArrayList<>();
        unusedNodes.add(nodeDesc);
        when(nodeService.getNodesWithLabels(Mockito.any()))
                .thenReturn(unusedNodes);

        PodDesc podDesc = new PodDesc("name");
        podDesc.setNodeName("nodeName");
        podDesc.setNodeIpAddress("nodeIpAddress");
        podDesc.setStatus(PodStatus.Succeeded);
        addresses = new HashMap<AddressType, String>();
        addresses.put(AddressType.fromLabel("InternalIP"), "internalIP");
        addresses.put(AddressType.fromLabel("ExternalIP"), "externalIP");
        podDesc.addAddresses(addresses);
        podDesc.addAddress(AddressType.fromLabel("Hostname"), "hostname");
        podDesc.addLabels("app", "l1-wrapper");
        List<PodDesc> pods = new ArrayList<>();
        pods.add(podDesc);
        when(podService.getPodsWithLabel(Mockito.any(), Mockito.any()))
                .thenReturn(pods);
    }

    private void mockMonitorL1Wrappers() throws WrapperStatusException {
        WrapperDesc wrapperDesc = new WrapperDesc("wrapperName");
        wrapperDesc.setTimeSinceLastChange(15000);
        wrapperDesc.setErrorCounter(0);
        wrapperDesc.setStatus(PodLogicalStatus.PROCESSING);
        when(wrapperService.getWrapperStatus(Mockito.anyString(),
                Mockito.anyString())).thenReturn(wrapperDesc);

        NodeDesc nodeDesc = new NodeDesc("nodeName");
        nodeDesc.setExternalId("externalId");
        Map<AddressType, String> addresses = new HashMap<AddressType, String>();
        addresses.put(AddressType.fromLabel("InternalIP"), "internalIP");
        addresses.put(AddressType.fromLabel("ExternalIP"), "externalIP");
        nodeDesc.addAddresses(addresses);
        nodeDesc.addAddress(AddressType.fromLabel("Hostname"), "hostname");
        Map<String, String> labels = new HashMap<String, String>();
        labels.put("wrapperconfig", "l1");
        labels.put("wrapperstate", "unused");
        nodeDesc.addLabels(labels);
        nodeDesc.addVolume(new VolumeDesc("VolumeName1"));
        List<NodeDesc> unusedNodes = new ArrayList<>();
        unusedNodes.add(nodeDesc);
        when(nodeService.getNodesWithLabel(Mockito.anyString(),
                Mockito.anyString())).thenReturn(unusedNodes);

        PodDesc podDesc = new PodDesc("name");
        podDesc.setNodeName("nodeName");
        podDesc.setNodeIpAddress("nodeIpAddress");
        podDesc.setStatus(PodStatus.Running);
        addresses = new HashMap<AddressType, String>();
        addresses.put(AddressType.fromLabel("InternalIP"), "internalIP");
        addresses.put(AddressType.fromLabel("ExternalIP"), "externalIP");
        podDesc.addAddresses(addresses);
        podDesc.addAddress(AddressType.fromLabel("Hostname"), "hostname");
        podDesc.addLabels("app", "l1-wrapper");
        List<PodDesc> pods = new ArrayList<>();
        pods.add(podDesc);
        when(podService.getPodsWithLabel(Mockito.any(), Mockito.any()))
                .thenReturn(pods);
    }

    @Test
    public void testMonitorNodesToDelete() {
        NodeDesc nodeDesc = new NodeDesc("nodeName");
        nodeDesc.setExternalId("externalId");
        Map<AddressType, String> addresses = new HashMap<AddressType, String>();
        addresses.put(AddressType.fromLabel("InternalIP"), "internalIP");
        addresses.put(AddressType.fromLabel("ExternalIP"), "externalIP");
        nodeDesc.addAddresses(addresses);
        nodeDesc.addAddress(AddressType.fromLabel("Hostname"), "hostname");
        Map<String, String> labels = new HashMap<String, String>();
        labels.put("wrapperconfig", "l1");
        labels.put("wrapperstate", "unused");
        nodeDesc.addLabels(labels);
        nodeDesc.addVolume(new VolumeDesc("VolumeName1"));
        WrapperNodeMonitor wrapperNodeMonitor =
                new WrapperNodeMonitor(nodeDesc);
        PodDesc podDesc = new PodDesc("name");
        podDesc.setNodeName("nodeName");
        podDesc.setNodeIpAddress("nodeIpAddress");
        podDesc.setStatus(PodStatus.Succeeded);
        addresses = new HashMap<AddressType, String>();
        addresses.put(AddressType.fromLabel("InternalIP"), "internalIP");
        addresses.put(AddressType.fromLabel("ExternalIP"), "externalIP");
        podDesc.addAddresses(addresses);
        podDesc.addAddress(AddressType.fromLabel("Hostname"), "hostname");
        podDesc.addLabels("app", "l1-wrapper");
        WrapperPodMonitor podMonitor = new WrapperPodMonitor(podDesc);
        wrapperNodeMonitor.addWrapperPod(podMonitor);
        List<WrapperNodeMonitor> expectedResult = new ArrayList<>();
        expectedResult.add(wrapperNodeMonitor);

        this.mockMonitorNodesToDelete();

        List<WrapperNodeMonitor> result = k8SMonitoring.monitorNodesToDelete();
        assertEquals("Result is different from expected result", expectedResult,
                result);
    }

    @Test
    public void testMonitorL1Wrappers() throws WrapperStatusException,
            esa.s1pdgs.cpoc.common.errors.AbstractCodedException {
        NodeDesc nodeDesc = new NodeDesc("nodeName");
        nodeDesc.setExternalId("externalId");
        Map<AddressType, String> addresses = new HashMap<AddressType, String>();
        addresses.put(AddressType.fromLabel("InternalIP"), "internalIP");
        addresses.put(AddressType.fromLabel("ExternalIP"), "externalIP");
        nodeDesc.addAddresses(addresses);
        nodeDesc.addAddress(AddressType.fromLabel("Hostname"), "hostname");
        Map<String, String> labels = new HashMap<String, String>();
        labels.put("wrapperconfig", "l1");
        labels.put("wrapperstate", "unused");
        nodeDesc.addLabels(labels);
        nodeDesc.addVolume(new VolumeDesc("VolumeName1"));
        WrapperNodeMonitor wrapperNodeMonitor =
                new WrapperNodeMonitor(nodeDesc);
        PodDesc podDesc = new PodDesc("name");
        podDesc.setNodeName("nodeName");
        podDesc.setNodeIpAddress("nodeIpAddress");
        podDesc.setStatus(PodStatus.Running);
        addresses = new HashMap<AddressType, String>();
        addresses.put(AddressType.fromLabel("InternalIP"), "internalIP");
        addresses.put(AddressType.fromLabel("ExternalIP"), "externalIP");
        podDesc.addAddresses(addresses);
        podDesc.addAddress(AddressType.fromLabel("Hostname"), "hostname");
        podDesc.addLabels("app", "l1-wrapper");
        WrapperPodMonitor podMonitor = new WrapperPodMonitor(podDesc);
        podMonitor.setLogicalStatus(PodLogicalStatus.PROCESSING);
        podMonitor.setPassedExecutionTime(15000);
        podMonitor.setRemainingExecutionTime(-15000);
        wrapperNodeMonitor.addWrapperPod(podMonitor);
        List<WrapperNodeMonitor> expectedResult = new ArrayList<>();
        expectedResult.add(wrapperNodeMonitor);

        this.mockMonitorL1Wrappers();
        List<WrapperNodeMonitor> result = k8SMonitoring.monitorL1Wrappers();
        assertEquals("Result is different from expected result", expectedResult,
                result);
    }
}
