package esa.s1pdgs.cpoc.prip.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.common.geo.GeoShapeType;
import org.elasticsearch.common.geo.Orientation;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.LineStringBuilder;
import org.elasticsearch.common.geo.builders.PointBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.geometry.Geometry;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.FootprintUtil;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.GeoShapeLineString;
import esa.s1pdgs.cpoc.prip.model.GeoShapePolygon;
import esa.s1pdgs.cpoc.prip.model.PripGeoCoordinate;
import esa.s1pdgs.cpoc.prip.model.PripGeoShape;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm;
import esa.s1pdgs.cpoc.prip.model.PripSortTerm.PripSortOrder;
import esa.s1pdgs.cpoc.prip.model.filter.NestableQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripGeometryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList.LogicalOperator;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterTerm;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripInFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;

@Service
public class PripElasticSearchMetadataRepo implements PripMetadataRepository {

	private static final Logger LOGGER = LogManager.getLogger(PripElasticSearchMetadataRepo.class);
	private static final String ES_INDEX = "prip";
	private final int maxSearchHits;
	private final boolean searchStringTermInLowerCase;

	private final RestHighLevelClient restHighLevelClient;

	@Autowired
	public PripElasticSearchMetadataRepo(@Qualifier("pripEsClient") RestHighLevelClient restHighLevelClient,
			@Value("${prip-client.repository.max-search-hits:1000}") final int maxSearchHits,
			@Value("${prip-client.repository.search-string-term-in-lower-case:true}") final boolean searchStringTermInLowerCase) {
		this.restHighLevelClient = restHighLevelClient;
		this.maxSearchHits = maxSearchHits;
		this.searchStringTermInLowerCase = searchStringTermInLowerCase;
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
					throw new RuntimeException("could not save PRIP metadata");
				}
			}
		} catch (final IOException e) {
			LOGGER.error("could not save PRIP metadata", e);
			throw new RuntimeException("could not save PRIP metadata", e);
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
			LOGGER.error("error while finding PRIP metadata", e);
			throw new RecoverableDataAccessException("Could not read from Elasticsearch", e);
		}
		LOGGER.info("finding PRIP metadata successful");
		return pripMetadata;
	}
	
	@Override
	public boolean deleteByName(String name) {
		LOGGER.info("delete PRIP metadata with name {}", name);

		PripMetadata pripMetadata = findByName(name);

		if (pripMetadata == null) {
			return false;
		}

		DeleteResponse deleteResponse;
		try {
			deleteResponse = this.restHighLevelClient.delete(new DeleteRequest(ES_INDEX, pripMetadata.getName()),
					RequestOptions.DEFAULT);
		} catch (final IOException e) {
			throw new RuntimeException("Failed to delete product " + name, e);
		}

		if (deleteResponse.getResult() == Result.DELETED) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public PripMetadata findByName(String name) {
		LOGGER.info("finding PRIP metadata with name {}", name);

		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.matchQuery(PripMetadata.FIELD_NAMES.NAME.fieldName(), name));

		final SearchRequest searchRequest = new SearchRequest(ES_INDEX);
		searchRequest.source(sourceBuilder);

		PripMetadata pripMetadata = null;

		try {
			final SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
			LOGGER.trace("response {}", searchResponse);
	
			if (searchResponse.getHits().getHits().length > 0) {
				pripMetadata = this.mapSearchHitToPripMetadata(searchResponse.getHits().getHits()[0]);
				LOGGER.info("PRIP metadata with name {} found", name);
			} else {
				LOGGER.info("PRIP metadata with name {} not found", name);
			}
		} catch (IOException e) {
			LOGGER.error("error while finding PRIP metadata", e);
			throw new RecoverableDataAccessException("Could not read from Elasticsearch", e);
		}

		return pripMetadata;
	}

	@Override
	public List<PripMetadata> findAll(Optional<Integer> top, Optional<Integer> skip, List<PripSortTerm> sortTerms) {
		LOGGER.info("finding PRIP metadata");

		return this.queryWithOffset(null, top, skip, sortTerms);
	}

	@Override
	public List<PripMetadata> findWithFilter(final PripQueryFilter filter, final Optional<Integer> top, final Optional<Integer> skip,
			final List<PripSortTerm> sortTerms) {
		LOGGER.info("finding PRIP metadata with filters: {}", filter);
		final BoolQueryBuilder query = buildQueryWithFilter(filter);

		if (query.hasClauses()) {
			return this.queryWithOffset(query, top, skip, sortTerms);
		} else {
			return this.findAll(top, skip, sortTerms);
		}
	}

	private BoolQueryBuilder buildQueryWithFilter(final PripQueryFilter rootFilter) {
		final BoolQueryBuilder rootQuery = QueryBuilders.boolQuery();

		if (rootFilter instanceof PripQueryFilterTerm) {
			// a single filter term at root level must match, therefore using AND operator for query
			final LogicalOperator operator = LogicalOperator.AND;
			appendFilterTerm(rootQuery, operator, (PripQueryFilterTerm) rootFilter);

		} else if (rootFilter instanceof PripQueryFilterList) {
			// dig deeper into the filter hierarchy
			appendFilterList(rootQuery, (PripQueryFilterList) rootFilter);
		} else {
			throw new IllegalArgumentException(String.format("filter type not supported: %s", rootFilter.getClass().getSimpleName()));
		}

		return rootQuery;
	}

	private void appendFilterList(final BoolQueryBuilder queryBuilder, final PripQueryFilterList filterList) {
		final LogicalOperator operator = filterList.getOperator();
		final List<PripQueryFilter> filters = CollectionUtil.nullToEmptyList((filterList).getFilterList());

		filters.forEach(filter -> {
			if (filter instanceof PripQueryFilterTerm) {
				appendFilterTerm(queryBuilder, operator, (PripQueryFilterTerm) filter);

			} else if (filter instanceof PripQueryFilterList) {
				final BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
				appendFilterList(subQuery, (PripQueryFilterList) filter);

				// append sub query
				if (LogicalOperator.AND == operator) {
					queryBuilder.must(subQuery);
				} else if (LogicalOperator.OR == operator) {
					queryBuilder.should(subQuery);
				} else if (LogicalOperator.NOT == operator) {
					queryBuilder.mustNot(subQuery);
				} else {
					throw new IllegalArgumentException(String.format("logocal filter operator not supported: %s", operator.name()));
				}
			} else {
				throw new IllegalArgumentException(String.format("filter type not supported: %s", filter.getClass().getSimpleName()));
			}
		});
	}

	private void appendFilterTerm(final BoolQueryBuilder queryBuilder, final LogicalOperator operator, final PripQueryFilterTerm filterTerm) {
		if (filterTerm instanceof PripRangeValueFilter) {
			buildQueryWithRangeValueFilter((PripRangeValueFilter<?>) filterTerm, queryBuilder, operator);
		} else if (filterTerm instanceof PripTextFilter) {
			buildQueryWithTextFilter((PripTextFilter) filterTerm, queryBuilder, operator);
		} else if (filterTerm instanceof PripBooleanFilter) {
			buildQueryWithBooleanFilter((PripBooleanFilter) filterTerm, queryBuilder, operator);
		} else if (filterTerm instanceof PripGeometryFilter) {
			buildQueryWithGeometryFilter((PripGeometryFilter) filterTerm, queryBuilder, operator);
		} else if (filterTerm instanceof PripInFilter) {
			buildQueryWithTermsFilter((PripInFilter) filterTerm, queryBuilder, operator);
		} else {
			throw new IllegalArgumentException(String.format("filter type not supported: %s", filterTerm.getClass().getSimpleName()));
		}
	}

	private void buildQueryWithRangeValueFilter(final PripRangeValueFilter<?> filter, final BoolQueryBuilder queryBuilder,
			final LogicalOperator operator) {
		switch (filter.getRelationalOperator()) {
		case LT:
			appendQuery(queryBuilder, operator, QueryBuilders.rangeQuery(filter.getFieldName()).lt(filter.getValue()), filter);
			break;
		case LE:
			appendQuery(queryBuilder, operator, QueryBuilders.rangeQuery(filter.getFieldName()).lte(filter.getValue()), filter);
			break;
		case GT:
			appendQuery(queryBuilder, operator, QueryBuilders.rangeQuery(filter.getFieldName()).gt(filter.getValue()), filter);
			break;
		case GE:
			appendQuery(queryBuilder, operator, QueryBuilders.rangeQuery(filter.getFieldName()).gte(filter.getValue()), filter);
			break;
		case EQ:
			appendQuery(queryBuilder, operator, QueryBuilders.termQuery(filter.getFieldName(), filter.getValue()), filter);
			break;
		case NE:
			appendQueryNegated(queryBuilder, operator, QueryBuilders.termQuery(filter.getFieldName(), filter.getValue()), filter);
			break;
		default:
			throw new IllegalArgumentException(String.format("relational filter operator not supported: %s", filter.getRelationalOperator().name()));
		}
	}

	private void buildQueryWithTextFilter(final PripTextFilter filter, final BoolQueryBuilder queryBuilder, final LogicalOperator operator) {
		switch (filter.getFunction()) {
		case STARTS_WITH:
			appendQuery(queryBuilder, operator, QueryBuilders.wildcardQuery(filter.getFieldName(), String.format("%s*", filter.getText())), filter);
			break;
		case ENDS_WITH:
			appendQuery(queryBuilder, operator, QueryBuilders.wildcardQuery(filter.getFieldName(), String.format("*%s", filter.getText())), filter);
			break;
		case CONTAINS:
			appendQuery(queryBuilder, operator, QueryBuilders.wildcardQuery(filter.getFieldName(), String.format("*%s*", filter.getText())), filter);
			break;
		case EQ:
			appendQuery(queryBuilder, operator,
					QueryBuilders.matchQuery(filter.getFieldName(), filter.getText()).fuzziness(Fuzziness.ZERO).operator(Operator.AND), filter);
			break;
      case NE:
         appendQueryNegated(queryBuilder, operator,
               QueryBuilders.matchQuery(filter.getFieldName(), filter.getText()).fuzziness(Fuzziness.ZERO).operator(Operator.AND), filter);
         break;
      default:
			throw new IllegalArgumentException(String.format("not supported filter function: %s", filter.getFunction().name()));
		}
	}

	private void buildQueryWithBooleanFilter(final PripBooleanFilter filter, final BoolQueryBuilder queryBuilder, final LogicalOperator operator) {
		switch (filter.getFunction()) {
		case EQ:
			appendQuery(queryBuilder, operator, QueryBuilders.termQuery(filter.getFieldName(), filter.getValue().booleanValue()), filter);
			break;
		case NE:
			appendQueryNegated(queryBuilder, operator, QueryBuilders.termQuery(filter.getFieldName(), filter.getValue().booleanValue()), filter);
			break;
		default:
			throw new IllegalArgumentException(String.format("not supported filter function: %s", filter.getFunction().name()));
		}
	}
	
	private void buildQueryWithGeometryFilter(final PripGeometryFilter filter, final BoolQueryBuilder queryBuilder, final LogicalOperator operator) {
		switch (filter.getFunction()) {
		case INTERSECTS:
			try {
				appendQuery(queryBuilder, operator, QueryBuilders.geoIntersectionQuery(filter.getFieldName(), convertGeometry(filter.getGeometry())), filter);
			} catch (final IOException e) {
				throw new IllegalArgumentException(String.format("not supported filter function: %s", filter.getFunction().name()));
			}
			break;
		case DISJOINTS:
			try {
				appendQuery(queryBuilder, operator, QueryBuilders.geoDisjointQuery(filter.getFieldName(), convertGeometry(filter.getGeometry())), filter);
			} catch (final IOException e) {
				throw new IllegalArgumentException(String.format("not supported filter function: %s", filter.getFunction().name()));
			}
			break;
		case WITHIN:
			try {
				appendQuery(queryBuilder, operator, QueryBuilders.geoWithinQuery(filter.getFieldName(), convertGeometry(filter.getGeometry())), filter);
			} catch (final IOException e) {
				throw new IllegalArgumentException(String.format("not supported filter function: %s", filter.getFunction().name()));
			}
			break;
		default:
			throw new IllegalArgumentException(String.format("not supported filter function: %s", filter.getFunction().name()));
		}
	}

	private void buildQueryWithTermsFilter(final PripInFilter filter, final BoolQueryBuilder queryBuilder, final LogicalOperator operator) {
		switch (filter.getFunction()) {
		case IN:
			if (searchStringTermInLowerCase && filter.getFieldName().endsWith(PripInFilter.FIELD_TYPE_STRING)) {
				appendQuery(queryBuilder, operator, QueryBuilders.termsQuery(filter.getFieldName(), filter.getTermsInLowerCase()), filter);
			} else {
				appendQuery(queryBuilder, operator, QueryBuilders.termsQuery(filter.getFieldName(), filter.getTerms()), filter);
			}
			break;
		default:
			throw new IllegalArgumentException(String.format("not supported filter function: %s", filter.getFunction().name()));
		}
	}
	
	private void appendQuery(final BoolQueryBuilder queryBuilder, final LogicalOperator operator, final QueryBuilder queryToAppend,
			final PripQueryFilter filter) {
		final QueryBuilder query;
		if (filter instanceof NestableQueryFilter && ((NestableQueryFilter) filter).isNested()) {
			query = QueryBuilders.nestedQuery(((NestableQueryFilter) filter).getPath(), queryToAppend, ScoreMode.None);
		} else {
			query = queryToAppend;
		}

		if (LogicalOperator.AND == operator) {
			queryBuilder.must(query);
		} else if (LogicalOperator.OR == operator) {
			queryBuilder.should(query);
		} else if (LogicalOperator.NOT == operator) {
			queryBuilder.mustNot(query);
		} else {
			throw new IllegalArgumentException(String.format("logocal filter operator not supported: %s", operator.name()));
		}
	}

	private void appendQueryNegated(final BoolQueryBuilder queryBuilder, final LogicalOperator operator, final QueryBuilder queryToAppend,
			final PripQueryFilter filter) {
		final QueryBuilder query;
		if (filter instanceof NestableQueryFilter && ((NestableQueryFilter) filter).isNested()) {
			query = QueryBuilders.nestedQuery(((NestableQueryFilter) filter).getPath(), queryToAppend, ScoreMode.None);
		} else {
			query = queryToAppend;
		}
		
		if (LogicalOperator.AND == operator) {
			queryBuilder.mustNot(query);
		} else if (LogicalOperator.OR == operator) {
			// there is no should_not, so we use should(must_not(match(value)))
			queryBuilder.should(QueryBuilders.boolQuery().mustNot(query));
		} else {
			throw new IllegalArgumentException(String.format("logocal filter operator not supported: %s", operator.name()));
		}
	}

	private List<PripMetadata> queryWithOffset(BoolQueryBuilder queryBuilder, Optional<Integer> top,
			Optional<Integer> skip, List<PripSortTerm> sortTerms) {

		List<PripMetadata> result = new ArrayList<>();
		if (skip.orElse(0) <= 0 || skip.orElse(0) + top.orElse(0) <= this.maxSearchHits) {
			// Paging through less than maxSearchHits -> default behaviour
			LOGGER.info(
					"Handling query with skip={} and top={} (max-search-hits={}) -> Use elastic classical pagination",
					skip.orElse(0), top.orElse(0), this.maxSearchHits);
			result.addAll(convert(this.query(queryBuilder, top, skip, sortTerms)));
		} else {
			// Paging through more than maxSearchHits ->
			// 1. iterate to offset (first page by using default mechanism, each further
			// page by using search_after)
			// 2. search_after(offset)
			Integer offset = skip.orElse(0);
			Integer pageSize = offset > maxSearchHits ? maxSearchHits : offset;
			List<SearchHit> offsetList = this.queryOffset(queryBuilder, Optional.of(pageSize), Optional.of(0),
					sortTerms, false, null);
			SearchHit offsetSearchHit = offsetList.get(offsetList.size() - 1);
			while (offset > maxSearchHits) {
				offsetList = this.queryOffset(queryBuilder, top, Optional.of(pageSize), sortTerms, true,
						offsetSearchHit.getSortValues());
				offsetSearchHit = offsetList.get(offsetList.size() - 1);
				offset = offset - offsetList.size();
				pageSize = offset > maxSearchHits ? maxSearchHits : offset;
			}

			LOGGER.info("Handling query with skip={} and top={} (max-search-hits={}) -> Use elastic search_after",
					skip.orElse(0), top.orElse(0), this.maxSearchHits);
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
			LOGGER.error("error while finding PRIP metadata", e);
			throw new RecoverableDataAccessException("Could not read from Elasticsearch", e);
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

	private Geometry convertGeometry(org.locationtech.jts.geom.Geometry input) {
		if (input instanceof Polygon) {
			final Polygon polygon = (Polygon) input;
			final List<Double> longitudes = new ArrayList<>();
			final CoordinatesBuilder coordBuilder = new CoordinatesBuilder();
			CollectionUtil.toList(polygon.getCoordinates()).forEach(coordinate -> {
				coordBuilder.coordinate(coordinate.x, coordinate.y);
				longitudes.add(coordinate.x);
			});
			final Orientation orientation = Orientation.fromString(
					FootprintUtil.elasticsearchPolygonOrientation(longitudes.toArray(new Double[0])));
			return new PolygonBuilder(coordBuilder, orientation).buildGeometry();

		} else if (input instanceof LineString) {
			final LineString lineString = (LineString) input;
			final CoordinatesBuilder coordBuilder = new CoordinatesBuilder();
			CollectionUtil.toList(lineString.getCoordinates()).forEach(coordinate -> coordBuilder.coordinate(coordinate.x, coordinate.y));

			return new LineStringBuilder(coordBuilder).buildGeometry();

		} else if (input instanceof Point) {
			final Point point = (Point) input;

			return new PointBuilder(point.getX(), point.getY()).buildGeometry();

		} else {
			throw new IllegalArgumentException(String.format("not supported geometry: %s", (null != input) ? input.getClass().getName() : "null"));
		}
	}

	private SortOrder sortOrderFor(String sortOrder) {
		if (SortOrder.ASC.name().equalsIgnoreCase(sortOrder) || SortOrder.ASC.toString().equalsIgnoreCase(sortOrder)) {
			return SortOrder.ASC;
		}
		if (SortOrder.DESC.name().equalsIgnoreCase(sortOrder)
				|| SortOrder.DESC.toString().equalsIgnoreCase(sortOrder)) {
			return SortOrder.DESC;
		}

		throw new IllegalArgumentException(String.format("sort order not supported: %s", sortOrder));
	}

	@SuppressWarnings("unchecked")
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
		if (Strings.isNotEmpty((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.ORIGIN_DATE.fieldName()))) {
			pm.setOriginDate(
					DateUtils.parse((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.ORIGIN_DATE.fieldName())));
		}
		
		// If no value is found, we assume it to be true for backward compatibility reasons
		if (sourceAsMap.get(PripMetadata.FIELD_NAMES.ONLINE.fieldName()) != null) {
			pm.setOnline((Boolean)sourceAsMap.get(PripMetadata.FIELD_NAMES.ONLINE.fieldName()));
		} else {
			pm.setOnline(true);
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

		pm.setFootprint(this.mapToPripGeoShape(sourceAsMap));

		// Collectors.toMap has a known bug: https://bugs.openjdk.org/browse/JDK-8261865
//		pm.setAttributes(sourceAsMap.entrySet().stream().filter(p -> p.getKey().startsWith("attr_"))
//				.collect(Collectors.toMap(Entry::getKey, s -> {
//					final String key = s.getKey();
//					final Object value = s.getValue();
//					if (key.endsWith("_date")) {
//						return DateUtils.parse((String) value);
//					} else if (key.endsWith("_double")) {
//						if (value instanceof Long) {
//							return Double.valueOf((Long) value);
//						} else if (value instanceof Integer) {
//							return Double.valueOf((Integer) value);
//						}
//					} else if (key.endsWith("_long") && value instanceof Integer) {
//						return Long.valueOf((Integer) value);
//					}
//					return value;
//				})));		
		Map<String, Object> attributeMap = new HashMap<>();
		for (Entry<String, Object> entry : sourceAsMap.entrySet()) {
			if (entry.getKey().startsWith("attr_")) {
				
				if (entry.getKey() == "attr_processingDate_date") {
					LOGGER.trace("Processing attr_processingDate_date");
				}
				
				final String key = entry.getKey();
				final Object value = entry.getValue();
				if (key.endsWith("_date")) {
					attributeMap.put(key, DateUtils.parse((String) value));
				} else if (key.endsWith("_double")) {
					if (value instanceof Long) {
						attributeMap.put(key, Double.valueOf((Long) value));
					} else if (value instanceof Integer) {
						attributeMap.put(key, Double.valueOf((Integer) value));
					}
				} else if (key.endsWith("_long") && value instanceof Integer) {
					attributeMap.put(key, Long.valueOf((Integer) value));
				}
				if (!attributeMap.containsKey(key)) {
					attributeMap.put(key, value);
				}
			}
		}
		pm.setAttributes(attributeMap);

      List<String> browseKeys = (List<String>)sourceAsMap.get(PripMetadata.FIELD_NAMES.BROWSE_KEYS.fieldName());
		if (null == browseKeys) {
		   browseKeys = new ArrayList<>();
		}
		pm.setBrowseKeys(browseKeys);

		LOGGER.trace("hit {}", pm);
		return pm;
	}

	@SuppressWarnings("unchecked")
	private PripGeoShape mapToPripGeoShape(Map<String, Object> sourceAsMap) {
		final Map<String, Object> footprintJson = (Map<String, Object>) sourceAsMap
				.get(PripMetadata.FIELD_NAMES.FOOTPRINT.fieldName());

		if (null != footprintJson && !footprintJson.isEmpty()) {
			final String footprintGeoshapeType = (String) footprintJson.get(PripGeoShape.FIELD_NAMES.TYPE.fieldName());

			if (GeoShapeType.POLYGON.wktName().equalsIgnoreCase(footprintGeoshapeType)) {
				final List<Object> footprintCoordinatesOuterArray = (List<Object>) footprintJson
						.get(PripGeoShape.FIELD_NAMES.COORDINATES.fieldName());
				final List<Object> footprintCoordinatesInnerArray = (List<Object>) footprintCoordinatesOuterArray
						.get(0);

				final List<PripGeoCoordinate> pripGeoCoordinates = new ArrayList<>();
				for (final Object coordPair : Objects.requireNonNull(footprintCoordinatesInnerArray)) {
					final List<Number> coords = (List<Number>) coordPair;
					final double lon = coords.get(0).doubleValue();
					final double lat = coords.get(1).doubleValue();
					pripGeoCoordinates.add(new PripGeoCoordinate(lon, lat));
				}
				return new GeoShapePolygon(pripGeoCoordinates);
			} else if (GeoShapeType.LINESTRING.wktName().equalsIgnoreCase(footprintGeoshapeType)) {
				final List<Object> footprintCoordinatesOuterArray = (List<Object>) footprintJson
						.get(PripGeoShape.FIELD_NAMES.COORDINATES.fieldName());

				final List<PripGeoCoordinate> pripGeoCoordinates = new ArrayList<>();
				for (final Object coordPair : Objects.requireNonNull(footprintCoordinatesOuterArray)) {
					final List<Number> coords = (List<Number>) coordPair;
					final double lon = coords.get(0).doubleValue();
					final double lat = coords.get(1).doubleValue();
					pripGeoCoordinates.add(new PripGeoCoordinate(lon, lat));
				}
				return new GeoShapeLineString(pripGeoCoordinates);
			} else {
				throw new IllegalArgumentException("PRIP metadata attribute value of "
						+ PripMetadata.FIELD_NAMES.FOOTPRINT.fieldName() + "."
						+ PripGeoShape.FIELD_NAMES.TYPE.fieldName() + " must be '" + GeoShapeType.POLYGON.wktName()
						+ "' or '" + GeoShapeType.LINESTRING.wktName() + "' but is '" + footprintGeoshapeType + "'!");
			}
		}
		return null;
	}

	@Override
	public int countAll() {
		LOGGER.info("counting PRIP metadata");
		return this.count(null);
	}

	@Override
	public int countWithFilter(final PripQueryFilter filter) {
		LOGGER.info("counting PRIP metadata with filters {}", filter);
		final BoolQueryBuilder query = buildQueryWithFilter(filter);

		if (query.hasClauses()) {
			return this.count(query);
		} else {
			return this.countAll();
		}
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
			count = (int) (this.restHighLevelClient.count(countRequest, RequestOptions.DEFAULT).getCount());
			LOGGER.info("counting PRIP metadata successful, number of hits {}", count);
		} catch (final IOException e) {
			LOGGER.error("error while counting PRIP metadata", e);
			throw new RecoverableDataAccessException("Could not read from Elasticsearch", e);
		}
		return count;
	}

}
