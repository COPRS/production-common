package esa.s1pdgs.cpoc.mqi.server.distribution;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiRouteNotAvailable;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
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
@RequestMapping(path = "/messages")
public class ProductDistributionController {
	
	@SuppressWarnings("serial")
	static final class ProductDistributionException extends RuntimeException
	{		
		private final HttpStatus status;
		
		public ProductDistributionException() {
			this(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		public ProductDistributionException(HttpStatus status) {
			this.status = status;
		}

		public HttpStatus getStatus() {
			return status;
		}	
	}
	
    /**
     * Logger
     */
    static final Logger LOGGER = LogManager.getLogger(ProductDistributionController.class);

    /**
     * Message consumption controller
     */
    private final MessageConsumptionController messages;

    /**
     * Message publication controller
     */
    private final MessagePublicationController publication;

    /**
     * Application properties
     */
    private final ApplicationProperties properties;
    /**
     * Constructor
     * 
     * @param messages
     */
    @Autowired
    public ProductDistributionController(
            final MessageConsumptionController messages,
            final MessagePublicationController publication,
            final ApplicationProperties properties
    ) {
        LOGGER.info("Starting REST API for product distribution");
        this.messages = messages;
        this.publication = publication;
        this.properties = properties;
    }

    /**
     * Get the next message to proceed for auxiliary files
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{category}/next")
    public GenericMessageDto<? extends AbstractMessage> next(@PathVariable("category") String categoryName) throws ProductDistributionException {
    	final ProductCategory category = ProductCategory.valueOf(categoryName.toUpperCase());    	
        LOGGER.debug("[MONITOR] [category {}] [api next] Starting", category);

        // We wait to be sure one message is read
        try {
            Thread.sleep(properties.getWaitNextMs());
        } catch (InterruptedException iee) {
            LOGGER.debug(
                    "[MONITOR] [category {}] [api next] Interrupted exception during waiting",
                    category);
        }
        return nextMessage(category);
    }

    /**
     * Get the next message to proceed for auxiliary files
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{category}/ack")
    public void ack(@RequestBody final AckMessageDto ackDto, @PathVariable("category") String categoryName) 
    		throws ProductDistributionException {    	    	
    	final ProductCategory category = ProductCategory.valueOf(categoryName.toUpperCase());    	
    	LOGGER.info("[MONITOR] [category {}] [api ack] [messageId {}] Starting", category, ackDto.getMessageId());
    	
        try {
            final ResumeDetails resumeDetails = messages.ackMessage(category, ackDto.getMessageId(), ackDto.getAck(), ackDto.isStop());
            // If an error, simply dump the message into the log. Appending to kafka error queue
            // will be done where the error occurs
			if (ackDto.getAck() == Ack.ERROR) {
				if (resumeDetails != null) {
					if (LOGGER.isErrorEnabled()) {
						LOGGER.error(ackDto.getMessage() + " [resumeDetails {}]", resumeDetails);
					}
				} else {
					LOGGER.error(ackDto.getMessage());
				}
			}

        } catch (AbstractCodedException mcna) {
            LOGGER.error(
                    "[MONITOR] [category {}] [api ack] [messageId {}] [code {}] [error {}]",
                    category, ackDto.getMessageId(), mcna.getCode().getCode(),
                    mcna.getLogMessage());            
            throw new ProductDistributionException();
        }
        LOGGER.info("[MONITOR] [category {}] [api ack] [messageId {}] [httpCode {}] End", category, ackDto.getMessageId(), 200); 
    }

    /**
     * Publish a message
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/{category}/publish")
    public void publish(
    		@RequestBody final JsonNode message, 
    		@PathVariable("category") String categoryName
    ) throws ProductDistributionException {    	
    	final ProductCategory category = ProductCategory.valueOf(categoryName.toUpperCase());   
        try {
        	LOGGER.debug("[MONITOR] [category {}] [api publish] Received publish request", categoryName);
        	final ObjectMapper objMapper = new ObjectMapper();
        	final TypeFactory typeFactory = objMapper.getTypeFactory();
        	final JavaType javaType = typeFactory.constructParametricType(
        			GenericPublicationMessageDto.class, 
        			category.getDtoClass()
        	);
        	final GenericPublicationMessageDto<? extends AbstractMessage> mess = objMapper
        			.readValue(objMapper.treeAsTokens(message), javaType);
        	
        	// TODO: the productname does not exist everywhere anymore.
            LOGGER.info(
                    "[MONITOR] [category {}] [api publish] [messageId {}] [productName: {}] Starting",
                    category, mess.getInputMessageId(), mess.getMessageToPublish().getKeyObjectStorage());
        	
            publication.publish(category, mess.getMessageToPublish(), mess.getInputKey(), mess.getOutputKey());

        	// TODO: the productname does not exist everywhere anymore.
            LOGGER.info(
                    "[MONITOR] [category {}] [api publish] [httpCode {}] [messageId {}] [productName: {}] End",
                    category, 200, mess.getInputMessageId(), mess.getMessageToPublish().getKeyObjectStorage());
        } catch (MqiPublicationError kse) {
            LOGGER.error("[publish] KafkaSendException occurred: {}", kse.getMessage());
            throw new ProductDistributionException(HttpStatus.GATEWAY_TIMEOUT);
        } catch (MqiCategoryNotAvailable | MqiRouteNotAvailable mcna) {
            LOGGER.error(
                    "[MONITOR] [category {}] [api publish] [code {}] [error {}]",
                    category, mcna.getCode().getCode(), mcna.getLogMessage());
            throw new ProductDistributionException();
        } catch (IOException e) {
        	LOGGER.error("Could not deserialize JSON {}", message);
        	LOGGER.error(e);
		} 
    }
    
    private final GenericMessageDto<? extends AbstractMessage> nextMessage(final ProductCategory category) throws ProductDistributionException
    {
    	try {
			final GenericMessageDto<? extends AbstractMessage> res = messages.nextMessage(category);
	        LOGGER.debug("[MONITOR] [category {}] [api next] [httpCode {}] End", category, 200);     
			return res;
		} catch (AbstractCodedException e) {
	        LOGGER.error(
                    "[MONITOR] [category {}] [api next] [code {}] [error {}]",
                    category, e.getCode().getCode(), e.getLogMessage());	        
	        throw new ProductDistributionException();
		}    	
    }
}
