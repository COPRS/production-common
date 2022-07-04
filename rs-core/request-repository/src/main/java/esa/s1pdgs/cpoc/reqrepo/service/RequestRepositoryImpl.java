package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.reqrepo.config.RequestRepositoryConfiguration;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;

@Component
public class RequestRepositoryImpl implements RequestRepository {
	private final FailedProcessingRepo failedProcessingRepo;
	private final RequestRepositoryConfiguration config;
	final MessageProducer<Object> messageProducer;

	@Autowired
	public RequestRepositoryImpl(
			final FailedProcessingRepo failedProcessingRepo,
			final RequestRepositoryConfiguration config,
			final MessageProducer<Object> messageProducer
	) {
		this.failedProcessingRepo = failedProcessingRepo;
		this.config = config;
		this.messageProducer = messageProducer;
	}
	
	@Override
	public List<FailedProcessing> getFailedProcessings() {
		return failedProcessingRepo.findAll(Sort.by(Direction.ASC, "failureDate"));
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
	public synchronized void restartAndDeleteFailedProcessing(final String id) {
		final Optional<FailedProcessing> failedProcessing = failedProcessingRepo.findById(id);
		assertNotEmpty("failed request", failedProcessing, id);
		assertDtoDefined(id, failedProcessing.get());
		assertTopicDefined(id, failedProcessing.get());	
		restart(id, failedProcessing.get().getTopic(), failedProcessing.get().getMessage());
		failedProcessingRepo.deleteById(id);
	}
	
	@Override
	public synchronized void deleteFailedProcessing(final String id) {		
		final Optional<FailedProcessing> failedProcessing = failedProcessingRepo.findById(id);		
		assertNotEmpty("failed request", failedProcessing, id);
		failedProcessingRepo.deleteById(id);
	}
	
	@Override
	public List<String> getProcessingTypes() {
		return config.getKafkaTopicList();
	}

	private void restart(final String id, final String topic, final String message) {
		final JSONObject json = new JSONObject(message);
		Map<String, Object> map = json.toMap();
		map.put("retryCounter", (int)map.get("retryCounter") + 1);
		
		try {
			messageProducer.send(topic, message);
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

}
