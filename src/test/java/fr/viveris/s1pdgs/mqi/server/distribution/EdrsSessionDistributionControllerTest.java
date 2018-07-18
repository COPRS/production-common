package fr.viveris.s1pdgs.mqi.server.distribution;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.viveris.s1pdgs.common.EdrsSessionFileType;
import fr.viveris.s1pdgs.common.ProductCategory;
import fr.viveris.s1pdgs.common.ProductFamily;
import fr.viveris.s1pdgs.common.errors.mqi.MqiCategoryNotAvailable;
import fr.viveris.s1pdgs.mqi.model.queue.EdrsSessionDto;
import fr.viveris.s1pdgs.mqi.model.rest.Ack;
import fr.viveris.s1pdgs.mqi.model.rest.AckMessageDto;
import fr.viveris.s1pdgs.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.mqi.model.rest.GenericPublicationMessageDto;
import fr.viveris.s1pdgs.mqi.server.ApplicationProperties;
import fr.viveris.s1pdgs.mqi.server.GenericKafkaUtils;
import fr.viveris.s1pdgs.mqi.server.consumption.MessageConsumptionController;
import fr.viveris.s1pdgs.mqi.server.publication.MessagePublicationController;
import fr.viveris.s1pdgs.mqi.server.test.RestControllerTest;

/**
 * Test the controller EdrsSessionDistributionController
 * 
 * @author Viveris Technologies
 */
public class EdrsSessionDistributionControllerTest extends RestControllerTest {

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
    private GenericMessageDto<EdrsSessionDto> consumedMessage;

    /**
     * The controller to test
     */
    private EdrsSessionDistributionController controller;

    /**
     * Initialization
     * 
     * @throws MqiCategoryNotAvailable
     */
    @Before
    public void init() throws MqiCategoryNotAvailable {
        MockitoAnnotations.initMocks(this);

        EdrsSessionDto dto = new EdrsSessionDto("key-obs", 1,
                EdrsSessionFileType.RAW, "S1", "A");
        consumedMessage =
                new GenericMessageDto<EdrsSessionDto>(123, "input-key", dto);

        doReturn(consumedMessage).when(messages).nextMessage(Mockito.any());

        doReturn(1000).when(properties).getWaitNextMs();

        controller = new EdrsSessionDistributionController(messages,
                publication, properties);

        this.initMockMvc(this.controller);
    }

    /**
     * Test the URI of the next message API
     * 
     * @throws Exception
     */
    @Test
    public void testNextMessageUri() throws Exception {
        request(get("/messages/edrs_sessions/next"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.identifier", is(123)))
                .andExpect(jsonPath("$.inputKey",
                        is(consumedMessage.getInputKey())))
                .andExpect(jsonPath("$.body.objectStorageKey", is("key-obs")));
        verify(messages, times(1))
                .nextMessage(Mockito.eq(ProductCategory.EDRS_SESSIONS));
    }

    /**
     * Test the URI of the ack message API
     * 
     * @throws Exception
     */
    @Test
    public void testAckMessageUri() throws Exception {
        doReturn(true).when(messages).ackMessage(Mockito.any(),
                Mockito.eq(123L), Mockito.any());
        doReturn(false).when(messages).ackMessage(Mockito.any(),
                Mockito.eq(312L), Mockito.any());
        doReturn(true).when(publication).publishError(Mockito.any());

        String dto1 = GenericKafkaUtils.convertObjectToJsonString(
                new AckMessageDto(123, Ack.OK, null));
        String dto2 = GenericKafkaUtils.convertObjectToJsonString(
                new AckMessageDto(321, Ack.ERROR, "Error log"));

        request(post("/messages/edrs_sessions/ack")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(dto1))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().string("true"));
        verify(messages, times(1)).ackMessage(
                Mockito.eq(ProductCategory.EDRS_SESSIONS), Mockito.eq(123L),
                Mockito.eq(Ack.OK));

        request(post("/messages/edrs_sessions/ack")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(dto2))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().string("false"));
        verify(messages, times(1)).ackMessage(
                Mockito.eq(ProductCategory.EDRS_SESSIONS), Mockito.eq(321L),
                Mockito.eq(Ack.ERROR));
        verify(publication, times(1)).publishError(Mockito.eq("Error log"));
        verifyNoMoreInteractions(messages);
    }

    @Test
    public void testPublishMessageUri() throws Exception {
        doNothing().when(publication).publish(Mockito.any(), Mockito.any());
        GenericPublicationMessageDto<EdrsSessionDto> dto =
                new GenericPublicationMessageDto<>(ProductFamily.EDRS_SESSION,
                        new EdrsSessionDto("key-test", 1,
                                EdrsSessionFileType.RAW, "S1", "A"));
        String convertedObj = GenericKafkaUtils.convertObjectToJsonString(dto);
        request(post("/messages/edrs_sessions/publish")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertedObj))
                        .andExpect(MockMvcResultMatchers.status().isOk());
        verify(publication, times(1)).publish(
                Mockito.eq(ProductCategory.EDRS_SESSIONS),
                Mockito.eq(dto.getMessageToPublish()));
    }
}
