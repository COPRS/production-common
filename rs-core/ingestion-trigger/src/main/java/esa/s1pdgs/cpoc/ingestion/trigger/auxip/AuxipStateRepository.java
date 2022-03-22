package esa.s1pdgs.cpoc.ingestion.trigger.auxip;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuxipStateRepository extends MongoRepository<AuxipState, Long> {

	Optional<AuxipState> findByProcessingPodAndPripUrlAndProductFamily(final String processingPod, final String pripUrl,
			final String productFamily);

}
