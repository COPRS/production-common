package de.werum.coprs.nativeapi.rest;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;
import de.werum.coprs.nativeapi.rest.model.stac.StacRootCatalog;
import de.werum.coprs.nativeapi.service.NativeAPIServiceImpl;

@CrossOrigin
@RestController
@RequestMapping("/stac")
public class StacRestController {
	private static final Logger LOG = LogManager.getLogger(StacRestController.class);

	@Autowired
	private NativeAPIServiceImpl nativeAPI;

	@RequestMapping(path = "/", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<StacRootCatalog> handleStacLandingPage() {
		LOG.info("Received external stac landing page request");
		
		return ResponseEntity.ok(nativeAPI.getLandingPage());
	}
	
	@RequestMapping(path = "/search", method = RequestMethod.GET, produces = "application/geo+json")
	public ResponseEntity<StacItemCollection> handleStacItemSearch(final HttpServletRequest request) {
		LOG.info("Received external query request: {}", request.toString());
		StacItemCollection result = nativeAPI.processSearchRequest(request);
		if (result != null) {
			return ResponseEntity.ok(result);			
		} 
		
		return ResponseEntity.notFound().build();
	}
}
