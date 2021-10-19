package esa.s1pdgs.cpoc.mqi.client.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.mqi.client.MqiMessageFilter;

@Configuration
@ConfigurationProperties("mqi")
public class MqiConfigurationProperties {	
	// S1PRO-922: this is done intentionally using 'null' in order to determine whether MqiClient is
	// configured or not
	private String hostUri = null;
	private int maxRetries = 3;
	private int tempoRetryMs = 1000;	
	private List<MqiMessageFilter> messageFilter = new ArrayList<>();
	
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
	
	public List<MqiMessageFilter> getMessageFilter() {
		return messageFilter;
	}

	public void setMessageFilter(final List<MqiMessageFilter> messageFilter) {
		this.messageFilter = messageFilter;
	}

	@Override
	public String toString() {
		return "MqiConfigurationProperties [hostUri=" + hostUri + ", maxRetries=" + maxRetries + 
				", tempoRetryMs="+ tempoRetryMs + ", messageFilter=" + messageFilter + "]";
	}	
}
