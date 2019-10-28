package esa.s1pdgs.cpoc.obs_sdk.s3;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

@Ignore
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:obs-aws-s3.properties")
@ContextConfiguration(classes = {ObsConfigurationProperties.class})
public class S3GetTempDownloadUrlTest {
	
	public final static ProductFamily PRODUCT_FAMILY = ProductFamily.GHOST;
	public final static String FILE_NAME = "testfile-random.bin";
	public final static File testFile = getResource("/" + FILE_NAME);
	public final static	ObsObject OBS_OBJECT = new ObsObject(PRODUCT_FAMILY, FILE_NAME);

	
	@Autowired
	private ObsConfigurationProperties configuration;
	
	private S3ObsClient uut;
	
	public static File getResource(String fileName) {
		try {
			return new File(S3GetTempDownloadUrlTest.class.getClass().getResource(fileName).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not get resource");
		}
	}
	
	@Before
	public void setUp() throws SdkClientException, AbstractCodedException {	
		uut = (S3ObsClient) new S3ObsClient.Factory().newObsClient(configuration);
		// prepare environment
		if (!uut.bucketExists(PRODUCT_FAMILY)) {
			uut.createBucket(PRODUCT_FAMILY);
		}
//		if (uut.exists(OBS_OBJECT)) {
//			uut.deleteObject(PRODUCT_FAMILY, FILE_NAME);
//			assertFalse(uut.exists(OBS_OBJECT));
//		}
//		uut.upload(Collections.singletonList(new ObsUploadObject(PRODUCT_FAMILY, FILE_NAME, testFile)));
		assertTrue(uut.exists(OBS_OBJECT));
	}
	
	@Test
	public void tempUrlDownloadTest_OnDownloadFinishesBeforeExpirationDate () throws ObsServiceException, SdkClientException, AbstractCodedException, IOException {		
		long expirationTimeInSeconds = 10L;
		URL url = uut.createTemporaryDownloadUrl(OBS_OBJECT, expirationTimeInSeconds);
		Instant expirationDate = Instant.now().plus(Duration.standardSeconds(expirationTimeInSeconds));
		System.out.println(String.format("expirationDate: %s", expirationDate.toString()));
		System.out.println(String.format("curl -v -o /tmp/file.tmp \"%s\"", url.toString()));
		
	}
}
