package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.Collections;
import java.util.Date;
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
import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.reqrepo.config.RequestRepositoryConfiguration;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.reqrepo.repo.MqiMessageRepo;

@Component
public class RequestRepositoryImpl implements RequestRepository {
	private final FailedProcessingRepo failedProcessingRepo;
	private final MqiMessageRepo mqiMessageRepository;
	private final MessageProducer<Object> messageProducer;
	private final AppStatus status;
	private final RequestRepositoryConfiguration config;

	@Autowired
	public RequestRepositoryImpl(
			final MqiMessageRepo mqiMessageRepository,
			final FailedProcessingRepo failedProcessingRepo,
			final MessageProducer<Object> messageProducer,
			final AppStatus status,
			final RequestRepositoryConfiguration config
	) {
		this.mqiMessageRepository = mqiMessageRepository;
		this.failedProcessingRepo = failedProcessingRepo;
		this.messageProducer = messageProducer;
		this.status = status;
		this.config = config;
	}

	@Override
	public synchronized void saveFailedProcessing(final FailedProcessingDto failedProcessingDto) {
		final GenericMessageDto<?> dto = failedProcessingDto.getProcessingDetails();
		
		final MqiMessage message = mqiMessageRepository.findById(dto.getId());		
		final FailedProcessingFactory factory = new FailedProcessingFactory(failedProcessingDto)
				.message(message);

		if (failedProcessingDto.getPredecessor() != null) {
			final MqiMessage predecessorMessage = mqiMessageRepository.findById(failedProcessingDto.getPredecessor().getId());
			if (predecessorMessage != null) {
				factory.predecessorMessage(predecessorMessage);
			}
			else {
				// S1PRO-2302: in case the MqiMessage is already gone, a new one needs to be created in order
				// to allow resubmission/reevaluate.				
				final GenericMessageDto<?> predecessorDto = failedProcessingDto.getPredecessor(); 
				
				final MqiMessage messageClone = new MqiMessage();
		
				messageClone.setId(predecessorDto.getId());
				messageClone.setCreationDate(new Date());
				messageClone.setState(MessageState.READ);
				messageClone.setTopic(predecessorDto.getInputKey());
				messageClone.setDto(predecessorDto.getDto());
				
				factory.predecessorMessage(messageClone);
			}			
		}
		final FailedProcessing failedProcessing = factory.newFailedProcessing();		
		failedProcessingRepo.save(failedProcessing);
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
		assertDtoDefined(id, failedProcessing);
		assertTopicDefined(id, failedProcessing);
		// TODO: is not possible to alter the retry counter here because the object being returned
		// by Jackson is not a AbstractMessage its LinkedHashMap
		// ((AbstractMessage) failedProcessing.getDto()).increaseControlRetryCounter();
		resubmit(id, failedProcessing.getTopic(), failedProcessing.getDto(), status);
		failedProcessingRepo.deleteById(id);
	}
	
	@Override
	public void reevaluateAndDeleteFailedProcessing(final long id) {
		final FailedProcessing failedProcessing = failedProcessingRepo.findById(id);
		assertNotNull("failed request", failedProcessing, id);
		assertPredecessorDefined(id, failedProcessing);
		assertPredecessorTopicDefined(id, failedProcessing);
		// TODO: is not possible to alter the retry counter here because the object being returned
		// by Jackson is not a AbstractMessage its LinkedHashMap	
		// ((AbstractMessage) failedProcessing.getPredecessorDto()).increaseControlRetryCounter(); 
		resubmit(id, failedProcessing.getPredecessorTopic(), failedProcessing.getPredecessorDto(), status);
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
		return config.getKafkaTopicList();
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
		final List<String> topics = topics(processingTypes);
		final List<MessageState> states = processingStatus.isEmpty() ? PROCESSING_STATE_LIST : processingStatus;

		// no paging?
		if (pageSize == null) {
			return toExternal(mqiMessageRepository.findByStateInAndTopicInOrderByCreationDate(states, topics));
		}		
		final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.ASC, "creationDate"));
		return toExternal(mqiMessageRepository.findByStateInAndTopicIn(states, topics, pageable).getContent());
	}
	
	@Override
	public long getProcessingsCount(final List<String> processingTypes, final List<MessageState> processingStatus) {
		final List<String> topics = topics(processingTypes);
		final List<MessageState> states = processingStatus.isEmpty() ? PROCESSING_STATE_LIST : processingStatus;

		return mqiMessageRepository.countByStateInAndTopicIn(states, topics);
	}
	
	private final List<String> topics(final List<String> provided) {
		if (provided == null || provided.isEmpty()) {
			return getProcessingTypes();
		}
		return provided;
	}

	private void resubmit(final long id, final String predecessorTopic, final Object predecessorDto, final AppStatus status) {
		try {
			messageProducer.send(predecessorTopic, predecessorDto);
		} catch (final Exception e) {
			status.getStatus().setFatalError();
			throw new RuntimeException(
					String.format(
							"Error restarting failedRequest '%s' on topic '%s': %s",
							id,
							predecessorTopic,
							e
					),
					e
			);
		}
	}



	private List<Processing> toExternal(final List<MqiMessage> messages)	{
		if (messages == null)
		{
			return Collections.emptyList();
		}		
		return messages.stream()
				.map(Processing::new)
				.collect(Collectors.toList());
	}
	
	static void assertNotNull(final String name, final Object object, final long id)
			throws IllegalArgumentException {
		if (object == null) {
			throw new IllegalArgumentException(
					String.format("Could not find %s by id %s", name, id)
			);
		}
	}
	
	private static void assertDtoDefined(final long id, final FailedProcessing failedProcessing) {
		if (failedProcessing.getDto() == null)
		{
			throw new RuntimeException(
					String.format(
							"Failed to restart request id %s as it has no message specified (not restartable)", 
							id
					)
			);
		}
	}
	
	private static void assertTopicDefined(final long id, final FailedProcessing failedProcessing) {
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
	
	private static void assertPredecessorDefined(final long id, final FailedProcessing failedProcessing) {
		if (failedProcessing.getPredecessorDto() == null)
		{
			throw new RuntimeException(
					String.format(
							"Failed to reevaluate request id %s as it has no predecessor specified (not reevaluatable)", 
							id
					)
			);
		}
	}
	
	private static void assertPredecessorTopicDefined(final long id, final FailedProcessing failedProcessing) {
		if (failedProcessing.getPredecessorTopic() == null)
		{
			throw new RuntimeException(
					String.format(
							"Failed to reevaluate request id %s as it has no predecessor topic specified (not reevaluatable)", 
							id
					)
			);
		}
	}
}
