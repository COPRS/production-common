package esa.s1pdgs.cpoc.appcatalog.client.job;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobGenerationDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;

/**
 * Implementation of job applicative data client for EDRS_SESSIONS product
 * category
 * 
 * @author Viveris Technologies
 */
public class EdrsSessionsAppCatalogJobService
        extends AbstractAppCatalogJobService<EdrsSessionDto> {

    /**
     * Constructor
     * 
     * @param restTemplate
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    public EdrsSessionsAppCatalogJobService(final RestTemplate restTemplate,
            final String hostUri, final int maxRetries,
            final int tempoRetryMs) {
        super(restTemplate, hostUri, maxRetries, tempoRetryMs,
                ProductCategory.EDRS_SESSIONS);
    }

    /**
     * @see super{@link #internalExchangeSearch(URI)}
     */
    @Override
    protected ResponseEntity<List<AppDataJobDto<EdrsSessionDto>>> internalExchangeSearch(
            final URI uri) {
        ResponseEntity<List<EdrsSessionAppDataJobDto>> response =
                restTemplate.exchange(uri, HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<EdrsSessionAppDataJobDto>>() {
                        });
        List<AppDataJobDto<EdrsSessionDto>> body = new ArrayList<>();
        List<EdrsSessionAppDataJobDto> responseBody = response.getBody();
        if (!CollectionUtils.isEmpty(responseBody)) {
            for (EdrsSessionAppDataJobDto elt : responseBody) {
                body.add(elt);
            }
        }
        return new ResponseEntity<List<AppDataJobDto<EdrsSessionDto>>>(body,
                response.getStatusCode());
    }

    /**
     * @see super{@link #internalExchangeNewJob(String, AppDataJobDto)}
     */
    @Override
    protected ResponseEntity<AppDataJobDto<EdrsSessionDto>> internalExchangeNewJob(
            final String uri, final AppDataJobDto<EdrsSessionDto> body) {
        ResponseEntity<EdrsSessionAppDataJobDto> response =
                restTemplate.exchange(uri, HttpMethod.POST,
                        new HttpEntity<AppDataJobDto<EdrsSessionDto>>(body),
                        EdrsSessionAppDataJobDto.class);

        return new ResponseEntity<AppDataJobDto<EdrsSessionDto>>(
                response.getBody(), response.getStatusCode());
    }

    /**
     * @see super{@link #internalExchangePatchJob(String, AppDataJobDto)}
     */
    @Override
    protected ResponseEntity<AppDataJobDto<EdrsSessionDto>> internalExchangePatchJob(
            final String uri, final AppDataJobDto<EdrsSessionDto> body) {
        ResponseEntity<EdrsSessionAppDataJobDto> response =
                restTemplate.exchange(uri, HttpMethod.PATCH,
                        new HttpEntity<AppDataJobDto<EdrsSessionDto>>(body),
                        EdrsSessionAppDataJobDto.class);

        return new ResponseEntity<AppDataJobDto<EdrsSessionDto>>(
                response.getBody(), response.getStatusCode());
    }

    /**
     * @see super{@link #internalExchangePatchTaskTableOfJob(String, AppDataJobGenerationDto)}
     */
    @Override
    protected ResponseEntity<AppDataJobDto<EdrsSessionDto>> internalExchangePatchTaskTableOfJob(
            final String uri, final AppDataJobGenerationDto body) {
        ResponseEntity<EdrsSessionAppDataJobDto> response =
                restTemplate.exchange(uri, HttpMethod.PATCH,
                        new HttpEntity<AppDataJobGenerationDto>(body),
                        EdrsSessionAppDataJobDto.class);

        return new ResponseEntity<AppDataJobDto<EdrsSessionDto>>(
                response.getBody(), response.getStatusCode());
    }

}

/**
 * @author Viveris Technologies
 */
class EdrsSessionAppDataJobDto extends AppDataJobDto<EdrsSessionDto> {

}
