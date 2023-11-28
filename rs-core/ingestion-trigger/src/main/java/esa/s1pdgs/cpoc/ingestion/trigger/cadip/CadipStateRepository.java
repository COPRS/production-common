package esa.s1pdgs.cpoc.ingestion.trigger.cadip;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CadipStateRepository extends MongoRepository<CadipState, Long> {

	Optional<CadipState> findByPodAndCadipUrlAndSatelliteId(final String pod, final String cadipUrl,
			final String satelliteId);
}
