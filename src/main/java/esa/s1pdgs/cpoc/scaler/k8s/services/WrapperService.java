package esa.s1pdgs.cpoc.scaler.k8s.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import esa.s1pdgs.cpoc.scaler.k8s.model.PodLogicalStatus;
import esa.s1pdgs.cpoc.scaler.k8s.model.WrapperDesc;
import esa.s1pdgs.cpoc.scaler.k8s.model.dto.WrapperStatusDto;
import esa.s1pdgs.cpoc.scaler.k8s.model.exceptions.WrapperStatusException;
import esa.s1pdgs.cpoc.scaler.k8s.model.exceptions.WrapperStopException;

@Service
public class WrapperService {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(WrapperService.class);

    private final RestTemplate restTemplate;

    private final int port;

    private int nbretry;

    private int temporetryms;

    @Autowired
    public WrapperService(
            @Qualifier("restWrapperTemplate") final RestTemplate restTemplate,
            @Value("${wrapper.rest-api.port}") final int port,
            @Value("${wrapper.rest-api.tempo-retry-ms}") final int temporetryms,
            @Value("${wrapper.rest-api.nb-retry}") final int nbretry) {
        this.restTemplate = restTemplate;
        this.port = port;
        this.nbretry = nbretry;
        this.temporetryms = temporetryms;
    }

    public WrapperDesc getWrapperStatus(String podName, String podIp)
            throws WrapperStatusException {
        int retries = -1;
        while (retries < nbretry) {
            retries++;
            try {
                String uri =
                        "http://" + podIp + ":" + this.port + "/app/status";
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Call rest api: {}", uri);
                }

                ResponseEntity<WrapperStatusDto> response =
                        this.restTemplate.exchange(uri, HttpMethod.GET, null,
                                WrapperStatusDto.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return buildWrapperDescFromDto(response.getBody(), podName,
                            podIp);
                } else {
                    waitOrThrowError(retries,
                            new WrapperStatusException(podIp, podName,
                                    String.format("Query failed with code %s",
                                            response.getStatusCode())));
                }
            } catch (RestClientException e) {
                waitOrThrowError(retries,
                        new WrapperStatusException(podIp, podName, String
                                .format("Query failed: %s", e.getMessage()),
                                e));
            }
        }

        throw new WrapperStatusException(podIp, podName,
                "Timeout on query execution");
    }

    protected WrapperDesc buildWrapperDescFromDto(WrapperStatusDto dto,
            String podName, String podIp) throws WrapperStatusException {
        if (dto == null) {
            throw new WrapperStatusException(podIp, podName, "Null object");
        }
        WrapperDesc r = new WrapperDesc(podName);
        r.setTimeSinceLastChange(dto.getTimeSinceLastChange());
        r.setErrorCounter(dto.getErrorCounter());
        switch (dto.getStatus()) {
            case PROCESSING:
                r.setStatus(PodLogicalStatus.PROCESSING);
                break;
            case STOPPING:
                r.setStatus(PodLogicalStatus.STOPPING);
                break;
            case ERROR:
                r.setStatus(PodLogicalStatus.ERROR);
                break;
            case FATALERROR:
                r.setStatus(PodLogicalStatus.FATALERROR);
                break;
            default:
                r.setStatus(PodLogicalStatus.WAITING);
                break;
        }
        return r;
    }

    private void waitOrThrowError(int retries, WrapperStatusException raise)
            throws WrapperStatusException {
        if (retries < this.nbretry) {
            LOGGER.warn("Call rest api failed: Attempt : {} / {}", retries,
                    this.nbretry);
            try {
                Thread.sleep(this.temporetryms);
            } catch (InterruptedException e) {
                throw raise;
            }
        } else {
            throw raise;
        }
    }

    private void waitOrThrowError(int retries, WrapperStopException raise)
            throws WrapperStopException {
        if (retries < this.nbretry) {
            LOGGER.warn("Call rest api failed: Attempt : {} / {}", retries,
                    this.nbretry);
            try {
                Thread.sleep(this.temporetryms);
            } catch (InterruptedException e) {
                throw raise;
            }
        } else {
            throw raise;
        }
    }

    public void stopWrapper(String ip) throws WrapperStopException {
        int retries = -1;
        while (retries < nbretry) {
            retries++;
            try {
                String uri = "http://" + ip + ":" + this.port + "/app/stop";
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Call rest api: {}, retries: {}", uri,
                            retries);
                }

                ResponseEntity<String> response = this.restTemplate
                        .exchange(uri, HttpMethod.POST, null, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return;
                } else {
                    waitOrThrowError(retries,
                            new WrapperStopException(ip,
                                    String.format("Query failed with code %s",
                                            response.getStatusCode())));
                }
            } catch (RestClientException e) {
                waitOrThrowError(retries, new WrapperStopException(ip,
                        String.format("Query failed: %s", e.getMessage()), e));
            }
        }

        throw new WrapperStopException(ip, "Timeout on query execution");
    }
}
