package esa.s1pdgs.cpoc.prip.service.processor;

import java.io.InputStream;
import java.util.List;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.prip.service.metadata.PripMetadataRepository;

public class ProductEntityProcessor implements org.apache.olingo.server.api.processor.EntityProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductEntityProcessor.class);
	
	private OData odata;
	private ServiceMetadata serviceMetadata;
	private PripMetadataRepository pripMetadataRepository;

	public ProductEntityProcessor(PripMetadataRepository pripMetadataRepository) {
		this.pripMetadataRepository = pripMetadataRepository;
	}
	
	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		if (EdmProvider.ES_PRODUCTS_NAME.equals(edmEntitySet.getName()) && keyPredicates.size() >= 1) {
			String uuid = keyPredicates.get(0).getText().replace("'", "");
			PripMetadata foundPripMetadata = pripMetadataRepository.findById(uuid);
			if (null != foundPripMetadata) {
				Entity entity = MappingUtil.pripMetadataToEntity(foundPripMetadata, request.getRawBaseUri());
							
				ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
				EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
				
				ODataSerializer serializer = odata.createSerializer(responseFormat);
				SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), entity, options);
				InputStream entityStream = serializerResult.getContent();
				
				response.setContent(entityStream);
				response.setStatusCode(HttpStatusCode.OK.getStatusCode());
				response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
				LOGGER.debug("Serving product metadata for id {}", uuid);
			} else {
				response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());				
				response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
				LOGGER.debug("No product metadata found with id {}", uuid);
			}
		}
	}

	@Override
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
			ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		// Not supported
	}

	@Override
	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
			ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		// Not supported
	}

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {
		// Not supported
	}
	
}
