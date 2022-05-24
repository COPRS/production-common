package esa.s1pdgs.cpoc.preparation.worker.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.preparation.worker.model.sequence.SequenceId;

@Service
public interface SequenceRepository extends MongoRepository<SequenceId, String> {
}
