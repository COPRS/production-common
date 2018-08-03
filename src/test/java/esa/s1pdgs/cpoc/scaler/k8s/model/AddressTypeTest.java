package esa.s1pdgs.cpoc.scaler.k8s.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.scaler.k8s.model.AddressType;


public class AddressTypeTest {

	@Test
	public void fromLabelTest() {
		AddressType resultInternelIP = AddressType.fromLabel("InternalIP");
		assertEquals("Address type are different", resultInternelIP, AddressType.INTERNAL_IP);
		AddressType resultExternalIP = AddressType.fromLabel("ExternalIP");
		assertEquals("Address type are different", resultExternalIP, AddressType.EXTERNAL_IP);
		AddressType resultHostName = AddressType.fromLabel("Hostname");
		assertEquals("Address type are different", resultHostName, AddressType.HOSTNAME);
		AddressType resultUnknown = AddressType.fromLabel("");
		assertEquals("Address type are different", resultUnknown, AddressType.UNKNOWN);
		AddressType resultUnknownBis = AddressType.fromLabel("Unknown");
		assertEquals("Address type are different", resultUnknownBis, AddressType.UNKNOWN);
	}

}
