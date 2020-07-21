package esa.s1pdgs.cpoc.ipf.preparation.worker.appcat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.ipf.preparation.worker.generator.GracePeriodHandler;

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
	public final void testCreate() {
		
		
		
	}
}
