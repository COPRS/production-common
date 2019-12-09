package esa.s1pdgs.cpoc.ipf.preparation.trigger.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app-l0-segment")
public class L0SegmentAppProperties {
    
    private String blacklistPattern;

    public String getBlacklistPattern() {
		return blacklistPattern;
	}

	public void setBlacklistPattern(final String blacklistPattern) {
		this.blacklistPattern = blacklistPattern;
	}
}
