package esa.s1pdgs.cpoc.appcatalog.client;

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
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiLightMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiSendMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiGetOffsetApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiReadApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiSendApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;

/**
 * Test the REST service ErrorService
 * 
 * @author Viveris Technologies
 */
public class GenericAppCatalogMqiServiceTest {

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
    private AppCatalogMqiLevelProductsService service;

    /**
     * DTO
     */
    private MqiLightMessageDto lightMessage;
    private MqiGenericReadMessageDto<LevelProductDto> readMessage;
    private MqiSendMessageDto sendMessage;
    private LevelProductDto dto =
            new LevelProductDto("name", "keyobs", ProductFamily.L0_PRODUCT);

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        service = new AppCatalogMqiLevelProductsService(restTemplate, "uri", 2,
                500);

        lightMessage = new MqiLightMessageDto(ProductCategory.LEVEL_PRODUCTS,
                1234, "topic", 2, 9876);

        readMessage = new MqiGenericReadMessageDto<LevelProductDto>("group",
                "pod", false, dto);

        sendMessage = new MqiSendMessageDto("pod", true);
    }

    @Test
    public void tesConstructor() {
        service = new AppCatalogMqiLevelProductsService(restTemplate, "uri", -1,
                500);
        assertEquals(0, service.maxRetries);
        service = new AppCatalogMqiLevelProductsService(restTemplate, "uri", 21,
                500);
        assertEquals(0, service.maxRetries);
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

        service.send(1234, sendMessage);
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

        service.send(1234, sendMessage);
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
            service.send(1234, sendMessage);
            fail("An exception shall be raised");
        } catch (AppCatalogMqiSendApiError mpee) {
            verify(restTemplate, times(2)).exchange(
                    Mockito.eq("uri/mqi/level_products/1234/send"),
                    Mockito.eq(HttpMethod.POST),
                    Mockito.eq(new HttpEntity<MqiSendMessageDto>(sendMessage)),
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

        assertTrue(service.send(1234, sendMessage));
        verify(restTemplate, times(2)).exchange(
                Mockito.eq("uri/mqi/level_products/1234/send"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<MqiSendMessageDto>(sendMessage)),
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

        assertFalse(service.send(1234, sendMessage));
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/mqi/level_products/1234/send"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<MqiSendMessageDto>(sendMessage)),
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
        doReturn(new ResponseEntity<Boolean>(HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        assertFalse(service.send(1234, sendMessage));
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/mqi/level_products/1234/send"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(new HttpEntity<MqiSendMessageDto>(sendMessage)),
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

        service.read("topic", 2, 9876, readMessage);
    }

    /**
     * Test send when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testReadWhenResponseKO() throws AbstractCodedException {
        doReturn(new ResponseEntity<MqiLightMessageDto>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<MqiLightMessageDto>(
                        HttpStatus.INTERNAL_SERVER_ERROR),
                new ResponseEntity<MqiLightMessageDto>(HttpStatus.NOT_FOUND))
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

        service.read("topic", 2, 9876, readMessage);
    }

    /**
     * Test send when the rest server respond an error
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testReadWhenResponseEmpty() throws AbstractCodedException {
        doReturn(new ResponseEntity<MqiLightMessageDto>(HttpStatus.OK))
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

        service.read("topic", 2, 9876, readMessage);
    }

    /**
     * Test send when the first time fails and the second works
     * 
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRead1() throws AbstractCodedException {
        doReturn(new ResponseEntity<MqiLightMessageDto>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<MqiLightMessageDto>(lightMessage,
                        HttpStatus.OK)).when(restTemplate).exchange(
                                Mockito.anyString(),
                                Mockito.any(HttpMethod.class),
                                Mockito.any(HttpEntity.class),
                                Mockito.any(Class.class));

        MqiLightMessageDto ret = service.read("topic", 2, 9876, readMessage);
        assertEquals(ret, lightMessage);
        verify(restTemplate, times(2)).exchange(
                Mockito.eq("uri/mqi/level_products/topic/2/9876/read"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<MqiGenericReadMessageDto<LevelProductDto>>(
                                readMessage)),
                Mockito.eq(MqiLightMessageDto.class));
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
        doReturn(new ResponseEntity<MqiLightMessageDto>(lightMessage,
                HttpStatus.OK)).when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.any(HttpMethod.class),
                        Mockito.any(HttpEntity.class),
                        Mockito.any(Class.class));

        MqiLightMessageDto ret = service.read("topic", 2, 9876, readMessage);
        assertEquals(ret, lightMessage);
        verify(restTemplate, times(1)).exchange(
                Mockito.eq("uri/mqi/level_products/topic/2/9876/read"),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<MqiGenericReadMessageDto<LevelProductDto>>(
                                readMessage)),
                Mockito.eq(MqiLightMessageDto.class));
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
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_PRODUCTS)));
        thrown.expect(hasProperty("uri", is(
                "uri/mqi/level_products/topic/2/earliestOffset?group=groupname")));

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
        thrown.expect(
                hasProperty("category", is(ProductCategory.LEVEL_PRODUCTS)));
        thrown.expect(hasProperty("uri", is(
                "uri/mqi/level_products/topic/2/earliestOffset?group=groupname")));
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

        String uriStr = "uri/mqi/level_products/topic/2/earliestOffset";
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

        String uriStr = "uri/mqi/level_products/topic/2/earliestOffset";
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
}
