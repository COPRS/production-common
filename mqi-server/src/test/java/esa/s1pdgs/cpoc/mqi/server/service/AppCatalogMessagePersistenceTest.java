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
import org.springframework.kafka.support.Acknowledgment;

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
     * Kafka acknowledgement
     */
    @Mock
    private Acknowledgment acknowledgment;


    /**
     * Received record
     */
    private ConsumerRecord<String, ProductionEvent> data;

    /**
     * Unit to test
     */
    private AppCatalogMessagePersistence<ProductionEvent> messagePersistence;

    /**
     * Initialization
     *
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        final ProductionEvent dto = new ProductionEvent("foo", "bar", ProductFamily.AUXILIARY_FILE);

        data = new ConsumerRecord<>("topic", 1, 145L,
                "key-record", dto);

        doReturn("pod-name").when(properties).getHostname();
        doReturn(consumerProperties).when(properties).getConsumer();
        doReturn("group-name").when(consumerProperties).getGroupId();

        doNothing().when(acknowledgment).acknowledge();

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

        messagePersistence = new AppCatalogMessagePersistence<>(mqiService, properties, otherAppService);
    }

    /**
     * Test messageShallBeIgnored when the other app return true
     *
     */
    @Test
    public void testMessageShallBeIgnoredWhenResponseTrue()
            throws AbstractCodedException {

        final AppCatMessageDto<ProductionEvent> msgLight = new AppCatMessageDto<>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("other-name");

        doReturn(true).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        // First time: msgLightForceRead
        assertTrue(messagePersistence.messageShallBeIgnored(data, msgLight, ProductCategory.AUXILIARY_FILES));
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
    }

    /**
     * Test messageShallBeIgnored when no response from the other app
     *
     */
    @Test
    public void testMessageShallBeIgnoredWhenNoResponse()
            throws AbstractCodedException {

        doThrow(new StatusProcessingApiError("uri", "error message"))
                .when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        testMessageShallBeIgnoredWhenFalse();
    }

    /**
     * Test messageShallBeIgnored when the other app return false
     *
     */
    @Test
    public void testMessageShallBeIgnoredWhenResponseFalse()
            throws AbstractCodedException {

        doReturn(false).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        testMessageShallBeIgnoredWhenFalse();
    }

    private void testMessageShallBeIgnoredWhenFalse()
            throws AbstractCodedException {

        final AppCatMessageDto<ProductionEvent> msgLight = new AppCatMessageDto<>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("other-name");

        final AppCatMessageDto<ProductionEvent> msgLightForceRead = new AppCatMessageDto<>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceRead.setState(MessageState.READ);
        msgLightForceRead.setReadingPod("pod-name");

        final AppCatMessageDto<ProductionEvent> msgLightForceAck = new AppCatMessageDto<>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceAck.setState(MessageState.ACK_KO);
        msgLightForceAck.setReadingPod("pod-name");
        msgLightForceAck.setSendingPod("other-name");

        final AppCatMessageDto<ProductionEvent> msgLightForceSend = new AppCatMessageDto<>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceSend.setState(MessageState.SEND);
        msgLightForceSend.setReadingPod("pod-name");
        msgLightForceSend.setSendingPod("other-name");

        final AppCatReadMessageDto<ProductionEvent> expectedReadBody =
                new AppCatReadMessageDto<>("group-name", "pod-name",
                        true, data.value());

        doReturn(msgLightForceRead, msgLightForceAck, msgLightForceSend)
                .when(mqiService).read(Mockito.any(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyLong(), Mockito.any());

        // First time: msgLightForceRead
        assertFalse(messagePersistence.messageShallBeIgnored(data, msgLight, ProductCategory.AUXILIARY_FILES));
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
        verify(mqiService, times(1)).read(Mockito.any(), Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));

        // Second time msgLightForceAck
        assertTrue(messagePersistence.messageShallBeIgnored(data, msgLight, ProductCategory.AUXILIARY_FILES));
        verify(otherAppService, times(2)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
        verify(mqiService, times(2)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));

        // Third time msgLightForceSend
        assertTrue(messagePersistence.messageShallBeIgnored(data, msgLight, ProductCategory.AUXILIARY_FILES));
        verify(otherAppService, times(3)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
        verify(mqiService, times(3)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
    }

    /**
     * Test onMessage when the message is already ack
     *
     */
    @Test
    public void testOnMessageWhenAlreadyAckKo() throws Exception {
        checkOnMessageWhenAckRead(MessageState.ACK_KO, false);
    }

    /**
     * Test onMessage when the message is already ack
     *
     */
    @Test
    public void testOnMessageWhenAlreadyAckOk() throws Exception {
        checkOnMessageWhenAckRead(MessageState.ACK_OK, false);
    }

    /**
     * Test onMessage when the message is already ack
     *
     */
    @Test
    public void testOnMessageWhenAlreadyAckWarn()
            throws Exception {
        checkOnMessageWhenAckRead(MessageState.ACK_WARN, false);
    }

    /**
     * Test onMessage when the message is assigned with success the first time
     *
     */
    @Test
    public void testOnMessageWhenRead() throws Exception {
        checkOnMessageWhenAckRead(MessageState.READ, true);
    }

    /**
     * Test onMessage when the message is already ack or is assigned
     *
     */
    private void checkOnMessageWhenAckRead(final MessageState state,
                                           final boolean pause) throws Exception {
        final AppCatMessageDto<ProductionEvent> msgLight = new AppCatMessageDto<>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(state);

        doReturn(msgLight).when(mqiService).read(Mockito.any(), Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyLong(), Mockito.any());

        messagePersistence.read(data, acknowledgment, genericConsumer, ProductCategory.AUXILIARY_FILES);

        verify(acknowledgment, times(1)).acknowledge();
        verifyZeroInteractions(otherAppService);
        if (pause) {
            verify(genericConsumer, times(1)).pause();
        } else {
            verifyZeroInteractions(genericConsumer);
        }
    }

    /**
     * Test onMessage when the first assignment fails
     *
     */
    @Test
    public void testOnMessageWhenProcessingByOtherPod()
            throws Exception {
        final AppCatMessageDto<ProductionEvent> msgLight = new AppCatMessageDto<>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setSendingPod("other-name");

        doReturn(msgLight).when(mqiService).read(Mockito.any(),Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyLong(), Mockito.any());
        doReturn(true).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        messagePersistence.read(data, acknowledgment, genericConsumer, ProductCategory.AUXILIARY_FILES);

        verify(acknowledgment, times(1)).acknowledge();
        verifyZeroInteractions(genericConsumer);
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
    }

    /**
     * Test onMessage when the first assignment fails
     *
     */
    @Test
    public void testOnMessageWhenProcessingButNoResponse()
            throws Exception {
        final AppCatMessageDto<ProductionEvent> msgLight = new AppCatMessageDto<>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setSendingPod("other-name");

        final AppCatMessageDto<ProductionEvent> msgLightForceRead = new AppCatMessageDto<>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceRead.setState(MessageState.READ);
        msgLightForceRead.setReadingPod("pod-name");

        final AppCatReadMessageDto<ProductionEvent> expectedReadBody =
                new AppCatReadMessageDto<>("group-name", "pod-name",
                        false, data.value());

        final AppCatReadMessageDto<ProductionEvent> expectedReadBodyForce =
                new AppCatReadMessageDto<>("group-name", "pod-name",
                        true, data.value());

        doReturn(msgLight, msgLightForceRead).when(mqiService).read(Mockito.any(),
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(),
                Mockito.any());
        doReturn(false).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        messagePersistence.read(data, acknowledgment, genericConsumer, ProductCategory.AUXILIARY_FILES);

        verify(mqiService, times(1)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
        verify(mqiService, times(1)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBodyForce));
        verify(acknowledgment, times(1)).acknowledge();
        verify(genericConsumer, times(1)).pause();
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
    }
}