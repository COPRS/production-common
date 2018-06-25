package fr.viveris.s1pdgs.scaler.k8s.model;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class NodeDescTest {

	@Test
	public void testToString() {
		NodeDesc obj = new NodeDesc("name");
		obj.setExternalId("externalId");
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
		List<VolumeDesc> volumes = new ArrayList<VolumeDesc>();
		volumes.add(new VolumeDesc("VolumeName1"));
		volumes.add(new VolumeDesc("VolumeName2"));
		obj.addVolumes(volumes);
		obj.addVolume(new VolumeDesc("VolumeName3"));

		String str = obj.toString();
		assertTrue(str.contains("name: name"));
		assertTrue(str.contains("externalId: externalId"));
		assertTrue(str.contains("addresses: {") && str.contains("EXTERNAL_IP=externalIP") && str.contains("HOSTNAME=hostname") && str.contains("INTERNAL_IP=internalIP"));
		assertTrue(str.contains("labels: {") && str.contains("label1=label1Value") && str.contains("label2=label2Value") && str.contains("label3=label3Value"));
		assertTrue(str.contains("volumes: [") && str.contains("{name: VolumeName1}") && str.contains("{name: VolumeName2}") && str.contains("{name: VolumeName3}"));
	}
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(NodeDesc.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
