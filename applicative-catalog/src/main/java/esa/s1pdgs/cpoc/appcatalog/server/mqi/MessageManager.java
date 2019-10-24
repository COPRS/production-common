package esa.s1pdgs.cpoc.appcatalog.server.mqi;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageService;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.rest.GenericMessageController;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

@Service
public class MessageManager {	
	private static final Logger LOGGER = LogManager.getLogger(GenericMessageController.class);
	
    private static final Set<MessageState> ACKS = EnumSet.of(
    		MessageState.ACK_OK,
    		MessageState.ACK_KO, 
    		MessageState.ACK_WARN
    );
	
    private final MqiMessageService messageService;
    private final int maxRetries;
    private final int defaultOffset;

    @Autowired
	public MessageManager(
			MqiMessageService messageService, 
			@Value("${mqi.max-retries}") int maxRetries,
			@Value("${mqi.dft-offset}") int defaultOffset
	) {
		this.messageService = messageService;
		this.maxRetries = maxRetries;
		this.defaultOffset = defaultOffset;
	}

	public <T> MqiMessage insertOrUpdate(final String topic, final int partition, final long offset,
			final AppCatReadMessageDto<T> body, final String logPrefix, final ProductCategory category) {
		LOGGER.debug("{} Searching MqiMessage", logPrefix);
		final List<MqiMessage> responseFromDB = messageService.searchByTopicPartitionOffsetGroup(
				topic,
				partition, 
				offset, 
				body.getGroup()
		);
		
		if (responseFromDB.isEmpty()) {
			LOGGER.debug("{} Inserting new MqiMessage", logPrefix);          	
		    final Date now = new Date();
			final MqiMessage messageToInsert = new MqiMessage(category, topic, partition, offset, body.getGroup(),
					MessageState.READ, body.getPod(), now, null, null, null, 0, body.getDto(), now);
		    messageService.insertMqiMessage(messageToInsert);
		    return messageToInsert;
		}
		LOGGER.debug("{} Found MqiMessage", logPrefix);  
		final MqiMessage messageFromDB = responseFromDB.get(0);          
		handleUpdateState(logPrefix, messageFromDB, body.isForce(), body.getPod());
		return messageFromDB;
	}
	
	private final HashMap<String, Object> handleUpdateStateSend(
			final String logPrefix, 
			final MqiMessage messageFromDB, 
			final boolean force,
			final String pod) {			
		final HashMap<String, Object> updateMap = new HashMap<>();
		
		if (!force) {
			LOGGER.debug("{} MqiMessage is at State SEND", logPrefix);
			final Date now = new Date();
			messageFromDB.setLastReadDate(now);
			updateMap.put("lastReadDate", now);
			messageFromDB.setReadingPod(pod);
			updateMap.put("readingPod", pod);
		} else  {
			LOGGER.debug("{} Force is true", logPrefix);	
			final int numRetries = messageFromDB.getNbRetries() + 1;
			messageFromDB.setNbRetries(numRetries);
			updateMap.put("nbRetries", numRetries);

			if (numRetries >= maxRetries) {
				LOGGER.error("{} Number of retries is reached", logPrefix);
				messageFromDB.setState(MessageState.ACK_KO);
				final Date now = new Date();
				messageFromDB.setLastAckDate(now);
				updateMap.put("lastAckDate", now);
			} else {
				LOGGER.debug("{} Number of retries is not reached", logPrefix);
				// on met status = READ
				messageFromDB.setState(MessageState.READ);
				updateMap.put("state", MessageState.READ);
				// on met le reading_pod au pod recu
				messageFromDB.setReadingPod(pod);
				updateMap.put("readingPod", pod);
				// on met le processing_pod à null
				messageFromDB.setSendingPod(null);
				updateMap.put("sendingPod", null);
				// on met à jour les éventuelles dates
				final Date now = new Date();
				messageFromDB.setLastSendDate(now);
				messageFromDB.setLastReadDate(now);
				updateMap.put("lastSendDate", now);
				updateMap.put("lastReadDate", now);
			}
		}
		return updateMap;
	}
    
    private final void handleUpdateState(final String logPrefix, final MqiMessage messageFromDB, final boolean force, final String pod) {    			
		switch (messageFromDB.getState()) {	
			case ACK_KO:
			case ACK_OK:
			case ACK_WARN:
				LOGGER.debug("{} MqiMessage is Acknowledge", logPrefix);
				break;
			case SEND:
				LOGGER.debug("{} MqiMessage is SEND", logPrefix);
				final HashMap<String, Object> updateMap = handleUpdateStateSend(logPrefix, messageFromDB, force, pod);
				messageService.updateByID(messageFromDB.getId(), updateMap);
				break;
			case READ:
				LOGGER.debug("{} MqiMessage is at State READ", logPrefix);
				final HashMap<String, Object> updates = new HashMap<>();	
				final Date now = new Date();
				messageFromDB.setLastReadDate(now);
				updates.put("lastReadDate", now);
				messageFromDB.setReadingPod(pod);
				updates.put("readingPod", pod);	
				messageService.updateByID(messageFromDB.getId(), updates);
				break;	
			default:
				throw new IllegalArgumentException(String.format("Unhandled state %s", messageFromDB.getState()));
		  }
	}

	public List<MqiMessage> getNextForPodByCategory(String pod, ProductCategory category) {
		// only return the ones not in any state ACK*
        return messageService.searchByPodStateCategory(pod, category, ACKS);		
	}
	
	public long getOffsetByTopicPartitionGroup(final String topic, final int partition, final String group){
		final List<MqiMessage> messages = messageService.searchByTopicPartitionGroup(topic, partition, group, ACKS);
		
        if (messages.isEmpty()) {
        	LOGGER.debug( "[EarliestOffset] [Topic {}] [Partition {}] [Group {}] Returning default Strategy", topic, partition, group);
        	return defaultOffset;
        } 
       	LOGGER.debug( "[EarliestOffset] [Topic {}] [Partition {}] [Group {}] Returning earlist offset", topic, partition, group);
       	return messages.get(0).getOffset();
	}
	
	public int countReadingMessages(final String pod, final String topic) {
		return messageService.countReadingMessages(pod, topic);
	}
	
	public MqiMessage getMessage(long messageID) throws IllegalStateException
	{
		final List<MqiMessage> responseFromDB = messageService.searchByID(messageID);

        if (responseFromDB.isEmpty()) {
        	throw new IllegalStateException(String.format("Message with id %s not found", messageID));
        }                 
        return responseFromDB.get(0);
	}

	public boolean handleSendMessage(long messageID, String pod) throws IllegalStateException {                
        final MqiMessage messageFromDB = getMessage(messageID);
        final Date now = new Date();        
     	LOGGER.debug("[Send Message] [MessageID {}] MqiMessage found is at state {}", messageID, messageFromDB.getState());
        
        switch (messageFromDB.getState()) {
        	case ACK_KO:
            case ACK_OK:
            case ACK_WARN:     
            	return false;
            case READ:            	
                final HashMap<String, Object> updateMap1 = new HashMap<>();

                messageFromDB.setState(MessageState.SEND);
                messageFromDB.setSendingPod(pod);
                updateMap1.put("state", MessageState.SEND);
                updateMap1.put("sendingPod",pod);  
                messageFromDB.setLastSendDate(now);
                updateMap1.put("lastSendDate", now);
                messageService.updateByID(messageID, updateMap1);
                return true;
            case SEND:
            	final HashMap<String, Object> updateMap2 = new HashMap<>();            	            	
            	final int numRetries = messageFromDB.getNbRetries() + 1;
    			messageFromDB.setNbRetries(numRetries);
    			updateMap2.put("nbRetries", numRetries);

				if (numRetries >= maxRetries) {
					LOGGER.error("[Send Message] [MessageID {}] Number of retries is reached", messageID);
					messageFromDB.setState(MessageState.ACK_KO);
					updateMap2.put("state", MessageState.ACK_KO);
					messageFromDB.setLastAckDate(now);
					updateMap2.put("lastAckDate", now);
					messageService.updateByID(messageID, updateMap2);
					return false;
				}
				LOGGER.debug("[Send Message] [MessageID {}] MqiMessage found state is set at SEND", messageID);
                messageFromDB.setState(MessageState.SEND);
                messageFromDB.setSendingPod(pod);
                updateMap2.put("state", MessageState.SEND);
                updateMap2.put("sendingPod", pod);   
                messageFromDB.setLastSendDate(now);
                updateMap2.put("lastSendDate", now);
                messageService.updateByID(messageID, updateMap2);
                return true;
            default:
            	throw new IllegalArgumentException(String.format("Unhandled state %s", messageFromDB.getState()));
        }
	}
	
	public final boolean ack(final long messageId, final MessageState ackState)
	{
        final HashMap<String, Object> updateMap = new HashMap<>();
        final Date now = new Date();
        updateMap.put("lastAckDate", now);
        updateMap.put("state", ackState);
        messageService.updateByID(messageId, updateMap);
        final List<MqiMessage> responseFromDB =  messageService.searchByID(messageId);

        if (responseFromDB.isEmpty()) {
            LOGGER.error("[Ack Message] [MessageID {}] [Ack {}] No MqiMessage Found with MessageID", messageId, ackState);
            return false;
        }
        return true;
	}
}
