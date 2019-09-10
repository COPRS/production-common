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
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

public class SwiftObsClientIT {

	public final static ProductFamily auxiliaryFiles = ProductFamily.AUXILIARY_FILE;
	public final static String testFilePrefix = "abc/def/";
	public final static String testFileName1 = "testfile1.txt";
	public final static String testFileName2 = "testfile2.txt";
	public final static String testDirectoryName = "testdir";
	public final static File testFile1 = getResource("/" + testFileName1);
	public final static File testFile2 = getResource("/" + testFileName2);
	public final static File testDirectory = getResource("/" + testDirectoryName);
	
	AbstractObsClient uut;
	
	public static File getResource(String fileName) {
		try {
			return new File(SwiftObsClientIT.class.getClass().getResource(fileName).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not get resource");
		}
	}
	
	@Before
	public void setUp() throws ObsException, SdkClientException {
		uut = new SwiftObsClient();
		
		// prepare environment
		if (!((SwiftObsClient) uut).containerExists(auxiliaryFiles)) {
			((SwiftObsClient)uut).createContainer(auxiliaryFiles);
		}

		if (uut.exists(new ObsObject(auxiliaryFiles, testFileName1))) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFileName1);
		}

		if (uut.exists(new ObsObject(auxiliaryFiles, testFileName2))) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFileName2);
		}

		if (uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1))) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFilePrefix + testFileName1);
		}

		if (uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName2))) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFilePrefix + testFileName2);
		}

		if (uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1))) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1);
		}

		if (uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2))) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2);
		}

		if (uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum"))) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testDirectoryName + ".md5sum");
		}
	}
	
	@Test
	public void uploadWithoutPrefixTest() throws ObsException, IOException, SdkClientException {	
		// upload
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
		uut.uploadFile(auxiliaryFiles, testFileName1, testFile1);
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
	}

	@Test
	public void uploadWithPrefixTest() throws ObsException, IOException, SdkClientException {	
		// upload
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1)));
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName1, testFile1);
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1)));
	}

	@Test
	public void uploadOfDirectoryTest() throws IOException, SdkClientException, AbstractCodedException {	
		// upload
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + testFileName1)));
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + testFileName2)));
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));
		uut.upload(Arrays.asList(new ObsUploadObject(auxiliaryFiles, testDirectoryName, testDirectory)));
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));
	}
	
	@Test
	public void deleteWithoutPrefixTest() throws ObsException, IOException, SdkClientException {	
		// upload
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
		uut.uploadFile(auxiliaryFiles, testFileName1, testFile1);
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));

		// delete
		if (uut.exists(new ObsObject(auxiliaryFiles, testFileName1))) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFileName1);
		}
	}
	
	@Test
	public void deleteWithPrefixTest() throws ObsException, IOException, SdkClientException {	
		// upload
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1)));
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName1, testFile1);
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1)));

		// delete
		if (uut.exists(new ObsObject(auxiliaryFiles, testFileName1))) {
			((SwiftObsClient)uut).deleteObject(auxiliaryFiles, testFilePrefix + testFileName1);
		}
	}

	@Test
	public void downloadFileWithoutPrefixTest() throws ObsException, IOException, SdkClientException {	
		// upload
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
		uut.uploadFile(auxiliaryFiles, testFileName1, testFile1);
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
		
		// single file download
        String targetDir = Files.createTempDirectory(this.getClass().getCanonicalName() + "-").toString();
		uut.downloadFile(auxiliaryFiles, testFileName1, targetDir);
		String send1 = new String(Files.readAllBytes(testFile1.toPath()));
		String received1 = new String(Files.readAllBytes((new File(targetDir + "/" + testFileName1)).toPath()));
		assertEquals(send1, received1);
	}

	@Test
	public void downloadFileWithPrefixTest() throws ObsException, IOException, SdkClientException {	
		// upload
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1)));
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName1, testFile1);
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1)));
		
		// single file download
        String targetDir = Files.createTempDirectory(this.getClass().getCanonicalName() + "-").toString();
		uut.downloadFile(auxiliaryFiles, testFilePrefix + testFileName1, targetDir);
		String send1 = new String(Files.readAllBytes(testFile1.toPath()));
		String received1 = new String(Files.readAllBytes((new File(targetDir + "/" + testFilePrefix + testFileName1)).toPath()));
		assertEquals(send1, received1);
	}

	@Test
	public void downloadOfDirectoryTest() throws IOException, SdkClientException, AbstractCodedException {	
		// upload
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + testFileName1)));
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + testFileName2)));
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));
		uut.upload(Arrays.asList(new ObsUploadObject(auxiliaryFiles, testDirectoryName, testDirectory)));
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName1)));
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + "/" + testFileName2)));
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testDirectoryName + ".md5sum")));
		
		// multi file download
		String targetDir = Files.createTempDirectory(this.getClass().getCanonicalName() + "-").toString();
		uut.download(Arrays.asList(new ObsDownloadObject(auxiliaryFiles, testDirectoryName + "/", targetDir)));

		System.out.println(new File(testDirectory, testFileName1).toPath());
		String send1 = new String(Files.readAllBytes(new File(testDirectory, testFileName1).toPath()));
		String received1 = new String(Files.readAllBytes((new File(targetDir + "/" + testDirectoryName + "/" + testFileName1)).toPath()));
		assertEquals(send1, received1);
		
		String send2 = new String(Files.readAllBytes(new File(testDirectory, testFileName2).toPath()));
		String received2 = new String(Files.readAllBytes((new File(targetDir + "/" + testDirectoryName + "/" + testFileName2)).toPath()));
		assertEquals(send2, received2);
		
		assertFalse(new File(targetDir + "/" + testDirectoryName + ".md5sum").exists());
	}
	
	@Test
	public void numberOfObjectsWithoutPrefixTest() throws ObsException, IOException, SdkClientException {	
		// upload
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFileName2)));
		uut.uploadFile(auxiliaryFiles, testFileName1, testFile1);
		uut.uploadFile(auxiliaryFiles, testFileName2, testFile2);
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName1)));
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFileName2)));
		
		// count
		int count = ((SwiftObsClient)uut).numberOfObjects(auxiliaryFiles, "");
		assertEquals(2, count);
	}

	@Test
	public void numberOfObjectsWithPrefixTest() throws ObsException, IOException, SdkClientException {	
		// upload
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1)));
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName2)));
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName1, testFile1);
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName2, testFile2);
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1)));
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName2)));
		
		// count
		int count = ((SwiftObsClient)uut).numberOfObjects(auxiliaryFiles, testFilePrefix);
		assertEquals(2, count);
	}
	
	@Test
	public final void getAllAsStreamTest() throws Exception {
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1)));
		assertFalse(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName2)));
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName1, testFile1);
		uut.uploadFile(auxiliaryFiles, testFilePrefix + testFileName2, testFile2);
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName1)));
		assertTrue(uut.exists(new ObsObject(auxiliaryFiles, testFilePrefix + testFileName2)));
		
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
