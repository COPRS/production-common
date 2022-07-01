package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.reqrepo.config.RequestRepositoryConfiguration;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;

@Component
public class RequestRepositoryImpl implements RequestRepository {
	private final FailedProcessingRepo failedProcessingRepo;
	private final RequestRepositoryConfiguration config;

	@Autowired
	public RequestRepositoryImpl(
			final FailedProcessingRepo failedProcessingRepo,
			final RequestRepositoryConfiguration config
	) {
		this.failedProcessingRepo = failedProcessingRepo;
		this.config = config;
	}
	
	@Override
	public List<FailedProcessing> getFailedProcessings() {
		return failedProcessingRepo.findAll(Sort.by(Direction.ASC, "creationTime"));
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
		// TODO: is not possible to alter the retry counter here because the object being returned
		// by Jackson is not a AbstractMessage its LinkedHashMap
		// ((AbstractMessage) failedProcessing.getDto()).increaseControlRetryCounter();
		
		///resubmit(id, failedProcessing.getTopic(), failedProcessing.getDto());
		///failedProcessingRepo.deleteById(id);
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

	private void resubmit(final long id, final String predecessorTopic, final Object predecessorDto) {

// FIXME: Instead of the removed messageProducer use e.g. StreamBridge or similar
		
//		try {
//			messageProducer.send(predecessorTopic, predecessorDto);
//		} catch (final Exception e) {
//			status.getStatus().setFatalError();
//			throw new RuntimeException(
//					String.format(
//							"Error restarting failedRequest '%s' on topic '%s': %s",
//							id,
//							predecessorTopic,
//							e
//					),
//					e
//			);
//		}
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
