package esa.s1pdgs.cpoc.prip.frontend.service.processor;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
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
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions.Builder;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.core.uri.UriInfoImpl;
import org.apache.olingo.server.core.uri.UriResourceNavigationPropertyImpl;
import org.apache.olingo.server.core.uri.queryoption.ExpandItemImpl;
import org.apache.olingo.server.core.uri.queryoption.ExpandOptionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor.ProductsFilterVisitor;
import esa.s1pdgs.cpoc.prip.frontend.utils.OlingoUtil;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;

public class ProductEntityCollectionProcessor implements EntityCollectionProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductEntityCollectionProcessor.class);

	private OData odata;
	private ServiceMetadata serviceMetadata;
	private PripMetadataRepository pripMetadataRepository;
	
	// --------------------------------------------------------------------------

	public ProductEntityCollectionProcessor(PripMetadataRepository pripMetadataRepository) {
		this.pripMetadataRepository = pripMetadataRepository;
	}

	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}
	
	// --------------------------------------------------------------------------

	@Override
	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		List<UriResource> resourceParts = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
		EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();
		
		if (!EdmProvider.ES_PRODUCTS_NAME.equals(startEdmEntitySet.getName())) {
			return;
		}
		
		final int segmentCount = resourceParts.size();
		EdmEntitySet responseEdmEntitySet = null;
	    EdmEntityType responseEdmEntityType;
	    
		if (segmentCount == 1) {
			responseEdmEntitySet = startEdmEntitySet; // the response body is built from the first (and only) entitySet
			responseEdmEntityType = startEdmEntitySet.getEntityType();
		} else if (segmentCount == 2) {
			final UriResource lastSegment = resourceParts.get(1);
			
			if (lastSegment instanceof UriResourceNavigation) {
				final UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) lastSegment;
				final EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
				responseEdmEntityType = edmNavigationProperty.getType();
				
				if (!edmNavigationProperty.containsTarget()) {
					responseEdmEntitySet = OlingoUtil.getNavigationTargetEntitySet(startEdmEntitySet, edmNavigationProperty);
				} else {
					responseEdmEntitySet = startEdmEntitySet;
				}
			} else {
				responseEdmEntityType = startEdmEntitySet.getEntityType();
			}
		} else {
			throw new ODataApplicationException("Only 2 UriResourceParts allowed",
					HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
		}
		
		// expand abstract type 'Attributes' if asked for
		final ExpandOptionImpl expandOptions = (ExpandOptionImpl) uriInfo.getExpandOption();
		if (null != expandOptions && EdmProvider.ATTRIBUTES_SET_NAME.equals(expandOptions.getText())) {
			for (String setname : EdmProvider.ATTRIBUTES_TYPE_NAMES) {
				final ExpandItemImpl item = new ExpandItemImpl().setResourcePath(new UriInfoImpl().addResourcePart(
						new UriResourceNavigationPropertyImpl(responseEdmEntityType.getNavigationProperty(setname))));
				expandOptions.addExpandItem(item);
			}
		}
		
		final ContextURL contextUrl = OlingoUtil.getContextUrl(responseEdmEntitySet, responseEdmEntityType, false);
		
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
					// TODO @MSc: impl mapping im ProductFilterVisitor für alle typen, am besten zusammenfassen die filter wie in legacy, dann einheitliche schnittstelle zum repository
					// (es fehlen noch, integer, boolean, double)
				} catch (ExpressionVisitException | ODataApplicationException e) {
					LOGGER.error("Invalid or unsupported filter expression: {}", filterOption.getText(), e);
					response.setStatusCode(HttpStatusCode.BAD_REQUEST.getStatusCode());
					return;
				}

			}
		}
		
		if (null == uriInfo.getCountOption() || !uriInfo.getCountOption().getValue()) {

			// List of Entities Request
			
			Optional<Integer> top = Optional.empty();
			if (null != uriInfo.getTopOption()) {
				top = Optional.of(uriInfo.getTopOption().getValue());
				if (top.get() < 0) {
					throw new ODataApplicationException("Invalid value for $top", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
				}
			}
			
			Optional<Integer> skip = Optional.empty();
			if (null != uriInfo.getSkipOption()) {
				skip = Optional.of(uriInfo.getSkipOption().getValue());
				if (skip.get() < 0) {
					throw new ODataApplicationException("Invalid value for $skip", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
				}
			}
				
			List<PripMetadata> queryResult;
			if (pripDateTimeFilters.isEmpty() && pripTextFilters.isEmpty()) {
				queryResult = pripMetadataRepository.findAll(top, skip);
			} else {
				queryResult = pripMetadataRepository.findWithFilters(pripTextFilters, pripDateTimeFilters, top, skip);
			} 
			List<Entity> productList = entityCollection.getEntities();
			for (PripMetadata pripMetadata : queryResult) {
				// TODO @MSc: impl mapping from PripMetadata to OData/Olingo response
				productList.add(MappingUtil.pripMetadataToEntity(pripMetadata, request.getRawBaseUri()));
			}
			
			// serialize
			final InputStream serializedContent = this.serializeEntityCollection(entityCollection,
					responseEdmEntityType, request.getRawBaseUri(), startEdmEntitySet.getName(), contextUrl,
					responseFormat, uriInfo, expandOptions);
			
			response.setContent(serializedContent);
			response.setStatusCode(HttpStatusCode.OK.getStatusCode());
			response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
			LOGGER.debug("Serving product metadata collection with {} items", entityCollection.getEntities().size());
			
		} else {
			
			// Count Request
			
			int count = 0;
			if (pripDateTimeFilters.isEmpty() && pripTextFilters.isEmpty()) {
				count = pripMetadataRepository.countAll();
			} else {
				count = pripMetadataRepository.countWithFilters(pripDateTimeFilters, pripTextFilters);
			}

			entityCollection.setCount(count);

			// serialize
			final InputStream serializedContent = this.serializeEntityCollection(entityCollection,
					responseEdmEntityType, request.getRawBaseUri(), startEdmEntitySet.getName(), contextUrl,
					responseFormat, uriInfo, expandOptions);

			response.setContent(serializedContent);
			response.setStatusCode(HttpStatusCode.OK.getStatusCode());
			response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
			
			LOGGER.debug("Serving product metadata collection with {} items", entityCollection.getEntities().size());
			
		}
	}
	
	// --------------------------------------------------------------------------
	
	private InputStream serializeEntityCollection(EntityCollection entityCollection,
			EdmEntityType responseEdmEntityType, String rawBaseUri, String entitySetNameForId, ContextURL contextUrl,
			ContentType format, UriInfo uriInfo, ExpandOptionImpl expandOptions) throws SerializerException {
		
		final ODataSerializer serializer = this.odata.createSerializer(format);
		final Builder optsBuilder = EntityCollectionSerializerOptions.with()
				.id(rawBaseUri + "/" + entitySetNameForId)
				.contextURL(contextUrl);
		
		if (null != expandOptions) {
			optsBuilder.expand(expandOptions);
		}
		if (null != uriInfo.getCountOption()) {
			optsBuilder.count(uriInfo.getCountOption());
		}
		if (null != uriInfo.getSelectOption()) {
			optsBuilder.select(uriInfo.getSelectOption());
		}
		
		final SerializerResult serializerResult = serializer.entityCollection(this.serviceMetadata,
				responseEdmEntityType, entityCollection, optsBuilder.build());
		return serializerResult.getContent();
	}
	
}
