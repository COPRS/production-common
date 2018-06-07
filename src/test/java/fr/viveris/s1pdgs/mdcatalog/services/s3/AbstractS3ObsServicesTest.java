package fr.viveris.s1pdgs.mdcatalog.services.s3;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;

import fr.viveris.s1pdgs.mdcatalog.model.exception.ObjectStorageException;

/**
 * Test the abstraction of OBS services using Amazon S3
 * 
 * @author Cyrielle Gailliard
 *
 */
public class AbstractS3ObsServicesTest {

	/**
	 * Bucket name
	 */
	private static final String BUCKET_NAME = "bucket-name";

	/**
	 * Amazon S3 client
	 */
	@Mock
	private AmazonS3 s3client;

	/**
	 * Service to test
	 */
	private S3ObsServicesImpl service;

	/**
	 * To check the raised custom exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * File path used for testing get file
	 */
	private static final String FILE_TEST_GET_PATH = "test/parent/file1.txt";

	/**
	 * File for testing getFile
	 */
	private File fileTestGet;

	/**
	 * Parent of file for testing getFile
	 */
	private File fileTestGetParent;

	/**
	 * Test initialization
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		service = new S3ObsServicesImpl(s3client, BUCKET_NAME);

		fileTestGet = new File(FILE_TEST_GET_PATH);
		fileTestGetParent = new File("test/parent");
	}

	/**
	 * Cleaning
	 */
	@After
	public void clean() {
		if (fileTestGet != null && fileTestGet.exists()) {
			fileTestGet.delete();
		}
		if (fileTestGetParent != null && fileTestGetParent.exists()) {
			fileTestGetParent.delete();
		}
	}

	/**
	 * Test exist function with nominal case
	 * 
	 * @throws ObjectStorageException
	 */
	@Test
	public void testExistOk() throws ObjectStorageException {
		// First test: s3client return true
		doReturn(true).when(s3client).doesObjectExist(Mockito.anyString(), Mockito.anyString());
		boolean result = service.exist("key-test");
		assertTrue("Service should return true", result);
		verify(s3client, times(1)).doesObjectExist(Mockito.eq(BUCKET_NAME), Mockito.eq("key-test"));

		// First test: s3client return false
		doReturn(false).when(s3client).doesObjectExist(Mockito.anyString(), Mockito.anyString());
		result = service.exist("key-test");
		assertFalse("Service should return false", result);
		verify(s3client, times(2)).doesObjectExist(Mockito.eq(BUCKET_NAME), Mockito.eq("key-test"));
	}

	/**
	 * Exist function shall raise ObjectStorageException exception when a
	 * AmazonServiceException occurs
	 * 
	 * @throws ObjectStorageException
	 */
	@Test
	public void testExistWhenAmazonServiceExceptions() throws ObjectStorageException {
		doThrow(new AmazonServiceException("amazon service exception raised")).when(s3client)
				.doesObjectExist(Mockito.anyString(), Mockito.anyString());

		thrown.expect(ObjectStorageException.class);
		thrown.expect(hasProperty("bucket", is(BUCKET_NAME)));
		thrown.expect(hasProperty("key", is("key-test")));
		thrown.expectMessage(containsString("amazon service exception raised"));
		thrown.expectCause(isA(AmazonServiceException.class));
		service.exist("key-test");
	}

	/**
	 * Exist function shall raise ObjectStorageException exception when a
	 * SdkClientException occurs
	 * 
	 * @throws ObjectStorageException
	 */
	@Test
	public void testExistWhenSDKClientExceptions() throws ObjectStorageException {
		doThrow(new SdkClientException("SDK client exception raised")).when(s3client)
				.doesObjectExist(Mockito.anyString(), Mockito.anyString());

		thrown.expect(ObjectStorageException.class);
		thrown.expect(hasProperty("bucket", is(BUCKET_NAME)));
		thrown.expect(hasProperty("key", is("key-test")));
		thrown.expectMessage(containsString("SDK client exception raised"));
		thrown.expectCause(isA(SdkClientException.class));
		service.exist("key-test");
	}

	@Test
	public void testUploadFileOk() throws ObjectStorageException {
		doReturn(null).when(s3client).putObject(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class));

		File file = new File(".tutu");
		service.uploadFile("key-name", file);
		verify(s3client, times(1)).putObject(BUCKET_NAME, "key-name", file);
	}

	/**
	 * uploadFile function shall raise ObjectStorageException exception when a
	 * AmazonServiceException occurs
	 * 
	 * @throws ObjectStorageException
	 */
	@Test
	public void testUploadFileWhenAmazonServiceExceptions() throws ObjectStorageException {
		doThrow(new AmazonServiceException("amazon service exception raised")).when(s3client)
				.putObject(Mockito.anyString(), Mockito.anyString(), Mockito.any(File.class));

		thrown.expect(ObjectStorageException.class);
		thrown.expect(hasProperty("bucket", is(BUCKET_NAME)));
		thrown.expect(hasProperty("key", is("key-test")));
		thrown.expectMessage(containsString("amazon service exception raised"));
		thrown.expectCause(isA(AmazonServiceException.class));
		service.uploadFile("key-test", new File(".tutu"));
	}

	/**
	 * uploadFile function shall raise ObjectStorageException exception when a
	 * SdkClientException occurs
	 * 
	 * @throws ObjectStorageException
	 */
	@Test
	public void testUploadFileWhenSDKClientExceptions() throws ObjectStorageException {
		doThrow(new SdkClientException("SDK client exception raised")).when(s3client).putObject(Mockito.anyString(),
				Mockito.anyString(), Mockito.any(File.class));

		thrown.expect(ObjectStorageException.class);
		thrown.expect(hasProperty("bucket", is(BUCKET_NAME)));
		thrown.expect(hasProperty("key", is("key-test")));
		thrown.expectMessage(containsString("SDK client exception raised"));
		thrown.expectCause(isA(SdkClientException.class));
		service.uploadFile("key-test", new File(".tutu"));
	}

	@Test
	public void testGetFileOk() throws ObjectStorageException {
		doReturn(null).when(s3client).getObject(Mockito.any(), Mockito.any(File.class));

		File resultFile = service.getFile("key-name", FILE_TEST_GET_PATH);
		verify(s3client, times(1)).getObject(Mockito.any(), Mockito.eq(fileTestGet));
		assertEquals(fileTestGet, resultFile);
		assertTrue(resultFile.exists());
		assertTrue(resultFile.isFile());
	}

	/**
	 * getFile function shall raise ObjectStorageException exception when a
	 * AmazonServiceException occurs
	 * 
	 * @throws ObjectStorageException
	 */
	@Test
	public void testGetFileWhenAmazonServiceExceptions() throws ObjectStorageException {
		doThrow(new AmazonServiceException("amazon service exception raised")).when(s3client).getObject(Mockito.any(),
				Mockito.any(File.class));

		thrown.expect(ObjectStorageException.class);
		thrown.expect(hasProperty("bucket", is(BUCKET_NAME)));
		thrown.expect(hasProperty("key", is("key-name")));
		thrown.expectMessage(containsString("amazon service exception raised"));
		thrown.expectCause(isA(AmazonServiceException.class));
		service.getFile("key-name", FILE_TEST_GET_PATH);
	}

	/**
	 * getFile function shall raise ObjectStorageException exception when a
	 * SdkClientException occurs
	 * 
	 * @throws ObjectStorageException
	 */
	@Test
	public void testGetFileWhenSDKClientExceptions() throws ObjectStorageException {
		doThrow(new SdkClientException("SDK client exception raised")).when(s3client).getObject(Mockito.any(),
				Mockito.any(File.class));

		thrown.expect(ObjectStorageException.class);
		thrown.expect(hasProperty("bucket", is(BUCKET_NAME)));
		thrown.expect(hasProperty("key", is("key-name")));
		thrown.expectMessage(containsString("SDK client exception raised"));
		thrown.expectCause(isA(SdkClientException.class));
		service.getFile("key-name", FILE_TEST_GET_PATH);
	}
}

/**
 * Implementation class for tests
 *
 */
class S3ObsServicesImpl extends AbstractS3ObsServices {

	/**
	 * Constructor
	 * 
	 * @param s3client
	 * @param bucketName
	 */
	public S3ObsServicesImpl(final AmazonS3 s3client, final String bucketName) {
		super(s3client, bucketName);
	}

}
