package esa.s1pdgs.cpoc.mqi.server.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.StatusProcessingApiError;

/**
 * Test the service OtherApplicationService
 * 
 * @author Viveris Technologies
 */
public class OtherApplicationServiceTest {

    /**
     * To check the raised custom exceptions
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Rest template
     */
    @Mock
    private RestTemplate restTemplate;

    /**
     * Service to test
     */
    private OtherApplicationService service;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new OtherApplicationService(restTemplate, "uri", 2, 500,
                ".processing.default.svc.cluster.local");
    }

    /**
     * Test the constructor
     */
    @Test
    public void tesConstructor() {
        assertEquals(2, service.getMaxRetries());
        assertEquals(500, service.getTempoRetryMs());
        assertEquals("uri", service.getPortUri());

        service = new OtherApplicationService(restTemplate, "uri", -1, 500,
                ".processing.default.svc.cluster.local");
        assertEquals(0, service.getMaxRetries());
        service = new OtherApplicationService(restTemplate, "uri", 21, 500,
                ".processing.default.svc.cluster.local");
        assertEquals(0, service.getMaxRetries());
    }

    /**
     * Test isProcessing when no response from the rest server
     * 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testIsProcessingWhenNoResponse() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class), Mockito.isNull(),
                        Mockito.any(Class.class));

        thrown.expect(StatusProcessingApiError.class);
        thrown.expect(hasProperty("uri", is(
                "http://pod-name.processing.default.svc.cluster.local:uri/app/level_jobs/process/12345")));

        service.isProcessing("pod-name", ProductCategory.LEVEL_JOBS, 12345);
    }

    /**
     * Test isProcessing when the rest server respond an error
     * 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testIsProcessingWhenResponseKO() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class), Mockito.isNull(),
                                Mockito.any(Class.class));

        thrown.expect(StatusProcessingApiError.class);
        thrown.expect(hasProperty("uri", is(
                "http://pod-name.processing.default.svc.cluster.local:uri/app/level_jobs/process/12345")));
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.isProcessing("pod-name", ProductCategory.LEVEL_JOBS, 12345);
    }

    /**
     * Test the max retries applied before launching an exception
     * 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMaxRetries() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class), Mockito.isNull(),
                                Mockito.any(Class.class));

        try {
            service.isProcessing("pod-name", ProductCategory.LEVEL_JOBS, 12345);
            fail("An exception shall be raised");
        } catch (StatusProcessingApiError mpee) {
            verify(restTemplate, times(2)).exchange(Mockito.eq(
                    "http://pod-name.processing.default.svc.cluster.local:uri/app/level_jobs/process/12345"),
                    Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                    Mockito.eq(Boolean.class));
            verifyNoMoreInteractions(restTemplate);
        }
    }

    /**
     * Test isProcessing when the first time fails and the second works
     * 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testIsProcessing1() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<>(true, HttpStatus.OK))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class), Mockito.isNull(),
                                Mockito.any(Class.class));

        assertTrue(service.isProcessing("pod-name", ProductCategory.LEVEL_JOBS,
                12345));
        verify(restTemplate, times(2)).exchange(Mockito.eq(
                "http://pod-name.processing.default.svc.cluster.local:uri/app/level_jobs/process/12345"),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test isProcessing when the first time works
     * 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testIsProcessing2() throws AbstractCodedException {
        doReturn(new ResponseEntity<>(false, HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class), Mockito.isNull(),
                        Mockito.any(Class.class));

        assertFalse(service.isProcessing("pod-name", ProductCategory.LEVEL_JOBS,
                12345));
        verify(restTemplate, times(1)).exchange(Mockito.eq(
                "http://pod-name.processing.default.svc.cluster.local:uri/app/level_jobs/process/12345"),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test isProcessing when server returns an empty body
     * 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testIsProcessingWhenEmptyBody() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.OK)).when(restTemplate)
                .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
                        Mockito.isNull(), Mockito.any(Class.class));

        assertFalse(service.isProcessing("pod-name", ProductCategory.LEVEL_JOBS,
                12345));
        verify(restTemplate, times(1)).exchange(Mockito.eq(
                "http://pod-name.processing.default.svc.cluster.local:uri/app/level_jobs/process/12345"),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }

}
