package esa.s1pdgs.cpoc.mqi.client;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import esa.s1pdgs.cpoc.common.errors.mqi.MqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublishApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

/**
 * Test the REST service ErrorService
 * 
 * @author Viveris Technologies
 */
public class GenericMqiServiceTest {

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
    private LevelProductsMqiService service;

    /**
     * DTO
     */
    private AckMessageDto ackMessage;
    private GenericPublicationMessageDto<LevelProductDto> pubMessage;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new LevelProductsMqiService(restTemplate, "uri", 2, 500);

        ackMessage = new AckMessageDto(1, Ack.OK, "message", true);

        pubMessage = new GenericPublicationMessageDto<LevelProductDto>(
                ProductFamily.L0_PRODUCT, new LevelProductDto("name", "keyobs",
                        ProductFamily.L0_PRODUCT));
    }
    
    @Test
    public void tesConstructor() {
        service = new LevelProductsMqiService(restTemplate, "uri", -1, 500);
        assertEquals(0, service.maxRetries);
        service = new LevelProductsMqiService(restTemplate, "uri", 20, 500);
        assertEquals(0, service.maxRetries);
    }

    /**
     * Test publish when no response from the rest server
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testPublishWhenNoResponse() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        thrown.expect(MqiPublishApiError.class);

        service.publish(pubMessage);
    }

    /**
     * Test publish when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testPublishWhenResponseKO() throws AbstractCodedException {
        doReturn(new ResponseEntity<Void>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Void>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        thrown.expect(MqiPublishApiError.class);
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.publish(pubMessage);
    }

    /**
     * Test the max retries applied before launching an exception
     * 
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

        try {
            service.publish(pubMessage);
            fail("An exception shall be raised");
        } catch (MqiPublishApiError mpee) {
            verify(restTemplate, times(2)).exchange(
                    Mockito.eq("uri/messages/level_products/publish"),
                    Mockito.eq(HttpMethod.POST),
                    Mockito.eq(
                            new HttpEntity<GenericPublicationMessageDto<LevelProductDto>>(
                                    pubMessage)),
                    Mockito.eq(Void.class));
            verifyNoMoreInteractions(restTemplate);
        }
    }

    /**
     * Test publish when the first time fails and the second works
     * 
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

        service.publish(pubMessage);
        verify(restTemplate, times(2)).exchange(
                Mockito.eq("uri/messages/level_products/publish"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<GenericPublicationMessageDto<LevelProductDto>>(
                                pubMessage)),
                Mockito.eq(Void.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test publish when the first time works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testPublish2() throws AbstractCodedException {
        doReturn(new ResponseEntity<Void>(HttpStatus.OK)).when(restTemplate)
                .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        service.publish(pubMessage);
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/messages/level_products/publish"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<GenericPublicationMessageDto<LevelProductDto>>(
                                pubMessage)),
                Mockito.eq(Void.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test publish when no response from the rest server
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAckWhenNoResponse() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        thrown.expect(MqiAckApiError.class);

        service.ack(ackMessage);
    }

    /**
     * Test publish when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAckWhenResponseKO() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        thrown.expect(MqiAckApiError.class);
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.ack(ackMessage);
    }

    /**
     * Test publish when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAck1() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Boolean>(true, HttpStatus.OK)).when(restTemplate)
                        .exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        assertTrue(service.ack(ackMessage));
        verify(restTemplate, times(2)).exchange(
                Mockito.eq("uri/messages/level_products/ack"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<AckMessageDto>(
                                ackMessage)),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test publish when the first time works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAck2() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(false,HttpStatus.OK)).when(restTemplate)
                .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        assertFalse(service.ack(ackMessage));
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/messages/level_products/ack"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<AckMessageDto>(
                                ackMessage)),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test publish when the first time works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAck3() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.OK)).when(restTemplate)
                .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        assertFalse(service.ack(ackMessage));
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/messages/level_products/ack"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<AckMessageDto>(
                                ackMessage)),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }
}
