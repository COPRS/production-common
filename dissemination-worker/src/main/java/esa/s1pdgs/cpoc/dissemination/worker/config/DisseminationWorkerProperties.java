package esa.s1pdgs.cpoc.dissemination.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "dissemination-worker")
public class DisseminationWorkerProperties {

	// --------------------------------------------------------------------------

}
