/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDao;
import esa.s1pdgs.cpoc.appcatalog.server.service.AppDataJobService;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 *
 *
 * @author Viveris Technologies
 */
public class AppDataJobServiceTest {
    
    @Mock
    private AppDataJobRepository appDataJobRepository;

    @Mock
    private SequenceDao sequenceDao;
    
    private AppDataJobService appDataJobService;
    
    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        this.appDataJobService = new AppDataJobService(appDataJobRepository, sequenceDao);
    }
    
    @Test
    public void findByMessagesIdTest() {
    	doReturn(new ArrayList<AppDataJob>()).when(appDataJobRepository)
    		.findByMessagesId(Mockito.anyLong());
    	appDataJobService.findByMessagesId(123L);
    	verify(appDataJobRepository, times(1)).findByMessagesId(Mockito.eq(123L));
    }

    @Test
    public void findByProductSessionIdTest() {
    	doReturn(new ArrayList<AppDataJob>()).when(appDataJobRepository)
    		.findByProductSessionId(Mockito.anyString());
    	appDataJobService.findByProductSessionId("sessionId");
    	verify(appDataJobRepository, times(1)).findByProductSessionId(Mockito.eq("sessionId"));
    }
    
    @Test
    public void findByProductDataTakeIdTest() {
    	doReturn(new ArrayList<AppDataJob>()).when(appDataJobRepository)
			.findByProductDataTakeId(Mockito.anyString());
    	appDataJobService.findByProductDataTakeId("dataTakeId");
    	verify(appDataJobRepository, times(1)).findByProductDataTakeId(Mockito.eq("dataTakeId"));
    }
    
    @Test
    public void findJobInStateGeneratingTest() {
    	doReturn(new ArrayList<AppDataJob>()).when(appDataJobRepository)
			.findJobInStateGenerating(Mockito.anyString());
    	appDataJobService.findJobInStateGenerating("taskTable");
    	verify(appDataJobRepository, times(1)).findJobInStateGenerating(Mockito.eq("taskTable"));
    }
    
    @Test
    public void getJobTest() throws AppCatalogJobNotFoundException {
        doReturn(Optional.of(new AppDataJob())).when(appDataJobRepository).findById(Mockito.any());
        appDataJobService.getJob(123L);
        verify(appDataJobRepository, times(1)).findById(Mockito.eq(123L));
    }
    
    @Test(expected = AppCatalogJobNotFoundException.class)
    public void getJobExceptionTest() throws AppCatalogJobNotFoundException {
        doReturn(Optional.empty()).when(appDataJobRepository).findById(Mockito.any());
        appDataJobService.getJob(123L);
    }
    
    @Test
    public void newJobTest() {        
        final AppDataJob newJob = new AppDataJob();
        
        doReturn(12L).when(sequenceDao).getNextSequenceId(Mockito.any());
        doReturn(newJob).when(appDataJobRepository).save(Mockito.any());
        
        appDataJobService.newJob(newJob);
        
        verify(sequenceDao, times(1)).getNextSequenceId(Mockito.eq("appDataJob"));
        verify(appDataJobRepository, times(1)).save(Mockito.eq(newJob));
    }
    
    @Test
    public void deleteJobTest() {
        doNothing().when(appDataJobRepository).deleteById(Mockito.any());
        appDataJobService.deleteJob(123L);
        verify(appDataJobRepository, times(1)).deleteById(Mockito.eq(123L));
    }
    
    @Test
    public void patchJobTest() throws AppCatalogJobNotFoundException {
        final AppDataJob obj = new AppDataJob();
        final AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        final AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("tast-table-1");
        final GenericMessageDto<CatalogEvent> message1 = new GenericMessageDto<CatalogEvent>(1, "topic1", null);
        final GenericMessageDto<CatalogEvent> message2 = new GenericMessageDto<CatalogEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.DISPATCHING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGeneration(gen1);
        
        doReturn(Optional.of(obj))
        	.when(appDataJobRepository)
        	.findById(Mockito.any());
        doReturn(obj)
        	.when(appDataJobRepository)
        	.save(Mockito.any());
        
        appDataJobService.updateJob(obj);
        
        verify(appDataJobRepository, times(1)).findById(Mockito.eq(123L));
        verify(appDataJobRepository, times(1)).save(Mockito.eq(obj));
    }
}
