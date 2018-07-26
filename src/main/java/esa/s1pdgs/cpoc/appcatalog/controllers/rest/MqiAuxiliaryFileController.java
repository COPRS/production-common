/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.controllers.rest;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.model.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiAuxiliaryFileMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiLightMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiSendMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.appcatalog.services.mongodb.MongoDBServices;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;



/**
 * Rest server for AUXILIARY file
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/mqi/auxiliary_files")
public class MqiAuxiliaryFileController {
    
    private static final Logger LOGGER = LogManager.getLogger(MqiAuxiliaryFileController.class);
    
    private final MongoDBServices mongoDBServices;
    
    private final int maxRetries;

    @Autowired
    public MqiAuxiliaryFileController(final MongoDBServices mongoDBServices, 
            @Value("${mqi.max-retries}") final int maxRetries) {
        this.mongoDBServices = mongoDBServices;
        this.maxRetries = maxRetries;
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, 
            path = "/{topic}/{partition}/{offset}/read")
    public ResponseEntity<MqiLightMessageDto> readMessage(@PathVariable(name = "topic") String topic, 
            @PathVariable(name = "partition") int partition, @PathVariable(name = "offset") long offset, 
            @RequestBody MqiGenericReadMessageDto<AuxiliaryFileDto> body) {
        LOGGER.info("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] Searching MqiMessage", 
                topic, partition, offset, body.getGroup());
        List<MqiMessage> responseFromDB = 
                mongoDBServices.searchByTopicPartitionOffsetGroup(topic, partition, offset, body.getGroup());
        
        //Si un objet n'existe pas dans la BDD avec topic / partition / offset / group
        if(responseFromDB.isEmpty()) {
            //On créer le message dans la BDD
            LOGGER.info("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] Inserting new MqiMessage", 
                    topic, partition, offset, body.getGroup());
            MqiMessage messageToInsert = new MqiMessage(ProductCategory.AUXILIARY_FILES, 
                    topic, partition, offset, body.getGroup(), MqiStateMessageEnum.READ, 
                    body.getPod(), new Date(), null, null, null, 0, body.getDto());
            mongoDBServices.insertMqiMessage(messageToInsert);
            
            //On renvoie le message que l'on vient de créer
            return new ResponseEntity<MqiLightMessageDto>(transformMqiMessageToMqiLightMessage(messageToInsert), HttpStatus.OK);
        } else { //Sinon on récupère le premier de la liste
            LOGGER.info("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] Found MqiMessage", 
                    topic, partition, offset, body.getGroup());
            MqiMessage messageFromDB = responseFromDB.get(0);
            //Si l'état est à ACK
            if(messageFromDB.getState().equals(MqiStateMessageEnum.ACK_OK) || 
                    messageFromDB.getState().equals(MqiStateMessageEnum.ACK_KO) ||
                    messageFromDB.getState().equals(MqiStateMessageEnum.ACK_WARN)) {
                //on renvoie l’objet
                LOGGER.info("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] MqiMessage is Acknowledge", 
                        topic, partition, offset, body.getGroup());
                return new ResponseEntity<MqiLightMessageDto>(transformMqiMessageToMqiLightMessage(messageFromDB), HttpStatus.OK);
            } else if(body.isForce()) { // sinon si force = true
                LOGGER.info("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] Force is true", 
                        topic, partition, offset, body.getGroup());
                HashMap<String, Object> updateMap = new HashMap<>();
                // on incrémente nb_retry
                messageFromDB.setNbRetries(messageFromDB.getNbRetries() + 1);
                updateMap.put("nbRetries", messageFromDB.getNbRetries());
                if(messageFromDB.getNbRetries() == maxRetries) {
                    // on publie un message d’erreur dans queue (via mqi du catalogue)
                    //TODO 
                    LOGGER.error("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] Number of retries is reached", 
                            topic, partition, offset, body.getGroup());
                    // on met status = ACK_KO
                    messageFromDB.setState(MqiStateMessageEnum.ACK_KO);
                    updateMap.put("state", messageFromDB.getState());
                    // on met à jour les éventuelles dates
                    Date now = new Date();
                    messageFromDB.setLastAckDate(now);
                    messageFromDB.setLastReadDate(now);
                    updateMap.put("lastAckDate", now);
                    updateMap.put("lastReadDate", now);
                    // Modifier l'objet dans la bdd
                    mongoDBServices.updateByID(messageFromDB.getIdentifier() ,updateMap);
                    // on renvoie l’objet
                    return new ResponseEntity<MqiLightMessageDto>(transformMqiMessageToMqiLightMessage(messageFromDB), HttpStatus.OK);     
                } else {
                    LOGGER.info("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] Number of retries is not reached", 
                            topic, partition, offset, body.getGroup());
                    // on met status = READ
                    messageFromDB.setState(MqiStateMessageEnum.READ);
                    updateMap.put("state", messageFromDB.getState());
                    // on met le reading_pod au pod recu
                    messageFromDB.setReadingPod(body.getPod());
                    updateMap.put("readingPod", messageFromDB.getReadingPod());
                    // on met le processing_pod à null
                    messageFromDB.setSendingPod(null);
                    updateMap.put("sendingPod", messageFromDB.getSendingPod());
                    // on met à jour les éventuelles dates
                    Date now = new Date();
                    messageFromDB.setLastSendPod(now);
                    messageFromDB.setLastReadDate(now);
                    updateMap.put("lastSendPod", now);
                    updateMap.put("lastReadDate", now);
                    // Modifier l'objet dans la bdd
                    mongoDBServices.updateByID(messageFromDB.getIdentifier() ,updateMap);
                    // on renvoie l’objet
                    return new ResponseEntity<MqiLightMessageDto>(transformMqiMessageToMqiLightMessage(messageFromDB), HttpStatus.OK);
                }
            } else {
                HashMap<String, Object> updateMap = new HashMap<>();
                if(messageFromDB.getState().equals(MqiStateMessageEnum.READ)) {
                    LOGGER.info("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] MqiMessage is at State READ", 
                            topic, partition, offset, body.getGroup());
                    // on met à jour les éventuelles dates et le reading_pod
                    Date now = new Date();
                    messageFromDB.setLastReadDate(now);
                    updateMap.put("lastReadDate", now);
                    messageFromDB.setReadingPod(body.getPod());
                    updateMap.put("readingPod", messageFromDB.getReadingPod());                    
                    // Modifier l'objet dans la bdd
                    mongoDBServices.updateByID(messageFromDB.getIdentifier() ,updateMap);
                    // on renvoie l’objet
                    return new ResponseEntity<MqiLightMessageDto>(transformMqiMessageToMqiLightMessage(messageFromDB), HttpStatus.OK);
                }
                if(messageFromDB.getState().equals(MqiStateMessageEnum.SEND)) {
                    LOGGER.info("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] MqiMessage is at State SEND", 
                            topic, partition, offset, body.getGroup());
                    // on met à jour les éventuelles dates et le reading_pod
                    Date now = new Date();
                    messageFromDB.setLastSendPod(now);
                    updateMap.put("lastSendPod", now);
                    messageFromDB.setReadingPod(body.getPod());
                    updateMap.put("readingPod", messageFromDB.getReadingPod());                    
                    // Modifier l'objet dans la bdd
                    mongoDBServices.updateByID(messageFromDB.getIdentifier() ,updateMap);
                    // on renvoie l’objet
                    return new ResponseEntity<MqiLightMessageDto>(transformMqiMessageToMqiLightMessage(messageFromDB), HttpStatus.OK);
                }
            }
        }
        LOGGER.error("[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] ERROR", 
                topic, partition, offset, body.getGroup());
        return new ResponseEntity<MqiLightMessageDto>(HttpStatus.NOT_FOUND);
        
    }
    
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/next")
    public ResponseEntity<List<MqiAuxiliaryFileMessageDto>> next(@RequestParam("pod") String pod) {
        
        //TODO : modify search function when we will use priority
        Set<MqiStateMessageEnum> ackStates = new HashSet<>();
        ackStates.add(MqiStateMessageEnum.ACK_KO);
        ackStates.add(MqiStateMessageEnum.ACK_OK);
        ackStates.add(MqiStateMessageEnum.ACK_WARN);
        LOGGER.info("[Next] [Pod {}] [States {}] [Product Category {}] Searching MqiMessage", 
                pod, ackStates, ProductCategory.AUXILIARY_FILES);
        List<MqiMessage> mqiMessages  = mongoDBServices.searchByPodStateCategory(pod,
                ProductCategory.AUXILIARY_FILES, ackStates);
        if(mqiMessages.isEmpty()) {
            LOGGER.error("[Next] [Pod {}] [States {}] [Product Category {}] No MqiMessage found", 
                    pod, ackStates, ProductCategory.AUXILIARY_FILES);
            return new ResponseEntity<List<MqiAuxiliaryFileMessageDto>>(HttpStatus.NOT_FOUND);
        } else {
            LOGGER.info("[Next] [Pod {}] [States {}] [Product Category {}] Returning list of found MqiMessage", 
                    pod, ackStates, ProductCategory.AUXILIARY_FILES);
            List<MqiAuxiliaryFileMessageDto> messagesToReturn = new ArrayList<>();
            mqiMessages.forEach(x-> messagesToReturn.add(transformMqiMessageToMqiAuxiliaryFileMessage(x)));
            return new ResponseEntity<List<MqiAuxiliaryFileMessageDto>>(messagesToReturn, HttpStatus.OK);
        }
        
     }
    
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, 
            path = "/{messageID}/send")
    public ResponseEntity<Boolean> sendMessage(@PathVariable(name = "messageID") long messageID, 
            @RequestBody MqiSendMessageDto body) {
        
        LOGGER.info("[Send Message] [MessageID {}] Searching MqiMessage", messageID);
        List<MqiMessage> responseFromDB = mongoDBServices.searchByID(messageID);
        
        if(responseFromDB.isEmpty()) {
            LOGGER.error("[Send Message] [MessageID {}] No MqiMessage found", messageID);
            return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
        } else { // Si le message existe
            MqiMessage messageFromDB = responseFromDB.get(0);
            if(messageFromDB.getState().equals(MqiStateMessageEnum.ACK_OK) || 
                messageFromDB.getState().equals(MqiStateMessageEnum.ACK_KO) ||
                messageFromDB.getState().equals(MqiStateMessageEnum.ACK_WARN)) {
                LOGGER.info("[Send Message] [MessageID {}] MqiMessage found is at state ACK", messageID);
                return new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.OK);
            } else if(messageFromDB.getState().equals(MqiStateMessageEnum.READ)) {
                HashMap<String, Object> updateMap = new HashMap<>();
                // on met status à SEND et son processing_pod
                messageFromDB.setState(MqiStateMessageEnum.ACK_KO);
                updateMap.put("state", messageFromDB.getState());
                // on met à jour les éventuelles dates
                Date now = new Date();
                messageFromDB.setLastAckDate(now);
                messageFromDB.setLastSendPod(now);
                updateMap.put("lastAckDate", now);
                updateMap.put("lastSendPod", now);
                mongoDBServices.updateByID(messageID, updateMap);
                LOGGER.info("[Send Message] [MessageID {}] MqiMessage found is at state READ", messageID);
                return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
            } else {
                HashMap<String, Object> updateMap = new HashMap<>();
                // on incrémente nb_retry
                messageFromDB.setNbRetries(messageFromDB.getNbRetries() + 1);
                updateMap.put("nbRetries", messageFromDB.getNbRetries());
                if(messageFromDB.getNbRetries() == maxRetries) {
                    // on publie un message d’erreur dans queue (via mqi du catalogue)
                    //TODO
                    LOGGER.error("[Send Message] [MessageID {}] Number of retries is not reached", messageID);
                    // on met status = ACK_KO
                    messageFromDB.setState(MqiStateMessageEnum.ACK_KO);
                    updateMap.put("state", messageFromDB.getState());
                    // on met à jour les éventuelles dates
                    Date now = new Date();
                    messageFromDB.setLastAckDate(now);
                    messageFromDB.setLastSendPod(now);
                    updateMap.put("lastAckDate", now);
                    updateMap.put("lastSendPod", now);
                    mongoDBServices.updateByID(messageID, updateMap);
                    return new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.OK);
                } else {
                    // on met status = à SEND et son processing_pod
                    messageFromDB.setState(MqiStateMessageEnum.SEND);
                    updateMap.put("state", messageFromDB.getState());
                    // on met à jour les éventuelles dates
                    Date now = new Date();
                    messageFromDB.setLastSendPod(now);
                    updateMap.put("lastSendPod", now);
                    mongoDBServices.updateByID(messageID, updateMap);
                    LOGGER.info("[Send Message] [MessageID {}] MqiMessage found state is set at SEND", messageID);
                    return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
                }
            }
        }
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, 
            path = "/{messageID}/ack")
    public ResponseEntity<MqiAuxiliaryFileMessageDto> ackMessage(@PathVariable(name = "messageID") long messageID,
            @RequestBody AckMessageDto ackMessageDto) {
        
        HashMap<String, Object> updateMap = new HashMap<>();
        if(ackMessageDto.getAck().equals(Ack.OK)) {
            updateMap.put("state", MqiStateMessageEnum.ACK_OK);
        } else if(ackMessageDto.getAck().equals(Ack.ERROR)) {
            updateMap.put("state", MqiStateMessageEnum.ACK_KO);
        } else if(ackMessageDto.getAck().equals(Ack.WARN)) {
            updateMap.put("state", MqiStateMessageEnum.ACK_WARN);
        } else {
            LOGGER.error("[Ack Message] [MessageID {}] [Ack {}] Ack is not valid", 
                    messageID, ackMessageDto.getAck());
            return new ResponseEntity<MqiAuxiliaryFileMessageDto>(HttpStatus.NOT_FOUND);
        }
        mongoDBServices.updateByID(messageID, updateMap);
        LOGGER.info("[Ack Message] [MessageID {}] [Ack {}] MqiMessage state is set at ACK", 
                messageID, ackMessageDto.getAck());
        List<MqiMessage> responseFromDB = mongoDBServices.searchByID(messageID);
        //on met le status à ak_ok ou ack_ko
        if(responseFromDB.isEmpty()) {
            LOGGER.error("[Ack Message] [MessageID {}] [Ack {}] No MqiMessage Found with MessageID", 
                    messageID, ackMessageDto.getAck());
            return new ResponseEntity<MqiAuxiliaryFileMessageDto>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<MqiAuxiliaryFileMessageDto>(
                transformMqiMessageToMqiAuxiliaryFileMessage(responseFromDB.get(0)), HttpStatus.OK);
        }
    }
    
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, 
            path = "/{topic}/{partition}/earliestOffset")
    public ResponseEntity<Long> earliestOffset(@PathVariable(name = "topic") String topic, 
            @PathVariable(name = "partition") int partition, @RequestParam("group") String group) {
        
        // Pour le topic / partition / group donné, on récupère l’offset du message avec status != ACK et la plus petite date de lecture (à voir si on prend le plus petit offset)
        List<MqiMessage> responseFromDB = mongoDBServices.searchByTopicPartitionGroup(topic, partition, group);
        if(responseFromDB.isEmpty()) {
            //TODO define the strategy
            // Si pas d’entrée, on renvoie valeur par défaut :
            // -2 : on laisse le consumer faire ce qu’il veut
            // -1 : on démarre à l’offset du début
            // 0 : on démarre à l’offset de fin
            LOGGER.info("[EarliestOffset] [Topic {}] [Partition {}] [Group {}] Returning default Strategy", 
                    topic, partition, group);
            return new ResponseEntity<Long>(Long.valueOf(0), HttpStatus.OK);
        } else {
            LOGGER.info("[EarliestOffset] [Topic {}] [Partition {}] [Group {}] Returning earlist offset", 
                    topic, partition, group);
            return new ResponseEntity<Long>(responseFromDB.get(0).getOffset(), HttpStatus.OK);
        }
    }

    private MqiLightMessageDto transformMqiMessageToMqiLightMessage(MqiMessage messageToTransform) {
        MqiLightMessageDto messageTransformed = new MqiLightMessageDto();
        messageTransformed.setCategory(messageToTransform.getCategory());
        messageTransformed.setGroup(messageToTransform.getGroup());
        messageTransformed.setIdentifier(messageToTransform.getIdentifier());
        messageTransformed.setLastAckDate(messageToTransform.getLastAckDate());
        messageTransformed.setLastReadDate(messageToTransform.getLastReadDate());
        messageTransformed.setLastSendDate(messageToTransform.getLastSendPod());
        messageTransformed.setNbRetries(messageToTransform.getNbRetries());
        messageTransformed.setOffset(messageToTransform.getOffset());
        messageTransformed.setPartition(messageToTransform.getPartition());
        messageTransformed.setReadingPod(messageToTransform.getReadingPod());
        messageTransformed.setSendingPod(messageToTransform.getSendingPod());
        messageTransformed.setState(messageToTransform.getState());
        messageTransformed.setTopic(messageToTransform.getTopic());
        return messageTransformed;
    }
    
    private MqiAuxiliaryFileMessageDto transformMqiMessageToMqiAuxiliaryFileMessage(MqiMessage messageToTransform) {
        MqiAuxiliaryFileMessageDto messageTransformed = new MqiAuxiliaryFileMessageDto();
        messageTransformed.setCategory(messageToTransform.getCategory());
        messageTransformed.setGroup(messageToTransform.getGroup());
        messageTransformed.setIdentifier(messageToTransform.getIdentifier());
        messageTransformed.setLastAckDate(messageToTransform.getLastAckDate());
        messageTransformed.setLastReadDate(messageToTransform.getLastReadDate());
        messageTransformed.setLastSendDate(messageToTransform.getLastSendPod());
        messageTransformed.setNbRetries(messageToTransform.getNbRetries());
        messageTransformed.setOffset(messageToTransform.getOffset());
        messageTransformed.setPartition(messageToTransform.getPartition());
        messageTransformed.setReadingPod(messageToTransform.getReadingPod());
        messageTransformed.setSendingPod(messageToTransform.getSendingPod());
        messageTransformed.setState(messageToTransform.getState());
        messageTransformed.setTopic(messageToTransform.getTopic());
        messageTransformed.setDto((AuxiliaryFileDto) messageToTransform.getDto());
        return messageTransformed;
    }
    
}
