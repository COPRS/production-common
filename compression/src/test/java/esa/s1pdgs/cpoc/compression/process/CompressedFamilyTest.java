package esa.s1pdgs.cpoc.compression.process;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.mqi.OutputProducerFactory;
import esa.s1pdgs.cpoc.compression.obs.ObsService;
import esa.s1pdgs.cpoc.compression.test.MockPropertiesTest;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJobDto;

public class CompressedFamilyTest extends MockPropertiesTest {
	@Mock
	private ObsService obsService;

	@Mock
	private GenericMqiService<CompressionJobDto> mqiService;
	
	@Mock
	private OutputProducerFactory producerFactory;

	private CompressProcessor processor;

	@Before
	public void init() throws AbstractCodedException {
		MockitoAnnotations.initMocks(this);

		mockDefaultAppProperties();
		mockDefaultDevProperties();
		mockDefaultStatus();

//		inputMessage = new GenericMessageDto<CompressionJobDto>(123, "", TestUtils.buildL0LevelJobDto());
//		workingDir = new File(inputMessage.getBody().getWorkDirectory());
//		if (!workingDir.exists()) {
//			workingDir.mkdir();
//		}
//		// TODO FIX
		processor = new CompressProcessor(appStatus, properties, obsService, producerFactory, mqiService,
				mqiStatusService);
//
//		procExecutorSrv = Executors.newSingleThreadExecutor();
//		procCompletionSrv = new ExecutorCompletionService<>(procExecutorSrv);
	}

	@Test
	public void test() {
		ProductFamily family = ProductFamily.L0_ACN;

//		CompressProcessor proc = new CompressProcessor(, properties, obsService, producerFactory, mqiService, mqiStatusService)
	}
}
