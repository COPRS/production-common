package esa.s1pdgs.cpoc.reqrepo.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.reqrepo.config.TestConfig;

@RunWith(SpringRunner.class)
@DataMongoTest
@Import(TestConfig.class)
@TestPropertySource(locations="classpath:default-mongodb-port.properties")
public class TestFailedProcessingRepo {	
    @Autowired
    private MongoOperations ops;

    @Autowired
    private FailedProcessingRepo uut;
    
    @Test
    public final void testFindById_OnExistingId_ShallReturnObject()
    {
    	ops.insert(newFailedProcessing("1"));    	
    	ops.insert(newFailedProcessing("2"));   
    	
    	final Optional<FailedProcessing> actual = uut.findById("1");
    	assertEquals("1", actual.get().getId());
    }
    
    @Test
    public final void testDeleteById_OnExistingId_ShallDeleteObject()
    {
    	ops.insert(newFailedProcessing("3"));    	
    	ops.insert(newFailedProcessing("4"));    
    	uut.deleteById("4");
    	final Optional<FailedProcessing> actual = uut.findById("4");
    	assertTrue(actual.isEmpty());
    }
        
    
    private final FailedProcessing newFailedProcessing(final String id)
    {
    	final FailedProcessing proc = new FailedProcessing();
    	proc.setId(id);
    	return proc;
    }    
}
