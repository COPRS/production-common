package esa.s1pdgs.cpoc.appcatalog.server.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.MessageConverter;
import esa.s1pdgs.cpoc.appcatalog.server.service.MessageManager;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;

/**
 * REST server for managing MQI messages in DB 
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/mqi")
public class GenericMessageController<T> {

    private static final Logger LOGGER = LogManager.getLogger(GenericMessageController.class);

    private final MessageConverter messageConverter;
    private final MessageManager messageManager;
    private final AppStatus appStatus;
    
    @Autowired
    public GenericMessageController(MessageConverter messageConverter, MessageManager messageManager, AppStatus appStatus) {
		this.messageConverter = messageConverter;
		this.messageManager = messageManager;
		this.appStatus = appStatus;
	    this.appStatus.setWaiting();
	}

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{category}/{topic}/{partition}/{offset}/read")
    public ResponseEntity<AppCatMessageDto<T>> readMessage(
    		@PathVariable(name = "category") final String categoryName,
            @PathVariable(name = "topic") final String topic,
            @PathVariable(name = "partition") final int partition,
            @PathVariable(name = "offset") final long offset,
            @RequestBody final AppCatReadMessageDto<T> body) {
        try {
            final String logPrefix = String.format(
                    "[Read Message] [Topic %s] [Partition %d] [Offset %d] [Body %s]",
                    topic, partition, offset, body.getGroup()
            );            
        	final ProductCategory category = ProductCategory.valueOf(categoryName.toUpperCase());
            final MqiMessage messageFromDB = messageManager.insertOrUpdate(topic, partition, offset, body, logPrefix, category);
      	  	this.appStatus.setWaiting();
      	  	
            return new ResponseEntity<AppCatMessageDto<T>>(
            		messageConverter.toAppCatMessageDto(messageFromDB, body.getDto()),
                    HttpStatus.OK
            );
        // thrown if state does not match
        } catch (IllegalArgumentException e) {
    	   LOGGER.error("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] ERROR", topic, partition, offset, body.getGroup(), e);
           this.appStatus.setError("NEXT_MESSAGE");
            
        } catch (Exception e) {
            LOGGER.error("[read] {}", LogUtils.toString(e));
            this.appStatus.setError("NEXT_MESSAGE");            
        }
        return new ResponseEntity<AppCatMessageDto<T>>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{category}/next")
    public ResponseEntity<List<AppCatMessageDto<T>>> next(
    		@PathVariable(name = "category") final String categoryName,
            @RequestParam("pod") final String pod) {
        try {
        	final ProductCategory category = ProductCategory.valueOf(categoryName.toUpperCase());
        	
        	final List<MqiMessage> mqiMessages = messageManager.getNextForPodByCategory(pod, category);
                                    
            if (!mqiMessages.isEmpty()) {
            	LOGGER.debug("[Next] [Pod {}] [Product Category {}] Returning list of found MqiMessage", pod, category);
            } 
            @SuppressWarnings("unchecked")
			final List<AppCatMessageDto<T>> messagesToReturn = mqiMessages.stream()
            		.map(x -> messageConverter.toAppCatMessageDto(x, (T) x.getDto()))
            		.collect(Collectors.toList());

            this.appStatus.setWaiting();            
            return new ResponseEntity<List<AppCatMessageDto<T>>>(messagesToReturn, HttpStatus.OK);          
        } catch (Exception e) {
            LOGGER.error("[next] {}", LogUtils.toString(e));
            this.appStatus.setError("NEXT_MESSAGE");            
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{category}/{messageID}/send")
    public ResponseEntity<Boolean> sendMessage(
    		@PathVariable(name = "category") final String categoryName,
            @PathVariable(name = "messageID") final long messageID,
            @RequestBody final AppCatSendMessageDto body) {
        try {
        	LOGGER.debug("[Send Message] [MessageID {}] Searching MqiMessage", messageID);
        	boolean found = messageManager.handleSendMessage(messageID, body.getPod());        		
        	return new ResponseEntity<Boolean>(Boolean.valueOf(found), HttpStatus.OK);     
        // if message with id is not found
        } catch (IllegalStateException e) {
            LOGGER.error("[Send Message] [MessageID {}] No MqiMessage found", messageID);
            this.appStatus.setWaiting();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);	
        } catch (Exception e) {
            LOGGER.error("[send] {}", LogUtils.toString(e));
            this.appStatus.setError("NEXT_MESSAGE");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{category}/{messageID}/ack")
    public ResponseEntity<Boolean> ackMessage(
    		@PathVariable(name = "category") final String categoryName,
            @PathVariable(name = "messageID") final long messageID,
            @RequestBody final Ack ack) {
        try {
        	final boolean success = messageManager.ack(messageID, MessageState.of(ack));
        	this.appStatus.setWaiting();
            return new ResponseEntity<Boolean>(Boolean.valueOf(success), HttpStatus.OK);
        } catch (IllegalArgumentException exc) {
            LOGGER.error("[Ack Message] [MessageID {}] [Ack {}] {}", messageID, ack, exc.getMessage());
            this.appStatus.setWaiting();
            return new ResponseEntity<Boolean>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOGGER.error("[ack] {}", LogUtils.toString(e));
            this.appStatus.setError("NEXT_MESSAGE");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{category}/{messageID}")
    public ResponseEntity<AppCatMessageDto<T>> getMessage(
    		@PathVariable(name = "category") final String categoryName,
            @PathVariable(name = "messageID") final long messageID) {
        try {
        	final MqiMessage mess = messageManager.getMessage(messageID);  
            this.appStatus.setWaiting();
        	return new ResponseEntity<AppCatMessageDto<T>>(
        			messageConverter.toAppCatMessageDto(mess,(T) mess.getDto()), 
                    HttpStatus.OK);
         // if message with id is not found
        } catch (IllegalStateException e) {
            LOGGER.error("[Get] [MessageID {}] No MqiMessage Found with MessageID", messageID);
            this.appStatus.setWaiting();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            LOGGER.error("[Get] {}", LogUtils.toString(e));
            this.appStatus.setError("NEXT_MESSAGE");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{topic}/{partition}/earliestOffset")
    public ResponseEntity<Long> earliestOffset(
            @PathVariable(name = "topic") final String topic,
            @PathVariable(name = "partition") final int partition,
            @RequestParam("group") final String group) {
        try {
           	final long offset = messageManager.getOffsetByTopicPartitionGroup(topic, partition, group);           	
           	this.appStatus.setWaiting();
           	return new ResponseEntity<Long>(Long.valueOf(offset), HttpStatus.OK);
       
        } catch (Exception e) {
            LOGGER.error("[earliestOffset] {}", LogUtils.toString(e));
            this.appStatus.setError("NEXT_MESSAGE");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param topic
     * @param pod
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, path = "/{topic}/nbReading")
    public ResponseEntity<Integer> nbMessages(
            @PathVariable(name = "topic") final String topic,
            @RequestParam("pod") final String pod) {
        try {
        	final int number = messageManager.countReadingMessages(pod, topic);
            this.appStatus.setWaiting();
            return new ResponseEntity<Integer>(Integer.valueOf(number), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("[nbMessages] {}", LogUtils.toString(e));
            this.appStatus.setError("NEXT_MESSAGE");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
