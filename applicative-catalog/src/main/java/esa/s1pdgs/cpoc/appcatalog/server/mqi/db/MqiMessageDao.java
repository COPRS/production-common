/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.server.mqi.db;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Service to access to MQI messages in mongo db
 * 
 * @author Viveris Technologies
 */
@Service
public class MqiMessageDao {

    private static final String ID_NAME = "_id";
	/**
     * Mongo DB client
     */
    private final MongoTemplate mongoClient;

    /**
     * @param mongoClient
     */
    @Autowired
    public MqiMessageDao(final MongoTemplate mongoClient) {
        this.mongoClient = mongoClient;
    }

    /**
     * Find messages
     * 
     * @param query
     * @return
     */
    private List<MqiMessage> find(final Query query) {
        return mongoClient.find(query, MqiMessage.class);
    }

    /**
     * Return a list of message which contains the right topic, partition,
     * offset and group
     * 
     * @param topic
     * @param partition
     * @param offset
     * @param group
     * @return the list of message
     */
    public List<MqiMessage> searchByTopicPartitionOffsetGroup(
            final String topic, final int partition, final long offset,
            final String group) {
        Query query = query(where("topic").is(topic).and("partition")
                .is(partition).and("offset").is(offset).and("group").is(group));
        return find(query);
    }

    /**
     * Return a list of message which contains the right topic, partition and
     * group
     * 
     * @param topic
     * @param partition
     * @param group
     * @return the list of message
     */
    public List<MqiMessage> searchByTopicPartitionGroup(final String topic,
            final int partition, final String group,
            final Set<MessageState> states) {
        Query query = query(where("topic").is(topic).and("partition")
                .is(partition).and("group").is(group).and("state").nin(states));
        query.with(new Sort(Direction.ASC, "lastReadDate"));
        return find(query);
    }

    /**
     * Return a list of message which contains the right pod name, product
     * category but its not in the states
     * 
     * @param pod
     * @param category
     * @param states
     * @return the list of message
     */
    public List<MqiMessage> searchByPodStateCategory(final String pod,
            final ProductCategory category,
            final Set<MessageState> states) {
        Query query = query(where("readingPod").is(pod).and("state").nin(states)
                .and("category").is(category));
        query.with(new Sort(Direction.ASC, "creationDate"));
        return find(query);
    }

    /**
     * Return a list of message which contains the right pod name, product
     * category but its not in the states
     * 
     * @param pod
     * @param category
     * @param states
     * @return the list of message
     */
    public int countReadingMessages(final String pod, final String topic) {
        Query query = query(where("readingPod").is(pod).and("state")
                .is(MessageState.READ).and("topic").is(topic));
        query.with(new Sort(Direction.ASC, "creationDate"));
        return find(query).size();
    }

    /**
     * Return a list of message which contains the right ID
     * 
     * @param messageID
     * @return the list of message
     */
    public List<MqiMessage> searchByID(final long messageID) {
        Query query = query(where(ID_NAME).is(messageID));
        return find(query);
    }

    /**
     * Insert a MQI message
     * 
     * @param messageToInsert
     */
    public void insert(final MqiMessage messageToInsert) {
        mongoClient.insert(messageToInsert);
    }

    /**
     * @param query
     * @param update
     */
    private void updateFirst(final Query query, final Update update) {
        mongoClient.updateFirst(query, update, MqiMessage.class);
    }

    /**
     * Function which update a MqiMessage
     * 
     * @param messageID
     * @param updateMap
     */
    public void updateByID(final long messageID,
            final Map<String, Object> updateMap) {
        Query query = query(where(ID_NAME).is(messageID));
        Update update = new Update();
        updateMap.forEach((k, v) -> update.set(k, v));
        updateFirst(query, update);
    }

    /**
     * @param query
     */
    public void findAllAndRemove(final Query query) {
        mongoClient.findAllAndRemove(query, MqiMessage.class);
    }
}
