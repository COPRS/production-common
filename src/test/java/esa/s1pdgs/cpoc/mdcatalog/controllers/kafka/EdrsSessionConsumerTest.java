package esa.s1pdgs.cpoc.mdcatalog.controllers.kafka;

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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.mdcatalog.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.mdcatalog.controllers.kafka.EdrsSessionFileConsumer;
import esa.s1pdgs.cpoc.mdcatalog.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdcatalog.model.EdrsSessionFileType;
import esa.s1pdgs.cpoc.mdcatalog.model.dto.KafkaEdrsSessionDto;
import esa.s1pdgs.cpoc.mdcatalog.model.exception.FilePathException;
import esa.s1pdgs.cpoc.mdcatalog.model.exception.IgnoredFileException;
import esa.s1pdgs.cpoc.mdcatalog.model.exception.IllegalFileExtension;
import esa.s1pdgs.cpoc.mdcatalog.model.exception.MetadataExtractionException;
import esa.s1pdgs.cpoc.mdcatalog.model.exception.ObjectStorageException;
import esa.s1pdgs.cpoc.mdcatalog.services.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.services.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdcatalog.services.files.MetadataBuilder;

public class EdrsSessionConsumerTest {

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
	private EdrsSessionFileConsumer controller;

	/**
	 * 
	 */
	private File fileNoSafe;

	/**
	 * Initialization
	 * 
	 * @throws Exception
	 */
	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		doAnswer(i -> {
			EdrsSessionFileDescriptor desc = new EdrsSessionFileDescriptor();
			File keyObs = i.getArgument(0);
			desc.setKeyObjectStorage(keyObs.getName());
			return desc;
		}).when(fileDescriptorBuilder).buildEdrsSessionFileDescriptor(Mockito.any());

		doAnswer(i -> {
			JSONObject desc = new JSONObject();
			EdrsSessionFileDescriptor file = i.getArgument(0);
			desc.put("url", file.getKeyObjectStorage());
			return desc;
		}).when(mdBuilder).buildEdrsSessionFileMetadata(Mockito.any());

		doReturn(false).when(esServices).isMetadataExist(Mockito.any());
		doNothing().when(esServices).createMetadata(Mockito.any());

		fileNoSafe = new File("test/file_no_safe.xml");

		controller = new EdrsSessionFileConsumer(esServices, LOCAL_DIRECTORY, extractorConfig, TOPIC_NAME,
				fileDescriptorBuilder, mdBuilder);
	}

	/**
	 * Test when object cannot be retrieved from OBS
	 * 
	 * @throws ObjectStorageException
	 * @throws IgnoredFileException
	 * @throws FilePathException
	 * @throws IllegalFileExtension 
	 */
	@Test
	public void testReceiveWhenBuildDescriptorFailed()
			throws ObjectStorageException, FilePathException, IgnoredFileException, IllegalFileExtension {
		doThrow(new IgnoredFileException("product-name", "ignored-name")).when(fileDescriptorBuilder)
				.buildEdrsSessionFileDescriptor(Mockito.any());

		controller.receive(new KafkaEdrsSessionDto("file_no_safe.xml", 1, EdrsSessionFileType.RAW));
		verify(fileDescriptorBuilder, times(1)).buildEdrsSessionFileDescriptor(Mockito.eq(fileNoSafe));
		verifyZeroInteractions(esServices);
		verifyZeroInteractions(mdBuilder);
	}

	/**
	 * Test when the metdata extraction failed
	 * 
	 * @throws ObjectStorageException
	 * @throws IgnoredFileException
	 * @throws FilePathException
	 * @throws MetadataExtractionException
	 * @throws IllegalFileExtension 
	 */
	@Test
	public void testReceiveWhenExtractionFailed()
			throws ObjectStorageException, FilePathException, IgnoredFileException, MetadataExtractionException, IllegalFileExtension {
		doThrow(new MetadataExtractionException("product-name", new Exception("erro"))).when(mdBuilder)
				.buildEdrsSessionFileMetadata(Mockito.any());
		EdrsSessionFileDescriptor desc = new EdrsSessionFileDescriptor();
		desc.setKeyObjectStorage("file_no_safe.xml");

		controller.receive(new KafkaEdrsSessionDto("file_no_safe.xml", 1, EdrsSessionFileType.RAW));
		verify(fileDescriptorBuilder, times(1)).buildEdrsSessionFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildEdrsSessionFileMetadata(Mockito.eq(desc));
		verifyZeroInteractions(esServices);
	}

	/**
	 * Test when the metdata extraction failed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveWhenMetadataAlreadyExist() throws Exception {
		doReturn(true).when(esServices).isMetadataExist(Mockito.any());
		EdrsSessionFileDescriptor desc = new EdrsSessionFileDescriptor();
		desc.setKeyObjectStorage("file_no_safe.xml");
		JSONObject metadata = new JSONObject();
		metadata.put("url", "file_no_safe.xml");

		controller.receive(new KafkaEdrsSessionDto("file_no_safe.xml", 1, EdrsSessionFileType.RAW));
		verify(fileDescriptorBuilder, times(1)).buildEdrsSessionFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildEdrsSessionFileMetadata(Mockito.eq(desc));
		verify(esServices, times(1)).isMetadataExist(Mockito.any());
		verify(esServices, never()).createMetadata(Mockito.any());
	}

	/**
	 * Test when the metdata extraction failed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveWhenMetadataCreationFailed() throws Exception {
		doThrow(new Exception("error")).when(esServices).createMetadata(Mockito.any());

		controller.receive(new KafkaEdrsSessionDto("file_no_safe.xml", 1, EdrsSessionFileType.RAW));
		verify(fileDescriptorBuilder, times(1)).buildEdrsSessionFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildEdrsSessionFileMetadata(Mockito.any());
		verify(esServices, times(1)).isMetadataExist(Mockito.any());
		verify(esServices, times(1)).createMetadata(Mockito.any());
	}

	/**
	 * Test when the metdata extraction failed
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceive() throws Exception {
		EdrsSessionFileDescriptor desc = new EdrsSessionFileDescriptor();
		desc.setKeyObjectStorage("file_no_safe.xml");
		JSONObject metadata = new JSONObject();
		metadata.put("productName", "file_no_safe.xml");

		controller.receive(new KafkaEdrsSessionDto("file_no_safe.xml", 1, EdrsSessionFileType.RAW));
		verify(fileDescriptorBuilder, times(1)).buildEdrsSessionFileDescriptor(Mockito.eq(fileNoSafe));
		verify(mdBuilder, times(1)).buildEdrsSessionFileMetadata(Mockito.eq(desc));
		verify(esServices, times(1)).isMetadataExist(Mockito.any());
		verify(esServices, times(1)).createMetadata(Mockito.any());
	}

}
