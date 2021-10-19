package esa.s1pdgs.cpoc.prip.frontend.service.rest;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.ProductEntityCollectionProcessor;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.ProductEntityProcessor;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;

@RestController
@RequestMapping(value = "/odata")
public class OdataController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OdataController.class);

	@Autowired
	private EdmProvider edmProvider;
	
	@Autowired
	private PripMetadataRepository pripMetadataRepository;
	
	@Autowired
	private ObsClient obsClient;
	
	@Value("${prip-worker.download-url-expiration-time-in-seconds:600}")
	private long downloadUrlExpirationTimeInSeconds;
	
	@RequestMapping(value = "/v1/**")
	public void process(HttpServletRequest request, HttpServletResponse response) {
		String queryParams = request.getQueryString() == null ? "" : "?" + request.getQueryString();
		LOGGER.info("Received HTTP request for URL: {}{}", request.getRequestURL().toString(), queryParams);		
		OData odata = OData.newInstance();
		ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<EdmxReference>());
		ODataHttpHandler handler = odata.createHandler(serviceMetadata);
		handler.register(new ProductEntityProcessor(pripMetadataRepository, obsClient, downloadUrlExpirationTimeInSeconds));
		handler.register(new ProductEntityCollectionProcessor(pripMetadataRepository));

		handler.process(new HttpServletRequestWrapper(request) {
	         @Override
	         public String getServletPath() {
	            return "odata/v1"; // just the prefix up to /odata/v1, the rest is used as parameters by Olingo
	         }
	         
	         /**
	          * @see javax.servlet.http.HttpServletRequestWrapper#getQueryString()
	          */
	         @Override
	         public String getQueryString() {
	            // normalise query string
	            return handleGeometricRequests(super.getQueryString());
	         }
	      }, response);
	}
	
	   /**
	    * Normalised geometric query string.
	    *
	    * @param Raw input query string
	    * @return Normalised query string
	    */
	   protected String handleGeometricRequests(String queryString) {
	      //$filter=intersects%28Footprint,SRID=4326;POLYGON%28%2870.99733072142419%20-20.198077715931756,%2030.396664557989194%20-20.549640215931756,%2030.035907414837293%2030.69059415906824,%2071.74130413670193%2040.62223478406824,%2070.99733072142419%20-20.198077715931756%29%29%29

	      if(queryString != null) {
//	         // use FQN for function
//	         queryString = queryString.replaceFirst("(\\$filter=.*)(intersects)(\\(|%28)", "$1OData.CSC.Intersects$3");
//	         
//	         // use parameterName for first argument
//	         queryString = queryString.replaceFirst("(\\$filter=.*OData\\.CSC\\.Intersects(?:\\(|%28))([^=,]+),", "$1geo_property=$2,");
//	         
//	         // use single ' around the GML
//	         queryString = queryString.replaceFirst("(\\$filter=.*OData\\.CSC\\.Intersects(?:\\(|%28)[^,]+,)(SRID.*)(\\)|%29)", "$1%27$2%27$3");
//	         
//	         // add geography
//	         queryString = queryString.replaceFirst("(\\$filter=.*OData\\.CSC\\.Intersects(?:\\(|%28)[^,]+,)((?:'|%27)SRID.*)", "$1geography$2");
//	         
//	         // use parameterName for second argument
//	         queryString = queryString.replaceFirst("(\\$filter=.*OData\\.CSC\\.Intersects(?:\\(|%28)[^,]+,)(geography)", "$1geo_polygon=$2");

	         // ICD 1.4 intersect query with area parameter name (Requested)
	         queryString = queryString.replaceFirst("(\\$filter=.*OData\\.CSC\\.Intersects(?:\\(|%28))([^,]*area=geography)", "$1geo_property=Footprint,geo_polygon=geography");

	         // (Additionally already supported due to testing requirements)
	         queryString = queryString.replaceFirst("(\\$filter=.*OData\\.CSC\\.Within(?:\\(|%28))([^,]*area=geography)", "$1geo_property=Footprint,geo_polygon=geography");
	         queryString = queryString.replaceFirst("(\\$filter=.*OData\\.CSC\\.Disjoints(?:\\(|%28))([^,]*area=geography)", "$1geo_property=Footprint,geo_polygon=geography");
	         
	         // replace unsupported spaces after COMMAR
	         queryString = queryString.replaceAll(",%20", ",");
	      }
	      
	      if (LOGGER.isDebugEnabled()) {
	    	 LOGGER.debug("Normalised query string: " + queryString);
	      }
	      
	      return queryString;
	   }
}
