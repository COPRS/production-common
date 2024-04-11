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

package de.werum.coprs.requestparkinglot.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.werum.coprs.requestparkinglot.config.RequestParkingLotConfiguration;
import de.werum.coprs.requestparkinglot.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.message.MessageProducer;

public class RequestParkingLotTest {
	private final RequestParkingLotConfiguration config = new RequestParkingLotConfiguration("defResubmitTopic");
	
	@Mock
	private FailedProcessingRepo failedProcessingRepo;
	
	@Mock
	private MessageProducer<Object> messageProducer;
	
	private RequestParkingLot uut;
		

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.uut = new RequestParkingLotImpl(
				failedProcessingRepo,
				config,
				messageProducer
		);
	}

	
	@Test
	public void testGetFailedProcessings_OnInvocation_ShallReturnAllElements() {
		doReturn(Arrays.asList(newFailedProcessing("123"), newFailedProcessing("456")))
			.when(failedProcessingRepo)
			.findAllByOrderByFailureDateAsc();

		final List<FailedProcessing> actual = uut.getFailedProcessings();
		
		assertEquals(2, actual.size());
		assertEquals("123", actual.get(0).getId());
		assertEquals("456", actual.get(1).getId());
	}

	@Test
	public void testGetFailedProcessingById_OnExistingId_ShallReturnProduct() {
		doReturn(Optional.of(newFailedProcessing("123")))
			.when(failedProcessingRepo)
			.findById("123");

		final FailedProcessing actual = uut.getFailedProcessingById("123");
		
		assertNotNull(actual);
		assertEquals("123", actual.getId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFailedProcessingById_OnNonExistingId_ShallReturnNull() {
		doReturn(Optional.empty())
			.when(failedProcessingRepo)
			.findById("456");
		
		uut.getFailedProcessingById("456");
	}

	@Test
	public void testDeleteFailedProcessing_OnExistingId_ShallDeleteElement() {
		doReturn(Optional.of(newFailedProcessing("123")))
			.when(failedProcessingRepo)
			.findById("123");

		uut.deleteFailedProcessing("123");

		verify(failedProcessingRepo).deleteById("123");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDeleteFailedProcessing_OnNonExistingId_ShallThrowException() {
		doReturn(Optional.empty())
			.when(failedProcessingRepo)
			.findById("456");
		
		uut.deleteFailedProcessing("456");
	}

	@Test
	public void testRestartAndDeleteFailedProcessing_OnRestartableRequest_ShallRestartAndDelete()
			throws JsonMappingException, JsonProcessingException, AllowedActionNotAvailableException {	
		final FailedProcessing fp = newFailedProcessing("123", "{\"foo\":\"bar\",\"retryCounter\":0,\"allowedActions\":[\"RESTART\"]}");
		doReturn(Optional.of(fp))
			.when(failedProcessingRepo)
			.findById("123");

		uut.restartAndDeleteFailedProcessing("123");
		
		verify(failedProcessingRepo, times(1)).findById("123");
		verify(failedProcessingRepo, times(1)).deleteById("123");
		final Map<String, Object> newMessage = new ObjectMapper().readValue(
				fp.getMessage(), new TypeReference<HashMap<String, Object>>(){});
		newMessage.put("retryCounter", 1);
		verify(messageProducer, times(1)).send(fp.getTopic(), newMessage);
	}
	
	@Test(expected = AllowedActionNotAvailableException.class)
	public void testRestartAndDeleteFailedProcessing_OnNonRestartableRequest_ShallThrowException()
			throws JsonMappingException, JsonProcessingException, AllowedActionNotAvailableException {	
		final FailedProcessing fp = newFailedProcessing("123"); 
		doReturn(Optional.of(fp))
			.when(failedProcessingRepo)
			.findById("123");

		uut.restartAndDeleteFailedProcessing("123");
	}
	
	@Test
	public void testResubmitAndDeleteFailedProcessing_OnDefaultResubmitTopicAndResubmittableRequest_ShallResubmitAndDelete()
			throws JsonMappingException, JsonProcessingException, AllowedActionNotAvailableException {	
		final FailedProcessing fp = newFailedProcessing("123", "{\"foo\":\"bar\",\"retryCounter\":0,\"allowedActions\":[\"RESUBMIT\"],\"additionalFields\":{\"resubmitMessage\":{\"retryCounter\":0}}}");
		doReturn(Optional.of(fp))
			.when(failedProcessingRepo)
			.findById("123");

		uut.resubmitAndDeleteFailedProcessing("123");
		
		verify(failedProcessingRepo, times(1)).findById("123");
		verify(failedProcessingRepo, times(1)).deleteById("123");
		final Map<String, Object> oldMessage = new ObjectMapper().readValue(
				fp.getMessage(), new TypeReference<HashMap<String, Object>>(){});
		@SuppressWarnings("unchecked")
		final Map<String, Object> newMessage = (Map<String, Object>) ((Map<String, Object>)oldMessage.get("additionalFields")).get("resubmitMessage");
		newMessage.put("retryCounter", 1);
		verify(messageProducer, times(1)).send("defResubmitTopic", newMessage);
	}
	
	@Test
	public void testResubmitAndDeleteFailedProcessing_OnCustomResubmitTopicAndResubmittableRequest_ShallResubmitAndDelete()
			throws JsonMappingException, JsonProcessingException, AllowedActionNotAvailableException {	
		final FailedProcessing fp = newFailedProcessing("123", "{\"foo\":\"bar\",\"retryCounter\":0,\"allowedActions\":[\"RESUBMIT\"],\"additionalFields\":{\"resubmitMessage\":{\"retryCounter\":0},\"resubmitTopic\":\"fooTopic\"}}");
		doReturn(Optional.of(fp))
			.when(failedProcessingRepo)
			.findById("123");

		uut.resubmitAndDeleteFailedProcessing("123");
		
		verify(failedProcessingRepo, times(1)).findById("123");
		verify(failedProcessingRepo, times(1)).deleteById("123");
		final Map<String, Object> oldMessage = new ObjectMapper().readValue(
				fp.getMessage(), new TypeReference<HashMap<String, Object>>(){});
		@SuppressWarnings("unchecked")
		final Map<String, Object> newMessage = (Map<String, Object>) ((Map<String, Object>)oldMessage.get("additionalFields")).get("resubmitMessage");
		newMessage.put("retryCounter", 1);
		verify(messageProducer, times(1)).send("fooTopic", newMessage);
	}
	
	@Test(expected = AllowedActionNotAvailableException.class)
	public void testResubmitAndDeleteFailedProcessing_OnExistingTopicAndNonResubmittableRequest_ShallThrowException()
			throws JsonMappingException, JsonProcessingException, AllowedActionNotAvailableException {	
		final FailedProcessing fp = newFailedProcessing("123"); 
		doReturn(Optional.of(fp))
			.when(failedProcessingRepo)
			.findById("123");

		uut.restartAndDeleteFailedProcessing("123");
	}

	@Test(expected = RuntimeException.class)
	public void testRestartAndDeleteFailedProcessing_OnTopicNull_ShallThrowException() throws AllowedActionNotAvailableException {		
		final FailedProcessing fp = newFailedProcessing("456");
		fp.setTopic(null);

		doReturn(Optional.of(fp))
			.when(failedProcessingRepo)
			.findById("456");
		
		uut.restartAndDeleteFailedProcessing("456");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testRestartAndDeleteFailedProcessing_OnMissingRequest_ShallThrowException() throws AllowedActionNotAvailableException {
		doReturn(Optional.empty())
			.when(failedProcessingRepo)
			.findById("789");

		uut.restartAndDeleteFailedProcessing("789");
	}
	
	private FailedProcessing newFailedProcessing(final String id) {
		return newFailedProcessing(id, "{\"foo\":\"bar\",\"retryCounter\":0,\"allowedActions\":[]}");
	}
	
	private FailedProcessing newFailedProcessing(final String id, final String mess) {
		final FailedProcessing failedProcessing = new FailedProcessing();
		failedProcessing.setId(id);
		failedProcessing.setMessage(mess);
		failedProcessing.setTopic("myTopic");
		return failedProcessing;
	}
	
}
