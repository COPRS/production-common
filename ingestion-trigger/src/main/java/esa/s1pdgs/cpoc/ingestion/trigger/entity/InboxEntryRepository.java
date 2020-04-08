package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface InboxEntryRepository extends CrudRepository<InboxEntry, Long>{	
	List<InboxEntry> findByPickupURLAndStationName(final String pickupURL, final String stationName);
}
