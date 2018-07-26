package esa.s1pdgs.cpoc.appcatalog.controllers.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiLightMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiSendMessageDto;
import esa.s1pdgs.cpoc.appcatalog.services.mongodb.MongoDBServices;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;

/**
 * REST server for managing MQI messages in DB for the product category
 * LEVEL_REPORTS
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/mqi/level_products")
public class MqiLevelProductController
        extends GenericMqiController<LevelProductDto> {

    /**
     * @param mongoDBServices
     * @param maxRetries
     */
    @Autowired
    public MqiLevelProductController(final MongoDBServices mongoDBServices,
            @Value("${mqi.max-retries}") final int maxRetries) {
        super(mongoDBServices, maxRetries, ProductCategory.LEVEL_PRODUCTS);
    }

    /**
     * @see GenericMqiController#readMessage(String, int, long,
     *      MqiGenericReadMessageDto)
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{topic}/{partition}/{offset}/read")
    public ResponseEntity<MqiLightMessageDto> readMessage(
            @PathVariable(name = "topic") final String topic,
            @PathVariable(name = "partition") final int partition,
            @PathVariable(name = "offset") final long offset,
            @RequestBody final MqiGenericReadMessageDto<LevelProductDto> body) {
        return super.readMessage(topic, partition, offset, body);
    }

    /**
     * @see GenericMqiController#next(String)
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/next")
    public ResponseEntity<List<MqiGenericMessageDto<LevelProductDto>>> next(
            @RequestParam("pod") final String pod) {
        return super.next(pod);
    }

    /**
     * @see GenericMqiController#sendMessage(long, MqiSendMessageDto)
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{messageID}/send")
    public ResponseEntity<Boolean> sendMessage(
            @PathVariable(name = "messageID") final long messageID,
            @RequestBody final MqiSendMessageDto body) {
        return super.sendMessage(messageID, body);
    }

    /**
     * @see GenericMqiController#ackMessage(long, AckMessageDto)
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{messageID}/ack")
    public ResponseEntity<MqiGenericMessageDto<LevelProductDto>> ackMessage(
            @PathVariable(name = "messageID") final long messageID,
            @RequestBody final Ack ack) {
        return super.ackMessage(messageID, ack);
    }

    /**
     * @see GenericMqiController#earliestOffset(String, int, String)
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{topic}/{partition}/earliestOffset")
    public ResponseEntity<Long> earliestOffset(
            @PathVariable(name = "topic") final String topic,
            @PathVariable(name = "partition") final int partition,
            @RequestParam("group") final String group) {
        return super.earliestOffset(topic, partition, group);
    }

}
