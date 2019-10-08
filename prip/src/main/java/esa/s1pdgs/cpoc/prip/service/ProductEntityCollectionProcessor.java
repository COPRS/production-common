package esa.s1pdgs.cpoc.prip.service;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import esa.s1pdgs.cpoc.prip.service.mapping.MappingUtil;

public class ProductEntityCollectionProcessor
		implements org.apache.olingo.server.api.processor.EntityCollectionProcessor {

	private OData odata;
	private ServiceMetadata serviceMetadata;

	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		EntityCollection entitySet = getData(request, edmEntitySet);

		ODataSerializer serializer = odata.createSerializer(responseFormat);

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

		final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl)
				.build();
		SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entitySet,
				opts);

		InputStream serializedContent = serializerResult.getContent();

		response.setContent(serializedContent);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	private EntityCollection getData(ODataRequest request, EdmEntitySet edmEntitySet) {
		EntityCollection productsCollection = new EntityCollection();
		// check for which EdmEntitySet the data is requested
		if (EdmProvider.ES_PRODUCTS_NAME.equals(edmEntitySet.getName())) {
			List<Entity> productList = productsCollection.getEntities();

			// add some sample product entities
			final Entity e1 = new Entity()
					.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, MappingUtil.createId(request, "Product", UUID.fromString("123e4567-e89b-12d3-a456-556642440000"))))
					.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "DummyProduct1"))
					.addProperty(new Property(null, "ContentType", ValueType.PRIMITIVE, "application/octet-stream"))
					.addProperty(new Property(null, "ContentLength", ValueType.PRIMITIVE, 0))
					.addProperty(new Property(null, "CreationDate", ValueType.PRIMITIVE, MappingUtil.map(Instant.now())))
					.addProperty(new Property(null, "EvictionDate", ValueType.PRIMITIVE, MappingUtil.map(Instant.now())))
					.addProperty(new Property(null, "Checksum", ValueType.COLLECTION_COMPLEX, MappingUtil.mapToChecksumList("MD5", "d41d8cd98f00b204e9800998ecf8427e")));
			e1.setMediaContentType("application/octet-stream");
			e1.setId(createId("Products", 1));
			productList.add(e1);

			final Entity e2 = new Entity()
					.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, MappingUtil.createId(request, "Product", UUID.fromString("123e4567-e89b-12d3-a456-556642440001"))))
					.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "DummyProduct2"))
					.addProperty(new Property(null, "ContentType", ValueType.PRIMITIVE, "application/octet-stream"))
					.addProperty(new Property(null, "ContentLength", ValueType.PRIMITIVE, 0))
					.addProperty(new Property(null, "CreationDate", ValueType.PRIMITIVE, MappingUtil.map(Instant.now())))
					.addProperty(new Property(null, "EvictionDate", ValueType.PRIMITIVE, MappingUtil.map(Instant.now())))
					.addProperty(new Property(null, "Checksum", ValueType.COLLECTION_COMPLEX, MappingUtil.mapToChecksumList("MD5", "d41d8cd98f00b204e9800998ecf8427e")));
			e2.setMediaContentType("application/octet-stream");
			e2.setId(createId("Products", 2));
			productList.add(e2);

			final Entity e3 = new Entity()
					.addProperty(new Property(null, "Id", ValueType.PRIMITIVE, MappingUtil.createId(request, "Product", UUID.fromString("123e4567-e89b-12d3-a456-556642440002"))))
					.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "DummyProduct1"))
					.addProperty(new Property(null, "ContentType", ValueType.PRIMITIVE, "application/octet-stream"))
					.addProperty(new Property(null, "ContentLength", ValueType.PRIMITIVE, 0))
					.addProperty(new Property(null, "CreationDate", ValueType.PRIMITIVE, MappingUtil.map(Instant.now())))
					.addProperty(new Property(null, "EvictionDate", ValueType.PRIMITIVE, MappingUtil.map(Instant.now())))
					.addProperty(new Property(null, "Checksum", ValueType.COLLECTION_COMPLEX, MappingUtil.mapToChecksumList("MD5", "d41d8cd98f00b204e9800998ecf8427e")));
			e3.setMediaContentType("application/octet-stream");
			e3.setId(createId("Products", 3));
			productList.add(e3);
		}

		return productsCollection;
	}

	private URI createId(String entitySetName, Object id) {
	    try {
	        return new URI(entitySetName + "(" + String.valueOf(id) + ")");
	    } catch (URISyntaxException e) {
	        throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
	    }
	}
}
