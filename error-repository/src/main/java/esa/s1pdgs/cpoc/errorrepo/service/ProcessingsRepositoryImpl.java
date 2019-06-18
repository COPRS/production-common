package esa.s1pdgs.cpoc.errorrepo.service;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.errorrepo.model.rest.ProcessingDto;

@Component
public class ProcessingsRepositoryImpl implements ProcessingsRepository {

	private final MongoTemplate mongoTemplate;
	
	@Autowired
	public ProcessingsRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getProcessingTypes() {
		return (List<String>) mongoTemplate.getCollection("mqiMessage")
		    .distinct("topic", String.class);
	}

	@Override
	public final List<ProcessingDto> getProcessings() {
		return toExternal(mongoTemplate.findAll(MqiMessage.class));
	}
	
	@Override
	public ProcessingDto getProcessing(long id) {		
		final MqiMessage mess = mongoTemplate.findOne(query(where("identifier").is(id)), MqiMessage.class);
		
		if (mess == null)
		{
			return null;
		}	
		return toProcessingDto(mess);
	}
	
	private final List<ProcessingDto> toExternal(final List<MqiMessage> messages)	{
		if (messages == null || messages.size() == 0)
		{
			return Collections.emptyList();
		}		
		return messages.stream()
				.map(m -> toProcessingDto(m))
				.sorted(ProcessingDto.ASCENDING_CREATION_TIME_COMPERATOR)
				.collect(Collectors.toList());
	}
	
	private final ProcessingDto toProcessingDto(final MqiMessage message)
	{
		return new ProcessingDto()
				.identifier(message.getIdentifier())
				.processingType(message.getTopic())
				.processingStatus(message.getState())
				.productCategory(message.getCategory())
				.partition(message.getPartition())
				.offset(message.getOffset())
				.group(message.getGroup())
				.assignedPod(message.getReadingPod())
				.lastAssignmentDate(message.getLastReadDate())
				.sendingPod(message.getSendingPod())
				.lastSendDate(message.getLastSendDate())
				.lastAckDate(message.getLastAckDate())
				.nbRetries(message.getNbRetries())
				.creationDate(message.getCreationDate())
				.processingDetails(message.getDto());
	}

}
