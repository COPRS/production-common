package esa.s1pdgs.cpoc.reqrepo.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.utils.StringUtil;

@Configuration
public class RequestRepositoryConfiguration {
	private final List<String> kafkaTopicList;

	public RequestRepositoryConfiguration(
			@Value("${kafkaTopicList:}") final String kafkaTopicList
    ) {
		if (StringUtil.isEmpty(kafkaTopicList)) {
			this.kafkaTopicList = Collections.emptyList();
		}
		else {
			this.kafkaTopicList = Arrays.asList(kafkaTopicList.split("\\s+"));
		}
	}

	public List<String> getKafkaTopicList() {
		return kafkaTopicList;
	}
}
