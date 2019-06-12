package esa.s1pdgs.cpoc.mqi.server.distribution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.MessageConsumptionController;
import esa.s1pdgs.cpoc.mqi.server.publication.MessagePublicationController;

/**
 * Controller around errors
 * 
 * @author Viveris Technologies
 */
@RestController
@RequestMapping(path = "/errorQueue")
public class ErrorsController {

	/**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(ErrorsController.class);
    
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
     * Constructor
     * 
     * @param publication
     */
    public ErrorsController(
            final MessageConsumptionController messages,
            final MessagePublicationController publication,
            final ApplicationProperties properties) {
        LOGGER.info("Starting REST API for error messages");
        this.messages = messages;
        this.publication = publication;
        this.properties = properties;
    }
    
    /**
     * Get the next error message
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/next")
    public ResponseEntity<GenericMessageDto<ErrorDto>> next() {
    	LOGGER.debug("[MONITOR] [errors] [api next] Starting");

        // We wait to be sure one message is read
        try {
            Thread.sleep(properties.getWaitNextMs());
        } catch (InterruptedException iee) {
            LOGGER.debug(
                    "[MONITOR] [errors] [api next] Interrupted exception during waiting");
        }

        ResponseEntity<GenericMessageDto<ErrorDto>> result =
                new ResponseEntity<GenericMessageDto<ErrorDto>>(
                        HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            result = new ResponseEntity<GenericMessageDto<ErrorDto>>(
                    (GenericMessageDto<ErrorDto>) messages.nextMessage(ProductCategory.ERRORS),
                    HttpStatus.OK);
        } catch (AbstractCodedException mcna) {
            LOGGER.error(
                    "[MONITOR] [errors] [api next] [code {}] [error {}]",
                    mcna.getCode().getCode(), mcna.getLogMessage());
            result = new ResponseEntity<GenericMessageDto<ErrorDto>>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LOGGER.debug("[MONITOR] [errors] [api next] [httpCode {}] End",
                result.getStatusCodeValue());
        return result;
    }

    /**
     * Publish a message
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, path = "/publish")
    public ResponseEntity<Void> publish(@RequestBody() final String message) {
        ResponseEntity<Void> ret =
                new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT);
        if (publication.publishError(message)) {
            ret = new ResponseEntity<>(HttpStatus.OK);
        }
        return ret;
    }

}
