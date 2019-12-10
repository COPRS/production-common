package esa.s1pdgs.cpoc.ipf.preparation.trigger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("appcatalog")
public class AppCatalogConfigurationProperties {

	/**
	 * Host URI for the applicative catalog server
	 */
	private String hostUri;

	/**
	 * Maximal number of retries when query fails
	 */
	private int maxRetries;

	/**
	 * Temporisation in ms between 2 retries
	 */
	private int tempoRetryMs;

    /**
     * Connection timeout
     */
    private int tmConnectMs;

	public String getHostUri() {
		return hostUri;
	}

	public void setHostUri(final String hostUri) {
		this.hostUri = hostUri;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(final int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public int getTempoRetryMs() {
		return tempoRetryMs;
	}

	public void setTempoRetryMs(final int tempoRetryMs) {
		this.tempoRetryMs = tempoRetryMs;
	}

	public int getTmConnectMs() {
		return tmConnectMs;
	}

	public void setTmConnectMs(final int tmConnectMs) {
		this.tmConnectMs = tmConnectMs;
	}
}
