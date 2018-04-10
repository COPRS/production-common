package fr.viveris.s1pdgs.scaler.k8s.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.viveris.s1pdgs.scaler.k8s.model.PodLogicalStatus;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.dto.WrapperStatusDto;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperException;

@Service
public class WrapperService {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(WrapperService.class);

	private final RestTemplate restTemplate;

	private final int port;

	@Autowired
	public WrapperService(@Qualifier("restWrapperTemplate") final RestTemplate restTemplate,
			@Value("${wrapper.rest-api.port}") final int port) {
		this.restTemplate = restTemplate;
		this.port = port;
	}

	public WrapperDesc getWrapperStatus(String podName, String podIp) throws WrapperException {
		try {
			String uri = "http://" + podIp + ":" + this.port + "/wrapper/status";
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Call rest api: {}", uri);
			}

			ResponseEntity<WrapperStatusDto> response = this.restTemplate.exchange(uri, HttpMethod.GET, null,
					WrapperStatusDto.class);
			if (response.getStatusCode() != HttpStatus.OK) {
				String message = String.format("Query for retrieving %s wrapper status failed with code %s", podName,
						response.getStatusCode());
				throw new WrapperException(message);
			}

			WrapperStatusDto dto = response.getBody();
			if (dto == null) {
				String message = String.format("Null status retrieved for wrapper %s", podName);
				throw new WrapperException(message);
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
			case WAITING:
				r.setStatus(PodLogicalStatus.WAITING);
				break;
			default:
				String message = String.format("Invalid logical status retrieved for wrapper %s: %s", podName,
						dto.getStatus());
				throw new WrapperException(message);
			}

			return r;
		} catch (RestClientException e) {
			String message = String.format("Cannot retrieve status for wrapper %s: %s", podName, e.getMessage());
			throw new WrapperException(message, e);
		}
	}

	public void stopWrapper(String ip) throws WrapperException {
		try {
			String uri = "http://" + ip + ":" + this.port + "/wrapper/stop";
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Call rest api: {}", uri);
			}

			ResponseEntity<String> response = this.restTemplate.exchange(uri, HttpMethod.POST, null, String.class);
			if (response.getStatusCode() != HttpStatus.OK) {
				String message = String.format("Query for retrieving %s wrapper status failed with code %s", ip,
						response.getStatusCode());
				throw new WrapperException(message);
			}
		} catch (RestClientException e) {
			String message = String.format("Cannot retrieve status for wrapper %s: %s", ip, e.getMessage());
			throw new WrapperException(message, e);
		}
	}
}
