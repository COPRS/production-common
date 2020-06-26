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
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDao;
import esa.s1pdgs.cpoc.appcatalog.server.service.AppDataJobService;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;
import esa.s1pdgs.cpoc.common.filter.FilterOperator;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

/**
 *
 *
 * @author Viveris Technologies
 */
public class AppDataJobServiceTest {
    
    @Mock
    private AppDataJobRepository appDataJobDao;

    @Mock
    private SequenceDao sequenceDao;
    
    private AppDataJobService appDataJobService;
    
    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        this.appDataJobService = new AppDataJobService(appDataJobDao, sequenceDao);
    }
    
    @Test
    public void searchTest() {
        final List<FilterCriterion> filterCriterion = new ArrayList<>();
        filterCriterion.add(new FilterCriterion("key-filter", 1, FilterOperator.LTE));
        final Sort sort = new Sort(Direction.ASC, "valueFilter");
        
        doReturn(new ArrayList<AppDataJob>())
        	.when(appDataJobDao)
        	.search(Mockito.any(), Mockito.any());
        
        appDataJobService.search(filterCriterion, sort);
        
        verify(appDataJobDao, times(1))
        	.search(Mockito.eq(filterCriterion), Mockito.eq(sort));
    }
    
    @Test
    public void getJobTest() throws AppCatalogJobNotFoundException {
        doReturn(Optional.of(new AppDataJob())).when(appDataJobDao).findById(Mockito.any());
        appDataJobService.getJob(123L);
        verify(appDataJobDao, times(1)).findById(Mockito.eq(123L));
    }
    
    @Test(expected = AppCatalogJobNotFoundException.class)
    public void getJobExceptionTest() throws AppCatalogJobNotFoundException {
        doReturn(Optional.empty()).when(appDataJobDao).findById(Mockito.any());
        appDataJobService.getJob(123L);
    }
    
    @Test
    public void newJobTest() {        
        final AppDataJob newJob = new AppDataJob();
        
        doReturn(12L).when(sequenceDao).getNextSequenceId(Mockito.any());
        doReturn(newJob).when(appDataJobDao).save(Mockito.any());
        
        appDataJobService.newJob(newJob);
        
        verify(sequenceDao, times(1)).getNextSequenceId(Mockito.eq("appDataJob"));
        verify(appDataJobDao, times(1)).save(Mockito.eq(newJob));
    }
    
    @Test
    public void deleteJobTest() {
        doNothing().when(appDataJobDao).deleteById(Mockito.any());
        appDataJobService.deleteJob(123L);
        verify(appDataJobDao, times(1)).deleteById(Mockito.eq(123L));
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
        	.when(appDataJobDao)
        	.findById(Mockito.any());
        doReturn(obj)
        	.when(appDataJobDao)
        	.save(Mockito.any());
        
        appDataJobService.updateJob(obj);
        
        verify(appDataJobDao, times(1)).findById(Mockito.eq(123L));
        verify(appDataJobDao, times(1)).save(Mockito.eq(obj));
    }
}
