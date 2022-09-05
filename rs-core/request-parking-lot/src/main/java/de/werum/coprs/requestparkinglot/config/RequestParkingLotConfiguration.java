package de.werum.coprs.requestparkinglot.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.utils.StringUtil;

@Configuration
public class RequestParkingLotConfiguration {
	private final List<String> kafkaTopicList;
	private final String defaultResubmitTopic;

	public RequestParkingLotConfiguration(
			@Value("${kafkaTopicList:}") final String kafkaTopicList,
			@Value("${defaultResubmitTopic:catalog-event}") final String defaultResubmitTopic
    ) {
		if (StringUtil.isEmpty(kafkaTopicList)) {
			this.kafkaTopicList = Collections.emptyList();
		}
		else {
			this.kafkaTopicList = Arrays.asList(kafkaTopicList.split("\\s+"));
		}
		this.defaultResubmitTopic = defaultResubmitTopic;
	}

	public List<String> getKafkaTopicList() {
		return kafkaTopicList;
	}
	
	public String getDefaultResubmitTopic() {
		return defaultResubmitTopic;
	}
}
