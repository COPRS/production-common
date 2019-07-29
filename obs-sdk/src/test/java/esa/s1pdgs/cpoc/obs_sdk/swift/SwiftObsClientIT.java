package esa.s1pdgs.cpoc.obs_sdk.swift;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;

public class SwiftObsClientIT {

	@Test
	public void existsTest() throws ObsServiceException, ObsException {		
		AbstractObsClient client = new SwiftObsClient();
		assertFalse(client.exist(ProductFamily.AUXILIARY_FILE, "nonexistent"));
		assertTrue(client.exist(ProductFamily.AUXILIARY_FILE, "index.html"));
	}
	
}
