package esa.s1pdgs.cpoc.appcatalog.server.mqi;

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

import esa.s1pdgs.cpoc.appcatalog.server.mqi.db.MqiMessageRepository;


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
    private MqiMessageRepository mqiMessageRepository;
    
    private int oldms = 86400;
    
    private DatabaseCleaningTask mongoDBClean;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mongoDBClean = new DatabaseCleaningTask(mqiMessageRepository, oldms);
    }
    
    @Test
    public void testClean() {
        doNothing().when(mqiMessageRepository).truncateBefore(Mockito.any());
        mongoDBClean.clean();
        verify(mqiMessageRepository, times(1)).truncateBefore(Mockito.any());
    }

}
