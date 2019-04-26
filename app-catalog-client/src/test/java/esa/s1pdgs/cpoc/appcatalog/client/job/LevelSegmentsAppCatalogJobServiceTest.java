package esa.s1pdgs.cpoc.appcatalog.client.job;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;

public class LevelSegmentsAppCatalogJobServiceTest {

    /**
     * Rest template
     */
    @Mock
    protected RestTemplate restTemplate;

    /**
     * Client to test
     */
    protected AbstractAppCatalogJobService<LevelSegmentDto> client;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        client = new LevelSegmentsAppCatalogJobService(restTemplate,
                "http://localhost:8080", 3, 200);
    }

    @Test
    public void testConstructor() {
        assertEquals(3, client.getMaxRetries());
        assertEquals(200, client.getTempoRetryMs());
        assertEquals("http://localhost:8080", client.getHostUri());
        assertEquals(ProductCategory.LEVEL_SEGMENTS, client.getCategory());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearch() {
        List<LevelSegmentAppDataJobDto> list = new ArrayList<>();
        list.add(new LevelSegmentAppDataJobDto());
        list.get(0).setIdentifier(123);
        list.add(new LevelSegmentAppDataJobDto());
        list.get(1).setIdentifier(124);
        doReturn(new ResponseEntity<>(list, HttpStatus.OK)).when(restTemplate)
                .exchange(Mockito.any(), Mockito.eq(HttpMethod.GET),
                        Mockito.isNull(),
                        Mockito.any(ParameterizedTypeReference.class));

        String uriStr = "http://localhost:8080/level_segments/jobs/search";
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromUriString(uriStr);
        builder = builder.queryParam("key1", "value1");
        URI uri = builder.build().toUri();

        ResponseEntity<List<AppDataJobDto<LevelSegmentDto>>> result =
                client.internalExchangeSearch(uri);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(list, result.getBody());
        verify(restTemplate, only()).exchange(Mockito.eq(uri),
                Mockito.eq(HttpMethod.GET), Mockito.isNull(),
                Mockito.any(ParameterizedTypeReference.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchNoResult() {
        List<LevelSegmentAppDataJobDto> list = new ArrayList<>();
        list.add(new LevelSegmentAppDataJobDto());
        list.get(0).setIdentifier(123);
        list.add(new LevelSegmentAppDataJobDto());
        list.get(1).setIdentifier(124);
        doReturn(new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.any(),
                        Mockito.eq(HttpMethod.GET), Mockito.isNull(),
                        Mockito.any(ParameterizedTypeReference.class));

        String uriStr = "http://localhost:8080/level_segments/jobs/search";
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromUriString(uriStr);
        builder = builder.queryParam("key1", "value1");
        URI uri = builder.build().toUri();

        ResponseEntity<List<AppDataJobDto<LevelSegmentDto>>> result =
                client.internalExchangeSearch(uri);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().size());
        verify(restTemplate, only()).exchange(Mockito.eq(uri),
                Mockito.eq(HttpMethod.GET), Mockito.isNull(),
                Mockito.any(ParameterizedTypeReference.class));
    }

    @Test
    public void testNew() {
        LevelSegmentAppDataJobDto body = new LevelSegmentAppDataJobDto();
        body.setIdentifier(123);
        LevelSegmentAppDataJobDto expected = new LevelSegmentAppDataJobDto();
        expected.setIdentifier(123);
        doReturn(new ResponseEntity<LevelSegmentAppDataJobDto>(expected,
                HttpStatus.OK)).when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.eq(HttpMethod.POST), Mockito.any(),
                        Mockito.eq(LevelSegmentAppDataJobDto.class));

        String uri = "http://localhost:8080/level_segments/jobs/search";

        ResponseEntity<AppDataJobDto<LevelSegmentDto>> result =
                client.internalExchangeNewJob(uri, body);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expected, result.getBody());
        verify(restTemplate, only()).exchange(Mockito.eq(uri),
                Mockito.eq(HttpMethod.POST),
                Mockito.eq(
                        new HttpEntity<AppDataJobDto<LevelSegmentDto>>(body)),
                Mockito.eq(LevelSegmentAppDataJobDto.class));
    }

    @Test
    public void testPatch() {
        LevelSegmentAppDataJobDto body = new LevelSegmentAppDataJobDto();
        body.setIdentifier(123);
        LevelSegmentAppDataJobDto expected = new LevelSegmentAppDataJobDto();
        expected.setIdentifier(123);
        doReturn(new ResponseEntity<LevelSegmentAppDataJobDto>(expected,
                HttpStatus.OK)).when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.eq(HttpMethod.PATCH), Mockito.any(),
                        Mockito.eq(LevelSegmentAppDataJobDto.class));

        String uri = "http://localhost:8080/level_segments/jobs/search";

        ResponseEntity<AppDataJobDto<LevelSegmentDto>> result =
                client.internalExchangePatchJob(uri, body);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expected, result.getBody());
        verify(restTemplate, only()).exchange(Mockito.eq(uri),
                Mockito.eq(HttpMethod.PATCH),
                Mockito.eq(
                        new HttpEntity<AppDataJobDto<LevelSegmentDto>>(body)),
                Mockito.eq(LevelSegmentAppDataJobDto.class));
    }

    @Test
    public void testPatchGeneration() {
        AppDataJobGenerationDto body = new AppDataJobGenerationDto();
        body.setTaskTable("task-table");
        LevelSegmentAppDataJobDto expected = new LevelSegmentAppDataJobDto();
        expected.setIdentifier(123);
        doReturn(new ResponseEntity<LevelSegmentAppDataJobDto>(expected,
                HttpStatus.OK)).when(restTemplate).exchange(Mockito.anyString(),
                        Mockito.eq(HttpMethod.PATCH), Mockito.any(),
                        Mockito.eq(LevelSegmentAppDataJobDto.class));

        String uri = "http://localhost:8080/level_segments/jobs/search";

        ResponseEntity<AppDataJobDto<LevelSegmentDto>> result =
                client.internalExchangePatchTaskTableOfJob(uri, body);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expected, result.getBody());
        verify(restTemplate, only()).exchange(Mockito.eq(uri),
                Mockito.eq(HttpMethod.PATCH),
                Mockito.eq(new HttpEntity<AppDataJobGenerationDto>(body)),
                Mockito.eq(LevelSegmentAppDataJobDto.class));
    }

}
