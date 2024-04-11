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

package esa.s1pdgs.cpoc.xbip.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.xbip.client.config.XbipClientConfiguration;
import esa.s1pdgs.cpoc.xbip.client.config.XbipClientConfigurationProperties;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(value = XbipClientConfigurationProperties.class)
@PropertySource({"${xbipConfigFile:classpath:xbip.properties}"})
public class ITXbipClient {
	
	@Autowired
	private XbipClientConfigurationProperties config;
	
	@Test
	public final void testConfigProperties() {
		assertNotNull(config);
		assertEquals(1, config.getHostConfigs().size());
		System.out.println(config);
	}
	
	@Ignore
	@Test
	public final void testFoo() throws Exception {
		final XbipClientConfiguration c = new XbipClientConfiguration(config);		
		final XbipClientFactory factory = c.xbipClientFactory();		
		final XbipClient uut = factory.newXbipClient(new URI("https://cgs01.sentinel1.eo.esa.int/NOMINAL/"));
		
		final List<XbipEntry> result = uut.list(XbipEntryFilter.ALLOW_ALL);
		
		result.stream()
			.forEach(e -> System.out.println(e));
		
		System.err.println(result.size());
	}
}
