package esa.s1pdgs.cpoc.preparation.worker.config.type;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "s3-synergy")
public class S3SynergyProperties {

}
