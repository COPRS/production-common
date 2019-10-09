package esa.s1pdgs.cpoc.prip.service.processor;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
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

import esa.s1pdgs.cpoc.prip.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.service.mapping.MappingUtil;

public class ProductEntityProcessor implements org.apache.olingo.server.api.processor.EntityProcessor {

	private OData odata;
	private ServiceMetadata serviceMetadata;
	
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
		
		if (EdmProvider.ES_PRODUCTS_NAME.equals(edmEntitySet.getName())) {
			List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
			
			Entity dummyEntity = new Entity()
					.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, MappingUtil.createId(request, "Products", UUID.fromString("123e4567-e89b-12d3-a456-556642440001"))))
					.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "DummyProduct2" + uriResourceEntitySet))
					.addProperty(new Property(null, "ContentType", ValueType.PRIMITIVE, "application/octet-stream"))
					.addProperty(new Property(null, "ContentLength", ValueType.PRIMITIVE, 0))
					.addProperty(new Property(null, "CreationDate", ValueType.PRIMITIVE, MappingUtil.map(Instant.now())))
					.addProperty(new Property(null, "EvictionDate", ValueType.PRIMITIVE, MappingUtil.map(Instant.now())))
					.addProperty(new Property(null, "Checksum", ValueType.COLLECTION_COMPLEX, MappingUtil.mapToChecksumList("MD5", "d41d8cd98f00b204e9800998ecf8427e")));
			dummyEntity.setMediaContentType("application/octet-stream");
			dummyEntity.setId(MappingUtil.createId(request, "Products", UUID.fromString("123e4567-e89b-12d3-a456-556642440001")));
			
			ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
			EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
			
			ODataSerializer serializer = odata.createSerializer(responseFormat);
			SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), dummyEntity, options);
			InputStream entityStream = serializerResult.getContent();
			
			response.setContent(entityStream);
			response.setStatusCode(HttpStatusCode.OK.getStatusCode());
			response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
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
