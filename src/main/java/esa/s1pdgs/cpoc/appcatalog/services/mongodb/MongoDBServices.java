/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.services.mongodb;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.model.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Services to access mongoDB data
 * 
 * @author Viveris Technologies
 */
@Service
public class MongoDBServices {

	private static final String MQI_MSG_SEQ_KEY = "mqiMessage";
    
    /**
     * DAO for mongoDB
     */
    private final MongoDBDAO mongoDBDAO;
    
    /**
     * DAO for mongoDB
     */
    private final SequenceDao sequenceDao;
    
    /**
     * Constructor for the Services
     * 
     * @param mongoDBDAO
     */
    @Autowired
    public MongoDBServices(final MongoDBDAO mongoDBDAO,
    		final SequenceDao sequenceDao) {
        this.mongoDBDAO = mongoDBDAO;
        this.sequenceDao = sequenceDao;
    }
    
    /**
     * 
     * Return a list of message which contains the right topic, partition, offset and group
     * 
     * @param topic
     * @param partition
     * @param offset
     * @param group
     * 
     * @return the list of message
     */
    public List<MqiMessage> searchByTopicPartitionOffsetGroup(String topic, int partition, 
            long offset, String group) {
        Query query = query(where("topic").is(topic).and("partition").is(partition)
                .and("offset").is(offset).and("group").is(group));
        return mongoDBDAO.find(query);
    }
    
    /**
     * 
     * Return a list of message which contains the right topic, partition and group
     * 
     * @param topic
     * @param partition
     * @param group
     * 
     * @return the list of message
     */
    public List<MqiMessage> searchByTopicPartitionGroup (String topic, int partition, 
            String group, Set<MqiStateMessageEnum> states){
        Query query = query(where("topic").is(topic).and("partition").is(partition)
                .and("group").is(group).and("state").nin(states));
        query.with(new Sort(Direction.ASC, "lastReadDate"));
        return mongoDBDAO.find(query);
    }
    
    /**
     * Return a list of message which contains the right pod name, product category but its not in the states
     * 
     * @param pod
     * @param category
     * @param states
     * 
     * @return the list of message
     */
    public List<MqiMessage> searchByPodStateCategory(String pod, ProductCategory category,
            Set<MqiStateMessageEnum> states){
        Query query = query(where("readingPod").is(pod).and("state").nin(states)
                .and("category").is(category));
        query.with(new Sort(Direction.ASC, "lastReadDate"));
        return mongoDBDAO.find(query);
    }
    
    /**
     * Return a list of message which contains the right pod name, product category but its not in the states
     * 
     * @param pod
     * @param category
     * @param states
     * 
     * @return the list of message
     */
    public int countReadingMessages(String pod, String topic){
        Query query = query(where("readingPod").is(pod).and("state").is(MqiStateMessageEnum.READ)
                .and("topic").is(topic));
        query.with(new Sort(Direction.ASC, "lastReadDate"));
        return mongoDBDAO.find(query).size();
    }
    
    /**
     * Return a list of message which contains the right ID
     * 
     * @param messageID
     * 
     * @return the list of message
     */
    public List<MqiMessage> searchByID(long messageID) {
        Query query = query(where("identifier").is(messageID));
        return mongoDBDAO.find(query);
    }
    
    /**
     * Function which insert new message to the database
     * 
     * @param messageToInsert
     * 
     */
    public void insertMqiMessage(MqiMessage messageToInsert) {
        long sequence = sequenceDao.getNextSequenceId(MQI_MSG_SEQ_KEY);
    	messageToInsert.setIdentifier(sequence);
        mongoDBDAO.insert(messageToInsert);
    }
    
    /**
     * Function which update a MqiMessage
     * 
     * @param messageID
     * @param updateMap
     * 
     */
    public void updateByID(long messageID, Map<String, Object> updateMap) {
        Query query = query(where("identifier").is(messageID));
        Update update = new Update();
        updateMap.forEach((k,v)-> update.set(k, v));
        mongoDBDAO.updateFirst(query, update);
    }

}
