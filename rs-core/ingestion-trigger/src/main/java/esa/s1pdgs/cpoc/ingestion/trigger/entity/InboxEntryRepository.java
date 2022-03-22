package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

@Component
public interface InboxEntryRepository  extends MongoRepository<InboxEntry, Long> {

	List<InboxEntry> findByProcessingPodAndPickupURLAndStationNameAndMissionIdAndProductFamily(final String processingPod,
			final String pickupURL, final String stationName, final String missionId, final String productFamily);

	List<InboxEntry> findByProcessingPodAndPickupURLAndStationNameAndMissionId(final String processingPod,
										final String pickupURL, final String stationName, final String missionId);

}
