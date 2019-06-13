package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

@Component
public class ErrorRepositoryImpl implements ErrorRepository {

	private final MongoTemplate mongoTemplate;

	@Autowired
	public ErrorRepositoryImpl(final MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
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
	public void deleteFailedProcessing(String id) {
		// TODO Auto-generated method stub

	}

}
