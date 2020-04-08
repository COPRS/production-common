package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface InboxEntryRepository  extends MongoRepository<InboxEntry, Long> {
	List<InboxEntry> findByPickupURLAndStationName(final String pickupURL, final String stationName);
}
