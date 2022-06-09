package esa.s1pdgs.cpoc.dlq.manager.service;

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
	
	public static String X_EXCEPTION_MESSAGE = "x-exception-message";
	public static String X_ORIGINAL_TOPIC = "x-original-topic";
	
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
		LOGGER.debug("Receiving new message: {}", message);
		final String originalTopic = new String(message.getHeaders().get(X_ORIGINAL_TOPIC, byte[].class),
				StandardCharsets.UTF_8);
		LOGGER.debug("DLQ message topic: {}", originalTopic);
		final String exceptionMessage = new String(message.getHeaders().get(X_EXCEPTION_MESSAGE, byte[].class),
				StandardCharsets.UTF_8);
		final String payload = new String((byte[])message.getPayload());
		
		LOGGER.info("Receiving DLQ message on topic {} with error: {}", originalTopic, exceptionMessage);
		LOGGER.info("Payload: {}", payload);
		
		Optional<Rule> optRule = routingTable.findRule(exceptionMessage);
		if (optRule.isEmpty()) {
			LOGGER.info("No matching rule found");
			LOGGER.info("Publishing to parking lot");
			producer.send(parkingLotTopic, payload);
		} else {
			Rule rule = optRule.get();
			final String targetTopic = "".equals(rule.getTargetTopic()) ? originalTopic : rule.getTargetTopic();
			LOGGER.info("Found rule {}: {}", rule.getActionType().name(), rule.getErrorTitle());
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
						LOGGER.error("Corrupt message");
						throw new IllegalArgumentException("Corrupt message", e);
					}
					break;
				default:
					LOGGER.error("Invalid configuration");
					throw new IllegalStateException("Invalid configuration");
			}
		}
	}
}
