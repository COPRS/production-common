package esa.s1pdgs.cpoc.obs_sdk.swift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;

public class SwiftObsClientIT {

	public final static ProductFamily auxiliaryFiles = ProductFamily.AUXILIARY_FILE;
	public final static String testFilePrefix = "abc/def/";
	public final static String testFileName1 = "testfile1.txt";
	public final static String testFileName2 = "testfile2.txt";
	public final static File testFile1 = getResource("/" + testFileName1);
	public final static File testFile2 = getResource("/" + testFileName2);
	
	AbstractObsClient uut;
	
	public static File getResource(String fileName) {
		try {
			return new File(SwiftObsClientIT.class.getClass().getResource(fileName).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not get resource");
		}
	}
	
	@Before
	public void setUp() throws ObsServiceException, ObsException, SwiftSdkClientException {
		uut = new SwiftObsClient();
		
		// prepare environment
		if (!((SwiftObsClient) uut).doesContainerExist(auxiliaryFiles)) {
			((SwiftObsClient)uut).createContainer(auxiliaryFiles);
		}

		if (uut.exist(auxiliaryFiles, testFileName1)) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFileName1);
		}

		if (uut.exist(auxiliaryFiles, testFileName2)) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFileName2);
		}

		if (uut.exist(auxiliaryFiles, testFilePrefix + testFileName1)) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFilePrefix + testFileName1);
		}

		if (uut.exist(auxiliaryFiles, testFilePrefix + testFileName2)) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFilePrefix + testFileName2);
		}
	}
	
	@Test
	public void uploadWithoutPrefixTest() throws ObsServiceException, ObsException, SwiftSdkClientException, IOException {	
		// upload
		assertFalse(uut.exist(auxiliaryFiles, testFileName1));
		uut.uploadFile(auxiliaryFiles, testFileName1, testFile1);
		assertTrue(uut.exist(auxiliaryFiles, testFileName1));
	}

	@Test
	public void uploadWithPrefixTest() throws ObsServiceException, ObsException, SwiftSdkClientException, IOException {	
		// upload
		assertFalse(uut.exist(auxiliaryFiles, testFilePrefix + testFileName1));
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName1, testFile1);
		assertTrue(uut.exist(auxiliaryFiles, testFilePrefix + testFileName1));
	}
	
	@Test
	public void deleteWithoutPrefixTest() throws ObsServiceException, ObsException, SwiftSdkClientException, IOException {	
		// upload
		assertFalse(uut.exist(auxiliaryFiles, testFileName1));
		uut.uploadFile(auxiliaryFiles, testFileName1, testFile1);
		assertTrue(uut.exist(auxiliaryFiles, testFileName1));

		// delete
		if (uut.exist(auxiliaryFiles, testFileName1)) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFileName1);
		}
	}
	
	@Test
	public void deleteWithPrefixTest() throws ObsServiceException, ObsException, SwiftSdkClientException, IOException {	
		// upload
		assertFalse(uut.exist(auxiliaryFiles, testFilePrefix + testFileName1));
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName1, testFile1);
		assertTrue(uut.exist(auxiliaryFiles, testFilePrefix + testFileName1));

		// delete
		if (uut.exist(auxiliaryFiles, testFileName1)) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFilePrefix + testFileName1);
		}
	}

	@Test
	public void downloadFileWithoutPrefixTest() throws ObsServiceException, ObsException, SwiftSdkClientException, IOException {	
		// upload
		assertFalse(uut.exist(auxiliaryFiles, testFileName1));
		uut.uploadFile(auxiliaryFiles, testFileName1, testFile1);
		assertTrue(uut.exist(auxiliaryFiles, testFileName1));
		
		// single file download
        String targetDir = Files.createTempDirectory(this.getClass().getCanonicalName() + "-").toString();
		uut.downloadFile(auxiliaryFiles, testFileName1, targetDir);
		String send1 = new String(Files.readAllBytes(testFile1.toPath()));
		String received1 = new String(Files.readAllBytes((new File(targetDir + "/" + testFileName1)).toPath()));
		assertEquals(send1, received1);
	}

	@Test
	public void downloadFileWithPrefixTest() throws ObsServiceException, ObsException, SwiftSdkClientException, IOException {	
		// upload
		assertFalse(uut.exist(auxiliaryFiles, testFilePrefix + testFileName1));
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName1, testFile1);
		assertTrue(uut.exist(auxiliaryFiles, testFilePrefix + testFileName1));
		
		// single file download
        String targetDir = Files.createTempDirectory(this.getClass().getCanonicalName() + "-").toString();
		uut.downloadFile(auxiliaryFiles, testFilePrefix + testFileName1, targetDir);
		String send1 = new String(Files.readAllBytes(testFile1.toPath()));
		String received1 = new String(Files.readAllBytes((new File(targetDir + "/" + testFilePrefix + testFileName1)).toPath()));
		assertEquals(send1, received1);
	}

	@Test
	public void numberOfObjectsWithoutPrefixTest() throws ObsServiceException, ObsException, SwiftSdkClientException, IOException {	
		// upload
		assertFalse(uut.exist(auxiliaryFiles, testFileName1));
		assertFalse(uut.exist(auxiliaryFiles, testFileName2));
		uut.uploadFile(auxiliaryFiles, testFileName1, testFile1);
		uut.uploadFile(auxiliaryFiles, testFileName2, testFile1);
		assertTrue(uut.exist(auxiliaryFiles, testFileName1));
		assertTrue(uut.exist(auxiliaryFiles, testFileName2));
		
		// count
		int count = ((SwiftObsClient)uut).numberOfObjects(auxiliaryFiles, "");
		assertEquals(2, count);
	}

	@Test
	public void numberOfObjectsWithPrefixTest() throws ObsServiceException, ObsException, SwiftSdkClientException, IOException {	
		// upload
		assertFalse(uut.exist(auxiliaryFiles, testFilePrefix + testFileName1));
		assertFalse(uut.exist(auxiliaryFiles, testFilePrefix + testFileName2));
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName1, testFile1);
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName2, testFile1);
		assertTrue(uut.exist(auxiliaryFiles, testFilePrefix + testFileName1));
		assertTrue(uut.exist(auxiliaryFiles, testFilePrefix + testFileName2));
		
		// count
		int count = ((SwiftObsClient)uut).numberOfObjects(auxiliaryFiles, testFilePrefix);
		assertEquals(2, count);
	}
	
	@Test
	public final void getAllAsStreamTest() throws Exception {
		assertFalse(uut.exist(auxiliaryFiles, testFilePrefix + testFileName1));
		assertFalse(uut.exist(auxiliaryFiles, testFilePrefix + testFileName2));
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName1, testFile1);
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName2, testFile2);
		assertTrue(uut.exist(auxiliaryFiles, testFilePrefix + testFileName1));
		assertTrue(uut.exist(auxiliaryFiles, testFilePrefix + testFileName2));
		
		final Map<String,InputStream> res = uut.getAllAsInputStream(auxiliaryFiles, testFilePrefix);
		for (final Map.Entry<String,InputStream> entry : res.entrySet()) {
			try (final InputStream in = entry.getValue()) {
				final String content = IOUtils.toString(in);
				
				if ("abc/def/testfile1.txt".equals(entry.getKey())) {
					assertEquals("test", content);
				}
				else if ("abc/def/testfile2.txt".equals(entry.getKey())) {
					assertEquals("test2", content);
				}
				else {
					fail();
				}
			}
		}
	}
}
