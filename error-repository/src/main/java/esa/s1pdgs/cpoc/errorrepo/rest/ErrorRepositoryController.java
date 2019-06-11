package esa.s1pdgs.cpoc.errorrepo.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.errorrepo.service.ErrorRepository;
import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;

@RestController
@RequestMapping(path = "/errorrepo")
public class ErrorRepositoryController {

	private final ErrorRepository errorRepository;
	
	public ErrorRepositoryController(@Autowired ErrorRepository errorRepository) {
		this.errorRepository = errorRepository;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/failedProcessings")
	public List<ErrorDto> getFailedProcessings() {
		return errorRepository.getFailedProcessings();
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/failedProcessings/{id}")
	public ErrorDto getFailedProcessingsById(@PathVariable("id") String id) {
		return errorRepository.getFailedProcessingsById(id);
	}
}
