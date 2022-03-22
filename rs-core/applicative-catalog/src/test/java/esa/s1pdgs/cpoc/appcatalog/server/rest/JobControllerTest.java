package esa.s1pdgs.cpoc.appcatalog.server.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.service.AppDataJobService;

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
    public void findByMessagesId() {
    	final List<AppDataJob> jobsDb = new ArrayList<>();
        
        doReturn(jobsDb)
        	.when(appDataJobService)
        	.findByMessagesId(Mockito.anyLong());
                
        appDataJobService.findByMessagesId(123L);
        
        verify(appDataJobService, times(1)).findByMessagesId(Mockito.eq(123L));
    }

    @Test
    public void findByProductSessionId() {
    	final List<AppDataJob> jobsDb = new ArrayList<>();

    	doReturn(jobsDb)
    		.when(appDataJobService)
    		.findByProductSessionId(Mockito.anyString());

    	appDataJobService.findByProductSessionId("sessionId");
    	
    	verify(appDataJobService, times(1)).findByProductSessionId(Mockito.eq("sessionId"));
    }
    
    @Test
    public void findByProductDataTakeId() {
    	final List<AppDataJob> jobsDb = new ArrayList<>();
    	
    	doReturn(jobsDb)
			.when(appDataJobService)
			.findByProductDataTakeId(Mockito.anyString(), Mockito.anyString());
    	
    	appDataJobService.findByProductDataTakeId("fooBar","dataTakeId");
    	
    	verify(appDataJobService, times(1)).findByProductDataTakeId(Mockito.eq("fooBar"), Mockito.eq("dataTakeId"));
    }
    
    @Test
    public void findJobInStateGenerating() {
    	final List<AppDataJob> jobsDb = new ArrayList<>();
    	
    	doReturn(jobsDb)
			.when(appDataJobService)
			.findJobInStateGenerating(Mockito.anyString());
    	
    	appDataJobService.findJobInStateGenerating("taskTable");
    	
    	verify(appDataJobService, times(1)).findJobInStateGenerating(Mockito.eq("taskTable"));
    }
    
}
