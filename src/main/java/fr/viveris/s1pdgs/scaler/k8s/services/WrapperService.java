package fr.viveris.s1pdgs.scaler.k8s.services;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperStatusException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperStopException;

@Service
public class WrapperService {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(WrapperService.class);

	private final RestTemplate restTemplate;

	private final int port;
	
	private int nbretry;
	
	private int temporetryms;

	@Autowired
	public WrapperService(@Qualifier("restWrapperTemplate") final RestTemplate restTemplate,
			@Value("${wrapper.rest-api.port}") final int port,
			@Value("${wrapper.rest-api.tempo-retry-ms}") final int temporetryms,
			@Value("${wrapper.rest-api.nb-retry}") final int nbretry) {
		this.restTemplate = restTemplate;
		this.port = port;
		this.nbretry = nbretry;
		this.temporetryms = temporetryms;
	}

	public WrapperDesc getWrapperStatus(String podName, String podIp) throws WrapperStatusException {
		for (int retries = 0;; retries++) {
			try {
				String uri = "http://" + podIp + ":" + this.port + "/wrapper/status";
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Call rest api: {}", uri);
				}
	
				ResponseEntity<WrapperStatusDto> response = this.restTemplate.exchange(uri, HttpMethod.GET, null,
						WrapperStatusDto.class);
				if (response.getStatusCode() != HttpStatus.OK) {
					if (retries < this.nbretry){
						LOGGER.warn("Call rest api failed: Attempt : {} / {}", retries, this.nbretry);
						try {
							Thread.sleep(this.temporetryms);
						} catch (InterruptedException e) {
							throw new WrapperStatusException(podIp, podName, e.getMessage(), e);
						}
						continue;
					}
					else {
						throw new WrapperStatusException(podIp, podName,
								String.format("Query failed with code %s", response.getStatusCode()));
					}
					
				}
				else {
					WrapperStatusDto dto = response.getBody();
					if (dto == null) {
						throw new WrapperStatusException(podIp, podName, "Null status");
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
						throw new WrapperStatusException(podIp, podName,
								String.format("Invalid logical status %s", dto.getStatus()));
					}
		
					return r;
				}
			} catch (RestClientException e) {
				if (retries < this.nbretry)
				{
					LOGGER.warn("Call rest api failed: Attempt : {} / {}", retries, this.nbretry);
					try {
						Thread.sleep(this.temporetryms);
					} catch (InterruptedException e1) {
						throw new WrapperStatusException(podIp, podName, e1.getMessage(), e1);
					}
					continue;
				}
				else {
					throw new WrapperStatusException(podIp, podName, e.getMessage(), e);
				}
			}
		}
	}

	public void stopWrapper(String ip) throws WrapperStopException {
		for (int retries = 0;; retries++) {
			try {
				String uri = "http://" + ip + ":" + this.port + "/wrapper/stop";
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Call rest api: {}, retries: {}", uri, retries);
				}
	
				ResponseEntity<String> response = this.restTemplate.exchange(uri, HttpMethod.POST, null, String.class);
				if (response.getStatusCode() != HttpStatus.OK) {
					if (retries < this.nbretry)
					{
						LOGGER.warn("Call rest api failed: Attempt : {} / {}", retries, this.nbretry);
						try {
							Thread.sleep(this.temporetryms);
						} catch (InterruptedException e) {
							throw new WrapperStopException(ip, e.getMessage(), e);
						}
						continue;
					}
					else {
						throw new WrapperStopException(ip, String.format("Queryfailed with code %s", response.getStatusCode()));
					}
				}
				else {
					return;
				}
			} catch (RestClientException e) {
				if (retries < this.nbretry)
				{
					LOGGER.warn("Call rest api failed: Attempt : {} / {}", retries, this.nbretry);
					try {
						Thread.sleep(this.temporetryms);
					} catch (InterruptedException e1) {
						throw new WrapperStopException(ip, e1.getMessage(), e1);
					}
					continue;
				}
				else {
					throw new WrapperStopException(ip, e.getMessage(), e);
				}
			}
		}
	}
}
