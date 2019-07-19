package esa.s1pdgs.cpoc.inbox.entity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface InboxEntryRepository extends CrudRepository<InboxEntry, Long>{
	void deleteByUrl(String url);
}
