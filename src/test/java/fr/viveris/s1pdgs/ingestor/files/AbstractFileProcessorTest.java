package fr.viveris.s1pdgs.ingestor.files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

import fr.viveris.s1pdgs.ingestor.FileUtils;
import fr.viveris.s1pdgs.ingestor.exceptions.FilePathException;
import fr.viveris.s1pdgs.ingestor.exceptions.IgnoredFileException;
import fr.viveris.s1pdgs.ingestor.exceptions.KafkaSendException;
import fr.viveris.s1pdgs.ingestor.exceptions.ObjectStorageException;
import fr.viveris.s1pdgs.ingestor.files.model.EdrsSessionFileType;
import fr.viveris.s1pdgs.ingestor.files.model.FileDescriptor;
import fr.viveris.s1pdgs.ingestor.files.services.AbstractFileDescriptorService;
import fr.viveris.s1pdgs.ingestor.files.services.ObsServices;
import fr.viveris.s1pdgs.ingestor.kafka.PublicationServices;

public class AbstractFileProcessorTest {

	/**
	 * Amazon S3 service for configuration files
	 */
	@Mock
	private ObsServices obsService;

	/**
	 * KAFKA producer on the topic "metadata"
	 */
	@Mock
	private PublicationServices<String> publisher;

	/**
	 * Builder of file descriptors
	 */
	@Mock
	private AbstractFileDescriptorService extractor;

	/**
	 * Service to test
	 */
	private FileProcessorImpl service;

	/**
	 * Message file of test
	 */
	@Mock
	private Message<File> message;

	/**
	 * File test
	 */
	private File file = new File("test.xml");

	/**
	 * To check the raised custom exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Initialization
	 * 
	 * @throws IOException
	 */
	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);

		file.createNewFile();

		doReturn(file).when(message).getPayload();

		service = new FileProcessorImpl(obsService, publisher, extractor);
	}

	/**
	 * Cleaning
	 */
	@After
	public void clean() {
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * Test that directory are completely ignored
	 */
	@Test
	public void testProcessWhenDirectory() {
		doReturn(FileUtils.TEST_DIR).when(message).getPayload();
		service.processFile(message);
		verifyZeroInteractions(obsService);
		verifyZeroInteractions(publisher);
		verifyZeroInteractions(extractor);
		assertTrue(file.exists());
	}

	/**
	 * Test when extraction failed due to file ignored
	 * 
	 * @throws ObjectStorageException
	 * @throws FilePathException
	 * @throws IgnoredFileException
	 */
	@Test
	public void testProcessWhenExtractionSendFilePathException()
			throws ObjectStorageException, FilePathException, IgnoredFileException {
		FilePathException exc = new FilePathException("product-name", "path", "family", "error message");
		doThrow(exc).when(extractor).extractDescriptor(Mockito.any());

		service.processFile(message);

		verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
		verifyZeroInteractions(obsService);
		verifyZeroInteractions(publisher);
		assertFalse(file.exists());
	}

	/**
	 * Test when extraction failed due to file ignored
	 * 
	 * @throws ObjectStorageException
	 * @throws FilePathException
	 * @throws IgnoredFileException
	 */
	@Test
	public void testProcessWhenExtractionSendIgnoredFileException()
			throws ObjectStorageException, FilePathException, IgnoredFileException {
		IgnoredFileException exc = new IgnoredFileException("product-name", "ignored-name");
		doThrow(exc).when(extractor).extractDescriptor(Mockito.any());

		service.processFile(message);

		verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
		verifyZeroInteractions(obsService);
		verifyZeroInteractions(publisher);
		assertFalse(file.exists());
	}

	@Test
	public void testProcessWhenFileAlreadyExist()
			throws ObjectStorageException, FilePathException, IgnoredFileException {
		FileDescriptor desc = getFileDescriptor(true);
		doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
		doReturn(true).when(obsService).exist(Mockito.anyString());

		service.processFile(message);

		verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
		verify(obsService, times(1)).exist(Mockito.eq(desc.getKeyObjectStorage()));
		verifyZeroInteractions(publisher);
		assertFalse(file.exists());
	}

	@Test
	public void testProcessWhenObsError() throws ObjectStorageException, FilePathException, IgnoredFileException {
		FileDescriptor desc = getFileDescriptor(true);
		doReturn(desc).when(extractor).extractDescriptor(Mockito.any());

		ObjectStorageException exc = new ObjectStorageException("product-name", "key-obs", "bucket",
				new Exception("cause"));
		doThrow(exc).when(obsService).exist(Mockito.anyString());

		service.processFile(message);

		verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
		verify(obsService, times(1)).exist(Mockito.eq(desc.getKeyObjectStorage()));
		verify(obsService, never()).uploadFile(Mockito.anyString(), Mockito.any());
		verifyZeroInteractions(publisher);
		assertTrue(file.exists());
	}

	@Test
	public void testProcessWhenObsError2() throws ObjectStorageException, FilePathException, IgnoredFileException {
		FileDescriptor desc = getFileDescriptor(true);
		doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
		doReturn(false).when(obsService).exist(Mockito.anyString());

		ObjectStorageException exc = new ObjectStorageException("product-name", "key-obs", "bucket",
				new Exception("cause"));
		doThrow(exc).when(obsService).uploadFile(Mockito.anyString(), Mockito.any());

		service.processFile(message);

		verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
		verify(obsService, times(1)).exist(Mockito.eq(desc.getKeyObjectStorage()));
		verify(obsService, times(1)).uploadFile(Mockito.eq(desc.getKeyObjectStorage()), Mockito.eq(file));
		verifyZeroInteractions(publisher);
		assertTrue(file.exists());
	}

	@Test
	public void testProcessWhenNoNeedPublication() throws ObjectStorageException, FilePathException, IgnoredFileException {
		FileDescriptor desc = getFileDescriptor(false);
		doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
		doReturn(false).when(obsService).exist(Mockito.anyString());
		doNothing().when(obsService).uploadFile(Mockito.anyString(), Mockito.any());

		service.processFile(message);

		verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
		verify(obsService, times(1)).exist(Mockito.eq(desc.getKeyObjectStorage()));
		verify(obsService, times(1)).uploadFile(Mockito.eq(desc.getKeyObjectStorage()), Mockito.eq(file));
		verifyZeroInteractions(publisher);
		assertFalse(file.exists());
	}

	@Test
	public void testProcessWhenNeedPublication() throws ObjectStorageException, FilePathException, IgnoredFileException, KafkaSendException {
		FileDescriptor desc = getFileDescriptor(true);
		doReturn(desc).when(extractor).extractDescriptor(Mockito.any());
		doReturn(false).when(obsService).exist(Mockito.anyString());
		doNothing().when(obsService).uploadFile(Mockito.anyString(), Mockito.any());

		service.processFile(message);

		verify(extractor, times(1)).extractDescriptor(Mockito.eq(file));
		verify(obsService, times(1)).exist(Mockito.eq(desc.getKeyObjectStorage()));
		verify(obsService, times(1)).uploadFile(Mockito.eq(desc.getKeyObjectStorage()), Mockito.eq(file));
		verify(publisher, times(1)).send(Mockito.eq(desc.getProductName()));
		assertFalse(file.exists());
	}

	private FileDescriptor getFileDescriptor(boolean published) {
		FileDescriptor desc = new FileDescriptor();
		desc.setProductName("product-name");
		desc.setKeyObjectStorage("key-obs");
		desc.setChannel(15);
		desc.setProductType(EdrsSessionFileType.RAW);
		desc.setMissionId("mission");
		desc.setSatelliteId("sat");
		desc.setHasToBePublished(published);
		return desc;
	}

}

/**
 * Implementation of fileprocessor for tests
 */
class FileProcessorImpl extends AbstractFileProcessor<String> {

	/**
	 * 
	 * @param obsService
	 * @param publisher
	 * @param extractor
	 */
	public FileProcessorImpl(final ObsServices obsService, final PublicationServices<String> publisher,
			final AbstractFileDescriptorService extractor) {
		super(obsService, publisher, extractor);
	}

	/**
	 * 
	 */
	@Override
	protected String buildDto(final FileDescriptor descriptor) {
		return descriptor.getProductName();
	}

}
