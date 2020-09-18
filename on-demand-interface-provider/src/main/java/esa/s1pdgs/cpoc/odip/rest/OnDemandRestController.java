package esa.s1pdgs.cpoc.odip.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.appcatalog.common.OnDemandProcessingRequest;
import esa.s1pdgs.cpoc.odip.service.OnDemandService;

@RestController
@RequestMapping("odip/v1")
public class OnDemandRestController {
	private final OnDemandService service;

	@Autowired
	public OnDemandRestController(final OnDemandService service) {
		this.service = service;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/onDemandProcessings")
	public void submit(@RequestBody final OnDemandProcessingRequest dto) {
		service.submit(dto);
	}

}
