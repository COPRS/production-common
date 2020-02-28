/**
 * 
 */
package esa.s1pdgs.cpoc.appcatalog.server.job.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
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
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidTransitionStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationTerminatedException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.appcatalog.server.sequence.db.SequenceDao;
import esa.s1pdgs.cpoc.appcatalog.server.service.AppDataJobService;
import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;
import esa.s1pdgs.cpoc.common.filter.FilterOperator;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
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
        List<FilterCriterion> filterCriterion = new ArrayList<>();
        filterCriterion.add(new FilterCriterion("key-filter", 1, FilterOperator.LTE));
        Sort sort = new Sort(Direction.ASC, "valueFilter");
        
        doReturn(new ArrayList<AppDataJob>()).when(appDataJobDao).search(Mockito.any(), Mockito.any(), Mockito.any());
        this.appDataJobService.search(filterCriterion, ProductCategory.LEVEL_JOBS, sort);
        verify(appDataJobDao, times(1)).search(Mockito.eq(filterCriterion),
                Mockito.eq(ProductCategory.LEVEL_JOBS), Mockito.eq(sort));
    }
    
    @Test
    public void getJobTest() throws AppCatalogJobNotFoundException {
        doReturn(Optional.of(new AppDataJob())).when(appDataJobDao).findById(Mockito.any());
        this.appDataJobService.getJob(123L);
        verify(appDataJobDao, times(1)).findById(Mockito.eq(123L));
    }
    
    @Test(expected = AppCatalogJobNotFoundException.class)
    public void getJobExceptionTest() throws AppCatalogJobNotFoundException {
        doReturn(Optional.empty()).when(appDataJobDao).findById(Mockito.any());
        this.appDataJobService.getJob(123L);
    }
    
    @Test
    public void newJobTest() {        
        AppDataJob newJob = new AppDataJob();
        
        doReturn(12L).when(sequenceDao).getNextSequenceId(Mockito.any());
        doReturn(newJob).when(appDataJobDao).save(Mockito.any());
        
        this.appDataJobService.newJob(newJob);
        
        verify(sequenceDao, times(1)).getNextSequenceId(Mockito.eq("appDataJob"));
        verify(appDataJobDao, times(1)).save(Mockito.eq(newJob));
    }
    
    @Test
    public void deleteJobTest() {
        doNothing().when(appDataJobDao).deleteById(Mockito.any());
        this.appDataJobService.deleteJob(123L);

        verify(appDataJobDao, times(1)).deleteById(Mockito.eq(123L));
    }
    
    @Test
    public void patchJobTest() throws AppCatalogJobNotFoundException {
        AppDataJob obj = new AppDataJob();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("tast-table-1");
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("tast-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("tast-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.DISPATCHING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);
        
        AppDataJob obj2 = new AppDataJob();
        AppDataJobProduct product2 = new AppDataJobProduct();
        product.setSessionId("session-id2");
        AppDataJobGeneration gen12 = new AppDataJobGeneration();
        gen1.setTaskTable("tast-table-12");
        AppDataJobGeneration gen22 = new AppDataJobGeneration();
        gen2.setTaskTable("tast-table-22");
        AppDataJobGeneration gen32 = new AppDataJobGeneration();
        gen3.setTaskTable("tast-table-32");
        GenericMessageDto<ProductionEvent> message12 = new GenericMessageDto<ProductionEvent>(12, "topic12", null);
        GenericMessageDto<ProductionEvent> message22 = new GenericMessageDto<ProductionEvent>(22, "topic12", null);
        obj2.setId(123);
        obj2.setLevel(ApplicationLevel.L1);
        obj2.setPod("pod-name-2");
        obj2.setState(AppDataJobState.GENERATING);
        obj2.setCreationDate(new Date());
        obj2.setLastUpdateDate(new Date());
        obj2.setProduct(product2);
        obj2.setMessages(Arrays.asList(message12, message22));
        obj2.setGenerations(Arrays.asList(gen12, gen22, gen32));
        obj2.setCategory(ProductCategory.AUXILIARY_FILES);
        
        doReturn(Optional.of(obj2)).when(appDataJobDao).findById(Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        this.appDataJobService.patchJob(123L, obj);
        verify(appDataJobDao, times(1)).findById(Mockito.eq(123L));
        verify(appDataJobDao, times(1)).save(Mockito.eq(obj2));
    }
    
    @Test
    public void patchGenerationToJobTest() throws AppCatalogJobNotFoundException, AppCatalogJobGenerationTerminatedException, AppCatalogJobGenerationInvalidTransitionStateException, AppCatalogJobGenerationNotFoundException {
        AppDataJob obj = new AppDataJob();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("task-table-1");
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("task-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.WAITING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);       
        
        doReturn(Optional.of(obj)).when(appDataJobDao).findById(Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        doNothing().when(appDataJobDao).udpateJobGeneration(Mockito.anyLong(), Mockito.any());
        AppDataJobGeneration newGen = new AppDataJobGeneration();
        newGen.setTaskTable("task-table-1");
        newGen.setState(AppDataJobGenerationState.PRIMARY_CHECK);
        AppDataJob<?> newJob = this.appDataJobService.patchGenerationToJob(123L,"task-table-1" , newGen, 3);
        verify(appDataJobDao, times(1)).findById(Mockito.eq(123L));
        verify(appDataJobDao, never()).save(Mockito.any());
        verify(appDataJobDao, times(1)).udpateJobGeneration(Mockito.eq(123L), Mockito.any());
        assertEquals(AppDataJobGenerationState.PRIMARY_CHECK, newJob.getGenerations().get(0).getState());
        assertEquals(0, newJob.getGenerations().get(0).getNbErrors());
        assertEquals("task-table-1", newJob.getGenerations().get(0).getTaskTable());
        assertNotNull(newJob.getGenerations().get(0).getLastUpdateDate());
    }
    
    @Test
    public void patchGenerationToJobGen4Test() throws AppCatalogJobNotFoundException, AppCatalogJobGenerationTerminatedException, AppCatalogJobGenerationInvalidTransitionStateException, AppCatalogJobGenerationNotFoundException {
        AppDataJob<ProductionEvent> obj = new AppDataJob<>();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("task-table-1");
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("task-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.WAITING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);
        
        AppDataJobGeneration gen4 = new AppDataJobGeneration();
        gen4.setTaskTable("task-table-4");
        gen4.setState(AppDataJobGenerationState.PRIMARY_CHECK);
        
        doReturn(Optional.of(obj)).when(appDataJobDao).findById(Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        doNothing().when(appDataJobDao).udpateJobGeneration(Mockito.anyLong(), Mockito.any());
        AppDataJob<?> newJob = this.appDataJobService.patchGenerationToJob(123L,"task-table-1" ,gen4, 3);
        verify(appDataJobDao, times(1)).findById(Mockito.eq(123L));
        verify(appDataJobDao, never()).save(Mockito.any());
        verify(appDataJobDao, times(1)).udpateJobGeneration(Mockito.eq(123L), Mockito.any());
        assertEquals(AppDataJobGenerationState.PRIMARY_CHECK, newJob.getGenerations().get(0).getState());
        assertEquals(0, newJob.getGenerations().get(0).getNbErrors());
        assertEquals("task-table-1", newJob.getGenerations().get(0).getTaskTable());
        assertNotNull(newJob.getGenerations().get(0).getLastUpdateDate());
    }
    
    @Test(expected = AppCatalogJobGenerationInvalidTransitionStateException.class)
    public void patchGenerationToJobGen4InvalidTransitionTest() throws AppCatalogJobNotFoundException, AppCatalogJobGenerationTerminatedException, AppCatalogJobGenerationInvalidTransitionStateException, AppCatalogJobGenerationNotFoundException {
        AppDataJob<ProductionEvent> obj = new AppDataJob<>();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("task-table-1");
        gen1.setState(AppDataJobGenerationState.READY);
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("task-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.WAITING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);
        
        AppDataJobGeneration gen5 = new AppDataJobGeneration();
        gen5.setTaskTable("task-table-4");
        gen5.setState(AppDataJobGenerationState.INITIAL);
        
        doReturn(Optional.of(obj)).when(appDataJobDao).findById(Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        doNothing().when(appDataJobDao).udpateJobGeneration(Mockito.anyLong(), Mockito.any());
        this.appDataJobService.patchGenerationToJob(123L,"task-table-1" ,gen5, 3);
    }
    
    @Test
    public void patchGenerationToJobGen5Test() throws AppCatalogJobNotFoundException, AppCatalogJobGenerationTerminatedException, AppCatalogJobGenerationInvalidTransitionStateException, AppCatalogJobGenerationNotFoundException {
        AppDataJob<ProductionEvent> obj = new AppDataJob<>();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("task-table-1");
        gen1.setState(AppDataJobGenerationState.PRIMARY_CHECK);
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("task-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.WAITING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);
        
        AppDataJobGeneration gen5 = new AppDataJobGeneration();
        gen5.setTaskTable("task-table-4");
        gen5.setState(AppDataJobGenerationState.READY);
        
        doReturn(Optional.of(obj)).when(appDataJobDao).findById(Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        doNothing().when(appDataJobDao).udpateJobGeneration(Mockito.anyLong(), Mockito.any());
        AppDataJob<?> newJob = this.appDataJobService.patchGenerationToJob(123L,"task-table-1" ,gen5, 3);
        verify(appDataJobDao, times(1)).findById(Mockito.eq(123L));
        verify(appDataJobDao, never()).save(Mockito.any());
        verify(appDataJobDao, times(1)).udpateJobGeneration(Mockito.eq(123L), Mockito.any());
        assertEquals(AppDataJobGenerationState.READY, newJob.getGenerations().get(0).getState());
        assertEquals(0, newJob.getGenerations().get(0).getNbErrors());
        assertEquals("task-table-1", newJob.getGenerations().get(0).getTaskTable());
        assertNotNull(newJob.getGenerations().get(0).getLastUpdateDate());
    }
    
    @Test(expected = AppCatalogJobGenerationInvalidTransitionStateException.class)
    public void patchGenerationToJobGen5InvalidTransitionTest() throws AppCatalogJobNotFoundException, AppCatalogJobGenerationTerminatedException, AppCatalogJobGenerationInvalidTransitionStateException, AppCatalogJobGenerationNotFoundException {
        AppDataJob<ProductionEvent> obj = new AppDataJob<>();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("task-table-1");
        gen1.setState(AppDataJobGenerationState.INITIAL);
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("task-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.WAITING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);
        
        AppDataJobGeneration gen5 = new AppDataJobGeneration();
        gen5.setTaskTable("task-table-4");
        gen5.setState(AppDataJobGenerationState.READY);
        
        doReturn(Optional.of(obj)).when(appDataJobDao).findById(Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        doNothing().when(appDataJobDao).udpateJobGeneration(Mockito.anyLong(), Mockito.any());
        this.appDataJobService.patchGenerationToJob(123L,"task-table-1" ,gen5, 3);
    }
    
    @Test
    public void patchGenerationToJobGen6Test() throws AppCatalogJobNotFoundException, AppCatalogJobGenerationTerminatedException, AppCatalogJobGenerationInvalidTransitionStateException, AppCatalogJobGenerationNotFoundException {
        AppDataJob<ProductionEvent> obj = new AppDataJob<>();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("task-table-1");
        gen1.setState(AppDataJobGenerationState.READY);
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("task-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.WAITING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);
        
        AppDataJobGeneration gen6 = new AppDataJobGeneration();
        gen6.setTaskTable("task-table-4");
        gen6.setState(AppDataJobGenerationState.SENT);
        
        doReturn(Optional.of(obj)).when(appDataJobDao).findById(Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        doNothing().when(appDataJobDao).udpateJobGeneration(Mockito.anyLong(), Mockito.any());
        AppDataJob<?> newJob = this.appDataJobService.patchGenerationToJob(123L,"task-table-1" ,gen6, 3);
        verify(appDataJobDao, times(2)).findById(Mockito.eq(123L));
        verify(appDataJobDao, never()).save(Mockito.any());
        verify(appDataJobDao, times(1)).udpateJobGeneration(Mockito.eq(123L), Mockito.any());
        assertEquals(AppDataJobGenerationState.SENT, newJob.getGenerations().get(0).getState());
        assertEquals(0, newJob.getGenerations().get(0).getNbErrors());
        assertEquals("task-table-1", newJob.getGenerations().get(0).getTaskTable());
        assertNotNull(newJob.getGenerations().get(0).getLastUpdateDate());
    }
    
    @Test
    public void patchGenerationToJobGenAllSent() throws AppCatalogJobNotFoundException, AppCatalogJobGenerationTerminatedException, AppCatalogJobGenerationInvalidTransitionStateException, AppCatalogJobGenerationNotFoundException {
        AppDataJob<ProductionEvent> obj = new AppDataJob<>();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("task-table-1");
        gen1.setState(AppDataJobGenerationState.READY);
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task-table-2");
        gen2.setState(AppDataJobGenerationState.SENT);
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("task-table-3");
        gen3.setState(AppDataJobGenerationState.SENT);
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.WAITING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);
        
        AppDataJobGeneration gen6 = new AppDataJobGeneration();
        gen6.setTaskTable("task-table-4");
        gen6.setState(AppDataJobGenerationState.SENT);
        
        doReturn(Optional.of(obj)).when(appDataJobDao).findById(Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        doNothing().when(appDataJobDao).udpateJobGeneration(Mockito.anyLong(), Mockito.any());
        AppDataJob<?> newJob = this.appDataJobService.patchGenerationToJob(123L,"task-table-1" ,gen6, 3);
        verify(appDataJobDao, times(2)).findById(Mockito.eq(123L));
        verify(appDataJobDao, times(1)).save(Mockito.any());
        verify(appDataJobDao, times(1)).udpateJobGeneration(Mockito.eq(123L), Mockito.any());
        assertEquals(AppDataJobGenerationState.SENT, newJob.getGenerations().get(0).getState());
        assertEquals(0, newJob.getGenerations().get(0).getNbErrors());
        assertEquals("task-table-1", newJob.getGenerations().get(0).getTaskTable());
        assertNotNull(newJob.getGenerations().get(0).getLastUpdateDate());
        assertEquals(AppDataJobState.TERMINATED, newJob.getState());
    }
    
    @Test(expected = AppCatalogJobGenerationInvalidTransitionStateException.class)
    public void patchGenerationToJobGen6InvalidTransitionTest() throws AppCatalogJobNotFoundException, AppCatalogJobGenerationTerminatedException, AppCatalogJobGenerationInvalidTransitionStateException, AppCatalogJobGenerationNotFoundException {
        AppDataJob<ProductionEvent> obj = new AppDataJob<>();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("task-table-1");
        gen1.setState(AppDataJobGenerationState.PRIMARY_CHECK);
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("task-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.WAITING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);
        
        AppDataJobGeneration gen5 = new AppDataJobGeneration();
        gen5.setTaskTable("task-table-4");
        gen5.setState(AppDataJobGenerationState.SENT);
        
        doReturn(Optional.of(obj)).when(appDataJobDao).findById(Mockito.any());
        doNothing().when(appDataJobDao).udpateJobGeneration(Mockito.anyLong(), Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        this.appDataJobService.patchGenerationToJob(123L,"task-table-1" ,gen5, 3);
    }
    
    @Test(expected = AppCatalogJobGenerationTerminatedException.class)
    public void patchGenerationToJobErrorTest() throws AppCatalogJobNotFoundException, AppCatalogJobGenerationTerminatedException, AppCatalogJobGenerationInvalidTransitionStateException, AppCatalogJobGenerationNotFoundException {
        AppDataJob<ProductionEvent> obj = new AppDataJob<>();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("task-table-1");
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("task-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.WAITING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);       
        
        doReturn(Optional.of(obj)).when(appDataJobDao).findById(Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        doNothing().when(appDataJobDao).udpateJobGeneration(Mockito.anyLong(), Mockito.any());
        this.appDataJobService.patchGenerationToJob(123L,"task-table-1" ,gen1, 1);
    }
    
    @Test(expected = AppCatalogJobGenerationNotFoundException.class)
    public void patchGenerationToJobNoTaskTableTest() throws AppCatalogJobNotFoundException, AppCatalogJobGenerationTerminatedException, AppCatalogJobGenerationInvalidTransitionStateException, AppCatalogJobGenerationNotFoundException {
        AppDataJob<ProductionEvent> obj = new AppDataJob<>();
        AppDataJobProduct product = new AppDataJobProduct();
        product.setSessionId("session-id");
        AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("tast-table-1");
        AppDataJobGeneration gen2 = new AppDataJobGeneration();
        gen2.setTaskTable("task-table-2");
        AppDataJobGeneration gen3 = new AppDataJobGeneration();
        gen3.setTaskTable("task-table-3");
        GenericMessageDto<ProductionEvent> message1 = new GenericMessageDto<ProductionEvent>(1, "topic1", null);
        GenericMessageDto<ProductionEvent> message2 = new GenericMessageDto<ProductionEvent>(2, "topic1", null);
        obj.setId(123);
        obj.setLevel(ApplicationLevel.L1);
        obj.setPod("pod-name");
        obj.setState(AppDataJobState.WAITING);
        obj.setCreationDate(new Date());
        obj.setLastUpdateDate(new Date());
        obj.setProduct(product);
        obj.setMessages(Arrays.asList(message1, message2));
        obj.setGenerations(Arrays.asList(gen1, gen2, gen3));
        obj.setCategory(ProductCategory.AUXILIARY_FILES);       
        
        doReturn(Optional.of(obj)).when(appDataJobDao).findById(Mockito.any());
        doReturn(obj).when(appDataJobDao).save(Mockito.any());
        doNothing().when(appDataJobDao).udpateJobGeneration(Mockito.anyLong(), Mockito.any());
        this.appDataJobService.patchGenerationToJob(123L,"task-table-1" ,gen1, 1);
    }
    

}
