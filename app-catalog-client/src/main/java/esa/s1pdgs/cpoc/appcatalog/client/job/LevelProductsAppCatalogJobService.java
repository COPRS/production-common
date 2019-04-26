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
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;

/**
 * Implementation of job applicative data client for LEVEL_PRODUCTS product
 * category
 * 
 * @author Viveris Technologies
 */
public class LevelProductsAppCatalogJobService
        extends AbstractAppCatalogJobService<LevelProductDto> {

    /**
     * @param restTemplate
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    public LevelProductsAppCatalogJobService(final RestTemplate restTemplate,
            final String hostUri, final int maxRetries,
            final int tempoRetryMs) {
        super(restTemplate, hostUri, maxRetries, tempoRetryMs,
                ProductCategory.LEVEL_PRODUCTS);
    }

    /**
     * @see super{@link #internalExchangeSearch(URI)}
     */
    @Override
    protected ResponseEntity<List<AppDataJobDto<LevelProductDto>>> internalExchangeSearch(
            final URI uri) {
        ResponseEntity<List<LevelProductAppDataJobDto>> response =
                restTemplate.exchange(uri, HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<LevelProductAppDataJobDto>>() {
                        });
        List<AppDataJobDto<LevelProductDto>> body = new ArrayList<>();
        List<LevelProductAppDataJobDto> responseBody = response.getBody();
        if (!CollectionUtils.isEmpty(responseBody)) {
            for (LevelProductAppDataJobDto elt : responseBody) {
                body.add(elt);
            }
        }
        return new ResponseEntity<List<AppDataJobDto<LevelProductDto>>>(body,
                response.getStatusCode());
    }

    /**
     * @see super{@link #internalExchangeNewJob(String, AppDataJobDto)}
     */
    @Override
    protected ResponseEntity<AppDataJobDto<LevelProductDto>> internalExchangeNewJob(
            final String uri, final AppDataJobDto<LevelProductDto> body) {
        ResponseEntity<LevelProductAppDataJobDto> response =
                restTemplate.exchange(uri, HttpMethod.POST,
                        new HttpEntity<AppDataJobDto<LevelProductDto>>(body),
                        LevelProductAppDataJobDto.class);

        return new ResponseEntity<AppDataJobDto<LevelProductDto>>(
                response.getBody(), response.getStatusCode());
    }

    /**
     * @see super{@link #internalExchangePatchJob(String, AppDataJobDto)}
     */
    @Override
    protected ResponseEntity<AppDataJobDto<LevelProductDto>> internalExchangePatchJob(
            final String uri, final AppDataJobDto<LevelProductDto> body) {
        ResponseEntity<LevelProductAppDataJobDto> response =
                restTemplate.exchange(uri, HttpMethod.PATCH,
                        new HttpEntity<AppDataJobDto<LevelProductDto>>(body),
                        LevelProductAppDataJobDto.class);

        return new ResponseEntity<AppDataJobDto<LevelProductDto>>(
                response.getBody(), response.getStatusCode());
    }

    /**
     * @see super{@link #internalExchangePatchTaskTableOfJob(String, AppDataJobGenerationDto)}
     */
    @Override
    protected ResponseEntity<AppDataJobDto<LevelProductDto>> internalExchangePatchTaskTableOfJob(
            final String uri, final AppDataJobGenerationDto body) {
        ResponseEntity<LevelProductAppDataJobDto> response =
                restTemplate.exchange(uri, HttpMethod.PATCH,
                        new HttpEntity<AppDataJobGenerationDto>(body),
                        LevelProductAppDataJobDto.class);

        return new ResponseEntity<AppDataJobDto<LevelProductDto>>(
                response.getBody(), response.getStatusCode());
    }

}

/**
 * @author Viveris Technologies
 */
class LevelProductAppDataJobDto extends AppDataJobDto<LevelProductDto> {

}
