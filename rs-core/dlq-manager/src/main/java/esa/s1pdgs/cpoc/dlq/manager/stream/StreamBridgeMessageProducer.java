package esa.s1pdgs.cpoc.dlq.manager.stream;

import org.springframework.cloud.stream.function.StreamBridge;

public class StreamBridgeMessageProducer<M> {

	// StreamBridgeMessageProducer is wrapping StreamBridge (which cannot be mocked)
	// to allow Mocking in tests

	private final StreamBridge streamBridge;
	
	public StreamBridgeMessageProducer(final StreamBridge streamBridge) {
		this.streamBridge = streamBridge;
	}
	
	public void send(String topic, M message) {
		streamBridge.send(topic, message);
	}

}
