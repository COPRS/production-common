package esa.s1pdgs.cpoc.mqi.client;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublishApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

/**
 * Test the REST service ErrorService
 * @author Viveris Technologies
 *
 */
public class ErrorServiceTest {

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
    private ErrorService service;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new ErrorService(restTemplate, "uri", 2, 500);
    }
    
    @Test
    public void tesConstructor() {
        service = new ErrorService(restTemplate, "uri", -1, 500);
        assertEquals(0, service.maxRetries);
        service = new ErrorService(restTemplate, "uri", 21, 500);
        assertEquals(0, service.maxRetries);
    }

    /**
     * Test publish when no response from the rest server
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testPublishWhenNoResponse() throws AbstractCodedException {
    	ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage("message to publish");
        GenericPublicationMessageDto<ErrorDto> message = new GenericPublicationMessageDto<ErrorDto>(ProductFamily.BLANK, errorDto);

        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        thrown.expect(MqiPublishApiError.class);
        thrown.expect(hasProperty("output", is(message)));

        service.publish(message);
    }

    /**
     * Test publish when the rest server respond an error
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testPublishWhenResponseKO() throws AbstractCodedException {
    	ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage("message to publish");
        GenericPublicationMessageDto<ErrorDto> message = new GenericPublicationMessageDto<ErrorDto>(ProductFamily.BLANK, errorDto);
    	
        doReturn(new ResponseEntity<Void>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Void>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        thrown.expect(MqiPublishApiError.class);
        thrown.expect(hasProperty("output", is(message)));
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.publish(message);
    }

    /**
     * Test the max retries applied before launching an exception
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMaxRetries() throws AbstractCodedException {
        doReturn(new ResponseEntity<Void>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Void>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

    	ErrorDto errorDto = new ErrorDto();
    	errorDto.setMessage("message to publish");
    	GenericPublicationMessageDto<ErrorDto> message = new GenericPublicationMessageDto<ErrorDto>(ProductFamily.BLANK, errorDto);
        try {
            service.publish(message);
            fail("An exception shall be raised");
        } catch (MqiPublishApiError mpee) {
            verify(restTemplate, times(2)).exchange(
                    Mockito.eq("uri/errors/publish"),
                    Mockito.eq(HttpMethod.POST),
                    Mockito.eq(new HttpEntity<GenericPublicationMessageDto<ErrorDto>>(message)),
                    Mockito.eq(Void.class));
            verifyNoMoreInteractions(restTemplate);
        }
    }

    /**
     * Test publish when the first time fails and the second works
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testPublish1() throws AbstractCodedException {
        doReturn(new ResponseEntity<Void>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Void>(HttpStatus.OK)).when(restTemplate)
                        .exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage("message to publish");
        GenericPublicationMessageDto<ErrorDto> message = new GenericPublicationMessageDto<ErrorDto>(ProductFamily.BLANK, errorDto);
        service.publish(message);
        verify(restTemplate, times(2)).exchange(
                Mockito.eq("uri/errors/publish"), Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<GenericPublicationMessageDto<ErrorDto>>(message)),
                Mockito.eq(Void.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test publish when the first time works
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testPublish2() throws AbstractCodedException {
        doReturn(new ResponseEntity<Void>(HttpStatus.OK)).when(restTemplate)
                .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        ErrorDto errorDto = new ErrorDto();
        errorDto.setMessage("message to publish");
        GenericPublicationMessageDto<ErrorDto> message = new GenericPublicationMessageDto<ErrorDto>(ProductFamily.BLANK, errorDto);
        service.publish(message);
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/errors/publish"), Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<GenericPublicationMessageDto<ErrorDto>>(message)),
                Mockito.eq(Void.class));
        verifyNoMoreInteractions(restTemplate);
    }
}
