package esa.s1pdgs.cpoc.ipf.preparation.worker.appcat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;

public class TestAppCatJobService {
	@Mock
	private AppCatalogJobClient appCatClient;
	
	@Mock
    private GracePeriodHandler gracePeriodHandler;
	
	private AppCatJobService uut;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        uut = new AppCatJobService(appCatClient, gracePeriodHandler);
    }
    
	@Test
	public final void testCreate() throws Exception {
		final AppDataJob job = new AppDataJob();		
    	doReturn(job).when(appCatClient).newJob(job);
		uut.create(job);
		verify(appCatClient, times(1)).newJob(Mockito.eq(job));		
	}
	
	@Test
	public final void testNext_AppCatReturnedNull_ShallReturnNull() throws Exception {
		final String tasktableName = "foo";
	  	doReturn(null).when(appCatClient).findJobInStateGenerating(Mockito.eq(tasktableName));
	  	assertNull(uut.next(tasktableName));
	}
	
	@Test
	public final void testNext_AppCatReturnedNulEmptyList_ShallReturnNull() throws Exception {
		final String tasktableName = "foo";
	  	doReturn(Collections.emptyList()).when(appCatClient).findJobInStateGenerating(Mockito.eq(tasktableName));
	  	assertNull(uut.next(tasktableName));
	}
	
	@Test
	public final void testNext_AppCatReturnedJobWithinGracePeriod_ShallReturnNull() throws Exception {
		final String tasktableName = "foo";
		final AppDataJob job = new AppDataJob();	
	  	doReturn(Collections.singletonList(job)).when(appCatClient).findJobInStateGenerating(Mockito.eq(tasktableName));
    	doReturn(true).when(gracePeriodHandler).isWithinGracePeriod(Mockito.any(), Mockito.eq(job.getGeneration()));
     	assertNull(uut.next(tasktableName));
		verify(gracePeriodHandler, times(1)).isWithinGracePeriod(Mockito.any(), Mockito.eq(job.getGeneration()));	
	}
	
	@Test
	public final void testNext_AppCatReturnedJobAfterGracePeriod_ShallReturnJob() throws Exception {
		final String tasktableName = "foo";
		final AppDataJob job = new AppDataJob();	
	  	doReturn(Collections.singletonList(job)).when(appCatClient).findJobInStateGenerating(Mockito.eq(tasktableName));
    	doReturn(false).when(gracePeriodHandler).isWithinGracePeriod(Mockito.any(), Mockito.eq(job.getGeneration()));
    	assertEquals(job, uut.next(tasktableName));
		verify(gracePeriodHandler, times(1)).isWithinGracePeriod(Mockito.any(), Mockito.eq(job.getGeneration()));	
	}
//	
//	@Test
//	public final void testFindJobFor_AppCatReturnsJobForMessage() {
//		final GenericMessageDto<CatalogEvent> mess = new GenericMessageDto<>();
//		mess.setId(1234);
//		final AppDataJob job = new AppDataJob();
//		job.getMessages()
//		
//	  	doReturn(Collections.singletonList(job)).when(appCatClient).findJobInStateGenerating(Mockito.eq(tasktableName));
//	
//		uut.findJobFor(mqiMessage)
//	}
}
