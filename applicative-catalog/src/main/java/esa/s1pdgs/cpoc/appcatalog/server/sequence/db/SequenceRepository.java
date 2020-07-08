package esa.s1pdgs.cpoc.appcatalog.server.sequence.db;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface SequenceRepository extends MongoRepository<SequenceId, String> {
}
