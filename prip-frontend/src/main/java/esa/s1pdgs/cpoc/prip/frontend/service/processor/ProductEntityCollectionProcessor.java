package esa.s1pdgs.cpoc.prip.frontend.service.processor;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
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
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor.ProductsFilterVisitor;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripTextFilter;

public class ProductEntityCollectionProcessor implements EntityCollectionProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductEntityCollectionProcessor.class);

	private OData odata;
	private ServiceMetadata serviceMetadata;
	private PripMetadataRepository pripMetadataRepository;

	public ProductEntityCollectionProcessor(PripMetadataRepository pripMetadataRepository) {
		this.pripMetadataRepository = pripMetadataRepository;
	}

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

		if (!EdmProvider.ES_PRODUCTS_NAME.equals(edmEntitySet.getName())) {
			return;
		}
		
		EntityCollection entityCollection = new EntityCollection();
		List<PripDateTimeFilter> pripDateTimeFilters = Collections.emptyList();
		List<PripTextFilter> pripTextFilters = Collections.emptyList();
		for (SystemQueryOption queryOption : uriInfo.getSystemQueryOptions()) {
			if (queryOption instanceof FilterOption && queryOption.getKind().equals(SystemQueryOptionKind.FILTER)) {
				FilterOption filterOption = (FilterOption) queryOption;
				Expression expression = filterOption.getExpression();
				try {
					ProductsFilterVisitor productFilterVistor = new ProductsFilterVisitor();
					expression.accept(productFilterVistor); // also has a return value, which is currently not needed
					pripDateTimeFilters = productFilterVistor.getPripDateTimeFilters();
					pripTextFilters = productFilterVistor.getPripTextFilters();
				} catch (ExpressionVisitException | ODataApplicationException e) {
					LOGGER.error("Invalid or unsupported filter expression: {}", filterOption.getText(), e);
					response.setStatusCode(HttpStatusCode.BAD_REQUEST.getStatusCode());
					return;
				}

			}
		}

		List<PripMetadata> queryResult;
		if (!pripDateTimeFilters.isEmpty() && !pripTextFilters.isEmpty()) {
			queryResult = pripMetadataRepository.findByCreationDateAndProductName(pripDateTimeFilters, pripTextFilters, 0, 0);
		} else if (!pripDateTimeFilters.isEmpty()) {
			queryResult = pripMetadataRepository.findByCreationDate(pripDateTimeFilters, 0, 0);
		} else if (!pripTextFilters.isEmpty()) {
			queryResult = pripMetadataRepository.findByProductName(pripTextFilters, 0, 0);
		} else {
			queryResult = pripMetadataRepository.findAll(0, 0);
		}

		List<Entity> productList = entityCollection.getEntities();
		for (PripMetadata pripMetadata : queryResult) {
			productList.add(MappingUtil.pripMetadataToEntity(pripMetadata, request.getRawBaseUri()));
		}

		InputStream serializedContent = serializeEntityCollection(entityCollection, edmEntitySet,
				request.getRawBaseUri(), responseFormat);

		response.setContent(serializedContent);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
		LOGGER.debug("Serving product metadata collection with {} items", entityCollection.getEntities().size());
	}

	private InputStream serializeEntityCollection(EntityCollection entityCollection, EdmEntitySet edmEntitySet,
			String rawBaseUri, ContentType format) throws SerializerException {
		ODataSerializer serializer = odata.createSerializer(format);
		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
		EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
				.id(rawBaseUri + "/" + edmEntitySet.getName()).contextURL(contextUrl).build();
		SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntitySet.getEntityType(),
				entityCollection, opts);
		return serializerResult.getContent();
	}

}
