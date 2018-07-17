package fr.viveris.s1pdgs.mqi.server.distribution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.common.errors.mqi.MqiCategoryNotAvailable;
import fr.viveris.s1pdgs.common.errors.mqi.MqiPublicationError;
import fr.viveris.s1pdgs.mqi.model.rest.Ack;
import fr.viveris.s1pdgs.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.mqi.model.rest.GenericPublicationMessageDto;
import fr.viveris.s1pdgs.mqi.server.ApplicationProperties;
import fr.viveris.s1pdgs.mqi.server.consumption.MessageConsumptionController;
import fr.viveris.s1pdgs.mqi.server.publication.MessagePublicationController;

/**
 * Generic message distribution
 * 
 * @author Viveris Technologies
 * @param <T>
 *            product category
 */
public class GenericMessageDistribution<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(GenericMessageDistribution.class);

    /**
     * Message consumption controller
     */
    protected final MessageConsumptionController messages;

    /**
     * Message publication controller
     */
    protected final MessagePublicationController publication;

    /**
     * Application properties
     */
    protected final ApplicationProperties properties;

    /**
     * Product category
     */
    protected final ProductCategory category;

    /**
     * Constructor
     * 
     * @param messages
     */
    public GenericMessageDistribution(
            final MessageConsumptionController messages,
            final MessagePublicationController publication,
            final ApplicationProperties properties,
            final ProductCategory category) {
        LOGGER.info("Starting REST API for {}", category);
        this.messages = messages;
        this.publication = publication;
        this.properties = properties;
        this.category = category;
    }

    /**
     * Get the next message to proceed
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected ResponseEntity<GenericMessageDto<T>> next() {
        LOGGER.info("[MONITOR] [category {}] [api next] Starting", category);

        // We wait to be sure one message is read
        try {
            Thread.sleep(properties.getWaitNextMs());
        } catch (InterruptedException iee) {
            LOGGER.debug(
                    "[MONITOR] [category {}] [api next] Interrupted exception during waiting",
                    category);
        }

        ResponseEntity<GenericMessageDto<T>> result =
                new ResponseEntity<GenericMessageDto<T>>(
                        HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            result = new ResponseEntity<GenericMessageDto<T>>(
                    (GenericMessageDto<T>) messages.nextMessage(category),
                    HttpStatus.OK);
        } catch (MqiCategoryNotAvailable mcna) {
            LOGGER.error(
                    "[MONITOR] [category {}] [api next] [code {}] [error {}]",
                    category, mcna.getCode().getCode(), mcna.getLogMessage());
            result = new ResponseEntity<GenericMessageDto<T>>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOGGER.info("[MONITOR] [category {}] [api next] [httpCode {}] End",
                category, result.getStatusCodeValue());
        return result;
    }

    /**
     * Acknowledge the message
     * 
     * @param messageId
     * @return
     */
    protected ResponseEntity<Boolean> ack(final long identifier, final Ack ack,
            final String message) {
        LOGGER.info("[MONITOR] [category {}] [api ack] [messageId {}] Starting",
                category, identifier);

        // If error, log in KAFKA topic the message
        if (ack == Ack.ERROR) {
            publication.publishError(message);
        }

        // Ack message if OK
        ResponseEntity<Boolean> result =
                new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            result = new ResponseEntity<Boolean>(
                    messages.ackMessage(category, identifier, ack),
                    HttpStatus.OK);
        } catch (MqiCategoryNotAvailable mcna) {
            LOGGER.error(
                    "[MONITOR] [category {}] [api ack] [messageId {}] [code {}] [error {}]",
                    category, identifier, mcna.getCode().getCode(),
                    mcna.getLogMessage());
            result = new ResponseEntity<Boolean>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOGGER.info(
                "[MONITOR] [category {}] [api ack] [messageId {}] [httpCode {}] End",
                category, identifier, result.getStatusCodeValue());
        return result;
    }

    /**
     * Publish a message
     * 
     * @param inputMessageId
     * @param inputKey
     * @param outputKey
     * @param family
     * @param ouputMessage
     * @return
     */
    protected ResponseEntity<Void> publish(final String logMessage,
            final GenericPublicationMessageDto<T> message) {

        LOGGER.info(
                "[MONITOR] [category {}] [api publish] [messageId {}] {} Starting",
                category, message.getInputMessageId(), logMessage);

        ResponseEntity<Void> result =
                new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        try {
            publication.publish(category, message.getMessageToPublish());
            result = new ResponseEntity<Void>(HttpStatus.OK);
        } catch (MqiPublicationError kse) {
            LOGGER.error("[publish] KafkaSendException occurred: {}",
                    kse.getMessage());
            result = new ResponseEntity<Void>(HttpStatus.GATEWAY_TIMEOUT);
        } catch (MqiCategoryNotAvailable mcna) {
            LOGGER.error(
                    "[MONITOR] [category {}] [api publish] {} [code {}] [error {}]",
                    category, logMessage, mcna.getCode().getCode(),
                    mcna.getLogMessage());
            result = new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LOGGER.info(
                "[MONITOR] [category {}] [api publish] [httpCode {}] [messageId {}] {} End",
                category, result.getStatusCodeValue(),
                message.getInputMessageId(), logMessage);

        return result;
    }

}
