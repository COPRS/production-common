package esa.s1pdgs.cpoc.reqrepo.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.common.MessageState;

@Service
public interface MqiMessageRepo extends MongoRepository<MqiMessage, Long> {
	
	MqiMessage findById(long id);
	
	List<MqiMessage> findByStateInAndTopicInOrderByCreationDate(List<MessageState> states, List<String> topics);
	
	long countByStateInAndTopicIn(List<MessageState> states, List<String> topics);
	
	Page<MqiMessage> findByStateInAndTopicIn(List<MessageState> states, List<String> topics, Pageable pageable);	
}
