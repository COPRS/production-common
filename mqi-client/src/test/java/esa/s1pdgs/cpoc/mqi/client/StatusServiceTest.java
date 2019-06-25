package esa.s1pdgs.cpoc.mqi.client;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiStatusApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiStopApiError;
import esa.s1pdgs.cpoc.mqi.model.rest.StatusDto;

/**
 * Test the REST service ErrorService
 * 
 * @author Viveris Technologies
 */
public class StatusServiceTest {

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
    private StatusService service;

    /**
     * DTO
     */
    private StatusDto message;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        
        final MqiClientFactory factory = new MqiClientFactory("uri", 2, 500)
        		.restTemplateSupplier(() -> restTemplate);
        
        service = factory.newStatusService();
        message = new StatusDto(AppState.ERROR, 123, 1);
    }
    
    @Test
    public void tesConstructor() {
        service = new StatusService(restTemplate, "uri", -1, 500);
        assertEquals(0, service.maxRetries);
        service = new StatusService(restTemplate, "uri", 21, 500);
        assertEquals(0, service.maxRetries);
    }

    /**
     * Test stop when no response from the rest server
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStopWhenNoResponse() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class), Mockito.isNull(),
                        Mockito.any(Class.class));

        thrown.expect(MqiStopApiError.class);

        service.stop();
    }

    /**
     * Test stop when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStopWhenResponseKO() throws AbstractCodedException {
        doReturn(new ResponseEntity<String>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<String>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class), Mockito.isNull(),
                                Mockito.any(Class.class));

        thrown.expect(MqiStopApiError.class);
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.stop();
    }

    /**
     * Test the max retries applied before launching an exception
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMaxRetries() throws AbstractCodedException {
        doReturn(new ResponseEntity<String>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<String>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class), Mockito.isNull(),
                                Mockito.any(Class.class));

        try {
            service.stop();
            fail("An exception shall be raised");
        } catch (MqiStopApiError mpee) {
            verify(restTemplate, times(2)).exchange(Mockito.eq("uri/app/stop"),
                    Mockito.eq(HttpMethod.POST), Mockito.eq(null),
                    Mockito.eq(String.class));
            verifyNoMoreInteractions(restTemplate);
        }
    }

    /**
     * Test stop when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStop1() throws AbstractCodedException {
        doReturn(new ResponseEntity<String>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<String>(HttpStatus.OK)).when(restTemplate)
                        .exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class), Mockito.isNull(),
                                Mockito.any(Class.class));

        service.stop();
        verify(restTemplate, times(2)).exchange(Mockito.eq("uri/app/stop"),
                Mockito.eq(HttpMethod.POST), Mockito.eq(null),
                Mockito.eq(String.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test stop when the first time works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStop2() throws AbstractCodedException {
        doReturn(new ResponseEntity<String>(HttpStatus.OK)).when(restTemplate)
                .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
                        Mockito.isNull(), Mockito.any(Class.class));

        service.stop();
        verify(restTemplate, times(1)).exchange(Mockito.eq("uri/app/stop"),
                Mockito.eq(HttpMethod.POST), Mockito.eq(null),
                Mockito.eq(String.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test publish when no response from the rest server
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStatusWhenNoResponse() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class), Mockito.isNull(),
                        Mockito.any(Class.class));

        thrown.expect(MqiStatusApiError.class);

        service.status();
    }

    /**
     * Test publish when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStatusWhenResponseKO() throws AbstractCodedException {
        doReturn(new ResponseEntity<StatusDto>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<StatusDto>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<StatusDto>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class), Mockito.isNull(),
                                Mockito.any(Class.class));

        thrown.expect(MqiStatusApiError.class);
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.status();
    }

    /**
     * Test publish when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStatus1() throws AbstractCodedException {
        doReturn(new ResponseEntity<StatusDto>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<StatusDto>(message, HttpStatus.OK))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class), Mockito.isNull(),
                                Mockito.any(Class.class));

        StatusDto ret = service.status();
        assertEquals(message, ret);
        verify(restTemplate, times(2)).exchange(Mockito.eq("uri/app/status"),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(StatusDto.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test publish when the first time works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStatus2() throws AbstractCodedException {
        doReturn(new ResponseEntity<StatusDto>(message, HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class), Mockito.isNull(),
                        Mockito.any(Class.class));

        StatusDto ret = service.status();
        assertEquals(message, ret);
        verify(restTemplate, times(1)).exchange(Mockito.eq("uri/app/status"),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(StatusDto.class));
        verifyNoMoreInteractions(restTemplate);
    }
}
