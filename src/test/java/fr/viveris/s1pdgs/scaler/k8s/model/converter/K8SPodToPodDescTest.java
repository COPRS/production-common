package fr.viveris.s1pdgs.scaler.k8s.model.converter;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fr.viveris.s1pdgs.scaler.k8s.model.AddressType;
import fr.viveris.s1pdgs.scaler.k8s.model.PodDesc;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;

public class K8SPodToPodDescTest {

	@Test
	public void applyTest() {
		//ObjectMeta
		Map<String, String> labels = new HashMap<String, String>();
		labels.put("label1", "label1Value");
		labels.put("label2", "label2Value");
		ObjectMeta objectMeta = new ObjectMeta();
		objectMeta.setName("podName");
		objectMeta.setLabels(labels);
		
		//PodSpec
		PodSpec podSpec = new PodSpec();
		podSpec.setNodeName("nodeName");
		podSpec.setHostname("hostname");
		
		//NodeStatus
		PodStatus podStatus = new PodStatus();
		podStatus.setHostIP("hostIP");
		podStatus.setPodIP("podIP");
		podStatus.setPhase("Running");
		
		//Node to convert
		Pod nodeToConvert = new Pod("apiVersion", "kind", objectMeta, podSpec, podStatus);
				
		//ExpectedResult
		PodDesc expectedResult = new PodDesc("podName");
		expectedResult.setNodeName("nodeName");
		expectedResult.setNodeIpAddress("hostIP");
		expectedResult.addAddress(AddressType.fromLabel("InternalIP"), "podIP");
		expectedResult.addAddress(AddressType.fromLabel("Hostname"), "hostname");
		expectedResult.addLabels(labels);
		expectedResult.setStatus(fr.viveris.s1pdgs.scaler.k8s.model.PodStatus.Running);
		
		//Test the apply function
		K8SPodToPodDesc convertor = new K8SPodToPodDesc();
		PodDesc result = convertor.apply(nodeToConvert);
		
		assertEquals("Result is different from the expected result", expectedResult, result);
	}

}
