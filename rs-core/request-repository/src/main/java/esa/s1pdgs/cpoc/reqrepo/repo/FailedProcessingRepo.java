package esa.s1pdgs.cpoc.reqrepo.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;

@Service
public interface FailedProcessingRepo extends MongoRepository<FailedProcessing, String>{

	public Optional<FailedProcessing> findById(String id);

	public void deleteById(String id);

}
