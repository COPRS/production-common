package esa.s1pdgs.cpoc.errorrepo.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public interface FailedProcessingRepo extends MongoRepository<FailedProcessingDto, Long>{

	public FailedProcessingDto findByIdentifier(long identifier);

	List<FailedProcessingDto> findAllOrderByCreationDateAsc();
	
	public void deleteByIdentfier(long identifier);
}
