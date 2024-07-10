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

package esa.s1pdgs.cpoc.evictionmanagement.worker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.evictionmanagement.worker.service.EvictionManagementService;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;

@ConditionalOnProperty(value = "scheduling.enable", havingValue="true", matchIfMissing = true)
@EnableScheduling
@Configuration
public class SchedulingConfiguration {
	
	@Autowired
	private DataLifecycleMetadataRepository metadataRepo;
	
	@Autowired
	private ObsClient obsClient;
	
	@Autowired
	private MetadataClient metadataClient;
	
	@Autowired
	private PripMetadataRepository pripMetadataRepo;
	
	@Bean
	public EvictionManagementService evictionManagement() {
		return new EvictionManagementService(metadataRepo, obsClient, metadataClient, pripMetadataRepo);
		
	}

}
