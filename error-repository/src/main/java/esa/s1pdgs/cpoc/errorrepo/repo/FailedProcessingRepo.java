package esa.s1pdgs.cpoc.errorrepo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

@Service
public interface FailedProcessingRepo extends MongoRepository<FailedProcessingDto, Long>{

	public FailedProcessingDto findByIdentifier(long identifier);
	
	public void deleteByIdentifier(long identifier);
}
