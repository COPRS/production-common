package esa.s1pdgs.cpoc.mqi.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiNextApiError;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublishApiError;
import esa.s1pdgs.cpoc.mqi.client.config.MqiClientConfiguration;
import esa.s1pdgs.cpoc.mqi.client.config.MqiConfigurationProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;

/**
 * Test the REST service ErrorService
 * 
 * @author Viveris Technologies
 */
public class GenericMqiClientTest {

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
    private GenericMqiClient client;

    /**
     * DTO
     */
    private AckMessageDto ackMessage;
    private GenericPublicationMessageDto<ProductionEvent> pubMessage;
    
    private GenericMessageDto<ProductionEvent> message;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        final MqiConfigurationProperties props = new MqiConfigurationProperties();
        props.setHostUri("uri");
        props.setMaxRetries(2);
        props.setTempoRetryMs(500);
        
        final MqiClientConfiguration config =  new MqiClientConfiguration(props);
        
        client = config.newGenericMqiService(() -> restTemplate);
        ackMessage = new AckMessageDto(1, Ack.OK, "message", true);

        pubMessage = new GenericPublicationMessageDto<ProductionEvent>(
                ProductFamily.L0_SLICE, new ProductionEvent("name", "keyobs",
                        ProductFamily.L0_SLICE, "NRT"));
        
        message = new GenericMessageDto<ProductionEvent>(123, "input-key",
                new ProductionEvent("name", "keyobs", ProductFamily.AUXILIARY_FILE, null));
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

        client.publish(pubMessage, ProductCategory.LEVEL_PRODUCTS);
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

        client.publish(pubMessage, ProductCategory.LEVEL_PRODUCTS);
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
            client.publish(pubMessage, ProductCategory.LEVEL_PRODUCTS);
            fail("An exception shall be raised");
        } catch (final MqiPublishApiError mpee) {
            verify(restTemplate, times(2)).exchange(
                    Mockito.eq("uri/messages/level_products/publish"),
                    Mockito.eq(HttpMethod.POST),
                    Mockito.eq(
                            new HttpEntity<GenericPublicationMessageDto<ProductionEvent>>(
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

        client.publish(pubMessage, ProductCategory.LEVEL_PRODUCTS);
        verify(restTemplate, times(2)).exchange(
                Mockito.eq("uri/messages/level_products/publish"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<GenericPublicationMessageDto<ProductionEvent>>(
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

        client.publish(pubMessage, ProductCategory.LEVEL_PRODUCTS);
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/messages/level_products/publish"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<GenericPublicationMessageDto<ProductionEvent>>(
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

        client.ack(ackMessage, ProductCategory.LEVEL_PRODUCTS);
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

        client.ack(ackMessage, ProductCategory.LEVEL_PRODUCTS);
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

        assertTrue(client.ack(ackMessage, ProductCategory.LEVEL_PRODUCTS));
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

        assertFalse(client.ack(ackMessage, ProductCategory.LEVEL_PRODUCTS));
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

        assertFalse(client.ack(ackMessage, ProductCategory.LEVEL_PRODUCTS));
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
     * Test publish when no response from the rest server
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testNextWhenNoResponse() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(
                		Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.isNull(),
                        Mockito.any(ParameterizedTypeReference.class));

        thrown.expect(MqiNextApiError.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.AUXILIARY_FILES)));

        client.next(ProductCategory.AUXILIARY_FILES);
    }

    /**
     * Test publish when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testNextWhenResponseKO() throws AbstractCodedException {
        doReturn(
                new ResponseEntity<GenericMessageDto<ProductionEvent>>(
                        HttpStatus.BAD_GATEWAY),
                new ResponseEntity<GenericMessageDto<ProductionEvent>>(
                        HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<GenericMessageDto<ProductionEvent>>(
                        HttpStatus.NOT_FOUND)).when(restTemplate).exchange(
                                Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.isNull(),
                                Mockito.any(ParameterizedTypeReference.class));

        thrown.expect(MqiNextApiError.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.AUXILIARY_FILES)));
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        client.next(ProductCategory.AUXILIARY_FILES);
    }

    /**
     * Test publish when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testNext1() throws AbstractCodedException {
        doReturn(
                new ResponseEntity<GenericMessageDto<ProductionEvent>>(
                        HttpStatus.BAD_GATEWAY),
                new ResponseEntity<GenericMessageDto<ProductionEvent>>(message,
                        HttpStatus.OK)).when(restTemplate).exchange(
                                Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.isNull(),
                                Mockito.any(ParameterizedTypeReference.class));

        final GenericMessageDto<ProductionEvent> ret = client.next(ProductCategory.AUXILIARY_FILES);
        assertEquals(message, ret);
        
    	final ResolvableType type = ResolvableType.forClassWithGenerics(
    			GenericMessageDto.class, 
    			ProductionEvent.class
    	);           
        verify(restTemplate, times(2)).exchange(
                Mockito.eq("uri/messages/auxiliary_files/next"),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(ParameterizedTypeReference.forType(type.getType())));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test publish when the first time works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testNext2() throws AbstractCodedException {
        doReturn(new ResponseEntity<GenericMessageDto<ProductionEvent>>(message, HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.isNull(),
                        Mockito.any(ParameterizedTypeReference.class));

        final GenericMessageDto<ProductionEvent> ret = client.next(ProductCategory.AUXILIARY_FILES);
        
    	final ResolvableType type = ResolvableType.forClassWithGenerics(
    			GenericMessageDto.class, 
    			ProductionEvent.class
    	);         
        
        assertEquals(message, ret);
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/messages/auxiliary_files/next"),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(ParameterizedTypeReference.forType(type.getType())));
        verifyNoMoreInteractions(restTemplate);
    }
}
