package esa.s1pdgs.cpoc.appcatalog.client.job;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGeneration;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobGenerationState;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobNewApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobPatchApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobSearchApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;


public class AppCatalogJobClientTest {

    /**
     * Rest template
     */
    @Mock
    private RestTemplate restTemplate;

    
    /**
     * Client to test
     */
    private AppCatalogJobClient client;
    
    private static final CatalogEvent DUMMY = new CatalogEvent();

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        client = new AppCatalogJobClient(restTemplate, "http://localhost:8080", 3, 200);
    }

    @Test
    public void testConstructor() {
        assertEquals(3, client.getMaxRetries());
        assertEquals(200, client.getTempoRetryMs());
        assertEquals("http://localhost:8080", client.getHostUri());
    }

    @SuppressWarnings("unchecked")
	@Test(expected = AppCatalogJobSearchApiError.class)
    public void testFindByMessagesIdWhenError() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
	        .when(restTemplate).exchange(
	        		Mockito.any(URI.class),
	                Mockito.any(HttpMethod.class),
	                Mockito.any(),
	                Mockito.any(ParameterizedTypeReference.class)
	    );        
        client.findByMessagesId(1L);
    }
    
    @SuppressWarnings("unchecked")
	@Test(expected = AppCatalogJobSearchApiError.class)
    public void testFindByProductSessionIdWhenError() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
	        .when(restTemplate).exchange(
	        		Mockito.any(URI.class),
	                Mockito.any(HttpMethod.class),
	                Mockito.any(),
	                Mockito.any(ParameterizedTypeReference.class)
	    );        
        client.findByProductSessionId("sessionId");
    }

    @SuppressWarnings("unchecked")
	@Test(expected = AppCatalogJobSearchApiError.class)
    public void testFindByProductDataTakeIdWhenError() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
	        .when(restTemplate).exchange(
	        		Mockito.any(URI.class),
	                Mockito.any(HttpMethod.class),
	                Mockito.any(),
	                Mockito.any(ParameterizedTypeReference.class)
	    );        
        client.findByProductDataTakeId("dataTakeId");
    }

    @SuppressWarnings("unchecked")
	@Test(expected = AppCatalogJobSearchApiError.class)
    public void testFindJobInStateGeneratingWhenError() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
	        .when(restTemplate).exchange(
	        		Mockito.any(URI.class),
	                Mockito.any(HttpMethod.class),
	                Mockito.any(),
	                Mockito.any(ParameterizedTypeReference.class)
	    );        
        client.findJobInStateGenerating("taskTable");
    }

    @SuppressWarnings("unchecked")
	private final void runSearchTest(final Callable<Void> callable, final String apiMethod, final String apiParameterValue) throws Exception {   
    	final URI expectedUri = new URI("http://localhost:8080/jobs/" + apiMethod + "/" + apiParameterValue);
    	doReturn(new ResponseEntity<AppDataJob>(HttpStatus.OK))
	    	.when(restTemplate).exchange(
		        		Mockito.any(URI.class),
		                Mockito.any(HttpMethod.class),
		                Mockito.any(),
		                Mockito.any(ParameterizedTypeReference.class)
	    );
        callable.call();
        
        verify(restTemplate, times(1)).exchange(
                Mockito.eq(expectedUri),
                Mockito.eq(HttpMethod.GET),
                Mockito.isNull(),
                Mockito.eq(new ParameterizedTypeReference<List<AppDataJob>>() {})
        );
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    public void testtestFindByMessagesId() throws Exception {
    	runSearchTest(
    			() -> {
    			    client.findByMessagesId(1L);
    		        return null;
    			},
    			"findByMessagesId",
    			"1"
    	);
    }

    @Test
    public void testFindByProductSessionId() throws Exception {
    	runSearchTest(
    			() -> {
    			    client.findByProductSessionId("sessionId");
    		        return null;
    			},
    			"findByProductSessionId",
    			"sessionId"
    	);
    }

    @Test
    public void testFindByProductDataTakeId() throws Exception {
    	runSearchTest(
    			() -> {
    			    client.findByProductDataTakeId("dataTakeId");
    		        return null;
    			},
    			"findByProductDataTakeId",
    			"dataTakeId"
    	);
    }
    
    @Test
    public void testFindJobInStateGenerating() throws Exception {
    	runSearchTest(
    			() -> {
    			    client.findJobInStateGenerating("taskTable");
    		        return null;
    			},
    			"findJobInStateGenerating",
    			"taskTable"
    	);
    }
    private AppDataJob buildJob() {
        final AppDataJob job = new AppDataJob();
        job.setId(142);
        job.setState(AppDataJobState.DISPATCHING);
        
        final AppDataJobProduct product = new AppDataJobProduct();
        product.getMetadata().put("productName", "toto");
        job.setProduct(product);
        
        final GenericMessageDto<CatalogEvent> message1 = new GenericMessageDto<CatalogEvent>(1, "key1", DUMMY);
        final GenericMessageDto<CatalogEvent> message2 = new GenericMessageDto<CatalogEvent>(2, "key2", DUMMY);
        job.setMessages(Arrays.asList(message1, message2));
        
        final AppDataJobGeneration gen1 = new AppDataJobGeneration();
        gen1.setTaskTable("tasktable1");
        gen1.setState(AppDataJobGenerationState.INITIAL);
        gen1.setCreationDate(new Date(0L));
        job.setGeneration(gen1);
        return job;
    }

    @SuppressWarnings("unchecked")
	@Test(expected = AppCatalogJobNewApiError.class)
    public void testNewWhenError() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
	        .when(restTemplate).exchange(
	        		Mockito.anyString(),
	                Mockito.eq(HttpMethod.POST),
	                Mockito.any(HttpEntity.class),
	                Mockito.any(ParameterizedTypeReference.class)
	    );
        client.newJob(buildJob());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testNew() throws AbstractCodedException {
    	final AppDataJob job = buildJob();
    	doReturn(new ResponseEntity<AppDataJob>(job, HttpStatus.OK))
	    	.when(restTemplate).exchange(
	        		Mockito.anyString(),
	                Mockito.eq(HttpMethod.POST),
	                Mockito.any(HttpEntity.class),
	                Mockito.any(ParameterizedTypeReference.class)
	    );
        final AppDataJob result = client.newJob(job);
        assertEquals(job, result);
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("http://localhost:8080/jobs"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<AppDataJob>(job)),
                  Mockito.eq(new ParameterizedTypeReference<AppDataJob>() {})
        );
        verifyNoMoreInteractions(restTemplate);        
    }

    @SuppressWarnings("unchecked")
	@Test(expected = AppCatalogJobPatchApiError.class)
    public void testPatchWhenError() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
	        .when(restTemplate).exchange(
	        		Mockito.anyString(),
	                Mockito.eq(HttpMethod.PATCH),
	                Mockito.any(HttpEntity.class),
	                Mockito.any(ParameterizedTypeReference.class)
	    );    	
        final AppDataJob job = buildJob();
        client.updateJob(job);
    }
    
	@SuppressWarnings("unchecked")
	private final AppDataJob runPatchTest(final AppDataJob job, final Callable<AppDataJob> callable) throws Exception {   
    	doReturn(new ResponseEntity<AppDataJob>(job, HttpStatus.OK))
    	
	    	.when(restTemplate).exchange(
	        		Mockito.anyString(),
	                Mockito.eq(HttpMethod.PATCH),
	                Mockito.any(HttpEntity.class),
	                Mockito.any(ParameterizedTypeReference.class)
	    );
        final AppDataJob result = callable.call();     
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("http://localhost:8080/jobs/142"),
                Mockito.eq(HttpMethod.PATCH),
                Mockito.eq(new HttpEntity<AppDataJob>(result)),
                Mockito.eq(new ParameterizedTypeReference<AppDataJob>() {})
        );
        verifyNoMoreInteractions(restTemplate);
        return result;
    }

	@Test
    public void testPatch() throws Exception {
    	final AppDataJob job = buildJob();
    	final AppDataJob result = runPatchTest(
    			job,
    			() ->  client.updateJob(job)
    	);
    	assertEquals(job, result);    	
    }

	@Test
    public void testUpdate() throws Exception {
        final AppDataJob job = buildJob();
        final AppDataJob result = runPatchTest(
    			job,
    			() -> client.updateJob(job)
    	);        
        assertEquals(job.getMessages().size(), result.getMessages().size());
        assertEquals(job.getProduct(), result.getProduct());
        assertEquals(job.getGeneration(), result.getGeneration());
    }
}
