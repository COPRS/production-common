package fr.viveris.s1pdgs.ingestor.s3;

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
public class SessionFilesS3ServicesTest {

	/**
	 * Bucket name
	 */
	private static final String BUCKET_NAME = "bucket-session";

	/**
	 * Amazon S3 client
	 */
	@Mock
	private AmazonS3 s3client;
	
	/**
	 * Service to test
	 */
	private SessionFilesS3Services service;

	/**
	 * Test initialization
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		service = new SessionFilesS3Services(s3client, BUCKET_NAME);
	}
	
	/**
	 * Test constructor
	 */
	@Test
	public void testConstructor() {
		assertEquals(BUCKET_NAME, service.getBucketName());
	}

}
