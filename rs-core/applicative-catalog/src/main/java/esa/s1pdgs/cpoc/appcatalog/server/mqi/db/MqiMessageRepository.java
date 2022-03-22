package esa.s1pdgs.cpoc.appcatalog.server.mqi.db;

import java.util.Date;
import java.util.List;
import java.util.Set;

//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

@Service
public interface MqiMessageRepository extends MongoRepository<MqiMessage, Long> {
	
	public List<MqiMessage> findByTopicAndPartitionAndOffsetAndGroup(
            final String topic, final int partition, final long offset, final String group);
	
	public List<MqiMessage> findByTopicAndPartitionAndGroupAndStateNotInOrderByLastReadDateAsc(final String topic,
            final int partition, final String group, final Set<MessageState> states);
		
	public List<MqiMessage> findByReadingPodAndCategoryAndStateNotInOrderByCreationDateAsc(final String readingPod, final ProductCategory category,
			final Set<MessageState> states);
	
	@Query(value = "{ 'readingPod' : ?0, 'topic' : ?1, 'state' : 'READ' }", count = true)
	public int countByReadingPodAndTopicAndStateIsRead(final String readingPod, final String topic);
	
	@Query(value = "{ 'lastReadDate': { $lt: ?0 }, 'lastSendDate' : { $lt: ?0 }, 'lastAckDate': { $lt: ?0 } }", delete = true)
	public void truncateBefore(final Date date);
}
