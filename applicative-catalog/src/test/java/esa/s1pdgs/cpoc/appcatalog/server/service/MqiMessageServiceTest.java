/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.server.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageDao;
import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageRepository;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDao;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Test class for MongoDB services class
 *
 * @author Viveris Technologies
 */
public class MqiMessageServiceTest {

    @Mock
    private MqiMessageRepository mqiMessageRepository;

    @Mock
    private MqiMessageDao mongoDBDAO;

    @Mock
    private SequenceDao sequenceDao;

    @InjectMocks
    private MqiMessageService mongoDBServices;

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
                5, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 0, null, null));
        doReturn(response).when(mqiMessageRepository).findByTopicAndPartitionAndOffsetAndGroup(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyLong(),
                Mockito.anyString());

        List<MqiMessage> result = mongoDBServices
                .searchByTopicPartitionOffsetGroup("topic", 1, 5, "group");

        MqiMessage expectedResult =
                new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1, 5,
                        "group", MessageState.READ, "readingPod", null,
                        "sendingPod", null, null, 0, null, null);

        assertEquals(expectedResult, result.get(0));
        verify(mqiMessageRepository, times(1)).findByTopicAndPartitionAndOffsetAndGroup(
                Mockito.eq("topic"), Mockito.eq(1), Mockito.eq(5L),
                Mockito.eq("group"));

    }

    @Test
    public void testSearchByTopicPartitionGroup() {
        List<MqiMessage> response = new ArrayList<>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 0, null, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                8, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 0, null, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                18, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 0, null, null));
        doReturn(response).when(mqiMessageRepository).findByTopicAndPartitionAndGroupAndStateNotInOrderByLastReadDateAsc(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(),
                Mockito.any());

        Set<MessageState> ackStates = new HashSet<>();
        ackStates.add(MessageState.ACK_KO);
        ackStates.add(MessageState.ACK_OK);
        ackStates.add(MessageState.ACK_WARN);
        List<MqiMessage> result = mongoDBServices
                .searchByTopicPartitionGroup("topic", 1, "group", ackStates);

        List<MqiMessage> expectedResult = new ArrayList<>();
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.READ, "readingPod",
                null, "sendingPod", null, null, 0, null, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 8, "group", MessageState.READ, "readingPod",
                null, "sendingPod", null, null, 0, null, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 18, "group", MessageState.READ, "readingPod",
                null, "sendingPod", null, null, 0, null, null));

        assertEquals(expectedResult, result);
        verify(mqiMessageRepository, times(1)).findByTopicAndPartitionAndGroupAndStateNotInOrderByLastReadDateAsc(
                Mockito.eq("topic"), Mockito.eq(1), Mockito.eq("group"),
                Mockito.eq(ackStates));

    }

    @Test
    public void testSearchByPodStateCategory() {
        List<MqiMessage> response = new ArrayList<>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 0, null, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                8, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 0, null, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                18, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 0, null, null));
        doReturn(response).when(mqiMessageRepository).findByReadingPodAndCategoryAndStateNotInOrderByCreationDateAsc(
                Mockito.anyString(), Mockito.any(), Mockito.any());

        Set<MessageState> ackStates = new HashSet<>();
        ackStates.add(MessageState.ACK_KO);
        ackStates.add(MessageState.ACK_OK);
        ackStates.add(MessageState.ACK_WARN);
        List<MqiMessage> result = mongoDBServices.searchByPodStateCategory(
                "readingPod", ProductCategory.AUXILIARY_FILES, ackStates);

        List<MqiMessage> expectedResult = new ArrayList<>();
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 5, "group", MessageState.READ, "readingPod",
                null, "sendingPod", null, null, 0, null, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 8, "group", MessageState.READ, "readingPod",
                null, "sendingPod", null, null, 0, null, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES,
                "topic", 1, 18, "group", MessageState.READ, "readingPod",
                null, "sendingPod", null, null, 0, null, null));

        assertEquals(expectedResult, result);
        verify(mqiMessageRepository, times(1)).findByReadingPodAndCategoryAndStateNotInOrderByCreationDateAsc(
                Mockito.eq("readingPod"),
                Mockito.eq(ProductCategory.AUXILIARY_FILES),
                Mockito.eq(ackStates));

    }

    @Test
    public void testSearchByID() {
    	Optional<MqiMessage> optMsg = Optional.of(
        		new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1,
                5, "group", MessageState.READ, "readingPod", null,
                "sendingPod", null, null, 0, null, null));
        doReturn(optMsg).when(mqiMessageRepository).findById(Mockito.anyLong());

        List<MqiMessage> result = mongoDBServices.searchByID(1);

        MqiMessage expectedResult =
                new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1, 5,
                        "group", MessageState.READ, "readingPod", null,
                        "sendingPod", null, null, 0, null, null);

        assertEquals(expectedResult, result.get(0));
        verify(mqiMessageRepository, times(1)).findById(Mockito.eq(1L));
    }

    @Test
    public void testInsertMqiMessage() {
        MqiMessage messageToInsert =
                new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic", 1, 5,
                        "group", MessageState.READ, "readingPod", null,
                        "sendingPod", null, null, 0, null, null);
        doReturn(messageToInsert).when(mqiMessageRepository).insert(Mockito.any(MqiMessage.class));
        doReturn(2L).when(sequenceDao).getNextSequenceId(Mockito.anyString());

        mongoDBServices.insertMqiMessage(messageToInsert);

        verify(mqiMessageRepository, times(1)).save(Mockito.any(MqiMessage.class));
        verify(sequenceDao, times(1))
                .getNextSequenceId(Mockito.eq(MqiMessageService.MQI_MSG_SEQ_KEY));

    }

    @Test
    public void testUpdateByID() {
    	MqiMessage messageToUpdate = new MqiMessage();    	
    	messageToUpdate.setDto("dto-before");
    	messageToUpdate.setLastReadDate(new Date(111L));
    	messageToUpdate.setReadingPod("readingPod-before");
    	messageToUpdate.setCategory(ProductCategory.AUXILIARY_FILES);
    	messageToUpdate.setCreationDate(new Date(222L));
    	messageToUpdate.setGroup("group-before");
    	messageToUpdate.setLastAckDate(new Date(333L));
    	messageToUpdate.setLastSendDate(new Date(444L));
    	messageToUpdate.setNbRetries(1);
    	messageToUpdate.setOffset(2L);
    	messageToUpdate.setPartition(3);
    	messageToUpdate.setSendingPod("sendingPod-before");
    	messageToUpdate.setState(MessageState.READ);
    	messageToUpdate.setTopic("topic-before");
    	
        doReturn(Optional.of(messageToUpdate)).when(mqiMessageRepository).findById(Mockito.eq(1L));
        doReturn(messageToUpdate).when(mqiMessageRepository).save(Mockito.any());

        Map<String,Object> updates = new HashMap<>();
        updates.put("dto", "dto-after");
        updates.put("lastReadDate", new Date(111000L));
        updates.put("readingPod", "readingPod-after");
        updates.put("category", ProductCategory.LEVEL_PRODUCTS);
        updates.put("creationDate", new Date(222000L));
        updates.put("group", "group-after");
        updates.put("lastAckDate", new Date(333000L));
        updates.put("lastSendDate", new Date(444000L));
        updates.put("nbRetries", 10);
        updates.put("offset", 20L);
        updates.put("partition", 30);
        updates.put("sendingPod", "sendingPod-after");
        updates.put("state", MessageState.ACK_OK);
        updates.put("topic", "topic-after");
        
        mongoDBServices.updateByID(1L, updates);

        verify(mqiMessageRepository, times(1)).findById(Mockito.eq(1L));

        assertEquals("dto-after", messageToUpdate.getDto());
        assertEquals(new Date(111000L), messageToUpdate.getLastReadDate());
        assertEquals("readingPod-after", messageToUpdate.getReadingPod());
        assertEquals(ProductCategory.LEVEL_PRODUCTS, messageToUpdate.getCategory());
        assertEquals(new Date(222000L), messageToUpdate.getCreationDate());
        assertEquals("group-after", messageToUpdate.getGroup());
        assertEquals(new Date(333000L), messageToUpdate.getLastAckDate());
        assertEquals(new Date(444000L), messageToUpdate.getLastSendDate());
        assertEquals(10, messageToUpdate.getNbRetries());
        assertEquals(20L, messageToUpdate.getOffset());
        assertEquals(30, messageToUpdate.getPartition());
        assertEquals("sendingPod-after", messageToUpdate.getSendingPod());
        assertEquals(MessageState.ACK_OK, messageToUpdate.getState());
        assertEquals("topic-after", messageToUpdate.getTopic());
        
        verify(mqiMessageRepository, times(1)).save(Mockito.any());

    }
    
    @Test
    public void testCountReading() {
        doReturn(15).when(mqiMessageRepository).countByReadingPodAndTopicAndStateIsRead(Mockito.anyString(), Mockito.anyString());
        
        assertEquals(15, mongoDBServices.countReadingMessages("pod-name", "topic-name"));
        verify(mqiMessageRepository, times(1)).countByReadingPodAndTopicAndStateIsRead(Mockito.eq("pod-name"), Mockito.eq("topic-name"));
    }
}
