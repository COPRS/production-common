package esa.s1pdgs.cpoc.dlq.manager.service;

import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_EXCEPTION_MESSAGE;
import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_ORIGINAL_TOPIC;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.dlq.manager.configuration.DlqManagerConfigurationProperties;
import esa.s1pdgs.cpoc.dlq.manager.model.routing.RoutingTable;
import esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule;
import esa.s1pdgs.cpoc.dlq.manager.stream.StreamBridgeMessageProducer;

public class DlqManagerService implements Consumer<Message<?>> {

	private static final Logger LOGGER = LogManager.getLogger(DlqManagerService.class);
	
	private final RoutingTable routingTable;
	private final String parkingLotTopic;
	private final StreamBridgeMessageProducer<String> producer;
	
	public DlqManagerService(final RoutingTable routingTable, 
			final StreamBridgeMessageProducer<String> producer,
			final DlqManagerConfigurationProperties properties) {
		this.routingTable = routingTable;
		this.producer = producer;
		this.parkingLotTopic = properties.getParkingLotTopic();
	}
	
	@Override
	public void accept(Message<?> message) {
		LOGGER.info("Receiving DLQ message: {}", message);
		final String originalTopic = new String(message.getHeaders().get(X_ORIGINAL_TOPIC, byte[].class),
				StandardCharsets.UTF_8);
		LOGGER.debug("DLQ message topic: {}", originalTopic);
		final String exceptionMessage = new String(message.getHeaders().get(X_EXCEPTION_MESSAGE, byte[].class),
				StandardCharsets.UTF_8);
		final String payload = (String)message.getPayload();
		LOGGER.debug("Payload: {}", payload);
		
		Optional<Rule> optRule = routingTable.findRule(exceptionMessage);
		if (optRule.isEmpty()) {
			LOGGER.info("No matching rule found");
			LOGGER.info("Publishing to parking lot");
			producer.send(parkingLotTopic, payload);
		} else {
			Rule rule = optRule.get();
			final String targetTopic = "".equals(rule.getTargetTopic()) ? originalTopic : rule.getTargetTopic();
			LOGGER.info("Found rule: {} with action {}", rule.getErrorTitle(), rule.getActionType());
			switch(rule.getActionType()) {
				case DELETE:
					LOGGER.info("Ignoring message (error is deleted)");
					break;
				case RESTART:
					try {
						JSONObject json = new JSONObject(payload);
						int retryCounter = json.getInt("retryCounter");
						if (retryCounter < rule.getMaxRetry()) {
							json.put("retryCounter", ++retryCounter);
							LOGGER.info("Publishing to topic {} (retry {}/{})", targetTopic, retryCounter, rule.getMaxRetry());
							producer.send(targetTopic, json.toString());
						} else {
							LOGGER.info("Publishing to parking lot (no retries left, {}/{} retries used)", retryCounter, rule.getMaxRetry());
							producer.send(parkingLotTopic, payload);
						}
					} catch (JSONException e) {
						throw new IllegalArgumentException("Corrupt message", e);
					}
					break;
				default:
					throw new IllegalStateException("Invalid configuration");
			}
		}
	}
}
