package esa.s1pdgs.cpoc.prip.service.rest;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.prip.service.EdmProvider;
import esa.s1pdgs.cpoc.prip.service.ProductEntityCollectionProcessor;

@RestController
@RequestMapping(value = "/odata")
public class OdataController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdataController.class);

	@Autowired
	EdmProvider edmProvider;
	
	@RequestMapping(value = "/v1/**")
	public void process(HttpServletRequest request, HttpServletResponse response) {
		OData odata = OData.newInstance();
		ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<EdmxReference>());
		ODataHttpHandler handler = odata.createHandler(serviceMetadata);
		handler.register(new ProductEntityCollectionProcessor());

		handler.process(new HttpServletRequestWrapper(request) {
	         @Override
	         public String getServletPath() {
	            return "odata/v1"; // just the prefix up to /odata/v1, the rest is used as parameters by Olingo
	         }
	      }, response);
	}
}
