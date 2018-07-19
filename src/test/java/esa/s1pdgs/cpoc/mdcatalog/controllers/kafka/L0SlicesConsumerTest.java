package esa.s1pdgs.cpoc.mdcatalog.controllers.kafka;

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

import esa.s1pdgs.cpoc.mdcatalog.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.mdcatalog.controllers.kafka.L0SlicesConsumer;
import esa.s1pdgs.cpoc.mdcatalog.model.L0OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.model.dto.KafkaL0SliceDto;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObject;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataExtractionException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataFilePathException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIgnoredFileException;
import esa.s1pdgs.cpoc.mdcatalog.services.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.services.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdcatalog.services.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdcatalog.services.s3.ObsService;

public class L0SlicesConsumerTest {

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
	private ObsService s3Services;

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
	private L0SlicesConsumer controller;

	/**
	 * 
	 */
	private File fileNoSafe;
	private File fileSafe;
	private File parentFileSafe;

	/**
	 * Initialization
	 * 
	 * @throws Exception
	 */
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		doAnswer(i -> {
			String keyObs = i.getArgument(1);
			if ("file_safe.safe/manifest.safe".equals(keyObs)) {
				return fileSafe;
			}
			return fileNoSafe;
		}).when(s3Services).downloadFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());

		doAnswer(i -> {
			L0OutputFileDescriptor desc = new L0OutputFileDescriptor();
			File keyObs = i.getArgument(0);
			if (keyObs.getName().equals("manifest.safe")) {
				desc.setProductName("file_safe.safe");
				desc.setKeyObjectStorage("file_safe.safe/manifest.safe");
			} else {
				desc.setProductName(keyObs.getName());
				desc.setKeyObjectStorage(keyObs.getName());
			}
			return desc;
		}).when(fileDescriptorBuilder).buildL0OutputFileDescriptor(Mockito.any());

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
		}).when(mdBuilder).buildL0SliceOutputFileMetadata(Mockito.any(), Mockito.any());

		doReturn(false).when(esServices).isMetadataExist(Mockito.any());
		doNothing().when(esServices).createMetadata(Mockito.any());

		fileNoSafe = new File("test/file_no_safe.xml");
		fileNoSafe.createNewFile();
		parentFileSafe = new File("test/file_safe.safe");
		parentFileSafe.mkdir();
		fileSafe = new File("test/file_safe.safe/manifest.safe");
		fileSafe.createNewFile();

		controller = new L0SlicesConsumer(esServices, s3Services, LOCAL_DIRECTORY, extractorConfig,
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
	 * @throws ObsUnknownObjectException 
	 */
	@Test
	public void testReceiveWhenNotExistInOBS() throws ObsException, ObsUnknownObject {
		doThrow(new ObsUnknownObject(ProductFamily.L0_PRODUCT, "key-obs")).when(s3Services)
				.downloadFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());

		controller.receive(new KafkaL0SliceDto("product-name", "key-obs"));
		verify(s3Services, times(1)).downloadFile(Mockito.eq(ProductFamily.L0_PRODUCT), Mockito.eq("key-obs"),
				Mockito.eq(LOCAL_DIRECTORY));
		verifyZeroInteractions(esServices);
		verifyZeroInteractions(mdBuilder);
		verifyZeroInteractions(fileDescriptorBuilder);
	}

	/**
	 * Test when object cannot be retrieved from OBS
	 * 
	 * @throws ObjectStorageException
	 * @throws ObsUnknownObjectException
	 */
	@Test
	public void testReceiveWhenGetFromObsFailed() throws ObsException, ObsUnknownObject {
		doThrow(new ObsException(ProductFamily.L0_PRODUCT, "key-obs", new Exception())).when(s3Services)
				.downloadFile(Mockito.any(), Mockito.anyString(), Mockito.anyString());

		controller.receive(new KafkaL0SliceDto("product-name", "key-obs"));
		verify(s3Services, times(1)).downloadFile(Mockito.eq(ProductFamily.L0_PRODUCT), Mockito.eq("key-obs"),
				Mockito.eq(LOCAL_DIRECTORY));
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
	 * @throws ObsUnknownObjectException
	 */
	@Test
	public void testReceiveWhenBuildDescriptorFailed()
			throws ObsException, MetadataFilePathException, MetadataIgnoredFileException, ObsUnknownObject {
		doThrow(new MetadataIgnoredFileException("ignored-name")).when(fileDescriptorBuilder)
				.buildL0OutputFileDescriptor(Mockito.any());

		controller.receive(new KafkaL0SliceDto("product-name", "file_no_safe.xml"));
		verify(s3Services, times(1)).downloadFile(Mockito.eq(ProductFamily.L0_PRODUCT), Mockito.eq("file_no_safe.xml"),
				Mockito.eq(LOCAL_DIRECTORY));
		verify(fileDescriptorBuilder, times(1)).buildL0OutputFileDescriptor(Mockito.eq(fileNoSafe));
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
	 * @throws ObsUnknownObjectException
	 */
	@Test
	public void testReceiveWhenExtractionFailed() throws ObsException, MetadataFilePathException,
			MetadataIgnoredFileException, MetadataExtractionException, ObsUnknownObject {
		doThrow(new MetadataExtractionException(new Exception("erro"))).when(mdBuilder)
				.buildL0SliceOutputFileMetadata(Mockito.any(), Mockito.any());
		L0OutputFileDescriptor desc = new L0OutputFileDescriptor();
		desc.setKeyObjectStorage("file_no_safe.xml");
		desc.setProductName("file_no_safe.xml");

		controller.receive(new KafkaL0SliceDto("product-name", "file_no_safe.xml"));
		verify(s3Services, times(1)).downloadFile(Mockito.eq(ProductFamily.L0_PRODUCT), Mockito.eq("file_no_safe.xml"),
				Mockito.eq(LOCAL_DIRECTORY));
		verify(fileDescriptorBuilder, times(1)).buildL0OutputFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildL0SliceOutputFileMetadata(Mockito.eq(desc), Mockito.eq(fileNoSafe));
		verifyZeroInteractions(esServices);
		assertFalse(fileNoSafe.exists());
	}

	/**
	 * Test when the metdata extraction failed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveWhenMetadataAlreadyExist() throws Exception {
		doReturn(true).when(esServices).isMetadataExist(Mockito.any());
		L0OutputFileDescriptor desc = new L0OutputFileDescriptor();
		desc.setKeyObjectStorage("file_no_safe.xml");
		desc.setProductName("file_no_safe.xml");
		JSONObject metadata = new JSONObject();
		metadata.put("productName", "file_no_safe.xml");
		metadata.put("url", "file_no_safe.xml");

		controller.receive(new KafkaL0SliceDto("product-name", "file_no_safe.xml"));
		verify(s3Services, times(1)).downloadFile(Mockito.eq(ProductFamily.L0_PRODUCT), Mockito.eq("file_no_safe.xml"),
				Mockito.eq(LOCAL_DIRECTORY));
		verify(fileDescriptorBuilder, times(1)).buildL0OutputFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildL0SliceOutputFileMetadata(Mockito.eq(desc), Mockito.eq(fileNoSafe));
		verify(esServices, times(1)).isMetadataExist(Mockito.any());
		verify(esServices, never()).createMetadata(Mockito.any());
		assertFalse(fileNoSafe.exists());
	}

	/**
	 * Test when the metdata extraction failed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveWhenMetadataCreationFailed() throws Exception {
		doThrow(new Exception("error")).when(esServices).createMetadata(Mockito.any());

		controller.receive(new KafkaL0SliceDto("product-name", "file_no_safe.xml"));
		verify(s3Services, times(1)).downloadFile(Mockito.eq(ProductFamily.L0_PRODUCT), Mockito.eq("file_no_safe.xml"),
				Mockito.eq(LOCAL_DIRECTORY));
		verify(fileDescriptorBuilder, times(1)).buildL0OutputFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildL0SliceOutputFileMetadata(Mockito.any(), Mockito.eq(fileNoSafe));
		verify(esServices, times(1)).isMetadataExist(Mockito.any());
		verify(esServices, times(1)).createMetadata(Mockito.any());
		assertFalse(fileNoSafe.exists());
	}

	/**
	 * Test when the metdata extraction failed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveNoSafe() throws Exception {
		L0OutputFileDescriptor desc = new L0OutputFileDescriptor();
		desc.setKeyObjectStorage("file_no_safe.xml");
		desc.setProductName("file_no_safe.xml");
		JSONObject metadata = new JSONObject();
		metadata.put("productName", "file_no_safe.xml");
		metadata.put("url", "file_no_safe.xml");
		assertTrue(fileNoSafe.exists());

		controller.receive(new KafkaL0SliceDto("product-name", "file_no_safe.xml"));
		verify(s3Services, times(1)).downloadFile(Mockito.eq(ProductFamily.L0_PRODUCT), Mockito.eq("file_no_safe.xml"),
				Mockito.eq(LOCAL_DIRECTORY));
		verify(fileDescriptorBuilder, times(1)).buildL0OutputFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildL0SliceOutputFileMetadata(Mockito.eq(desc), Mockito.eq(fileNoSafe));
		verify(esServices, times(1)).isMetadataExist(Mockito.any());
		verify(esServices, times(1)).createMetadata(Mockito.any());
		assertFalse(fileNoSafe.exists());
	}

	/**
	 * Test when the metdata extraction failed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveSafe() throws Exception {
		L0OutputFileDescriptor desc = new L0OutputFileDescriptor();
		desc.setKeyObjectStorage("file_safe.safe/manifest.safe");
		desc.setProductName("file_safe.safe");
		JSONObject metadata = new JSONObject();
		metadata.put("productName", "file_safe.safe");
		metadata.put("url", "file_safe.safe/manifest.safe");
		assertTrue(fileSafe.exists());

		controller.receive(new KafkaL0SliceDto("product-name", "file_safe.safe"));
		verify(s3Services, times(1)).downloadFile(Mockito.eq(ProductFamily.L0_PRODUCT),
				Mockito.eq("file_safe.safe/manifest.safe"), Mockito.eq(LOCAL_DIRECTORY));
		verify(fileDescriptorBuilder, times(1)).buildL0OutputFileDescriptor(Mockito.eq(fileSafe));
		verify(mdBuilder, times(1)).buildL0SliceOutputFileMetadata(Mockito.eq(desc), Mockito.eq(fileSafe));
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
