package de.werum.coprs.requestparkinglot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestParkingLotConfiguration {
	private final String defaultResubmitTopic;

	public RequestParkingLotConfiguration(
			@Value("${defaultResubmitTopic:catalog-event}") final String defaultResubmitTopic
    ) {
		this.defaultResubmitTopic = defaultResubmitTopic;
	}
	
	public String getDefaultResubmitTopic() {
		return defaultResubmitTopic;
	}
}
