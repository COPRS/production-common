package esa.s1pdgs.cpoc.validation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "validation")
public class ApplicationProperties {
	/**
	 * How many seconds in the past should the interval start
	 */
	private long intervalOffset = 86400; // 24h
	/**
	 * How many seconds should be handled as to new in order
	 * to avoid false-positives
	 */
	private long intervalDelay = 60;

	public long getIntervalOffset() {
		return intervalOffset;
	}

	public void setIntervalOffset(long intervalOffset) {
		this.intervalOffset = intervalOffset;
	}

	public long getIntervalDelay() {
		return intervalDelay;
	}

	public void setIntervalDelay(long intervalDelay) {
		this.intervalDelay = intervalDelay;
	}

}
