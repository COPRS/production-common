package esa.s1pdgs.cpoc.appcatalog.client.mqi;

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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetNbReadingApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetOffsetApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiReadApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiSendApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;

/**
 * Test the REST service ErrorService
 * 
 * @author Viveris Technologies
 */
public class AppCatalogMqiServiceTest {

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
    private AppCatalogMqiService service;

    /**
     * DTO
     */
    private AppCatMessageDto<ProductDto> message;
    private List<AppCatMessageDto<ProductDto>> messages;
    private AppCatReadMessageDto<ProductDto> readMessage;
    private AppCatSendMessageDto sendMessage;
    private ProductDto dto =
            new ProductDto("name", "keyobs", ProductFamily.L0_SLICE, "FAST");

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new AppCatalogMqiService(restTemplate, "uri", 2, 500);
        message = new AppCatMessageDto<>(ProductCategory.LEVEL_PRODUCTS, 1234, "topic", 2, 9876);
        messages = Collections.singletonList(message);
        readMessage = new AppCatReadMessageDto<ProductDto>("group","pod", false, dto);
        sendMessage = new AppCatSendMessageDto("pod", true);
    }

    @Test
    public void testConstructor() {
        assertEquals(2, service.getMaxRetries());
        assertEquals(500, service.getTempoRetryMs());
        assertEquals("uri", service.getHostUri());
        service = new AppCatalogMqiService(restTemplate, "uri", 0, 500);
        assertEquals(0, service.getMaxRetries());
        service = new AppCatalogMqiService(restTemplate, "uri", 15, 500);
        assertEquals(15, service.getMaxRetries());
    }

    /**
     * Test send when no response from the rest server
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendWhenNoResponse() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiSendApiError.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_PRODUCTS)));
        thrown.expect(
                hasProperty("uri", is("uri/mqi/level_products/1234/send")));
        thrown.expect(hasProperty("dto", is(sendMessage)));

        service.send(ProductCategory.LEVEL_PRODUCTS, 1234, sendMessage);
    }

    /**
     * Test send when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendWhenResponseKO() throws AbstractCodedException {
        doReturn(new ResponseEntity<Void>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Void>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiSendApiError.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_PRODUCTS)));
        thrown.expect(
                hasProperty("uri", is("uri/mqi/level_products/1234/send")));
        thrown.expect(hasProperty("dto", is(sendMessage)));
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.send(ProductCategory.LEVEL_PRODUCTS, 1234, sendMessage);
    }

    /**
     * Test the max retries applied before launching an exception
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMaxRetries() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        try {
            service.send(ProductCategory.LEVEL_PRODUCTS, 1234, sendMessage);
            fail("An exception shall be raised");
        } catch (AppCatalogMqiSendApiError mpee) {
            verify(restTemplate, times(2)).exchange(
                    Mockito.eq("uri/mqi/level_products/1234/send"),
                    Mockito.eq(HttpMethod.POST),
                    Mockito.eq(new HttpEntity<AppCatSendMessageDto>(sendMessage)),
                    Mockito.eq(Boolean.class));
            verifyNoMoreInteractions(restTemplate);
        }
    }

    /**
     * Test send when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSend1() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Boolean>(true, HttpStatus.OK))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        assertTrue(service.send(ProductCategory.LEVEL_PRODUCTS, 1234, sendMessage));
        verify(restTemplate, times(2)).exchange(
                Mockito.eq("uri/mqi/level_products/1234/send"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<AppCatSendMessageDto>(sendMessage)),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test send when the first time works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSend2() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(false, HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        assertFalse(service.send(ProductCategory.LEVEL_PRODUCTS, 1234, sendMessage));
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/mqi/level_products/1234/send"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<AppCatSendMessageDto>(sendMessage)),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test send when server returns an empty body
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendWhenEmptyBody() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.OK)).when(restTemplate)
                .exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        assertFalse(service.send(ProductCategory.LEVEL_PRODUCTS, 1234, sendMessage));
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/mqi/level_products/1234/send"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<AppCatSendMessageDto>(sendMessage)),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test send when no response from the rest server
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testReadWhenNoResponse() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiReadApiError.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_PRODUCTS)));
        thrown.expect(hasProperty("uri",
                is("uri/mqi/level_products/topic/2/9876/read")));
        thrown.expect(hasProperty("dto", is(readMessage)));

        service.read(ProductCategory.LEVEL_PRODUCTS, "topic", 2, 9876, readMessage);
    }

    /**
     * Test send when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testReadWhenResponseKO() throws AbstractCodedException {
        doReturn(new ResponseEntity<AppCatMessageDto<ProductDto>>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<AppCatMessageDto<ProductDto>>(
                        HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<AppCatMessageDto<ProductDto>>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiReadApiError.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_PRODUCTS)));
        thrown.expect(hasProperty("uri",
                is("uri/mqi/level_products/topic/2/9876/read")));
        thrown.expect(hasProperty("dto", is(readMessage)));
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.read(ProductCategory.LEVEL_PRODUCTS, "topic", 2, 9876, readMessage);
    }

    /**
     * Test send when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testReadWhenResponseEmpty() throws AbstractCodedException {
        doReturn(new ResponseEntity<AppCatMessageDto<ProductDto>>(HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiReadApiError.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_PRODUCTS)));
        thrown.expect(hasProperty("uri",
                is("uri/mqi/level_products/topic/2/9876/read")));
        thrown.expect(hasProperty("dto", is(readMessage)));

        service.read(ProductCategory.LEVEL_PRODUCTS, "topic", 2, 9876, readMessage);
    }

    /**
     * Test send when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRead1() throws AbstractCodedException {
        doReturn(new ResponseEntity<AppCatMessageDto<ProductDto>>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<AppCatMessageDto<ProductDto>>(message,
                        HttpStatus.OK)).when(restTemplate).exchange(
                                Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        AppCatMessageDto<ProductDto> ret = (AppCatMessageDto<ProductDto>) service.read(ProductCategory.LEVEL_PRODUCTS, "topic", 2, 9876, readMessage);
        assertEquals(ret, message);
        verify(restTemplate, times(2)).exchange(
                Mockito.eq("uri/mqi/level_products/topic/2/9876/read"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<AppCatReadMessageDto<ProductDto>>(
                                readMessage)),
                Mockito.eq(AppCatMessageDto.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test send when the first time works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRead2() throws AbstractCodedException {
        doReturn(new ResponseEntity<AppCatMessageDto<ProductDto>>(message,
                HttpStatus.OK)).when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        AppCatMessageDto<ProductDto> ret = (AppCatMessageDto<ProductDto>) service.read(ProductCategory.LEVEL_PRODUCTS, "topic", 2, 9876, readMessage);
        assertEquals(ret, message);
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/mqi/level_products/topic/2/9876/read"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<AppCatReadMessageDto<ProductDto>>(
                                readMessage)),
                Mockito.eq(AppCatMessageDto.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test send when no response from the rest server
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetEarliestOffsetWhenNoResponse()
            throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.any(URI.class),
                        Mockito.any(HttpMethod.class), Mockito.isNull(),
                        Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiGetOffsetApiError.class);
        thrown.expect(hasProperty("uri", is("uri/mqi/topic/2/earliestOffset?group=groupname")));

        service.getEarliestOffset("topic", 2, "groupname");
    }

    /**
     * Test send when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetEarliestOffsetWhenResponseKO()
            throws AbstractCodedException {
        doReturn(new ResponseEntity<Long>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Long>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Long>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.any(URI.class),
                                Mockito.any(HttpMethod.class), Mockito.isNull(),
                                Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiGetOffsetApiError.class);
        thrown.expect(hasProperty("uri", is(
                "uri/mqi/topic/2/earliestOffset?group=groupname")));
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.getEarliestOffset("topic", 2, "groupname");
    }

    /**
     * Test send when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetEarliestOffsetWhenResponseEmpty()
            throws AbstractCodedException {
        doReturn(new ResponseEntity<Long>(HttpStatus.OK)).when(restTemplate)
                .exchange(Mockito.any(URI.class), Mockito.any(HttpMethod.class),
                        Mockito.isNull(), Mockito.any(Class.class));

        assertEquals(0, service.getEarliestOffset("topic", 2, "groupname"));
    }

    /**
     * Test send when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetEarliestOffset1() throws AbstractCodedException {
        doReturn(new ResponseEntity<Long>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Long>(125L, HttpStatus.OK))
                        .when(restTemplate).exchange(Mockito.any(URI.class),
                                Mockito.any(HttpMethod.class), Mockito.isNull(),
                                Mockito.any(Class.class));

        String uriStr = "uri/mqi/topic/2/earliestOffset";
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(uriStr).queryParam("group", "groupname");
        URI expectedUri = builder.build().toUri();

        long ret = service.getEarliestOffset("topic", 2, "groupname");
        assertEquals(ret, 125L);
        verify(restTemplate, times(2)).exchange(Mockito.eq(expectedUri),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(Long.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test send when the first time works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetEarliestOffset2() throws AbstractCodedException {
        doReturn(new ResponseEntity<Long>(7596L, HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.any(URI.class),
                        Mockito.any(HttpMethod.class), Mockito.isNull(),
                        Mockito.any(Class.class));

        String uriStr = "uri/mqi/topic/2/earliestOffset";
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(uriStr).queryParam("group", "groupname");
        URI expectedUri = builder.build().toUri();

        long ret = service.getEarliestOffset("topic", 2, "groupname");
        assertEquals(ret, 7596L);
        verify(restTemplate, times(1)).exchange(Mockito.eq(expectedUri),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(Long.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test next when no response from the rest server
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAckWhenNoResponse() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class), Mockito.any(),
                        Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiAckApiError.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_PRODUCTS)));
        thrown.expect(
                hasProperty("uri", is("uri/mqi/level_products/1234/ack")));
        thrown.expect(hasProperty("dto", is(Ack.ERROR)));

        service.ack(ProductCategory.LEVEL_PRODUCTS, 1234, Ack.ERROR);
    }

    /**
     * Test next when the rest server respond an error
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
                                Mockito.any(HttpMethod.class), Mockito.any(),
                                Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiAckApiError.class);
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_PRODUCTS)));
        thrown.expect(
                hasProperty("uri", is("uri/mqi/level_products/1234/ack")));
        thrown.expect(hasProperty("dto", is(Ack.OK)));
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.ack(ProductCategory.LEVEL_PRODUCTS, 1234, Ack.OK);
    }

    /**
     * Test next when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAck1() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Boolean>(true, HttpStatus.OK))
                        .when(restTemplate).exchange(Mockito.anyString(),
                                Mockito.any(HttpMethod.class), Mockito.any(),
                                Mockito.any(Class.class));

        assertTrue(service.ack(ProductCategory.LEVEL_PRODUCTS, 1234, Ack.WARN));
        verify(restTemplate, times(2)).exchange(
                Mockito.eq("uri/mqi/level_products/1234/ack"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<Ack>(Ack.WARN)),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test next when the first time works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAck2() throws AbstractCodedException {
        doReturn(new ResponseEntity<Boolean>(false, HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class), Mockito.any(),
                        Mockito.any(Class.class));

        assertFalse(service.ack(ProductCategory.LEVEL_PRODUCTS, 1234, Ack.OK));
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/mqi/level_products/1234/ack"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<Ack>(Ack.OK)),
                Mockito.eq(Boolean.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test send when no response from the rest server
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testgetNbReadingWhenNoResponse() throws AbstractCodedException {
        doThrow(new RestClientException("rest client exception"))
                .when(restTemplate).exchange(Mockito.any(),
                        Mockito.any(HttpMethod.class),
                        Mockito.isNull(),
                        Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiGetNbReadingApiError.class);
        thrown.expect(hasProperty("uri", is("uri/mqi/topic1/nbReading?pod=pod-name")));

        service.getNbReadingMessages("topic1", "pod-name");
    }

    /**
     * Test send when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testgetNbReadingWhenResponseKO() throws AbstractCodedException {
        doReturn(new ResponseEntity<Integer>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Integer>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Integer>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.any(),
                                Mockito.any(HttpMethod.class),
                                Mockito.isNull(),
                                Mockito.any(Class.class));

        thrown.expect(AppCatalogMqiGetNbReadingApiError.class);
        thrown.expect(hasProperty("uri", is(
                "uri/mqi/topic1/nbReading?pod=pod-name")));
        thrown.expectMessage(
                containsString("" + HttpStatus.INTERNAL_SERVER_ERROR.value()));

        service.getNbReadingMessages("topic1", "pod-name");
    }

    /**
     * Test the max retries applied before launching an exception
     * 
     * @throws AbstractCodedException
     * @throws URISyntaxException 
     * @throws RestClientException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testgetNbReadingMaxRetries() throws AbstractCodedException, RestClientException, URISyntaxException {
        doReturn(new ResponseEntity<Integer>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Integer>(HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<Integer>(HttpStatus.NOT_FOUND))
                        .when(restTemplate).exchange(Mockito.any(),
                                Mockito.any(HttpMethod.class),
                                Mockito.isNull(),
                                Mockito.any(Class.class));

        try {
            service.getNbReadingMessages("topic1", "pod-name");
            fail("An exception shall be raised");
        } catch (AppCatalogMqiGetNbReadingApiError mpee) {
            verify(restTemplate, times(2)).exchange(Mockito.eq(
                    new URI("uri/mqi/topic1/nbReading?pod=pod-name")),
                    Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                    Mockito.eq(Integer.class));
            verifyNoMoreInteractions(restTemplate);
        }
    }

    /**
     * Test send when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     * @throws URISyntaxException 
     * @throws RestClientException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testgetNbReading1() throws AbstractCodedException, RestClientException, URISyntaxException {
        doReturn(new ResponseEntity<Integer>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<Integer>(15, HttpStatus.OK))
                        .when(restTemplate).exchange(Mockito.any(),
                                Mockito.any(HttpMethod.class),
                                Mockito.isNull(),
                                Mockito.any(Class.class));

        assertEquals(15, service.getNbReadingMessages("topic1", "pod-name"));
        verify(restTemplate, times(2)).exchange(Mockito
                .eq(new URI("uri/mqi/topic1/nbReading?pod=pod-name")),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(Integer.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test send when the first time works
     * 
     * @throws AbstractCodedException
     * @throws URISyntaxException 
     * @throws RestClientException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testgetNbReading2() throws AbstractCodedException, RestClientException, URISyntaxException {
        doReturn(new ResponseEntity<Integer>(0, HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.any(),
                        Mockito.any(HttpMethod.class),
                        Mockito.isNull(),
                        Mockito.any(Class.class));

        assertEquals(0, service.getNbReadingMessages("topic1", "pod-name"));
        verify(restTemplate, times(1)).exchange(Mockito
                .eq(new URI("uri/mqi/topic1/nbReading?pod=pod-name")),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(Integer.class));
        verifyNoMoreInteractions(restTemplate);
    }

    /**
     * Test send when server returns an empty body
     * 
     * @throws AbstractCodedException
     * @throws URISyntaxException 
     * @throws RestClientException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testgetNbReadingWhenEmptyBody() throws AbstractCodedException, RestClientException, URISyntaxException {
        doReturn(new ResponseEntity<Integer>(HttpStatus.OK)).when(restTemplate)
                .exchange(Mockito.any(), Mockito.any(HttpMethod.class),
                        Mockito.isNull(),
                        Mockito.any(Class.class));

        assertEquals(0, service.getNbReadingMessages("topic1", "pod-name"));
        verify(restTemplate, times(1)).exchange(Mockito
                .eq(new URI("uri/mqi/topic1/nbReading?pod=pod-name")),
                Mockito.eq(HttpMethod.GET), Mockito.eq(null),
                Mockito.eq(Integer.class));
        verifyNoMoreInteractions(restTemplate);
    }
    
    /**
     * Test next when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testNext() throws AbstractCodedException {
    	
        doReturn(new ResponseEntity<List<AppCatMessageDto<ProductDto>>>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<List<AppCatMessageDto<ProductDto>>>(messages, HttpStatus.OK))
        	.when(restTemplate).exchange(
        			Mockito.any(URI.class),
                    Mockito.any(HttpMethod.class), 
                    Mockito.isNull(),
                    Mockito.any(ParameterizedTypeReference.class)
            );
        
        final URI expectedUri = UriComponentsBuilder
                .fromUriString("uri/mqi/level_products/next")
                .queryParam("pod", "pod-name")
                .build()
                .toUri();

        final List<AppCatMessageDto<? extends AbstractDto>> result = service.next(ProductCategory.LEVEL_PRODUCTS, "pod-name");
        assertEquals(messages, result);
        verify(restTemplate, times(2)).exchange(
        		Mockito.eq(expectedUri),
                Mockito.eq(HttpMethod.GET), 
                Mockito.eq(null), 
                Mockito.eq(AppCatalogMqiService.forCategory(ProductCategory.LEVEL_PRODUCTS))
        );
        verifyNoMoreInteractions(restTemplate);
    }
}
