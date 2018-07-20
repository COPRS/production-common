package esa.s1pdgs.cpoc.appcatalog.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiEdrsSessionMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiNextApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;

/**
 * Service for managing MQI edrs sessions messages in applicative catalog (REST
 * client)
 * 
 * @author Viveris Technologies
 */
public class AppCatalogMqiEdrsSessionsService
        extends GenericAppCatalogMqiService<EdrsSessionDto> {

    /**
     * Constructor
     * 
     * @param restTemplate
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    public AppCatalogMqiEdrsSessionsService(final RestTemplate restTemplate,
            final String hostUri, final int maxRetries,
            final int tempoRetryMs) {
        super(restTemplate, ProductCategory.EDRS_SESSIONS, hostUri,
                maxRetries, tempoRetryMs);
    }

    /**
     * @see GenericAppCatalogMqiService#next()
     */
    @Override
    public List<MqiGenericMessageDto<EdrsSessionDto>> next()
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri =
                    hostUri + "/mqi/" + category.name().toLowerCase() + "/next";
            try {
                ResponseEntity<List<MqiEdrsSessionMessageDto>> response =
                        restTemplate.exchange(uri, HttpMethod.GET, null,
                                new ParameterizedTypeReference<List<MqiEdrsSessionMessageDto>>() {
                                });
                if (response.getStatusCode() == HttpStatus.OK) {
                    List<MqiEdrsSessionMessageDto> body = response.getBody();
                    if (body == null) {
                        return new ArrayList<>();
                    } else {
                        List<MqiGenericMessageDto<EdrsSessionDto>> ret =
                                new ArrayList<MqiGenericMessageDto<EdrsSessionDto>>();
                        ret.addAll(body);
                        return ret;
                    }
                } else {
                    waitOrThrow(retries,
                            new AppCatalogMqiNextApiError(category,
                                    "HTTP status code "
                                            + response.getStatusCode()),
                            "next");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new AppCatalogMqiNextApiError(category,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "next");
            }
        }
    }

    /**
     * @see GenericAppCatalogMqiService#ack(long)
     */
    @Override
    public MqiGenericMessageDto<EdrsSessionDto> ack(final long messageId)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/mqi/" + category.name().toLowerCase() + "/"
                    + messageId + "/ack";
            try {
                ResponseEntity<MqiEdrsSessionMessageDto> response =
                        restTemplate.exchange(uri, HttpMethod.POST, null,
                                MqiEdrsSessionMessageDto.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return response.getBody();
                } else {
                    waitOrThrow(retries, new AppCatalogMqiAckApiError(category,
                            uri, null,
                            "HTTP status code " + response.getStatusCode()),
                            "ack");
                }
            } catch (RestClientException rce) {
                waitOrThrow(retries, new AppCatalogMqiAckApiError(category, uri,
                        null,
                        "RestClientException occurred: " + rce.getMessage(),
                        rce), "ack");
            }
        }
    }

}
