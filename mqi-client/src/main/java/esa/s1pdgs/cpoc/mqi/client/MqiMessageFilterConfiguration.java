package esa.s1pdgs.cpoc.mqi.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("mqi")
public class MqiMessageFilterConfiguration {

	private List<MqiMessageFilter> messageFilter = new ArrayList<>();

	public List<MqiMessageFilter> getMessageFilter() {
		return messageFilter;
	}

	public void setMessageFilter(final List<MqiMessageFilter> messageFilter) {
		this.messageFilter = messageFilter;
	}

}
