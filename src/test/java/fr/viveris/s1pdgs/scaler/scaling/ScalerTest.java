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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

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

import fr.viveris.s1pdgs.scaler.AbstractCodedException;
import fr.viveris.s1pdgs.scaler.AbstractCodedException.ErrorCode;
import fr.viveris.s1pdgs.scaler.DevProperties;
import fr.viveris.s1pdgs.scaler.InternalErrorException;
import fr.viveris.s1pdgs.scaler.k8s.K8SAdministration;
import fr.viveris.s1pdgs.scaler.k8s.K8SMonitoring;
import fr.viveris.s1pdgs.scaler.k8s.WrapperProperties;
import fr.viveris.s1pdgs.scaler.k8s.WrapperProperties.LabelKubernetes;
import fr.viveris.s1pdgs.scaler.k8s.WrapperProperties.TimeProperties;
import fr.viveris.s1pdgs.scaler.k8s.model.AddressType;
import fr.viveris.s1pdgs.scaler.k8s.model.NodeDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.PodLogicalStatus;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperNodeMonitor;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperPodMonitor;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.K8sUnknownResourceException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.PodResourceException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperStatusException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperStopException;
import fr.viveris.s1pdgs.scaler.kafka.KafkaMonitoring;
import fr.viveris.s1pdgs.scaler.kafka.model.KafkaPerGroupPerTopicMonitor;
import fr.viveris.s1pdgs.scaler.openstack.OpenStackAdministration;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsEntityException;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsEntityInternaloErrorException;
import fr.viveris.s1pdgs.scaler.openstack.model.exceptions.OsServerNotActiveException;
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
        mockWrapperProperties(ScalingAction.NOTHING);

        // Mock
        mockStep1(false);
        mockStep2(ScalingAction.NOTHING);
        mockStep3(ScalingAction.NOTHING);
        mockStep5(ScalingAction.NOTHING);
        mockStep6();

        //

    }

    // -----------------------------------------------------
    // Mock
    // -----------------------------------------------------

    private void mockStepsForAllocation()
            throws WrapperStatusException, PodResourceException,
            K8sUnknownResourceException, OsEntityException {
        mockWrapperProperties(ScalingAction.ALLOC);
        mockStep2(ScalingAction.ALLOC);
        mockStep3(ScalingAction.ALLOC);
        mockStep5(ScalingAction.ALLOC);
    }

    private void mockStepsForFree()
            throws WrapperStatusException, PodResourceException,
            K8sUnknownResourceException, OsEntityException {
        mockWrapperProperties(ScalingAction.FREE);
        mockStep2(ScalingAction.FREE);
        mockStep3(ScalingAction.FREE);
        mockStep5(ScalingAction.FREE);
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

    private void mockStep2(ScalingAction action) {
        switch (action) {
            case ALLOC:
                kafkaMonitor = new KafkaPerGroupPerTopicMonitor(new Date(),
                        "group", "topic");
                kafkaMonitor.setNbConsumers(3);
                kafkaMonitor.setNbPartitions(6);
                kafkaMonitor.getLagPerConsumers().put("consumer1",
                        Long.valueOf(3));
                kafkaMonitor.getLagPerConsumers().put("consumer2",
                        Long.valueOf(5));
                kafkaMonitor.getLagPerConsumers().put("consumer3",
                        Long.valueOf(0));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(0),
                        Long.valueOf(2));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(1),
                        Long.valueOf(3));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(2),
                        Long.valueOf(0));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(3),
                        Long.valueOf(2));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(4),
                        Long.valueOf(0));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(5),
                        Long.valueOf(1));
                break;
            case FREE:
                kafkaMonitor = new KafkaPerGroupPerTopicMonitor(new Date(),
                        "group", "topic");
                kafkaMonitor.setNbConsumers(6);
                kafkaMonitor.setNbPartitions(6);
                kafkaMonitor.getLagPerConsumers().put("consumer1",
                        Long.valueOf(2));
                kafkaMonitor.getLagPerConsumers().put("consumer2",
                        Long.valueOf(3));
                kafkaMonitor.getLagPerConsumers().put("consumer3",
                        Long.valueOf(0));
                kafkaMonitor.getLagPerConsumers().put("consumer4",
                        Long.valueOf(2));
                kafkaMonitor.getLagPerConsumers().put("consumer5",
                        Long.valueOf(0));
                kafkaMonitor.getLagPerConsumers().put("consumer6",
                        Long.valueOf(1));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(0),
                        Long.valueOf(2));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(1),
                        Long.valueOf(3));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(2),
                        Long.valueOf(0));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(3),
                        Long.valueOf(2));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(4),
                        Long.valueOf(0));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(5),
                        Long.valueOf(1));
                break;
            default:
                kafkaMonitor = new KafkaPerGroupPerTopicMonitor(new Date(),
                        "group", "topic");
                kafkaMonitor.setNbConsumers(3);
                kafkaMonitor.setNbPartitions(6);
                kafkaMonitor.getLagPerConsumers().put("consumer1",
                        Long.valueOf(3));
                kafkaMonitor.getLagPerConsumers().put("consumer2",
                        Long.valueOf(5));
                kafkaMonitor.getLagPerConsumers().put("consumer3",
                        Long.valueOf(0));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(0),
                        Long.valueOf(2));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(1),
                        Long.valueOf(3));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(2),
                        Long.valueOf(0));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(3),
                        Long.valueOf(2));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(4),
                        Long.valueOf(0));
                kafkaMonitor.getLagPerPartition().put(Integer.valueOf(5),
                        Long.valueOf(1));
                break;
        }

        doReturn(kafkaMonitor).when(kafkaMonitoring).monitorL1Jobs();
    }

    private void mockStep3(ScalingAction action) throws WrapperStatusException {
        switch (action) {
            case ALLOC:
                wrappersMonitor = new ArrayList<>();
                WrapperNodeMonitor wAlloc1 = buildWrapperNodeMonitor("node1",
                        "nodeId1", LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
                wAlloc1.addWrapperPod(buildWrapperPodMonitor("pod1", "node1",
                        PodLogicalStatus.PROCESSING, 15000000, 1000000));
                wrappersMonitor.add(wAlloc1);
                WrapperNodeMonitor wAlloc2 = buildWrapperNodeMonitor("node2",
                        "nodeId2", LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
                wAlloc2.addWrapperPod(buildWrapperPodMonitor("pod2", "node2",
                        PodLogicalStatus.PROCESSING, 15000000, 0));
                wrappersMonitor.add(wAlloc2);
                WrapperNodeMonitor wAlloc3 = buildWrapperNodeMonitor("node3",
                        "nodeId3", LABEL_WRAPPER_L1, LABEL_WRAPPER_NOTUSED);
                wrappersMonitor.add(wAlloc3);
                break;

            case FREE:
                wrappersMonitor = new ArrayList<>();
                WrapperNodeMonitor wFree1 = buildWrapperNodeMonitor("node1",
                        "nodeId1", LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
                wFree1.addWrapperPod(buildWrapperPodMonitor("pod1", "node1",
                        PodLogicalStatus.PROCESSING, 15000000, 1000000));
                wrappersMonitor.add(wFree1);
                WrapperNodeMonitor wFree2 = buildWrapperNodeMonitor("node2",
                        "nodeId2", LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
                wFree2.addWrapperPod(buildWrapperPodMonitor("pod2", "node2",
                        PodLogicalStatus.PROCESSING, 15000000, 2400000));
                wrappersMonitor.add(wFree2);
                WrapperNodeMonitor wFree3 = buildWrapperNodeMonitor("node3",
                        "nodeId3", LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
                wFree3.addWrapperPod(buildWrapperPodMonitor("pod3", "node3",
                        PodLogicalStatus.PROCESSING, 15000000, 0));
                wrappersMonitor.add(wFree3);
                WrapperNodeMonitor wFree4 = buildWrapperNodeMonitor("node4",
                        "nodeId4", LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
                wFree4.addWrapperPod(buildWrapperPodMonitor("pod4", "node4",
                        PodLogicalStatus.PROCESSING, 15000000, 1000000));
                wrappersMonitor.add(wFree4);
                WrapperNodeMonitor wFree5 = buildWrapperNodeMonitor("node5",
                        "nodeId5", LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
                wFree5.addWrapperPod(buildWrapperPodMonitor("pod5", "node5",
                        PodLogicalStatus.PROCESSING, 15000000, 0));
                wrappersMonitor.add(wFree5);
                WrapperNodeMonitor wFree6 = buildWrapperNodeMonitor("node6",
                        "nodeId6", LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
                wFree6.addWrapperPod(buildWrapperPodMonitor("pod6", "node6",
                        PodLogicalStatus.PROCESSING, 15000000, 500000));
                wrappersMonitor.add(wFree6);
                break;

            default:
                wrappersMonitor = new ArrayList<>();
                WrapperNodeMonitor wrapperNodeMonitor =
                        buildWrapperNodeMonitor("node1", "nodeId1",
                                LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
                wrapperNodeMonitor.addWrapperPod(buildWrapperPodMonitor("pod1",
                        "node1", PodLogicalStatus.PROCESSING, 15000000,
                        1000000));
                wrappersMonitor.add(wrapperNodeMonitor);
                WrapperNodeMonitor wrapperNodeMonitor2 =
                        buildWrapperNodeMonitor("node4", "nodeId4",
                                LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
                wrapperNodeMonitor2.addWrapperPod(buildWrapperPodMonitor("pod4",
                        "node4", PodLogicalStatus.WAITING, 15000000, 0));
                wrappersMonitor.add(wrapperNodeMonitor2);

                break;
        }
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
        podDesc.addAddress(AddressType.INTERNAL_IP, "address-" + podName);
        WrapperPodMonitor podMonitor = new WrapperPodMonitor(podDesc);
        podMonitor.setLogicalStatus(status);
        podMonitor.setPassedExecutionTime(execPAssTime);
        podMonitor.setRemainingExecutionTime(RemainExecTime);
        return podMonitor;
    }

    private void mockStep5(ScalingAction action) throws PodResourceException,
            K8sUnknownResourceException, OsEntityException {
        doNothing().when(k8SAdministration)
                .setWrapperNodeUnusable(Mockito.anyString());
        doNothing().when(k8SAdministration)
                .setWrapperNodeUsable(Mockito.anyString());
        doNothing().when(k8SAdministration)
                .launchWrapperPodsPool(Mockito.anyInt(), Mockito.any());
        doReturn("serverId").when(osAdministration)
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());
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

    private void mockWrapperProperties(ScalingAction action) {
        switch (action) {
            case ALLOC:
                mockWrapperProperties(900, 800, 1400);
                break;
            case FREE:
                mockWrapperProperties(900, 4500, 7000);
                break;
            default:
                mockWrapperProperties(900, 800, 6000);
                break;
        }
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

        doReturn(1).when(wrapperProperties).getNbPodsPerServer();
        doReturn(6).when(wrapperProperties).getNbMaxServers();
        doReturn(3).when(wrapperProperties).getNbPoolingPods();
        doReturn(3).when(wrapperProperties).getNbMinServers();
    }

    // -----------------------------------------------------
    // Init scaling
    // -----------------------------------------------------

    @Test
    public void testInitscaleWhenNotActivated()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException {
        mockDevProperties(false);
        scaler.initscale();

        verifyZeroInteractions(osAdministration);
        verifyZeroInteractions(k8SAdministration);
        verifyZeroInteractions(k8SMonitoring);
    }

    @Test
    public void testInitscaleWhenException()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException {
        doThrow(new WrapperStatusException("ip","server","error")).when(k8SMonitoring)
                .monitorL1Wrappers();
        scaler.initscale();

        verify(k8SMonitoring, times(1)).monitorL1Wrappers();
        verifyZeroInteractions(k8SAdministration);
        verifyZeroInteractions(k8SMonitoring);
    }

    @Test
    public void testInitscaleAdd()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException {
        scaler.initscale();

        verify(k8SMonitoring, times(1)).monitorL1Wrappers();
        verify(k8SAdministration, times(1)).launchWrapperPodsPool(Mockito.eq(1),
                Mockito.any());
        verify(osAdministration, times(1))
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());
        verifyNoMoreInteractions(osAdministration);
        verifyNoMoreInteractions(k8SAdministration);
    }

    @Test
    public void testInitscaleAddWhenNoNeed()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException {
        mockStepsForFree();
        scaler.initscale();

        verify(k8SMonitoring, times(1)).monitorL1Wrappers();
        verifyZeroInteractions(osAdministration);
        verifyZeroInteractions(k8SAdministration);
    }

    @Test
    public void testInitscaleFreeWhenNoNeed()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException {
        mockStepsForFree();
        scaler.initscale();

        verify(k8SMonitoring, times(1)).monitorL1Wrappers();
        verifyZeroInteractions(osAdministration);
        verifyZeroInteractions(k8SAdministration);
    }

    @Test
    public void testInitscaleFree()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException, WrapperStopException {
        mockStepsForFree();
        WrapperNodeMonitor wFree6 = buildWrapperNodeMonitor("node7", "nodeId7",
                LABEL_WRAPPER_L1, LABEL_WRAPPER_USED);
        wFree6.addWrapperPod(buildWrapperPodMonitor("pod7", "node7",
                PodLogicalStatus.PROCESSING, 15000000, 500000));
        wrappersMonitor.add(wFree6);
        doReturn(wrappersMonitor).when(k8SMonitoring).monitorL1Wrappers();

        scaler.initscale();

        verify(k8SMonitoring, times(1)).monitorL1Wrappers();
        verify(k8SAdministration, times(1))
                .setWrapperNodeUnusable(Mockito.eq("node3"));
        verify(k8SAdministration, times(1))
                .stopWrapperPods(Mockito.eq(Arrays.asList("address-pod3")));
        verifyNoMoreInteractions(osAdministration);
        verifyNoMoreInteractions(k8SAdministration);
    }

    // -----------------------------------------------------
    // Invalid resources
    // -----------------------------------------------------

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

    // -----------------------------------------------------
    // Scaling global
    // -----------------------------------------------------

    @Test
    public void nominalScaleNothing()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException {

        scaler.scale();

        // Check step 2
        verify(kafkaMonitoring, times(1)).monitorL1Jobs();

        // Check step 3
        verify(k8SMonitoring, times(1)).monitorL1Wrappers();

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
    public void nominalScaleAlloc()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException {
        this.mockStepsForAllocation();

        scaler.scale();

        // Check step 2
        verify(kafkaMonitoring, times(1)).monitorL1Jobs();

        // Check step 3
        verify(k8SMonitoring, times(1)).monitorL1Wrappers();

        // Check step 5
        verify(k8SAdministration, times(1))
                .setWrapperNodeUsable(Mockito.eq("node3"));
        verify(k8SAdministration, times(3)).launchWrapperPodsPool(Mockito.eq(1),
                Mockito.any());
        verify(osAdministration, times(2))
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());

        // Check step 6
        verify(k8SMonitoring, times(1)).monitorNodesToDelete();
        verify(osAdministration, times(2)).deleteServer(Mockito.anyString());
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId1"));
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId2"));
    }

    @Test
    public void nominalScaleFree()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException, WrapperStopException {
        this.mockStepsForFree();

        scaler.scale();

        // Check step 2
        verify(kafkaMonitoring, times(1)).monitorL1Jobs();

        // Check step 3
        verify(k8SMonitoring, times(1)).monitorL1Wrappers();

        // Check step 5
        verify(k8SAdministration, times(1))
                .setWrapperNodeUnusable(Mockito.eq("node3"));
        verify(k8SAdministration, times(1))
                .setWrapperNodeUnusable(Mockito.eq("node5"));
        verify(k8SAdministration, times(1))
                .setWrapperNodeUnusable(Mockito.eq("node6"));
        verify(k8SAdministration, times(1))
                .stopWrapperPods(Mockito.eq(Arrays.asList("address-pod3")));
        verify(k8SAdministration, times(1))
                .stopWrapperPods(Mockito.eq(Arrays.asList("address-pod5")));
        verify(k8SAdministration, times(1))
                .stopWrapperPods(Mockito.eq(Arrays.asList("address-pod6")));

        // Check step 6
        verify(k8SMonitoring, times(1)).monitorNodesToDelete();
        verify(osAdministration, times(2)).deleteServer(Mockito.anyString());
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId1"));
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId2"));
    }

    @Test
    public void nominalNothingWithoutStepActivated()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException {
        mockDevProperties(false);

        scaler.scale();

        verify(kafkaMonitoring, never()).monitorL1Jobs();

        // Check step 3
        verify(k8SMonitoring, never()).monitorL1Wrappers();

        // Check step 5
        verify(k8SAdministration, never())
                .launchWrapperPodsPool(Mockito.anyInt(), Mockito.any());
        verify(osAdministration, never())
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());

        // Check step 6
        verify(k8SMonitoring, never()).monitorNodesToDelete();
    }

    @Test
    public void nominalNothingWithoutStepActivatedExceptKafka()
            throws PodResourceException, K8sUnknownResourceException,
            OsEntityException, WrapperStatusException {
        Map<String, Boolean> activations = new HashMap<>();
        activations.put("pod-deletion", false);
        activations.put("kafka-monitoring", true);
        activations.put("init-scaling", false);
        activations.put("k8s-monitoring", false);
        activations.put("value-monitored", false);
        activations.put("scaling", false);
        activations.put("unused-ressources-deletion", false);
        doReturn(activations).when(devProperties).getActivations();

        scaler.scale();

        // Check step 2
        verify(kafkaMonitoring, times(1)).monitorL1Jobs();

        // Check step 3
        verify(k8SMonitoring, never()).monitorL1Wrappers();

        // Check step 5
        verify(k8SAdministration, never())
                .launchWrapperPodsPool(Mockito.anyInt(), Mockito.any());
        verify(osAdministration, never())
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());

        // Check step 6
        verify(k8SMonitoring, never()).monitorNodesToDelete();
    }

    @Test
    public void testScaleWhenNoKafkaMonitor() throws WrapperStatusException {

        // Mock no consumer
        KafkaPerGroupPerTopicMonitor noConsumer =
                new KafkaPerGroupPerTopicMonitor(new Date(), "group", "topic");

        doReturn(noConsumer).when(kafkaMonitoring).monitorL1Jobs();

        long before = System.currentTimeMillis();
        scaler.scale();
        long after = System.currentTimeMillis();

        // Check thread sleep
        assertTrue(before + 15000 * 2 <= after);

        // Check step 2
        verify(kafkaMonitoring, times(2)).monitorL1Jobs();

        // Check step 3
        verify(k8SMonitoring, never()).monitorL1Wrappers();

    }

    // -----------------------------------------------------
    // Test step 1 and 6
    // -----------------------------------------------------

    @Test
    public void testRemoveSucceedeedPods()
            throws PodResourceException, K8sUnknownResourceException,
            InternalErrorException, FileNotFoundException {
        long currentTimestamp = System.currentTimeMillis();

        mockStep1(false);
        scaler.removeSucceededPods();
        assertTrue(currentTimestamp + 10000 > System.currentTimeMillis());

        mockStep1(true);
        scaler.removeSucceededPods();
        assertTrue(currentTimestamp + 10000 <= System.currentTimeMillis());
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
        doThrow(new OsEntityException("server", "nodeId2",
                ErrorCode.INTERNAL_ERROR, "message")).when(osAdministration)
                        .deleteServer(Mockito.anyString());

        scaler.deleteUnusedResources();
        verify(k8SMonitoring, times(1)).monitorNodesToDelete();
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId1"));
        verify(osAdministration, times(1)).deleteServer(Mockito.eq("nodeId2"));
    }

    // -----------------------------------------------------
    // Test step4
    // -----------------------------------------------------

    @Test
    public void testcalculateMonitoredValue() {
        double val1 =
                scaler.calculateMonitoredValue(kafkaMonitor, new ArrayList<>());
        assertEquals(0.0, val1, 0.0);

        double val2 =
                scaler.calculateMonitoredValue(kafkaMonitor, wrappersMonitor);
        assertEquals(4100.0, val2, 0.0);
    }

    // -----------------------------------------------------
    // Test step 5
    // -----------------------------------------------------

    @Test
    public void testNeedScaling() {
        assertEquals(ScalingAction.FREE, scaler.needScaling(799.9));
        assertEquals(ScalingAction.NOTHING, scaler.needScaling(800.01));
        assertEquals(ScalingAction.NOTHING, scaler.needScaling(5999.09));
        assertEquals(ScalingAction.ALLOC, scaler.needScaling(6050));
    }

    @Test
    public void testAddResources() throws AbstractCodedException {
        mockStepsForAllocation();
        scaler.addRessources(wrappersMonitor, 3);

        // Check reuse one node and creat 2 one
        verify(k8SAdministration, times(1))
                .setWrapperNodeUsable(Mockito.eq("node3"));
        verify(k8SAdministration, times(3)).launchWrapperPodsPool(Mockito.eq(1),
                Mockito.any());
        verify(osAdministration, times(2))
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());
        verifyNoMoreInteractions(osAdministration);
        verifyNoMoreInteractions(k8SAdministration);
    }

    @Test
    public void testAddResourcesOnlyReused() throws AbstractCodedException {
        mockStepsForAllocation();
        scaler.addRessources(wrappersMonitor, 1);

        // Check reuse one node and creat 2 one
        verify(k8SAdministration, times(1))
                .setWrapperNodeUsable(Mockito.eq("node3"));
        verify(k8SAdministration, times(1)).launchWrapperPodsPool(Mockito.eq(1),
                Mockito.any());
        verifyNoMoreInteractions(osAdministration);
        verifyNoMoreInteractions(k8SAdministration);
    }

    @Test
    public void testAddRessourcesWhenMax() throws AbstractCodedException {
        mockStepsForFree();

        scaler.addRessources(wrappersMonitor, 3);

        verify(osAdministration, never())
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());
        verify(k8SAdministration, never())
                .launchWrapperPodsPool(Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void testAddRessourcesWhenServerCreationFailed()
            throws AbstractCodedException {
        mockStepsForAllocation();

        doReturn(null).when(osAdministration)
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());

        scaler.addRessources(wrappersMonitor, 3);

        verify(k8SAdministration, times(1))
                .setWrapperNodeUsable(Mockito.eq("node3"));
        verify(k8SAdministration, times(3)).launchWrapperPodsPool(Mockito.eq(1),
                Mockito.any());
        verify(osAdministration, times(2))
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());
        verifyNoMoreInteractions(osAdministration);
        verifyNoMoreInteractions(k8SAdministration);
    }

    @Test(expected = OsServerNotActiveException.class)
    public void testAddRessourcesWhenServerCreationException()
            throws AbstractCodedException {
        mockStepsForAllocation();

        doThrow(new OsServerNotActiveException("serverId", "error message"))
                .when(osAdministration)
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());

        scaler.addRessources(wrappersMonitor, 3);
    }

    @Test(expected = InternalErrorException.class)
    public void testAddRessourcesWhenServerCreationException2()
            throws AbstractCodedException {
        mockStepsForAllocation();

        doThrow(new IllegalArgumentException("error message"))
                .when(osAdministration)
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());

        scaler.addRessources(wrappersMonitor, 3);
    }

    @Test
    public void testFreeResourcesWhenMin() throws AbstractCodedException {
        mockStepsForAllocation();

        scaler.freeRessources(wrappersMonitor, 3);

        verifyZeroInteractions(k8SAdministration);
    }

    @Test
    public void testFreeResources() throws AbstractCodedException {
        mockStepsForFree();

        scaler.freeRessources(wrappersMonitor, 3);

        verify(k8SAdministration, times(1))
                .setWrapperNodeUnusable(Mockito.eq("node3"));
        verify(k8SAdministration, times(1))
                .setWrapperNodeUnusable(Mockito.eq("node5"));
        verify(k8SAdministration, times(1))
                .setWrapperNodeUnusable(Mockito.eq("node6"));
        verify(k8SAdministration, times(1))
                .stopWrapperPods(Mockito.eq(Arrays.asList("address-pod3")));
        verify(k8SAdministration, times(1))
                .stopWrapperPods(Mockito.eq(Arrays.asList("address-pod5")));
        verify(k8SAdministration, times(1))
                .stopWrapperPods(Mockito.eq(Arrays.asList("address-pod6")));
    }
}
