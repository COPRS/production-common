package esa.s1pdgs.cpoc.reqrepo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;

@Service
public interface FailedProcessingRepo extends MongoRepository<FailedProcessing, Long>{

	public FailedProcessing findById(long id);

	public void deleteById(long id);
}
