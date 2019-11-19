package esa.s1pdgs.cpoc.ingestion.trigger.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface InboxEntryRepository extends CrudRepository<InboxEntry, Long>{
	//void deleteByUrl(String url);
	
	void deleteByUrlIn(Collection<String> url);
	
	List<InboxEntry> findByPickupPath(final String pickupPath);
}
