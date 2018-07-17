package fr.viveris.s1pdgs.mqi.server.distribution;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.common.ProductFamily;
import fr.viveris.s1pdgs.common.errors.mqi.MqiCategoryNotAvailable;
import fr.viveris.s1pdgs.common.errors.mqi.MqiPublicationError;
import fr.viveris.s1pdgs.mqi.model.rest.Ack;
import fr.viveris.s1pdgs.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.mqi.model.rest.GenericPublicationMessageDto;
import fr.viveris.s1pdgs.mqi.server.ApplicationProperties;
import fr.viveris.s1pdgs.mqi.server.consumption.MessageConsumptionController;
import fr.viveris.s1pdgs.mqi.server.publication.MessagePublicationController;

public class GenericMessageDistributionTest {

    /**
     * Mock the controller of consumed messages
     */
    @Mock
    private MessageConsumptionController messages;

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
     * The consumed messsage
     */
    private GenericMessageDto<String> consumedMessage;

    /**
     * The controller to test
     */
    private GenericMessageDistribution<String> controller;

    /**
     * Initialization
     * 
     * @throws MqiCategoryNotAvailable
     */
    @Before
    public void init() throws MqiCategoryNotAvailable {
        MockitoAnnotations.initMocks(this);

        consumedMessage =
                new GenericMessageDto<String>(123, "input-key", "message-test");

        doReturn(consumedMessage).when(messages).nextMessage(Mockito.any());

        doReturn(1000).when(properties).getWaitNextMs();

        controller = new GenericMessageDistribution<String>(messages,
                publication, properties, ProductCategory.AUXILIARY_FILES);
    }

    /**
     * Test when nextMessage throw an error
     * 
     * @throws MqiCategoryNotAvailable
     */
    @Test
    public void testNextApiCategoryNotAvailable()
            throws MqiCategoryNotAvailable {
        doThrow(new MqiCategoryNotAvailable(ProductCategory.AUXILIARY_FILES,
                "consumer")).when(messages).nextMessage(Mockito.any());
        ResponseEntity<GenericMessageDto<String>> message = controller.next();

        assertEquals(message.getBody(), null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, message.getStatusCode());

        verify(messages, times(1))
                .nextMessage(Mockito.eq(ProductCategory.AUXILIARY_FILES));
    }

    /**
     * Test when nextMessage throw an error
     * 
     * @throws MqiCategoryNotAvailable
     */
    @Test
    public void testAckApiCategoryNotAvailable()
            throws MqiCategoryNotAvailable {
        doThrow(new MqiCategoryNotAvailable(ProductCategory.AUXILIARY_FILES,
                "consumer")).when(messages).ackMessage(Mockito.any(),
                        Mockito.anyLong(), Mockito.any());
        ResponseEntity<Boolean> message =
                controller.ack(123L, Ack.OK, "message");

        assertEquals(message.getBody(), null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, message.getStatusCode());

        verify(messages, times(1)).ackMessage(
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(123L),
                Mockito.eq(Ack.OK));
    }

    /**
     * Test when nextMessage throw an error
     * 
     * @throws MqiCategoryNotAvailable
     * @throws MqiPublicationError
     */
    @Test
    public void testPublishApiCategoryNotAvailable()
            throws MqiCategoryNotAvailable, MqiPublicationError {
        doThrow(new MqiCategoryNotAvailable(ProductCategory.AUXILIARY_FILES,
                "publisher")).when(publication).publish(Mockito.any(),
                        Mockito.any());
        ResponseEntity<Void> message = controller.publish("log message",
                new GenericPublicationMessageDto<String>(ProductFamily.BLANK,
                        "message"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, message.getStatusCode());

        verify(publication, times(1)).publish(
                Mockito.eq(ProductCategory.AUXILIARY_FILES),
                Mockito.eq("message"));
    }

    /**
     * Test when nextMessage throw an error
     * 
     * @throws MqiCategoryNotAvailable
     * @throws MqiPublicationError
     */
    @Test
    public void testPublishApiError()
            throws MqiCategoryNotAvailable, MqiPublicationError {
        doThrow(MqiPublicationError.class).when(publication)
                .publish(Mockito.any(), Mockito.any());
        ResponseEntity<Void> message = controller.publish("log message",
                new GenericPublicationMessageDto<String>(ProductFamily.BLANK,
                        "message"));

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, message.getStatusCode());

        verify(publication, times(1)).publish(
                Mockito.eq(ProductCategory.AUXILIARY_FILES),
                Mockito.eq("message"));
    }
}
