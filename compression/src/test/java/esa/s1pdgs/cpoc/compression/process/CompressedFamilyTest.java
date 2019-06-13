package esa.s1pdgs.cpoc.compression.process;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.obs.ObsService;
import esa.s1pdgs.cpoc.compression.test.MockPropertiesTest;

public class CompressedFamilyTest extends MockPropertiesTest {
	@Mock
	private ObsService obsService;

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
////      processor = new CompressProcessor(appStatus, properties,
////              obsService,mqiService, mqiStatusService);
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
