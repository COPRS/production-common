package fr.viveris.s1pdgs.scaler.k8s.services;


import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

import fr.viveris.s1pdgs.scaler.k8s.K8SMonitoring;
import fr.viveris.s1pdgs.scaler.k8s.WrapperProperties;
import fr.viveris.s1pdgs.scaler.k8s.model.AddressType;
import fr.viveris.s1pdgs.scaler.k8s.model.NodeDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.PodStatus;
import fr.viveris.s1pdgs.scaler.k8s.model.VolumeDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperStatusException;

public class K8SMonitoringTest {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS) 
	private WrapperProperties wrapperProperties;
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) 
	private NodeService nodeService;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS) 
	private PodService podService;
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) 
	private WrapperService wrapperService;
	
	@InjectMocks
	private K8SMonitoring k8SMonitoring;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(wrapperProperties.getLabelWrapperConfig().getLabel()).thenReturn("wrapperconfig");
		when(wrapperProperties.getLabelWrapperConfig().getValue()).thenReturn("l1");
		when(wrapperProperties.getLabelWrapperStateUnused().getLabel()).thenReturn("wrapperstate");
		when(wrapperProperties.getLabelWrapperStateUnused().getValue()).thenReturn("unused");
		when(wrapperProperties.getLabelWrapperApp().getLabel()).thenReturn("app");
		when(wrapperProperties.getLabelWrapperApp().getValue()).thenReturn("l1-wrapper");
		
		NodeDesc nodeDesc1 = new NodeDesc("nodeName");
		nodeDesc1.setExternalId("externalId");
		Map<AddressType, String> addresses = new HashMap<AddressType, String>();
		addresses.put(AddressType.fromLabel("InternalIP"), "internalIP");
		addresses.put(AddressType.fromLabel("ExternalIP"), "externalIP");
		nodeDesc1.addAddresses(addresses);
		nodeDesc1.addAddress(AddressType.fromLabel("Hostname"), "hostname");
		Map<String, String> labels = new HashMap<String, String>();
		labels.put("wrapperconfig", "l1");
		labels.put("wrapperstate", "unused");
		nodeDesc1.addLabels(labels);
		nodeDesc1.addVolume(new VolumeDesc("VolumeName1"));
		List<NodeDesc> unusedNodes = new ArrayList<>();
		unusedNodes.add(nodeDesc1);
		when(nodeService.getNodesWithLabels(Mockito.any())).thenReturn(unusedNodes);
		
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
		when(podService.getPodsWithLabel(Mockito.any(), Mockito.any())).thenReturn(pods);
	}

	@Test
	public void TestMonitorNodesToDelete() {
		System.out.println(k8SMonitoring.monitorNodesToDelete());
	}
	
	@Test
	public void TestMonitorL1Wrappers() throws WrapperStatusException {
		k8SMonitoring.monitorL1Wrappers();
	}
}
