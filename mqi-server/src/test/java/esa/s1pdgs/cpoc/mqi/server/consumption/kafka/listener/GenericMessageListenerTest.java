package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiService;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetOffsetApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiReadApiError;
import esa.s1pdgs.cpoc.common.errors.processing.StatusProcessingApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties.KafkaConsumerProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;
import esa.s1pdgs.cpoc.mqi.server.persistence.OtherApplicationService;
import esa.s1pdgs.cpoc.mqi.server.status.AppStatus;

/**
 * Test the listener GenericMessageListener
 * 
 * @author Viveris Technologies
 */
public class GenericMessageListenerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Properties
     */
    @Mock
    private KafkaProperties properties;
    @Mock
    private KafkaConsumerProperties consumerProperties;

    /**
     * Service for persisting data
     */
    @Mock
    private AppCatalogMqiService service;

    /**
     * Service for checking if a message is processing or not by another
     */
    @Mock
    private OtherApplicationService otherAppService;

    /**
     * Generic consumer
     */
    @Mock
    private GenericConsumer<ProductDto> genericConsumer;

    /**
     * Kafka acknowledgement
     */
    @Mock
    private Acknowledgment acknowledgment;

    /**
     * Application status
     */
    @Mock
    private AppStatus appStatus;

    /**
     * Consumer of message listener
     */
    @Mock
    private Consumer<String, String> onMsgConsumer;

    /**
     * Listener to test
     */
    private GenericMessageListener<ProductDto> listener;

    /**
     * Received record
     */
    private ConsumerRecord<String, ProductDto> data;

    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);
        
        final ProductDto dto = new ProductDto("foo", "bar", ProductFamily.AUXILIARY_FILE);

        data = new ConsumerRecord<String, ProductDto>("topic", 1, 145L,
                "key-record", dto);

        doReturn("pod-name").when(properties).getHostname();
        doReturn(consumerProperties).when(properties).getConsumer();
        doReturn("group-name").when(consumerProperties).getGroupId();

        doNothing().when(appStatus).resetError();
        doNothing().when(appStatus).setError();

        doNothing().when(genericConsumer).pause();
        doNothing().when(acknowledgment).acknowledge();

        doReturn(-2L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(0), Mockito.anyString());
        doReturn(-1L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(1), Mockito.anyString());
        doReturn(0L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(2), Mockito.anyString());
        doReturn(128L).when(service).getEarliestOffset(Mockito.anyString(),
                Mockito.eq(3), Mockito.anyString());
        doThrow(new AppCatalogMqiGetOffsetApiError("uri", "message"))
                        .when(service).getEarliestOffset(Mockito.anyString(),
                                Mockito.eq(4), Mockito.anyString());

        listener = new GenericMessageListener<ProductDto>(ProductCategory.AUXILIARY_FILES, properties, service,
                otherAppService, genericConsumer, appStatus);
    }

    /**
     * Test pause
     */
    @Test
    public void testPause() {
        listener.pause();

        verify(genericConsumer, times(1)).pause();
        verifyNoMoreInteractions(genericConsumer);
        verifyZeroInteractions(service);
        verifyZeroInteractions(otherAppService);
        verifyZeroInteractions(acknowledgment);
    }

    /**
     * Test acknowledge
     */
    @Test
    public void testAcknowledge() {
        listener.acknowlegde(data, acknowledgment);

        verify(acknowledgment, times(1)).acknowledge();
        verifyNoMoreInteractions(acknowledgment);
        verifyZeroInteractions(genericConsumer);
        verifyZeroInteractions(service);
        verifyZeroInteractions(otherAppService);
    }

    /**
     * Test acknowledge
     */
    @Test
    public void testAcknowledgeWhenException() {
        doThrow(NullPointerException.class).when(acknowledgment).acknowledge();

        listener.acknowlegde(data, acknowledgment);

        verify(acknowledgment, times(1)).acknowledge();
        verifyNoMoreInteractions(acknowledgment);
        verifyZeroInteractions(genericConsumer);
        verifyZeroInteractions(service);
        verifyZeroInteractions(otherAppService);
    }

    /**
     * Test messageShallBeIgnored when the other app return true
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testmessageShallBeIgnoredWhenResponseTrue()
            throws AbstractCodedException {

        AppCatMessageDto<ProductDto> msgLight = new AppCatMessageDto<ProductDto>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("other-name");

        doReturn(true).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        // First time: msgLightForceRead
        assertTrue(listener.messageShallBeIgnored(data, msgLight));
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

        AppCatMessageDto<ProductDto> msgLight = new AppCatMessageDto<ProductDto>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("other-name");

        AppCatMessageDto<ProductDto> msgLightForceRead = new AppCatMessageDto<ProductDto>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceRead.setState(MessageState.READ);
        msgLightForceRead.setReadingPod("pod-name");

        AppCatMessageDto<ProductDto> msgLightForceAck = new AppCatMessageDto<ProductDto>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceAck.setState(MessageState.ACK_KO);
        msgLightForceAck.setReadingPod("pod-name");
        msgLightForceAck.setSendingPod("other-name");

        AppCatMessageDto<ProductDto> msgLightForceSend = new AppCatMessageDto<ProductDto>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceSend.setState(MessageState.SEND);
        msgLightForceSend.setReadingPod("pod-name");
        msgLightForceSend.setSendingPod("other-name");

        AppCatReadMessageDto<ProductDto> expectedReadBody =
                new AppCatReadMessageDto<ProductDto>("group-name", "pod-name",
                        true, data.value());

        doReturn(msgLightForceRead, msgLightForceAck, msgLightForceSend)
                .when(service).read(Mockito.any(),Mockito.anyString(), Mockito.anyInt(),
                        Mockito.anyLong(), Mockito.any());

        // First time: msgLightForceRead
        assertFalse(listener.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
        verify(service, times(1)).read(Mockito.any(),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));

        // Second time msgLightForceAck
        assertTrue(listener.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(2)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
        verify(service, times(2)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));

        // Third time msgLightForceSend
        assertTrue(listener.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(3)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
        verify(service, times(3)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
    }

    /**
     * Test onMessage when the message is already ack
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenAlreadyAckKo() throws AbstractCodedException {
        checkOnMEssageWhenAckRead(MessageState.ACK_KO, false);
    }

    /**
     * Test onMessage when the message is already ack
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenAlreadyAckOk() throws AbstractCodedException {
        checkOnMEssageWhenAckRead(MessageState.ACK_OK, false);
    }

    /**
     * Test onMessage when the message is already ack
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenAlreadyAckWarn()
            throws AbstractCodedException {
        checkOnMEssageWhenAckRead(MessageState.ACK_WARN, false);
    }

    /**
     * Test onMessage when the message is assigned with success the first time
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenRead() throws AbstractCodedException {
        checkOnMEssageWhenAckRead(MessageState.READ, true);
    }

    /**
     * Test onMessage when the message is already ack or is assigned
     * 
     * @param state
     * @param pause
     * @throws AbstractCodedException
     */
    private void checkOnMEssageWhenAckRead(MessageState state,
            boolean pause) throws AbstractCodedException {
        AppCatMessageDto<ProductDto> msgLight = new AppCatMessageDto<ProductDto>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(state);

        AppCatReadMessageDto<ProductDto> expectedReadBody =
                new AppCatReadMessageDto<ProductDto>("group-name", "pod-name",
                        false, data.value());

        doReturn(msgLight).when(service).read(Mockito.any(), Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyLong(), Mockito.any());

        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verifyZeroInteractions(onMsgConsumer);
        verify(acknowledgment, times(1)).acknowledge();
        verifyZeroInteractions(otherAppService);
        verify(service, times(1)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
        verify(appStatus, times(1)).resetError();
        if (pause) {
            verify(genericConsumer, times(1)).pause();
        } else {
            verifyZeroInteractions(genericConsumer);
        }
    }

    /**
     * Test onMessage when the first assignment fails
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenFirstReadFails()
            throws AbstractCodedException {
        doThrow(new AppCatalogMqiReadApiError(ProductCategory.AUXILIARY_FILES,
                "uri", "dto-object", "error-message")).when(service).read(Mockito.any(),
                        Mockito.anyString(), Mockito.anyInt(),
                        Mockito.anyLong(), Mockito.any());

        AppCatReadMessageDto<ProductDto> expectedReadBody =
                new AppCatReadMessageDto<>("group-name", "pod-name",
                        false, data.value());

        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verify(appStatus, times(1)).setError();
        verifyNoMoreInteractions(appStatus);
        verifyZeroInteractions(onMsgConsumer);
        verifyZeroInteractions(otherAppService);
        verifyZeroInteractions(genericConsumer);
        verify(service, times(1)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
    }

    /**
     * Test onMessage when the first assignment fails
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenProcessingBySamePod()
            throws AbstractCodedException {
        AppCatMessageDto<ProductDto> msgLight = new AppCatMessageDto<ProductDto>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setSendingPod("pod-name");

        AppCatReadMessageDto<ProductDto> expectedReadBody =
                new AppCatReadMessageDto<>("group-name", "pod-name",
                        false, data.value());

        doReturn(msgLight).when(service).read(Mockito.any(),Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyLong(), Mockito.any());

        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verify(service, times(1)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
        verify(appStatus, times(1)).resetError();
        verify(acknowledgment, times(1)).acknowledge();
        verify(genericConsumer, times(1)).pause();
        verifyNoMoreInteractions(appStatus);
        verifyZeroInteractions(onMsgConsumer);
        verifyZeroInteractions(otherAppService);
    }

    /**
     * Test onMessage when the first assignment fails
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenProcessingByOtherPod()
            throws AbstractCodedException {
        AppCatMessageDto<ProductDto> msgLight = new AppCatMessageDto<ProductDto>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setSendingPod("other-name");

        AppCatReadMessageDto<ProductDto> expectedReadBody =
                new AppCatReadMessageDto<>("group-name", "pod-name",
                        false, data.value());

        doReturn(msgLight).when(service).read(Mockito.any(),Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyLong(), Mockito.any());
        doReturn(true).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verify(service, times(1)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
        verify(appStatus, times(1)).resetError();
        verify(acknowledgment, times(1)).acknowledge();
        verifyNoMoreInteractions(appStatus);
        verifyZeroInteractions(genericConsumer);
        verifyZeroInteractions(onMsgConsumer);
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
    }

    /**
     * Test onMessage when the first assignment fails
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenProcessingButNoResponse()
            throws AbstractCodedException {
        AppCatMessageDto<ProductDto> msgLight = new AppCatMessageDto<ProductDto>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MessageState.SEND);
        msgLight.setSendingPod("other-name");

        AppCatMessageDto<ProductDto> msgLightForceRead = new AppCatMessageDto<ProductDto>(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceRead.setState(MessageState.READ);
        msgLightForceRead.setReadingPod("pod-name");

        AppCatReadMessageDto<ProductDto> expectedReadBody =
                new AppCatReadMessageDto<>("group-name", "pod-name",
                        false, data.value());

        AppCatReadMessageDto<ProductDto> expectedReadBodyForce =
                new AppCatReadMessageDto<>("group-name", "pod-name",
                        true, data.value());

        doReturn(msgLight, msgLightForceRead).when(service).read(Mockito.any(),
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(),
                Mockito.any());
        doReturn(false).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.any(), Mockito.anyLong());

        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verify(service, times(1)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
        verify(service, times(1)).read(Mockito.eq(ProductCategory.AUXILIARY_FILES),Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBodyForce));
        verify(appStatus, times(1)).resetError();
        verifyNoMoreInteractions(appStatus);
        verify(acknowledgment, times(1)).acknowledge();
        verify(genericConsumer, times(1)).pause();
        verifyZeroInteractions(onMsgConsumer);
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES), Mockito.eq(1234L));
    }

}
