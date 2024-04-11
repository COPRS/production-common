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

package esa.s1pdgs.cpoc.cronbased.trigger.config;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.cronbased.trigger.db.CronbasedTriggerEntryRepository;
import esa.s1pdgs.cpoc.cronbased.trigger.service.CronbasedTriggerService;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Configuration
public class CronbasedTriggerConfiguration {

	@Autowired
	private CronbasedTriggerProperties properties;
	
	@Autowired
	private MetadataClient metadataClient;
	
	@Autowired
	private CronbasedTriggerEntryRepository repository;
	
	@Autowired
	private ObsClient obsClient;

	@Bean
	public Function<Message<?>, List<Message<CatalogEvent>>> cronbasedTrigger() {
		return new CronbasedTriggerService(properties, metadataClient, repository, obsClient);
	}
}
