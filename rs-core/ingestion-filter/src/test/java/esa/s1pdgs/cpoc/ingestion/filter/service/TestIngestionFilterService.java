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

package esa.s1pdgs.cpoc.ingestion.filter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ingestion.filter.config.IngestionFilterConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestIngestionFilterService {

	@Autowired
	private IngestionFilterConfigurationProperties properties;	
	
	@Test
	public final void testAcceptCase() throws ParseException {
		IngestionFilterService service = new IngestionFilterService(properties);

		String productName = "DCS_01_S3A_2021112408000009000_dat/ch_1/DCS_01_S3A_2021112408000009000_ch1_DSDB_00001.raw";
		
		IngestionJob job = new IngestionJob(ProductFamily.EDRS_SESSION,
				productName,
				"pickupBase",
				"WILE/S3A/7000/DCS_01_S3A_2021112408000009000_dat/ch_1/DCS_01_S3A_2021112408000009000_ch1_DSDB_00001.raw",
				179, Date.from(DateUtils.parse("2021-11-24T08:00:00").toInstant(ZoneOffset.UTC)), UUID.randomUUID(), "S3", "TEST", null, null, "xbip", null, "2021-11-24T08:00:00");
		
		List<IngestionJob> ingestionJobs = new ArrayList<>();
		ingestionJobs.add(job);
		
		List<Message<IngestionJob>> filteredJobs = service.apply(ingestionJobs);
		
		assertEquals(1, filteredJobs.size());
	}
	
	@Test
	public final void testRejectCase() throws ParseException {
		IngestionFilterService service = new IngestionFilterService(properties);

		String productName = "DCS_01_S3A_2021112409000009000_dat/ch_1/DCS_01_S3A_2021112409000009000_ch1_DSDB_00001.raw";
		
		IngestionJob job = new IngestionJob(ProductFamily.EDRS_SESSION,
				productName,
				"pickupBase",
				"WILE/S3A/7000/DCS_01_S3A_2021112408000009000_dat/ch_1/DCS_01_S3A_2021112408000009000_ch1_DSDB_00001.raw",
				179, Date.from(DateUtils.parse("2021-11-24T09:00:00").toInstant(ZoneOffset.UTC)), UUID.randomUUID(), "S3", "TEST", null, null, "xbip", null, "2021-11-24T09:00:00");
		List<IngestionJob> ingestionJobs = new ArrayList<>();
		ingestionJobs.add(job);
		
		List<Message<IngestionJob>> filteredJobs = service.apply(ingestionJobs);
		
		assertEquals(null, filteredJobs);
	}
}
