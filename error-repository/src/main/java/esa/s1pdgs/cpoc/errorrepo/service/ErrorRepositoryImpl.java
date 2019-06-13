package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;

@Component
public class ErrorRepositoryImpl implements ErrorRepository {

	private final MongoTemplate mongoTemplate;

	@Autowired
	public ErrorRepositoryImpl(final MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;		
	}

	@Override
	public List<FailedProcessing> getFailedProcessings() {		
		List<FailedProcessing> failedProcessings = mongoTemplate.findAll(FailedProcessing.class);
		return failedProcessings;
	}

	@Override
	public FailedProcessing getFailedProcessingsById(String id) {
		FailedProcessing failedProcessing = mongoTemplate.findById(id,FailedProcessing.class);
		return failedProcessing;
	}

	@Override
	public void restartAndDeleteFailedProcessing(String id) {
		// TODO Auto-generated method stub
	}

}
