package esa.s1pdgs.cpoc.reqrepo.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.reqrepo.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;
import esa.s1pdgs.cpoc.reqrepo.repo.MqiMessageRepo;

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

	public synchronized void saveFailedProcessing(FailedProcessingDto failedProcessingDto) {
		final GenericMessageDto<?> dto = failedProcessingDto.getProcessingDetails();
		final MqiMessage message = findOriginalMessage(dto.getIdentifier());

		if (message == null) {
			String errmsg = String.format("Could not find orginal request by id %s. Message was %s",
					dto.getIdentifier(), failedProcessingDto);
			LOGGER.error(errmsg);
			throw new IllegalArgumentException(errmsg);
		}		
		failedProcessingRepo.save(FailedProcessing.valueOf(message, failedProcessingDto));
	}

	@Override
	public List<FailedProcessing> getFailedProcessings() {
		return failedProcessingRepo
				.findAll(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Direction.ASC, "creationTime")))
				.getContent();
	}

	@Override
	public FailedProcessing getFailedProcessingById(long id) {
		final FailedProcessing failedProcessing = failedProcessingRepo.findByIdentifier(id);
		if (failedProcessing == null) {
			throw new IllegalArgumentException(String.format("Could not find failed request by id %s", id));
		}
		return failedProcessing;
	}

	@Override
	public synchronized void restartAndDeleteFailedProcessing(long id) {
		final FailedProcessing failedProcessing = failedProcessingRepo.findByIdentifier(id);

		if (failedProcessing == null) {
			throw new IllegalArgumentException(String.format("Could not find failed request by id %s", id));
		}
		
		if (failedProcessing.getTopic() == null)
		{
			throw new IllegalArgumentException(
					String.format(
							"Failed to restart request id %s as it has no topic specified (not restartable)", 
							id
					)
			);
		}	
		kafkaSubmissionClient.resubmit(failedProcessing, failedProcessing.getDto());
		failedProcessingRepo.deleteByIdentifier(id);
	}

	@Override
	public synchronized void deleteFailedProcessing(long id) {		
		final FailedProcessing failed = failedProcessingRepo.findByIdentifier(id);		
		if (failed == null) {
			throw new IllegalArgumentException(String.format("Could not find failed request by id %s", id));
		}
		failedProcessingRepo.deleteByIdentifier(id);
	}

	private final MqiMessage findOriginalMessage(final long id) {		
		return mqiMessageRepository.findByIdentifier(id);
	}
}
