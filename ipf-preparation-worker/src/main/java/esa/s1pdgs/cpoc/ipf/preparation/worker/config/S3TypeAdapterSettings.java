package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Additional settings used to configure the S3 type adapter
 * 
 * @author Julian Kaping
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "s3-type-adapter")
public class S3TypeAdapterSettings {
	
	private List<String> marginProductTypes = new ArrayList<>();

	public List<String> getMarginProductTypes() {
		return marginProductTypes;
	}

	public void setMarginProductTypes(List<String> marginProductTypes) {
		this.marginProductTypes = marginProductTypes;
	}
}
