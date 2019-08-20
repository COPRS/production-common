package esa.s1pdgs.cpoc.mdcatalog.es;

import java.io.IOException;
import java.text.ParseException;
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
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoShapeQueryBuilder;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataCreationException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
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

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(EsServices.class);
	

	/**
	 * Elasticsearch client
	 */
	private final ElasticsearchDAO elasticsearchDAO;

	/**
	 * Index type for elastic search
	 */
	private final String indexType;
	
	private final String landmaskIndexType;

	@Autowired
	public EsServices(final ElasticsearchDAO elasticsearchDAO,
			@Value("${elasticsearch.index-type}") final String indexType,
			@Value("${elasticsearch.landmask-index-type:metadata}") final String landmaskIndexType
			) {
		this.elasticsearchDAO = elasticsearchDAO;
		this.indexType = indexType;
		this.landmaskIndexType = landmaskIndexType;
	}

	/**
	 * Check if a given metadata already exist
	 * 
	 * @param product
	 * @return
	 * @throws Exception
	 */
	public boolean isMetadataExist(JSONObject product) throws Exception {
		try {
			String productType = null;
			if (ProductFamily.AUXILIARY_FILE.equals(ProductFamily.valueOf(product.getString("productFamily")))
					|| ProductFamily.EDRS_SESSION.equals(ProductFamily.valueOf(product.getString("productFamily")))) {
				productType = product.getString("productType").toLowerCase();
			} else {
				productType = product.getString("productFamily").toLowerCase();
			}
			String productName = product.getString("productName");

			GetRequest getRequest = new GetRequest(productType, indexType, productName);

			GetResponse response = elasticsearchDAO.get(getRequest);
			return response.isExists();
		} catch (JSONException je) {
			throw new Exception(je.getMessage());
		} catch (IOException io) {
			throw new Exception(io.getMessage());
		}
	}

	/**
	 * Save the metadata in elastic search. The metadata data is created in the
	 * index named [productType] with id [productName]
	 * 
	 * @param product
	 * @throws Exception
	 */
	public void createMetadata(JSONObject product) throws Exception {
		try {
			String productType = null;
			if (ProductFamily.AUXILIARY_FILE.equals(ProductFamily.valueOf(product.getString("productFamily")))
					|| ProductFamily.EDRS_SESSION.equals(ProductFamily.valueOf(product.getString("productFamily")))) {
				productType = product.getString("productType").toLowerCase();
			} else {
				productType = product.getString("productFamily").toLowerCase();
			}
			String productName = product.getString("productName");

			IndexRequest request = new IndexRequest(productType, indexType, productName).source(product.toString(),
					XContentType.JSON);

			IndexResponse response = elasticsearchDAO.index(request);

			if (response.status() != RestStatus.CREATED) {
				throw new MetadataCreationException(productName, response.status().toString(),
						response.getResult().toString());
			}
		} catch (JSONException | IOException e) {
			throw new Exception(e);
		}
	}
	
	public void createGeoMetadata(JSONObject product, String landName) throws Exception {
		try {
//			String landName = product.getString("name");

			// indexType is usually "metadata"
			IndexRequest request = new IndexRequest("landmask", indexType, landName).source(product.toString(),
					XContentType.JSON);

			IndexResponse response = elasticsearchDAO.index(request);

			if (response.status() != RestStatus.CREATED) {
				throw new MetadataCreationException(landName, response.status().toString(),
						response.getResult().toString());
			}
		} catch (JSONException | IOException e) {
			throw new Exception(e);
		}
	}

	/**
	 * Function which return the product that correspond to the lastValCover
	 * specification If there is no corresponding product return null
	 * 
	 * @param productType
	 * @param beginDate
	 * @param endDate
	 * @param satelliteId
	 * 
	 * @return the key object storage of the chosen product
	 * @throws Exception
	 */
	public SearchMetadata lastValCover(String productType, ProductFamily productFamily, String beginDate,
			String endDate, String satelliteId, int instrumentConfId, String processMode) throws Exception {

		ProductCategory category = ProductCategory.of(productFamily);

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("validityStartTime").lt(beginDate))
				.must(QueryBuilders.rangeQuery("validityStopTime").gt(endDate))
				.must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId));
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
		
		String index = null;
		if (ProductFamily.AUXILIARY_FILE.equals(productFamily) || ProductFamily.EDRS_SESSION.equals(productFamily)) {
			index = productType.toLowerCase();
		} else {
			index = productFamily.name().toLowerCase();
		}
		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC));

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(indexType);
		searchRequest.source(sourceBuilder);
		try {
			SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (searchResponse.getHits().totalHits >= 1) {
				Map<String, Object> source = searchResponse.getHits().getAt(0).getSourceAsMap();
				SearchMetadata r = new SearchMetadata();
				r.setProductName(source.get("productName").toString());
				r.setProductType(source.get("productType").toString());
				r.setKeyObjectStorage(source.get("url").toString());
				if (source.containsKey("validityStartTime")) {
					try {
						r.setValidityStart(DateUtils.convertToMetadataDateTimeFormat(source.get("validityStartTime").toString()));
					} catch(DateTimeParseException e) {
						throw new MetadataMalformedException("validityStartTime");
					}
				}
				if (source.containsKey("validityStopTime")) {
					try {
						r.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("validityStopTime").toString()));
					} catch(DateTimeParseException e) {
						throw new MetadataMalformedException("validityStopTime");
					}
				}
				return r;
			}
		} catch (IOException e) {
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
	 * lesser startTime where startTime >= centreTime 
	 * implementation.Needs to be implemented properly
	 */
	public SearchMetadata closestStartValidity(String productType, ProductFamily productFamily, String beginDate,
			String endDate, String satelliteId, int instrumentConfId, String processMode) throws Exception {
		LOGGER.debug("Searching products via selection policy 'closestStartValidity' for {}, startDate {}, endDate {} ",
				productType, beginDate, endDate);
		
		// mimic the same behaviour used in the old processing system	
		final LocalDateTime cTime = calculateCentreTime(beginDate, endDate);
		final String centreTime = DateUtils.formatToMetadataDateTimeFormat(cTime);
		
		final SearchRequest beforeRequest = newQueryFor(
				productType, 
				productFamily, 
				instrumentConfId, 
				processMode, 
				QueryBuilders.rangeQuery("validityStartTime").lt(centreTime), 
				new FieldSortBuilder("validityStartTime").order(SortOrder.DESC)				
		);
		final SearchRequest afterRequest = newQueryFor(
				productType, 
				productFamily, 
				instrumentConfId, 
				processMode, 
				QueryBuilders.rangeQuery("validityStartTime").gte(centreTime), 
				new FieldSortBuilder("validityStartTime").order(SortOrder.ASC)	
		);		
		try {
			final SearchResponse beforeResponse = elasticsearchDAO.search(beforeRequest);
			final SearchResponse afterResponse = elasticsearchDAO.search(afterRequest);
			
			final SearchHits before = beforeResponse.getHits();
			final SearchHits after = afterResponse.getHits();
			
			LOGGER.debug("Total Hits Found before {} and after {}", before.totalHits, after.totalHits);
			
			if (before.totalHits == 0 && after.totalHits > 0) {
				final SearchMetadata metaAfter = toSearchMetadata(after.getAt(0));
				LOGGER.debug("Candidate after was the best result, {}", metaAfter.getProductName());
				return metaAfter;
			}
			else if (before.totalHits > 0 && after.totalHits == 0) {				
				final SearchMetadata metaBefore = toSearchMetadata(before.getAt(0));
				LOGGER.debug("Candidate before was the best result, {}", metaBefore.getProductName());
				return metaBefore;
			}
			else if (before.totalHits == 0 && after.totalHits == 0)	{
				return null;
			}
			
			// "merge" functionality from old processing system implementation 
			final SearchMetadata metaBefore = toSearchMetadata(before.getAt(0));
			final SearchMetadata metaAfter = toSearchMetadata(after.getAt(0));

			final Duration durationBefore = Duration.between(DateUtils.parse(metaBefore.getValidityStart()), cTime).abs();
			final Duration durationAfter = Duration.between(DateUtils.parse(metaAfter.getValidityStart()), cTime).abs();
			
			if (durationBefore.compareTo(durationAfter) <= 0) {
				LOGGER.debug("Candidate before was the best result, {}", metaBefore.getProductName());
				return metaBefore;
			}
			else {
				LOGGER.debug("Candidate after was the best result, {}", metaAfter.getProductName());
				return metaAfter;
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}


	
	private final SearchMetadata toSearchMetadata(final SearchHit hit)
	{
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

	private final SearchRequest newQueryFor(String productType, ProductFamily productFamily, int instrumentConfId,
			String processMode, RangeQueryBuilder rangeQueryBuilder, FieldSortBuilder sortOrder) throws InternalErrorException {
		ProductCategory category = ProductCategory.of(productFamily);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(rangeQueryBuilder);

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

		String index = null;
		if (ProductFamily.AUXILIARY_FILE.equals(productFamily) || ProductFamily.EDRS_SESSION.equals(productFamily)) {
			index = productType.toLowerCase();
		} else {
			index = productFamily.name().toLowerCase();
		}
		sourceBuilder.size(1);
		sourceBuilder.sort(sortOrder);

		

		final SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(indexType);
		searchRequest.source(sourceBuilder);
		return searchRequest;
	}

	/*
	 * ClosestStopValidity Similar to 'ClosestStartValidity', this policy uses a
	 * centre time calculated as (t0-t1) / 2 to determine auxiliary data, which is
	 * located closest to the centre time but using stopTime as the reference
	 * instead of startTime 
	 */
	public SearchMetadata closestStopValidity(String productType, ProductFamily productFamily, String beginDate,
			String endDate, String satelliteId, int instrumentConfId, String processMode) throws Exception {
		LOGGER.debug("Searching products via selection policy 'closestStartValidity' for {}, startDate {}, endDate {} ",
				productType, beginDate, endDate);
		
		// mimic the same behaviour used in the old processing system	
		final LocalDateTime cTime = calculateCentreTime(beginDate, endDate);
		final String centreTime = DateUtils.formatToMetadataDateTimeFormat(cTime);
		
		final SearchRequest beforeRequest = newQueryFor(
				productType, 
				productFamily, 
				instrumentConfId, 
				processMode, 
				QueryBuilders.rangeQuery("validityStopTime").lt(centreTime), 
				new FieldSortBuilder("validityStopTime").order(SortOrder.DESC)	
		);
		final SearchRequest afterRequest = newQueryFor(
				productType, 
				productFamily, 
				instrumentConfId, 
				processMode, 
				QueryBuilders.rangeQuery("validityStopTime").gte(centreTime), 
				new FieldSortBuilder("validityStopTime").order(SortOrder.ASC)	
		);		
		try {
			final SearchResponse beforeResponse = elasticsearchDAO.search(beforeRequest);
			final SearchResponse afterResponse = elasticsearchDAO.search(afterRequest);
			
			final SearchHits before = beforeResponse.getHits();
			final SearchHits after = afterResponse.getHits();
			
			LOGGER.debug("Total Hits Found before {} and after {}", before.totalHits, after.totalHits);
			
			if (before.totalHits == 0 && after.totalHits > 0) {
				return toSearchMetadata(after.getAt(0));
			}
			else if (before.totalHits > 0 && after.totalHits == 0) {
				return toSearchMetadata(before.getAt(0));
			}
			else if (before.totalHits == 0 && after.totalHits == 0)	{
				return null;
			}
			
			// "merge" functionality from old processing system implementation 
			final SearchMetadata metaBefore = toSearchMetadata(before.getAt(0));
			final SearchMetadata metaAfter = toSearchMetadata(after.getAt(0));
			
			final Duration durationBefore = Duration.between(DateUtils.parse(metaBefore.getValidityStop()), cTime).abs();
			final Duration durationAfter = Duration.between(DateUtils.parse(metaAfter.getValidityStop()), cTime).abs();
			
			if (durationBefore.compareTo(durationAfter) <= 0) {
				LOGGER.debug("Candidate before was the best result, {}", metaBefore.getProductName());
				return metaBefore;
			}
			else {
				LOGGER.debug("Candidate after was the best result, {}", metaAfter.getProductName());
				return metaAfter;
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * Function which returns the list of all the Segments for a specific datatakeid
	 * and start/stop time
	 * 
	 * @param beginDate
	 * @param endDate
	 * @param dataTakeId
	 * 
	 * @return the list of the corresponding Segment
	 * @throws Exception
	 */
	public List<SearchMetadata> valIntersect(String beginDate, String endDate, String productType, String processMode,
			String satelliteId) throws Exception {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// Generic fields
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("startTime").lt(endDate))
				.must(QueryBuilders.rangeQuery("stopTime").gt(beginDate))
				.must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId))
				.must(QueryBuilders.regexpQuery("productType.keyword", productType))
				.must(QueryBuilders.termQuery("processMode.keyword", processMode));
		sourceBuilder.query(queryBuilder);
		sourceBuilder.size(20);
		SearchRequest searchRequest = new SearchRequest(ProductFamily.L0_SEGMENT.name().toLowerCase());
		searchRequest.types(indexType);
		searchRequest.source(sourceBuilder);
		try {
			SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (searchResponse.getHits().totalHits >= 1) {
				List<SearchMetadata> r = new ArrayList<>();
				for (SearchHit hit : searchResponse.getHits().getHits()) {
					Map<String, Object> source = hit.getSourceAsMap();
					SearchMetadata local = new SearchMetadata();
					local.setProductName(source.get("productName").toString());
					local.setProductType(source.get("productType").toString());
					local.setKeyObjectStorage(source.get("url").toString());
					if (source.containsKey("startTime")) {
						try {
							local.setValidityStart(DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
						} catch(DateTimeParseException e) {
							throw new MetadataMalformedException("startTime");
						}
					}
					if (source.containsKey("stopTime")) {
						try {
							local.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
						} catch(DateTimeParseException e) {
							throw new MetadataMalformedException("stopTime");
						}
					}
					r.add(local);
				}
				return r;
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}
	
	
	public List<SearchMetadata> intervalQuery(String startTime, String stopTime, ProductFamily productFamily) throws Exception {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("insertionTime").from(startTime).to(stopTime));
				//.must(QueryBuilders.rangeQuery("insertionTime").gt(stopTime));
				//.must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId))
				//.must(QueryBuilders.regexpQuery("productType.keyword", productType));
				//.must(QueryBuilders.termQuery("processMode.keyword", processMode));
		
		LOGGER.debug("query composed is {}", queryBuilder);
		
		sourceBuilder.query(queryBuilder);
		sourceBuilder.size(20);
		
		String index = null;
		if (ProductFamily.EDRS_SESSION.equals(productFamily)) {
			index = "raw";
		} else if (ProductFamily.AUXILIARY_FILE.equals(productFamily)) {
			index = "aux*";
		} else {
			index = productFamily.name().toLowerCase();
		}
		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(indexType);
		searchRequest.source(sourceBuilder);
		
		try {
			SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (searchResponse.getHits().totalHits >= 1) {
				List<SearchMetadata> r = new ArrayList<>();
				for (SearchHit hit : searchResponse.getHits().getHits()) {
					Map<String, Object> source = hit.getSourceAsMap();
					SearchMetadata local = new SearchMetadata();
					local.setProductName(source.get("productName").toString());
					local.setProductType(source.get("productType").toString());
					local.setKeyObjectStorage(source.get("url").toString());
					if (source.containsKey("startTime")) {
						try {
							local.setValidityStart(DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
						} catch(DateTimeParseException e) {
							throw new MetadataMalformedException("startTime");
						}
					}
					if (source.containsKey("stopTime")) {
						try {
							local.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
						} catch(DateTimeParseException e) {
							throw new MetadataMalformedException("stopTime");
						}
					}
					r.add(local);
				}
				return r;
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		
		return null;
	}

	/**
	 * Function which return the product that correspond to the lastValCover
	 * specification If there is no corresponding product return null
	 * 
	 * @param productType
	 * @param beginDate
	 * @param endDate
	 * @param satelliteId
	 * 
	 * @return the key object storage of the chosen product
	 * @throws Exception
	 */
	public EdrsSessionMetadata getEdrsSession(String productType, String productName) throws Exception {
		Map<String, Object> source = this.getRequest(productType.toLowerCase(), productName);
		EdrsSessionMetadata r = new EdrsSessionMetadata();
		r.setProductType(productType);
		r.setProductName(productName);
		if (source.isEmpty()) {
			throw new MetadataNotPresentException(productName);
		}
		r.setKeyObjectStorage(source.get("url").toString());
		if (source.containsKey("validityStartTime")) {
			try {
				r.setValidityStart(DateUtils.convertToMetadataDateTimeFormat(source.get("validityStartTime").toString()));
			} catch(DateTimeParseException e) {
				throw new MetadataMalformedException("validityStartTime");
			}
		}
		if (source.containsKey("validityStopTime")) {
			try {
				r.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("validityStopTime").toString()));
			} catch(DateTimeParseException e) {
				throw new MetadataMalformedException("validityStopTime");
			}
		}
		r.setStartTime(source.getOrDefault("startTime", "NOT_FOUND").toString());
		r.setSessionId(source.getOrDefault("sessionId", "NOT_FOUND").toString());
		r.setStopTime(source.getOrDefault("stopTime", "NOT_FOUND").toString());
		r.setStationCode(source.getOrDefault("stationCode", "NOT_FOUND").toString());
		r.setSatelliteId(source.getOrDefault("satelliteId", "NOT_FOUND").toString());
		r.setMissionId(source.getOrDefault("missionId", "NOT_FOUND").toString());
				
		@SuppressWarnings("unchecked")
		List<String> rawNames = (List<String>) source.getOrDefault("rawNames", Collections.emptyList());
		r.setRawNames(rawNames);
		return r;
	}

	public L0SliceMetadata getL0Slice(String productName) throws Exception {
		Map<String, Object> source = this.getRequest(ProductFamily.L0_SLICE.name().toLowerCase(), productName);
		return this.extractInfoForL0Slice(source, productName);
	}

	public L0AcnMetadata getL0Acn(String productType, String datatakeId, String processMode) throws Exception {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("dataTakeId.keyword", datatakeId))
				.must(QueryBuilders.termQuery("productType.keyword", productType))
				.must(QueryBuilders.termQuery("processMode.keyword", processMode)));

		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC));

		SearchRequest searchRequest = new SearchRequest(ProductFamily.L0_ACN.name().toLowerCase());
		searchRequest.types(indexType);
		searchRequest.source(sourceBuilder);
		try {
			SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (searchResponse.getHits().totalHits >= 1) {
				return this.extractInfoForL0ACN(searchResponse.getHits().getAt(0).getSourceAsMap());
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}

	private LocalDateTime calculateCentreTime(String startDate, String stopDate) throws ParseException {
		final LocalDateTime start = DateUtils.parse(startDate);
		final LocalDateTime stop = DateUtils.parse(stopDate);
		// centre time calculation similar to legacy
		return start.plus(Duration.between(start, stop).dividedBy(2));
	}

	private Map<String, Object> getRequest(String index, String productName) throws Exception {
		try {
			GetRequest getRequest = new GetRequest(index.toLowerCase(), indexType, productName);

			GetResponse response = elasticsearchDAO.get(getRequest);

			if (response.isExists()) {
				return response.getSourceAsMap();
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return new HashMap<>();
	}

	private L0AcnMetadata extractInfoForL0ACN(Map<String, Object> source) throws MetadataMalformedException {
		L0AcnMetadata r = new L0AcnMetadata();
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
			} catch(DateTimeParseException e) {
				throw new MetadataMalformedException("startTime");
			}
		} else {
			throw new MetadataMalformedException("startTime");
		}
		if (source.containsKey("stopTime")) {
			try {
				r.setValidityStop(DateUtils.convertToMetadataDateTimeFormat((source.get("stopTime").toString())));
			} catch(DateTimeParseException e) {
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

	private L0SliceMetadata extractInfoForL0Slice(Map<String, Object> source, String productName)
			throws MetadataMalformedException, MetadataNotPresentException {

		L0SliceMetadata r = new L0SliceMetadata();
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
			} catch(DateTimeParseException e) {
				throw new MetadataMalformedException("startTime");
			}
		} else {
			throw new MetadataMalformedException("startTime");
		}
		if (source.containsKey("stopTime")) {
			try {
				r.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
			} catch(DateTimeParseException e) {
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
	
	@SuppressWarnings("unchecked")
	public int getSeaCoverage(ProductFamily family, String productName) throws MetadataNotPresentException {		
		try {			
			// dirty workaround to wait for the product to arrive
			final GetResponse response = Retries.performWithRetries(
					() -> {
						final GetResponse resp = elasticsearchDAO.get(
								new GetRequest(family.name().toLowerCase(), indexType, productName)
						);
						if (!resp.isExists()) {
							throw new MetadataNotPresentException(productName);				
						}	
						return resp;
					}, 
					20, 
					10000L
			);
			
			// TODO FIXME this needs to be fixed to use a proper abstraction  			
			final Map<String,Object> sliceCoordinates = (Map<String, Object>) response.getSourceAsMap()
					.get("sliceCoordinates");
			
			final String type = (String) sliceCoordinates.get("type");
			LOGGER.debug("Found sliceCoordinates of type {}", type);
			
			final List<Object> firstArray = (List<Object>) sliceCoordinates.get("coordinates");
			final List<Object> secondArray = (List<Object>) firstArray.get(0);
			
			final CoordinatesBuilder coordBuilder = new CoordinatesBuilder();	

			for (final Object arr : secondArray) {
				final List<Double> coords = (List<Double>) arr;			
				final double lon = coords.get(0);
				final double lat = coords.get(1);
				coordBuilder.coordinate(lon, lat);
			}
			final GeoShapeQueryBuilder queryBuilder = QueryBuilders.geoIntersectionQuery(
					"geometry", 
					new PolygonBuilder(coordBuilder)
			);
			LOGGER.trace("Using {}", queryBuilder);			
			final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(queryBuilder);
			sourceBuilder.size(200);
		
			final SearchRequest request = new SearchRequest(family.name().toLowerCase());
			request.types(landmaskIndexType);
			request.source(sourceBuilder);
			
			final SearchResponse searchResponse = elasticsearchDAO.search(request);			
			if (searchResponse.getHits().totalHits > 0) {				
				// TODO FIXME implement coverage calculation
				return 0;
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return 100;
	}

	public LevelSegmentMetadata getLevelSegment(ProductFamily family, String productName) throws Exception {
		LevelSegmentMetadata ret = null;
		try {
			GetRequest getRequest = new GetRequest(family.name().toLowerCase(), indexType, productName);
			GetResponse response = elasticsearchDAO.get(getRequest);

			if (response.isExists()) {
				ret = this.extractInfoForLevelSegment(response.getSourceAsMap(), productName);
			} else {
				throw new MetadataNotPresentException(productName);
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return ret;
	}

	private LevelSegmentMetadata extractInfoForLevelSegment(Map<String, Object> source, String productName)
			throws MetadataMalformedException, MetadataNotPresentException {

		LevelSegmentMetadata r = new LevelSegmentMetadata();
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
		if (source.containsKey("startTime")) {
			try {
				r.setValidityStart(DateUtils.convertToMetadataDateTimeFormat(source.get("startTime").toString()));
			} catch(DateTimeParseException e) {
				throw new MetadataMalformedException("startTime");
			}
		} else {
			throw new MetadataMalformedException("startTime");
		}
		if (source.containsKey("stopTime")) {
			try {
				r.setValidityStop(DateUtils.convertToMetadataDateTimeFormat(source.get("stopTime").toString()));
			} catch(DateTimeParseException e) {
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
		return r;
	}
}
