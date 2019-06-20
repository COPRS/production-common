package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.errorrepo.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.errorrepo.repo.MqiMessageRepo;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@Component
public class ErrorRepositoryImpl implements ErrorRepository {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(ErrorRepositoryImpl.class);

	private final FailedProcessingRepo failedProcessingRepo;
	private final MqiMessageRepo mqiMessageRepository;
	private final SubmissionClient kafkaSubmissionClient;

	@Autowired
	public ErrorRepositoryImpl(final MqiMessageRepo mqiMessageRepository, final FailedProcessingRepo failedProcessingRepo, final SubmissionClient kafkaSubmissionClient) {
		this.mqiMessageRepository = mqiMessageRepository;
		this.failedProcessingRepo = failedProcessingRepo;
		this.kafkaSubmissionClient = kafkaSubmissionClient;
	}

	public synchronized void saveFailedProcessing(FailedProcessingDto failedProcessing) {
		final GenericMessageDto<?> dto = (GenericMessageDto<?>) failedProcessing.getDto();
		final MqiMessage message = findOriginalMessage(dto.getIdentifier());

		if (message == null) {
			String errmsg = String.format("Could not find orginal request by id %s. Message was %s",
					dto.getIdentifier(), failedProcessing);

			LOGGER.error(errmsg);

			throw new IllegalArgumentException(errmsg);
		}
		failedProcessing.setIdentifier(message.getIdentifier());
		failedProcessing
				.group(message.getGroup())
				.partition(message.getPartition())
				.offset(message.getOffset())
				.lastAssignmentDate(message.getLastReadDate())
				.sendingPod(message.getReadingPod())
				.lastSendDate(message.getLastSendDate())
				.lastAckDate(message.getLastAckDate())
				.nbRetries(message.getNbRetries())
				.creationDate(message.getCreationDate());
		
		failedProcessingRepo.save(failedProcessing);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<FailedProcessingDto> getFailedProcessings() {
		List<FailedProcessingDto> failedProcessings = failedProcessingRepo.findAll();
		Collections.sort(failedProcessings, FailedProcessingDto.ASCENDING_CREATION_TIME_COMPERATOR);
				
		return failedProcessings;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public FailedProcessingDto getFailedProcessingById(long id) {
		return failedProcessingRepo.findByIdentifier(id);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public synchronized void restartAndDeleteFailedProcessing(long id) {
		final FailedProcessingDto failedProcessing = getFailedProcessingById(id);

		if (failedProcessing == null) {
			throw new IllegalArgumentException(String.format("Could not find failed request by id %s", id));
		}

		final GenericMessageDto<?> dto = (GenericMessageDto<?>) failedProcessing.getDto();
		
		if (failedProcessing.getTopic() == null)
		{
			throw new IllegalArgumentException(
					String.format(
							"Failed to restart request id %s as it has no topic specified (not restartable)", 
							id
					)
			);
		}	
		kafkaSubmissionClient.resubmit(failedProcessing, dto.getBody());
		deleteFailedProcessing(id);
	}

	@Override
	public synchronized void deleteFailedProcessing(long id) {		
		final FailedProcessingDto failed = failedProcessingRepo.findByIdentifier(id);
		
		if (failed == null)
		{
			throw new IllegalArgumentException(String.format("Could not find failed request by id %s", id));
		}
		failedProcessingRepo.deleteByIdentifier(id);
	}

	private final MqiMessage findOriginalMessage(final long id) {		
		return mqiMessageRepository.findByIdentifier(id);
	}
}
