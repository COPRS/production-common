package fr.viveris.s1pdgs.mqi.server.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.mqi.model.queue.LevelJobDto;
import fr.viveris.s1pdgs.mqi.model.rest.AckMessageDto;
import fr.viveris.s1pdgs.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.mqi.model.rest.GenericPublicationMessageDto;
import fr.viveris.s1pdgs.mqi.server.ApplicationProperties;
import fr.viveris.s1pdgs.mqi.server.consumption.MessageConsumptionController;
import fr.viveris.s1pdgs.mqi.server.publication.MessagePublicationController;

/**
 * Message distribution controller
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/messages/level_jobs")
public class LevelJobDistributionController
        extends GenericMessageDistribution<LevelJobDto> {

    /**
     * Constructor
     * 
     * @param messages
     */
    @Autowired
    public LevelJobDistributionController(
            final MessageConsumptionController messages,
            final MessagePublicationController publication,
            final ApplicationProperties properties) {
        super(messages, publication, properties, ProductCategory.LEVEL_JOBS);
    }

    /**
     * Get the next message to proceed for auxiliary files
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/next")
    public ResponseEntity<GenericMessageDto<LevelJobDto>> next() {
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
            @RequestBody() final GenericPublicationMessageDto<LevelJobDto> message) {
        String log = String.format("[productName: %s]",
                message.getMessageToPublish().getProductIdentifier());
        return super.publish(log, message);
    }

}
