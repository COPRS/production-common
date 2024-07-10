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

package standalone.prip.frontend.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.prip.metadata.PripElasticSearchMetadataRepo;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import standalone.prip.frontend.obs.FakeObsClient;

public class Config {
	@Autowired
	RestHighLevelClient restHighLevelClient;
	
    @Bean
    @Primary
    PripMetadataRepository getPripMetadataRepository() {
    	int maxSearchHits = 100;
    	return new PripElasticSearchMetadataRepo(restHighLevelClient, maxSearchHits, true);
    }
    
    @Bean
    @Primary
    ObsClient getObsClient() {
    	return new FakeObsClient();
    }
}
