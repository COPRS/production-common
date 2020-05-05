package esa.s1pdgs.cpoc.reqrepo.repo;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.common.MqiMessage;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.reqrepo.service.RequestRepository;

@Ignore
@RunWith(SpringRunner.class)
@DataMongoTest
@EnableMongoRepositories(basePackageClasses = MqiMessageRepo.class)
@ActiveProfiles("test")
public class TestMqiMessageRepo {	
    @Autowired
    private MongoOperations ops;

    @Autowired
    private MqiMessageRepo uut;

	{
		if ("http://proxy.net.werum:8080/".equals(System.getenv("http_proxy"))) {
			System.setProperty("http.proxyHost", "proxy.net.werum");
			System.setProperty("http.proxyPort", "8080");
			System.setProperty("https.proxyHost", "proxy.net.werum");
			System.setProperty("https.proxyPort", "8080");
		}

	}
	
    @Test
    public final void testFindByIdentifier_OnExistingId_ShallReturnObject() throws Exception
    {
    	ops.insert(newMqiMessage(1));    	    	
    	final MqiMessage actual = uut.findById(1);
    	assertEquals(1L, actual.getId());
    	uut.deleteAll();
    }
    
    @Test
    public final void testFindByStateInAndTopicInOrderByCreationDate_OnNoFilter_ShallReturnAll() throws Exception
    {
    	final List<MqiMessage> expected = Arrays.asList(
    			newMqiMessage(4),
    			newMqiMessage(2),
    			newMqiMessage(5),
    			newMqiMessage(3)
    	); 
    	for (final MqiMessage mess : expected)    	{
    		ops.insert(mess);  
    	}    	
    	final List<MqiMessage> actual = uut.findByStateInAndTopicInOrderByCreationDate(
    			RequestRepository.PROCESSING_STATE_LIST, 
    			RequestRepository.PROCESSING_TYPES_LIST
    	);
    	assertEquals(4, actual.size()); 

    	// assert correct order of returned objects
    	assertEquals(4, actual.get(0).getId()); 
    	assertEquals(2, actual.get(1).getId()); 
    	assertEquals(5, actual.get(2).getId()); 
    	assertEquals(3, actual.get(3).getId()); 
    	
    	uut.deleteAll();
    }
    
    @Test
    public final void testFindByStateInAndTopicInOrderByCreationDate_OnMatchingStateFilter_ShallReturnAll() throws Exception
    {
    	final List<MqiMessage> expected = Arrays.asList(
    			newMqiMessage(4),
    			newMqiMessage(2),
    			newMqiMessage(5),
    			newMqiMessage(3)
    	); 
    	for (final MqiMessage mess : expected)    	{
    		ops.insert(mess);  
    	}    	
    	final List<MqiMessage> actual = uut.findByStateInAndTopicInOrderByCreationDate(
    			Collections.singletonList(MessageState.READ), 
    			RequestRepository.PROCESSING_TYPES_LIST
    	); 
    	assertEquals(4, actual.size()); 
    	
    	// assert correct order of returned objects
    	assertEquals(4, actual.get(0).getId()); 
    	assertEquals(2, actual.get(1).getId()); 
    	assertEquals(5, actual.get(2).getId()); 
    	assertEquals(3, actual.get(3).getId()); 
    	
    	uut.deleteAll();
    }
    
    @Test
    public final void testFindByStateInAndTopicInOrderByCreationDate_OnNonMatchingStateFilter_ShallReturnNone() throws Exception
    {
    	final List<MqiMessage> expected = Arrays.asList(
    			newMqiMessage(4),
    			newMqiMessage(2),
    			newMqiMessage(5),
    			newMqiMessage(3)
    	); 
    	for (final MqiMessage mess : expected)    	{
    		ops.insert(mess);  
    	}    	
    	final List<MqiMessage> actual = uut.findByStateInAndTopicInOrderByCreationDate(
    			Collections.singletonList(MessageState.SEND), 
    			RequestRepository.PROCESSING_TYPES_LIST
    	); 
    	assertEquals(0, actual.size());     	
    	uut.deleteAll();
    }
    
    @Test
    public final void testFindByStateInAndTopicInOrderByCreationDate_OnNonMatchingTopicFilter_ShallReturnNone() throws Exception
    {
    	ops.insert(newMqiMessage(6));  
    	ops.insert(newMqiMessage(7));    	
    	ops.insert(newMqiMessage(5));    
    	ops.insert(newMqiMessage(9));
    	
    	final List<MqiMessage> actual = uut.findByStateInAndTopicInOrderByCreationDate(    			
    			RequestRepository.PROCESSING_STATE_LIST,
    			Collections.singletonList("t-pdgs-session-file-ingestion-events")
    	);
    	assertEquals(0, actual.size());     	
    	uut.deleteAll();
    }
    
    @Test
    public final void testFindByStateInAndTopicInOrderByCreationDate_OnPartiallyMatchingTopicFilter_ShallReturnSubset() throws Exception
    {
       	final List<MqiMessage> expected = Arrays.asList(
    			newMqiMessage(4),
    			newMqiMessage(2),
    			newMqiMessage(5),
    			newMqiMessage(3)
    	);        	
       	expected.get(1).setTopic("t-pdgs-session-file-ingestion-events");
     	expected.get(3).setTopic("t-pdgs-session-file-ingestion-events");
    	for (final MqiMessage mess : expected)    	{
    		ops.insert(mess);  
    	}   
    	
    	final List<MqiMessage> actual = uut.findByStateInAndTopicInOrderByCreationDate(    			
    			RequestRepository.PROCESSING_STATE_LIST,
    			Collections.singletonList("t-pdgs-session-file-ingestion-events")
    	);
    	assertEquals(2, actual.size());   
    	
    	assertEquals(2, actual.get(0).getId()); 
    	assertEquals(3, actual.get(1).getId()); 
    	
    	uut.deleteAll();
    }


    @Test
    public final void testFindByStateInAndTopicIn_OnNoFilter_ShallReturnAll() throws Exception
    {
    	final List<MqiMessage> expected = Arrays.asList(
    			newMqiMessage(4),
    			newMqiMessage(2),
    			newMqiMessage(5),
    			newMqiMessage(3)
    	); 
    	for (final MqiMessage mess : expected) {
    		ops.insert(mess);  
    	}    	
    	final List<MqiMessage> actual = uut.findByStateInAndTopicIn(
    			RequestRepository.PROCESSING_STATE_LIST, 
    			RequestRepository.PROCESSING_TYPES_LIST,
    			PageRequest.of(0, Integer.MAX_VALUE)
    	).getContent();
    	assertEquals(4, actual.size());
    	uut.deleteAll();
    }
    
    @Test
    public final void testFindByStateInAndTopicIn_OnNoFilterAndPagesizeTwo_ShallReturnTwoResults() throws Exception
    {
    	final List<MqiMessage> expected = Arrays.asList(
    			newMqiMessage(4),
    			newMqiMessage(2),
    			newMqiMessage(5),
    			newMqiMessage(3)
    	); 
    	for (final MqiMessage mess : expected)    	{
    		ops.insert(mess);  
    	}    	
    	final List<MqiMessage> actual = uut.findByStateInAndTopicIn(
    			Collections.singletonList(MessageState.READ), 
    			RequestRepository.PROCESSING_TYPES_LIST,
    			PageRequest.of(0, 2)
    	).getContent();    
    	assertEquals(2, actual.size()); 
    	
    	// assert correct order of returned objects
    	assertEquals(4, actual.get(0).getId()); 
    	assertEquals(2, actual.get(1).getId());     	
    	uut.deleteAll();
    }
    
    @Test
    public final void testFindByStateInAndTopicIn_OnPartiallMatchingStateFilterAndPagingOrderByCreationDate_ShallReturnSubset() throws Exception
    {
       	final List<MqiMessage> expected = Arrays.asList(
    			newMqiMessage(4),
    			newMqiMessage(2),
    			newMqiMessage(5),
    			newMqiMessage(3)
    	);        	
       	expected.get(1).setState(MessageState.ACK_KO);
     	expected.get(3).setState(MessageState.ACK_KO);
    	for (final MqiMessage mess : expected)    	{
    		ops.insert(mess);  
    	}    	
    	final List<MqiMessage> actual = uut.findByStateInAndTopicIn(
    			Collections.singletonList(MessageState.ACK_KO), 
    			RequestRepository.PROCESSING_TYPES_LIST,
    			PageRequest.of(0, 2, Sort.by(Direction.ASC,"creationDate"))
    	).getContent();    
    	
    	assertEquals(2, actual.size()); 
    	System.err.println(actual);
    	// assert correct order of returned objects
    	assertEquals(2, actual.get(0).getId()); 
    	assertEquals(3, actual.get(1).getId()); 
    	
    	uut.deleteAll();
    }
    
    @Test
    public final void testCountByStateInAndTopicIn_NoConstraint_ShallReturnAll() throws Exception
    {
    	ops.insert(newMqiMessage(1));    
    	ops.insert(newMqiMessage(2));  
    	ops.insert(newMqiMessage(3));  
    	assertEquals(3L, uut.countByStateInAndTopicIn(RequestRepository.PROCESSING_STATE_LIST, RequestRepository.PROCESSING_TYPES_LIST));
    	uut.deleteAll();
    	assertEquals(0L, uut.countByStateInAndTopicIn(RequestRepository.PROCESSING_STATE_LIST, RequestRepository.PROCESSING_TYPES_LIST));
    }
    
    @Test
    public final void testCountByStateInAndTopicIn_ConstraintOnExistingType_ShallReturnAll() throws Exception
    {
    	ops.insert(newMqiMessage(1));    
    	ops.insert(newMqiMessage(2));  
    	ops.insert(newMqiMessage(3));  
    	assertEquals(3L, uut.countByStateInAndTopicIn(RequestRepository.PROCESSING_STATE_LIST, Collections.singletonList("t-pdgs-aio-l0-segment-production-events")));
    	uut.deleteAll();
    }
    
    @Test
    public final void testCountByStateInAndTopicIn_ConstraintOnNonExistingType_ShallReturnNone() throws Exception
    {
    	ops.insert(newMqiMessage(1));    
    	ops.insert(newMqiMessage(2));  
    	ops.insert(newMqiMessage(3));  
    	assertEquals(0L, uut.countByStateInAndTopicIn(RequestRepository.PROCESSING_STATE_LIST, Collections.singletonList("foo")));
    	uut.deleteAll();
    }
    
    
    private final MqiMessage newMqiMessage(final long id) throws InterruptedException
    {
    	final MqiMessage proc = new MqiMessage();
    	proc.setId(id);
    	proc.setCreationDate(new Date());
    	proc.setState(MessageState.READ);
    	proc.setTopic("t-pdgs-aio-l0-segment-production-events");
    	return proc;
    }    
}
