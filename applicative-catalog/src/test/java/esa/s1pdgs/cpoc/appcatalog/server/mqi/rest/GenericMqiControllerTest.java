/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.server.mqi.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.appcatalog.server.RestControllerTest;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.MessageConverter;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.MessageManager;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageService;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.status.AppStatus;

/**
 * Test class for MqiAuxiliaryFile rest controller
 *
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class GenericMqiControllerTest extends RestControllerTest {

    @Mock
    private MqiMessageService mongoDBServices;

    @Value("${mqi.max-retries}")
    private int maxRetries;
    
    @Mock
    private AppStatus appStatus;

    private GenericMessageController<ProductDto> controller;
    
    private MessageManager messManager;
    private MessageConverter messConverter = new MessageConverter();

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        messManager = new MessageManager(mongoDBServices, maxRetries, -3);
        
        this.controller =  new GenericMessageController<ProductDto>(messConverter,messManager,appStatus);
        this.initMockMvc(this.controller);
    }

    private void mockSearchByTopicPartitionOffsetGroup(
            List<MqiMessage> message) {
        doReturn(message).when(mongoDBServices)
                .searchByTopicPartitionOffsetGroup(Mockito.anyString(),
                        Mockito.anyInt(), Mockito.anyLong(),
                        Mockito.anyString());
    }

    private void mockSearchByPodStateCategory(List<MqiMessage> message) {
        doReturn(message).when(mongoDBServices).searchByPodStateCategory(
                Mockito.anyString(), Mockito.any(ProductCategory.class),
                Mockito.any());
    }

    private void mockSearchByTopicPartitionGroup(List<MqiMessage> message) {
        doReturn(message).when(mongoDBServices).searchByTopicPartitionGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.any());
    }

    private void mockSearchByID(List<MqiMessage> message) {
        doReturn(message).when(mongoDBServices).searchByID(Mockito.anyLong());
    }

    private static String convertObjectToJsonString(Object dto)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(dto);
    }

    @Test
    public void testReadMessageMqiMessageDontExists() throws Exception {
        doNothing().when(mongoDBServices)
                .insertMqiMessage(Mockito.any(MqiMessage.class));
        this.mockSearchByTopicPartitionOffsetGroup(new ArrayList<MqiMessage>());
        AppCatReadMessageDto<ProductDto> body =
                new AppCatReadMessageDto<ProductDto>("group",
                        "readingPod", false, null);
        request(post("/mqi/auxiliary_files/topic/1/5/read")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByTopicPartitionOffsetGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(),
                Mockito.anyString());
        verify(mongoDBServices, times(1))
                .insertMqiMessage(Mockito.any(MqiMessage.class));
    }

    @Test
    public void testReadMessageMqiMessageStateACK() throws Exception {
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.ACK_OK,
                "readingPod", null, "sendingPod", null, null, 0, null, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        this.mockSearchByTopicPartitionOffsetGroup(response);
        AppCatReadMessageDto<ProductDto> body =
                new AppCatReadMessageDto<ProductDto>("group",
                        "readingPod", false, null);
        request(post("/mqi/auxiliary_files/topic/1/5/read")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByTopicPartitionOffsetGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(),
                Mockito.anyString());
        verify(mongoDBServices, never())
                .insertMqiMessage(Mockito.any(MqiMessage.class));
    }

    @Test
    public void testReadMessageMqiMessageStateSendNotForce() throws Exception {
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.SEND, "readingPod",
                null, "sendingPod", null, null, 0, null, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        this.mockSearchByTopicPartitionOffsetGroup(response);

        doNothing().when(mongoDBServices).updateByID(Mockito.anyLong(),
                Mockito.any());

        AppCatReadMessageDto<ProductDto> body =
                new AppCatReadMessageDto<ProductDto>("group",
                        "readingPod2", false, null);
        request(post("/mqi/auxiliary_files/topic/1/5/read")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .andExpect(jsonPath("$.state", is("SEND")))
                        .andExpect(jsonPath("$.readingPod", is("readingPod2")))
                        .andExpect(jsonPath("$.sendingPod", is("sendingPod")));

        verify(mongoDBServices, times(1)).searchByTopicPartitionOffsetGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(),
                Mockito.anyString());
        verify(mongoDBServices, never())
                .insertMqiMessage(Mockito.any(MqiMessage.class));
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(), Mockito.any());
    }

    @Test
    public void testReadMessageMqiMessageStateSendForce() throws Exception {
        doNothing().when(mongoDBServices).updateByID(Mockito.anyLong(),
                Mockito.any());
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.SEND, "readingPod",
                null, "sendingPod", null, null, 0, null, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        this.mockSearchByTopicPartitionOffsetGroup(response);
        AppCatReadMessageDto<ProductDto> body =
                new AppCatReadMessageDto<ProductDto>("group",
                        "readingPod2", true, null);
        request(post("/mqi/auxiliary_files/topic/1/5/read")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .andExpect(jsonPath("$.state", is("READ")))
                        .andExpect(jsonPath("$.readingPod", is("readingPod2")));
        verify(mongoDBServices, times(1)).searchByTopicPartitionOffsetGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(),
                Mockito.anyString());
        verify(mongoDBServices, never())
                .insertMqiMessage(Mockito.any(MqiMessage.class));
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(),
                Mockito.any());
    }

    @Test
    public void testReadMessageMqiMessageStateSendForceRetryMax() throws Exception {
        doNothing().when(mongoDBServices).updateByID(Mockito.anyLong(),
                Mockito.any());
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.SEND, "readingPod",
                null, "sendingPod", null, null, 2, null, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        this.mockSearchByTopicPartitionOffsetGroup(response);
        AppCatReadMessageDto<ProductDto> body =
                new AppCatReadMessageDto<ProductDto>("group",
                        "readingPod", true, null);
        request(post("/mqi/auxiliary_files/topic/1/5/read")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .andExpect(jsonPath("$.state", is("ACK_KO")));
        verify(mongoDBServices, times(1)).searchByTopicPartitionOffsetGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(),
                Mockito.anyString());
        verify(mongoDBServices, never())
                .insertMqiMessage(Mockito.any(MqiMessage.class));
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(),
                Mockito.any());
    }

    @Test
    public void testReadMessageMqiMessageNoForceRead() throws Exception {
        doNothing().when(mongoDBServices).updateByID(Mockito.anyLong(),
                Mockito.any());
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.READ, "readingPod",
                null, "sendingPod", null, null, 2, null, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        this.mockSearchByTopicPartitionOffsetGroup(response);
        AppCatReadMessageDto<ProductDto> body =
                new AppCatReadMessageDto<ProductDto>("group",
                        "readingPod", false, null);
        request(post("/mqi/auxiliary_files/topic/1/5/read")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByTopicPartitionOffsetGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(),
                Mockito.anyString());
        verify(mongoDBServices, never())
                .insertMqiMessage(Mockito.any(MqiMessage.class));
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(),
                Mockito.any());
    }

    @Test
    public void testReadMessageMqiMessageNoForceSend() throws Exception {
        doNothing().when(mongoDBServices).updateByID(Mockito.anyLong(),
                Mockito.any());
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.SEND, "readingPod",
                null, "sendingPod", null, null, 2, null, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        this.mockSearchByTopicPartitionOffsetGroup(response);
        AppCatReadMessageDto<ProductDto> body =
                new AppCatReadMessageDto<ProductDto>("group",
                        "readingPod", false, null);
        request(post("/mqi/auxiliary_files/topic/1/5/read")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByTopicPartitionOffsetGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(),
                Mockito.anyString());
        verify(mongoDBServices, never())
                .insertMqiMessage(Mockito.any(MqiMessage.class));
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(),
                Mockito.any());
    }

    @Test
    public void testReadMessageWhenexception() throws Exception {
        doThrow(RuntimeException.class).when(mongoDBServices)
                .searchByTopicPartitionOffsetGroup(Mockito.anyString(),
                        Mockito.anyInt(), Mockito.anyLong(),
                        Mockito.anyString());
        AppCatReadMessageDto<ProductDto> body =
                new AppCatReadMessageDto<ProductDto>("group",
                        "readingPod", false, null);
        request(post("/mqi/auxiliary_files/topic/1/5/read")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body))).andExpect(
                        MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void testNextMessageMqiMessageDontExists() throws Exception {
        this.mockSearchByPodStateCategory(new ArrayList<MqiMessage>());
        request(get("/mqi/auxiliary_files/next").param("pod", "readingPod"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByPodStateCategory(
                Mockito.anyString(), Mockito.any(ProductCategory.class),
                Mockito.any());
    }

    @Test
    public void testNextMessageMqiMessage() throws Exception {
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 2, null, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                8, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 2, null, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                18, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 2, null, null));
        this.mockSearchByPodStateCategory(response);
        request(get("/mqi/auxiliary_files/next").param("pod", "readingPod"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByPodStateCategory(
                Mockito.anyString(), Mockito.any(ProductCategory.class),
                Mockito.any());
    }

    @Test
    public void testNextMessageWhenException() throws Exception {
        doThrow(RuntimeException.class).when(mongoDBServices)
                .searchByPodStateCategory(Mockito.anyString(), Mockito.any(),
                        Mockito.any());
        request(get("/mqi/auxiliary_files/next").param("pod", "readingPod"))
                .andExpect(
                        MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void testSendMessageMqiMessageDontExists() throws Exception {
        this.mockSearchByID(new ArrayList<MqiMessage>());
        AppCatSendMessageDto body = new AppCatSendMessageDto("pod", false);
        request(post("/mqi/auxiliary_files/1/send")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body))).andExpect(
                        MockMvcResultMatchers.status().is4xxClientError());
        verify(mongoDBServices, times(1)).searchByID(Mockito.anyLong());
    }

    @Test
    public void testSendMessageMqiMessageACKOK() throws Exception {
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MessageState.ACK_OK, "readingPod", null,
                "sendingPod", null, null, 2, null, null));
        this.mockSearchByID(response);
        AppCatSendMessageDto body = new AppCatSendMessageDto("pod", false);
        request(post("/mqi/auxiliary_files/1/send")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByID(Mockito.anyLong());
    }

    @Test
    public void testSendMessageMqiMessageREAD() throws Exception {
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 0, null, null));
        this.mockSearchByID(response);
        AppCatSendMessageDto body = new AppCatSendMessageDto("pod", false);
        request(post("/mqi/auxiliary_files/1/send")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByID(Mockito.anyLong());
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(),
                Mockito.any());
    }

    @Test
    public void testSendMessageMqiMessageRetry() throws Exception {
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MessageState.SEND, "readingPod", null,
                "sendingPod", null, null, 0, null, null));
        this.mockSearchByID(response);
        AppCatSendMessageDto body = new AppCatSendMessageDto("pod", false);
        request(post("/mqi/auxiliary_files/1/send")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByID(Mockito.anyLong());
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(),
                Mockito.any());
    }

    @Test
    public void testSendMessageMqiMessageRetryMax() throws Exception {
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MessageState.SEND, "readingPod", null,
                "sendingPod", null, null, 2, null, null));
        this.mockSearchByID(response);
        AppCatSendMessageDto body = new AppCatSendMessageDto("pod", false);
        request(post("/mqi/auxiliary_files/1/send")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByID(Mockito.anyLong());
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(),
                Mockito.any());
    }

    @Test
    public void testSendMessagWhenException() throws Exception {
        doThrow(RuntimeException.class).when(mongoDBServices)
                .searchByID(Mockito.anyLong());
        AppCatSendMessageDto body = new AppCatSendMessageDto("pod", false);
        request(post("/mqi/auxiliary_files/1/send")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body))).andExpect(
                        MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void testAckMessageMqiMessageOK() throws Exception {
        doNothing().when(mongoDBServices).updateByID(Mockito.anyLong(),
                Mockito.any());
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.READ, "readingPod",
                null, "sendingPod", null, null, 2, null, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        this.mockSearchByID(response);
        request(post("/mqi/auxiliary_files/1/ack")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(Ack.OK)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByID(Mockito.anyLong());
        verify(mongoDBServices, never())
                .insertMqiMessage(Mockito.any(MqiMessage.class));
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(),
                Mockito.any());
    }

    @Test
    public void testAckMessageMqiMessageError() throws Exception {
        doNothing().when(mongoDBServices).updateByID(Mockito.anyLong(),
                Mockito.any());
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.ACK_KO,
                "readingPod", null, "sendingPod", null, null, 2, null, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        this.mockSearchByID(response);
        request(post("/mqi/auxiliary_files/1/ack")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(Ack.ERROR)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByID(Mockito.anyLong());
        verify(mongoDBServices, never())
                .insertMqiMessage(Mockito.any(MqiMessage.class));
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(),
                Mockito.any());
    }

    @Test
    public void testAckMessageMqiMessageWARN() throws Exception {
        doNothing().when(mongoDBServices).updateByID(Mockito.anyLong(),
                Mockito.any());
        MqiMessage message = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.ACK_WARN,
                "readingPod", null, "sendingPod", null, null, 2, null, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        this.mockSearchByID(response);
        request(post("/mqi/auxiliary_files/1/ack")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(Ack.WARN)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByID(Mockito.anyLong());
        verify(mongoDBServices, never())
                .insertMqiMessage(Mockito.any(MqiMessage.class));
        verify(mongoDBServices, times(1)).updateByID(Mockito.anyLong(),
                Mockito.any());
    }

    @Test
    public void testAckMessageWhenException() throws Exception {
        doThrow(RuntimeException.class).when(mongoDBServices)
                .updateByID(Mockito.anyLong(), Mockito.any());
        request(post("/mqi/auxiliary_files/1/ack")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(Ack.WARN))).andExpect(
                        MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void testEarliestOffsetMessageMqiMessageDontExists()
            throws Exception {
        this.mockSearchByTopicPartitionGroup(new ArrayList<MqiMessage>());
        request(get("/mqi/topic/1/earliestOffset")
                .param("group", "group"))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByTopicPartitionGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.any());
    }

    @Test
    public void testEarliestOffsetMessageMqiMessage() throws Exception {
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 2, null, null));
        this.mockSearchByTopicPartitionGroup(response);
        request(get("/mqi/topic/1/earliestOffset")
                .param("group", "group"))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByTopicPartitionGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.any());
    }

    @Test
    public void testEarliestOffsetWhenException() throws Exception {
        doThrow(RuntimeException.class).when(mongoDBServices)
                .searchByTopicPartitionGroup(Mockito.anyString(),
                        Mockito.anyInt(), Mockito.anyString(), Mockito.any());
        request(get("/mqi/topic/1/earliestOffset")
                .param("group", "group")).andExpect(
                        MockMvcResultMatchers.status().isInternalServerError());
        verify(mongoDBServices, times(1)).searchByTopicPartitionGroup(
                Mockito.eq("topic"), Mockito.eq(1), Mockito.eq("group"),
                Mockito.any());
    }

    @Test
    public void testGetNbMessages() throws Exception {
        doReturn(2).when(mongoDBServices)
                .countReadingMessages(Mockito.anyString(), Mockito.anyString());

        request(get("/mqi/topic/nbReading?pod=pod-name"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().string("2"));

        verify(mongoDBServices, times(1)).countReadingMessages(
                Mockito.eq("pod-name"), Mockito.eq("topic"));
    }

    @Test
    public void testGetNbMessagesWhenException() throws Exception {
        doThrow(RuntimeException.class).when(mongoDBServices)
                .countReadingMessages(Mockito.anyString(), Mockito.anyString());

        request(get("/mqi/topic/nbReading?pod=pod-name"))
                .andExpect(
                        MockMvcResultMatchers.status().isInternalServerError());
        verify(mongoDBServices, times(1)).countReadingMessages(
                Mockito.eq("pod-name"), Mockito.eq("topic"));
    }

    @Test
    public void testGetWhenException() throws Exception {
        doThrow(RuntimeException.class).when(mongoDBServices)
                .searchByID(Mockito.anyLong());

        request(get("/mqi/auxiliary_files/1234")).andExpect(
                MockMvcResultMatchers.status().isInternalServerError());
        verify(mongoDBServices, times(1)).searchByID(Mockito.eq(1234L));
    }

    @Test
    public void testGetWhenNoMEssage() throws Exception {
        doReturn(new ArrayList<>()).when(mongoDBServices)
                .searchByID(Mockito.anyLong());

        request(get("/mqi/auxiliary_files/1234"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
        verify(mongoDBServices, times(1)).searchByID(Mockito.eq(1234L));
    }

    @Test
    public void testGetWhenMEssages() throws Exception {
        MqiMessage message1 = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.ACK_WARN,
                "readingPod", null, "sendingPod", null, null, 2, null, null);
        MqiMessage message2 = new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 6, "group", MessageState.ACK_WARN,
                "readingPod", null, "sendingPod", null, null, 2, null, null);
        doReturn(Arrays.asList(message1, message2)).when(mongoDBServices)
                .searchByID(Mockito.anyLong());

        request(get("/mqi/auxiliary_files/1234"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(mongoDBServices, times(1)).searchByID(Mockito.eq(1234L));
    }

}
