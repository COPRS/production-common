package esa.s1pdgs.cpoc.scaler.k8s.services;

import static org.junit.Assert.assertEquals;
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

import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;
import esa.s1pdgs.cpoc.scaler.k8s.model.NodeDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.VolumeDesc;
import esa.s1pdgs.cpoc.scaler.k8s.services.NodeService;
import io.fabric8.kubernetes.api.model.AttachedVolume;
import io.fabric8.kubernetes.api.model.DoneableNode;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.NodeFluent.MetadataNested;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.NodeSpec;
import io.fabric8.kubernetes.api.model.NodeStatus;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public class NodeServiceTest {

    @Mock
    private KubernetesClient k8sClient;

    @Mock
    private NonNamespaceOperation<Node, NodeList, DoneableNode, Resource<Node, DoneableNode>> nodes;

    private NodeService service;
    private NodeList nodeList;
    private List<NodeDesc> nodeDesc;
    private Map<String, String> labels;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new NodeService(k8sClient);

        labels = new HashMap<String, String>();
        labels.put("label1", "label1Value");
        labels.put("label2", "label2Value");

        nodeList = new NodeList();
        nodeList.setItems(Arrays.asList(buildNode("node1", labels),
                buildNode("node2", labels)));

        nodeDesc = Arrays.asList(buildNodeDesc("node1", labels),
                buildNodeDesc("node2", labels));

        doReturn(nodes).when(k8sClient).nodes();
    }

    private Node buildNode(String nodeName, Map<String, String> labels) {
        // ObjectMeta
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(nodeName);
        objectMeta.setLabels(labels);

        // NodeSpec
        NodeSpec nodeSpec = new NodeSpec();
        nodeSpec.setExternalID("externalId");

        // NodeStatus
        NodeStatus nodeStatus = new NodeStatus();
        List<NodeAddress> addresses = new ArrayList<>();
        addresses.add(new NodeAddress("address", "InternalIP"));
        nodeStatus.setAddresses(addresses);
        List<AttachedVolume> volumesAttached = new ArrayList<AttachedVolume>();
        volumesAttached.add(new AttachedVolume("devicePath", "volumeName"));
        nodeStatus.setVolumesAttached(volumesAttached);

        // Node to convert
        return new Node("apiVersion", "kind", objectMeta, nodeSpec, nodeStatus);
    }

    private NodeDesc buildNodeDesc(String nodeName,
            Map<String, String> labels) {
        NodeDesc expectedResult = new NodeDesc(nodeName);
        expectedResult.setExternalId("externalId");
        expectedResult.addAddress(AddressType.fromLabel("InternalIP"),
                "address");
        expectedResult.addLabels(labels);
        expectedResult.addVolume(new VolumeDesc("volumeName"));
        return expectedResult;
    }

    // ----------------------------------------------------
    // LABELS
    // ----------------------------------------------------

    @Mock
    private FilterWatchListDeletable<Node, NodeList, Boolean, Watch, Watcher<Node>> filterWithLabels;

    private void mockClientForLabels() {
        doReturn(filterWithLabels).when(nodes).withLabels(Mockito.any());
        doReturn(filterWithLabels).when(nodes).withLabel(Mockito.anyString(),
                Mockito.any());
        doReturn(nodeList).when(filterWithLabels).list();
    }

    @Test
    public void testGetNodesWithLabels() {
        mockClientForLabels();
        List<NodeDesc> result = service.getNodesWithLabels(labels);
        assertEquals(nodeDesc, result);
        verify(nodes, times(1)).withLabels(Mockito.eq(labels));
        verifyNoMoreInteractions(nodes);
    }

    @Test
    public void testGetNodesWithLabelsWhenNoNodes() {
        mockClientForLabels();
        doReturn(null).when(filterWithLabels).list();
        List<NodeDesc> result = service.getNodesWithLabels(labels);
        assertEquals(0, result.size());
        verify(nodes, times(1)).withLabels(Mockito.eq(labels));
        verifyNoMoreInteractions(nodes);
    }

    @Test
    public void testGetNodesWithLabelsWhenEmptyNodes() {
        mockClientForLabels();
        doReturn(new NodeList()).when(filterWithLabels).list();
        List<NodeDesc> result = service.getNodesWithLabels(labels);
        assertEquals(0, result.size());
        verify(nodes, times(1)).withLabels(Mockito.eq(labels));
        verifyNoMoreInteractions(nodes);
    }

    @Test
    public void testGetNodesWithLabel() {
        mockClientForLabels();
        List<NodeDesc> result = service.getNodesWithLabel("lab", "val");
        assertEquals(nodeDesc, result);
        verify(nodes, times(1)).withLabel(Mockito.eq("lab"), Mockito.eq("val"));
        verifyNoMoreInteractions(nodes);
    }

    @Test
    public void testGetNodesWithLabelNoNodes() {
        mockClientForLabels();
        doReturn(null).when(filterWithLabels).list();
        List<NodeDesc> result = service.getNodesWithLabel("lab", "val");
        assertEquals(0, result.size());
        verify(nodes, times(1)).withLabel(Mockito.eq("lab"), Mockito.eq("val"));
        verifyNoMoreInteractions(nodes);
    }

    @Test
    public void testGetNodesWithLabelEmptyNodes() {
        mockClientForLabels();
        doReturn(new NodeList()).when(filterWithLabels).list();
        List<NodeDesc> result = service.getNodesWithLabel("lab", "val");
        assertEquals(0, result.size());
        verify(nodes, times(1)).withLabel(Mockito.eq("lab"), Mockito.eq("val"));
        verifyNoMoreInteractions(nodes);
    }

    // ----------------------------------------------------
    // LABELS
    // ----------------------------------------------------

    @Mock
    private Resource<Node, DoneableNode> withName;
    @Mock
    private DoneableNode doneableNode;
    @Mock
    private MetadataNested<DoneableNode> metadata;

    private void mockClientForEditLabel() {
        doReturn(withName).when(nodes).withName(Mockito.anyString());
        doReturn(doneableNode).when(withName).edit();
        doReturn(metadata).when(doneableNode).editMetadata();
        doReturn(metadata).when(metadata).removeFromLabels(Mockito.anyString());
        doReturn(metadata).when(metadata).addToLabels(Mockito.anyString(),
                Mockito.anyString());
        doReturn(doneableNode).when(metadata).endMetadata();
    }

    @Test
    public void testEditLabelToNode() {
        mockClientForEditLabel();

        service.editLabelToNode("nodename", "lab", "val");
        verify(metadata, times(1)).removeFromLabels(Mockito.eq("lab"));
        verify(metadata, times(1)).addToLabels(Mockito.eq("lab"),
                Mockito.eq("val"));
    }

}
