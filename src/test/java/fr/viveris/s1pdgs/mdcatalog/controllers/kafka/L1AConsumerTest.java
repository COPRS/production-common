package fr.viveris.s1pdgs.mdcatalog.controllers.kafka;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.viveris.s1pdgs.mdcatalog.config.MetadataExtractorConfig;
import fr.viveris.s1pdgs.mdcatalog.model.L1OutputFileDescriptor;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaL1ADto;
import fr.viveris.s1pdgs.mdcatalog.model.exception.FilePathException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.IgnoredFileException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.MetadataExtractionException;
import fr.viveris.s1pdgs.mdcatalog.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;
import fr.viveris.s1pdgs.mdcatalog.services.files.FileDescriptorBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.files.MetadataBuilder;
import fr.viveris.s1pdgs.mdcatalog.services.s3.L1AcnsS3Services;

public class L1AConsumerTest {

	private static final String LOCAL_DIRECTORY = "test/";

	private static final String TOPIC_NAME = "test/";

	/**
	 * 
	 */
	@Mock
	private MetadataExtractorConfig extractorConfig;

	/**
	 * Elasticsearch services
	 */
	@Mock
	private EsServices esServices;

	/**
	 * Amazon S3 service for configuration files
	 */
	@Mock
	private L1AcnsS3Services s3Services;

	/**
	 * Metadata builder
	 */
	@Mock
	private MetadataBuilder mdBuilder;

	/**
	 * Builder of file descriptors
	 */
	@Mock
	private FileDescriptorBuilder fileDescriptorBuilder;

	/**
	 * Controller to test
	 */
	private L1AConsumer controller;

	/**
	 * 
	 */
	private File fileNoSafe;
	private File fileSafe;
	private File parentFileSafe;

	/**
	 * Initialization
	 * @throws Exception 
	 */
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		doReturn(true).when(s3Services).exist(Mockito.anyString());
		doAnswer(i -> {
			String keyObs = i.getArgument(0);
			if ("file_safe.safe/manifest.safe".equals(keyObs)) {
				return fileSafe;
			}
			return fileNoSafe;
		}).when(s3Services).getFile(Mockito.anyString(), Mockito.anyString());

		doAnswer(i -> {
			L1OutputFileDescriptor desc = new L1OutputFileDescriptor();
			File keyObs = i.getArgument(0);
			if (keyObs.getName().equals("manifest.safe")) {
				desc.setProductName("file_safe.safe");
				desc.setKeyObjectStorage("file_safe.safe/manifest.safe");
			} else {
				desc.setProductName(keyObs.getName());
				desc.setKeyObjectStorage(keyObs.getName());
			}
			return desc;
		}).when(fileDescriptorBuilder).buildL1OutputFileDescriptor(Mockito.any());

		doAnswer(i -> {
			JSONObject desc = new JSONObject();
			File keyObs = i.getArgument(1);
			if (keyObs.getName().equals("manifest.safe")) {
				desc.put("productName", "file_safe.safe");
				desc.put("url", "file_safe.safe/manifest.safe");
			} else {
				desc.put("productName", keyObs.getName());
				desc.put("url", keyObs.getName());
			}
			return desc;
		}).when(mdBuilder).buildL1AcnOutputFileMetadata(Mockito.any(), Mockito.any());
		
		doReturn(false).when(esServices).isMetadataExist(Mockito.any());
		doNothing().when(esServices).createMetadata(Mockito.any());

		fileNoSafe = new File("test/file_no_safe.xml");
		fileNoSafe.createNewFile();
		parentFileSafe = new File("test/file_safe.safe");
		parentFileSafe.mkdir();
		fileSafe = new File("test/file_safe.safe/manifest.safe");
		fileSafe.createNewFile();

		controller = new L1AConsumer(esServices, s3Services, LOCAL_DIRECTORY, extractorConfig,
				fileDescriptorBuilder, mdBuilder, "manifest.safe", ".safe", TOPIC_NAME);
	}

	/**
	 * Cleaning
	 */
	@After
	public void clean() {
		if (fileNoSafe.exists()) {
			fileNoSafe.delete();
		}
		if (fileSafe.exists()) {
			fileSafe.delete();
		}
		if (parentFileSafe.exists()) {
			parentFileSafe.delete();
		}
	}

	/**
	 * Test when object does not exist in obs
	 * 
	 * @throws ObjectStorageException
	 */
	@Test
	public void testReceiveWhenNotExistInOBSWithExc() throws ObjectStorageException {
		doThrow(new ObjectStorageException("product-name", "key-obs", "bucket", new Exception())).when(s3Services)
				.exist(Mockito.anyString());

		controller.receive(new KafkaL1ADto("product-name", "key-obs"));
		verify(s3Services, never()).getFile(Mockito.anyString(), Mockito.anyString());
		verifyZeroInteractions(esServices);
		verifyZeroInteractions(mdBuilder);
		verifyZeroInteractions(fileDescriptorBuilder);
	}

	/**
	 * Test when object does not exist in obs
	 * 
	 * @throws ObjectStorageException
	 */
	@Test
	public void testReceiveWhenNotExistInOBS() throws ObjectStorageException {
		doReturn(false).when(s3Services).exist(Mockito.anyString());

		controller.receive(new KafkaL1ADto("product-name", "key-obs"));
		verify(s3Services, never()).getFile(Mockito.anyString(), Mockito.anyString());
		verifyZeroInteractions(esServices);
		verifyZeroInteractions(mdBuilder);
		verifyZeroInteractions(fileDescriptorBuilder);
	}

	/**
	 * Test when object cannot be retrieved from OBS
	 * 
	 * @throws ObjectStorageException
	 */
	@Test
	public void testReceiveWhenGetFromObsFailed() throws ObjectStorageException {
		doThrow(new ObjectStorageException("product-name", "key-obs", "bucket", new Exception())).when(s3Services)
				.getFile(Mockito.anyString(), Mockito.anyString());

		controller.receive(new KafkaL1ADto("product-name", "key-obs"));
		verify(s3Services, times(1)).exist(Mockito.eq("key-obs"));
		verify(s3Services, times(1)).getFile(Mockito.eq("key-obs"), Mockito.eq(LOCAL_DIRECTORY + "key-obs"));
		verifyZeroInteractions(esServices);
		verifyZeroInteractions(mdBuilder);
		verifyZeroInteractions(fileDescriptorBuilder);
	}

	/**
	 * Test when object cannot be retrieved from OBS
	 * 
	 * @throws ObjectStorageException
	 * @throws IgnoredFileException
	 * @throws FilePathException
	 */
	@Test
	public void testReceiveWhenBuildDescriptorFailed()
			throws ObjectStorageException, FilePathException, IgnoredFileException {
		doThrow(new IgnoredFileException("product-name", "ignored-name")).when(fileDescriptorBuilder)
				.buildL1OutputFileDescriptor(Mockito.any());

		controller.receive(new KafkaL1ADto("product-name", "file_no_safe.xml"));
		verify(s3Services, times(1)).exist(Mockito.eq("file_no_safe.xml"));
		verify(s3Services, times(1)).getFile(Mockito.eq("file_no_safe.xml"),
				Mockito.eq(LOCAL_DIRECTORY + "file_no_safe.xml"));
		verify(fileDescriptorBuilder, times(1)).buildL1OutputFileDescriptor(Mockito.eq(fileNoSafe));
		verifyZeroInteractions(esServices);
		verifyZeroInteractions(mdBuilder);
		assertFalse(fileNoSafe.exists());
	}

	/**
	 * Test when the metdata extraction failed
	 * 
	 * @throws ObjectStorageException
	 * @throws IgnoredFileException
	 * @throws FilePathException
	 * @throws MetadataExtractionException 
	 */
	@Test
	public void testReceiveWhenExtractionFailed()
			throws ObjectStorageException, FilePathException, IgnoredFileException, MetadataExtractionException {
		doThrow(new MetadataExtractionException("product-name", new Exception("erro"))).when(mdBuilder)
				.buildL1AcnOutputFileMetadata(Mockito.any(), Mockito.any());
		L1OutputFileDescriptor desc = new L1OutputFileDescriptor();
		desc.setKeyObjectStorage("file_no_safe.xml");
		desc.setProductName("file_no_safe.xml");

		controller.receive(new KafkaL1ADto("product-name", "file_no_safe.xml"));
		verify(s3Services, times(1)).exist(Mockito.eq("file_no_safe.xml"));
		verify(s3Services, times(1)).getFile(Mockito.eq("file_no_safe.xml"),
				Mockito.eq(LOCAL_DIRECTORY + "file_no_safe.xml"));
		verify(fileDescriptorBuilder, times(1)).buildL1OutputFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildL1AcnOutputFileMetadata(Mockito.eq(desc), Mockito.eq(fileNoSafe));
		verifyZeroInteractions(esServices);
		assertFalse(fileNoSafe.exists());
	}

	/**
	 * Test when the metdata extraction failed
	 * @throws Exception 
	 */
	@Test
	public void testReceiveWhenMetadataAlreadyExist()
			throws Exception {
		doReturn(true).when(esServices).isMetadataExist(Mockito.any());
		L1OutputFileDescriptor desc = new L1OutputFileDescriptor();
		desc.setKeyObjectStorage("file_no_safe.xml");
		desc.setProductName("file_no_safe.xml");
		JSONObject metadata = new JSONObject();
		metadata.put("productName", "file_no_safe.xml");
		metadata.put("url", "file_no_safe.xml");

		controller.receive(new KafkaL1ADto("product-name", "file_no_safe.xml"));
		verify(s3Services, times(1)).exist(Mockito.eq("file_no_safe.xml"));
		verify(s3Services, times(1)).getFile(Mockito.eq("file_no_safe.xml"),
				Mockito.eq(LOCAL_DIRECTORY + "file_no_safe.xml"));
		verify(fileDescriptorBuilder, times(1)).buildL1OutputFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildL1AcnOutputFileMetadata(Mockito.eq(desc), Mockito.eq(fileNoSafe));
		verify(esServices, times(1)).isMetadataExist(Mockito.any());
		verify(esServices, never()).createMetadata(Mockito.any());
		assertFalse(fileNoSafe.exists());
	}

	/**
	 * Test when the metdata extraction failed
	 * @throws Exception 
	 */
	@Test
	public void testReceiveWhenMetadataCreationFailed()
			throws Exception {
		doThrow(new Exception("error")).when(esServices).createMetadata(Mockito.any());
		
		controller.receive(new KafkaL1ADto("product-name", "file_no_safe.xml"));
		verify(s3Services, times(1)).exist(Mockito.eq("file_no_safe.xml"));
		verify(s3Services, times(1)).getFile(Mockito.eq("file_no_safe.xml"),
				Mockito.eq(LOCAL_DIRECTORY + "file_no_safe.xml"));
		verify(fileDescriptorBuilder, times(1)).buildL1OutputFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildL1AcnOutputFileMetadata(Mockito.any(), Mockito.eq(fileNoSafe));
		verify(esServices, times(1)).isMetadataExist(Mockito.any());
		verify(esServices, times(1)).createMetadata(Mockito.any());
		assertFalse(fileNoSafe.exists());
	}

	/**
	 * Test when the metdata extraction failed
	 * @throws Exception 
	 */
	@Test
	public void testReceiveNoSafe()
			throws Exception {
		L1OutputFileDescriptor desc = new L1OutputFileDescriptor();
		desc.setKeyObjectStorage("file_no_safe.xml");
		desc.setProductName("file_no_safe.xml");
		JSONObject metadata = new JSONObject();
		metadata.put("productName", "file_no_safe.xml");
		metadata.put("url", "file_no_safe.xml");
		assertTrue(fileNoSafe.exists());

		controller.receive(new KafkaL1ADto("product-name", "file_no_safe.xml"));
		verify(s3Services, times(1)).exist(Mockito.eq("file_no_safe.xml"));
		verify(s3Services, times(1)).getFile(Mockito.eq("file_no_safe.xml"),
				Mockito.eq(LOCAL_DIRECTORY + "file_no_safe.xml"));
		verify(fileDescriptorBuilder, times(1)).buildL1OutputFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildL1AcnOutputFileMetadata(Mockito.eq(desc), Mockito.eq(fileNoSafe));
		verify(esServices, times(1)).isMetadataExist(Mockito.any());
		verify(esServices, times(1)).createMetadata(Mockito.any());
		assertFalse(fileNoSafe.exists());
	}

	/**
	 * Test when the metdata extraction failed
	 * @throws Exception 
	 */
	@Test
	public void testReceiveSafe()
			throws Exception {
		L1OutputFileDescriptor desc = new L1OutputFileDescriptor();
		desc.setKeyObjectStorage("file_safe.safe/manifest.safe");
		desc.setProductName("file_safe.safe");
		JSONObject metadata = new JSONObject();
		metadata.put("productName", "file_safe.safe");
		metadata.put("url", "file_safe.safe/manifest.safe");
		assertTrue(fileSafe.exists());

		controller.receive(new KafkaL1ADto("product-name", "file_safe.safe"));
		verify(s3Services, times(1)).exist(Mockito.eq("file_safe.safe/manifest.safe"));
		verify(s3Services, times(1)).getFile(Mockito.eq("file_safe.safe/manifest.safe"),
				Mockito.eq(LOCAL_DIRECTORY + "file_safe.safe/manifest.safe"));
		verify(fileDescriptorBuilder, times(1)).buildL1OutputFileDescriptor(Mockito.eq(fileSafe));
		verify(mdBuilder, times(1)).buildL1AcnOutputFileMetadata(Mockito.eq(desc), Mockito.eq(fileSafe));
		verify(esServices, times(1)).isMetadataExist(Mockito.any());
		verify(esServices, times(1)).createMetadata(Mockito.any());
		assertFalse(fileSafe.exists());
	}
	
	/**
	 * Test the file deletion
	 */
	@Test
	public void testDeleteFile() {
		controller.deleteFile(null, "log");
		
		controller.deleteFile(fileNoSafe, "log");
		assertFalse(fileNoSafe.exists());
		
		controller.deleteFile(fileSafe, "log");
		assertFalse(fileSafe.exists());
		assertFalse(parentFileSafe.exists());
	}

}
