package esa.s1pdgs.cpoc.mdc.worker.service;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.geometry.Geometry;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoShapeQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataCreationException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.mdc.worker.es.ElasticsearchDAO;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;

/**
 * Service for accessing to elasticsearch data
 * 
 * @author Cyrielle
 *
 */
@Service
public class EsServices {

	private static final String REQUIRED_INSTRUMENT_ID_PATTERN = "(aux_pp1|aux_pp2|aux_cal|aux_ins)";
	static final String REQUIRED_SATELLITE_ID_PATTERN = "(aux_.*)";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(EsServices.class);

	private static final int SIZE_LIMIT = 1000;

	/**
	 * Elasticsearch client
	 */
	private final ElasticsearchDAO elasticsearchDAO;

	@Autowired
	public EsServices(final ElasticsearchDAO elasticsearchDAO) {
		this.elasticsearchDAO = elasticsearchDAO;
	}

	/**
	 * Check if a given metadata already exist
	 * 
	 */
	public boolean isMetadataExist(final JSONObject product) throws Exception {
		try {
			final String productType;
			if (ProductFamily.AUXILIARY_FILE.equals(ProductFamily.valueOf(product.getString("productFamily")))
					|| ProductFamily.EDRS_SESSION.equals(ProductFamily.valueOf(product.getString("productFamily")))) {
				productType = product.getString("productType").toLowerCase();
			} else {
				productType = product.getString("productFamily").toLowerCase();
			}
			final String productName = product.getString("productName");

			final GetRequest getRequest = new GetRequest(productType, productName);

			final GetResponse response = elasticsearchDAO.get(getRequest);

			LOGGER.debug("Product {} response from ES {}", productName, response);

			return response.isExists();
		} catch (final JSONException | IOException je) {
			throw new Exception(je.getMessage());
		}
	}

	public void createMetadataWithRetries(final JSONObject product, final String productName, final int numRetries,
			final long retrySleep) throws InterruptedException {
		Retries.performWithRetries(() -> {
			if (!isMetadataExist(product)) {
				LOGGER.debug("Creating metadata in ES for product {}", productName);
				createMetadata(product);
			} else {
				LOGGER.debug("ES already contains metadata for product {}", productName);
			}
			return null;
		}, "Create metadata " + product, numRetries, retrySleep);
	}

	/**
	 * Save the metadata in elastic search. The metadata data is created in the
	 * index named [productType] with id [productName]
	 * 
	 */
	public void createMetadata(final JSONObject product) throws Exception {
		try {
			final String productType;
			final ProductFamily family = ProductFamily.valueOf(product.getString("productFamily"));

			if (ProductFamily.AUXILIARY_FILE.equals(family) || ProductFamily.EDRS_SESSION.equals(family)) {
				productType = product.getString("productType").toLowerCase();
			} else {
				productType = product.getString("productFamily").toLowerCase();
			}
			final String productName = product.getString("productName");

			IndexRequest request = new IndexRequest(productType).id(productName).source(product.toString(),
					XContentType.JSON);

			IndexResponse response;
			try {
				response = elasticsearchDAO.index(request);
			} catch (final ElasticsearchStatusException e) {
				/*
				 * S1PRO-783: This is a temporary work around for the WV footprint issue that
				 * occurs for WV products when the footprint does cross the date line border. As
				 * it is currently not possible to submit these kind of products, we are not
				 * failing immediately, but trying to resubmit it without a footprint.
				 * 
				 * This is a workaround and will be obsoleted by S1PRO-778. Due to no defined
				 * pattern, we have to parse the exception to identify possible footprint
				 * issues.
				 */
				LOGGER.warn("An exception occurred while accessing the elastic search index: {}", LogUtils.toString(e));
				final String result = e.getMessage();
				boolean fixed = false;
				if (result.contains("failed to parse field [sliceCoordinates] of type [geo_shape]")) {
					LOGGER.warn(
							"Parsing error occurred for sliceCoordinates, dropping them as workaround for #S1PRO-783");
					product.remove("sliceCoordinates");
					fixed = true;
				}

				if (result.contains("failed to parse field [segmentCoordinates] of type [geo_shape]")) {
					LOGGER.warn(
							"Parsing error occurred for segmentCoordinates, dropping them as workaround for #S1PRO-783");
					product.remove("segmentCoordinates");
					fixed = true;
				}

				if (!fixed) {
					throw e;
				}

				LOGGER.debug("Content of JSON second attempt: {}", product.toString());

				request = new IndexRequest(productType).id(productName).source(product.toString(), XContentType.JSON);
				response = elasticsearchDAO.index(request);
				// END OF WORKAROUND S1PRO-783
			}

			if (response.status() != RestStatus.CREATED) {
				// If it still fails, we cannot fix it. Raise exception
				if (response.status() != RestStatus.CREATED) {
					throw new MetadataCreationException(productName, response.status().toString(),
							response.getResult().toString());
				}

			}
		} catch (JSONException | IOException e) {
			throw new Exception(e);
		}
	}

	public void createGeoMetadata(final JSONObject product, final String landName) throws Exception {
		try {
//			String landName = product.getString("name");

			final IndexRequest request = new IndexRequest("landmask").id(landName).source(product.toString(),
					XContentType.JSON);

			final IndexResponse response = elasticsearchDAO.index(request);

			if (response.status() != RestStatus.CREATED) {
				throw new MetadataCreationException(landName, response.status().toString(),
						response.getResult().toString());
			}
		} catch (JSONException | IOException e) {
			throw new Exception(e);
		}
	}

	/**
	 * Function which return the products that correspond to the valCover
	 * specification If there is no corresponding product return null
	 * 
	 * @return a list of object storage keys of the chosen product
	 */
	public List<SearchMetadata> valCover(final String productType, final ProductFamily productFamily,
			final String beginDate, final String endDate, final String satelliteId, final int instrumentConfId,
			final String processMode) throws Exception {

		final ProductCategory category = ProductCategory.of(productFamily);

		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("validityStartTime").lt(beginDate))
				.must(QueryBuilders.rangeQuery("validityStopTime").gt(endDate)).must(satelliteId(satelliteId));
		// Product type
		if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
			queryBuilder = queryBuilder.must(QueryBuilders.regexpQuery("productType.keyword", productType));
		} else {
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("productType.keyword", productType));
		}
		// Instrument configuration id
		if (instrumentConfId != -1 && productType.toLowerCase().matches(REQUIRED_INSTRUMENT_ID_PATTERN)) {
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("instrumentConfigurationId", instrumentConfId));
		}
		// Process mode
		if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("processMode.keyword", processMode));
		}
		LOGGER.debug("query composed is {}", queryBuilder);

		sourceBuilder.query(queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);

		final SearchRequest searchRequest = new SearchRequest(getIndexForProductFamily(productFamily, productType));
		searchRequest.source(sourceBuilder);
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			final List<SearchMetadata> r = new ArrayList<>();
			if (this.isNotEmpty(searchResponse)) {
				for (final SearchHit hit : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = hit.getSourceAsMap();
					final SearchMetadata local = new SearchMetadata();
					local.setProductName(source.get("productName").toString());
					local.setProductType(source.get("productType").toString());
					local.setKeyObjectStorage(source.get("url").toString());
					if (source.containsKey("validityStartTime")) {
						try {
							local.setValidityStart(DateUtils
									.convertToMetadataDateTimeFormat(source.get("validityStartTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("validityStartTime");
						}
					}
					if (source.containsKey("validityStopTime")) {
						try {
							local.setValidityStop(DateUtils
									.convertToMetadataDateTimeFormat(source.get("validityStopTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("validityStopTime");
						}
					}
					r.add(local);
				}
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}

	/**
	 * Function which return the product that correspond to the lastValCover
	 * specification If there is no corresponding product return null
	 * 
	 * @return the key object storage of the chosen product
	 */
	public SearchMetadata lastValCover(final String productType, final ProductFamily productFamily,
			final String beginDate, final String endDate, final String satelliteId, final int instrumentConfId,
			final String processMode) throws Exception {

		final ProductCategory category = ProductCategory.of(productFamily);

		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("validityStartTime").lt(beginDate))
				.must(QueryBuilders.rangeQuery("validityStopTime").gt(endDate)).must(satelliteId(satelliteId));
		// Product type
		if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
			queryBuilder = queryBuilder.must(QueryBuilders.regexpQuery("productType.keyword", productType));
		} else {
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("productType.keyword", productType));
		}
		// Instrument configuration id
		if (instrumentConfId != -1 && productType.toLowerCase().matches(REQUIRED_INSTRUMENT_ID_PATTERN)) {
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("instrumentConfigurationId", instrumentConfId));
		}
		// Process mode
		if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("processMode.keyword", processMode));
		}
		LOGGER.debug("query composed is {}", queryBuilder);

		sourceBuilder.query(queryBuilder);

		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC));

		final SearchRequest searchRequest = new SearchRequest(getIndexForProductFamily(productFamily, productType));
		searchRequest.source(sourceBuilder);
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				final Map<String, Object> source = searchResponse.getHits().getAt(0).getSourceAsMap();
				final SearchMetadata r = new SearchMetadata();
				r.setProductName(source.get("productName").toString());
				r.setProductType(source.get("productType").toString());
				r.setKeyObjectStorage(source.get("url").toString());
				if (source.containsKey("validityStartTime")) {
					try {
						r.setValidityStart(
								DateUtils.convertToMetadataDateTimeFormat(source.get("validityStartTime").toString()));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStartTime");
					}
				}
				if (source.containsKey("validityStopTime")) {
					try {
						r.setValidityStop(
								DateUtils.convertToMetadataDateTimeFormat(source.get("validityStopTime").toString()));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStopTime");
					}
				}
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}

	/*
	 * ClosestStartValidity This policy uses a centre time, calculated as (t0-t1) /
	 * 2 to determinate auxiliary data, which is located nearest to the centre time.
	 * In order to do this, it checks the product located directly before and behind
	 * the centre time and selects the one with the smallest distance. If both
	 * distances are equal, the product before will be choose. select from File_Type
	 * where startTime < centreTime and there exists no corresponding File_Type with
	 * greater startTime where startTime < centreTime select from File_Type where
	 * startTime >= centreTime and there exists no corresponding File_Type with
	 * lesser startTime where startTime >= centreTime implementation.Needs to be
	 * implemented properly
	 */
	public SearchMetadata closestStartValidity(final String productType, final ProductFamily productFamily,
			final String beginDate, final String endDate, final String satelliteId, final int instrumentConfId,
			final String processMode) throws Exception {
		LOGGER.debug("Searching products via selection policy 'closestStartValidity' for {}, startDate {}, endDate {} ",
				productType, beginDate, endDate);

		// mimic the same behaviour used in the old processing system
		final LocalDateTime cTime = calculateCentreTime(beginDate, endDate);
		final String centreTime = DateUtils.formatToMetadataDateTimeFormat(cTime);

		final SearchRequest beforeRequest = newQueryFor(productType, productFamily, satelliteId, instrumentConfId,
				processMode, QueryBuilders.rangeQuery("validityStartTime").lt(centreTime),
				new FieldSortBuilder("validityStartTime").order(SortOrder.DESC), "NONE");
		final SearchRequest afterRequest = newQueryFor(productType, productFamily, satelliteId, instrumentConfId,
				processMode, QueryBuilders.rangeQuery("validityStartTime").gte(centreTime),
				new FieldSortBuilder("validityStartTime").order(SortOrder.ASC), "NONE");
		try {
			final SearchResponse beforeResponse = elasticsearchDAO.search(beforeRequest);
			final SearchResponse afterResponse = elasticsearchDAO.search(afterRequest);

			final SearchHits before = beforeResponse.getHits();
			final SearchHits after = afterResponse.getHits();

			LOGGER.debug("Total Hits Found before {} and after {}", this.getTotalSearchHitsStr(before),
					this.getTotalSearchHitsStr(after));

			if (this.isEmpty(before) && this.isNotEmpty(after)) {
				final SearchMetadata metaAfter = toSearchMetadata(after.getAt(0));
				LOGGER.debug("Candidate after was the best result, {}", metaAfter.getProductName());
				return metaAfter;
			} else if (this.isNotEmpty(before) && this.isEmpty(after)) {
				final SearchMetadata metaBefore = toSearchMetadata(before.getAt(0));
				LOGGER.debug("Candidate before was the best result, {}", metaBefore.getProductName());
				return metaBefore;
			} else if (this.isEmpty(before) && this.isEmpty(after)) {
				return null;
			}

			// "merge" functionality from old processing system implementation
			final SearchMetadata metaBefore = toSearchMetadata(before.getAt(0));
			final SearchMetadata metaAfter = toSearchMetadata(after.getAt(0));

			final Duration durationBefore = Duration.between(DateUtils.parse(metaBefore.getValidityStart()), cTime)
					.abs();
			final Duration durationAfter = Duration.between(DateUtils.parse(metaAfter.getValidityStart()), cTime).abs();

			if (durationBefore.compareTo(durationAfter) <= 0) {
				LOGGER.debug("Candidate before was the best result, {}", metaBefore.getProductName());
				return metaBefore;
			} else {
				LOGGER.debug("Candidate after was the best result, {}", metaAfter.getProductName());
				return metaAfter;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public SearchMetadata latestValidity(final String beginDate, final String endDate, final String productType,
			final ProductFamily productFamily, final String processMode, final String satelliteId) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("validityStartTime").lt(endDate))
				.must(QueryBuilders.rangeQuery("validityStopTime").gt(beginDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType));
		sourceBuilder.query(queryBuilder);
		LOGGER.debug("latestValidity: query composed is {}", queryBuilder);

		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("validityStartTime").order(SortOrder.DESC));

		final SearchRequest searchRequest = new SearchRequest(getIndexForProductFamily(productFamily, productType));
		searchRequest.source(sourceBuilder);
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				final Map<String, Object> source = searchResponse.getHits().getAt(0).getSourceAsMap();
				final SearchMetadata r = new SearchMetadata();
				r.setProductName(source.get("productName").toString());
				r.setProductType(source.get("productType").toString());
				r.setKeyObjectStorage(source.get("url").toString());
				if (source.containsKey("validityStartTime")) {
					try {
						r.setValidityStart(
								DateUtils.convertToMetadataDateTimeFormat(source.get("validityStartTime").toString()));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStartTime");
					}
				}
				if (source.containsKey("validityStopTime")) {
					try {
						r.setValidityStop(
								DateUtils.convertToMetadataDateTimeFormat(source.get("validityStopTime").toString()));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStopTime");
					}
				}
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;	
	}

	private SearchMetadata toSearchMetadata(final SearchHit hit) {
		final Map<String, Object> source = hit.getSourceAsMap();
		final SearchMetadata r = new SearchMetadata();
		r.setProductName(source.get("productName").toString());
		r.setProductType(source.get("productType").toString());
		r.setKeyObjectStorage(source.get("url").toString());
		if (source.containsKey("validityStartTime")) {
			r.setValidityStart(DateUtils.convertToMetadataDateTimeFormat(source.get("validityStartTime").toString()));
		}
		if (source.containsKey("validityStopTime")) {
			r.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("validityStopTime").toString()));
		}
		return r;
	}

	private SearchRequest newQueryFor(final String productType, final ProductFamily productFamily,
			final String satelliteId, final int instrumentConfId, final String processMode,
			final RangeQueryBuilder rangeQueryBuilder, final FieldSortBuilder sortOrder, final String polarisation) {
		final ProductCategory category = ProductCategory.of(productFamily);
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(rangeQueryBuilder);

		if (productType.toLowerCase().matches(REQUIRED_SATELLITE_ID_PATTERN)) {
			queryBuilder = queryBuilder.must(satelliteId(satelliteId));
		}

		if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
			queryBuilder = queryBuilder.must(QueryBuilders.regexpQuery("productType.keyword", productType));
		} else {
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("productType.keyword", productType));
		}
		// Instrument configuration id
		if (instrumentConfId != -1 && productType.toLowerCase().matches(REQUIRED_INSTRUMENT_ID_PATTERN)) {
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("instrumentConfigurationId", instrumentConfId));
		}
		// Process mode
		if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("processMode.keyword", processMode));
		}
		if (!polarisation.equals("NONE")) {
			queryBuilder.must(QueryBuilders.termQuery("polarisation.keyword", polarisation));
		}
		LOGGER.debug("query composed is {}", queryBuilder);

		sourceBuilder.query(queryBuilder);

		final String index;
		if (ProductFamily.AUXILIARY_FILE.equals(productFamily) || ProductFamily.EDRS_SESSION.equals(productFamily)) {
			index = productType.toLowerCase();
		} else {
			index = productFamily.name().toLowerCase();
		}
		sourceBuilder.size(1);
		sourceBuilder.sort(sortOrder);

		final SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(sourceBuilder);
		return searchRequest;
	}

	/*
	 * ClosestStopValidity Similar to 'ClosestStartValidity', this policy uses a
	 * centre time calculated as (t0-t1) / 2 to determine auxiliary data, which is
	 * located closest to the centre time but using stopTime as the reference
	 * instead of startTime
	 */
	public SearchMetadata closestStopValidity(final String productType, final ProductFamily productFamily,
			final String beginDate, final String endDate, final String satelliteId, final int instrumentConfId,
			final String processMode, final String polarisation) throws Exception {
		LOGGER.debug("Searching products via selection policy 'closestStopValidity' for {}, startDate {}, endDate {} ",
				productType, beginDate, endDate);

		// mimic the same behaviour used in the old processing system
		final LocalDateTime cTime = calculateCentreTime(beginDate, endDate);
		final String centreTime = DateUtils.formatToMetadataDateTimeFormat(cTime);

		final SearchRequest beforeRequest = newQueryFor(productType, productFamily, satelliteId, instrumentConfId,
				processMode, QueryBuilders.rangeQuery("validityStopTime").lt(centreTime),
				new FieldSortBuilder("validityStopTime").order(SortOrder.DESC), polarisation);
		final SearchRequest afterRequest = newQueryFor(productType, productFamily, satelliteId, instrumentConfId,
				processMode, QueryBuilders.rangeQuery("validityStopTime").gte(centreTime),
				new FieldSortBuilder("validityStopTime").order(SortOrder.ASC), polarisation);
		try {
			final SearchResponse beforeResponse = elasticsearchDAO.search(beforeRequest);
			final SearchResponse afterResponse = elasticsearchDAO.search(afterRequest);

			final SearchHits before = beforeResponse.getHits();
			final SearchHits after = afterResponse.getHits();

			LOGGER.debug("Total Hits Found before {} and after {}", this.getTotalSearchHitsStr(before),
					this.getTotalSearchHitsStr(after));

			if (this.isEmpty(before) && this.isNotEmpty(after)) {
				return toSearchMetadata(after.getAt(0));
			} else if (this.isNotEmpty(before) && this.isEmpty(after)) {
				return toSearchMetadata(before.getAt(0));
			} else if (this.isEmpty(before) && this.isEmpty(after)) {
				return null;
			}

			// "merge" functionality from old processing system implementation
			final SearchMetadata metaBefore = toSearchMetadata(before.getAt(0));
			final SearchMetadata metaAfter = toSearchMetadata(after.getAt(0));

			final Duration durationBefore = Duration.between(DateUtils.parse(metaBefore.getValidityStop()), cTime)
					.abs();
			final Duration durationAfter = Duration.between(DateUtils.parse(metaAfter.getValidityStop()), cTime).abs();

			if (durationBefore.compareTo(durationAfter) <= 0) {
				LOGGER.debug("Candidate before was the best result, {}", metaBefore.getProductName());
				return metaBefore;
			} else {
				LOGGER.debug("Candidate after was the best result, {}", metaAfter.getProductName());
				return metaAfter;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * Function which returns the list of all the Segments for a specific datatakeid
	 * and start/stop time
	 *
	 * @return the list of the corresponding Segment
	 */
	public List<SearchMetadata> valIntersect(final String beginDate, final String endDate, final String productType,
			final ProductFamily productFamily, final String processMode, final String satelliteId) throws Exception {

		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("startTime").lt(endDate))
				.must(QueryBuilders.rangeQuery("stopTime").gt(beginDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
				.must(QueryBuilders.termQuery("processMode.keyword", processMode));
		sourceBuilder.query(queryBuilder);
		LOGGER.debug("valIntersect: query composed is {}", queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);

		final SearchRequest searchRequest = new SearchRequest(getIndexForProductFamily(productFamily, productType));
		searchRequest.source(sourceBuilder);
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			LOGGER.debug("valIntersect: Total Hits Found  {}", this.getTotalSearchHitsStr(searchResponse.getHits()));

			if (this.isNotEmpty(searchResponse)) {
				final List<SearchMetadata> r = new ArrayList<>();
				for (final SearchHit hit : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = hit.getSourceAsMap();
					final SearchMetadata local = new SearchMetadata();
					local.setProductName(source.get("productName").toString());
					local.setProductType(source.get("productType").toString());
					local.setKeyObjectStorage(source.get("url").toString());

					if (source.containsKey("startTime")) {
						try {
							local.setValidityStart(
									DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("startTime");
						}
					}
					if (source.containsKey("stopTime")) {
						try {
							local.setValidityStop(
									DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("stopTime");
						}
					}
					r.add(local);
				}
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}

	/**
	 * From ValIntersect result set, select the one where startTime - (start-T0 + stop+T1) is minimal.
	 */
	public SearchMetadata latestValidityClosest(final String beginDate, final String endDate, final String productType,
			final ProductFamily productFamily, final String processMode, final String satelliteId) throws Exception {

		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("startTime").lt(endDate))
				.must(QueryBuilders.rangeQuery("stopTime").gt(beginDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
				.must(QueryBuilders.termQuery("processMode.keyword", processMode));
		sourceBuilder.query(queryBuilder);
		LOGGER.debug("latestValidityClosest: query composed is {}", queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);

		final SearchRequest searchRequest = new SearchRequest(getIndexForProductFamily(productFamily, productType));
		searchRequest.source(sourceBuilder);
		
		Map<String, Object> r = null;
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			LOGGER.debug("latestValidityClosest: Total Hits Found  {}", this.getTotalSearchHitsStr(searchResponse.getHits()));
			
			if (this.isNotEmpty(searchResponse)) {
			
				BigInteger distance = null;
				final BigInteger valStart = BigInteger.valueOf(Duration.parse(beginDate).getNano());
				final BigInteger valStop  = BigInteger.valueOf(Duration.parse(endDate).getNano());
				
				for (final SearchHit candidate : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = candidate.getSourceAsMap();
					
					final BigInteger requested_starttime  = BigInteger.valueOf(Duration.parse(source.get("startTime").toString()).getNano());
					
					final BigInteger magic = requested_starttime.subtract(valStart.add(valStop));
					
					if ((distance == null) || (magic.compareTo(distance) < 0))
				    {
						distance = magic;
						r = candidate.getSourceAsMap();
				    }
				}
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
		
		final SearchMetadata local = new SearchMetadata();
		local.setProductName(r.get("productName").toString());
		local.setProductType(r.get("productType").toString());
		local.setKeyObjectStorage(r.get("url").toString());

		if (r.containsKey("startTime")) {
			try {
				local.setValidityStart(
						DateUtils.convertToMetadataDateTimeFormat(r.get("startTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("startTime");
			}
		}
		if (r.containsKey("stopTime")) {
			try {
				local.setValidityStop(
						DateUtils.convertToMetadataDateTimeFormat(r.get("stopTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("stopTime");
			}
		}
		
		return local;
	}

	/**
	 * Retrieveal Mode LatestValIntersect to retrieve latest intersecting product
	 * (latest creation time) for product search criteria
	 *
	 * @return latest product with intersecting validity time
	 */
	public SearchMetadata lastValIntersect(final String beginDate, final String endDate, final String productType,
			final ProductFamily productFamily, final String processMode, final String satelliteId) throws Exception {

		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("validityStartTime").lt(endDate))
				.must(QueryBuilders.rangeQuery("validityStopTime").gt(beginDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType));
		sourceBuilder.query(queryBuilder);
		LOGGER.debug("latestValIntersect: query composed is {}", queryBuilder);

		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC));

		final SearchRequest searchRequest = new SearchRequest(getIndexForProductFamily(productFamily, productType));
		searchRequest.source(sourceBuilder);
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				final Map<String, Object> source = searchResponse.getHits().getAt(0).getSourceAsMap();
				final SearchMetadata r = new SearchMetadata();
				r.setProductName(source.get("productName").toString());
				r.setProductType(source.get("productType").toString());
				r.setKeyObjectStorage(source.get("url").toString());
				if (source.containsKey("validityStartTime")) {
					try {
						r.setValidityStart(
								DateUtils.convertToMetadataDateTimeFormat(source.get("validityStartTime").toString()));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStartTime");
					}
				}
				if (source.containsKey("validityStopTime")) {
					try {
						r.setValidityStop(
								DateUtils.convertToMetadataDateTimeFormat(source.get("validityStopTime").toString()));
					} catch (final DateTimeParseException e) {
						throw new MetadataMalformedException("validityStopTime");
					}
				}
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}

	/**
	 * Queries the elastic search for products matching the given parameters. Query
	 * build is based on the marginTT workflow extension.
	 * 
	 * @return list of matching products
	 * @throws Exception if start or stop time is in an invalid format, or the searh
	 *                   itself throws an error
	 */
	public List<S3Metadata> marginTTQuery(final String startTime, final String stopTime, final String productType,
			final String satelliteId, final String timeliness, final ProductFamily productFamily) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("startTime").lt(stopTime))
				.must(QueryBuilders.rangeQuery("stopTime").gt(startTime)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
				.must(QueryBuilders.termQuery(timeliness, true));

		LOGGER.debug("query composed is {}", queryBuilder);

		sourceBuilder.query(queryBuilder);
		sourceBuilder.sort("startTime", SortOrder.ASC);
		sourceBuilder.size(SIZE_LIMIT);

		final String index = productFamily.name().toLowerCase();
		final SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(sourceBuilder);

		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				final List<S3Metadata> r = new ArrayList<>();
				for (final SearchHit hit : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = hit.getSourceAsMap();
					final S3Metadata local = new S3Metadata();
					local.setProductName(source.get("productName").toString());
					local.setProductType(source.get("productType").toString());
					local.setKeyObjectStorage(source.get("url").toString());
					local.setGranuleNumber(Integer.parseInt(source.get("granuleNumber").toString()));
					local.setGranulePosition(source.get("granulePosition").toString());
					if (source.containsKey("startTime")) {
						try {
							local.setValidityStart(
									DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("startTime");
						}
					}
					if (source.containsKey("stopTime")) {
						try {
							local.setValidityStop(
									DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("stopTime");
						}
					}
					if (source.containsKey("creationTime")) {
						try {
							local.setCreationTime(
									DateUtils.convertToMetadataDateTimeFormat(source.get("creationTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("creationTime");
						}
					}
					r.add(local);
				}
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}

		return null;
	}

	public List<SearchMetadata> intervalQuery(final String startTime, final String stopTime,
			final ProductFamily productFamily, final String productType) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("insertionTime").from(startTime).to(stopTime));
		// .must(QueryBuilders.rangeQuery("insertionTime").gt(stopTime));
		// .must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId))
		// .must(QueryBuilders.regexpQuery("productType.keyword", productType));
		// .must(QueryBuilders.termQuery("processMode.keyword", processMode));

		LOGGER.debug("query composed is {}", queryBuilder);

		sourceBuilder.query(queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);

		final String index;
		if (ProductFamily.EDRS_SESSION.equals(productFamily)) {
			index = "raw";
		} else if (ProductFamily.AUXILIARY_FILE.equals(productFamily)) {
			index = productType;
		} else {
			index = productFamily.name().toLowerCase();
		}
		final SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(sourceBuilder);

		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				final List<SearchMetadata> r = new ArrayList<>();
				for (final SearchHit hit : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = hit.getSourceAsMap();
					final SearchMetadata local = new SearchMetadata();
					local.setProductName(source.get("productName").toString());
					local.setProductType(source.get("productType").toString());
					local.setKeyObjectStorage(source.get("url").toString());
					if (source.containsKey("startTime")) {
						try {
							local.setValidityStart(
									DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("startTime");
						}
					}
					if (source.containsKey("stopTime")) {
						try {
							local.setValidityStop(
									DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("stopTime");
						}
					}
					r.add(local);
				}
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}

		return null;
	}

	/**
	 * Searches for the product with given productName and in the index =
	 * productFamily. Returns only validity start and stop time.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public SearchMetadata productNameQuery(final String productFamily, final String productName)
			throws MetadataMalformedException, MetadataNotPresentException, IOException {

		final Map<String, Object> source = getRequest(productFamily, productName);

		if (source.isEmpty()) {
			throw new MetadataNotPresentException(productName);
		}

		final SearchMetadata searchMetadata = new SearchMetadata();

		if (source.containsKey("startTime")) {
			try {
				searchMetadata.setValidityStart(
						DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("startTime");
			}
		} else {
			throw new MetadataMalformedException("startTime");
		}
		if (source.containsKey("stopTime")) {
			try {
				searchMetadata
						.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("stopTime");
			}
		} else {
			throw new MetadataMalformedException("stopTime");
		}

		Map<String, Object> coordinates = null;
		if (source.containsKey("sliceCoordinates")) {
			coordinates = (Map<String, Object>) source.get("sliceCoordinates");
		} else if (source.containsKey("segmentCoordinates")) {
			coordinates = (Map<String, Object>) source.get("segmentCoordinates");
		}

		final List<List<Double>> footprint = new ArrayList<>();
		if (null != coordinates && coordinates.containsKey("coordinates") && coordinates.containsKey("type")) {
			final String type = (String) coordinates.get("type");
			final List<Object> firstArray = (List<Object>) coordinates.get("coordinates");
			if (null != firstArray && "polygon".equalsIgnoreCase(type)) {
				final List<Object> secondArray = (List<Object>) firstArray.get(0);
				for (final Object arr : secondArray) {
					final List<Double> p = new ArrayList<>();
					final List<Number> coords = (List<Number>) arr;
					final double lon = coords.get(0).doubleValue();
					final double lat = coords.get(1).doubleValue();
					p.add(lon);
					p.add(lat);
					footprint.add(p);
				}
			}
		}
		searchMetadata.setFootprint(footprint);

		return searchMetadata;
	}

	public List<LevelSegmentMetadata> getLevelSegmentMetadataFor(final String dataTakeId) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		final QueryBuilder queryBuilder = QueryBuilders.termQuery("dataTakeId.keyword", dataTakeId);
		sourceBuilder.query(queryBuilder);
		LOGGER.debug("LevelSegmentQuery: query composed is {}", queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);
		final SearchRequest searchRequest = new SearchRequest(ProductFamily.L0_SEGMENT.name().toLowerCase());
		searchRequest.source(sourceBuilder);

		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			LOGGER.debug("LevelSegmentQuery: Total Hits Found  {}",
					this.getTotalSearchHitsStr(searchResponse.getHits()));
			final List<LevelSegmentMetadata> results = new ArrayList<>();

			if (this.isNotEmpty(searchResponse)) {
				for (final SearchHit hit : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = hit.getSourceAsMap();
					if (!source.isEmpty()) {
						results.add(toLevelSegmentMetadata(source));
					}
				}
			}
			return results;
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	public List<EdrsSessionMetadata> getEdrsSessionsFor(final String sessionId) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final QueryBuilder queryBuilder = QueryBuilders.termQuery("sessionId.keyword", sessionId);
		sourceBuilder.query(queryBuilder);
		LOGGER.debug("EdrsSessionQuery: query composed is {}", queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);
		final List<EdrsSessionMetadata> results = new ArrayList<>();

		for (final EdrsSessionFileType sessionType : EdrsSessionFileType.values()) {
			final SearchRequest searchRequest = new SearchRequest(sessionType.name().toLowerCase());
			searchRequest.source(sourceBuilder);

			try {
				final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
				LOGGER.debug("EdrsSessionQuery {}: Total Hits Found  {}", sessionType.name().toLowerCase(),
						this.getTotalSearchHitsStr(searchResponse.getHits()));

				if (this.isNotEmpty(searchResponse)) {
					for (final SearchHit hit : searchResponse.getHits().getHits()) {
						final Map<String, Object> source = hit.getSourceAsMap();
						if (!source.isEmpty()) {
							results.add(toSessionMetadata(source));
						}
					}
				}
			} catch (final IOException e) {
				throw new Exception(e.getMessage());
			}
		}
		return results;
	}

	private EdrsSessionMetadata toSessionMetadata(final Map<String, Object> source) throws MetadataMalformedException {
		final EdrsSessionMetadata r = new EdrsSessionMetadata();
		r.setProductName(source.get("productName").toString());
		r.setProductType(source.get("productType").toString());
		r.setKeyObjectStorage(source.get("url").toString());
		if (source.containsKey("validityStartTime")) {
			try {
				r.setValidityStart(
						DateUtils.convertToMetadataDateTimeFormat(source.get("validityStartTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("validityStartTime");
			}
		}
		if (source.containsKey("validityStopTime")) {
			try {
				r.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("validityStopTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("validityStopTime");
			}
		}
		r.setStartTime(source.getOrDefault("startTime", "NOT_FOUND").toString());
		r.setSessionId(source.getOrDefault("sessionId", "NOT_FOUND").toString());
		r.setStopTime(source.getOrDefault("stopTime", "NOT_FOUND").toString());
		r.setStationCode(source.getOrDefault("stationCode", "NOT_FOUND").toString());
		r.setSatelliteId(source.getOrDefault("satelliteId", "NOT_FOUND").toString());
		r.setMissionId(source.getOrDefault("missionId", "NOT_FOUND").toString());
		r.setChannelId(Integer.parseInt(source.get("channelId").toString()));

		@SuppressWarnings("unchecked")
		final List<String> rawNames = (List<String>) source.getOrDefault("rawNames", Collections.emptyList());
		r.setRawNames(rawNames);
		return r;
	}

	public L0SliceMetadata getL0Slice(final String productName) throws Exception {
		final Map<String, Object> source = this.getRequest(ProductFamily.L0_SLICE.name().toLowerCase(), productName);
		return this.extractInfoForL0Slice(source, productName);
	}

	public L0AcnMetadata getL0Acn(final String productType, final String datatakeId, final String processMode) {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("dataTakeId.keyword", datatakeId))
				.must(QueryBuilders.termQuery("productType.keyword", productType))
				.must(QueryBuilders.termQuery("processMode.keyword", processMode)));

		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC));

		final SearchRequest searchRequest = new SearchRequest(ProductFamily.L0_ACN.name().toLowerCase());
		searchRequest.source(sourceBuilder);
		try {
			LOGGER.debug("Sending search request to ES for L0 ACN: {}", searchRequest);
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			LOGGER.debug("Hits found: {}", this.getTotalSearchHitsStr(searchResponse.getHits()));
			if (this.isNotEmpty(searchResponse)) {
				return this.extractInfoForL0ACN(searchResponse.getHits().getAt(0).getSourceAsMap());
			}
		} catch (final Exception e) {
			LOGGER.error("Exception occurred while searching for acns: {}", LogUtils.toString(e));
			throw new RuntimeException(
					String.format("Exception occurred while searching for productType %s", productType), e);
		}
		return null;
	}

	private LocalDateTime calculateCentreTime(final String startDate, final String stopDate) {
		final LocalDateTime start = DateUtils.parse(startDate);
		final LocalDateTime stop = DateUtils.parse(stopDate);
		// centre time calculation similar to legacy
		return start.plus(Duration.between(start, stop).dividedBy(2));
	}

	private Map<String, Object> getRequest(final String index, final String productName) throws IOException {
		final GetRequest getRequest = new GetRequest(index.toLowerCase(), productName);

		final GetResponse response = elasticsearchDAO.get(getRequest);

		if (response.isExists()) {
			return response.getSourceAsMap();
		}
		return new HashMap<>();
	}

	private L0AcnMetadata extractInfoForL0ACN(final Map<String, Object> source) throws MetadataMalformedException {
		final L0AcnMetadata r = new L0AcnMetadata();
		if (source.containsKey("productName")) {
			r.setProductName(source.get("productName").toString());
		} else {
			throw new MetadataMalformedException("productName");
		}
		if (source.containsKey("productType")) {
			r.setProductType(source.get("productType").toString());
		} else {
			throw new MetadataMalformedException("productType");
		}
		if (source.containsKey("url")) {
			r.setKeyObjectStorage(source.get("url").toString());
		} else {
			throw new MetadataMalformedException("url");
		}
		if (source.containsKey("instrumentConfigurationId")) {
			r.setInstrumentConfigurationId(Integer.parseInt(source.get("instrumentConfigurationId").toString()));
		} else {
			throw new MetadataMalformedException("instrumentConfigurationId");
		}
		if (source.containsKey("totalNumberOfSlice")) {
			r.setNumberOfSlices(Integer.parseInt(source.get("totalNumberOfSlice").toString()));
		} else {
			throw new MetadataMalformedException("totalNumberOfSlice");
		}
		if (source.containsKey("startTime")) {
			try {
				r.setValidityStart(DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("startTime");
			}
		} else {
			throw new MetadataMalformedException("startTime");
		}
		if (source.containsKey("stopTime")) {
			try {
				r.setValidityStop(DateUtils.convertToMetadataDateTimeFormat((source.get("stopTime").toString())));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("stopTime");
			}
		} else {
			throw new MetadataMalformedException("stopTime");
		}
		if (source.containsKey("dataTakeId")) {
			r.setDatatakeId(source.get("dataTakeId").toString());
		} else {
			throw new MetadataMalformedException("dataTakeId");
		}
		return r;
	}

	private L0SliceMetadata extractInfoForL0Slice(final Map<String, Object> source, final String productName)
			throws MetadataMalformedException, MetadataNotPresentException {

		final L0SliceMetadata r = new L0SliceMetadata();
		if (source.isEmpty()) {
			throw new MetadataNotPresentException(productName);
		}
		r.setProductName(productName);
		if (source.containsKey("productType")) {
			r.setProductType(source.get("productType").toString());
		} else {
			throw new MetadataMalformedException("productType");
		}
		if (source.containsKey("url")) {
			r.setKeyObjectStorage(source.get("url").toString());
		} else {
			throw new MetadataMalformedException("url");
		}
		if (source.containsKey("instrumentConfigurationId")) {
			r.setInstrumentConfigurationId(Integer.parseInt(source.get("instrumentConfigurationId").toString()));
		} else {
			throw new MetadataMalformedException("instrumentConfigurationId");
		}
		if (source.containsKey("sliceNumber")) {
			r.setNumberSlice(Integer.parseInt(source.get("sliceNumber").toString()));
		} else {
			throw new MetadataMalformedException("sliceNumber");
		}
		if (source.containsKey("startTime")) {
			try {
				r.setValidityStart(DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("startTime");
			}
		} else {
			throw new MetadataMalformedException("startTime");
		}
		if (source.containsKey("stopTime")) {
			try {
				r.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("stopTime");
			}
		} else {
			throw new MetadataMalformedException("stopTime");
		}
		if (source.containsKey("dataTakeId")) {
			r.setDatatakeId(source.get("dataTakeId").toString());
		} else {
			throw new MetadataMalformedException("dataTakeId");
		}
		return r;
	}

	public int getSeaCoverage(final ProductFamily family, final String productName) {
		try {
			final GetResponse response = elasticsearchDAO.get(new GetRequest(family.name().toLowerCase(), productName));
			if (!response.isExists()) {
				throw new MetadataNotPresentException(productName);
			}

			final GeoShapeQueryBuilder queryBuilder = QueryBuilders.geoShapeQuery("geometry",
					extractPolygonFrom(response));
			queryBuilder.relation(ShapeRelation.CONTAINS);
			LOGGER.debug("Using {}", queryBuilder);
			final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(queryBuilder);
			sourceBuilder.size(SIZE_LIMIT);

			final SearchRequest request = new SearchRequest("landmask");
			request.source(sourceBuilder);

			final SearchResponse searchResponse = elasticsearchDAO.search(request);
			if (this.isNotEmpty(searchResponse)) {
				return 0;
			}
			// TODO FIXME implement coverage calculation
			return 100;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to check for sea coverage", e);
		}
	}

	@SuppressWarnings("unchecked")
	private Geometry extractPolygonFrom(GetResponse response) {
		// TODO FIXME this needs to be fixed to use a proper abstraction
		final Map<String, Object> sliceCoordinates = (Map<String, Object>) response.getSourceAsMap()
				.get("sliceCoordinates");

		final String type = (String) sliceCoordinates.get("type");
		LOGGER.debug("Found sliceCoordinates of type {}", type);

		final List<Object> firstArray = (List<Object>) sliceCoordinates.get("coordinates");
		final List<Object> secondArray = (List<Object>) firstArray.get(0);

		final CoordinatesBuilder coordBuilder = new CoordinatesBuilder();

		for (final Object arr : secondArray) {
			final List<Number> coords = (List<Number>) arr;
			final double lon = coords.get(0).doubleValue();
			final double lat = coords.get(1).doubleValue();
			coordBuilder.coordinate(lon, lat);
		}
		return new PolygonBuilder(coordBuilder).buildGeometry();
	}

	private LevelSegmentMetadata toLevelSegmentMetadata(final Map<String, Object> source)
			throws MetadataMalformedException {
		final LevelSegmentMetadata r = new LevelSegmentMetadata();
		r.setProductName(source.get("productName").toString());
		if (source.containsKey("productType")) {
			r.setProductType(source.get("productType").toString());
		} else {
			throw new MetadataMalformedException("productType");
		}
		if (source.containsKey("url")) {
			r.setKeyObjectStorage(source.get("url").toString());
		} else {
			throw new MetadataMalformedException("url");
		}
		if (source.containsKey("startTime")) {
			try {
				r.setValidityStart(DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("startTime");
			}
		} else {
			throw new MetadataMalformedException("startTime");
		}
		if (source.containsKey("stopTime")) {
			try {
				r.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("stopTime");
			}
		} else {
			throw new MetadataMalformedException("stopTime");
		}
		if (source.containsKey("dataTakeId")) {
			r.setDatatakeId(source.get("dataTakeId").toString());
		} else {
			throw new MetadataMalformedException("dataTakeId");
		}
		if (source.containsKey("polarisation")) {
			r.setPolarisation(source.get("polarisation").toString());
		} else {
			throw new MetadataMalformedException("polarisation");
		}
		if (source.containsKey("productConsolidation")) {
			r.setConsolidation(source.get("productConsolidation").toString());
		} else {
			throw new MetadataMalformedException("productConsolidation");
		}
		if (source.containsKey("productSensingConsolidation")) {
			r.setProductSensingConsolidation(source.get("productSensingConsolidation").toString());
		} else {
			r.setProductSensingConsolidation("NOT_DEFINED");
		}
		return r;
	}

	private QueryBuilder satelliteId(final String satelliteId) {
		return QueryBuilders.boolQuery().should(QueryBuilders.termQuery("satelliteId.keyword", satelliteId))
				.should(QueryBuilders.termQuery("satelliteId.keyword", "_"));

	}

	private boolean isNotEmpty(final SearchResponse searchResponse) {
		if (null != searchResponse) {
			return this.isNotEmpty(searchResponse.getHits());
		}

		return false;
	}

	private boolean isEmpty(final SearchHits searchHits) {
		return !this.isNotEmpty(searchHits);
	}

	private boolean isNotEmpty(final SearchHits searchHits) {
		if (null != searchHits) {
			final TotalHits hits = searchHits.getTotalHits();
			if (null != hits) {
				return hits.value > 0;
			}
		}
		return false;
	}

	/**
	 * Determines the ES Index for a given Product Family
	 */
	private String getIndexForProductFamily(final ProductFamily family, final String type) {
		if (ProductFamily.AUXILIARY_FILE.equals(family) || ProductFamily.EDRS_SESSION.equals(family)) {
			return type.toLowerCase();
		} else {
			return family.name().toLowerCase();
		}
	}

	private String getTotalSearchHitsStr(final SearchHits searchHits) {
		if (null != searchHits) {
			final TotalHits hits = searchHits.getTotalHits();
			if (null != hits) {
				if (Relation.GREATER_THAN_OR_EQUAL_TO == hits.relation) {
					return hits.value + " (or more)";
				} else {
					return String.valueOf(hits.value);
				}
			}
		}
		return "0";
	}
}
