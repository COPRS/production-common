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
public class MongoDBServicesTest {

    @Mock
    private MongoDBDAO mongoDBDAO;
    
    @InjectMocks
    private MongoDBServices mongoDBServices;
    
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
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        doReturn(response).when(mongoDBDAO).find(Mockito.any(Query.class));
        
        List<MqiMessage> result = mongoDBServices.searchByTopicPartitionOffsetGroup("topic", 1, 5, "group");
        
        MqiMessage expectedResult = new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null);
        
        assertEquals(expectedResult, result.get(0));
        verify(mongoDBDAO, times(1)).find(Mockito.any(Query.class));
        
    }
    
    @Test
    public void testSearchByTopicPartitionGroup() {
        List<MqiMessage> response = new ArrayList<>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 8, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 18, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        doReturn(response).when(mongoDBDAO).find(Mockito.any(Query.class));
        
        List<MqiMessage> result = mongoDBServices.searchByTopicPartitionGroup("topic", 1, "group");
        
        
        List<MqiMessage> expectedResult = new ArrayList<>();
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 8, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 18, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        
        assertEquals(expectedResult, result);
        verify(mongoDBDAO, times(1)).find(Mockito.any(Query.class));
        
    }
    
    @Test
    public void testSearchByPodStateCategory() {
        List<MqiMessage> response = new ArrayList<>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 8, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 18, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        doReturn(response).when(mongoDBDAO).find(Mockito.any(Query.class));
        
        Set<MqiStateMessageEnum> ackStates = new HashSet<>();
        ackStates.add(MqiStateMessageEnum.ACK_KO);
        ackStates.add(MqiStateMessageEnum.ACK_OK);
        ackStates.add(MqiStateMessageEnum.ACK_WARN);
        List<MqiMessage> result = mongoDBServices.searchByPodStateCategory("readingPod", 
                ProductCategory.AUXILIARY_FILES, ackStates);
        
        List<MqiMessage> expectedResult = new ArrayList<>();
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 8, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        expectedResult.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 18, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        
        assertEquals(expectedResult, result);
        verify(mongoDBDAO, times(1)).find(Mockito.any(Query.class));
        
    }
    
    @Test
    public void testSearchByID() {
        List<MqiMessage> response = new ArrayList<>();
        response.add(new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null));
        doReturn(response).when(mongoDBDAO).find(Mockito.any(Query.class));
        
        List<MqiMessage> result = mongoDBServices.searchByID(1);
        
        MqiMessage expectedResult = new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null);
        
        assertEquals(expectedResult, result.get(0));
        verify(mongoDBDAO, times(1)).find(Mockito.any(Query.class));
        
    }
    
    @Test
    public void testInsertMqiMessage() {
        MqiMessage messageToInsert = new MqiMessage(ProductCategory.AUXILIARY_FILES, "topic",
                1, 5, "group", MqiStateMessageEnum.READ, "readingPod", null, 
                "sendingPod", null, null, 0, null);
        doNothing().when(mongoDBDAO).insert(Mockito.any(MqiMessage.class));
        
        mongoDBServices.insertMqiMessage(messageToInsert);
        
        verify(mongoDBDAO, times(1)).insert(Mockito.any(MqiMessage.class));
        
    }
    
    @Test
    public void testUpdateByID() {
        doNothing().when(mongoDBDAO).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class));
        
        mongoDBServices.updateByID(1,  new HashMap<>());
        
        verify(mongoDBDAO, times(1)).updateFirst(Mockito.any(Query.class), Mockito.any(Update.class));
        
    }
    
}
