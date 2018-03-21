package fr.viveris.s1pdgs.scaler.scaling;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "scaler")
public class ScalerProperties {

	private int fixedDelayMs;

	/**
	 * @return the fixedDelay
	 */
	public int getFixedDelayMs() {
		return fixedDelayMs;
	}

	/**
	 * @param fixedDelay the fixedDelay to set
	 */
	public void setFixedDelayMs(int fixedDelayMs) {
		this.fixedDelayMs = fixedDelayMs;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ScalerProperties [fixedDelayMs=" + fixedDelayMs + "]";
	}

}
