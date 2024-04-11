/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ipf.execution.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

public class GhostTest {
	@Mock
	private ObsClient obsClient;

	private OutputProcessor processor;

	@Before
	public void setup() {
		final IpfExecutionJob inputMessage = 
				new IpfExecutionJob(ProductFamily.L0_JOB, "product-name", "FAST24", "", "job-order", "FAST24", new UUID(23L, 42L));
		
		final ApplicationProperties properties = new ApplicationProperties();
		properties.setThresholdEw(2);
		properties.setThresholdIw(2);
		properties.setThresholdSm(2);
		properties.setThresholdWv(30);
		
		final CommonConfigurationProperties commonProperties = new CommonConfigurationProperties();
		commonProperties.setRsChainName("test-chain");
		commonProperties.setRsChainVersion("0.0.1");

		processor = new OutputProcessor(obsClient, inputMessage, "outputs.list", 2, "MONITOR",
				ApplicationLevel.L0, properties, commonProperties);
	}

	private final boolean isGhostCandidate(final String name) {
		return processor.isGhostCandidate(new File(name));
	}
	
	@Test
	public void testInvalidProductName() {
		assertEquals(false, isGhostCandidate("S1A_IW_RAW__IAMINVALID"));
		assertEquals(false,
				isGhostCandidate("S1A_IW_RAW__0SVV_XXXX0810T225025_XXXX810T225412_028513_033938_569F"));

		assertEquals(false,
				isGhostCandidate("S1A_FB_RAW__0SVV_20190810T225025_20190810T225412_028513_033938_569F"));
	}

	@Test
	public void testLegacyCompatibility() {
		assertEquals(false,
				isGhostCandidate("S1A_IW_RAW__0SVV_20190810T225025_20190810T225412_028513_033938_569F"));
		assertEquals(false,
				isGhostCandidate("S1A_IW_RAW__0SVH_20190810T225025_20190810T225412_028513_033938_F24F"));
		assertEquals(true,
				isGhostCandidate("S1A_IW_RAW__0SVH_20190810T225412_20190810T225412_028513_033938_271A"));
		assertEquals(true,
				isGhostCandidate("S1A_IW_RAW__0SVV_20190810T225412_20190810T225412_028513_033938_CFA2"));
	}
	
	@Test
	public void testSMAquisitionMode() {
		assertEquals(true,
				isGhostCandidate("S1B_S6_RAW__0SHH_20181001T065647_20181001T065649_012955_017ED9_79B7.SAFE"));
		assertEquals(false,
				isGhostCandidate("S1B_S7_RAW__0SHH_20181001T065647_20181001T065649_012955_017ED9_79B7.SAFE"));
		
	}
}
