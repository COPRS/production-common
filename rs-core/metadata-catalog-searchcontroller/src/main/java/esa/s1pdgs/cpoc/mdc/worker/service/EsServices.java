package esa.s1pdgs.cpoc.mdc.worker.service;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.json.JsonObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataCreationException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.common.time.TimeInterval;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.mdc.worker.config.SearchControllerConfig;
import esa.s1pdgs.cpoc.mdc.worker.es.ElasticsearchDAO;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.AuxFilenameMetadataExtractor;
import esa.s1pdgs.cpoc.metadata.model.AuxMetadata;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
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

    private final SearchControllerConfig searchControllerConfig;
	
	@Autowired
	public EsServices(final ElasticsearchDAO elasticsearchDAO, final SearchControllerConfig searchControllerConfig) {
		this.elasticsearchDAO = elasticsearchDAO;
		this.searchControllerConfig = searchControllerConfig;
	}

	/**
	 * Check if a given metadata already exist
	 * 
	 */
	public boolean isMetadataExist(final JsonObject product) throws Exception {
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
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	public String createMetadataWithRetries(final JsonObject product, final String productName, final int numRetries,
			final long retrySleep) throws InterruptedException {
		return Retries.performWithRetries(() -> {
			if (!isMetadataExist(product)) {
				LOGGER.debug("Creating metadata in ES for product {}", productName);
				return createMetadata(product);
			} else {
				LOGGER.debug("ES already contains metadata for product {}", productName);
			}
			return "";
		}, "Create metadata " + product, numRetries, retrySleep);
	}

	/**
	 * Save the metadata in elastic search. The metadata data is created in the
	 * index named [productType] with id [productName]
	 * 
	 */
	String createMetadata(final JsonObject product) throws Exception {
		
		String warningMessage = "";
		
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
					XContentType.JSON).setRefreshPolicy(RefreshPolicy.WAIT_UNTIL);

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
					warningMessage = "Parsing error occurred for sliceCoordinates, dropping them as workaround for #S1PRO-783";
					LOGGER.warn(warningMessage);
					product.remove("sliceCoordinates");
					fixed = true;
				}
				
				/*
				 * RS-1002: There are some situations where the footprint raises a topology exception in ES and breaking the workflow.
				 * It was decided to catch this kind of exceptions as well and remove the footprint as a WA
				 */
				if (e.getDetailedMessage().contains("found non-noded intersection between LINESTRING")) {
					warningMessage = "Parsing error occurred and identified as non-noded intersection between LINESTRING, dropping them as workaround for #RS-1002";
					LOGGER.warn(warningMessage);
					product.remove("sliceCoordinates");
					fixed = true;
				}

				if (result.contains("failed to parse field [segmentCoordinates] of type [geo_shape]")) {
					warningMessage = "Parsing error occurred for segmentCoordinates, dropping them as workaround for #S1PRO-783";
					LOGGER.warn(warningMessage);
					product.remove("segmentCoordinates");
					fixed = true;
				}

				if (!fixed) {
					throw e;
				}

				LOGGER.debug("Content of JSON second attempt: {}", product.toString());

				request = new IndexRequest(productType).id(productName).source(product.toString(), XContentType.JSON).setRefreshPolicy(RefreshPolicy.WAIT_UNTIL);
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
		} catch (IOException e) {
			throw new Exception(e);
		}
		return warningMessage;
	}

	/**
	 * Refresh the index to ensure new documents can be found. The index is
	 * extracted from the family and type.
	 * 
	 * @param productFamily productFamily of the product that will be searched in
	 *                      the future
	 * @param productType   product type of the product that will be searched in the
	 *                      future
	 */
	public void refreshIndex(final ProductFamily productFamily, final String productType) throws Exception {
		final String index = getIndexForProductFamily(productFamily, productType);
		final RefreshRequest request = new RefreshRequest(index);

		try {
			elasticsearchDAO.refresh(request);
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
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
				.must(QueryBuilders.rangeQuery("validityStartTime").lte(beginDate))
				.must(QueryBuilders.rangeQuery("validityStopTime").gte(endDate)).must(satelliteId(satelliteId));
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
					source.forEach((key, value) -> { 
						if (value != null)
							local.addAdditionalProperty(key, value.toString());
					});
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
			final String processMode,
			final String bandIndexId) throws Exception {

		final ProductCategory category = ProductCategory.of(productFamily);

		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("validityStartTime").lte(beginDate))
				.must(QueryBuilders.rangeQuery("validityStopTime").gte(endDate)).must(satelliteId(satelliteId));
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
		
		//RS-422: Allowing to use optional parameter bandIndexId on latestValCover query
		if (bandIndexId != null) {
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("bandIndexId.keyword", bandIndexId));
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
				source.forEach((key, value) -> {
					if (value != null)
						r.addAdditionalProperty(key, value.toString());
				});
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
				"NONE",
				new FieldSortBuilder("validityStartTime").order(SortOrder.DESC),
				new FieldSortBuilder("creationTime").order(SortOrder.DESC));
		final SearchRequest afterRequest = newQueryFor(productType, productFamily, satelliteId, instrumentConfId,
				processMode, QueryBuilders.rangeQuery("validityStartTime").gte(centreTime),
				"NONE",
				new FieldSortBuilder("validityStartTime").order(SortOrder.ASC),
				new FieldSortBuilder("creationTime").order(SortOrder.DESC));

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
										 final ProductFamily productFamily, final String satelliteId) throws Exception {
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
				source.forEach((key, value) -> {
					if (value != null)
						r.addAdditionalProperty(key, value.toString());
				});
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

		source.forEach((key, value) -> {
			if (value != null)
				r.addAdditionalProperty(key, value.toString());
		});

		return r;
	}

	private SearchRequest newQueryFor(final String productType, final ProductFamily productFamily,
									  final String satelliteId, final int instrumentConfId, final String processMode,
									  final RangeQueryBuilder rangeQueryBuilder, final String polarisation,
									  final FieldSortBuilder... sortOrder) {
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

		if(sortOrder != null) {
			Arrays.stream(sortOrder).forEach(sourceBuilder::sort);
		}

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
				polarisation,
				new FieldSortBuilder("validityStopTime").order(SortOrder.DESC),
				new FieldSortBuilder("creationTime").order(SortOrder.DESC));
		final SearchRequest afterRequest = newQueryFor(productType, productFamily, satelliteId, instrumentConfId,
				processMode, QueryBuilders.rangeQuery("validityStopTime").gte(centreTime),
				polarisation,
				new FieldSortBuilder("validityStopTime").order(SortOrder.ASC),
				new FieldSortBuilder("creationTime").order(SortOrder.DESC));

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
	
	/* 
	 * Creating a method for executing the ES query and format the metadata. This function is exclusively used for ValIntersect variants
	 * and do not replace a major refactoring needed on all the selection policies  
	 */
	private List<SearchMetadata> executeValIntersectQuery(String selectionPolicyName, SearchSourceBuilder sourceBuilder, ProductFamily productFamily, String productType) throws Exception {
		final SearchRequest searchRequest = new SearchRequest(getIndexForProductFamily(productFamily, productType));
		// Generic fields
		final String fieldNameStart = ProductFamily.AUXILIARY_FILE.equals(productFamily) ? "validityStartTime" : "startTime";
		final String fieldNameStop = ProductFamily.AUXILIARY_FILE.equals(productFamily) ? "validityStopTime" : "stopTime";
		searchRequest.source(sourceBuilder);
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			LOGGER.debug("{}: Total Hits Found  {}", selectionPolicyName, this.getTotalSearchHitsStr(searchResponse.getHits()));

			if (this.isNotEmpty(searchResponse)) {
				final List<SearchMetadata> r = new ArrayList<>();
				for (final SearchHit hit : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = hit.getSourceAsMap();
					final SearchMetadata local = new SearchMetadata();
					local.setProductName(source.get("productName").toString());
					local.setProductType(source.get("productType").toString());
					local.setKeyObjectStorage(source.get("url").toString());

					if (source.containsKey(fieldNameStart)) {
						try {
							local.setValidityStart(
									DateUtils.convertToMetadataDateTimeFormat(source.get(fieldNameStart).toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException(fieldNameStart);
						}
					}
					if (source.containsKey(fieldNameStop)) {
						try {
							local.setValidityStop(
									DateUtils.convertToMetadataDateTimeFormat(source.get(fieldNameStop).toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException(fieldNameStop);
						}
					}
					source.forEach((key, value) -> {
						if (value != null)
							local.addAdditionalProperty(key, value.toString());
					});
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
	 * Function which returns the list of all the Segments for a specific datatakeid
	 * and start/stop time
	 *
	 * @return the list of the corresponding Segment
	 */
	public List<SearchMetadata> valIntersect(final String beginDate, final String endDate, final String productType,
			final ProductFamily productFamily, final String processMode, final String satelliteId) throws Exception {

		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final String fieldNameStart = ProductFamily.AUXILIARY_FILE.equals(productFamily) ? "validityStartTime" : "startTime";
		final String fieldNameStop = ProductFamily.AUXILIARY_FILE.equals(productFamily) ? "validityStopTime" : "stopTime";
		final BoolQueryBuilder queryBuilder = ProductFamily.AUXILIARY_FILE.equals(productFamily) ?
				QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery(fieldNameStart).lt(endDate))
				.must(QueryBuilders.rangeQuery(fieldNameStop).gt(beginDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
		:
				QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery(fieldNameStart).lt(endDate))
				.must(QueryBuilders.rangeQuery(fieldNameStop).gt(beginDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
				.must(QueryBuilders.termQuery("processMode.keyword", processMode));
		sourceBuilder.query(queryBuilder);
		LOGGER.debug("valIntersect: query composed is {}", queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);

		return executeValIntersectQuery("valIntersect", sourceBuilder, productFamily, productType);
	}
	
	private static final class DateRange {
		private String startTime;
		private String stopTime;
		
		public DateRange(final String startTime, final String stopTime) {
			this.startTime = startTime;
			this.stopTime = stopTime;
		}
		
		public String getStartTime() {
			return startTime;
		}

		public String getStopTime() {
			return stopTime;
		}

		@Override
		public int hashCode() {
			return Objects.hash(startTime, stopTime);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DateRange other = (DateRange) obj;
			return Objects.equals(startTime, other.startTime) && Objects.equals(stopTime, other.stopTime);
		}
	}
	
	public List<SearchMetadata> valIntersectWithoutDuplicates(final String beginDate, final String endDate, final String productType,
	final ProductFamily productFamily, final String processMode, final String satelliteId) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final String fieldNameStart = ProductFamily.AUXILIARY_FILE.equals(productFamily) ? "validityStartTime" : "startTime";
		final String fieldNameStop = ProductFamily.AUXILIARY_FILE.equals(productFamily) ? "validityStopTime" : "stopTime";
		final BoolQueryBuilder queryBuilder = ProductFamily.AUXILIARY_FILE.equals(productFamily) ?
				QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery(fieldNameStart).lt(endDate))
				.must(QueryBuilders.rangeQuery(fieldNameStop).gt(beginDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
		:
				QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery(fieldNameStart).lt(endDate))
				.must(QueryBuilders.rangeQuery(fieldNameStop).gt(beginDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
				.must(QueryBuilders.termQuery("processMode.keyword", processMode));
		sourceBuilder.query(queryBuilder);		
		LOGGER.debug("valIntersectWithoutDuplicates: query composed is {}", queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);
		sourceBuilder.sort("insertionTime");
		
		List<SearchMetadata> queryResults = executeValIntersectQuery("valIntersectWithoutDuplicates", sourceBuilder, productFamily, productType);
		if (queryResults == null || queryResults.size() == 0) {
			// Query not successful or no hits at all, return empty list
			return Collections.emptyList();
		}
		
		final List<SearchMetadata> results = new ArrayList<>();
		final Set<DateRange> usedDateRanges = new HashSet<>();
		
		for (final SearchMetadata candidate: queryResults) {
			final DateRange currentDateRange = new DateRange(candidate.getValidityStart(), candidate.getValidityStop());
			// results are already ordered by insertion time so the first hit for a specific time slot wins
			if (!usedDateRanges.contains(currentDateRange)) {
				results.add(candidate);
				usedDateRanges.add(currentDateRange);
			}
		}
		
		LOGGER.debug("After ValintersectNoDuplicate filtering {} hits remains", results.size());

		return results;
	}

	public List<SearchMetadata> fullCoverage(final String beginDate, final String endDate, final String productType,
			final ProductFamily productFamily, final String processMode, final String satelliteId) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("startTime").lt(endDate))
				.must(QueryBuilders.rangeQuery("stopTime").gt(beginDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
				.must(QueryBuilders.termQuery("processMode.keyword", processMode));
		sourceBuilder.query(queryBuilder);
		LOGGER.debug("fullCoverage: query composed is {}", queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);

		final SearchRequest searchRequest = new SearchRequest(getIndexForProductFamily(productFamily, productType));
		searchRequest.source(sourceBuilder);
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			LOGGER.debug("fullCoverage: Total Hits Found  {}", this.getTotalSearchHitsStr(searchResponse.getHits()));

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

					source.forEach((key, value) -> {
						if (value != null)
							local.addAdditionalProperty(key, value.toString());
					});

					r.add(local);
				}
				return checkIfFullyCoverage(r, beginDate);
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}

		return null;
	}

	private List<SearchMetadata> checkIfFullyCoverage(final List<SearchMetadata> products, final String beginDateStr) {
		final LocalDateTime beginDate = DateUtils.parse(beginDateStr);
		final LocalDateTime endDate = DateUtils.parse(beginDateStr);

		// We initialize the reference time with the start time of the interval
		long refTime = beginDate.toEpochSecond(ZoneOffset.UTC);

		for (final SearchMetadata product : products) {
			/*
			 * Try to detect, if the product does have a follower. This happens, when the
			 * following criteria are both true: 1. The startTime of the product lies before
			 * or directly on the current (refTime + gapThresholdMillis) 2. The stopTime of
			 * the product must be bigger than the current refTime (to avoid refTime gets
			 * smaller again)
			 */
			final long startTime = DateUtils.parse(product.getValidityStart()).toEpochSecond(ZoneOffset.UTC);
			final long stopTime = DateUtils.parse(product.getValidityStop()).toEpochSecond(ZoneOffset.UTC);
			if ((startTime <= refTime) && (stopTime > refTime)) {
				refTime = stopTime;
			}
		}

		if ((refTime) >= (endDate.toEpochSecond(ZoneOffset.UTC))) {
			// No gaps, full coverage, return all results
			return products;
		}

		// There was a gap in the coverage, return nothing
		return Collections.emptyList();
	}

	public SearchMetadata latestStartValidity(final String productType, final ProductFamily productFamily,
			final String satelliteId) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.regexpQuery("productType.keyword", productType)).must(satelliteId(satelliteId));

		sourceBuilder.query(queryBuilder);
		LOGGER.debug("latestStartValidity: query composed is {}", queryBuilder);

		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("startTime").order(SortOrder.DESC));

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
				source.forEach((key, value) -> {
					if (value != null)
						r.addAdditionalProperty(key, value.toString());
				});
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}

		return null;
	}
	
	public SearchMetadata latestStopValidity(final String productType,
											 final ProductFamily productFamily, final String satelliteId) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.regexpQuery("productType.keyword", productType)).must(satelliteId(satelliteId));

		sourceBuilder.query(queryBuilder);
		LOGGER.debug("latestStopValidity: query composed is {}", queryBuilder);

		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("stopTime").order(SortOrder.DESC));

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
				source.forEach((key, value) -> {
					if (value != null) 
						r.addAdditionalProperty(key, value.toString());
				});
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}

		return null;
	}

	/**
	 * From ValIntersect result set, select the one where startTime - (start-T0 +
	 * stop+T1) is minimal.
	 */
	public SearchMetadata latestValidityClosest(final String beginDate, final String endDate, final String productType,
			final ProductFamily productFamily, final String processMode, final String satelliteId) throws Exception {

		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final String fieldNameStart = ProductFamily.AUXILIARY_FILE.equals(productFamily) ? "validityStartTime" : "startTime";
		final String fieldNameStop = ProductFamily.AUXILIARY_FILE.equals(productFamily) ? "validityStopTime" : "stopTime";
		final BoolQueryBuilder queryBuilder = ProductFamily.AUXILIARY_FILE.equals(productFamily) ?
				QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery(fieldNameStart).lt(endDate))
				.must(QueryBuilders.rangeQuery(fieldNameStop).gt(beginDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
			:
				QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery(fieldNameStart).lt(endDate))
				.must(QueryBuilders.rangeQuery(fieldNameStop).gt(beginDate)).must(satelliteId(satelliteId))
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
			LOGGER.debug("latestValidityClosest: Total Hits Found  {}",
					this.getTotalSearchHitsStr(searchResponse.getHits()));

			if (this.isNotEmpty(searchResponse)) {

				BigInteger distance = null;
				final BigInteger valStart = BigInteger.valueOf(DateUtils.parse(beginDate).getNano());
				final BigInteger valStop = BigInteger.valueOf(DateUtils.parse(beginDate).getNano());

				for (final SearchHit candidate : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = candidate.getSourceAsMap();

					final BigInteger requested_starttime = BigInteger
							.valueOf(DateUtils.parse(source.get(fieldNameStart).toString()).getNano());

					final BigInteger magic = requested_starttime.subtract(valStart.add(valStop));

					if ((distance == null) || (magic.compareTo(distance) < 0)) {
						distance = magic;
						r = candidate.getSourceAsMap();
					}
				}
			} else {
				return null;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}

		if(r == null) {
			return null;
		}

		final SearchMetadata local = new SearchMetadata();
		local.setProductName(r.get("productName").toString());
		local.setProductType(r.get("productType").toString());
		local.setKeyObjectStorage(r.get("url").toString());

		if (r.containsKey(fieldNameStart)) {
			try {
				local.setValidityStart(DateUtils.convertToMetadataDateTimeFormat(r.get(fieldNameStart).toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException(fieldNameStart);
			}
		}
		if (r.containsKey(fieldNameStop)) {
			try {
				local.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(r.get(fieldNameStop).toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException(fieldNameStop);
			}
		}

		r.forEach((key, value) -> local.addAdditionalProperty(key, value.toString()));

		return local;
	}

	/**
	 * Retrieveal Mode LatestValIntersect to retrieve latest intersecting product
	 * (latest creation time) for product search criteria
	 *
	 * @return latest product with intersecting validity time
	 */
	public SearchMetadata lastValIntersect(final String beginDate, final String endDate, final String productType,
										   final ProductFamily productFamily, final String satelliteId) throws Exception {

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
				source.forEach((key, value) -> {
					if (value != null)
						r.addAdditionalProperty(key, value.toString());
				});
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}

	/**
	 * Returned product covers entire interval and has the closest start time to the
	 * beginning of the interval
	 * 
	 * @return product that matches mode
	 * @throws Exception if start or stop time is in an invalid format, or the
	 *                   search itself throws an error
	 */
	public SearchMetadata latestValCoverClosest(final String beginDate, final String endDate, final String productType,
												final ProductFamily productFamily, final String satelliteId) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("validityStartTime").lte(beginDate))
				.must(QueryBuilders.rangeQuery("validityStopTime").gte(endDate)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType));
		sourceBuilder.query(queryBuilder);
		LOGGER.debug("latestValCoverClosest: query composed is {}", queryBuilder);

		sourceBuilder.size(SIZE_LIMIT);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC));

		final SearchRequest searchRequest = new SearchRequest(getIndexForProductFamily(productFamily, productType));
		searchRequest.source(sourceBuilder);
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				SearchMetadata result = null;
				for (final SearchHit hit : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = hit.getSourceAsMap();

					/*
					 * Update result product, if we don't have a result product yet, or the start
					 * time is after the current result product. If start times are the same we keep
					 * the current result product, because it has the later creationTime of the two
					 * (sorting of search query)
					 */
					if (source.containsKey("startTime") && (result == null || DateUtils.parse(result.getValidityStart())
							.isBefore(DateUtils.parse(source.get("startTime").toString())))) {
						final SearchMetadata local = new SearchMetadata();
						local.setProductName(source.get("productName").toString());
						local.setProductType(source.get("productType").toString());
						local.setKeyObjectStorage(source.get("url").toString());

						try {
							local.setValidityStart(
									DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("startTime");
						}
						if (source.containsKey("stopTime")) {
							try {
								local.setValidityStop(
										DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
							} catch (final DateTimeParseException e) {
								throw new MetadataMalformedException("stopTime");
							}
						}
						source.forEach((key, value) -> {
							if (value != null)
								local.addAdditionalProperty(key, value.toString());
						});
						result = local;
					}
				}
				return result;
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
	 * @throws Exception if start or stop time is in an invalid format, or the
	 *                   search itself throws an error
	 */
	public List<S3Metadata> rangeCoverQuery(final String startTime, final String stopTime, final String productType,
			final String satelliteId, final ProductFamily productFamily) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("startTime").lt(stopTime))
				.must(QueryBuilders.rangeQuery("stopTime").gt(startTime)).must(satelliteId(satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType));

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
					r.add(toS3Metadata(hit));
				}
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}

		return null;
	}

	/**
	 * Extract the metadata information "L1Triggering" from the given productName.
	 * 
	 * @param productFamily productFamily of the product, used to determine index
	 * @param productName   productName to query for
	 * @return L1Triggering information, "NONE" as default
	 * @throws Exception error on query execution
	 */
	public String getL1Triggering(final ProductFamily productFamily, final String productName) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("productName.keyword", productName));

		LOGGER.debug("query composed is {}", queryBuilder);

		sourceBuilder.query(queryBuilder);
		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC));

		final String index = productFamily.name().toLowerCase();
		final SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(sourceBuilder);

		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				final Map<String, Object> source = searchResponse.getHits().getAt(0).getSourceAsMap();
				if (source.containsKey("L1Triggering")) {
					return source.get("L1Triggering").toString();
				}
			}

		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}

		// In case of error, no product with this productname or missing L1Triggering return null
		return null;
	}
	
	/**
	 * Extract the metadata information for a given product
	 * 
	 * @param productFamily productFamily of the product, used to determine index
	 * @param productName   productName to query for
	 * @return metadata of the product
	 * @throws Exception error on query execution
	 */
	public S3Metadata getS3ProductMetadata(final ProductFamily productFamily, final String productName) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("productName.keyword", productName));

		LOGGER.debug("query composed is {}", queryBuilder);

		sourceBuilder.query(queryBuilder);
		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC));

		final String index = productFamily.name().toLowerCase();
		final SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(sourceBuilder);

		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				return toS3Metadata(searchResponse.getHits().getAt(0));
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
		
		return null;
	}
	
	/**
	 * Queries the elastic search for products of the given orbit number
	 * 
	 * @return list of matching products
	 * @throws Exception if the search throws an error
	 */
	public S3Metadata getFirstProductForOrbit(final ProductFamily productFamily, final String productType,
			final String satelliteId, final long orbitNumber) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
				.must(satelliteId(satelliteId))
				.must(QueryBuilders.termQuery("absoluteStartOrbit", orbitNumber));

		LOGGER.debug("query composed is {}", queryBuilder);

		sourceBuilder.query(queryBuilder);
		sourceBuilder.sort("insertionTime", SortOrder.ASC);
		sourceBuilder.size(1);

		final String index = productFamily.name().toLowerCase();
		final SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(sourceBuilder);

		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				return toS3Metadata(searchResponse.getHits().getAt(0));
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}

		return null;
	}
	
	/**
	 * Queries the elastic search for products of the given start or stop orbit number
	 * 
	 * @return list of matching products
	 * @throws Exception if the search throws an error
	 */
	public List<L0AcnMetadata> getL0AcnForStartOrStopOrbit(
			final ProductFamily productFamily, 
			final String productType,
			final String satelliteId, 
			final long orbitNumber
	) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
				.must(satelliteId(satelliteId))
				.must(QueryBuilders.boolQuery()
						.should(QueryBuilders.termQuery("absoluteStartOrbit", orbitNumber))
						.should(QueryBuilders.termQuery("absoluteStopOrbit", orbitNumber))
				);

		LOGGER.debug("query composed is {}", queryBuilder);
		sourceBuilder.query(queryBuilder);
		sourceBuilder.sort("insertionTime", SortOrder.ASC);
		sourceBuilder.size(SIZE_LIMIT);

		final String index = productFamily.name().toLowerCase();
		final SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(sourceBuilder);

		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				final List<L0AcnMetadata> r = new ArrayList<>();
				for (final SearchHit hit : searchResponse.getHits().getHits()) {
					r.add(extractInfoForL0ACN(hit.getSourceAsMap()));
				}
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
		return Collections.emptyList();
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

		final List<SearchMetadata> result = new ArrayList<>();
		
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				for (final SearchHit hit : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = hit.getSourceAsMap();
					final SearchMetadata local = new SearchMetadata();
					local.setProductName(source.get("productName").toString());
					local.setProductType(source.get("productType").toString());
					
					if (source.containsKey("url")) {
						local.setKeyObjectStorage(source.get("url").toString());
					} else {
						local.setKeyObjectStorage(source.get("productName").toString());
					}
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
					source.forEach((key, value) -> {
						if (value != null)
							local.addAdditionalProperty(key, value.toString());
					});
					result.add(local);
				}
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}
		return result;
	}
	
	/**
	 * Searches for matchings products with an insertionTime inside the given
	 * interval (lower bound not included), and matching productFamily and productType
	 */
	public List<SearchMetadata> intervalTypeQuery(final String startTime, final String stopTime,
			final ProductFamily productFamily, final String productType, final String satelliteId,
			final String timeliness) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("insertionTime").from(startTime, false).to(stopTime))
				.must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType));
		
		if (!timeliness.isEmpty()) {
			queryBuilder = queryBuilder.must(QueryBuilders.termsQuery("timeliness", timeliness));
		}

		LOGGER.debug("query composed is {}", queryBuilder);

		sourceBuilder.query(queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);
		sourceBuilder.sort(new FieldSortBuilder("insertionTime").order(SortOrder.ASC));

		final String index = getIndexForProductFamily(productFamily, productType);
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
					local.setMissionId(source.get(MissionId.FIELD_NAME).toString());
					local.setKeyObjectStorage(source.get("url").toString());
					if (source.containsKey("satelliteId")) {
						local.setSatelliteId(source.get("satelliteId").toString());
					}
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
					if (source.containsKey("insertionTime")) {
						try {
							local.setInsertionTime(
									DateUtils.convertToMetadataDateTimeFormat(source.get("insertionTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("insertionTime");
						}
					}
					
					source.forEach((key, value) -> {
						if (value != null)
							local.addAdditionalProperty(key, value.toString());
					});
					
					r.add(local);
				}
				return r;
			}
		} catch (final IOException e) {
			throw new Exception(e.getMessage());
		}

		return null;
	}
	public List<SearchMetadata> query(
			final ProductFamily family,
			final String productType, 
			final TimeInterval timeInterval
	) 
		throws MetadataMalformedException {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();		
		final String start = DateUtils.METADATA_DATE_FORMATTER.format(timeInterval.getStart());
		final String stop = DateUtils.METADATA_DATE_FORMATTER.format(timeInterval.getStop());
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("insertionTime").from(start, false).to(stop))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType));
		LOGGER.debug("query compost is {}", queryBuilder);		
		sourceBuilder.query(queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);
		sourceBuilder.sort(new FieldSortBuilder("insertionTime").order(SortOrder.ASC));
		final String index = getIndexForProductFamily(family, productType);
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
					local.setMissionId(source.get(MissionId.FIELD_NAME).toString());
					local.setKeyObjectStorage(source.get("url").toString());
					if (source.containsKey("stationCode")) {
						local.setSatelliteId(source.get("stationCode").toString());
					}
					if (source.containsKey("satelliteId")) {
						local.setSatelliteId(source.get("satelliteId").toString());
					}
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
					if (source.containsKey("insertionTime")) {
						try {
							local.setInsertionTime(
									DateUtils.convertToMetadataDateTimeFormat(source.get("insertionTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("insertionTime");
						}
					}
					if (source.containsKey("satelliteId")) {
						try {
							local.setSatelliteId(source.get("satelliteId").toString());
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("satelliteId");
						}
					}
					if (source.containsKey("swathtype")) {
						try {
							local.setSwathtype(source.get("swathtype").toString());
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("swathtype");
						}
					}
					else {
						final String leType = source.get("productType").toString();
						final String firstTwoCharsOfType = leType.substring(0,2);
						local.setSwathtype(firstTwoCharsOfType);
					}
					
					source.forEach((key, value) -> {
						if (value != null)
							local.addAdditionalProperty(key, value.toString());
					});
					
					if (!local.getAdditionalProperties().containsKey("dataTakeId")) {
						throw new MetadataMalformedException("dataTakeId");
					}
					r.add(local);
				}
				return r;
			}
		} catch (final IOException e) {
			throw new RuntimeException(
					String.format("Error on execution of %s: %s", searchRequest, e.getMessage()),
					e
			);
		}
		return null;
	}

	public AuxMetadata auxiliaryQuery(final String searchProductType, final String searchProductName) throws IOException, MetadataNotPresentException, MetadataMalformedException {

		final Map<String, Object> source = getRequest(searchProductType, searchProductName);

		if (source.isEmpty()) {
			throw new MetadataNotPresentException(searchProductName);
		}

		final String productName = getProperty(source, "productName", orThrowMalformed("productName"));
		final String productType = getProperty(source, "productType", orThrowMalformed("productType"));
		final String keyObjectStorage = getProperty(source, "url", orThrowMalformed("url"));
		final String validityStart = getPropertyAsDate(source, "validityStartTime", orThrowMalformed("validityStartTime"));
		final String validityStop = getPropertyAsDate(source, "validityStopTime", orThrowMalformed("validityStopTime"));
		final String missionId = getProperty(source, MissionId.FIELD_NAME, orThrowMalformed(MissionId.FIELD_NAME));
		final String satelliteId = getProperty(source, "satelliteId", orThrowMalformed("satelliteId"));

		final Map<String, String> additionalProperties = source.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().toString()));

		return new AuxMetadata(
				productName,
				productType,
				keyObjectStorage,
				validityStart,
				validityStop,
				missionId,
				satelliteId,
				"UNDEFINED",
				additionalProperties
		);
	}

	/**
	 * Searches for the product with given productName and in the index =
	 * productFamily. Returns only validity start and stop time.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public SearchMetadata productNameQuery(final String productFamily, final String productName)
			throws MetadataMalformedException, MetadataNotPresentException, IOException {
		
		final ProductFamily family = ProductFamily.valueOf(productFamily);
		
		final String index = getIndexForFilename(family, productName);

		final Map<String, Object> source = getRequest(index, productName);

		if (source.isEmpty()) {
			throw new MetadataNotPresentException(productName);
		}

		final SearchMetadata searchMetadata = new SearchMetadata();
		
		searchMetadata.setProductName(source.get("productName").toString());
		searchMetadata.setKeyObjectStorage(source.get("url").toString());		
		searchMetadata.setProductType(source.get("productType").toString());
		searchMetadata.setInsertionTime(source.get("insertionTime").toString());

		searchMetadata.setSwathtype((String) source.getOrDefault("swathtype", "UNDEFINED"));
		
		if (! ProductFamily.PLAN_AND_REPORT.equals(family) && ! ProductFamily.PLAN_AND_REPORT_ZIP.equals(family)) {
			// dirty workaround: 
			if (source.containsKey("startTime") && source.containsKey("stopTime")) {
				searchMetadata.setValidityStart(getPropertyAsDate(source, "startTime", orThrowMalformed("startTime")));
				searchMetadata.setValidityStop(getPropertyAsDate(source, "stopTime", orThrowMalformed("stopTime")));
			}
			else if (source.containsKey("validityStartTime") && source.containsKey("validityStopTime")) {
				searchMetadata.setValidityStart(getPropertyAsDate(source, "validityStartTime", orThrowMalformed("validityStartTime")));
				searchMetadata.setValidityStop(getPropertyAsDate(source, "validityStopTime", orThrowMalformed("validityStopTime")));
			}
			else {
				throw new MetadataMalformedException("start/stop times");
			}
		}

		Map<String, Object> coordinates = null;
		if (source.containsKey("sliceCoordinates")) {
			coordinates = (Map<String, Object>) source.get("sliceCoordinates");
		} else if (source.containsKey("segmentCoordinates")) {
			coordinates = (Map<String, Object>) source.get("segmentCoordinates");
		} else if (source.containsKey("coordinates")) {
			//for Sentinel-2 products 
			coordinates = (Map<String, Object>) source.get("coordinates");
		}

		final List<List<Double>> footprint = new ArrayList<>();
		if (null != coordinates && coordinates.containsKey("coordinates") && coordinates.containsKey("type")) {
			final String type = (String) coordinates.get("type");
			final List<Object> firstArray = (List<Object>) coordinates.get("coordinates");
			if (null != firstArray ) {
				if ("polygon".equalsIgnoreCase(type)) {
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
				} else if ("linestring".equalsIgnoreCase(type)) {
					for (final Object arr : firstArray) {
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
		}
		searchMetadata.setFootprint(footprint);

		source.forEach((key, value) -> {
			if (value != null)
				searchMetadata.addAdditionalProperty(key, value.toString());
		});

		return searchMetadata;
	}

	private static DefaultProvider orThrowMalformed(final String key) {
		return () -> {
			throw new MetadataMalformedException((key));
		};
	}

	@FunctionalInterface
	private interface DefaultProvider {
		String get() throws MetadataMalformedException;
	}

	private String getProperty(final Map<String, Object> source, final String key, final DefaultProvider defaultProvider) throws MetadataMalformedException {
		if (!source.containsKey(key)) {
			return defaultProvider.get();
		}

		return source.get(key).toString();
	}

	private String getPropertyAsDate(final Map<String, Object> source, final String key, final DefaultProvider defaultProvider) throws MetadataMalformedException {
		try {
			return DateUtils.convertToMetadataDateTimeFormat(getProperty(source, key, defaultProvider));
		} catch (final DateTimeParseException e) {
			throw new MetadataMalformedException(key);
		}
	}
	public List<SearchMetadata> getL1AcnProductsForDatatakeId(final String productType, final String dataTakeId) throws Exception {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("dataTakeId.keyword", dataTakeId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType));		
		sourceBuilder.query(queryBuilder);
		LOGGER.debug("L1ACN Datatake Query: query compost is {}", queryBuilder);
		sourceBuilder.size(SIZE_LIMIT);
		final SearchRequest searchRequest = new SearchRequest(ProductFamily.L1_ACN.name().toLowerCase());
		searchRequest.source(sourceBuilder);
		final List<SearchMetadata> result = new ArrayList<>();
		try {
			final SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (this.isNotEmpty(searchResponse)) {
				for (final SearchHit hit : searchResponse.getHits().getHits()) {
					final Map<String, Object> source = hit.getSourceAsMap();
					final SearchMetadata local = new SearchMetadata();
					local.setProductName(source.get("productName").toString());
					local.setProductType(source.get("productType").toString());
					local.setMissionId(source.get(MissionId.FIELD_NAME).toString());
					local.setKeyObjectStorage(source.get("url").toString());
					if (source.containsKey("stationCode")) {
						local.setSatelliteId(source.get("stationCode").toString());
					}
					if (source.containsKey("satelliteId")) {
						local.setSatelliteId(source.get("satelliteId").toString());
					}
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
					if (source.containsKey("insertionTime")) {
						try {
							local.setInsertionTime(
									DateUtils.convertToMetadataDateTimeFormat(source.get("insertionTime").toString()));
						} catch (final DateTimeParseException e) {
							throw new MetadataMalformedException("insertionTime");
						}
					}
					source.forEach((key, value) -> {
						if (value != null)
							local.addAdditionalProperty(key, value.toString());
					});
					result.add(local);
				}				
			}
			return result;
		} 
		catch (final IOException e) {
			throw new RuntimeException(
					String.format("Error on execution of %s: %s", searchRequest, e.getMessage()),
					e
			);
		}		
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
		r.setMissionId(source.getOrDefault(MissionId.FIELD_NAME, "NOT_FOUND").toString());
		r.setChannelId(Integer.parseInt(source.get("channelId").toString()));

		@SuppressWarnings("unchecked")
		final List<String> rawNames = (List<String>) source.getOrDefault("rawNames", Collections.emptyList());
		r.setRawNames(rawNames);

		source.forEach((key, value) -> {
			if (value != null)
				r.addAdditionalProperty(key, value.toString());
		});

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

		source.forEach((key, value) -> {
			if (value != null)
				r.addAdditionalProperty(key, value.toString());
		});
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
		
		source.forEach((key, value) -> {
			if (value != null)
				r.addAdditionalProperty(key, value.toString());
		});
		return r;
	}
	
	public boolean deleteProduct(final ProductFamily family, final String productName) throws MetadataNotPresentException {
		
		final String index = getIndexForFilename(family, productName);
		
		GetResponse response;
		try {
			response = elasticsearchDAO.get(new GetRequest(index, productName));
		} catch (final IOException e) {
			throw new RuntimeException("Failed to check product exists " + productName, e);
		}
		
		if (!response.isExists()) {
			throw new MetadataNotPresentException(productName);
		}
		
		DeleteResponse deleteResponse;
		try {
			deleteResponse = elasticsearchDAO.delete(new DeleteRequest(index, productName));
		} catch (final IOException e) {
			throw new RuntimeException("Failed to delete product " + productName, e);
		}
		
		if (deleteResponse.getResult() == Result.DELETED) {
			return true;
		} else {			
			return false;
		}
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
		// should always be set in esa.s1pdgs.cpoc.mdc.worker.service.MetadataExtractionService
		r.setInsertionTime(source.get("insertionTime").toString());
		source.forEach((key, value) -> {
			if (value != null)
				r.addAdditionalProperty(key, value.toString());
		});
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
	
	private String getIndexForFilename(final ProductFamily family, final String productName) {
		if (ProductFamily.AUXILIARY_FILE == family) {	
			// FIXME: idea here is to use the same config as for metadata extraction in order to 
			// detect the correct product type.
			// In the long run, this needs to be moved to a separate entity handling product type
			// mapping in a generic manner
			
			String auxPatternExpression = searchControllerConfig.getAuxPatternConfig();
			final Pattern auxPattern = Pattern.compile(auxPatternExpression, Pattern.CASE_INSENSITIVE);			
			final AuxFilenameMetadataExtractor met = new AuxFilenameMetadataExtractor(auxPattern.matcher(productName));
			if (!met.matches()) {
				throw new RuntimeException(
						String.format(
								"Product name %s does not match configured pattern %s", 
								productName, 
								auxPatternExpression
						)
				);
			}	
			return met.getFileType().toLowerCase();			
		}
		else if (ProductFamily.EDRS_SESSION == family) {
			// FIXME dirty workaround here. As sessions are not atomic, simply use a DSIB here
			// this really needs to be cleaned up for the future
			return EdrsSessionFileType.ofFilename(productName).toString().toLowerCase();			
		}
		// nominal case
		return family.name().toLowerCase();		
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
	
	/**
	 * Converts a search hit to an S3Metadata object
	 */
	private S3Metadata toS3Metadata(final SearchHit searchHit) throws MetadataMalformedException {
		final Map<String, Object> source = searchHit.getSourceAsMap();
		
		final S3Metadata local = new S3Metadata();
		local.setProductName(source.get("productName").toString());
		local.setProductType(source.get("productType").toString());
		local.setSatelliteId(source.get("satelliteId").toString());
		local.setKeyObjectStorage(source.get("url").toString());
		local.setGranuleNumber(Integer.parseInt(source.get("granuleNumber").toString()));
		local.setGranulePosition(source.get("granulePosition").toString());
		local.setAbsoluteStartOrbit(source.get("absoluteStartOrbit").toString());
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
		if (source.containsKey("utcTime")) {
			try {
				local.setAnxTime(
						DateUtils.convertToMetadataDateTimeFormat(source.get("utcTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("utcTime");
			}
		}
		if (source.containsKey("utc1Time")) {
			try {
				local.setAnx1Time(
						DateUtils.convertToMetadataDateTimeFormat(source.get("utc1Time").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("utc1Time");
			}
		}
		if (source.containsKey("insertionTime")) {
			try {
				local.setInsertionTime(
						DateUtils.convertToMetadataDateTimeFormat(source.get("insertionTime").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("insertionTime");
			}
		}
		if (source.containsKey("dumpStart")) {
			try {
				local.setDumpStart(
						DateUtils.convertToMetadataDateTimeFormat(source.get("dumpStart").toString()));
			} catch (final DateTimeParseException e) {
				throw new MetadataMalformedException("dumpStart");
			}
		}
		source.forEach((key, value) -> {
			if (value != null)
				local.addAdditionalProperty(key, value.toString());
		});
		return local;
	}
}
