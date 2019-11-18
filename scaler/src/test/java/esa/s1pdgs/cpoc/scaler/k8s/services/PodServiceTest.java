package esa.s1pdgs.cpoc.scaler.k8s.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.errors.k8s.K8sUnknownResourceException;
import esa.s1pdgs.cpoc.common.errors.k8s.PodResourceException;
import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodDesc;
import io.fabric8.kubernetes.api.model.DoneablePersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;

public class PodServiceTest {

    @Mock
    private KubernetesClient k8sClient;

    @Mock
    private MixedOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> pods;

    private PodService service;
    private PodList podList;
    private List<PodDesc> podDesc;
    private Map<String, String> labels;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new PodService(k8sClient);

        labels = new HashMap<String, String>();
        labels.put("label1", "label1Value");
        labels.put("label2", "label2Value");

        podList = new PodList();
        podList.setItems(Arrays.asList(buildPod("node1", labels),
                buildPod("node2", labels)));

        podDesc = Arrays.asList(buildPodDesc("node1", labels),
                buildPodDesc("node2", labels));

        doReturn(pods).when(k8sClient).pods();
    }

    private Pod buildPod(String nodeName, Map<String, String> labels) {
        // ObjectMeta
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(nodeName);
        objectMeta.setLabels(labels);

        // PodSpec
        PodSpec podSpec = new PodSpec();
        podSpec.setNodeName("nodeName");
        podSpec.setHostname("hostname");

        // PodStatus
        PodStatus podStatus = new PodStatus();
        podStatus.setHostIP("hostIP");
        podStatus.setPodIP("podIP");
        podStatus.setPhase("Running");

        // Node to convert
        return new Pod("apiVersion", "kind", objectMeta, podSpec, podStatus);
    }

    private PodDesc buildPodDesc(String nodeName, Map<String, String> labels) {
        PodDesc expectedResult = new PodDesc(nodeName);
        expectedResult.setNodeName("nodeName");
        expectedResult.setNodeIpAddress("hostIP");
        expectedResult.addAddress(AddressType.fromLabel("InternalIP"), "podIP");
        expectedResult.addAddress(AddressType.fromLabel("Hostname"),
                "hostname");
        expectedResult.addLabels(labels);
        expectedResult
                .setStatus(esa.s1pdgs.cpoc.scaler.k8s.model.PodStatus.Running);
        return expectedResult;
    }

    // ----------------------------------------------------
    // LABELS
    // ----------------------------------------------------

    @Mock
    private FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> filterWithLabels;

    private void mockClientForLabels(PodList podList) {
        doReturn(filterWithLabels).when(pods).withLabel(Mockito.anyString(),
                Mockito.anyString());
        doReturn(filterWithLabels).when(filterWithLabels)
                .withField(Mockito.anyString(), Mockito.anyString());
        doReturn(podList).when(filterWithLabels).list();
    }

    @Test
    public void testGetPodsWithLabel() {
        mockClientForLabels(podList);
        List<PodDesc> result = service.getPodsWithLabel("lab", "val");
        assertEquals(podDesc, result);
        verify(pods, times(1)).withLabel(Mockito.eq("lab"), Mockito.eq("val"));
        verifyNoMoreInteractions(pods);
    }

    @Test
    public void testGetPodsWithLabelNoPod() {
        mockClientForLabels(null);
        List<PodDesc> result = service.getPodsWithLabel("lab", "val");
        assertEquals(0, result.size());
        verify(pods, times(1)).withLabel(Mockito.eq("lab"), Mockito.eq("val"));
        verifyNoMoreInteractions(pods);
    }

    @Test
    public void testGetPodsWithLabelEmptyPod() {
        mockClientForLabels(new PodList());
        List<PodDesc> result = service.getPodsWithLabel("lab", "val");
        assertEquals(0, result.size());
        verify(pods, times(1)).withLabel(Mockito.eq("lab"), Mockito.eq("val"));
        verifyNoMoreInteractions(pods);
    }

    @Test
    public void testGetPodsWithLabelStatus() {
        mockClientForLabels(podList);
        List<PodDesc> result =
                service.getPodsWithLabelAndStatusPhase("lab", "val", "pahse");
        assertEquals(podDesc, result);
        verify(pods, times(1)).withLabel(Mockito.eq("lab"), Mockito.eq("val"));
        verify(filterWithLabels, times(1)).withField(Mockito.eq("status.phase"),
                Mockito.eq("pahse"));
        verify(filterWithLabels, times(1)).list();
        verifyNoMoreInteractions(pods);
        verifyNoMoreInteractions(filterWithLabels);
    }

    // ----------------------------------------------------
    // CREATE / DELETE POD
    // ----------------------------------------------------

    @Mock
    private ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean> load;

    @Mock
    private MixedOperation<PersistentVolumeClaim, PersistentVolumeClaimList, DoneablePersistentVolumeClaim, Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>> persistentVolumeClaims;

    @Mock
    private NonNamespaceOperation<PersistentVolumeClaim, PersistentVolumeClaimList, DoneablePersistentVolumeClaim, Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>> mockedVolumes;

    @Mock
    private NonNamespaceOperation<Pod, PodList, DoneablePod, PodResource<Pod, DoneablePod>> mockedPods;

    private List<HasMetadata> resources;

    private void mockPodOperations() {
        resources = Arrays.asList(buildPersistentVolumeClaim("volume-name"),
                buildPodWithVol("podName1", null),
                buildPodWithVol("podName2", buildVolume("claimName")));

        doReturn(load).when(k8sClient).load(Mockito.any());
        doReturn(resources).when(load).get();

        doReturn(persistentVolumeClaims).when(k8sClient)
                .persistentVolumeClaims();
        doReturn(mockedVolumes).when(persistentVolumeClaims)
                .inNamespace(Mockito.anyString());
        doReturn(buildPersistentVolumeClaim("volName1")).when(mockedVolumes)
                .create(Mockito.any());

        doReturn(mockedPods).when(pods).inNamespace(Mockito.anyString());
        doReturn(buildPodWithVol("podName1", null)).when(mockedPods)
                .create(Mockito.any());

    }

    private PersistentVolumeClaim buildPersistentVolumeClaim(
            String volumeName) {
        // ObjectMeta
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(volumeName);
        objectMeta.setLabels(labels);

        // Node to convert
        return new PersistentVolumeClaim("apiVersion", "PersistentVolumeClaim",
                objectMeta, null, null);
    }

    private Pod buildPodWithVol(String podName, Volume volume) {
        // ObjectMeta
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(podName);
        objectMeta.setLabels(labels);
        //
        PodSpec podSpec = new PodSpec();
        if (volume != null) {
            podSpec.setVolumes(Arrays.asList(volume));
        }

        // Node to convert
        return new Pod("apiVersion", "Pod", objectMeta, podSpec, null);
    }

    private Volume buildVolume(String claimname) {
        Volume vol = new Volume();
        vol.setPersistentVolumeClaim(
                new PersistentVolumeClaimVolumeSource(claimname, true));
        return vol;
    }

    private PersistentVolumeClaim buildInvalidResources(String volumeName) {
        // ObjectMeta
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(volumeName);
        objectMeta.setLabels(labels);

        // Node to convert
        return new PersistentVolumeClaim("apiVersion", "kind", objectMeta, null,
                null);
    }

    @Test(expected = PodResourceException.class)
    public void testLoadREsourcesWhenPbFile()
            throws PodResourceException, K8sUnknownResourceException {
        mockPodOperations();
        service.loadRessourcesFromFile("xdfg", "suffixe");
    }

    @Test(expected = PodResourceException.class)
    public void testLoadREsourcesWhenNoResources()
            throws PodResourceException, K8sUnknownResourceException {
        mockPodOperations();
        doReturn(new ArrayList<HasMetadata>()).when(load).get();
        service.loadRessourcesFromFile("./pom.xml", "suffixe");
    }

    @Test(expected = K8sUnknownResourceException.class)
    public void testLoadREsourcesWhenInvamlidResource()
            throws PodResourceException, K8sUnknownResourceException {
        resources = Arrays.asList(buildInvalidResources("volume-name"));
        doReturn(load).when(k8sClient).load(Mockito.any());
        doReturn(resources).when(load).get();
        service.loadRessourcesFromFile("./pom.xml", "suffixe");
    }

    @Test
    public void testLoadResources()
            throws PodResourceException, K8sUnknownResourceException {
        mockPodOperations();
        List<HasMetadata> result =
                service.loadRessourcesFromFile("./pom.xml", "-suffixe");
        assertEquals(3, result.size());
        assertEquals("volume-name-suffixe",
                result.get(0).getMetadata().getName());
        assertEquals("podName1-suffixe", result.get(1).getMetadata().getName());
        assertEquals("podName2-suffixe", result.get(2).getMetadata().getName());
        Pod pExp = (Pod) result.get(2);
        assertEquals("claimName", pExp.getSpec().getVolumes().get(0)
                .getPersistentVolumeClaim().getClaimName());
    }

    @Test
    public void testCreatePod()
            throws PodResourceException, K8sUnknownResourceException {
        mockPodOperations();

        service.createPodFromTemplate("./pom.xml", 12);
        verify(mockedVolumes, times(1)).create(Mockito.any());
        verify(mockedPods, times(2)).create(Mockito.any());
        verifyNoMoreInteractions(mockedPods, mockedVolumes);
    }

    @Test
    public void testDeletePod()
            throws PodResourceException, K8sUnknownResourceException {
        mockPodOperations();
        doReturn(true).when(mockedVolumes)
                .delete(Mockito.any(PersistentVolumeClaim.class));
        doReturn(true).when(mockedPods).delete(Mockito.any(Pod.class));
        boolean ret = service.deletePodFromTemplate("./pom.xml", "12");
        assertTrue(ret);
        verify(mockedVolumes, times(1))
                .delete(Mockito.any(PersistentVolumeClaim.class));
        verify(mockedPods, times(2)).delete(Mockito.any(Pod.class));
        verifyNoMoreInteractions(mockedPods, mockedVolumes);
    }

    @Test
    public void testDeletePod2()
            throws PodResourceException, K8sUnknownResourceException {
        mockPodOperations();
        doReturn(false).when(mockedVolumes)
                .delete(Mockito.any(PersistentVolumeClaim.class));
        doReturn(false).when(mockedPods).delete(Mockito.any(Pod.class));
        boolean ret = service.deletePodFromTemplate("./pom.xml", "12");
        assertFalse(ret);
        verify(mockedVolumes, times(1))
                .delete(Mockito.any(PersistentVolumeClaim.class));
        verify(mockedPods, times(2)).delete(Mockito.any(Pod.class));
        verifyNoMoreInteractions(mockedPods, mockedVolumes);
    }

    @Test
    public void testDeletePod3()
            throws PodResourceException, K8sUnknownResourceException {
        mockPodOperations();
        doReturn(false).when(mockedVolumes)
                .delete(Mockito.any(PersistentVolumeClaim.class));
        doReturn(true).when(mockedPods).delete(Mockito.any(Pod.class));
        boolean ret = service.deletePodFromTemplate("./pom.xml", "12");
        assertFalse(ret);
        verify(mockedVolumes, times(1))
                .delete(Mockito.any(PersistentVolumeClaim.class));
        verify(mockedPods, times(2)).delete(Mockito.any(Pod.class));
        verifyNoMoreInteractions(mockedPods, mockedVolumes);
    }

    @Test
    public void testDeletePod4()
            throws PodResourceException, K8sUnknownResourceException {
        mockPodOperations();
        doReturn(true).when(mockedVolumes)
                .delete(Mockito.any(PersistentVolumeClaim.class));
        doReturn(false).when(mockedPods).delete(Mockito.any(Pod.class));
        boolean ret = service.deletePodFromTemplate("./pom.xml", "12");
        assertFalse(ret);
        verify(mockedVolumes, times(1))
                .delete(Mockito.any(PersistentVolumeClaim.class));
        verify(mockedPods, times(2)).delete(Mockito.any(Pod.class));
        verifyNoMoreInteractions(mockedPods, mockedVolumes);
    }
}
