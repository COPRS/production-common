package esa.s1pdgs.cpoc.errorrepo.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.errorrepo.service.ErrorRepository;

@RestController
@RequestMapping(path = "/errors")
public class ErrorRepositoryController {

	private final ErrorRepository errorRepository;
	
	public ErrorRepositoryController(@Autowired ErrorRepository errorRepository) {
		this.errorRepository = errorRepository;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/failedProcessings")
	public List<FailedProcessingDto> getFailedProcessings() {
		return errorRepository.getFailedProcessings();
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/failedProcessings/{id}")
	public FailedProcessingDto getFailedProcessingsById(@PathVariable("id") String id) {
		return errorRepository.getFailedProcessingsById(id);
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/failedProcessings/{id}/restart")
	public void restartFailedProcessing(@PathVariable("id") String id) {
		errorRepository.restartAndDeleteFailedProcessing(id);
	}
	
}
