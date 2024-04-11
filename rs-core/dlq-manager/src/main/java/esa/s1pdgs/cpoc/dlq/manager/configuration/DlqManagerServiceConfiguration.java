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

package esa.s1pdgs.cpoc.dlq.manager.configuration;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.dlq.manager.model.routing.RoutingTable;
import esa.s1pdgs.cpoc.dlq.manager.service.DlqManagerService;

@Configuration
public class DlqManagerServiceConfiguration {

	@Autowired
	private CommonConfigurationProperties commonProperties;
	
	@Autowired
	private RoutingTable routingTable;

	@Autowired
	private DlqManagerConfigurationProperties dlqManagerConfigurationProperties;
	
	@Bean
	public Function<Message<byte[]>, List<Message<byte[]>>> route() {
		return new DlqManagerService(commonProperties, routingTable, dlqManagerConfigurationProperties);
	}
}
