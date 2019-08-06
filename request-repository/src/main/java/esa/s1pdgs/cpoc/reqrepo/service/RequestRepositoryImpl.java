package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.common.Processing;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.reqrepo.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.reqrepo.repo.MqiMessageRepo;

@Component
public class RequestRepositoryImpl implements RequestRepository {
	private final FailedProcessingRepo failedProcessingRepo;
	private final MqiMessageRepo mqiMessageRepository;
	private final SubmissionClient kafkaSubmissionClient;

	@Autowired
	public RequestRepositoryImpl(
			final MqiMessageRepo mqiMessageRepository, 
			final FailedProcessingRepo failedProcessingRepo, 
			final SubmissionClient kafkaSubmissionClient
	) {
		this.mqiMessageRepository = mqiMessageRepository;
		this.failedProcessingRepo = failedProcessingRepo;
		this.kafkaSubmissionClient = kafkaSubmissionClient;
	}

	@Override
	public synchronized void saveFailedProcessing(FailedProcessingDto failedProcessingDto) {
		final GenericMessageDto<?> dto = failedProcessingDto.getProcessingDetails();
		final MqiMessage message = mqiMessageRepository.findByIdentifier(dto.getIdentifier());
		assertNotNull("original request", message, dto.getIdentifier());
		failedProcessingRepo.save(FailedProcessing.valueOf(message, failedProcessingDto));
	}

	@Override
	public List<FailedProcessing> getFailedProcessings() {
		return failedProcessingRepo.findAll(Sort.by(Direction.ASC, "creationTime"));
	}

	@Override
	public FailedProcessing getFailedProcessingById(long id) {
		final FailedProcessing failedProcessing = failedProcessingRepo.findById(id);
		assertNotNull("failed request", failedProcessing, id);
		return failedProcessing;
	}

	@Override
	public synchronized void restartAndDeleteFailedProcessing(long id) {
		final FailedProcessing failedProcessing = failedProcessingRepo.findById(id);
		assertNotNull("failed request", failedProcessing, id);
		assertTopicDefined(id, failedProcessing);	
		kafkaSubmissionClient.resubmit(failedProcessing, failedProcessing.getDto());
		failedProcessingRepo.deleteById(id);
	}

	@Override
	public synchronized void deleteFailedProcessing(long id) {		
		final FailedProcessing failedProcessing = failedProcessingRepo.findById(id);		
		assertNotNull("failed request", failedProcessing, id);
		failedProcessingRepo.deleteById(id);
	}
	
	@Override
	public List<String> getProcessingTypes() {
		return PROCESSING_TYPES_LIST;
	}
	
	@Override
	public Processing getProcessing(long id) {		
		final MqiMessage mess = mqiMessageRepository.findByIdentifier(id);		
		if (mess == null) {
			return null;
		}	
		return new Processing(mess);
	}
	
	@Override
	public List<Processing> getProcessings(Integer pageSize, Integer pageNumber, List<String> processingTypes, List<MessageState> processingStatus) {	
		final List<String> topics = processingTypes == null || processingTypes.isEmpty() ? PROCESSING_TYPES_LIST : processingTypes;
		final List<MessageState> states = processingStatus.isEmpty() ? PROCESSING_STATE_LIST : processingStatus;

		// no paging?
		if (pageSize == null) {
			return toExternal(mqiMessageRepository.findByStateInAndTopicInOrderByCreationDate(states, topics));
		}		
		final Pageable pageable = PageRequest.of(pageNumber.intValue(), pageSize.intValue(), Sort.by(Direction.ASC, "creationDate"));
		return toExternal(mqiMessageRepository.findByStateInAndTopicIn(states, topics, pageable).getContent());
	}
	
	private final List<Processing> toExternal(final List<MqiMessage> messages)	{
		if (messages == null)
		{
			return Collections.emptyList();
		}		
		return messages.stream()
				.map(m -> new Processing(m))
				.collect(Collectors.toList());
	}
	
	static final void assertNotNull(final String name, final Object object, final long id) 
			throws IllegalArgumentException {
		if (object == null) {
			throw new IllegalArgumentException(
					String.format("Could not find %s by id %s", name, id)
			);
		}
	}
	
	private static final void assertTopicDefined(long id, final FailedProcessing failedProcessing) {
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
