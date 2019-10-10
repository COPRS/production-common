package esa.s1pdgs.cpoc.prip.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import esa.s1pdgs.cpoc.prip.service.metadata.DummyPripMetadataRepositoryImpl;
import esa.s1pdgs.cpoc.prip.service.metadata.PripMetadataRepository;

@Configuration
public class TestApplicationContext {
	@Bean
	@Primary
	PripMetadataRepository getPripMetadataRepository() {
		return new DummyPripMetadataRepositoryImpl();
	}
}
