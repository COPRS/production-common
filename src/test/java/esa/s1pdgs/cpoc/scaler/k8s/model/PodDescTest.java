package esa.s1pdgs.cpoc.scaler.k8s.model;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.PodStatus;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class PodDescTest {

	
	@Test
	public void testToString() {
		PodDesc obj = new PodDesc("name");
		obj.setNodeName("nodeName");
		obj.setNodeIpAddress("nodeIpAddress");
		obj.setStatus(PodStatus.Running);
		Map<AddressType, String> addresses = new HashMap<AddressType, String>();
		addresses.put(AddressType.fromLabel("InternalIP"), "internalIP");
		addresses.put(AddressType.fromLabel("ExternalIP"), "externalIP");
		obj.addAddresses(addresses);
		obj.addAddress(AddressType.fromLabel("Hostname"), "hostname");
		Map<String, String> labels = new HashMap<String, String>();
		labels.put("label1", "label1Value");
		labels.put("label2", "label2Value");
		obj.addLabels(labels);
		obj.addLabels("label3", "label3Value");

		String str = obj.toString();
		assertTrue(str.contains("name: name"));
		assertTrue(str.contains("nodeName: nodeName"));
		assertTrue(str.contains("nodeIpAddress: nodeIpAddress"));
		assertTrue(str.contains("status: Running"));
		assertTrue(str.contains("addresses: {") && str.contains("EXTERNAL_IP=externalIP") && str.contains("HOSTNAME=hostname") && str.contains("INTERNAL_IP=internalIP"));
		assertTrue(str.contains("labels: {") && str.contains("label1=label1Value") && str.contains("label2=label2Value") && str.contains("label3=label3Value"));
	}
	
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(PodDesc.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
