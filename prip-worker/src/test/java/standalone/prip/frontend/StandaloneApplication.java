package standalone.prip.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;

import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import standalone.prip.frontend.obs.FakeObsClient;
import standalone.prip.frontend.service.metadata.DummyPripMetadataRepositoryImpl;

@SpringBootApplication
@ComponentScan("esa.s1pdgs.cpoc.prip")
public class StandaloneApplication {
    public static void main(String[] args) {
        SpringApplication.run(StandaloneApplication.class, args);
    }
    
    @Bean
    @Primary
    PripMetadataRepository getPripMetadataRepository() {
    	return new DummyPripMetadataRepositoryImpl();
    }
    
    @Bean
    @Primary
    ObsClient getObsClient() {
    	return new FakeObsClient();
    }
}
