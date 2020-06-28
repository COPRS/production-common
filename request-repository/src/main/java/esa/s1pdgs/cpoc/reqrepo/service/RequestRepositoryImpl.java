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
import esa.s1pdgs.cpoc.appstatus.AppStatus;
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
	private final AppStatus status;

	@Autowired
	public RequestRepositoryImpl(
			final MqiMessageRepo mqiMessageRepository, 
			final FailedProcessingRepo failedProcessingRepo, 
			final SubmissionClient kafkaSubmissionClient,
			final AppStatus status
	) {
		this.mqiMessageRepository = mqiMessageRepository;
		this.failedProcessingRepo = failedProcessingRepo;
		this.kafkaSubmissionClient = kafkaSubmissionClient;
		this.status = status;
	}

	@Override
	public synchronized void saveFailedProcessing(final FailedProcessingDto failedProcessingDto) {
		failedProcessingRepo.save(FailedProcessing.valueOf(firstMessageOf(failedProcessingDto), failedProcessingDto));
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
	public FailedProcessing getFailedProcessingById(final long id) {
		final FailedProcessing failedProcessing = failedProcessingRepo.findById(id);
		assertNotNull("failed request", failedProcessing, id);
		return failedProcessing;
	}

	@Override
	public synchronized void restartAndDeleteFailedProcessing(final long id) {
		final FailedProcessing failedProcessing = failedProcessingRepo.findById(id);
		assertNotNull("failed request", failedProcessing, id);
		assertTopicDefined(id, failedProcessing);	
		kafkaSubmissionClient.resubmit(failedProcessing, failedProcessing.getDtos(), status);
		failedProcessingRepo.deleteById(id);
	}

	@Override
	public synchronized void deleteFailedProcessing(final long id) {		
		final FailedProcessing failedProcessing = failedProcessingRepo.findById(id);		
		assertNotNull("failed request", failedProcessing, id);
		failedProcessingRepo.deleteById(id);
	}
	
	@Override
	public List<String> getProcessingTypes() {
		return PROCESSING_TYPES_LIST;
	}
	
	@Override
	public Processing getProcessing(final long id) {		
		final MqiMessage mess = mqiMessageRepository.findById(id);		
		if (mess == null) {
			return null;
		}	
		return new Processing(mess);
	}
	
	@Override
	public List<Processing> getProcessings(final Integer pageSize, final Integer pageNumber, final List<String> processingTypes, final List<MessageState> processingStatus) {	
		final List<String> topics = processingTypes == null || processingTypes.isEmpty() ? PROCESSING_TYPES_LIST : processingTypes;
		final List<MessageState> states = processingStatus.isEmpty() ? PROCESSING_STATE_LIST : processingStatus;

		// no paging?
		if (pageSize == null) {
			return toExternal(mqiMessageRepository.findByStateInAndTopicInOrderByCreationDate(states, topics));
		}		
		final Pageable pageable = PageRequest.of(pageNumber.intValue(), pageSize.intValue(), Sort.by(Direction.ASC, "creationDate"));
		return toExternal(mqiMessageRepository.findByStateInAndTopicIn(states, topics, pageable).getContent());
	}
	
	@Override
	public long getProcessingsCount(final List<String> processingTypes, final List<MessageState> processingStatus) {
		final List<String> topics = processingTypes == null || processingTypes.isEmpty() ? PROCESSING_TYPES_LIST : processingTypes;
		final List<MessageState> states = processingStatus.isEmpty() ? PROCESSING_STATE_LIST : processingStatus;

		return mqiMessageRepository.countByStateInAndTopicIn(states, topics);
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
	
	private final MqiMessage firstMessageOf(final FailedProcessingDto failedProcessingDto) {
		final List<GenericMessageDto<?>> dtos = failedProcessingDto.getProcessingDetails();
		for (final GenericMessageDto<?> dto : dtos) {
			final MqiMessage message = mqiMessageRepository.findById(dto.getId());
			assertNotNull("original request", message, dto.getId());
			return message;
		}
		throw new IllegalArgumentException(
				String.format("No message found in FailedProcessingDto: %s", failedProcessingDto) 
		);
	}

	
	private static final void assertTopicDefined(final long id, final FailedProcessing failedProcessing) {
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
