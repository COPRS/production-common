package esa.s1pdgs.cpoc.obs_sdk.swift;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Ignore;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;

@Ignore
public class SwiftObsClientIT {

	public final static ProductFamily auxiliaryFiles = ProductFamily.AUXILIARY_FILE;
	public final static String testFileName = "testfile.txt";
	public final static String nonExistentFileName = "non-existent.txt";
	public final static File testFile = getResource("/" + testFileName);
	
	public static File getResource(String fileName) { 
		try {
			return new File(SwiftObsClientIT.class.getClass().getResource(fileName).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not get resource");
		}
	}
	
	@Test
	public void uploadFileTest() throws ObsServiceException, ObsException, SwiftSdkClientException {
		AbstractObsClient client = new SwiftObsClient();
		if (client.exist(auxiliaryFiles, testFileName)) {
			((SwiftObsClient)client).deleteObject(auxiliaryFiles, testFileName);
		}
		assertFalse(client.exist(auxiliaryFiles, testFileName));
		client.uploadFile(auxiliaryFiles, testFileName, testFile);
		assertTrue(client.exist(auxiliaryFiles, testFileName));	
	}
	
}
