package esa.s1pdgs.cpoc.inbox.polling.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PickupContentRepository extends CrudRepository<PickupContent, Long>{
	long deleteByUrl(String url);
}
