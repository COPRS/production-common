package esa.s1pdgs.cpoc.mqi.server.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiService;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetOffsetApiError;
import esa.s1pdgs.cpoc.common.errors.processing.StatusProcessingApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.MessageConsumer;

public class AppCatalogMessagePersistenceTest {

    /**
     * Properties
     */
    @Mock
    private KafkaProperties properties;

    @Mock
    private KafkaProperties.KafkaConsumerProperties consumerProperties;

    /**
     * Generic consumer
     */
    @Mock
    private GenericConsumer<ProductionEvent> genericConsumer;

    /**
     * Service for checking if a message is processing or not by another
     */
    @Mock
    private OtherApplicationService otherAppService;

    @Mock
    private AppCatalogMqiService<ProductionEvent> mqiService;

    /**
     * Received record
     */
    private ConsumerRecord<String, ProductionEvent> data;

    /**
     * Listener to test
     */
    private AppCatalogMessagePersistence<ProductionEvent> messagePersistence;

    /**
     * Initialization
     *
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        final ProductionEvent dto = new ProductionEvent("foo", "bar", ProductFamily.AUXILIARY_FILE);

        data = new ConsumerRecord<String, ProductionEvent>("topic", 1, 145L,
                "key-record", dto);

        doReturn("pod-name").when(properties).getHostname();
        doReturn(consumerProperties).when(properties).getConsumer();
        doReturn("group-name").when(consumerProperties).getGroupId();


        doNothing().when(genericConsumer).pause();
        //doNothing().when(acknowledgment).acknowledge();

        doReturn(-2L).when(mqiService).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(0), Mockito.anyString());
        doReturn(-1L).when(mqiService).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(1), Mockito.anyString());
        doReturn(0L).when(mqiService).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(2), Mockito.anyString());
        doReturn(128L).when(mqiService).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(3), Mockito.anyString());
        doThrow(new AppCatalogMqiGetOffsetApiError("uri", "message"))
                .when(mqiService).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(4), Mockito.anyString());

        messagePersistence = new AppCatalogMessagePersistence<>(mqiService, properties, MessageConsumer.nullConsumer(), genericConsumer, otherAppService, ProductCategory.AUXILIARY_FILES);
    }

    /**
     * Test messageShallBeIgnored when the other app return true
     *
     * @throws AbstractCodedException
     */
    @Test
    public void testmessageShallBeIgnoredWhenResponseTrue()
            throws AbstractCodedException {

        final AppCatMessageDto<ProductionEvent> msgLight = new AppCatMessageDto<ProductionEvent>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("other-name");

        doReturn(true).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        // First time: msgLightForceRead
        assertTrue(messagePersistence.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
    }

    /**
     * Test messageShallBeIgnored when no response from the other app
     *
     * @throws AbstractCodedException
     */
    @Test
    public void testmessageShallBeIgnoredWhenNoResponse()
            throws AbstractCodedException {

        doThrow(new StatusProcessingApiError("uri", "error message"))
                .when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        testmessageShallBeIgnoredWhenFalse();
    }

    /**
     * Test messageShallBeIgnored when the other app return false
     *
     * @throws AbstractCodedException
     */
    @Test
    public void testmessageShallBeIgnoredWhenResponseFalse()
            throws AbstractCodedException {

        doReturn(false).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        testmessageShallBeIgnoredWhenFalse();
    }

    private void testmessageShallBeIgnoredWhenFalse()
            throws AbstractCodedException {

        final AppCatMessageDto<ProductionEvent> msgLight = new AppCatMessageDto<ProductionEvent>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("other-name");

        final AppCatMessageDto<ProductionEvent> msgLightForceRead = new AppCatMessageDto<ProductionEvent>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceRead.setState(MessageState.READ);
        msgLightForceRead.setReadingPod("pod-name");

        final AppCatMessageDto<ProductionEvent> msgLightForceAck = new AppCatMessageDto<ProductionEvent>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceAck.setState(MessageState.ACK_KO);
        msgLightForceAck.setReadingPod("pod-name");
        msgLightForceAck.setSendingPod("other-name");

        final AppCatMessageDto<ProductionEvent> msgLightForceSend = new AppCatMessageDto<ProductionEvent>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceSend.setState(MessageState.SEND);
        msgLightForceSend.setReadingPod("pod-name");
        msgLightForceSend.setSendingPod("other-name");

        final AppCatReadMessageDto<ProductionEvent> expectedReadBody =
                new AppCatReadMessageDto<ProductionEvent>("group-name", "pod-name",
                        true, data.value());

        doReturn(msgLightForceRead, msgLightForceAck, msgLightForceSend)
                .when(mqiService).read(Mockito.any(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyLong(), Mockito.any());

        // First time: msgLightForceRead
        assertFalse(messagePersistence.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
        verify(mqiService, times(1)).read(Mockito.any(), Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));

        // Second time msgLightForceAck
        assertTrue(messagePersistence.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(2)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
        verify(mqiService, times(2)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));

        // Third time msgLightForceSend
        assertTrue(messagePersistence.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(3)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
        verify(mqiService, times(3)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
    }
}