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

import esa.s1pdgs.cpoc.appcatalog.client.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiLightMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetOffsetApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiReadApiError;
import esa.s1pdgs.cpoc.common.errors.processing.StatusProcessingApiError;
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
    private GenericAppCatalogMqiService<String> service;

    /**
     * Service for checking if a message is processing or not by another
     */
    @Mock
    private OtherApplicationService otherAppService;

    /**
     * Generic consumer
     */
    @Mock
    private GenericConsumer<String> genericConsumer;

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
    private GenericMessageListener<String> listener;

    /**
     * Received record
     */
    private ConsumerRecord<String, String> data;

    /**
     * Initialization
     * 
     * @throws AbstractCodedException
     */
    @Before
    public void init() throws AbstractCodedException {
        MockitoAnnotations.initMocks(this);

        data = new ConsumerRecord<String, String>("topic", 1, 145L,
                "key-record", "value-record");

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
        doThrow(new AppCatalogMqiGetOffsetApiError(
                ProductCategory.AUXILIARY_FILES, "uri", "message"))
                        .when(service).getEarliestOffset(Mockito.anyString(),
                                Mockito.eq(4), Mockito.anyString());

        listener = new GenericMessageListener<>(properties, service,
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

        MqiLightMessageDto msgLight = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MqiStateMessageEnum.SEND);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("other-name");

        doReturn(true).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.anyLong());

        // First time: msgLightForceRead
        assertTrue(listener.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(1234L));
        verifyZeroInteractions(service);
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
                .when(otherAppService)
                .isProcessing(Mockito.anyString(), Mockito.anyLong());

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
                Mockito.anyLong());

        testmessageShallBeIgnoredWhenFalse();
    }

    private void testmessageShallBeIgnoredWhenFalse()
            throws AbstractCodedException {

        MqiLightMessageDto msgLight = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MqiStateMessageEnum.SEND);
        msgLight.setReadingPod("pod-name");
        msgLight.setSendingPod("other-name");

        MqiLightMessageDto msgLightForceRead = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceRead.setState(MqiStateMessageEnum.READ);
        msgLightForceRead.setReadingPod("pod-name");

        MqiLightMessageDto msgLightForceAck = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceAck.setState(MqiStateMessageEnum.ACK_KO);
        msgLightForceAck.setReadingPod("pod-name");
        msgLightForceAck.setSendingPod("other-name");

        MqiLightMessageDto msgLightForceSend = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceSend.setState(MqiStateMessageEnum.SEND);
        msgLightForceSend.setReadingPod("pod-name");
        msgLightForceSend.setSendingPod("other-name");

        MqiGenericReadMessageDto<String> expectedReadBody =
                new MqiGenericReadMessageDto<String>("group-name", "pod-name",
                        true, data.value());

        doReturn(msgLightForceRead, msgLightForceAck, msgLightForceSend)
                .when(service).read(Mockito.anyString(), Mockito.anyInt(),
                        Mockito.anyLong(), Mockito.any());

        // First time: msgLightForceRead
        assertFalse(listener.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(1234L));
        verify(service, times(1)).read(Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));

        // Second time msgLightForceAck
        assertTrue(listener.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(2)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(1234L));
        verify(service, times(2)).read(Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));

        // Third time msgLightForceSend
        assertTrue(listener.messageShallBeIgnored(data, msgLight));
        verify(otherAppService, times(3)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(1234L));
        verify(service, times(3)).read(Mockito.eq(data.topic()),
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
        checkOnMEssageWhenAckRead(MqiStateMessageEnum.ACK_KO, false);
    }

    /**
     * Test onMessage when the message is already ack
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenAlreadyAckOk() throws AbstractCodedException {
        checkOnMEssageWhenAckRead(MqiStateMessageEnum.ACK_OK, false);
    }

    /**
     * Test onMessage when the message is already ack
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenAlreadyAckWarn()
            throws AbstractCodedException {
        checkOnMEssageWhenAckRead(MqiStateMessageEnum.ACK_WARN, false);
    }

    /**
     * Test onMessage when the message is assigned with success the first time
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenRead() throws AbstractCodedException {
        checkOnMEssageWhenAckRead(MqiStateMessageEnum.READ, true);
    }

    /**
     * Test onMessage when the message is already ack or is assigned
     * 
     * @param state
     * @param pause
     * @throws AbstractCodedException
     */
    private void checkOnMEssageWhenAckRead(MqiStateMessageEnum state,
            boolean pause) throws AbstractCodedException {
        MqiLightMessageDto msgLight = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(state);

        MqiGenericReadMessageDto<String> expectedReadBody =
                new MqiGenericReadMessageDto<String>("group-name", "pod-name",
                        false, data.value());

        doReturn(msgLight).when(service).read(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyLong(), Mockito.any());

        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verifyZeroInteractions(onMsgConsumer);
        verify(acknowledgment, times(1)).acknowledge();
        verifyZeroInteractions(otherAppService);
        verify(service, times(1)).read(Mockito.eq(data.topic()),
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
                "uri", "dto-object", "error-message")).when(service).read(
                        Mockito.anyString(), Mockito.anyInt(),
                        Mockito.anyLong(), Mockito.any());

        MqiGenericReadMessageDto<String> expectedReadBody =
                new MqiGenericReadMessageDto<String>("group-name", "pod-name",
                        false, data.value());

        listener.onMessage(data, acknowledgment, onMsgConsumer);
        
        verify(appStatus, times(1)).setError();
        verifyNoMoreInteractions(appStatus);
        verifyZeroInteractions(onMsgConsumer);
        verifyZeroInteractions(otherAppService);
        verifyZeroInteractions(genericConsumer);
        verify(service, times(1)).read(Mockito.eq(data.topic()),
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
        MqiLightMessageDto msgLight = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MqiStateMessageEnum.SEND);
        msgLight.setSendingPod("pod-name");

        MqiGenericReadMessageDto<String> expectedReadBody =
                new MqiGenericReadMessageDto<String>("group-name", "pod-name",
                        false, data.value());

        doReturn(msgLight).when(service).read(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyLong(), Mockito.any());

        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verify(service, times(1)).read(Mockito.eq(data.topic()),
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
        MqiLightMessageDto msgLight = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MqiStateMessageEnum.SEND);
        msgLight.setSendingPod("other-name");

        MqiGenericReadMessageDto<String> expectedReadBody =
                new MqiGenericReadMessageDto<String>("group-name", "pod-name",
                        false, data.value());

        doReturn(msgLight).when(service).read(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyLong(), Mockito.any());
        doReturn(true).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.anyLong());

        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verify(service, times(1)).read(Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
        verify(appStatus, times(1)).resetError();
        verify(acknowledgment, times(1)).acknowledge();
        verifyNoMoreInteractions(appStatus);
        verifyZeroInteractions(genericConsumer);
        verifyZeroInteractions(onMsgConsumer);
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(1234L));
    }

    /**
     * Test onMessage when the first assignment fails
     * 
     * @throws AbstractCodedException
     */
    @Test
    public void testOnMessageWhenProcessingButNoResponse()
            throws AbstractCodedException {
        MqiLightMessageDto msgLight = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLight.setState(MqiStateMessageEnum.SEND);
        msgLight.setSendingPod("other-name");

        MqiLightMessageDto msgLightForceRead = new MqiLightMessageDto(
                ProductCategory.AUXILIARY_FILES, 1234, "topic", 1, 111);
        msgLightForceRead.setState(MqiStateMessageEnum.READ);
        msgLightForceRead.setReadingPod("pod-name");

        MqiGenericReadMessageDto<String> expectedReadBody =
                new MqiGenericReadMessageDto<String>("group-name", "pod-name",
                        false, data.value());

        MqiGenericReadMessageDto<String> expectedReadBodyForce =
                new MqiGenericReadMessageDto<String>("group-name", "pod-name",
                        true, data.value());

        doReturn(msgLight, msgLightForceRead).when(service).read(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyLong(), Mockito.any());
        doReturn(false).when(otherAppService).isProcessing(Mockito.anyString(),
                Mockito.anyLong());

        listener.onMessage(data, acknowledgment, onMsgConsumer);

        verify(service, times(1)).read(Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBody));
        verify(service, times(1)).read(Mockito.eq(data.topic()),
                Mockito.eq(data.partition()), Mockito.eq(data.offset()),
                Mockito.eq(expectedReadBodyForce));
        verifyNoMoreInteractions(service);
        verify(appStatus, times(1)).resetError();
        verifyNoMoreInteractions(appStatus);
        verify(acknowledgment, times(1)).acknowledge();
        verify(genericConsumer, times(1)).pause();
        verifyZeroInteractions(onMsgConsumer);
        verify(otherAppService, times(1)).isProcessing(Mockito.eq("other-name"),
                Mockito.eq(1234L));
    }

}
