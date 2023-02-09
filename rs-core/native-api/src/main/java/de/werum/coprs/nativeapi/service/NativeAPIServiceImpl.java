package de.werum.coprs.nativeapi.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;

@Controller
public class NativeAPIServiceImpl {
	private static final Logger LOG = LogManager.getLogger(NativeAPIServiceImpl.class);

	@Autowired
	private StatementParserServiceImpl parser;
	
	@Autowired
	private ODataBackendServiceImpl backend;

	private Map<String,String> flattenParameters(Map<String,String[]> parameterMap) {
		Map<String,String> results = new HashMap<>();
		
		parameterMap.entrySet().stream().forEach(e -> {
			if (e.getValue().length > 1) {
				LOG.warn("Parameter '{}' occurs more than one, just first occurance will be evaluated",e.getKey());
			}
			results.put(e.getKey(), e.getValue()[0]);
		});
		
		return results;
	}
	
	public StacItemCollection processSearchRequest(final HttpServletRequest request) {
		Map<String,String> parameters = flattenParameters(request.getParameterMap());
		LOG.debug("Identified {} parameters for search request: {}",parameters.size(), parameters);
		String oDataQuery = parser.buildOdataQuery(parameters);
		if (oDataQuery == null || oDataQuery.isEmpty()) {
			throw new IllegalArgumentException("Unable to generate a request from the parameters and look up table");
		}
		
		LOG.debug("OData query generated: {}", oDataQuery);

		String queryUrl = backend.buildPripQueryUrl(oDataQuery, false);		
		StacItemCollection result = backend.queryOData(queryUrl);
		return result;
	}
	

}
