package esa.s1pdgs.cpoc.compression.worker.service;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.compression.worker.config.CompressionWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class CompressProcessorTest {

	private CompressProcessor uut;
	
	@Mock
	CommonConfigurationProperties commonProps;

	@Mock
	private AppStatus appStatus;

	@Mock
	private ObsClient obsClient;

	private Path tmpWorkdir;
	
	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);
		CompressionWorkerConfigurationProperties properties = new CompressionWorkerConfigurationProperties();
		tmpWorkdir = Files.createTempDirectory("compressprocessortest");
		properties.setWorkingDirectory(tmpWorkdir.toAbsolutePath().toString());
//		properties.setSizeBatchDownload(1000);
		properties.setCompressionTimeout(5);
		properties.getCompressionCommand().put("s1","echo");
		properties.getCompressionCommand().put("s2","echo");
		properties.getCompressionCommand().put("s3","echo");

		uut = new CompressProcessor(commonProps, appStatus, properties, obsClient);
	}

	@Test
	public final void onMessage_compress_s3() throws IOException {

		CatalogEvent event = new CatalogEvent();
		event.setMissionId("S3");
		event.setProductFamily(ProductFamily.L1_SLICE);
		event.setUid(UUID.randomUUID());
		event.setKeyObjectStorage("S3l1");
		
		Path filedir = Files.createDirectory(tmpWorkdir.resolve("S3l1.zip"));
		Files.createFile(filedir.resolve("S3l1.zip"));

		try {
			uut.apply(event);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public final void onMessage_compress_s2() throws IOException {

		CatalogEvent event = new CatalogEvent();
		event.setMissionId("S2");
		event.setProductFamily(ProductFamily.S2_L0_DS);
		event.setUid(UUID.randomUUID());
		event.setKeyObjectStorage("S2l0");
		
		Path filedir = Files.createDirectory(tmpWorkdir.resolve("S2l0.tar"));
		Files.createFile(filedir.resolve("S2l0.tar"));

		try {
			uut.apply(event);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public final void onMessage_compress_s2_jp2() throws IOException {

		CatalogEvent event = new CatalogEvent();
		event.setMissionId("S2");
		event.setProductFamily(ProductFamily.S2_L1C_TC);
		event.setUid(UUID.randomUUID());
		event.setKeyObjectStorage("S2_L1C.jp2");
		
		Path filedir = Files.createDirectory(tmpWorkdir.resolve("S2_L1C.jp2"));
		Files.createFile(filedir.resolve("S2_L1C.jp2"));

		try {
			uut.apply(event);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}

}
