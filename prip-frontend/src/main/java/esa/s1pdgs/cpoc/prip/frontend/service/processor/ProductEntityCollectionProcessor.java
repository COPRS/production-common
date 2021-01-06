package esa.s1pdgs.cpoc.prip.frontend.service.processor;

import java.io.InputStream;
import java.util.ArrayList;
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
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
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

import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties;
import esa.s1pdgs.cpoc.prip.frontend.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor.ProductsFilterVisitor;
import esa.s1pdgs.cpoc.prip.frontend.utils.OlingoUtil;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm;
import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm.PripSortOrder;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;

public class ProductEntityCollectionProcessor implements EntityCollectionProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductEntityCollectionProcessor.class);

	private OData odata;
	private ServiceMetadata serviceMetadata;
	private final PripMetadataRepository pripMetadataRepository;

	private static final List<String> SORTABLE_FIELDS;
	static {
		SORTABLE_FIELDS = new ArrayList<>();
		SORTABLE_FIELDS.add(FIELD_NAMES.CREATION_DATE.fieldName());
		SORTABLE_FIELDS.add(FIELD_NAMES.EVICTION_DATE.fieldName());
		SORTABLE_FIELDS.add(EntityTypeProperties.ContentDate + "/" + EntityTypeProperties.Start);
		SORTABLE_FIELDS.add(EntityTypeProperties.ContentDate + "/" + EntityTypeProperties.End);
		SORTABLE_FIELDS.add(FIELD_NAMES.NAME.fieldName());
		SORTABLE_FIELDS.add(FIELD_NAMES.CONTENT_LENGTH.fieldName());
		SORTABLE_FIELDS.add(FIELD_NAMES.PRODUCT_FAMILY.fieldName());
		SORTABLE_FIELDS.add(FIELD_NAMES.PRODUCTION_TYPE.fieldName());
		SORTABLE_FIELDS.add(FIELD_NAMES.CONTENT_TYPE.fieldName());
		SORTABLE_FIELDS.add(FIELD_NAMES.ID.fieldName());
	}

	// --------------------------------------------------------------------------

	public ProductEntityCollectionProcessor(PripMetadataRepository pripMetadataRepository) {
		this.pripMetadataRepository = pripMetadataRepository;
	}

	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	// --------------------------------------------------------------------------

	@Override
	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
		final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
		final EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();

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
			for (final String setname : EdmProvider.ATTRIBUTES_TYPE_NAMES) {
				final ExpandItemImpl item = new ExpandItemImpl().setResourcePath(new UriInfoImpl().addResourcePart(
						new UriResourceNavigationPropertyImpl(responseEdmEntityType.getNavigationProperty(setname))));
				expandOptions.addExpandItem(item);
			}
		}

		final ContextURL contextUrl = OlingoUtil.getContextUrl(responseEdmEntitySet, responseEdmEntityType, false);
		final EntityCollection entityCollection = new EntityCollection();
		List<PripQueryFilter> queryFilters = Collections.emptyList();

		for (final SystemQueryOption queryOption : uriInfo.getSystemQueryOptions()) {
			if (queryOption instanceof FilterOption && queryOption.getKind().equals(SystemQueryOptionKind.FILTER)) {
				final FilterOption filterOption = (FilterOption) queryOption;
				final Expression expression = filterOption.getExpression();
				try {
					final ProductsFilterVisitor productFilterVistor = new ProductsFilterVisitor();
					expression.accept(productFilterVistor); // also has a return value, which is currently not needed
					queryFilters =  productFilterVistor.getQueryFilters();
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

								if (SORTABLE_FIELDS.contains(fieldName)) {
									sortFieldName = FIELD_NAMES.fromString(fieldName);
								} else {
									throw new ODataApplicationException(
											"Invalid value for $orderby: field name '" + fieldName
											+ "' is not supported! Supported field names: " + SORTABLE_FIELDS,
											HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
								}
							}
						}
					}

					if (null != sortFieldName) {
						sortTerms.add(new PripSortTerm(sortFieldName, sortOrder));
					}
				}
			}

			List<PripMetadata> queryResult;
			if (queryFilters.isEmpty()) {
				queryResult = this.pripMetadataRepository.findAll(top, skip, sortTerms);
			} else {
				queryResult = this.pripMetadataRepository.findWithFilters(queryFilters, top, skip, sortTerms);
			}
			final List<Entity> productList = entityCollection.getEntities();
			for (final PripMetadata pripMetadata : queryResult) {
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
			if (queryFilters.isEmpty()) {
				count = this.pripMetadataRepository.countAll();
			} else {
				count = this.pripMetadataRepository.countWithFilters(queryFilters);
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
