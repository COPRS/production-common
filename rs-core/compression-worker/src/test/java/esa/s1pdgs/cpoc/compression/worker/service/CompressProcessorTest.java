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
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.compression.worker.config.CompressionWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class CompressProcessorTest {

	private CompressProcessor uut;

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
		properties.setCompressionCommand("echo");
		uut = new CompressProcessor(appStatus, properties, obsClient);
	}

	@Test
	public final void onMessage_compress() throws IOException {

		CatalogEvent event = new CatalogEvent();
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

}
