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

package esa.s1pdgs.cpoc.datalifecycle.worker.config;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.worker.service.CatalogEventService;
import esa.s1pdgs.cpoc.datalifecycle.worker.service.CompressionEventService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;

@Configuration
public class DataLifecycleWorkerServiceConfiguration {
	
	@Autowired
	private CommonConfigurationProperties commonProperties;
	
	@Autowired
	private DataLifecycleWorkerConfigurationProperties configurationProperties;
	
	@Autowired
	private DataLifecycleMetadataRepository metadataRepo;
	
	@Bean
	public Consumer<CatalogEvent> update() {
		return new CatalogEventService(commonProperties, configurationProperties, metadataRepo);
	}
	
	@Bean
	public Consumer<CompressionEvent> updateCompressed() {
		return new CompressionEventService(commonProperties, configurationProperties, metadataRepo);
	}

}
