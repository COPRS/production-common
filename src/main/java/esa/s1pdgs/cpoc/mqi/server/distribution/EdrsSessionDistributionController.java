package esa.s1pdgs.cpoc.mqi.server.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.MessageConsumptionController;
import esa.s1pdgs.cpoc.mqi.server.publication.MessagePublicationController;

/**
 * Message distribution controller
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/messages/edrs_sessions")
public class EdrsSessionDistributionController
        extends GenericMessageDistribution<EdrsSessionDto> {

    /**
     * Constructor
     * 
     * @param messages
     */
    @Autowired
    public EdrsSessionDistributionController(
            final MessageConsumptionController messages,
            final MessagePublicationController publication,
            final ApplicationProperties properties) {
        super(messages, publication, properties, ProductCategory.EDRS_SESSIONS);
    }

    /**
     * Get the next message to proceed for auxiliary files
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/next")
    public ResponseEntity<GenericMessageDto<EdrsSessionDto>> next() {
        return super.next();
    }

    /**
     * Get the next message to proceed for auxiliary files
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/ack")
    public ResponseEntity<Boolean> ack(@RequestBody() final AckMessageDto ack) {
        return super.ack(ack.getMessageId(), ack.getAck(), ack.getMessage());
    }

    /**
     * Publish a message
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/publish")
    public ResponseEntity<Void> publish(
            @RequestBody() final GenericPublicationMessageDto<EdrsSessionDto> message) {
        String log = String.format("[productName: %s]",
                message.getMessageToPublish().getObjectStorageKey());
        return super.publish(log, message);
    }

}
