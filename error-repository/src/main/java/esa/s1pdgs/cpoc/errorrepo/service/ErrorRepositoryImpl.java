package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.result.DeleteResult;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;


import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@Component
public class ErrorRepositoryImpl implements ErrorRepository {

	private final MongoTemplate mongoTemplate;

	@Autowired
	public ErrorRepositoryImpl(final MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public void saveFailedProcessing(FailedProcessingDto failedProcessing) {		
		final MqiGenericMessageDto<?> dto = (MqiGenericMessageDto<?>) failedProcessing.getDto();
		
		final MqiMessage message = findOriginalMessage(dto.getIdentifier());
		
		if (message == null)
		{
			throw new IllegalArgumentException(
					String.format(
							"Could not find orginal request by id %s. Message was %s", 
							dto.getIdentifier(),
							failedProcessing
					)
			);			
		}
		failedProcessing
			.lastAssignmentDate(message.getLastReadDate())
			.sendingPod(message.getReadingPod())
			.lastSendDate(message.getLastSendDate())
			.lastAckDate(message.getLastAckDate())
			.nbRetries(message.getNbRetries())
			.creationDate(message.getCreationDate());
		
		mongoTemplate.insert(failedProcessing);
	}

	@Override
	public List<FailedProcessingDto> getFailedProcessings() {
		List<FailedProcessingDto> failedProcessings = mongoTemplate.findAll(FailedProcessingDto.class);
		return failedProcessings;
	}

	@Override
	public FailedProcessingDto getFailedProcessingsById(String id) {
		FailedProcessingDto failedProcessing = mongoTemplate.findById(id, FailedProcessingDto.class);
		return failedProcessing;
	}

	@Override
	public void restartAndDeleteFailedProcessing(String id) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean deleteFailedProcessing(String id) {
		FailedProcessingDto failedProcessing = mongoTemplate.findById(id, FailedProcessingDto.class);
		if (failedProcessing == null) {
			return false;
		}
		if (failedProcessing != null) {
			DeleteResult result = mongoTemplate.remove(failedProcessing);
			if (result.getDeletedCount() == 0) {
				return false;
			}
		}
		return true;
	}
	
	private final MqiMessage findOriginalMessage(final long id)
	{
		return mongoTemplate.findOne(
				query(where("identifier").is(id)),
				MqiMessage.class
		);	
	}

}
