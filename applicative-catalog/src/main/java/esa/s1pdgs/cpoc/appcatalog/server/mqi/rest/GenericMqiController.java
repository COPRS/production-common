package esa.s1pdgs.cpoc.appcatalog.server.mqi.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiLightMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiSendMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageService;
import esa.s1pdgs.cpoc.appcatalog.server.status.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;

/**
 * @author Viveris Technologies
 * @param <T>
 */
public class GenericMqiController<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(GenericMqiController.class);

    /**
     * Service for managing MQI messages TODO rename class and attribute
     */
    protected final MqiMessageService mongoDBServices;

    /**
     * TODO: not use here: a controller check only the input and buils the
     * ouput, the intelligence is in a service
     */
    protected final int maxRetries;

    /**
     * Product category
     */
    protected final ProductCategory category;
    
    /**
     * Application status
     */
    protected final AppStatus appStatus;
    
    /**
     * Dft KAFKA offset
     */
    protected final int dftOffset;

    /**
     * Constructor
     * 
     * @param mongoDBServices
     * @param maxRetries
     * @param category
     */
    public GenericMqiController(final MqiMessageService mongoDBServices,
            final int maxRetries, final ProductCategory category,
            final AppStatus appStatus, final int dftOffset) {
        this.mongoDBServices = mongoDBServices;
        this.maxRetries = maxRetries;
        this.category = category;
        this.appStatus = appStatus;
        this.appStatus.setWaiting("MQI");
        this.dftOffset = dftOffset;
    }

    /**
     * Internal function to log messages
     * 
     * @param message
     */
    private void log(final String message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(message);
        }
    }

    /**
     * @param topic
     * @param partition
     * @param offset
     * @param body
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{topic}/{partition}/{offset}/read")
    public ResponseEntity<MqiLightMessageDto> readMessage(
            @PathVariable(name = "topic") final String topic,
            @PathVariable(name = "partition") final int partition,
            @PathVariable(name = "offset") final long offset,
            @RequestBody final MqiGenericReadMessageDto<T> body) {
        try {
            
            log(String.format(
                    "[Read Message] [Topic %s] [Partition %d] [Offset %d] [Body %s] Searching MqiMessage",
                    topic, partition, offset, body.getGroup()));
            List<MqiMessage> responseFromDB =
                    mongoDBServices.searchByTopicPartitionOffsetGroup(topic,
                            partition, offset, body.getGroup());

            
            // Si un objet n'existe pas dans la BDD avec topic / partition /
            // offset
            // / group
            if (responseFromDB.isEmpty()) {
                // On créer le message dans la BDD
                log(String.format(
                        "[Read Message] [Topic %s] [Partition %d] [Offset %d] [Body %s] Inserting new MqiMessage",
                        topic, partition, offset, body.getGroup()));
                Date now = new Date();
                MqiMessage messageToInsert = new MqiMessage(category, topic,
                        partition, offset, body.getGroup(),
                        MqiStateMessageEnum.READ, body.getPod(), now,
                        null, null, null, 0, body.getDto(), now);
                mongoDBServices.insertMqiMessage(messageToInsert);

                // On renvoie le message que l'on vient de créer
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<MqiLightMessageDto>(
                        transformMqiMessageToMqiLightMessage(messageToInsert),
                        HttpStatus.OK);
            } else { // Sinon on récupère le premier de la liste
                log(String.format(
                        "[Read Message] [Topic %s] [Partition %d] [Offset %d] [Body %s] Found MqiMessage",
                        topic, partition, offset, body.getGroup()));
                MqiMessage messageFromDB = responseFromDB.get(0);

                switch (messageFromDB.getState()) {
                    case ACK_KO:
                    case ACK_OK:
                    case ACK_WARN:
                        // on renvoie l’objet
                        log(String.format(
                                "[Read Message] [Topic %s] [Partition %d] [Offset %d] [Body %s] MqiMessage is Acknowledge",
                                topic, partition, offset, body.getGroup()));
                        this.appStatus.setWaiting("MQI");
                        return new ResponseEntity<MqiLightMessageDto>(
                                transformMqiMessageToMqiLightMessage(
                                        messageFromDB),
                                HttpStatus.OK);
                    case SEND:
                        if (body.isForce()) {

                            log(String.format(
                                    "[Read Message] [Topic %s] [Partition %d] [Offset %d] [Body %s] Force is true",
                                    topic, partition, offset, body.getGroup()));
                            HashMap<String, Object> updateMap = new HashMap<>();
                            //  on incrémente nb_retry
                            messageFromDB.setNbRetries(
                                    messageFromDB.getNbRetries() + 1);
                            updateMap.put("nbRetries",
                                    messageFromDB.getNbRetries());
                            if (messageFromDB.getNbRetries() >= maxRetries) {
                                // on publie un message d’erreur dans queue (via
                                // mqi du
                                // catalogue)
                                // TODO
                                LOGGER.error(
                                        "[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] Number of retries is reached",
                                        topic, partition, offset,
                                        body.getGroup());
                                // on met status = ACK_KO
                                messageFromDB
                                        .setState(MqiStateMessageEnum.ACK_KO);
                                updateMap.put("state",
                                        messageFromDB.getState());
                                // on met à jour les éventuelles dates
                                Date now = new Date();
                                messageFromDB.setLastAckDate(now);
                                updateMap.put("lastAckDate", now);
                                // Modifier l'objet dans la bdd
                                mongoDBServices.updateByID(
                                        messageFromDB.getIdentifier(),
                                        updateMap);
                                // on renvoie l’objet
                                this.appStatus.setWaiting("MQI");
                                return new ResponseEntity<MqiLightMessageDto>(
                                        transformMqiMessageToMqiLightMessage(
                                                messageFromDB),
                                        HttpStatus.OK);
                            } else {
                                log(String.format(
                                        "[Read Message] [Topic %s] [Partition %d] [Offset %d] [Body %s] Number of retries is not reached",
                                        topic, partition, offset,
                                        body.getGroup()));
                                // on met status = READ
                                messageFromDB
                                        .setState(MqiStateMessageEnum.READ);
                                updateMap.put("state",
                                        messageFromDB.getState());
                                // on met le reading_pod au pod recu
                                messageFromDB.setReadingPod(body.getPod());
                                updateMap.put("readingPod",
                                        messageFromDB.getReadingPod());
                                // on met le processing_pod à null
                                messageFromDB.setSendingPod(null);
                                updateMap.put("sendingPod",
                                        messageFromDB.getSendingPod());
                                // on met à jour les éventuelles dates
                                Date now = new Date();
                                messageFromDB.setLastSendDate(now);
                                messageFromDB.setLastReadDate(now);
                                updateMap.put("lastSendDate", now);
                                updateMap.put("lastReadDate", now);
                                // Modifier l'objet dans la bdd
                                mongoDBServices.updateByID(
                                        messageFromDB.getIdentifier(),
                                        updateMap);
                                // on renvoie l’objet
                                this.appStatus.setWaiting("MQI");
                                return new ResponseEntity<MqiLightMessageDto>(
                                        transformMqiMessageToMqiLightMessage(
                                                messageFromDB),
                                        HttpStatus.OK);
                            }
                        } else {
                            HashMap<String, Object> updateMap = new HashMap<>();
                            log(String.format(
                                    "[Read Message] [Topic %s] [Partition %d] [Offset %d] [Body %s] MqiMessage is at State SEND",
                                    topic, partition, offset, body.getGroup()));
                            // on met à jour les éventuelles dates et le
                            // reading_pod
                            Date now = new Date();
                            messageFromDB.setLastReadDate(now);
                            updateMap.put("lastReadDate", now);
                            messageFromDB.setReadingPod(body.getPod());
                            updateMap.put("readingPod",
                                    messageFromDB.getReadingPod());
                            // Modifier l'objet dans la bdd
                            mongoDBServices.updateByID(
                                    messageFromDB.getIdentifier(), updateMap);
                            // on renvoie l’objet
                            this.appStatus.setWaiting("MQI");
                            return new ResponseEntity<MqiLightMessageDto>(
                                    transformMqiMessageToMqiLightMessage(
                                            messageFromDB),
                                    HttpStatus.OK);
                        }
                        
                    default:
                        HashMap<String, Object> updateMap = new HashMap<>();
                        if (messageFromDB.getState()
                                .equals(MqiStateMessageEnum.READ)) {
                            log(String.format(
                                    "[Read Message] [Topic %s] [Partition %d] [Offset %d] [Body %s] MqiMessage is at State READ",
                                    topic, partition, offset, body.getGroup()));
                            // on met à jour les éventuelles dates et le reading_pod
                            Date now = new Date();
                            messageFromDB.setLastReadDate(now);
                            updateMap.put("lastReadDate", now);
                            messageFromDB.setReadingPod(body.getPod());
                            updateMap.put("readingPod",
                                    messageFromDB.getReadingPod());
                            // Modifier l'objet dans la bdd
                            mongoDBServices.updateByID(
                                    messageFromDB.getIdentifier(), updateMap);
                            // on renvoie l’objet
                            this.appStatus.setWaiting("MQI");
                            return new ResponseEntity<MqiLightMessageDto>(
                                    transformMqiMessageToMqiLightMessage(
                                            messageFromDB),
                                    HttpStatus.OK);
                        }
                }
            }
            LOGGER.error(
                    "[Read Message] [Topic {}] [Partition {}] [Offset {}] [Body {}] ERROR",
                    topic, partition, offset, body.getGroup());
            this.appStatus.setError("MQI");
        } catch (Exception exc) {
            LOGGER.error("[read] {}", exc.getMessage());
            this.appStatus.setError("MQI");            
        }
        return new ResponseEntity<MqiLightMessageDto>(
                HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/next")
    public ResponseEntity<List<MqiGenericMessageDto<T>>> next(
            @RequestParam("pod") final String pod) {
        try {
            Set<MqiStateMessageEnum> ackStates = new HashSet<>();
            ackStates.add(MqiStateMessageEnum.ACK_KO);
            ackStates.add(MqiStateMessageEnum.ACK_OK);
            ackStates.add(MqiStateMessageEnum.ACK_WARN);
            List<MqiMessage> mqiMessages = mongoDBServices
                    .searchByPodStateCategory(pod, category, ackStates);
            if (mqiMessages.isEmpty()) {
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<List<MqiGenericMessageDto<T>>>(
                        new ArrayList<MqiGenericMessageDto<T>>(),
                        HttpStatus.OK);
            } else {
                log(String.format(
                        "[Next] [Pod %s] [Product Category %s] Returning list of found MqiMessage",
                        pod, category));
                List<MqiGenericMessageDto<T>> messagesToReturn =
                        new ArrayList<>();
                mqiMessages.forEach(x -> messagesToReturn
                        .add(transformMqiMessageToDtoGenericMessage(x)));
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<List<MqiGenericMessageDto<T>>>(
                        messagesToReturn, HttpStatus.OK);
            }
        } catch (Exception exc) {
            LOGGER.error("[next] {}", exc.getMessage());
            this.appStatus.setError("MQI");            
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{messageID}/send")
    public ResponseEntity<Boolean> sendMessage(
            @PathVariable(name = "messageID") final long messageID,
            @RequestBody final MqiSendMessageDto body) {
        try {
            log(String.format(
                    "[Send Message] [MessageID %d] Searching MqiMessage",
                    messageID));
            List<MqiMessage> responseFromDB =
                    mongoDBServices.searchByID(messageID);

            if (responseFromDB.isEmpty()) {
                LOGGER.error(
                        "[Send Message] [MessageID {}] No MqiMessage found",
                        messageID);
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
            } else { // Si le message existe
                MqiMessage messageFromDB = responseFromDB.get(0);
                Date now = new Date();
                switch (messageFromDB.getState()) {
                    case ACK_KO:
                    case ACK_OK:
                    case ACK_WARN:
                        log(String.format(
                                "[Send Message] [MessageID %d] MqiMessage found is at state ACK",
                                messageID));
                        this.appStatus.setWaiting("MQI");
                        return new ResponseEntity<Boolean>(false,
                                HttpStatus.OK);
                    case READ:
                        HashMap<String, Object> updateMap1 = new HashMap<>();
                        // on met status à SEND et son processing_pod
                        messageFromDB.setState(MqiStateMessageEnum.SEND);
                        messageFromDB.setSendingPod(body.getPod());
                        updateMap1.put("state", messageFromDB.getState());
                        updateMap1.put("sendingPod",
                                messageFromDB.getSendingPod());
                        // on met à jour les éventuelles dates
                        messageFromDB.setLastSendDate(now);
                        updateMap1.put("lastSendDate", now);
                        mongoDBServices.updateByID(messageID, updateMap1);
                        log(String.format(
                                "[Send Message] [MessageID %d] MqiMessage found is at state READ",
                                messageID));
                        this.appStatus.setWaiting("MQI");
                        return new ResponseEntity<Boolean>(true,
                                HttpStatus.OK);
                    default:
                        HashMap<String, Object> updateMap2 = new HashMap<>();
                        //  on incrémente nb_retry
                        messageFromDB
                                .setNbRetries(messageFromDB.getNbRetries() + 1);
                        updateMap2.put("nbRetries",
                                messageFromDB.getNbRetries());
                        if (messageFromDB.getNbRetries() >= maxRetries) {
                            // on publie un message d’erreur dans queue (via mqi
                            // du
                            // catalogue)
                            // TODO
                            LOGGER.error(
                                    "[Send Message] [MessageID {}] Number of retries is not reached",
                                    messageID);
                            // on met status = ACK_KO
                            messageFromDB.setState(MqiStateMessageEnum.ACK_KO);
                            updateMap2.put("state", messageFromDB.getState());
                            // on met à jour les éventuelles dates
                            messageFromDB.setLastAckDate(now);
                            updateMap2.put("lastAckDate", now);
                            mongoDBServices.updateByID(messageID, updateMap2);
                            this.appStatus.setWaiting("MQI");
                            return new ResponseEntity<Boolean>(false,
                                    HttpStatus.OK);
                        } else {
                            // on met status = à SEND et son processing_pod
                            messageFromDB.setState(MqiStateMessageEnum.SEND);
                            messageFromDB.setSendingPod(body.getPod());
                            updateMap2.put("state", messageFromDB.getState());
                            updateMap2.put("sendingPod",
                                    messageFromDB.getSendingPod());
                            // on met à jour les éventuelles dates
                            messageFromDB.setLastSendDate(now);
                            updateMap2.put("lastSendDate", now);
                            mongoDBServices.updateByID(messageID, updateMap2);
                            log(String.format(
                                    "[Send Message] [MessageID %d] MqiMessage found state is set at SEND",
                                    messageID));
                            this.appStatus.setWaiting("MQI");
                            return new ResponseEntity<Boolean>(true,
                                    HttpStatus.OK);
                        }

                }
            }
        } catch (Exception exc) {
            LOGGER.error("[send] {}", exc.getMessage());
            this.appStatus.setError("MQI");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{messageID}/ack")
    public ResponseEntity<Boolean> ackMessage(
            @PathVariable(name = "messageID") final long messageID,
            @RequestBody final Ack ack) {
        try {
            HashMap<String, Object> updateMap = new HashMap<>();
            if (ack.equals(Ack.OK)) {
                updateMap.put("state", MqiStateMessageEnum.ACK_OK);
            } else if (ack.equals(Ack.ERROR)) {
                updateMap.put("state", MqiStateMessageEnum.ACK_KO);
            } else if (ack.equals(Ack.WARN)) {
                updateMap.put("state", MqiStateMessageEnum.ACK_WARN);
            } else {
                LOGGER.error(
                        "[Ack Message] [MessageID {}] [Ack {}] Ack is not valid",
                        messageID, ack);
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<Boolean>(HttpStatus.BAD_REQUEST);
            }
            Date now = new Date();
            updateMap.put("lastAckDate", now);

            mongoDBServices.updateByID(messageID, updateMap);
            List<MqiMessage> responseFromDB =
                    mongoDBServices.searchByID(messageID);
            // on met le status à ak_ok ou ack_ko

            if (responseFromDB.isEmpty()) {
                LOGGER.error(
                        "[Ack Message] [MessageID {}] [Ack {}] No MqiMessage Found with MessageID",
                        messageID, ack);
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<Boolean>(false, HttpStatus.OK);
            } else {
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<Boolean>(true, HttpStatus.OK);
            }
        } catch (Exception exc) {
            LOGGER.error("[ack] {}", exc.getMessage());
            this.appStatus.setError("MQI");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{messageID}")
    public ResponseEntity<MqiGenericMessageDto<T>> getMessage(
            @PathVariable(name = "messageID") final long messageID) {
        try {
            List<MqiMessage> responseFromDB =
                    mongoDBServices.searchByID(messageID);
            if (responseFromDB.isEmpty()) {
                LOGGER.error(
                        "[Get] [MessageID {}] No MqiMessage Found with MessageID",
                        messageID);
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<MqiGenericMessageDto<T>>(
                        HttpStatus.NOT_FOUND);
            } else {
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<MqiGenericMessageDto<T>>(
                        transformMqiMessageToDtoGenericMessage(
                                responseFromDB.get(0)),
                        HttpStatus.OK);
            }
        } catch (Exception exc) {
            LOGGER.error("[Get] {}", exc.getMessage());
            this.appStatus.setError("MQI");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{topic}/{partition}/earliestOffset")
    public ResponseEntity<Long> earliestOffset(
            @PathVariable(name = "topic") final String topic,
            @PathVariable(name = "partition") final int partition,
            @RequestParam("group") final String group) {
        try {
            // Pour le topic / partition / group donné, on récupère l’offset du
            // message avec status != ACK et la plus petite date de lecture (à
            // voir
            // si on prend le plus petit offset)
            Set<MqiStateMessageEnum> ackStates = new HashSet<>();
            ackStates.add(MqiStateMessageEnum.ACK_KO);
            ackStates.add(MqiStateMessageEnum.ACK_OK);
            ackStates.add(MqiStateMessageEnum.ACK_WARN);
            List<MqiMessage> responseFromDB =
                    mongoDBServices.searchByTopicPartitionGroup(topic,
                            partition, group, ackStates);
            if (responseFromDB.isEmpty()) {
                // TODO define the strategy
                // Si pas d’entrée, on renvoie valeur par défaut :
                // -2 : on laisse le consumer faire ce qu’il veut
                // -1 : on démarre à l’offset du début
                // 0 : on démarre à l’offset de fin
                log(String.format(
                        "[EarliestOffset] [Topic %s] [Partition %d] [Group %s] Returning default Strategy",
                        topic, partition, group));
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<Long>(Long.valueOf(dftOffset), HttpStatus.OK);
            } else {
                log(String.format(
                        "[EarliestOffset] [Topic %s] [Partition %d] [Group %s] Returning earlist offset",
                        topic, partition, group));
                this.appStatus.setWaiting("MQI");
                return new ResponseEntity<Long>(
                        responseFromDB.get(0).getOffset(), HttpStatus.OK);
            }
        } catch (Exception exc) {
            LOGGER.error("[earliestOffset] {}", exc.getMessage());
            this.appStatus.setError("MQI");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param topic
     * @param pod
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{topic}/nbReading")
    public ResponseEntity<Integer> nbMessages(
            @PathVariable(name = "topic") final String topic,
            @RequestParam("pod") final String pod) {
        try {
            this.appStatus.setWaiting("MQI");
            return new ResponseEntity<Integer>(
                    mongoDBServices.countReadingMessages(pod, topic),
                    HttpStatus.OK);
        } catch (Exception exc) {
            LOGGER.error("[earliestOffset] {}", exc.getMessage());
            this.appStatus.setError("MQI");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private MqiLightMessageDto transformMqiMessageToMqiLightMessage(
            final MqiMessage messageToTransform) {
        MqiLightMessageDto messageTransformed = new MqiLightMessageDto();
        messageTransformed.setCategory(messageToTransform.getCategory());
        messageTransformed.setGroup(messageToTransform.getGroup());
        messageTransformed.setIdentifier(messageToTransform.getIdentifier());
        messageTransformed.setLastAckDate(messageToTransform.getLastAckDate());
        messageTransformed
                .setLastReadDate(messageToTransform.getLastReadDate());
        messageTransformed
                .setLastSendDate(messageToTransform.getLastSendDate());
        messageTransformed.setNbRetries(messageToTransform.getNbRetries());
        messageTransformed.setOffset(messageToTransform.getOffset());
        messageTransformed.setPartition(messageToTransform.getPartition());
        messageTransformed.setReadingPod(messageToTransform.getReadingPod());
        messageTransformed.setSendingPod(messageToTransform.getSendingPod());
        messageTransformed.setState(messageToTransform.getState());
        messageTransformed.setTopic(messageToTransform.getTopic());
        return messageTransformed;
    }

    @SuppressWarnings("unchecked")
    private MqiGenericMessageDto<T> transformMqiMessageToDtoGenericMessage(
            final MqiMessage messageToTransform) {
        MqiGenericMessageDto<T> messageTransformed =
                new MqiGenericMessageDto<T>();
        messageTransformed.setCategory(messageToTransform.getCategory());
        messageTransformed.setGroup(messageToTransform.getGroup());
        messageTransformed.setIdentifier(messageToTransform.getIdentifier());
        messageTransformed.setLastAckDate(messageToTransform.getLastAckDate());
        messageTransformed
                .setLastReadDate(messageToTransform.getLastReadDate());
        messageTransformed
                .setLastSendDate(messageToTransform.getLastSendDate());
        messageTransformed.setNbRetries(messageToTransform.getNbRetries());
        messageTransformed.setOffset(messageToTransform.getOffset());
        messageTransformed.setPartition(messageToTransform.getPartition());
        messageTransformed.setReadingPod(messageToTransform.getReadingPod());
        messageTransformed.setSendingPod(messageToTransform.getSendingPod());
        messageTransformed.setState(messageToTransform.getState());
        messageTransformed.setTopic(messageToTransform.getTopic());
        messageTransformed.setDto((T) messageToTransform.getDto());
        return messageTransformed;
    }

}
