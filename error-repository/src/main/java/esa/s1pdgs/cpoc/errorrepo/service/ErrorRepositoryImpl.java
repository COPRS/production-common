package esa.s1pdgs.cpoc.errorrepo.service;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.result.DeleteResult;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.errorrepo.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.seq.SequenceDao;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@Component
public class ErrorRepositoryImpl implements ErrorRepository {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(ErrorRepositoryImpl.class);

	private final MongoTemplate mongoTemplate;
	private final SubmissionClient kafkaSubmissionClient;
	private final SequenceDao seq;

	@Autowired
	public ErrorRepositoryImpl(final MongoTemplate mongoTemplate, final SubmissionClient kafkaSubmissionClient, final SequenceDao seq) {
		this.mongoTemplate = mongoTemplate;
		this.kafkaSubmissionClient = kafkaSubmissionClient;
		this.seq = seq;
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
		failedProcessing.setIdentifier(seq.getNextSequenceId("errorMessage"));
		failedProcessing
				.partition(message.getPartition())
				.offset(message.getOffset())
				.lastAssignmentDate(message.getLastReadDate())
				.sendingPod(message.getReadingPod())
				.lastSendDate(message.getLastSendDate())
				.lastAckDate(message.getLastAckDate())
				.nbRetries(message.getNbRetries())
				.creationDate(message.getCreationDate());

		mongoTemplate.insert(failedProcessing);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<FailedProcessingDto> getFailedProcessings() {
		List<FailedProcessingDto> failedProcessings = mongoTemplate.findAll(FailedProcessingDto.class);
		return failedProcessings;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public FailedProcessingDto getFailedProcessingsById(long id) {
		FailedProcessingDto failedProcessing = mongoTemplate.findOne(query(where("identifier").is(id)), FailedProcessingDto.class);
		return failedProcessing;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public synchronized void restartAndDeleteFailedProcessing(long id) {
		final FailedProcessingDto failedProcessing = getFailedProcessingsById(id);

		if (failedProcessing == null) {
			throw new IllegalArgumentException(String.format("Could not find failed request by id %s", id));
		}

		final MqiGenericMessageDto<?> dto = (MqiGenericMessageDto<?>) failedProcessing.getDto();
		final MqiMessage message = findOriginalMessage(dto.getIdentifier());

		if (message == null) {
			throw new IllegalArgumentException(String.format("Could not find original request by id %s. Message was %s",
					dto.getIdentifier(), failedProcessing));
		}
		kafkaSubmissionClient.resubmit(failedProcessing, dto);

		// no error? remove from error queue
		mongoTemplate.remove(id);	
	}

	@SuppressWarnings("rawtypes")
	@Override
	public synchronized void deleteFailedProcessing(long id) {

		final FailedProcessingDto failedProcessing = getFailedProcessingsById(id);
		if (failedProcessing == null) {
			throw new IllegalArgumentException(String.format("Could not find failed request by id %s", id));
		}

		DeleteResult result = mongoTemplate.remove(failedProcessing);
		if (result.getDeletedCount() == 0) {
			throw new RuntimeException(String.format("Could not delete failed request with id %s", id));
		}
	}

	private final MqiMessage findOriginalMessage(final long id) {
		return mongoTemplate.findOne(query(where("identifier").is(id)), MqiMessage.class);
	}
}
