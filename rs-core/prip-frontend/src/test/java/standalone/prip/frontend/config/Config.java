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
    	return new PripElasticSearchMetadataRepo(restHighLevelClient, maxSearchHits);
    }
    
    @Bean
    @Primary
    ObsClient getObsClient() {
    	return new FakeObsClient();
    }
}
