package standalone.prip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;

import esa.s1pdgs.cpoc.prip.service.metadata.PripMetadataRepository;
import standalone.prip.service.metadata.DummyPripMetadataRepositoryImpl;

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
}
