package fr.viveris.s1pdgs.jobgenerator.service.s3;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;

import fr.viveris.s1pdgs.jobgenerator.exception.ObsS3Exception;

/**
 * Test the class SessionFilesS3Services
 * 
 * @author Cyrielle Gailliard
 *
 */
public class SessionFilesS3ServicesTest {

	/**
	 * Client
	 */
	@Mock
	private AmazonS3 s3client;

	/**
	 * Service to test
	 */
	private SessionFilesS3Services service;

	/**
	 * Initialization
	 */
	@Before
	public void init() {
		// Mcokito
		MockitoAnnotations.initMocks(this);

		service = new SessionFilesS3Services(s3client, "bucket-test");
	}

	/**
	 * Cleaning
	 */
	@After
	public void clean() {

	}

	/**
	 * Test SDK client exception throws with additional information
	 * 
	 * @throws ObsS3Exception
	 */
	@Test
	public void testGetFile() throws ObsS3Exception {
		File expected = new File("./tutu");
		Mockito.doReturn(null).when(s3client).getObject(Mockito.any(GetObjectRequest.class),
				Mockito.any(File.class));

		File result = service.getFile("tutu", "./tutu");
		verify(s3client, times(1)).getObject(Mockito.any(GetObjectRequest.class), Mockito.eq(expected));
		assertEquals(expected, result);
	}

	/**
	 * Test SDK client exception throws with additional information
	 */
	@Test
	public void testGetFileSdkClientException() {
		Mockito.doThrow(SdkClientException.class).when(s3client).getObject(Mockito.any(GetObjectRequest.class),
				Mockito.any(File.class));

		try {
			service.getFile("tutu", "./tutu");
			fail("ObsS3Exception should be raised");
		} catch (ObsS3Exception os3e) {
			assertEquals("bucket-test", os3e.getBucket());
			assertEquals("tutu", os3e.getKey());
		}
	}
}
