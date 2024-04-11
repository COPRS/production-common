/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.errorrepo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.MessageErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;

@Configuration
public class ErrorRepoConfiguration {
	
	@Bean
	@ConditionalOnProperty("kafka.producer.max-retries")
	public ErrorRepoAppender kafkaErrorRepoAppender(final MessageProducer<FailedProcessing> messageProducer, final KafkaProperties kafkaProperties)
	{
		return new MessageErrorRepoAppender(kafkaProperties.getErrorTopic(), messageProducer);
	}
}