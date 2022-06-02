package esa.s1pdgs.cpoc.dlq.manager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.PropertySource;

@TestConfiguration
@PropertySource("classpath:stream-parameters.properties")
public class TestConfig {

}
