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

package esa.s1pdgs.cpoc.dlq.manager.service;

import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_EXCEPTION_MESSAGE;
import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_EXCEPTION_STACKTRACE;
import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_ORIGINAL_TIMESTAMP;
import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_ORIGINAL_TOPIC;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.dlq.manager.configuration.DlqManagerConfigurationProperties;
import esa.s1pdgs.cpoc.dlq.manager.model.mapping.JsonMapping;
import esa.s1pdgs.cpoc.dlq.manager.model.routing.RoutingTable;
import esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule;
import esa.s1pdgs.cpoc.dlq.manager.report.DlqReportingInput;
import esa.s1pdgs.cpoc.dlq.manager.report.DlqReportingOutput;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;


public class DlqManagerService implements Function<Message<byte[]>, List<Message<byte[]>>> {

	private static final Logger LOGGER = LogManager.getLogger(DlqManagerService.class);
	
	private static final Gson GSON = new GsonBuilder().serializeNulls().create();
	
	private final CommonConfigurationProperties commonProperties;
	
	public static final String X_ROUTE_TO = "x-route-to";
	
	private final RoutingTable routingTable;
	private final String parkingLotTopic;
	
	public DlqManagerService(final CommonConfigurationProperties commonProperties,
			final RoutingTable routingTable,
			final DlqManagerConfigurationProperties properties) {
		this.commonProperties = commonProperties;
		this.routingTable = routingTable;
		this.parkingLotTopic = properties.getParkingLotTopic();
	}
	
	@Override
	public List<Message<byte[]>> apply(Message<byte[]> message) {
		final List<Message<byte[]>> result = new ArrayList<>();
		LOGGER.debug("Receiving new message: {}", message);
		final String originalTopic = new String(message.getHeaders().get(X_ORIGINAL_TOPIC, byte[].class),
				StandardCharsets.UTF_8);
		LOGGER.debug("DLQ message topic: {}", originalTopic);
		final String exceptionMessage = new String(message.getHeaders().get(X_EXCEPTION_MESSAGE, byte[].class),
				StandardCharsets.UTF_8);
		final String payload = new String(message.getPayload());
		final Map<String, Object> payloadMap;
		try {
			payloadMap = new ObjectMapper().readValue(
					payload, new TypeReference<HashMap<String, Object>>(){});
		} catch (JsonProcessingException e) {
			LOGGER.error("Corrupt payload: {}", payload);
			throw new IllegalArgumentException("Corrupt payload", e);
		}	

		LOGGER.info("Receiving DLQ message on topic {} with error: {}", originalTopic, exceptionMessage);
		LOGGER.debug("Payload: {}", payload);

		final UUID uid = UUID.fromString((String)payloadMap.get("uid"));
		final MissionId missionId = extractMissionId(payloadMap);
		int retryCounter = (Integer)payloadMap.get("retryCounter");

		final Reporting reporting = ReportingUtils.newReportingBuilder(missionId)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(uid).newReporting("DlqManagement");
		
		reporting.begin(DlqReportingInput.newInstance(originalTopic, retryCounter),
				new ReportingMessage("Start routing"));		
		
		Optional<Rule> optRule = routingTable.findRule(exceptionMessage);
		if (optRule.isEmpty()) {
			LOGGER.info("No matching rule found");
			LOGGER.info("Route to {}", parkingLotTopic);
			result.add(newParkingLotMessage(originalTopic, payloadMap, message.getHeaders(), missionId));
			reporting.end(DlqReportingOutput.newInstance(null, null, parkingLotTopic),
					new ReportingMessage("Finished routing"));
		} else {
			Rule rule = optRule.get();
			LOGGER.info("Found rule {}: {}", rule.getActionType().name(), rule.getErrorTitle());
			switch(rule.getActionType()) {
				case DROP:
					LOGGER.info("Ignoring message");
					reporting.end(DlqReportingOutput.newInstance(rule.getErrorTitle(), rule.getActionType(), null),
							new ReportingMessage("Finished routing"));
					break;
				case RESTART:
					final String targetTopic = "".equals(rule.getTargetTopic()) ? originalTopic : rule.getTargetTopic();					
					if (retryCounter < rule.getMaxRetry()) {
						payloadMap.put("retryCounter", ++retryCounter);
						LOGGER.info("Route to {} (retry {}/{})", targetTopic, retryCounter, rule.getMaxRetry());
						result.add(MessageBuilder.withPayload(GSON.toJson(payloadMap)
								.getBytes(StandardCharsets.UTF_8)).setHeader(X_ROUTE_TO, targetTopic).build());
						reporting.end(DlqReportingOutput.newInstance(rule.getErrorTitle(), rule.getActionType(), targetTopic),
								new ReportingMessage("Finished routing"));
					} else {
						LOGGER.info("Route to {} ({}/{} retries used)", parkingLotTopic, retryCounter, rule.getMaxRetry());
						result.add(newParkingLotMessage(originalTopic, payloadMap, message.getHeaders(), missionId));
						reporting.end(DlqReportingOutput.newInstance(rule.getErrorTitle(), rule.getActionType(), parkingLotTopic),
								new ReportingMessage("Finished routing"));
					}
					break;
				default:
					LOGGER.error("Invalid configuration");
					throw new IllegalStateException("Invalid configuration");
			}
		}
		return result;
	}
	
	private Message<byte[]> newParkingLotMessage(String originalTopic, Map<String, Object> payloadMap,
			MessageHeaders originalMessageHeader, MissionId missionId) {
		final Date originalTimestamp = new Date(bytesToLong(originalMessageHeader.get(X_ORIGINAL_TIMESTAMP, byte[].class)));
		final String exceptionMessage = new String(originalMessageHeader.get(X_EXCEPTION_MESSAGE, byte[].class),
				StandardCharsets.UTF_8);
		final String exceptionStacktrace = new String(originalMessageHeader.get(X_EXCEPTION_STACKTRACE, byte[].class), StandardCharsets.UTF_8);
		final String errorLevel = !originalMessageHeader.containsKey("errorLevel")
				? "NOT_DEFINED" : (String)originalMessageHeader.get("errorLevel");
		
		FailedProcessing failedProcessing = new FailedProcessing(originalTopic, originalTimestamp,
				missionId, errorLevel, GSON.toJson(payloadMap), exceptionMessage,
				exceptionStacktrace, (Integer)payloadMap.get("retryCounter"));
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			String s = mapper.addMixIn(FailedProcessing.class, JsonMapping.class).writeValueAsString(failedProcessing); // omit id field here, MongoDB will create it on insert
			return MessageBuilder.withPayload(s.getBytes(StandardCharsets.UTF_8))
					.setHeader(X_ROUTE_TO, parkingLotTopic).build();
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException("Serialization error", e);
		}
	}
	
	static MissionId extractMissionId(Map<String, Object> payloadMap) {
		try {
			return MissionId.fromFileName((String)payloadMap.get("missionId"));
		} catch (Exception e) {
			return MissionId.UNDEFINED;
		}
	}

	static long bytesToLong(byte[] bytes) {
		long result = 0;
		for (int idx = 0; idx < 8; idx++) {
			result |= (bytes[idx] & 0xffL) << 8L * (7L - idx);
		}
		return result;
	}

}
