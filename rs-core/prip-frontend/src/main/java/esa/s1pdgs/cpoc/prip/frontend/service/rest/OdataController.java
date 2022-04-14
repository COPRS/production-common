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
import esa.s1pdgs.cpoc.prip.frontend.service.processor.ProductActionProcessor;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.ProductEntityCollectionProcessor;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.ProductEntityProcessor;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;

@RestController
@RequestMapping(value = "/odata")
public class OdataController {

	public static final Logger LOGGER = LoggerFactory.getLogger(OdataController.class);

	@Autowired
	private EdmProvider edmProvider;
	
	@Autowired
	private PripMetadataRepository pripMetadataRepository;
	
	@Autowired
	private ObsClient obsClient;
	
	@Value("${prip-frontend.header-field-name-username:x-username}")
	private String headerFieldNameUsername;
	@Value("${prip-frontend.download-url-expiration-time-in-seconds:600}")
	private long downloadUrlExpirationTimeInSeconds;
	
	@Value("${prip-frontend.debug-support:false}")
	private boolean debugSupport;
	
	@RequestMapping(value = "/v1/**")
	public void process(HttpServletRequest request, HttpServletResponse response) {
		String queryParams = request.getQueryString() == null ? "" : "?" + request.getQueryString();
		LOGGER.info("Received HTTP request for URL: {}{}", request.getRequestURL().toString(), queryParams);		
		final String username = Objects.toString(request.getHeader(headerFieldNameUsername), "not defined");
		OData odata = OData.newInstance();
		ServiceMetadata serviceMetadata = odata.createServiceMetadata(edmProvider, new ArrayList<EdmxReference>());
		ODataHttpHandler handler = odata.createHandler(serviceMetadata);
		handler.register(new ProductEntityProcessor(pripMetadataRepository, obsClient, downloadUrlExpirationTimeInSeconds, username));
		handler.register(new ProductEntityCollectionProcessor(pripMetadataRepository));
		handler.register(new ProductActionProcessor(pripMetadataRepository));
		
	    // Enable DebugSupport of olingo. Add request parameter key: odata-debug value: json | html | download
		if (debugSupport) {
			LOGGER.info("Activating debug support for OLingo");
			handler.register(new org.apache.olingo.server.api.debug.DefaultDebugSupport());
		}

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
	    * Normalize geometric query string.
	    *
	    * @param Raw input query string
	    * @return Normalized query string
	    */
	   public static String handleGeometricRequests(String queryString) {
		   // Intersects function using a polygon
		   // incoming query string: $filter=OData.CSC.Intersects(area=geography'SRID=4326;POLYGON((44.8571 20.3411, 11.4484 49.9204, 2.4321 32.3625, 13.4321 1.3625, 44.8571 20.3411))')
		   //  handled query string: $filter=OData.CSC.Intersects(geo_property=Footprint,geo_shape=geography'SRID=4326;POLYGON((44.8571 20.3411, 11.4484 49.9204, 2.4321 32.3625, 13.4321 1.3625, 44.8571 20.3411))')
		   
		   // Within function using a point
		   // incoming query string: $filter=OData.CSC.Within(area=geography'SRID=4326;POINT(44.8571 20.3411)')
		   //  handled query string: $filter=OData.CSC.Within(geo_property=Footprint,geo_shape=geography'SRID=4326;POINT(44.8571 20.3411)')
		   
		   // Disjoints function using a linestring
		   // incoming query string: $filter=OData.CSC.Disjoints(area=geography'SRID=4326;LINESTRING(44.8571 20.3411, 11.4484 49.9204, 2.4321 32.3625)')
		   //  handled query string: $filter=OData.CSC.Disjoints(geo_property=Footprint,geo_shape=geography'SRID=4326;LINESTRING(44.8571 20.3411, 11.4484 49.9204, 2.4321 32.3625)')
		   
	      if(queryString != null) {
	         // ICD 1.4 Intersects query (required), Within and Disjoints queries (supported due to testing requirements)
	         queryString = queryString.replaceFirst("(\\$filter=.*OData\\.CSC\\.(Intersects|Within|Disjoints)(?:\\(|%28))([^,]*area=geography)", "$1geo_property=Footprint,geo_shape=geography")
	        		 				  .replaceAll(",%20", ","); // replace unsupported spaces after comma
	      }
	      
	      if (LOGGER.isDebugEnabled()) {
	    	 LOGGER.debug("Normalised query string: " + queryString);
	      }
	      
	      return queryString;
	   }
	   
}
