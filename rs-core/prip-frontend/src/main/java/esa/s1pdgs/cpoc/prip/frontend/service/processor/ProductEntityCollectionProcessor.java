package esa.s1pdgs.cpoc.prip.frontend.service.processor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
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
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.core.uri.UriInfoImpl;
import org.apache.olingo.server.core.uri.UriResourceNavigationPropertyImpl;
import org.apache.olingo.server.core.uri.queryoption.ExpandItemImpl;
import org.apache.olingo.server.core.uri.queryoption.ExpandOptionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.RecoverableDataAccessException;

import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties;
import esa.s1pdgs.cpoc.prip.frontend.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor.ProductsFilterVisitor;
import esa.s1pdgs.cpoc.prip.frontend.utils.OlingoUtil;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm.PripSortOrder;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;

public class ProductEntityCollectionProcessor implements EntityCollectionProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductEntityCollectionProcessor.class);

	private OData odata;
	private ServiceMetadata serviceMetadata;
	private final PripMetadataRepository pripMetadataRepository;

	private static final Map<String, PripMetadata.FIELD_NAMES> SORTABLE_FIELDS;
	static {
		SORTABLE_FIELDS = new HashMap<>();
		SORTABLE_FIELDS.put(ProductProperties.PublicationDate.name(), PripMetadata.FIELD_NAMES.CREATION_DATE);
		SORTABLE_FIELDS.put(ProductProperties.EvictionDate.name(), PripMetadata.FIELD_NAMES.EVICTION_DATE);
		SORTABLE_FIELDS.put(ProductProperties.ContentDate.name() + "/" + ProductProperties.Start.name(), PripMetadata.FIELD_NAMES.CONTENT_DATE_START);
		SORTABLE_FIELDS.put(ProductProperties.ContentDate.name() + "/" + ProductProperties.End.name(), PripMetadata.FIELD_NAMES.CONTENT_DATE_END);
		SORTABLE_FIELDS.put(ProductProperties.ContentLength.name(), PripMetadata.FIELD_NAMES.CONTENT_LENGTH);
		SORTABLE_FIELDS.put(ProductProperties.Name.name(), PripMetadata.FIELD_NAMES.NAME);
		SORTABLE_FIELDS.put(ProductProperties.ProductionType.name(), PripMetadata.FIELD_NAMES.PRODUCTION_TYPE);
		SORTABLE_FIELDS.put(ProductProperties.ContentType.name(), PripMetadata.FIELD_NAMES.CONTENT_TYPE);
		SORTABLE_FIELDS.put(ProductProperties.Id.name(), PripMetadata.FIELD_NAMES.ID);
	}

	public ProductEntityCollectionProcessor(PripMetadataRepository pripMetadataRepository) {
		this.pripMetadataRepository = pripMetadataRepository;
	}

	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void readEntityCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
	      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
		final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
		final EdmEntitySet rootEdmEntitySet = uriResourceEntitySet.getEntitySet();
		if (EdmProvider.ES_PRODUCTS_NAME.equals(rootEdmEntitySet.getName())) {
		   switch (resourceParts.size()) {
		      case 1: serveProducts(request, response, uriInfo, responseFormat, rootEdmEntitySet); break;
		      case 2: EdmEntitySet secondLevelEdmEntitySet = OlingoUtil.getNavigationTargetEntitySet(
		                     rootEdmEntitySet, ((UriResourceNavigation) resourceParts.get(1)).getProperty());
      		      if (EdmProvider.QUICKLOOK_SET_NAME.equals(secondLevelEdmEntitySet.getName())) {
      	            serveQuicklooks(request, response, uriInfo, responseFormat, secondLevelEdmEntitySet);
      	            break;
      	         }
	         default: throw new ODataApplicationException("Resource not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
		   }
		}
	}

	private void serveProducts(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
	      final ContentType responseFormat, final EdmEntitySet edmEntitySet) throws ODataApplicationException, ODataLibraryException {
      final ExpandOptionImpl expandOption = (ExpandOptionImpl) uriInfo.getExpandOption();
		if (null != expandOption && null != expandOption.getText() &&
		      List.of(expandOption.getText().split(",")).contains(EdmProvider.ATTRIBUTES_SET_NAME)) {
			for (final String setname : EdmProvider.ATTRIBUTES_TYPE_NAMES) {
				final ExpandItem item = new ExpandItemImpl().setResourcePath(new UriInfoImpl().addResourcePart(
						new UriResourceNavigationPropertyImpl(edmEntitySet.getEntityType().getNavigationProperty(setname))));
				expandOption.addExpandItem(item);
			}
		}

		final ContextURL contextUrl = OlingoUtil.getContextUrl(edmEntitySet, edmEntitySet.getEntityType(), false);
		final EntityCollection entityCollection = new EntityCollection();
		PripQueryFilter queryFilters = null;

		for (final SystemQueryOption queryOption : uriInfo.getSystemQueryOptions()) {
			if (queryOption instanceof FilterOption && queryOption.getKind().equals(SystemQueryOptionKind.FILTER)) {
				final FilterOption filterOption = (FilterOption) queryOption;
				final Expression expression = filterOption.getExpression();
				try {
					final ProductsFilterVisitor productFilterVistor = new ProductsFilterVisitor();
					expression.accept(productFilterVistor); // also has a return value, which is currently not needed
					queryFilters =  productFilterVistor.getFilter();
					LOGGER.debug(String.format("ProductsFilterVisitor returns: %s", queryFilters));
				} catch (ExpressionVisitException | ODataApplicationException e) {
					LOGGER.error("Invalid or unsupported filter expression: {}", filterOption.getText(), e);
					response.setStatusCode(HttpStatusCode.BAD_REQUEST.getStatusCode());
					return;
				}
			}
		}

		if (null != uriInfo.getCountOption() && uriInfo.getCountOption().getValue()) {

			// Count Request

			int count = 0;
			try {
				if (null == queryFilters) {
					count = this.pripMetadataRepository.countAll();
				} else {
					count = this.pripMetadataRepository.countWithFilter(queryFilters);
				}
			} catch (RecoverableDataAccessException e) {
				throw new ODataApplicationException(HttpStatusCode.SERVICE_UNAVAILABLE.getInfo(),
						HttpStatusCode.SERVICE_UNAVAILABLE.getStatusCode(), Locale.ROOT);
			}

			entityCollection.setCount(count);
		}
			
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

		// extract sorting parameters, if any
		final List<PripSortTerm> sortTerms = new ArrayList<>();
		final OrderByOption orderByOption = uriInfo.getOrderByOption();
		if (null != orderByOption && CollectionUtil.isNotEmpty(orderByOption.getOrders())) {
				for (final OrderByItem orderByItem : orderByOption.getOrders()) {
				final PripSortOrder sortOrder;
				if (orderByItem.isDescending()) {
					sortOrder = PripSortOrder.DESCENDING;
				} else {
					sortOrder = PripSortOrder.ASCENDING;
				}

				FIELD_NAMES sortFieldName = null;
				final Expression expression = orderByItem.getExpression();
				if (expression instanceof Member) {
					final UriInfoResource resourcePath = ((Member) expression).getResourcePath();
					final UriResource uriResource = resourcePath.getUriResourceParts().get(0);
						if (uriResource instanceof UriResourcePrimitiveProperty) {
							final EdmProperty edmProperty = ((UriResourcePrimitiveProperty) uriResource).getProperty();

						if (StringUtil.isNotBlank(edmProperty.getName())) {
							final String fieldName = edmProperty.getName();

							if (SORTABLE_FIELDS.containsKey(fieldName)) {
								sortFieldName = SORTABLE_FIELDS.get(fieldName);
							} else {
								throw new ODataApplicationException(
										"Invalid value for $orderby: field name '" + fieldName
										+ "' is not supported! Supported field names: " + SORTABLE_FIELDS.keySet(),
										HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
							}
						}
					} else if (uriResource instanceof UriResourceComplexProperty) {
						final UriResourceComplexProperty complexProperty = (UriResourceComplexProperty) uriResource;
						final String complexTypeAttrName = complexProperty.getSegmentValue();
						final UriResource subElement = resourcePath.getUriResourceParts().get(1);
						final String subElementAttrName = subElement.getSegmentValue();
						final String complexTypePath = complexTypeAttrName + "/" + subElementAttrName;

						if (SORTABLE_FIELDS.containsKey(complexTypePath)) {
							sortFieldName = SORTABLE_FIELDS.get(complexTypePath);
						} else {
							throw new ODataApplicationException(
									"Invalid value for $orderby: field name '" + complexTypePath
											+ "' is not supported! Supported field names: "
											+ SORTABLE_FIELDS.keySet(),
									HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
						}
					}
				}

				if (null != sortFieldName) {
					sortTerms.add(new PripSortTerm(sortFieldName, sortOrder));
				}
			}

			LOGGER.debug("using sort terms: " + sortTerms);
		}

		List<PripMetadata> queryResult;
		try {
			if (null == queryFilters) {
				queryResult = this.pripMetadataRepository.findAll(top, skip, sortTerms);
			} else {
				queryResult = this.pripMetadataRepository.findWithFilter(queryFilters, top, skip, sortTerms);
			}
		} catch (RecoverableDataAccessException e) {
			throw new ODataApplicationException(HttpStatusCode.SERVICE_UNAVAILABLE.getInfo(),
					HttpStatusCode.SERVICE_UNAVAILABLE.getStatusCode(), Locale.ROOT);
		}
		final List<Entity> productList = entityCollection.getEntities();
		for (final PripMetadata pripMetadata : queryResult) {
			productList.add(MappingUtil.pripMetadataToEntity(pripMetadata, request.getRawBaseUri()));
		}

		// serialize
		final InputStream serializedContent = this.serializeEntityCollection(entityCollection,
		      edmEntitySet.getEntityType(), request.getRawBaseUri(), edmEntitySet.getName(), contextUrl,
				responseFormat, uriInfo, expandOption);

		response.setContent(serializedContent);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
		LOGGER.debug("Serving product metadata collection with {} items", entityCollection.getEntities().size());
	}
	
	private void serveQuicklooks(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
	      final ContentType responseFormat, final EdmEntitySet edmEntitySet) throws ODataApplicationException, ODataLibraryException {
	   final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
	   final List<UriParameter> keyPredicates = ((UriResourceEntitySet)resourceParts.get(0)).getKeyPredicates();
      final String uuid = keyPredicates.get(0).getText().replace("'", "");
      try {
         final PripMetadata foundPripMetadata = pripMetadataRepository.findById(uuid);
         if (null != foundPripMetadata) {
            final EntityCollection quicklookEntityCollection = MappingUtil.quicklookEntityCollectionOf(foundPripMetadata);
            final ContextURL contextUrl = OlingoUtil.getContextUrl(edmEntitySet, edmEntitySet.getEntityType(), false);
            final InputStream serializedContent = this.serializeEntityCollection(quicklookEntityCollection,
                  edmEntitySet.getEntityType(), request.getRawBaseUri(), EdmProvider.QUICKLOOK_SET_NAME, contextUrl,
                  responseFormat, uriInfo, uriInfo.getExpandOption());
            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
            LOGGER.debug("Serving quicklook product metadata collection with {} items for product with id {}", quicklookEntityCollection.getEntities().size(), uuid);
         } else {
            response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
            LOGGER.debug("No product metadata found with id {}", uuid);
         }
      } catch (RecoverableDataAccessException e) {
         throw new ODataApplicationException(HttpStatusCode.SERVICE_UNAVAILABLE.getInfo(),
               HttpStatusCode.SERVICE_UNAVAILABLE.getStatusCode(), Locale.ROOT);
      }
   }

	private InputStream serializeEntityCollection(final EntityCollection entityCollection, final EdmEntityType responseEdmEntityType,
	      final String rawBaseUri, final String entitySetNameForId, final ContextURL contextUrl, final ContentType format,
	      final UriInfo uriInfo, final ExpandOption expandOption) throws SerializerException {
		final ODataSerializer serializer = this.odata.createSerializer(format);
		final Builder optsBuilder = EntityCollectionSerializerOptions.with()
				.id(rawBaseUri + "/" + entitySetNameForId)
				.contextURL(contextUrl);

		if (null != expandOption) {
			optsBuilder.expand(expandOption);
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
