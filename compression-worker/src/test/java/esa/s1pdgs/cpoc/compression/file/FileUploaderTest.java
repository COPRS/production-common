package esa.s1pdgs.cpoc.compression.file;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
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

		GenericMessageDto<ProductionEvent> inputMessage = new GenericMessageDto<ProductionEvent>(123, "",
				new ProductionEvent("product_name","object_key", ProductFamily.L0_ACN));

		fileUploader = new FileUploader(obsClient, producerFactory, "/tmp/compressed", inputMessage,
				inputMessage.getBody());
	}

	@Test
	public void testProductFamily() {
		 assertEquals(fileUploader.getCompressedProductFamily(ProductFamily.L0_ACN), ProductFamily.L0_ACN_ZIP);
		 assertEquals(fileUploader.getCompressedProductFamily(ProductFamily.L1_ACN), ProductFamily.L1_ACN_ZIP);
		 assertEquals(fileUploader.getCompressedProductFamily(ProductFamily.L2_ACN), ProductFamily.L2_ACN_ZIP);
		 assertEquals(fileUploader.getCompressedProductFamily(ProductFamily.L0_SLICE), ProductFamily.L0_SLICE_ZIP);
		 assertEquals(fileUploader.getCompressedProductFamily(ProductFamily.L1_SLICE), ProductFamily.L1_SLICE_ZIP);
		 assertEquals(fileUploader.getCompressedProductFamily(ProductFamily.L2_SLICE), ProductFamily.L2_SLICE_ZIP);
	}
}
