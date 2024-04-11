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

package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;

@Ignore
@RunWith(SpringRunner.class)
@EnableConfigurationProperties(value = ObsConfigurationProperties.class)
@PropertySource({"classpath:obs-aws-s3.properties"})
public class ObsConfigurationPropertiesTest {
	
	@Autowired
	private ObsConfigurationProperties uut;
	
	@Test
	public final void testConfigurationLoading() {
		// only spot check some configurations
		assertEquals("http://storage.gra.cloud.ovh.net", uut.getEndpoint());
		assertEquals("werum-ut-session-files", uut.getBucket().get(ProductFamily.EDRS_SESSION));
		assertEquals("aws-s3", uut.getBackend());
		assertEquals(true, uut.getDisableChunkedEncoding());
	}
	
}
