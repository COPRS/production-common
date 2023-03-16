package de.werum.coprs.requestparkinglot.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.werum.coprs.requestparkinglot.config.RequestParkingLotConfiguration;
import de.werum.coprs.requestparkinglot.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.mqi.model.control.AllowedAction;

@Component
public class RequestParkingLotImpl implements RequestParkingLot {
	private final FailedProcessingRepo failedProcessingRepo;
	private final RequestParkingLotConfiguration config;
	final MessageProducer<Object> messageProducer;

	@Autowired
	public RequestParkingLotImpl(
			final FailedProcessingRepo failedProcessingRepo,
			final RequestParkingLotConfiguration config,
			final MessageProducer<Object> messageProducer
	) {
		this.failedProcessingRepo = failedProcessingRepo;
		this.config = config;
		this.messageProducer = messageProducer;
	}
	
	@Override
	public List<FailedProcessing> getFailedProcessings() {
		return failedProcessingRepo.findAllOrderByFailureDate();
	}
	
	@Override
	public long getFailedProcessingsCount() {
		return failedProcessingRepo.count();
	}

	@Override
	public FailedProcessing getFailedProcessingById(final String id) {
		final Optional<FailedProcessing> failedProcessing = failedProcessingRepo.findById(id);
		assertNotEmpty("failed request", failedProcessing, id);
		return failedProcessing.get();
	}

	@Override
	public synchronized void restartAndDeleteFailedProcessing(final String id) throws AllowedActionNotAvailableException {
		final Optional<FailedProcessing> failedProcessing = failedProcessingRepo.findById(id);
		assertNotEmpty("failed request", failedProcessing, id);
		assertDtoDefined(id, failedProcessing.get());
		assertTopicDefined(id, failedProcessing.get());
		assertRestartable(id, failedProcessing.get().getMessage());
		restart(id, failedProcessing.get().getTopic(), failedProcessing.get().getMessage());
		failedProcessingRepo.deleteById(id);
	}
	
	@Override
	public synchronized void resubmitAndDeleteFailedProcessing(final String id) throws AllowedActionNotAvailableException {
		final Optional<FailedProcessing> failedProcessing = failedProcessingRepo.findById(id);
		assertNotEmpty("failed request", failedProcessing, id);
		assertDtoDefined(id, failedProcessing.get());
		assertTopicDefined(id, failedProcessing.get());
		assertResubmitable(id, failedProcessing.get().getMessage());
		resubmit(id, failedProcessing.get().getMessage());
		failedProcessingRepo.deleteById(id);
	}
	
	@Override
	public synchronized void deleteFailedProcessing(final String id) {		
		final Optional<FailedProcessing> failedProcessing = failedProcessingRepo.findById(id);		
		assertNotEmpty("failed request", failedProcessing, id);
		failedProcessingRepo.deleteById(id);
	}
	
	private void restart(final String id, final String topic, final String message) {		
		try {
			final Map<String, Object> map = new ObjectMapper().readValue(
					message, new TypeReference<HashMap<String, Object>>(){});
			map.put("retryCounter", (int)map.get("retryCounter") + 1);
			messageProducer.send(topic, map);
		} catch (final Exception e) {
			throw new RuntimeException(
					String.format(
							"Error restarting failedRequest '%s' on topic '%s': %s",
							id,
							topic,
							e
					),
					e
			);
		}
	}

	@SuppressWarnings("unchecked")
	private void resubmit(final String id, final String message) {
		String topic = config.getDefaultResubmitTopic();
		final Map<String, Object> resubmitMessage;
		try {
			Map<String, Object> map = new ObjectMapper().readValue(
					message, new TypeReference<HashMap<String, Object>>(){});
			final Map<String, Object> additionalFields = (Map<String, Object>)map.get("additionalFields");
			if (null == additionalFields || null == additionalFields.get("resubmitMessage")) {
				throw new RuntimeException(
						String.format(
								"Failed to resubmit request id %s as it has no resubmitMessage specified (not resubmittable)",
								id
						)
				);
			}
			if (null != additionalFields.get("resubmitTopic")
					&& ((String)additionalFields.get("resubmitTopic")).length() > 0) {
				topic = (String)additionalFields.get("resubmitTopic");
			}
			resubmitMessage = (Map<String, Object>)additionalFields.get("resubmitMessage");
		} catch (JsonProcessingException e1) {
			throw new RuntimeException(
					String.format(
							"Failed to resubmit request id %s as it has no resubmitMessage specified (not resubmittable)",
							id
					)
			);
		}
				
		try {
			resubmitMessage.put("retryCounter", (int)resubmitMessage.get("retryCounter") + 1);
			messageProducer.send(topic, resubmitMessage);
		} catch (final Exception e2) {
			throw new RuntimeException(
					String.format(
							"Error resubmitting failedRequest '%s' on topic '%s': %s",
							id,
							topic,
							e2
					),
					e2
			);
		}
	}
	
	static void assertNotEmpty(final String name, final Optional<FailedProcessing> failedProcessing, final String id)
			throws IllegalArgumentException {
		if (failedProcessing.isEmpty()) {
			throw new IllegalArgumentException(
					String.format("Could not find %s by id %s", name, id)
			);
		}
	}
	
	private static void assertDtoDefined(final String id, final FailedProcessing failedProcessing) {
		if (failedProcessing.getMessage() == null)
		{
			throw new RuntimeException(
					String.format(
							"Failed to restart request id %s as it has no message specified (not restartable)", 
							id
					)
			);
		}
	}
	
	private static void assertTopicDefined(final String id, final FailedProcessing failedProcessing) {
		if (failedProcessing.getTopic() == null)
		{
			throw new RuntimeException(
					String.format(
							"Failed to restart request id %s as it has no topic specified (not restartable)", 
							id
					)
			);
		}
	}
	
	private static void assertRestartable(final String id, final String message) throws AllowedActionNotAvailableException {
		final List<String> allowedActions = getAllowedActions(message);
		if (!allowedActions.contains(AllowedAction.RESTART.name())) {
			throw new AllowedActionNotAvailableException(
					String.format(
							"Failed to restart request id %s as RESTART is not part of its allowed actions '%s'",
							id,
							String.join(", ", allowedActions)
					)
			);
		}
	}
	
	private static void assertResubmitable(final String id, final String message) throws AllowedActionNotAvailableException {
		final List<String> allowedActions = getAllowedActions(message);
		if (!allowedActions.contains(AllowedAction.RESUBMIT.name())) {
			throw new AllowedActionNotAvailableException(
					String.format(
							"Failed to resubmit request id %s as RESUBMIT is not part of its allowed actions '%s'",
							id,
							String.join(", ", allowedActions)
					)
			);
		}
	}
	
	private static List<String> getAllowedActions(final String message) {
		try {
			final Map<String, Object> map = new ObjectMapper().readValue(
					message, new TypeReference<HashMap<String, Object>>(){});
			@SuppressWarnings("unchecked")
			final List<String> allowedActions = (List<String>)map.get("allowedActions");		
			return null != allowedActions ? allowedActions : Collections.emptyList();
		} catch (JsonProcessingException e) {
			return Collections.emptyList();
		}		
	}

}
