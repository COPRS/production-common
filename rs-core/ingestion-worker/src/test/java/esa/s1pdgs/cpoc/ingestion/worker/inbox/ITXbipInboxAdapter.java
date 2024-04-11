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

package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;

@Ignore
@RunWith(SpringRunner.class)
@EnableConfigurationProperties
@ComponentScan("esa.s1pdgs.cpoc.xbip")
@PropertySource({"${xbipConfigFile:classpath:xbip.properties}"})
public class ITXbipInboxAdapter {

	@Autowired
	private XbipClientFactory xbipFactory;
	
	@Test
	public final void test() throws Exception {
		System.out.println(xbipFactory);
		
		final XbipInboxAdapter uut = new XbipInboxAdapter(xbipFactory);
		
		try (final InboxAdapterResponse response = uut.read(
				new URI("https://cgs01.sentinel1.eo.esa.int/NOMINAL/S1A/DCS_04_20200403151525031965_dat/"),
				"DCS_04_20200403151525031965_dat", "", 123L))
		{
			response.getResult().forEach(e -> System.out.println(e));
		}
	}
	
}
