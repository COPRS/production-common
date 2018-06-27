package fr.viveris.s1pdgs.scaler.scaling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.scaler.DevProperties;
import fr.viveris.s1pdgs.scaler.InternalErrorException;
import fr.viveris.s1pdgs.scaler.k8s.K8SAdministration;
import fr.viveris.s1pdgs.scaler.k8s.K8SMonitoring;
import fr.viveris.s1pdgs.scaler.k8s.WrapperProperties;
import fr.viveris.s1pdgs.scaler.k8s.WrapperProperties.LabelKubernetes;
import fr.viveris.s1pdgs.scaler.k8s.WrapperProperties.TimeProperties;
import fr.viveris.s1pdgs.scaler.k8s.model.NodeDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.PodLogicalStatus;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperNodeMonitor;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperPodMonitor;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.K8sUnknownResourceException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.PodResourceException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperStatusException;
import fr.viveris.s1pdgs.scaler.kafka.KafkaMonitoring;
import fr.viveris.s1pdgs.scaler.kafka.model.KafkaPerGroupPerTopicMonitor;
import fr.viveris.s1pdgs.scaler.openstack.OpenStackAdministration;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsEntityException;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsEntityInternaloErrorException;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsServerNotDeletedException;
import fr.viveris.s1pdgs.scaler.scaling.Scaler.ScalingAction;

public class ScalerTest {

    public static String LABEL_WRAPPER_L1 = "l1";
    public static String LABEL_WRAPPER_USED = "used";
    public static String LABEL_WRAPPER_NOTUSED = "unused";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private KafkaMonitoring kafkaMonitoring;

    @Mock
    private K8SMonitoring k8SMonitoring;

    @Mock
    private K8SAdministration k8SAdministration;

    @Mock
    private OpenStackAdministration osAdministration;

    @Mock
    private WrapperProperties wrapperProperties;

    @Mock
    private DevProperties devProperties;

    private Scaler scaler;
    private KafkaPerGroupPerTopicMonitor kafkaMonitor;
    private List<WrapperNodeMonitor> wrappersMonitor;

    @Before
    public void init() throws PodResourceException, K8sUnknownResourceException,
            WrapperStatusException, OsEntityException {
        MockitoAnnotations.initMocks(this);

        scaler = new Scaler(kafkaMonitoring, k8SMonitoring, k8SAdministration,
                osAdministration, wrapperProperties, devProperties);

        mockDevProperties();
        mockWrapperPropertiesForAlloc();

        // Mock
        mockStep1(false);
        mockStep2();
        mockStep3();
        mockStep6();

        //

    }

    private void mockDevProperties() {
        mockDevProperties(true);
    }

    private void mockDevProperties(boolean act) {
        Map<String, Boolean> activations = new HashMap<>();
        activations.put("pod-deletion", act);
        activations.put("kafka-monitoring", act);
        activations.put("init-scaling", act);
        activations.put("k8s-monitoring", act);
        activations.put("value-monitored", act);
        activations.put("scaling", act);
        activations.put("unused-ressources-deletion", act);
        doReturn(activations).when(devProperties).getActivations();
    }

    private void mockStep1(boolean simulate)
            throws PodResourceException, K8sUnknownResourceException {
        if (simulate) {
            doReturn(Arrays.asList("server1", "server2"))
                    .when(k8SAdministration).deleteTerminatedWrapperPods();
        } else {
            doReturn(new ArrayList<>()).when(k8SAdministration)
                    .deleteTerminatedWrapperPods();
        }
    }

    private void mockStep2() {
        kafkaMonitor =
                new KafkaPerGroupPerTopicMonitor(new Date(), "group", "topic");
        kafkaMonitor.setNbConsumers(2);
        kafkaMonitor.setNbPartitions(3);
        kafkaMonitor.getLagPerConsumers().put("consumer1", Long.valueOf(3));
        kafkaMonitor.getLagPerConsumers().put("consumer2", Long.valueOf(5));
        kafkaMonitor.getLagPerPartition().put(Integer.valueOf(0),
                Long.valueOf(2));
        kafkaMonitor.getLagPerPartition().put(Integer.valueOf(1),
                Long.valueOf(5));
        kafkaMonitor.getLagPerPartition().put(Integer.valueOf(2),
                Long.valueOf(1));

        doReturn(kafkaMonitor).when(kafkaMonitoring).monitorL1Jobs();
    }

    private void mockStep3() throws WrapperStatusException {
        wrappersMonitor = new ArrayList<>();
        WrapperNodeMonitor wrapperNodeMonitor = buildWrapperNodeMonitor("node1",
                "nodeId1", LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
        wrapperNodeMonitor.addWrapperPod(buildWrapperPodMonitor("pod1", "node1",
                PodLogicalStatus.PROCESSING, 15000000, 1000000));
        wrappersMonitor.add(wrapperNodeMonitor);
        WrapperNodeMonitor wrapperNodeMonitor2 = buildWrapperNodeMonitor(
                "node4", "nodeId4", LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
        wrapperNodeMonitor2.addWrapperPod(buildWrapperPodMonitor("pod4",
                "node4", PodLogicalStatus.WAITING, 15000000, 0));
        wrappersMonitor.add(wrapperNodeMonitor2);

        doReturn(wrappersMonitor).when(k8SMonitoring).monitorL1Wrappers();
    }

    private WrapperNodeMonitor buildWrapperNodeMonitor(String nodeName,
            String externalId, String wrapperconfig, String wrapperstate) {
        NodeDesc nodeDesc = new NodeDesc(nodeName);
        nodeDesc.setExternalId(externalId);
        Map<String, String> labels = new HashMap<String, String>();
        if (wrapperconfig != null) {
            labels.put("wrapperconfig", wrapperconfig);
        }
        if (wrapperconfig != null) {
            labels.put("wrapperstate", wrapperstate);
        }
        nodeDesc.addLabels(labels);
        return new WrapperNodeMonitor(nodeDesc);
    }

    private WrapperPodMonitor buildWrapperPodMonitor(String podName,
            String nodeName, PodLogicalStatus status, long execPAssTime,
            long RemainExecTime) {
        PodDesc podDesc = new PodDesc(podName);
        podDesc.setNodeName(nodeName);
        WrapperPodMonitor podMonitor = new WrapperPodMonitor(podDesc);
        podMonitor.setLogicalStatus(status);
        podMonitor.setPassedExecutionTime(execPAssTime);
        podMonitor.setRemainingExecutionTime(RemainExecTime);
        return podMonitor;
    }

    private void mockStep6() throws OsEntityException {
        List<WrapperNodeMonitor> wrappers = new ArrayList<>();
        wrappers.add(buildWrapperNodeMonitor("node1", "nodeId1",
                LABEL_WRAPPER_L1, LABEL_WRAPPER_USED));
        wrappers.add(buildWrapperNodeMonitor("node2", "nodeId2",
                LABEL_WRAPPER_L1, LABEL_WRAPPER_USED));

        doReturn(wrappers).when(k8SMonitoring).monitorNodesToDelete();
        doNothing().when(osAdministration).deleteServer(Mockito.anyString());
    }

    private void mockWrapperPropertiesForAlloc() {
        mockWrapperProperties(900, 800, 1400);
    }

    /*
     * private void mockWrapperPropertiesForFree() { mockWrapperProperties(900,
     * 4500, 7000); }
     */

    private void mockWrapperPropertiesForNothing() {
        mockWrapperProperties(900, 800, 6000);
    }

    private void mockWrapperProperties(long average, long min, long max) {
        TimeProperties timeP = new WrapperProperties.TimeProperties();
        timeP.setAverageS(average);
        timeP.setMinThresholdS(min);
        timeP.setMaxThresholdS(max);
        doReturn(timeP).when(wrapperProperties).getExecutionTime();
        LabelKubernetes labelUsed = new WrapperProperties.LabelKubernetes(
                "wrapperstate", LABEL_WRAPPER_USED);
        doReturn(labelUsed).when(wrapperProperties).getLabelWrapperStateUsed();
        LabelKubernetes labelUnUsed = new WrapperProperties.LabelKubernetes(
                "wrapperstate", LABEL_WRAPPER_NOTUSED);
        doReturn(labelUnUsed).when(wrapperProperties)
                .getLabelWrapperStateUnused();
    }

    @Test
    public void testdeleteinvalidressources() throws OsEntityException {
        doNothing().when(osAdministration).deleteInvalidServers();
        doNothing().when(osAdministration).deleteInvalidVolumes();

        scaler.deleteinvalidressources();
        verify(osAdministration, times(1)).deleteInvalidServers();
        verify(osAdministration, times(1)).deleteInvalidVolumes();
    }

    @Test
    public void testdeleteinvalidressourcesErrorServer()
            throws OsEntityException {
        doThrow(new OsEntityInternaloErrorException("server", "id",
                "erroe message", new IllegalArgumentException()))
                        .when(osAdministration).deleteInvalidServers();
        doNothing().when(osAdministration).deleteInvalidVolumes();

        scaler.deleteinvalidressources();
        verify(osAdministration, times(1)).deleteInvalidServers();
        verify(osAdministration, times(1)).deleteInvalidVolumes();
    }

    @Test
    public void testdeleteinvalidressourcesErrorVolume()
            throws OsEntityException {
        doNothing().when(osAdministration).deleteInvalidServers();
        doThrow(new OsEntityInternaloErrorException("server", "id",
                "erroe message", new IllegalArgumentException()))
                        .when(osAdministration).deleteInvalidVolumes();

        scaler.deleteinvalidressources();
        verify(osAdministration, times(1)).deleteInvalidServers();
        verify(osAdministration, times(1)).deleteInvalidVolumes();
    }

    @Test
    public void nominalNothing() throws PodResourceException,
            K8sUnknownResourceException, OsEntityException {
        mockWrapperPropertiesForNothing();

        scaler.scale();

        // Check step 5
        verify(k8SAdministration, never())
                .launchWrapperPodsPool(Mockito.anyInt(), Mockito.any());
        verify(osAdministration, never())
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());

        // Check step 6
        verify(k8SMonitoring, times(1)).monitorNodesToDelete();
        verify(osAdministration, times(2)).deleteServer(Mockito.anyString());
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId1"));
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId2"));
    }

    @Test
    public void nominalNothingWithoutStepActivated() throws PodResourceException,
            K8sUnknownResourceException, OsEntityException {
        mockWrapperPropertiesForNothing();
        mockDevProperties(false);

        scaler.scale();

        verify(kafkaMonitoring, never()).monitorL1Jobs();
        
        // Check step 5
        verify(k8SAdministration, never())
                .launchWrapperPodsPool(Mockito.anyInt(), Mockito.any());
        verify(osAdministration, never())
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());

        // Check step 6
        verify(k8SMonitoring, never()).monitorNodesToDelete();
    }

    @Test
    public void testRemoveSucceedeedPods()
            throws PodResourceException, K8sUnknownResourceException,
            InternalErrorException, FileNotFoundException {
        long currentTimestamp = System.currentTimeMillis();

        mockStep1(false);
        scaler.removeSucceededPods();
        assertTrue(currentTimestamp + 10000 > System.currentTimeMillis());

        mockStep1(true);
        // scaler.removeSucceededPods();
        // assertTrue(currentTimestamp + 10000 < System.currentTimeMillis());
    }

    @Test
    public void testcalculateMonitoredValue() {
        double val1 =
                scaler.calculateMonitoredValue(kafkaMonitor, new ArrayList<>());
        assertEquals(0.0, val1, 0.0);

        double val2 =
                scaler.calculateMonitoredValue(kafkaMonitor, wrappersMonitor);
        assertEquals(4100.0, val2, 0.0);
    }

    @Test
    public void testNeedScaling() {
        assertEquals(ScalingAction.FREE, scaler.needScaling(799.9));
        assertEquals(ScalingAction.NOTHING, scaler.needScaling(800.01));
        assertEquals(ScalingAction.NOTHING, scaler.needScaling(1399.09));
        assertEquals(ScalingAction.ALLOC, scaler.needScaling(1450));
    }

    @Test
    public void testHasLabels() {
        Map<String, String> labels = new HashMap<String, String>();
        labels.put("wrapperconfig", "l1");
        labels.put("wrapperstate", LABEL_WRAPPER_USED);
        assertTrue(scaler.hasLabels(labels));
        labels.put("wrapperstate", LABEL_WRAPPER_NOTUSED);
        assertFalse(scaler.hasLabels(labels));
        labels.put("wrapperstate", LABEL_WRAPPER_USED);
        assertTrue(scaler.hasLabels(labels));
        labels.remove("wrapperstate");
        assertFalse(scaler.hasLabels(labels));

    }

    @Test
    public void testDeletUnusedResourcrs()
            throws PodResourceException, K8sUnknownResourceException,
            InternalErrorException, FileNotFoundException, OsEntityException {
        doReturn(null).when(k8SAdministration).deleteTerminatedWrapperPods();

        doReturn(new ArrayList<>()).when(k8SMonitoring).monitorNodesToDelete();
        scaler.deleteUnusedResources();
        verify(k8SMonitoring, times(1)).monitorNodesToDelete();
        verify(osAdministration, never()).deleteServer(Mockito.anyString());

        mockStep6();
        scaler.deleteUnusedResources();
        verify(k8SMonitoring, times(2)).monitorNodesToDelete();
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId1"));
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId2"));
    }

    @Test
    public void testDeletUnusedResourcrsWhenException()
            throws PodResourceException, K8sUnknownResourceException,
            InternalErrorException, FileNotFoundException, OsEntityException {
        mockStep6();
        doThrow(new OsServerNotDeletedException("nodeId2", "message"))
                .when(osAdministration).deleteServer(Mockito.anyString());

        mockStep6();
        scaler.deleteUnusedResources();
        verify(k8SMonitoring, times(1)).monitorNodesToDelete();
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId1"));
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId2"));
    }
}
