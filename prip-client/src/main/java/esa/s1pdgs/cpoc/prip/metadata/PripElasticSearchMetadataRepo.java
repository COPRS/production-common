package esa.s1pdgs.cpoc.prip.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.common.geo.GeoShapeType;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.geometry.Geometry;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.GeoShapePolygon;
import esa.s1pdgs.cpoc.prip.model.PripGeoCoordinate;
import esa.s1pdgs.cpoc.prip.model.PripGeoShape;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm.PripSortOrder;
import esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripGeometryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;

@Service
public class PripElasticSearchMetadataRepo implements PripMetadataRepository {

	private static final Logger LOGGER = LogManager.getLogger(PripElasticSearchMetadataRepo.class);
	private static final String ES_INDEX = "prip";
	private int maxSearchHits;

	private final RestHighLevelClient restHighLevelClient;

	@Autowired
	public PripElasticSearchMetadataRepo(RestHighLevelClient restHighLevelClient,
			@Value("${prip-client.repository.max-search-hits}") final int maxSearchHits) {
		this.restHighLevelClient = restHighLevelClient;
		this.maxSearchHits = maxSearchHits;
	}

	@Override
	public void save(PripMetadata pripMetadata) {

		LOGGER.info("saving PRIP metadata {}", pripMetadata);

		final IndexRequest request = new IndexRequest(ES_INDEX).id(pripMetadata.getName())
				.source(pripMetadata.toString(), XContentType.JSON);
		try {
			final IndexResponse indexResponse = this.restHighLevelClient.index(request, RequestOptions.DEFAULT);

			if (indexResponse.getResult() == DocWriteResponse.Result.CREATED
					|| indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
				LOGGER.info("saving PRIP metadata successful");
			} else {
				final ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
				if (shardInfo.getFailed() > 0) {
					LOGGER.error("could not save PRIP metadata");
					for (final ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
						final String reason = failure.reason();
						LOGGER.error(reason);
					}
				}
			}
		} catch (final IOException e) {
			LOGGER.error("could not save PRIP metadata", e);
		}
	}

	@Override
	public PripMetadata findById(String id) {

		LOGGER.info("finding PRIP metadata with id {}", id);

		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.matchQuery(PripMetadata.FIELD_NAMES.ID.fieldName(), id));

		final SearchRequest searchRequest = new SearchRequest(ES_INDEX);
		searchRequest.source(sourceBuilder);

		PripMetadata pripMetadata = null;

		try {
			final SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest,
					RequestOptions.DEFAULT);
			LOGGER.trace("response {}", searchResponse);

			if (searchResponse.getHits().getHits().length > 0) {
				pripMetadata = this.mapSearchHitToPripMetadata(searchResponse.getHits().getHits()[0]);
			} else {
				LOGGER.warn("PRIP metadata with id {} not found", id);
			}

		} catch (final IOException e) {
			LOGGER.warn("error while finding PRIP metadata", e);
		}
		LOGGER.info("finding PRIP metadata successful");
		return pripMetadata;
	}

	@Override
	public List<PripMetadata> findAll(Optional<Integer> top, Optional<Integer> skip, List<PripSortTerm> sortTerms) {
		LOGGER.info("finding PRIP metadata");

		return this.queryWithOffset(null, top, skip, sortTerms);
	}

	@Override
	public List<PripMetadata> findWithFilters(List<PripQueryFilter> filters, Optional<Integer> top,
			Optional<Integer> skip, List<PripSortTerm> sortTerms) {
		LOGGER.info("finding PRIP metadata with filters {}", filters);

		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		buildQueryWithFilters(filters, queryBuilder);

		return this.queryWithOffset(queryBuilder, top, skip, sortTerms);
	}

	private static void buildQueryWithFilters(List<? extends PripQueryFilter> filters, BoolQueryBuilder queryBuilder) {
		for (final PripQueryFilter filter : CollectionUtil.nullToEmpty(filters)) {
			if (filter instanceof PripRangeValueFilter) {
				buildQueryWithRangeValueFilter((PripRangeValueFilter<?>) filter, queryBuilder);
			} else if (filter instanceof PripTextFilter) {
				buildQueryWithTextFilter((PripTextFilter) filter, queryBuilder);
			} else if (filter instanceof PripBooleanFilter) {
				buildQueryWithBooleanFilter((PripBooleanFilter) filter, queryBuilder);
			} else if (filter instanceof PripGeometryFilter) {
				buildQueryWithGeometryFilter((PripGeometryFilter) filter, queryBuilder);
			} else {
				throw new IllegalArgumentException(
						String.format("filter type not supported: %s", filter.getClass().getSimpleName()));
			}
		}
	}

	private static void buildQueryWithRangeValueFilter(PripRangeValueFilter<?> filter, BoolQueryBuilder queryBuilder) {
		switch (filter.getOperator()) {
		case LT:
			queryBuilder.must(QueryBuilders.rangeQuery(filter.getFieldName()).lt(filter.getValue()));
			break;
		case LE:
			queryBuilder.must(QueryBuilders.rangeQuery(filter.getFieldName()).lte(filter.getValue()));
			break;
		case GT:
			queryBuilder.must(QueryBuilders.rangeQuery(filter.getFieldName()).gt(filter.getValue()));
			break;
		case GE:
			queryBuilder.must(QueryBuilders.rangeQuery(filter.getFieldName()).gte(filter.getValue()));
			break;
		case EQ:
			queryBuilder.must(QueryBuilders.termQuery(filter.getFieldName(), filter.getValue()));
			break;
		case NE:
			queryBuilder.mustNot(QueryBuilders.termQuery(filter.getFieldName(), filter.getValue()));
			break;
		default:
			throw new IllegalArgumentException(
					String.format("filter operator not supported: %s", filter.getOperator().name()));
		}
	}

	private static void buildQueryWithTextFilter(PripTextFilter filter, BoolQueryBuilder queryBuilder) {
		switch (filter.getFunction()) {
		case STARTS_WITH:
			queryBuilder
					.must(QueryBuilders.wildcardQuery(filter.getFieldName(), String.format("%s*", filter.getText())));
			break;
		case ENDS_WITH:
			queryBuilder
					.must(QueryBuilders.wildcardQuery(filter.getFieldName(), String.format("*%s", filter.getText())));
			break;
		case CONTAINS:
			queryBuilder
					.must(QueryBuilders.wildcardQuery(filter.getFieldName(), String.format("*%s*", filter.getText())));
			break;
		case EQUALS:
			queryBuilder.must(QueryBuilders.matchQuery(filter.getFieldName(), filter.getText())
					.fuzziness(Fuzziness.ZERO).operator(Operator.AND));
			break;
		default:
			throw new IllegalArgumentException(
					String.format("not supported filter function: %s", filter.getFunction().name()));
		}
	}

	private static void buildQueryWithBooleanFilter(PripBooleanFilter filter, BoolQueryBuilder queryBuilder) {
		switch (filter.getFunction()) {
		case EQUALS:
			queryBuilder.must(QueryBuilders.termQuery(filter.getFieldName(), filter.getValue().booleanValue()));
			break;
		case EQUALS_NOT:
			queryBuilder.mustNot(QueryBuilders.termQuery(filter.getFieldName(), filter.getValue().booleanValue()));
			break;
		default:
			throw new IllegalArgumentException(
					String.format("not supported filter function: %s", filter.getFunction().name()));
		}
	}

	private List<PripMetadata> queryWithOffset(BoolQueryBuilder queryBuilder, Optional<Integer> top,
			Optional<Integer> skip, List<PripSortTerm> sortTerms) {

		List<PripMetadata> result = new ArrayList<>();
		if (skip.orElse(0) <= 0 || skip.orElse(0) + top.orElse(0) <= this.maxSearchHits) {
			// Paging through less than maxSearchHits -> default behaviour
			LOGGER.info("Handling query with skip={} and top={} (max-search-hits={}) -> Use elastic classical pagination", skip.orElse(0), top.orElse(0), this.maxSearchHits);
			result.addAll(convert(this.query(queryBuilder, top, skip, sortTerms)));
		} else {
			// Paging through more than maxSearchHits ->
			// 1. iterate to offset (first page by using default mechanism, each further page by using search_after)
			// 2. search_after(offset)
			Integer offset = skip.orElse(0);
			Integer pageSize = offset > maxSearchHits ? maxSearchHits : offset;
			List<SearchHit> offsetList = this.queryOffset(queryBuilder, Optional.of(pageSize), Optional.of(0), sortTerms, false, null);
			SearchHit offsetSearchHit = offsetList.get(offsetList.size() - 1);
			while (offset > maxSearchHits) {
				offsetList = this.queryOffset(queryBuilder, top, Optional.of(pageSize), sortTerms, true,
						offsetSearchHit.getSortValues());
				offsetSearchHit = offsetList.get(offsetList.size() - 1);
				offset = offset - offsetList.size();
				pageSize = offset > maxSearchHits ? maxSearchHits : offset;
			}

			LOGGER.info("Handling query with skip={} and top={} (max-search-hits={}) -> Use elastic search_after", skip.orElse(0), top.orElse(0), this.maxSearchHits);
			offsetList = this.queryOffset(queryBuilder, top, Optional.of(pageSize), sortTerms, true,
					offsetSearchHit.getSortValues());
			result.addAll(convert(offsetList));
		}
		return result;
	}

	private List<SearchHit> queryOffset(BoolQueryBuilder queryBuilder, Optional<Integer> top, Optional<Integer> skip,
			List<PripSortTerm> sortTerms, boolean searchAfter, Object[] searchAfterOffset) {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		if (queryBuilder != null) {
			sourceBuilder.query(queryBuilder);
		}
		configurePaging(top, searchAfter ? Optional.of(0) : skip, sourceBuilder);
		configureSorting(sortTerms, sourceBuilder);

		if (searchAfter) {
			sourceBuilder.searchAfter(searchAfterOffset);
		}
		return search(sourceBuilder);
	}

	private List<SearchHit> query(BoolQueryBuilder queryBuilder, Optional<Integer> top, Optional<Integer> skip,
			List<PripSortTerm> sortTerms) {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		if (queryBuilder != null) {
			sourceBuilder.query(queryBuilder);
		}
		configurePaging(top, skip, sourceBuilder);
		configureSorting(sortTerms, sourceBuilder);

		return search(sourceBuilder);
	}

	private void configurePaging(Optional<Integer> top, Optional<Integer> skip,
			final SearchSourceBuilder sourceBuilder) {
		// paging
		if (skip.isPresent()) {
			sourceBuilder.from(skip.get());
		}
		if (top.isPresent() && 0 <= top.get() && top.get() <= this.maxSearchHits) {
			sourceBuilder.size(top.get());
		} else {
			sourceBuilder.size(this.maxSearchHits);
		}
	}

	private void configureSorting(List<PripSortTerm> sortTerms, final SearchSourceBuilder sourceBuilder) {
		// sorting
		if (CollectionUtil.isNotEmpty(sortTerms)) {
			for (final PripSortTerm sortTerm : sortTerms) {
				final String sortFieldName = sortTerm.getSortFieldName().fieldName();
				final PripSortOrder sortOrder = sortTerm.getSortOrderOrDefault(PripSortOrder.ASCENDING);

				sourceBuilder.sort(sortFieldName, sortOrderFor(sortOrder.abbreviation()));
			}
		} else {
			// when no sorting is specified, sort by creation date in ascending order
			sourceBuilder.sort(PripMetadata.FIELD_NAMES.CREATION_DATE.fieldName(), SortOrder.ASC);
		}

		// check if already sorted by id - if not, add sorting
		boolean sortedByTieBraker = false;
		for (final PripSortTerm sortTerm : sortTerms) {
			final String sortFieldName = sortTerm.getSortFieldName().fieldName();
			if (PripMetadata.FIELD_NAMES.ID.fieldName().equals(sortFieldName)) {
				sortedByTieBraker = true;
				break;
			}
		}
		if (!sortedByTieBraker) {
			sourceBuilder.sort(PripMetadata.FIELD_NAMES.ID.fieldName(), SortOrder.ASC);
		}
	}

	private List<SearchHit> search(final SearchSourceBuilder sourceBuilder) {
		final SearchRequest searchRequest = new SearchRequest(ES_INDEX);
		searchRequest.source(sourceBuilder);

		List<SearchHit> searchHits = new ArrayList<>();
		LOGGER.debug("search request: {}", searchRequest);

		try {
			final SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest,
					RequestOptions.DEFAULT);
			LOGGER.debug("response: {}", searchResponse);

			searchHits = Arrays.asList(searchResponse.getHits().getHits());
		} catch (final IOException e) {
			LOGGER.warn("error while finding PRIP metadata", e);
		}
		return searchHits;
	}

	private List<PripMetadata> convert(List<SearchHit> searchHits) {
		List<PripMetadata> result = new ArrayList<>();

		if (searchHits != null) {
			for (final SearchHit hit : searchHits) {
				result.add(this.mapSearchHitToPripMetadata(hit));
			}
		}
		return result;
	}

	private static void buildQueryWithGeometryFilter(PripGeometryFilter filter, BoolQueryBuilder queryBuilder) {
		switch (filter.getFunction()) {
		case INTERSECTS:
			try {
				queryBuilder.must(QueryBuilders.geoIntersectionQuery(filter.getFieldName(),
						convertGeometry(filter.getGeometry())));
			} catch (final IOException e) {
				throw new IllegalArgumentException(
						String.format("not supported filter function: %s", filter.getFunction().name()));
			}
			break;
		case DISJOINTS:
			try {
				queryBuilder.must(
						QueryBuilders.geoDisjointQuery(filter.getFieldName(), convertGeometry(filter.getGeometry())));
			} catch (final IOException e) {
				throw new IllegalArgumentException(
						String.format("not supported filter function: %s", filter.getFunction().name()));
			}
			break;
		case WITHIN:
			try {
				queryBuilder.must(
						QueryBuilders.geoWithinQuery(filter.getFieldName(), convertGeometry(filter.getGeometry())));
			} catch (final IOException e) {
				throw new IllegalArgumentException(
						String.format("not supported filter function: %s", filter.getFunction().name()));
			}
			break;
		default:
			throw new IllegalArgumentException(
					String.format("not supported filter function: %s", filter.getFunction().name()));
		}
	}

	private static Geometry convertGeometry(org.locationtech.jts.geom.Geometry input) {
		if (input instanceof Polygon) {
			final CoordinatesBuilder coordBuilder = new CoordinatesBuilder();
			final Polygon polygon = (Polygon) input;
			for (final Coordinate coord : polygon.getCoordinates()) {
				final double lon = coord.x;
				final double lat = coord.y;
				coordBuilder.coordinate(lon, lat);
			}
			return new PolygonBuilder(coordBuilder).buildGeometry();
		} else {
			throw new IllegalArgumentException(String.format("not supported geometry: %s", input.getClass().getName()));
		}

	}

	private static SortOrder sortOrderFor(String sortOrder) {
		if (SortOrder.ASC.name().equalsIgnoreCase(sortOrder) || SortOrder.ASC.toString().equalsIgnoreCase(sortOrder)) {
			return SortOrder.ASC;
		}
		if (SortOrder.DESC.name().equalsIgnoreCase(sortOrder)
				|| SortOrder.DESC.toString().equalsIgnoreCase(sortOrder)) {
			return SortOrder.DESC;
		}

		throw new IllegalArgumentException(String.format("sort order not supported: %s", sortOrder));
	}

	private PripMetadata mapSearchHitToPripMetadata(SearchHit hit) {
		final Map<String, Object> sourceAsMap = hit.getSourceAsMap();
		final PripMetadata pm = new PripMetadata();

		pm.setId(UUID.fromString((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.ID.fieldName())));
		pm.setObsKey((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.OBS_KEY.fieldName()));
		pm.setName((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.NAME.fieldName()));
		pm.setProductFamily(
				ProductFamily.valueOf((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.PRODUCT_FAMILY.fieldName())));
		pm.setContentType((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CONTENT_TYPE.fieldName()));
		pm.setContentLength(
				Long.parseLong(sourceAsMap.get(PripMetadata.FIELD_NAMES.CONTENT_LENGTH.fieldName()).toString()));
		pm.setCreationDate(
				DateUtils.parse((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CREATION_DATE.fieldName())));
		pm.setEvictionDate(
				DateUtils.parse((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.EVICTION_DATE.fieldName())));

		if (Strings.isNotEmpty((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CONTENT_DATE_START.fieldName()))) {
			pm.setContentDateStart(
					DateUtils.parse((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CONTENT_DATE_START.fieldName())));
		}
		if (Strings.isNotEmpty((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CONTENT_DATE_END.fieldName()))) {
			pm.setContentDateEnd(
					DateUtils.parse((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CONTENT_DATE_END.fieldName())));
		}

		final List<Checksum> checksumList = new ArrayList<>();
		for (final Map<String, Object> c : (List<Map<String, Object>>) sourceAsMap
				.get(PripMetadata.FIELD_NAMES.CHECKSUM.fieldName())) {
			final Checksum checksum = new Checksum();
			checksum.setAlgorithm((String) c.get(Checksum.FIELD_NAMES.ALGORITHM.fieldName()));
			checksum.setValue((String) c.get(Checksum.FIELD_NAMES.VALUE.fieldName()));

			final String dateStr = (String) c.get(Checksum.FIELD_NAMES.DATE.fieldName());
			if (StringUtil.isNotBlank(dateStr)) {
				checksum.setDate(DateUtils.parse(dateStr));
			}

			checksumList.add(checksum);
		}
		pm.setChecksums(checksumList);

		pm.setFootprint(this.mapToGeoShapePolygon(sourceAsMap));

		pm.setAttributes(sourceAsMap.entrySet().stream().filter(p -> p.getKey().startsWith("attr_"))
				.collect(Collectors.toMap(Entry::getKey, s -> {
					final String key = s.getKey();
					final Object value = s.getValue();
					if (key.endsWith("_date")) {
						return DateUtils.parse((String) value);
					} else if (key.endsWith("_double")) {
						if (value instanceof Long) {
							return Double.valueOf((Long) value);
						} else if (value instanceof Integer) {
							return Double.valueOf((Integer) value);
						}
					} else if (key.endsWith("_long") && value instanceof Integer) {
						return Long.valueOf((Integer) value);
					}
					return value;
				})));

		LOGGER.debug("hit {}", pm);
		return pm;
	}

	private GeoShapePolygon mapToGeoShapePolygon(Map<String, Object> sourceAsMap) {
		final Map<String, Object> footprintJson = (Map<String, Object>) sourceAsMap
				.get(PripMetadata.FIELD_NAMES.FOOTPRINT.fieldName());

		if (null != footprintJson && !footprintJson.isEmpty()) {
			final String footprintGeoshapeType = (String) footprintJson.get(PripGeoShape.FIELD_NAMES.TYPE.fieldName());
			if (!GeoShapeType.POLYGON.wktName().equals(footprintGeoshapeType)) {
				throw new IllegalArgumentException(
						"PRIP metadata attribute value of " + PripMetadata.FIELD_NAMES.FOOTPRINT.fieldName() + "."
								+ PripGeoShape.FIELD_NAMES.TYPE.fieldName() + " must be '"
								+ GeoShapeType.POLYGON.wktName() + "' but is '" + footprintGeoshapeType + "'!");
			}

			final List<Object> footprintCoordinatesOuterArray = (List<Object>) footprintJson
					.get(PripGeoShape.FIELD_NAMES.COORDINATES.fieldName());
			final List<Object> footprintCoordinatesInnerArray = (List<Object>) footprintCoordinatesOuterArray.get(0);

			final List<PripGeoCoordinate> pripGeoCoordinates = new ArrayList<>();
			for (final Object coordPair : Objects.requireNonNull(footprintCoordinatesInnerArray)) {
				final List<Number> coords = (List<Number>) coordPair;
				final double lon = coords.get(0).doubleValue();
				final double lat = coords.get(1).doubleValue();
				pripGeoCoordinates.add(new PripGeoCoordinate(lon, lat));
			}

			return new GeoShapePolygon(pripGeoCoordinates);
		}

		return null;
	}

	@Override
	public int countAll() {
		LOGGER.info("counting PRIP metadata");
		return this.count(null);
	}

	@Override
	public int countWithFilters(List<PripQueryFilter> filters) {
		LOGGER.info("counting PRIP metadata with filters {}", filters);

		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		buildQueryWithFilters(filters, queryBuilder);

		return this.count(queryBuilder);
	}

	private int count(BoolQueryBuilder queryBuilder) {
		int count = 0;
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		if (queryBuilder != null) {
			searchSourceBuilder.query(queryBuilder);
		}

		final CountRequest countRequest = new CountRequest(ES_INDEX);
		countRequest.source(searchSourceBuilder);

		try {
			count = new Long(this.restHighLevelClient.count(countRequest, RequestOptions.DEFAULT).getCount())
					.intValue();
			LOGGER.info("counting PRIP metadata successful, number of hits {}", count);
		} catch (final IOException e) {
			LOGGER.error("error while counting PRIP metadata", e);
		}
		return count;
	}

}
