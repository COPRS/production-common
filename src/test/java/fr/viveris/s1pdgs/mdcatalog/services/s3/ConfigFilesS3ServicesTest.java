package fr.viveris.s1pdgs.mdcatalog.services.s3;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.s3.AmazonS3;

/**
 * Test the service SessionFilesS3Services
 * @author Cyrielle Gailliard
 *
 */
public class ConfigFilesS3ServicesTest {

	/**
	 * Bucket name
	 */
	private static final String BUCKET_NAME = "bucket-config";

	/**
	 * Amazon S3 client
	 */
	@Mock
	private AmazonS3 s3client;
	
	/**
	 * Service to test
	 */
	private ConfigFilesS3Services service;

	/**
	 * Test initialization
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		service = new ConfigFilesS3Services(s3client, BUCKET_NAME);
	}
	
	/**
	 * Test constructor
	 */
	@Test
	public void testConstructor() {
		assertEquals(BUCKET_NAME, service.getBucketName());
	}

}
