package fr.viveris.s1pdgs.scaler.k8s;


import static org.junit.Assert.assertEquals;
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
import fr.viveris.s1pdgs.scaler.k8s.model.PodLogicalStatus;
import fr.viveris.s1pdgs.scaler.k8s.model.PodStatus;
import fr.viveris.s1pdgs.scaler.k8s.model.VolumeDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperNodeMonitor;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperPodMonitor;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperStatusException;
import fr.viveris.s1pdgs.scaler.k8s.services.NodeService;
import fr.viveris.s1pdgs.scaler.k8s.services.PodService;
import fr.viveris.s1pdgs.scaler.k8s.services.WrapperService;

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
		when(nodeService.getNodesWithLabels(Mockito.any())).thenReturn(unusedNodes);
		
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
		when(podService.getPodsWithLabel(Mockito.any(), Mockito.any())).thenReturn(pods);
	}
	
	private void mockMonitorL1Wrappers() throws WrapperStatusException {
		WrapperDesc wrapperDesc = new WrapperDesc("wrapperName");
		wrapperDesc.setTimeSinceLastChange(15000);
		wrapperDesc.setErrorCounter(0);
		wrapperDesc.setStatus(PodLogicalStatus.PROCESSING);
		when(wrapperService.getWrapperStatus(Mockito.anyString(),Mockito.anyString())).thenReturn(wrapperDesc);
		
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
		when(nodeService.getNodesWithLabel(Mockito.anyString(), Mockito.anyString())).thenReturn(unusedNodes);
		
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
		WrapperNodeMonitor wrapperNodeMonitor = new WrapperNodeMonitor(nodeDesc);
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
		assertEquals("Result is different from expected result", expectedResult, result);
	}
	
	@Test
	public void testMonitorL1Wrappers() throws WrapperStatusException {
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
		WrapperNodeMonitor wrapperNodeMonitor = new WrapperNodeMonitor(nodeDesc);
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
		assertEquals("Result is different from expected result", expectedResult, result);
	}
}
