package esa.s1pdgs.cpoc.ingestion.trigger.cadip;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CadipStateRepository extends MongoRepository<CadipState, Long>{

}
