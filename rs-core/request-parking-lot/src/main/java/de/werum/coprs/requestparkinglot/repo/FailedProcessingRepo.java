package de.werum.coprs.requestparkinglot.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Meta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;

@Service
public interface FailedProcessingRepo extends MongoRepository<FailedProcessing, String>{
	
	@Meta(allowDiskUse = true)
	public List<FailedProcessing> findAllByOrderByFailureDateAsc();
	
	public Optional<FailedProcessing> findById(String id);

	public void deleteById(String id);

}
