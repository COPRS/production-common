package esa.s1pdgs.cpoc.dlq.manager.service;

import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_EXCEPTION_MESSAGE;
import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_ORIGINAL_TIMESTAMP;
import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_ORIGINAL_TOPIC;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.dlq.manager.model.routing.RoutingTable;
import esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule;

public class DlqManagerService implements Consumer<Message<?>> {

	private static final Logger LOGGER = LogManager.getLogger(DlqManagerService.class);
	
	private final RoutingTable routingTable;
	private final StreamBridge streamBridge;
	private final String parkingLotTopic;
	
	@Autowired
	public DlqManagerService(final RoutingTable routingTable, final StreamBridge streamBridge,
			final String parkingLotTopic) {
		this.routingTable = routingTable;
		this.streamBridge = streamBridge;
		this.parkingLotTopic = parkingLotTopic;
	}
	
	@Override
	public void accept(Message<?> message) {
		LOGGER.info("Receiving DLQ message: {}", message);
		final String originalTopic = new String(message.getHeaders().get(X_ORIGINAL_TOPIC, byte[].class),
				StandardCharsets.UTF_8);
		LOGGER.trace("DLQ message topic: {}", originalTopic);
		final String timestamp = new String(message.getHeaders().get(X_ORIGINAL_TIMESTAMP, byte[].class),
				StandardCharsets.UTF_8);
		LOGGER.trace("DLQ message timestamp: {}", timestamp);
		final String exceptionMessage = new String(message.getHeaders().get(X_EXCEPTION_MESSAGE, byte[].class),
				StandardCharsets.UTF_8);
		final String payload = (String)message.getPayload();
		LOGGER.trace("Payload: {}", payload);
		
		Optional<Rule> optRule = routingTable.findRule(exceptionMessage);
		if (optRule.isEmpty()) {
			LOGGER.info("No matching rule found");
			LOGGER.info("Publishing to parking lot");
			streamBridge.send(parkingLotTopic, payload);
		} else {
			Rule rule = optRule.get();
			final String targetTopic = "".equals(rule.getTargetTopic()) ? originalTopic : rule.getTargetTopic();
			LOGGER.info("Found rule {} with action {}", rule.getErrorTitle(), rule.getActionType());
			switch(rule.getActionType()) {
				case DELETE:
					LOGGER.info("Ignoring message (error is deleted)");
					break;
				case RESTART:
					JSONObject json = (JSONObject)JSON.parse(payload);
					int retryCounter;
					try {
						retryCounter = json.getInt("retryCounter");
					} catch (JSONException e) {
						throw new IllegalArgumentException("Corrupt message", e);
					}
					if (retryCounter < rule.getMaxRetry()) {
						try {
							json.put("retryCounter", ++retryCounter);
						} catch (JSONException e) {
							throw new IllegalArgumentException("Corrupt message", e);
						}
						LOGGER.info("Publishing to topic {} (retry {}/{})", targetTopic, retryCounter, rule.getMaxRetry());
						streamBridge.send(originalTopic, json.toString());
					} else {
						LOGGER.info("Publishing to parking lot (no retries left, {}/{} retries used)", retryCounter, rule.getMaxRetry());
						streamBridge.send(parkingLotTopic, payload);
					}
					break;
				default:
					throw new IllegalStateException("Invalid configuration");
			}
		}
	}
}
