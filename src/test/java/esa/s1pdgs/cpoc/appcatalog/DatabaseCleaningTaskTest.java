package esa.s1pdgs.cpoc.appcatalog;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.DatabaseCleaningTask;
import esa.s1pdgs.cpoc.appcatalog.services.mongodb.MqiMessageDao;


/**
 * Test class for MongoDB clean class
 *
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class DatabaseCleaningTaskTest {
    
    @Mock
    private MqiMessageDao mongoDBDAO;
    
    private int oldms = 86400;
    
    private DatabaseCleaningTask mongoDBClean;
    
    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mongoDBClean = new DatabaseCleaningTask(mongoDBDAO, oldms);
    }
    
    @Test
    public void testClean() {
        doNothing().when(mongoDBDAO).findAllAndRemove(Mockito.any(Query.class));
        mongoDBClean.clean();
        verify(mongoDBDAO, times(1)).findAllAndRemove(Mockito.any(Query.class));
    }

}
