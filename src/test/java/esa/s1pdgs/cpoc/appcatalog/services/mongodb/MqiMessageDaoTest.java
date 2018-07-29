/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.services.mongodb;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import esa.s1pdgs.cpoc.appcatalog.model.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Test class for MongoDB services class
 *
 * @author Viveris Technologies
 */
public class MqiMessageDaoTest {

    @Mock
    private MongoTemplate mongoClient;

    @InjectMocks
    private MqiMessageDao mongoDBDAO;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSearchByTopicPartitionOffsetGroup() {
        List<MqiMessage> response = new ArrayList<>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 0, null));
        doReturn(response).when(mongoClient).find(Mockito.any(Query.class),
                Mockito.any());

        List<MqiMessage> result = mongoDBDAO
                .searchByTopicPartitionOffsetGroup("topic", 1, 5, "group");

        MqiMessage expectedResult =
                new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1, 5,
                        "group", MqiStateMessageEnum.READ, "readingPod", null,
                        "sendingPod", null, null, 0, null);

        assertEquals(expectedResult, result.get(0));
        verify(mongoClient, times(1)).find(Mockito.any(Query.class),
                Mockito.eq(MqiMessage.class));
        // TODO, check query
    }

    @Test
    public void testSearchByTopicPartitionGroup() {
        List<MqiMessage> response = new ArrayList<>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 0, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                8, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 0, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                18, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 0, null));
        doReturn(response).when(mongoClient).find(Mockito.any(Query.class),
                Mockito.any());

        Set<MqiStateMessageEnum> ackStates = new HashSet<>();
        ackStates.add(MqiStateMessageEnum.ACK_KO);
        ackStates.add(MqiStateMessageEnum.ACK_OK);
        ackStates.add(MqiStateMessageEnum.ACK_WARN);
        List<MqiMessage> result = mongoDBDAO
                .searchByTopicPartitionGroup("topic", 1, "group", ackStates);

        List<MqiMessage> expectedResult = new ArrayList<>();
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MqiStateMessageEnum.READ, "readingPod",
                null, "sendingPod", null, null, 0, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 8, "group", MqiStateMessageEnum.READ, "readingPod",
                null, "sendingPod", null, null, 0, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 18, "group", MqiStateMessageEnum.READ, "readingPod",
                null, "sendingPod", null, null, 0, null));

        assertEquals(expectedResult, result);
        verify(mongoClient, times(1)).find(Mockito.any(Query.class),
                Mockito.eq(MqiMessage.class));
        // TODO, check query

    }

    @Test
    public void testSearchByPodStateCategory() {
        List<MqiMessage> response = new ArrayList<>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 0, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                8, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 0, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                18, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 0, null));
        doReturn(response).when(mongoClient).find(Mockito.any(Query.class),
                Mockito.any());

        Set<MqiStateMessageEnum> ackStates = new HashSet<>();
        ackStates.add(MqiStateMessageEnum.ACK_KO);
        ackStates.add(MqiStateMessageEnum.ACK_OK);
        ackStates.add(MqiStateMessageEnum.ACK_WARN);
        List<MqiMessage> result = mongoDBDAO.searchByPodStateCategory(
                "readingPod", ProductCategory.AUXILIARY_FILES, ackStates);

        List<MqiMessage> expectedResult = new ArrayList<>();
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MqiStateMessageEnum.READ, "readingPod",
                null, "sendingPod", null, null, 0, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 8, "group", MqiStateMessageEnum.READ, "readingPod",
                null, "sendingPod", null, null, 0, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 18, "group", MqiStateMessageEnum.READ, "readingPod",
                null, "sendingPod", null, null, 0, null));

        assertEquals(expectedResult, result);
        verify(mongoClient, times(1)).find(Mockito.any(Query.class),
                Mockito.eq(MqiMessage.class));
        // TODO, check query

    }

    @Test
    public void testSearchByID() {
        List<MqiMessage> response = new ArrayList<>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 0, null));
        doReturn(response).when(mongoClient).find(Mockito.any(Query.class),
                Mockito.any());

        List<MqiMessage> result = mongoDBDAO.searchByID(1);

        MqiMessage expectedResult =
                new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1, 5,
                        "group", MqiStateMessageEnum.READ, "readingPod", null,
                        "sendingPod", null, null, 0, null);

        assertEquals(expectedResult, result.get(0));
        verify(mongoClient, times(1)).find(Mockito.any(Query.class),
                Mockito.eq(MqiMessage.class));
        // TODO, check query

    }

    @Test
    public void testCountReadingMsg() {
        List<MqiMessage> response = new ArrayList<>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 0, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic2",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", null,
                "sendingPod", null, null, 0, null));
        doReturn(response).when(mongoClient).find(Mockito.any(Query.class),
                Mockito.any());

        int result = mongoDBDAO.countReadingMessages("pod-name", "topic");

        assertEquals(2, result);
        verify(mongoClient, times(1)).find(Mockito.any(Query.class),
                Mockito.eq(MqiMessage.class));
        // TODO, check query

    }

    @Test
    public void testInsertMqiMessage() {
        MqiMessage messageToInsert =
                new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1, 5,
                        "group", MqiStateMessageEnum.READ, "readingPod", null,
                        "sendingPod", null, null, 0, null);
        doNothing().when(mongoClient).insert(Mockito.any(MqiMessage.class));

        mongoDBDAO.insert(messageToInsert);

        verify(mongoClient, times(1)).insert(Mockito.eq(messageToInsert));

    }

    @Test
    public void testUpdateByID() {
        doReturn(null).when(mongoClient).updateFirst(Mockito.any(Query.class),
                Mockito.any(Update.class), Mockito.any(Class.class));

        mongoDBDAO.updateByID(1, new HashMap<>());

        verify(mongoClient, times(1)).updateFirst(Mockito.any(Query.class),
                Mockito.any(Update.class), Mockito.eq(MqiMessage.class));

    }
}
