package esa.s1pdgs.cpoc.appcatalog.client.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobNewApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobPatchApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobPatchGenerationApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogJobSearchApiError;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public class AbstractAppCatalogJobServiceTest {

    /**
     * Rest template
     */
    @Mock
    protected RestTemplate restTemplate;

    /**
     * Client to test
     */
    protected AbstractAppCatalogJobServiceImpl client;

    /**
     * Client to test
     */
    protected AbstractAppCatalogJobServiceImpl clientError;

    /**
     * Initialization
     */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        client = new AbstractAppCatalogJobServiceImpl(restTemplate, 4);
        clientError = new AbstractAppCatalogJobServiceImpl(restTemplate, 3);
    }

    @Test
    public void tesConstructor() {
        assertEquals(3, clientError.getMaxRetries());
        assertEquals(200, clientError.getTempoRetryMs());
        assertEquals("http://localhost:8080", clientError.getHostUri());
        assertEquals(ProductCategory.LEVEL_PRODUCTS, clientError.getCategory());

        clientError = new AbstractAppCatalogJobServiceImpl(restTemplate, -1);
        assertEquals(0, clientError.getMaxRetries());

        clientError = new AbstractAppCatalogJobServiceImpl(restTemplate, 21);
        assertEquals(0, clientError.getMaxRetries());
    }

    @Test(expected = AppCatalogJobSearchApiError.class)
    public void testSearchWhenError() throws AbstractCodedException {
        Map<String, String> filters = new HashMap<>();
        filters.put("filter1", "value1");
        filters.put("filter2", "value2");
        clientError.search(filters);
    }

    @Test
    public void testSearch() throws AbstractCodedException {
        Map<String, String> filters = new HashMap<>();
        filters.put("filter1", "value1");
        filters.put("filter2", "value2");
        client.search(filters);
        assertEquals(4, client.getCounterSearch());
        assertEquals(0, client.getCounterNew());
        assertEquals(0, client.getCounterPatch());
        assertEquals(0, client.getCounterPatchGen());
        assertTrue(client.getLastUri().startsWith(
                "http://localhost:8080/level_products/jobs/search?"));
        assertTrue(client.getLastUri().contains("filter1=value1"));
        assertTrue(client.getLastUri().contains("filter2=value2"));
    }

    @Test
    public void testfindByMessagesIdentifier() throws AbstractCodedException {
        client.findByMessagesIdentifier(12L);
        assertEquals(4, client.getCounterSearch());
        assertEquals(0, client.getCounterNew());
        assertEquals(0, client.getCounterPatch());
        assertEquals(0, client.getCounterPatchGen());
        assertTrue(client.getLastUri().startsWith(
                "http://localhost:8080/level_products/jobs/search?"));
        assertTrue(client.getLastUri().contains("messages.identifier=12"));
    }

    @Test
    public void testfindByPodAndState() throws AbstractCodedException {
        client.findByPodAndState("pod-name", AppDataJobDtoState.DISPATCHING);
        assertEquals(4, client.getCounterSearch());
        assertEquals(0, client.getCounterNew());
        assertEquals(0, client.getCounterPatch());
        assertEquals(0, client.getCounterPatchGen());
        assertTrue(client.getLastUri().startsWith(
                "http://localhost:8080/level_products/jobs/search?"));
        assertTrue(client.getLastUri().contains("pod=pod-name"));
        assertTrue(client.getLastUri().contains("state=DISPATCHING"));
    }

    @Test
    public void testfindNByPodAndGenerationTaskTableWithNotSentGeneration()
            throws AbstractCodedException {
        client.findNByPodAndGenerationTaskTableWithNotSentGeneration("pod-name",
                "task-table");
        assertEquals(4, client.getCounterSearch());
        assertEquals(0, client.getCounterNew());
        assertEquals(0, client.getCounterPatch());
        assertEquals(0, client.getCounterPatchGen());
        assertTrue(client.getLastUri().startsWith(
                "http://localhost:8080/level_products/jobs/search?"));
        assertTrue(client.getLastUri().contains("pod=pod-name"));
        assertTrue(client.getLastUri()
                .contains("generations.taskTable=task-table"));
        assertTrue(client.getLastUri().contains("generations.state[neq]=SENT"));
        assertTrue(client.getLastUri()
                .contains("[orderByAsc]=generations.lastUpdateDate"));
    }

    private AppDataJobDto<String> buildJob() {
        AppDataJobDto<String> job = new AppDataJobDto<>();
        job.setIdentifier(142);
        job.setState(AppDataJobDtoState.DISPATCHING);
        
        AppDataJobProductDto product = new AppDataJobProductDto();
        product.setProductName("toto");
        job.setProduct(product);
        
        GenericMessageDto<String> message1 = new GenericMessageDto<String>(1, "key1", "body1");
        GenericMessageDto<String> message2 = new GenericMessageDto<String>(2, "key2", "body2");
        job.setMessages(Arrays.asList(message1, message2));
        
        AppDataJobGenerationDto gen1 = new AppDataJobGenerationDto();
        gen1.setTaskTable("tasktable1");
        gen1.setState(AppDataJobGenerationDtoState.INITIAL);
        AppDataJobGenerationDto gen2 = new AppDataJobGenerationDto();
        gen2.setTaskTable("tasktable2");
        gen2.setState(AppDataJobGenerationDtoState.READY);
        job.setGenerations(Arrays.asList(gen1, gen2));
        return job;
    }

    @Test(expected = AppCatalogJobNewApiError.class)
    public void testNewWhenError() throws AbstractCodedException {
        AppDataJobDto<String> job = buildJob();
        clientError.newJob(job);
    }

    @Test
    public void testNew() throws AbstractCodedException {
        AppDataJobDto<String> job = buildJob();
        AppDataJobDto<String> result = client.newJob(job);
        assertEquals(job, result);
        assertTrue(client.getLastUri()
                .equals("http://localhost:8080/level_products/jobs"));
        assertEquals(0, client.getCounterSearch());
        assertEquals(4, client.getCounterNew());
        assertEquals(0, client.getCounterPatch());
        assertEquals(0, client.getCounterPatchGen());
    }

    @Test(expected = AppCatalogJobPatchApiError.class)
    public void testPatchWhenError() throws AbstractCodedException {
        AppDataJobDto<String> job = buildJob();
        clientError.patchJob(job.getIdentifier(), job, true, true, true);
    }

    @Test
    public void testPatch() throws AbstractCodedException {
        AppDataJobDto<String> job = buildJob();
        AppDataJobDto<String> result =
                client.patchJob(job.getIdentifier(), job, true, true, true);
        assertEquals(job, result);
        assertTrue(client.getLastUri()
                .equals("http://localhost:8080/level_products/jobs/142"));
        assertEquals(0, client.getCounterSearch());
        assertEquals(0, client.getCounterNew());
        assertEquals(4, client.getCounterPatch());
        assertEquals(0, client.getCounterPatchGen());
    }

    @Test
    public void testPatchNotMessages() throws AbstractCodedException {
        AppDataJobDto<String> job = buildJob();
        AppDataJobDto<String> result =
                client.patchJob(job.getIdentifier(), job, false, true, true);
        assertEquals(0, result.getMessages().size());
        assertEquals(job.getProduct(), result.getProduct());
        assertEquals(job.getGenerations(), result.getGenerations());
        assertTrue(client.getLastUri()
                .equals("http://localhost:8080/level_products/jobs/142"));
        assertEquals(0, client.getCounterSearch());
        assertEquals(0, client.getCounterNew());
        assertEquals(4, client.getCounterPatch());
        assertEquals(0, client.getCounterPatchGen());
    }

    @Test
    public void testPatchNoProducts() throws AbstractCodedException {
        AppDataJobDto<String> job = buildJob();
        AppDataJobDto<String> result =
                client.patchJob(job.getIdentifier(), job, true, false, true);
        assertEquals(job.getMessages(), result.getMessages());
        assertNull(result.getProduct());
        assertEquals(job.getGenerations(), result.getGenerations());
        assertTrue(client.getLastUri()
                .equals("http://localhost:8080/level_products/jobs/142"));
        assertEquals(0, client.getCounterSearch());
        assertEquals(0, client.getCounterNew());
        assertEquals(4, client.getCounterPatch());
        assertEquals(0, client.getCounterPatchGen());
    }

    @Test
    public void testPatchNoGeneration() throws AbstractCodedException {
        AppDataJobDto<String> job = buildJob();
        AppDataJobDto<String> result =
                client.patchJob(job.getIdentifier(), job, true, true, false);
        assertEquals(0, result.getGenerations().size());
        assertEquals(job.getProduct(), result.getProduct());
        assertEquals(job.getMessages(), result.getMessages());
        assertTrue(client.getLastUri()
                .equals("http://localhost:8080/level_products/jobs/142"));
        assertEquals(0, client.getCounterSearch());
        assertEquals(0, client.getCounterNew());
        assertEquals(4, client.getCounterPatch());
        assertEquals(0, client.getCounterPatchGen());
    }

    @Test
    public void testPatchNoMessagesNorGeneration() throws AbstractCodedException {
        AppDataJobDto<String> job = buildJob();
        AppDataJobDto<String> result =
                client.patchJob(job.getIdentifier(), job, false, true, false);
        assertEquals(0, result.getMessages().size());
        assertEquals(job.getProduct(), result.getProduct());
        assertEquals(0, result.getGenerations().size());
        assertTrue(client.getLastUri()
                .equals("http://localhost:8080/level_products/jobs/142"));
        assertEquals(0, client.getCounterSearch());
        assertEquals(0, client.getCounterNew());
        assertEquals(4, client.getCounterPatch());
        assertEquals(0, client.getCounterPatchGen());
    }

    @Test(expected = AppCatalogJobPatchGenerationApiError.class)
    public void testPatchTaskTableWhenError() throws AbstractCodedException {
        AppDataJobDto<String> job = buildJob();
        clientError.patchTaskTableOfJob(job.getIdentifier(),
                job.getGenerations().get(0).getTaskTable(),
                AppDataJobGenerationDtoState.SENT);
    }

    @Test
    public void testPatchTaskTable() throws AbstractCodedException {
        AppDataJobDto<String> job = buildJob();
        AppDataJobDto<String> result = client.patchTaskTableOfJob(
                job.getIdentifier(), "tasktable2",
                AppDataJobGenerationDtoState.SENT);
        assertEquals(AppDataJobGenerationDtoState.SENT, result.getGenerations().get(0).getState());
        assertEquals("tasktable2", result.getGenerations().get(0).getTaskTable());
        assertTrue(client.getLastUri()
                .equals("http://localhost:8080/level_products/jobs/142/generations/tasktable2"));
        assertEquals(0, client.getCounterSearch());
        assertEquals(0, client.getCounterNew());
        assertEquals(0, client.getCounterPatch());
        assertEquals(4, client.getCounterPatchGen());
    }

}

class AbstractAppCatalogJobServiceImpl
        extends AbstractAppCatalogJobService<String> {

    private int counterSearch = 0;
    private int counterNew = 0;
    private int counterPatch = 0;
    private int counterPatchGen = 0;
    private String lastUri;

    public AbstractAppCatalogJobServiceImpl(RestTemplate restTemplate,
            int maxRetries) {
        super(restTemplate, "http://localhost:8080", maxRetries, 200,
                ProductCategory.LEVEL_PRODUCTS);
    }

    @Override
    protected ResponseEntity<List<AppDataJobDto<String>>> internalExchangeSearch(
            URI uri) {
        lastUri = uri.toString();
        counterSearch++;
        switch (counterSearch) {
            case 1:
                return new ResponseEntity<List<AppDataJobDto<String>>>(
                        HttpStatus.INTERNAL_SERVER_ERROR);
            case 2:
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND,
                        "HttpClientErrorException error");
            case 3:
                throw new RestClientException("RestClientException error");
            case 4:
                return new ResponseEntity<List<AppDataJobDto<String>>>(
                        HttpStatus.OK);
        }
        return null;
    }

    /**
     * return the body to check
     */
    @Override
    protected ResponseEntity<AppDataJobDto<String>> internalExchangeNewJob(
            String uri, AppDataJobDto<String> body) {
        lastUri = uri;
        counterNew++;
        switch (counterNew) {
            case 1:
                return new ResponseEntity<AppDataJobDto<String>>(
                        HttpStatus.INTERNAL_SERVER_ERROR);
            case 2:
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND,
                        "HttpClientErrorException error");
            case 3:
                throw new RestClientException("RestClientException error");
            case 4:
                return new ResponseEntity<AppDataJobDto<String>>(body,
                        HttpStatus.OK);
        }
        return null;
    }

    /**
     * return the body to check
     */
    @Override
    protected ResponseEntity<AppDataJobDto<String>> internalExchangePatchJob(
            String uri, AppDataJobDto<String> body) {
        lastUri = uri;
        counterPatch++;
        switch (counterPatch) {
            case 1:
                return new ResponseEntity<AppDataJobDto<String>>(
                        HttpStatus.INTERNAL_SERVER_ERROR);
            case 2:
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND,
                        "HttpClientErrorException error");
            case 3:
                throw new RestClientException("RestClientException error");
            case 4:
                return new ResponseEntity<>(body, HttpStatus.OK);
        }
        return null;
    }

    /**
     * return the body to check
     */
    @Override
    protected ResponseEntity<AppDataJobDto<String>> internalExchangePatchTaskTableOfJob(
            String uri, AppDataJobGenerationDto body) {
        lastUri = uri;
        counterPatchGen++;
        switch (counterPatchGen) {
            case 1:
                return new ResponseEntity<AppDataJobDto<String>>(
                        HttpStatus.INTERNAL_SERVER_ERROR);
            case 2:
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND,
                        "HttpClientErrorException error");
            case 3:
                throw new RestClientException("RestClientException error");
            case 4:
                AppDataJobDto<String> ret = new AppDataJobDto<>();
                ret.setGenerations(Arrays.asList(body));
                return new ResponseEntity<AppDataJobDto<String>>(ret,
                        HttpStatus.OK);
        }
        return null;
    }

    /**
     * @return the counterSearch
     */
    public int getCounterSearch() {
        return counterSearch;
    }

    /**
     * @param counterSearch
     *            the counterSearch to set
     */
    public void setCounterSearch(int counterSearch) {
        this.counterSearch = counterSearch;
    }

    /**
     * @return the counterNew
     */
    public int getCounterNew() {
        return counterNew;
    }

    /**
     * @param counterNew
     *            the counterNew to set
     */
    public void setCounterNew(int counterNew) {
        this.counterNew = counterNew;
    }

    /**
     * @return the counterPatch
     */
    public int getCounterPatch() {
        return counterPatch;
    }

    /**
     * @param counterPatch
     *            the counterPatch to set
     */
    public void setCounterPatch(int counterPatch) {
        this.counterPatch = counterPatch;
    }

    /**
     * @return the counterPatchGen
     */
    public int getCounterPatchGen() {
        return counterPatchGen;
    }

    /**
     * @param counterPatchGen
     *            the counterPatchGen to set
     */
    public void setCounterPatchGen(int counterPatchGen) {
        this.counterPatchGen = counterPatchGen;
    }

    /**
     * @return the lastUri
     */
    public String getLastUri() {
        return lastUri;
    }

    /**
     * @param lastUri
     *            the lastUri to set
     */
    public void setLastUri(String lastUri) {
        this.lastUri = lastUri;
    }

}
