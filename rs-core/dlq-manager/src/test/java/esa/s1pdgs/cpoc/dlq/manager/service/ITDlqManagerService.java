package esa.s1pdgs.cpoc.dlq.manager.service;

import static esa.s1pdgs.cpoc.dlq.manager.service.DlqManagerService.X_ROUTE_TO;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_EXCEPTION_MESSAGE;
import static org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder.X_ORIGINAL_TOPIC;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

@RunWith(SpringRunner.class)
@SpringBootTest	
@ComponentScan("esa.s1pdgs.cpoc")
@Import(TestConfig.class)
public class ITDlqManagerService {

	@Autowired
	DlqManagerServiceConfiguration dlqManagerServiceConfiguration;
	
	Function<Message<byte[]>, List<Message<byte[]>>> dlqManagerService;

	@Test
	public void testSameTopicRetryTwiceThenParkingLot() throws JsonProcessingException, JSONException {
		dlqManagerService = dlqManagerServiceConfiguration.route();
		
		// first retry
		
		CatalogJob catalogJob = new CatalogJob("foo", "foo", ProductFamily.AUXILIARY_FILE);
		assertEquals(0, catalogJob.getRetryCounter());

		ObjectMapper mapper = new ObjectMapper();
		Message<byte[]> message1 = new GenericMessage<>( //
				mapper.writeValueAsString(catalogJob).getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-same".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "first line\nRuntimeException".getBytes(StandardCharsets.UTF_8)));
		
		List<Message<byte[]>> actual1 = dlqManagerService.apply(message1);
		
		assertEquals("t-pdgs-same", (String)actual1.get(0).getHeaders().get(X_ROUTE_TO));

		JsonNode actualJson1 = mapper.readTree(new String(actual1.get(0).getPayload()));
		assertEquals(1, actualJson1.get("retryCounter").asInt());

		catalogJob.increaseRetryCounter();
		JsonNode expected1 = mapper.readTree(mapper.writeValueAsString(catalogJob));
		assertTrue(expected1.equals(actualJson1));

		// second retry
		
		Message<byte[]> message2 = new GenericMessage<>( //
				actualJson1.toString().getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-same".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "RuntimeException".getBytes(StandardCharsets.UTF_8)));
		
		List<Message<byte[]>> actual2 = dlqManagerService.apply(message2);
		
		assertEquals("t-pdgs-same", (String)actual2.get(0).getHeaders().get(X_ROUTE_TO));

		JsonNode actualJson2 = mapper.readTree(new String(actual2.get(0).getPayload()));
		assertEquals(2, actualJson2.get("retryCounter").asInt());

		catalogJob.increaseRetryCounter();
		JsonNode expected2 = mapper.readTree(mapper.writeValueAsString(catalogJob));
		assertTrue(expected2.equals(actualJson2));
		
		// to parking lot
		
		Message<byte[]> message3 = new GenericMessage<>( //
				actualJson2.toString().getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-same".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "RuntimeException".getBytes(StandardCharsets.UTF_8)));
		
		List<Message<byte[]>> actual3 = dlqManagerService.apply(message3);
		
		JsonNode actualJson3 = mapper.readTree(new String(actual3.get(0).getPayload()));
		assertEquals(2, actualJson3.get("retryCounter").asInt());
		
		assertEquals("t-pdgs-parking-lot", (String)actual3.get(0).getHeaders().get(X_ROUTE_TO));
	}
	
	@Test
	public void testDifferentTopicRetry() throws JsonProcessingException, JSONException {
		dlqManagerService = dlqManagerServiceConfiguration.route();
		
		CatalogJob catalogJob = new CatalogJob("foo", "foo", ProductFamily.AUXILIARY_FILE);
		assertEquals(0, catalogJob.getRetryCounter());

		ObjectMapper mapper = new ObjectMapper();
		Message<byte[]> message = new GenericMessage<>( //
				mapper.writeValueAsString(catalogJob).getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-origin".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "!$%foobar#§!\nsecond line".getBytes(StandardCharsets.UTF_8)));
		
		List<Message<byte[]>> actual = dlqManagerService.apply(message);
		
		assertEquals("t-pdgs-different", (String)actual.get(0).getHeaders().get(X_ROUTE_TO));

		JsonNode actualJson = mapper.readTree(new String(actual.get(0).getPayload()));
		assertEquals(1, actualJson.get("retryCounter").asInt());

		catalogJob.increaseRetryCounter();
		JsonNode expectedJson = mapper.readTree(mapper.writeValueAsString(catalogJob));
		assertTrue(expectedJson.equals(actualJson));
	}
	
	@Test
	public void testNoActionShallRouteToParkingLot() throws JsonProcessingException, JSONException {
		dlqManagerService = dlqManagerServiceConfiguration.route();
		
		CatalogJob catalogJob = new CatalogJob("foo", "foo", ProductFamily.AUXILIARY_FILE);
		assertEquals(0, catalogJob.getRetryCounter());

		ObjectMapper mapper = new ObjectMapper();
		Message<byte[]> message = new GenericMessage<>( //
				mapper.writeValueAsString(catalogJob).getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-origin".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "IOException"
						.getBytes(StandardCharsets.UTF_8)));
		
		List<Message<byte[]>> actual = dlqManagerService.apply(message);
		
		assertEquals("t-pdgs-parking-lot", (String)actual.get(0).getHeaders().get(X_ROUTE_TO));
		
		JsonNode actualJson = mapper.readTree(new String(actual.get(0).getPayload()));
		assertEquals(0, actualJson.get("retryCounter").asInt()); // this is not a retry

		JsonNode expectedJson = mapper.readTree(mapper.writeValueAsString(catalogJob));
		assertTrue(expectedJson.equals(actualJson));
	}
	
	@Test
	public void testDeleteShallBeIgnored() throws JsonProcessingException, JSONException {
		dlqManagerService = dlqManagerServiceConfiguration.route();
		
		CatalogJob catalogJob = new CatalogJob("foo", "foo", ProductFamily.AUXILIARY_FILE);
		assertEquals(0, catalogJob.getRetryCounter());

		ObjectMapper mapper = new ObjectMapper();
		Message<byte[]> message = new GenericMessage<>( //
				mapper.writeValueAsString(catalogJob).getBytes(StandardCharsets.UTF_8), //
				Map.of( X_ORIGINAL_TOPIC, "t-pdgs-origin".getBytes(StandardCharsets.UTF_8), //
						X_EXCEPTION_MESSAGE, "a message to IGNORE..."
						.getBytes(StandardCharsets.UTF_8)));
		
		List<Message<byte[]>> actual = dlqManagerService.apply(message);
		assertEquals(0, actual.size());
	}
	
}
