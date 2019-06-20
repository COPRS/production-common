package esa.s1pdgs.cpoc.errorrepo.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;

@Service
public interface MqiMessageRepo extends MongoRepository<MqiMessage, Long> {
	
	MqiMessage findByIdentifier(long identifier);
	
	List<MqiMessage> findAllOrderByCreationDate();
	
	Page<MqiMessage> findAll(Pageable p);
}
