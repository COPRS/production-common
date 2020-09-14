package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.mqi.OutputProcuderFactory;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class GhostTest {
	@Mock
	private ObsClient obsClient;
	@Mock
	private OutputProcuderFactory procuderFactory;

	private OutputProcessor processor;

	@Before
	public void setup() {
		final GenericMessageDto<IpfExecutionJob> inputMessage = new GenericMessageDto<IpfExecutionJob>(123, "",
				new IpfExecutionJob(ProductFamily.L0_JOB, "product-name", "FAST24", "", "job-order", "FAST24", new UUID(23L, 42L)));
		
		final ApplicationProperties properties = new ApplicationProperties();
		properties.setThresholdEw(2);
		properties.setThresholdIw(2);
		properties.setThresholdSm(2);
		properties.setThresholdWv(30);

		processor = new OutputProcessor(obsClient, procuderFactory, inputMessage, "outputs.list", 2, "MONITOR",
				ApplicationLevel.L0, properties);
	}

	@Test
	public void testInvalidProductName() {
		assertEquals(false, processor.isGhostCandidate("S1A_IW_RAW__IAMINVALID"));
		assertEquals(false,
				processor.isGhostCandidate("S1A_IW_RAW__0SVV_XXXX0810T225025_XXXX810T225412_028513_033938_569F"));

		assertEquals(false,
				processor.isGhostCandidate("S1A_FB_RAW__0SVV_20190810T225025_20190810T225412_028513_033938_569F"));
	}

	@Test
	public void testLegacyCompatibility() {
		assertEquals(false,
				processor.isGhostCandidate("S1A_IW_RAW__0SVV_20190810T225025_20190810T225412_028513_033938_569F"));
		assertEquals(false,
				processor.isGhostCandidate("S1A_IW_RAW__0SVH_20190810T225025_20190810T225412_028513_033938_F24F"));
		assertEquals(true,
				processor.isGhostCandidate("S1A_IW_RAW__0SVH_20190810T225412_20190810T225412_028513_033938_271A"));
		assertEquals(true,
				processor.isGhostCandidate("S1A_IW_RAW__0SVV_20190810T225412_20190810T225412_028513_033938_CFA2"));
	}
	
	@Test
	public void testSMAquisitionMode() {
		assertEquals(true,
				processor.isGhostCandidate("S1B_S6_RAW__0SHH_20181001T065647_20181001T065649_012955_017ED9_79B7.SAFE"));
		assertEquals(false,
				processor.isGhostCandidate("S1B_S7_RAW__0SHH_20181001T065647_20181001T065649_012955_017ED9_79B7.SAFE"));
		
	}
}
