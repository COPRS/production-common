package esa.s1pdgs.cpoc.appcatalog.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiAuxiliaryFileMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiAckApiError;
import esa.s1pdgs.cpoc.common.errors.appcatalog.AppCatalogMqiNextApiError;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;

/**
 * Service for managing MQI auxiliary files messages in applicative catalog
 * (REST client)
 * 
 * @author Viveris Technologies
 */
public class AppCatalogMqiAuxiliaryFilesService
        extends GenericAppCatalogMqiService<AuxiliaryFileDto> {

    /**
     * Constructor
     * 
     * @param restTemplate
     * @param hostUri
     * @param maxRetries
     * @param tempoRetryMs
     */
    public AppCatalogMqiAuxiliaryFilesService(final RestTemplate restTemplate,
            final String hostUri, final int maxRetries,
            final int tempoRetryMs) {
        super(restTemplate, ProductCategory.AUXILIARY_FILES, hostUri,
                maxRetries, tempoRetryMs);
    }

    /**
     * @see GenericAppCatalogMqiService#next()
     */
    @Override
    public List<MqiGenericMessageDto<AuxiliaryFileDto>> next()
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri =
                    hostUri + "/mqi/" + category.name().toLowerCase() + "/next";
            try {
                ResponseEntity<List<MqiAuxiliaryFileMessageDto>> response =
                        restTemplate.exchange(uri, HttpMethod.GET, null,
                                new ParameterizedTypeReference<List<MqiAuxiliaryFileMessageDto>>() {
                                });
                if (response.getStatusCode() == HttpStatus.OK) {
                    List<MqiAuxiliaryFileMessageDto> body = response.getBody();
                    if (body == null) {
                        return new ArrayList<>();
                    } else {
                        List<MqiGenericMessageDto<AuxiliaryFileDto>> ret =
                                new ArrayList<MqiGenericMessageDto<AuxiliaryFileDto>>();
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
    public MqiGenericMessageDto<AuxiliaryFileDto> ack(final long messageId)
            throws AbstractCodedException {
        int retries = 0;
        while (true) {
            retries++;
            String uri = hostUri + "/mqi/" + category.name().toLowerCase() + "/"
                    + messageId + "/ack";
            try {
                ResponseEntity<MqiAuxiliaryFileMessageDto> response =
                        restTemplate.exchange(uri, HttpMethod.POST, null,
                                MqiAuxiliaryFileMessageDto.class);
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
