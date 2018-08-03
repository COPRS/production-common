package esa.s1pdgs.cpoc.scaler.k8s.model.converter;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.converter.K8SPodToPodDesc;
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
		
		//PodStatus
		PodStatus podStatus = new PodStatus();
		podStatus.setHostIP("hostIP");
		podStatus.setPodIP("podIP");
		podStatus.setPhase("Running");
		
		//Pod to convert
		Pod podToConvert = new Pod("apiVersion", "kind", objectMeta, podSpec, podStatus);
				
		//ExpectedResult
		PodDesc expectedResult = new PodDesc("podName");
		expectedResult.setNodeName("nodeName");
		expectedResult.setNodeIpAddress("hostIP");
		expectedResult.addAddress(AddressType.fromLabel("InternalIP"), "podIP");
		expectedResult.addAddress(AddressType.fromLabel("Hostname"), "hostname");
		expectedResult.addLabels(labels);
		expectedResult.setStatus(esa.s1pdgs.cpoc.scaler.k8s.model.PodStatus.Running);
		
		//Test the apply function
		K8SPodToPodDesc convertor = new K8SPodToPodDesc();
		PodDesc result = convertor.apply(podToConvert);
		
		assertEquals("Result is different from the expected result", expectedResult, result);
	}

}
