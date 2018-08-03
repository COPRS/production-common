package esa.s1pdgs.cpoc.appcatalog.server.controllers.rest;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.io.IOException;
import java.util.ArrayList;
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

import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiSendMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.appcatalog.server.RestControllerTest;
import esa.s1pdgs.cpoc.appcatalog.server.controllers.rest.MqiEdrsSessionController;
import esa.s1pdgs.cpoc.appcatalog.server.model.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.server.services.mongodb.MqiMessageService;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MqiEdrsSessionControllerTest extends RestControllerTest {

    @Mock
    private MqiMessageService mongoDBServices;

    @Value("${mqi.max-retries}")
    private int maxRetries;

    private MqiEdrsSessionController controller;

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);

        this.controller =
                new MqiEdrsSessionController(mongoDBServices, maxRetries);
        this.initMockMvc(this.controller);
    }

    private static String convertObjectToJsonString(Object dto)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(dto);
    }

    @Test
    public void testReadMessageMqiMessageNoForceSend() throws Exception {
        doNothing().when(mongoDBServices).updateByID(Mockito.anyLong(),
                Mockito.any());
        MqiMessage message = new MqiMessage(ProductCategory.EDRS_SESSIONS,
                "topic", 1, 5, "group", MqiStateMessageEnum.SEND, "readingPod",
                null, "sendingPod", null, null, 2, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        doReturn(response).when(mongoDBServices)
                .searchByTopicPartitionOffsetGroup(Mockito.anyString(),
                        Mockito.anyInt(), Mockito.anyLong(),
                        Mockito.anyString());
        MqiGenericReadMessageDto<LevelReportDto> body =
                new MqiGenericReadMessageDto<LevelReportDto>("group",
                        "readingPod", false, null);
        request(post("/mqi/edrs_sessions/topic/1/5/read")
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
    public void testNextMessageMqiMessage() throws Exception {
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(new MqiMessage(ProductCategory.EDRS_SESSIONS, "topic", 1,
                5, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 2, null));
        response.add(new MqiMessage(ProductCategory.EDRS_SESSIONS, "topic", 1,
                8, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 2, null));
        response.add(new MqiMessage(ProductCategory.EDRS_SESSIONS, "topic", 1,
                18, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 2, null));
        doReturn(response).when(mongoDBServices).searchByPodStateCategory(
                Mockito.anyString(), Mockito.any(ProductCategory.class),
                Mockito.any());
        request(get("/mqi/edrs_sessions/next").param("pod", "readingPod"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByPodStateCategory(
                Mockito.anyString(), Mockito.any(ProductCategory.class),
                Mockito.any());
    }

    @Test
    public void testSendMessageMqiMessageACKOK() throws Exception {
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(new MqiMessage(ProductCategory.EDRS_SESSIONS, "topic", 1,
                5, "group", MqiStateMessageEnum.ACK_OK, "readingPod", null,
                "sendingPod", null, null, 2, null));
        doReturn(response).when(mongoDBServices).searchByID(Mockito.anyLong());
        MqiSendMessageDto body = new MqiSendMessageDto("pod", false);
        request(post("/mqi/edrs_sessions/1/send")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonString(body)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByID(Mockito.anyLong());
    }

    @Test
    public void testAckMessageMqiMessageOK() throws Exception {
        doNothing().when(mongoDBServices).updateByID(Mockito.anyLong(),
                Mockito.any());
        MqiMessage message = new MqiMessage(ProductCategory.EDRS_SESSIONS,
                "topic", 1, 5, "group", MqiStateMessageEnum.READ, "readingPod",
                null, "sendingPod", null, null, 2, null);
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(message);
        doReturn(response).when(mongoDBServices).searchByID(Mockito.anyLong());
        request(post("/mqi/edrs_sessions/1/ack")
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
    public void testEarliestOffsetMessageMqiMessage() throws Exception {
        List<MqiMessage> response = new ArrayList<MqiMessage>();
        response.add(new MqiMessage(ProductCategory.EDRS_SESSIONS, "topic", 1,
                5, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 2, null));
        doReturn(response).when(mongoDBServices).searchByTopicPartitionGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.any());
        request(get("/mqi/edrs_sessions/topic/1/earliestOffset").param("group",
                "group")).andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(
                                MediaType.APPLICATION_JSON_UTF8_VALUE));
        verify(mongoDBServices, times(1)).searchByTopicPartitionGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.any());
    }

}
