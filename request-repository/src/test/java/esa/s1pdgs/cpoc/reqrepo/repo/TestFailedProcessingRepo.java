package esa.s1pdgs.cpoc.reqrepo.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.reqrepo.repo.FailedProcessingRepo;

@RunWith(SpringRunner.class)
@DataMongoTest
@EnableMongoRepositories(basePackageClasses = FailedProcessingRepo.class)
public class TestFailedProcessingRepo {	
    @Autowired
    private MongoOperations ops;

    @Autowired
    private FailedProcessingRepo uut;
    
    // uncomment, if embedded mongo needs to be updated
//	{
//	System.setProperty("http.proxyHost", "proxy.net.werum");
//	System.setProperty("http.proxyPort", "8080");
//	System.setProperty("https.proxyHost", "proxy.net.werum");
//	System.setProperty("https.proxyPort", "8080");
//}
    
    @Test
    public final void testFindById_OnExistingId_ShallReturnObject()
    {
    	ops.insert(newFailedProcessing(1));    	
    	ops.insert(newFailedProcessing(2));   
    	
    	final FailedProcessing actual = uut.findById(1);
    	assertEquals(1L, actual.getId());
    }
    
    @Test
    public final void testDeleteById_OnExistingId_ShallDeleteObject()
    {
    	ops.insert(newFailedProcessing(3));    	
    	ops.insert(newFailedProcessing(4));    
    	uut.deleteById(4);
    	final FailedProcessing actual = uut.findById(4);
    	assertNull(actual);
    }
        
    
    private final FailedProcessing newFailedProcessing(long id)
    {
    	final FailedProcessing proc = new FailedProcessing();
    	proc.setId(id);
    	return proc;
    }    
}
