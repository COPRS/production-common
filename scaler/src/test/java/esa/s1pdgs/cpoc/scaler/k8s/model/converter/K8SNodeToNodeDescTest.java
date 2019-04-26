package esa.s1pdgs.cpoc.scaler.k8s.model.converter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;
import esa.s1pdgs.cpoc.scaler.k8s.model.NodeDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.VolumeDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.converter.K8SNodeToNodeDesc;
import io.fabric8.kubernetes.api.model.AttachedVolume;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.NodeSpec;
import io.fabric8.kubernetes.api.model.NodeStatus;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class K8SNodeToNodeDescTest {

	@Test
	public void applyTest() {
		//ObjectMeta
		Map<String, String> labels = new HashMap<String, String>();
		labels.put("label1", "label1Value");
		labels.put("label2", "label2Value");
		ObjectMeta objectMeta = new ObjectMeta();
		objectMeta.setName("nodeName");
		objectMeta.setLabels(labels);
		
		//NodeSpec
		NodeSpec nodeSpec = new NodeSpec();
		nodeSpec.setExternalID("externalId");
		
		//NodeStatus
		NodeStatus nodeStatus = new NodeStatus();
		List<NodeAddress> addresses = new ArrayList<>();
		addresses.add(new NodeAddress("address", "InternalIP"));
		nodeStatus.setAddresses(addresses);
		List<AttachedVolume> volumesAttached = new ArrayList<AttachedVolume>();
		volumesAttached.add(new AttachedVolume("devicePath", "volumeName"));
		nodeStatus.setVolumesAttached(volumesAttached);
		
		//Node to convert
		Node nodeToConvert = new Node("apiVersion", "kind", objectMeta, nodeSpec, nodeStatus);
				
		//ExpectedResult
		NodeDesc expectedResult = new NodeDesc("nodeName");
		expectedResult.setExternalId("externalId");
		expectedResult.addAddress(AddressType.fromLabel("InternalIP"), "address");
		expectedResult.addLabels(labels);
		expectedResult.addVolume(new VolumeDesc("volumeName"));
		
		//Test the apply function
		K8SNodeToNodeDesc convertor = new K8SNodeToNodeDesc();
		NodeDesc result = convertor.apply(nodeToConvert);

		assertEquals("Result is different from the expected result", expectedResult, result);
	}

}
