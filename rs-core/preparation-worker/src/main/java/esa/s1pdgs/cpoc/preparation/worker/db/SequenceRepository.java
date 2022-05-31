package esa.s1pdgs.cpoc.preparation.worker.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.preparation.worker.model.sequence.SequenceId;

@Component
public interface SequenceRepository extends MongoRepository<SequenceId, String> {
}
