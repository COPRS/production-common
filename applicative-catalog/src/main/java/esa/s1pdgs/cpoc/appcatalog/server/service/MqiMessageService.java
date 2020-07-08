/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageDao;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageRepository;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDao;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Services to access mongoDB data
 * 
 * @author Viveris Technologies
 */
@Service
public class MqiMessageService {

    /**
     * 
     */
    public static final String MQI_MSG_SEQ_KEY = "mqiMessage";

    private final MqiMessageRepository mqiMessageRepository;
    
    /**
     * DAO for mongoDB
     */
    private final MqiMessageDao mongoDBDAO;

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
    public MqiMessageService(
    		final MqiMessageRepository mqiMessageRepository,
    		final MqiMessageDao mongoDBDAO,
            final SequenceDao sequenceDao) {
    	this.mqiMessageRepository = mqiMessageRepository;
        this.mongoDBDAO = mongoDBDAO;
        this.sequenceDao = sequenceDao;
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
        return mqiMessageRepository.findByTopicAndPartitionAndOffsetAndGroup(
        		topic, partition, offset, group);
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
        return mqiMessageRepository.findByTopicAndPartitionAndGroupAndStateNotInOrderByLastReadDateAsc(topic, partition, group,
                states);
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
        return mqiMessageRepository.findByReadingPodAndCategoryAndStateNotInOrderByCreationDateAsc(pod, category, states);
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
        return mqiMessageRepository.countByReadingPodAndTopicAndStateIsRead(pod, topic);
    }

    /**
     * Return a list of message which contains the right ID
     * 
     * @param messageID
     * @return the list of message
     */
    public List<MqiMessage> searchByID(final long messageID) {
    	Optional<MqiMessage> msg = mqiMessageRepository.findById(messageID);
    	return msg.isPresent() ? Collections.singletonList(msg.get()) : Collections.emptyList();
    }

    /**
     * Function which insert new message to the database
     * 
     * @param messageToInsert
     */
    public void insertMqiMessage(final MqiMessage messageToInsert) {
        long sequence = sequenceDao.getNextSequenceId(MQI_MSG_SEQ_KEY);
        messageToInsert.setId(sequence);
        mqiMessageRepository.save(messageToInsert);
    }

    /**
     * Function which update a MqiMessage
     * 
     * @param messageID
     * @param updateMap
     */
    public void updateByID(final long messageID,
            final Map<String, Object> updateMap) {
    	MqiMessage messageToUpdate = mqiMessageRepository.findById(messageID).get();
    	for (Entry<String, Object> entrySet : updateMap.entrySet()) {
    		switch (entrySet.getKey()) {
    		
    			/* MqiMessage attributes */
	    		case "dto": messageToUpdate.setDto(entrySet.getValue()); break;
	    		case "lastReadDate": messageToUpdate.setLastReadDate((Date)entrySet.getValue()); break;
	    		case "readingPod": messageToUpdate.setReadingPod((String)entrySet.getValue()); break;
	    		
	    		/* AbstractRequest attributes */
	    		case "category": messageToUpdate.setCategory((ProductCategory)entrySet.getValue()); break;
	    		case "creationDate": messageToUpdate.setCreationDate((Date)entrySet.getValue()); break;
	    		case "group": messageToUpdate.setGroup((String)entrySet.getValue()); break;
	    		case "lastAckDate": messageToUpdate.setLastAckDate((Date)entrySet.getValue()); break;
	    		case "lastSendDate": messageToUpdate.setLastSendDate((Date)entrySet.getValue()); break;
	    		case "nbRetries": messageToUpdate.setNbRetries((int)entrySet.getValue()); break;
	    		case "offset": messageToUpdate.setOffset((long)entrySet.getValue()); break;
	    		case "partition": messageToUpdate.setPartition((int)entrySet.getValue()); break;
	    		case "sendingPod": messageToUpdate.setSendingPod((String)entrySet.getValue()); break;
	    		case "state": messageToUpdate.setState((MessageState)entrySet.getValue()); break;
	    		case "topic": messageToUpdate.setTopic((String)entrySet.getValue()); break;
	    		
	    		default:
    		}
    	}
    	mqiMessageRepository.save(messageToUpdate);
    }

}
