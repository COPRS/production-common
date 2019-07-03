package esa.s1pdgs.cpoc.reqrepo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;

@Service
public interface MqiMessageRepo extends MongoRepository<MqiMessage, Long> {
	
	MqiMessage findByIdentifier(long identifier);
	
//	@Query("select m from MqiMessage m where m.bar = :bar and (:goo is null or foo.goo = :goo)")	
//	Page<MqiMessage> findByQuery(Pageable page, List);
}
