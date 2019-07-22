package esa.s1pdgs.cpoc.appcatalog.server.job.rest;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.server.RestControllerTest;
import esa.s1pdgs.cpoc.appcatalog.server.job.converter.JobConverter;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobService;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobGenerationInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobInvalidStateException;
import esa.s1pdgs.cpoc.appcatalog.server.job.exception.AppCatalogJobNotFoundException;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;

public class JobControllerTest extends RestControllerTest{

    
    @Mock
    private AppDataJobService appDataJobService;

    @Mock
    private JobConverter jobConverter;
    
    private JobController jobController;
    
    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        this.jobController = new JobController(appDataJobService, jobConverter, new JobControllerConfiguration());
        this.initMockMvc(this.jobController);
    }
    
    @Test
    public void deleteJobTest() throws Exception {
        doNothing().when(appDataJobService).deleteJob(Mockito.eq(123L));
        this.jobController.deleteJob(123L);
    }
    
    @Test
    public void patchJobTest() throws AppCatalogJobInvalidStateException, AppCatalogJobGenerationInvalidStateException, AppCatalogJobNotFoundException {
        doReturn(new AppDataJobDto()).when(jobConverter).convertJobFromDbToDto(Mockito.any(), Mockito.any());
        this.jobController.patchJob(ProductCategory.LEVEL_JOBS.toString().toLowerCase(), 123L, new AppDataJobDto());
    }
    
    @Test
    public void searchTest() throws AppCatalogJobNotFoundException, AppCatalogJobInvalidStateException, AppCatalogJobGenerationInvalidStateException, InternalErrorException {
        Map<String, String> params = new HashMap<>();
        params.put("[orderByAsc]", "valueFilter");
        params.put("[orderByDesc]", "valueFilter1");
        params.put("_id", "123");
        params.put("messages.identifier", "124");
        params.put("creationDate", "20180227T104128");
        params.put("product.stopTime", "20180227T104128");
        
        List<FilterCriterion> filters = new ArrayList<>();
        filters.add(new FilterCriterion("product.stopTime", jobController.convertDateIso("20180227T104128")));
        filters.add(new FilterCriterion("messages.identifier", 124L));
        filters.add(new FilterCriterion("_id", 123L));
        filters.add(new FilterCriterion("creationDate", jobController.convertDateIso("20180227T104128")));
        
        List<AppDataJob> jobsDb = new ArrayList<>();
        doReturn(jobsDb).when(appDataJobService).search(Mockito.any(), Mockito.any(), Mockito.any());
                
        this.jobController.search(ProductCategory.LEVEL_JOBS.toString().toLowerCase(), params);
        
        verify(appDataJobService, times(1)).search(Mockito.eq(filters), 
                Mockito.eq(ProductCategory.LEVEL_JOBS), 
                Mockito.eq(new Sort(Direction.DESC, "valueFilter1")));        
       
    }
    
}
