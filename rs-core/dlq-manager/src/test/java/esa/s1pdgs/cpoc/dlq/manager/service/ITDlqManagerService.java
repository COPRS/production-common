package esa.s1pdgs.cpoc.dlq.manager.service;

import static esa.s1pdgs.cpoc.dlq.manager.service.DlqManagerService.X_EXCEPTION_MESSAGE;
import static esa.s1pdgs.cpoc.dlq.manager.service.DlqManagerService.X_ORIGINAL_TOPIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.dlq.manager.config.TestConfig;
import esa.s1pdgs.cpoc.dlq.manager.configuration.DlqManagerServiceConfiguration;
import esa.s1pdgs.cpoc.dlq.manager.stream.StreamBridgeMessageProducer;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

@RunWith(SpringRunner.class)
@SpringBootTest	
@ComponentScan("esa.s1pdgs.cpoc")
@Import(TestConfig.class)
public class ITDlqManagerService {

	@MockBean
	StreamBridgeMessageProducer<String> producer;
	
	@Autowired
	DlqManagerServiceConfiguration dlqManagerServiceConfiguration;
	
	Consumer<Message<byte[]>> consumer;
	
	@Test
	public void testSameTopicRetryTwiceThenParkingLot() throws JsonProcessingException, JSONException {
		consumer = dlqManagerServiceConfiguration.route();
		
		ArgumentCaptor<String> targetTopicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		
		// first retry
		
		CatalogJob catalogJob = new CatalogJob("foo", "foo", ProductFamily.AUXILIARY_FILE);
		assertEquals(0, catalogJob.getRetryCounter());

		ObjectMapper mapper = new ObjectMapper();
		Message<byte[]> message1 = new GenericMessage<>( //
				mapper.writeValueAsString(catalogJob).getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-same".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "RuntimeException".getBytes(StandardCharsets.UTF_8)));
		
		consumer.accept(message1);
		
		verify(producer, times(1)).send(targetTopicCaptor.capture(), messageCaptor.capture());
		assertEquals("t-pdgs-same", targetTopicCaptor.getValue());

		String capturedMessage1 = messageCaptor.getValue();
		JsonNode actual1 = mapper.readTree(capturedMessage1);
		assertEquals(1, actual1.get("retryCounter").asInt());

		catalogJob.increaseRetryCounter();
		JsonNode expected1 = mapper.readTree(mapper.writeValueAsString(catalogJob));
		assertTrue(expected1.equals(actual1));

		// second retry
		
		Message<byte[]> message2 = new GenericMessage<>( //
				capturedMessage1.getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-same".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "RuntimeException".getBytes(StandardCharsets.UTF_8)));
		
		consumer.accept(message2);
		
		verify(producer, times(2)).send(targetTopicCaptor.capture(), messageCaptor.capture());
		assertEquals("t-pdgs-same", targetTopicCaptor.getValue());

		String capturedMessage2 = messageCaptor.getValue();
		JsonNode actual2 = mapper.readTree(capturedMessage2);
		assertEquals(2, actual2.get("retryCounter").asInt());

		catalogJob.increaseRetryCounter();
		JsonNode expected2 = mapper.readTree(mapper.writeValueAsString(catalogJob));
		assertTrue(expected2.equals(actual2));
		
		// to parking lot
		
		Message<byte[]> message3 = new GenericMessage<>( //
				capturedMessage2.getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-same".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "RuntimeException".getBytes(StandardCharsets.UTF_8)));
		
		consumer.accept(message3);
		
		verify(producer, times(3)).send(targetTopicCaptor.capture(), messageCaptor.capture());
		assertEquals("t-pdgs-parking-lot", targetTopicCaptor.getValue());
	}
	
	@Test
	public void testDifferentTopicRetry() throws JsonProcessingException, JSONException {
		consumer = dlqManagerServiceConfiguration.route();
		
		ArgumentCaptor<String> targetTopicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		
		CatalogJob catalogJob = new CatalogJob("foo", "foo", ProductFamily.AUXILIARY_FILE);
		assertEquals(0, catalogJob.getRetryCounter());

		ObjectMapper mapper = new ObjectMapper();
		Message<byte[]> message = new GenericMessage<>( //
				mapper.writeValueAsString(catalogJob).getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-origin".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "!$%foobar#§!".getBytes(StandardCharsets.UTF_8)));
		
		consumer.accept(message);
		
		verify(producer, times(1)).send(targetTopicCaptor.capture(), messageCaptor.capture());
		assertEquals("t-pdgs-different", targetTopicCaptor.getValue());

		String capturedMessage = messageCaptor.getValue();
		JsonNode actual = mapper.readTree(capturedMessage);
		assertEquals(1, actual.get("retryCounter").asInt());

		catalogJob.increaseRetryCounter();
		JsonNode expected = mapper.readTree(mapper.writeValueAsString(catalogJob));
		assertTrue(expected.equals(actual));
	}
	
	@Test
	public void testNoActionShallRouteToParkingLot() throws JsonProcessingException, JSONException {
		consumer = dlqManagerServiceConfiguration.route();
		
		ArgumentCaptor<String> targetTopicCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		
		CatalogJob catalogJob = new CatalogJob("foo", "foo", ProductFamily.AUXILIARY_FILE);
		assertEquals(0, catalogJob.getRetryCounter());

		ObjectMapper mapper = new ObjectMapper();
		Message<byte[]> message = new GenericMessage<>( //
				mapper.writeValueAsString(catalogJob).getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-origin".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "IOException"
						.getBytes(StandardCharsets.UTF_8)));
		
		consumer.accept(message);
		
		verify(producer, times(1)).send(targetTopicCaptor.capture(), messageCaptor.capture());
		assertEquals("t-pdgs-parking-lot", targetTopicCaptor.getValue());

		String capturedMessage = messageCaptor.getValue();
		JsonNode actual = mapper.readTree(capturedMessage);
		assertEquals(0, actual.get("retryCounter").asInt()); // this is not a retry

		JsonNode expected = mapper.readTree(mapper.writeValueAsString(catalogJob));
		assertTrue(expected.equals(actual));
	}
	
	@Test
	public void testDeleteShallBeIgnored() throws JsonProcessingException, JSONException {
		consumer = dlqManagerServiceConfiguration.route();
		
		CatalogJob catalogJob = new CatalogJob("foo", "foo", ProductFamily.AUXILIARY_FILE);
		assertEquals(0, catalogJob.getRetryCounter());

		ObjectMapper mapper = new ObjectMapper();
		Message<byte[]> message = new GenericMessage<>( //
				mapper.writeValueAsString(catalogJob).getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-origin".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "a message to IGNORE..."
						.getBytes(StandardCharsets.UTF_8)));
		
		consumer.accept(message);
		
		verify(producer, times(0)).send(Mockito.anyString(), Mockito.anyString());
	}
	
}
