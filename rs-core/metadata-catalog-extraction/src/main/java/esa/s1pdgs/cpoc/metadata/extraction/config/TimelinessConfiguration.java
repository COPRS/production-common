package esa.s1pdgs.cpoc.metadata.extraction.config;

import java.util.HashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "timeliness")
public class TimelinessConfiguration extends HashMap<String, Integer> {

}
