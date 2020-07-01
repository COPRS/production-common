package esa.s1pdgs.cpoc.mqi.server.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiRouteNotAvailable;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.rest.ProductDistributionController.ProductDistributionException;
import esa.s1pdgs.cpoc.mqi.server.service.MessageConsumptionController;
import esa.s1pdgs.cpoc.mqi.server.service.MessagePublicationController;

public class TestProductDistributionController {
	
	static final class StringDto extends AbstractMessage {
		public StringDto() {
		}		
	}

    /**
     * Mock the controller of consumed messages
     */
    @Mock
    private MessageConsumptionController<StringDto> messages;

    /**
     * Mock the controller of published messages
     */
    @Mock
    private MessagePublicationController publication;

    /**
     * Mock the application properties
     */
    @Mock
    private ApplicationProperties properties;

    /**
     * The controller to test
     */
    private ProductDistributionController controller;

    /**
     * Initialization
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        GenericMessageDto<StringDto> consumedMessage = new GenericMessageDto<>(123, "input-key", new StringDto());

        doReturn(consumedMessage).when(messages).nextMessage(Mockito.any());

        doReturn(1000).when(properties).getWaitNextMs();

        controller = new ProductDistributionController(messages, publication, properties);
    }

    /**
     * Test when nextMessage throw an error
     */
    @Test
    public void testNextApiCategoryNotAvailable()
            throws AbstractCodedException {
        doThrow(new MqiCategoryNotAvailable(ProductCategory.AUXILIARY_FILES,
                "consumer")).when(messages).nextMessage(Mockito.any());
        try {
			controller.next(ProductCategory.AUXILIARY_FILES.name());
			fail();
		} catch (final ProductDistributionException e) {
			// expected
		}
        verify(messages, times(1))
                .nextMessage(Mockito.eq(ProductCategory.AUXILIARY_FILES));
    }

    /**
     * Test when nextMessage throw an error
     */
    @Test
    public void testAckApiCategoryNotAvailable()
            throws AbstractCodedException {
        doThrow(new MqiCategoryNotAvailable(ProductCategory.AUXILIARY_FILES,
                "consumer")).when(messages).ackMessage(Mockito.any(),
                        Mockito.anyLong(), Mockito.any(), Mockito.anyBoolean());
        
        try {
		    controller.ack(new AckMessageDto(123L, Ack.OK, "message", false), ProductCategory.AUXILIARY_FILES.name());
			fail();
		} catch (final ProductDistributionException e) {
			// expected
		}
        verify(messages, times(1)).ackMessage(
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(123L),
                Mockito.eq(Ack.OK), Mockito.eq(false));
    }

    /**
     * Test when nextMessage throw an error
     * 
     */
    @Test
    public void testPublishApiCategoryNotAvailable()
            throws MqiCategoryNotAvailable, MqiPublicationError, MqiRouteNotAvailable {
        doThrow(new MqiCategoryNotAvailable(ProductCategory.AUXILIARY_FILES,
                "publisher")).when(publication).publish(Mockito.any(), 
                        Mockito.any(), Mockito.any(), Mockito.any());
        
        final ProductionEvent dto = new ProductionEvent("test321", "bar", ProductFamily.AUXILIARY_FILE);
        
        try {
		    final GenericPublicationMessageDto<? extends AbstractMessage> mess = new GenericPublicationMessageDto<>(
                    ProductFamily.AUXILIARY_FILE,
                    dto
            );
        	final ObjectMapper objMapper = new ObjectMapper();
        	final JsonNode json = objMapper.convertValue(mess, JsonNode.class);		    
		    
		    controller.publish(json, ProductCategory.AUXILIARY_FILES.name());
			fail();
		} catch (final ProductDistributionException e) {
			// expected
		}
        verify(publication, times(1)).publish(
                Mockito.eq(ProductCategory.AUXILIARY_FILES),
                Mockito.eq(dto),
                Mockito.eq(null), 
                Mockito.eq(null));
    }

    /**
     * Test when nextMessage throw an error
     * 
     */
    @Test
    public void testPublishApiError()
            throws MqiCategoryNotAvailable, MqiPublicationError, MqiRouteNotAvailable {
        doThrow(MqiPublicationError.class).when(publication)
                .publish(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        
        final ProductionEvent dto = new ProductionEvent("test321", "bar", ProductFamily.AUXILIARY_FILE);
        
        try {
        	final ProductionEvent event = new ProductionEvent("test321", "bar", ProductFamily.AUXILIARY_FILE);
        	event.setUid(dto.getUid());
		    final GenericPublicationMessageDto<? extends AbstractMessage> mess = new GenericPublicationMessageDto<>(
                    ProductFamily.AUXILIARY_FILE,
                    event
            );
        	final ObjectMapper objMapper = new ObjectMapper();
        	final JsonNode json = objMapper.convertValue(mess, JsonNode.class);	
		    
		    controller.publish(json, ProductCategory.AUXILIARY_FILES.name());
			fail();
		} catch (final ProductDistributionException e) {
			// expected
	        assertEquals(HttpStatus.GATEWAY_TIMEOUT, e.getStatus());
		}   
        verify(publication, times(1)).publish(
                Mockito.eq(ProductCategory.AUXILIARY_FILES),
                Mockito.eq(dto), 
                Mockito.eq(null), 
                Mockito.eq(null));
    }
}
