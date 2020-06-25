package esa.s1pdgs.cpoc.appcatalog.server.rest;

import static org.junit.Assert.assertEquals;
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

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.service.AppDataJobService;
import esa.s1pdgs.cpoc.common.filter.FilterCriterion;

public class JobControllerTest extends RestControllerTest{

    
    @Mock
    private AppDataJobService appDataJobService;

    private JobController jobController;
    
    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        jobController = new JobController(appDataJobService);
        this.initMockMvc(jobController);
    }
    
    @Test
    public void deleteJobTest() throws Exception {
        doNothing().when(appDataJobService).deleteJob(Mockito.eq(123L));
        jobController.deleteJob(123L);
    }
    
    @Test
    public void patchJobTest() throws Exception {
        final long jobId = 1377;
    	doReturn(new AppDataJob(jobId))
        	.when(appDataJobService)
        	.updateJob(Mockito.any());    	
    	final AppDataJob actual = jobController.update(jobId, new AppDataJob(jobId));
    	assertEquals(jobId, actual.getId());
    }
    
    @Test
    public void searchTest() throws Exception {
        final Map<String, String> params = new HashMap<>();
        params.put("[orderByAsc]", "valueFilter");
        params.put("[orderByDesc]", "valueFilter1");
        params.put("_id", "124");
        params.put("messages.id", "124");
        params.put("creationDate", "20180227T104128");
        params.put("product.stopTime", "20180227T104128");
        
        final List<FilterCriterion> filters = new ArrayList<>();
        filters.add(new FilterCriterion("product.stopTime", jobController.convertDateIso("20180227T104128")));
        filters.add(new FilterCriterion("_id", 124L));
        filters.add(new FilterCriterion("creationDate", jobController.convertDateIso("20180227T104128")));
        filters.add(new FilterCriterion("messages.id", 124L));
        
        final List<AppDataJob> jobsDb = new ArrayList<>();
        
        doReturn(jobsDb)
        	.when(appDataJobService)
        	.search(Mockito.any(), Mockito.any());
                
        jobController.search(params);
        
        verify(appDataJobService, times(1))
        	.search(Mockito.eq(filters), Mockito.eq(new Sort(Direction.DESC, "valueFilter1")));        
       
    }
    
}
