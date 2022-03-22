package esa.s1pdgs.cpoc.compression.worker.file;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.worker.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionDirection;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class FileUploaderTest {
	@Mock
	private ObsClient obsClient;

	@Mock
	private OutputProducerFactory producerFactory;

	private FileUploader fileUploader;

	@Before
	public void init() throws AbstractCodedException {
		// Init mocks
		MockitoAnnotations.initMocks(this);

		final GenericMessageDto<CompressionJob> inputMessage = new GenericMessageDto<CompressionJob>(123, "",
				new CompressionJob("input_key", ProductFamily.L0_ACN, "input_key.zip", ProductFamily.L0_ACN_ZIP, CompressionDirection.COMPRESS));

		fileUploader = new FileUploader(obsClient, producerFactory, "/tmp/compressed", inputMessage,
				inputMessage.getBody(), UUID.randomUUID());
	}

	@Test
	public void testProductFamily() {

	}
}
