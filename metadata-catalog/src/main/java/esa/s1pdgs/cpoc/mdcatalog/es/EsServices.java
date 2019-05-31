package esa.s1pdgs.cpoc.mdcatalog.es;

import java.io.IOException;
import java.util.ArrayList;
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
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
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
import esa.s1pdgs.cpoc.common.errors.processing.MetadataCreationException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.mdcatalog.es.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mdcatalog.es.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.mdcatalog.es.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.mdcatalog.es.model.LevelSegmentMetadata;
import esa.s1pdgs.cpoc.mdcatalog.es.model.SearchMetadata;

/**
 * Service for accessing to elasticsearch data
 * 
 * @author Cyrielle
 *
 */
@Service
public class EsServices {

    /**
     * Logger */
	private static final Logger LOGGER =
            LogManager.getLogger(EsServices.class);
	
	
	/**
	 * Elasticsearch client
	 */
	private final ElasticsearchDAO elasticsearchDAO;

	/**
	 * Index type for elastic search
	 */
	private final String indexType;

	@Autowired
	public EsServices (final ElasticsearchDAO elasticsearchDAO, @Value("${elasticsearch.index-type}") final String indexType ) {
		this.elasticsearchDAO = elasticsearchDAO;
		this.indexType = indexType;
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
            if(ProductFamily.AUXILIARY_FILE.equals(ProductFamily.valueOf(product.getString("productFamily"))) 
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
            if(ProductFamily.AUXILIARY_FILE.equals(ProductFamily.valueOf(product.getString("productFamily"))) 
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
	        String endDate, String satelliteId,	int instrumentConfId, String processMode) throws Exception {

        ProductCategory category = ProductCategory.fromProductFamily(productFamily);
        
	    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	    // Generic fields
	    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("validityStartTime").lt(beginDate))
                .must(QueryBuilders.rangeQuery("validityStopTime").gt(endDate))
                .must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId));
	    // Product type
        if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
            queryBuilder = queryBuilder.must(QueryBuilders.regexpQuery("productType.keyword", productType));
        } else {
            queryBuilder = queryBuilder.must(QueryBuilders.termQuery("productType.keyword", productType));
        }
	    // Instrument configuration id
	    if (instrumentConfId != -1 && !productType.toLowerCase().startsWith("aux_res")) {
	        queryBuilder = queryBuilder.must(QueryBuilders.termQuery("instrumentConfigurationId", instrumentConfId));
	    }
	    // Process mode
	    if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
	        queryBuilder = queryBuilder.must(QueryBuilders.termQuery("processMode.keyword", processMode));
	    }
	    sourceBuilder.query(queryBuilder);
	    
		String index = null;
        if(ProductFamily.AUXILIARY_FILE.equals(productFamily) || ProductFamily.EDRS_SESSION.equals(productFamily)) {
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
					r.setValidityStart(source.get("validityStartTime").toString());
				}
				if (source.containsKey("validityStopTime")) {
					r.setValidityStop(source.get("validityStopTime").toString());
				}
				return r;
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}
	
	
	/*
	 * ClosestStartValidity
		This policy uses a centre time, calculated as (t0-t1) / 2
		to determinate auxiliary data, which is located nearest
		to the centre time. In order to do this, it checks the
		product located directly before and behind the centre
		time and selects the one with the smallest distance. If
		both distances are equal, the product before will be
		choose.
		select from File_Type where startTime <
		centreTime and there exists no corresponding
		File_Type with greater startTime where
		startTime < centreTime
		select from File_Type where startTime >=
		centreTime and there exists no corresponding
		File_Type with lesser startTime where
		startTime >= centreTime
		//TODO pseudo implementation.Needs to be implemented properly
	 */
	public SearchMetadata closestStartValidity(String productType, ProductFamily productFamily, String beginDate, 
	        String endDate, String satelliteId,	int instrumentConfId, String processMode) throws Exception {
		//FIXME date pattern should be define in somewhere common
		//TODO getAverageOfDates(startDate,StopDate);
		
		LOGGER.debug("Searching products via selection policy 'closestStartValidity' for {} ",productType);
		
       ProductCategory category = ProductCategory.fromProductFamily(productFamily);
       SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	    // Generic fields
	    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("validityStartTime").lt(beginDate))
               // .must(QueryBuilders.rangeQuery("validityStopTime").gt(endDate))
                .must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId));
	    // Product type
        if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
            queryBuilder = queryBuilder.must(QueryBuilders.regexpQuery("productType.keyword", productType));
        } else {
            queryBuilder = queryBuilder.must(QueryBuilders.termQuery("productType.keyword", productType));
        }
	    // Instrument configuration id
	    if (instrumentConfId != -1 && !productType.toLowerCase().startsWith("aux_res")) {
	        queryBuilder = queryBuilder.must(QueryBuilders.termQuery("instrumentConfigurationId", instrumentConfId));
	    }
	    // Process mode
	    if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
	        queryBuilder = queryBuilder.must(QueryBuilders.termQuery("processMode.keyword", processMode));
	    }
	    sourceBuilder.query(queryBuilder);
	    
		String index = null;
        if(ProductFamily.AUXILIARY_FILE.equals(productFamily) || ProductFamily.EDRS_SESSION.equals(productFamily)) {
            index = productType.toLowerCase();
        } else {
            index = productFamily.name().toLowerCase();
        }
		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("validityStartTime").order(SortOrder.DESC));

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
					r.setValidityStart(source.get("validityStartTime").toString());
				}
				if (source.containsKey("validityStopTime")) {
					r.setValidityStop(source.get("validityStopTime").toString());
				}
				LOGGER.debug(" Product {} found using selection policy 'closestStartValidity'",source.get("productName").toString());
				return r;
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}
	
	/*
	 *  ClosestStopValidity
		Similar to 'ClosestStartValidity', this policy uses a
		centre time calculated as (t0-t1) / 2 to determine
		auxiliary data, which is located closest to the centre
		time but using stopTime as the reference instead of
		startTime
		//TODO pseudo implementation.Needs to be implemented properly
	 */
	public SearchMetadata closestStopValidity(String productType, ProductFamily productFamily, String beginDate, 
	        String endDate, String satelliteId,	int instrumentConfId, String processMode) throws Exception {
  	  //FIXME date pattern should be define in somewhere common
	  //FIXME date pattern should be define in somewhere common
		
		LOGGER.debug("Searching products via selection policy 'closestStopValidity' for {} ",productType);
		
       ProductCategory category = ProductCategory.fromProductFamily(productFamily);
      SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	    // Generic fields
	    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
	    		//.must(QueryBuilders.rangeQuery("validityStartTime").lt(beginDate))
                .must(QueryBuilders.rangeQuery("validityStopTime").gt(endDate))
                .must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId));
	    // Product type
        if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
            queryBuilder = queryBuilder.must(QueryBuilders.regexpQuery("productType.keyword", productType));
        } else {
            queryBuilder = queryBuilder.must(QueryBuilders.termQuery("productType.keyword", productType));
        }
	    // Instrument configuration id
	    if (instrumentConfId != -1 && !productType.toLowerCase().startsWith("aux_res")) {
	        queryBuilder = queryBuilder.must(QueryBuilders.termQuery("instrumentConfigurationId", instrumentConfId));
	    }
	    // Process mode
	    if (category == ProductCategory.LEVEL_PRODUCTS || category == ProductCategory.LEVEL_SEGMENTS) {
	        queryBuilder = queryBuilder.must(QueryBuilders.termQuery("processMode.keyword", processMode));
	    }
	    sourceBuilder.query(queryBuilder);
	    
		String index = null;
        if(ProductFamily.AUXILIARY_FILE.equals(productFamily) || ProductFamily.EDRS_SESSION.equals(productFamily)) {
            index = productType.toLowerCase();
        } else {
            index = productFamily.name().toLowerCase();
        }
		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("closestStopValidity").order(SortOrder.ASC));

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
					r.setValidityStart(source.get("validityStartTime").toString());
				}
				if (source.containsKey("validityStopTime")) {
					r.setValidityStop(source.get("validityStopTime").toString());
				}
				LOGGER.debug(" Product {} found using selection policy 'closestStopValidity'",source.get("productName").toString());
				return r;
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}
	
	
	
	
	/**
	 * Function which returns the list of all the Segments for a specific datatakeid and start/stop time
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
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("startTime").lt(endDate))
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
                for(SearchHit hit : searchResponse.getHits().getHits()) {
                    Map<String, Object> source = hit.getSourceAsMap();
                    SearchMetadata local = new SearchMetadata();
                    local.setProductName(source.get("productName").toString());
                    local.setProductType(source.get("productType").toString());
                    local.setKeyObjectStorage(source.get("url").toString());
                    if (source.containsKey("startTime")) {
                        local.setValidityStart(source.get("startTime").toString());
                    }
                    if (source.containsKey("stopTime")) {
                        local.setValidityStop(source.get("stopTime").toString());
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
		if(source.isEmpty()) {
			throw new MetadataNotPresentException(productName);
		}
		r.setKeyObjectStorage(source.get("url").toString());
		if (source.containsKey("validityStartTime")) {
			r.setValidityStart(source.get("validityStartTime").toString());
		}
		if (source.containsKey("validityStopTime")) {
			r.setValidityStop(source.get("validityStopTime").toString());
		}
		return r;
	}

	public L0SliceMetadata getL0Slice(String productName) throws Exception{
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
	

//	private String getAverageOfDates(String startDate, String stopDate)
//			throws ParseException {
//		SimpleDateFormat formatter = new SimpleDateFormat(
//				"yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
//		Date date1 = formatter.parse(startDate);
//		Date date2 = formatter.parse(stopDate);
//		long millis = (date1.getTime()+ date2.getTime() );
//		Date averageDate = new Date( millis / 2);
//		String formattedDateStr = formatter.format(averageDate);
//		return formattedDateStr;
//	}

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

	private L0AcnMetadata extractInfoForL0ACN(Map<String, Object> source)
			throws MetadataMalformedException {
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
			r.setValidityStart(source.get("startTime").toString());
		} else {
			throw new MetadataMalformedException("startTime");
		}
		if (source.containsKey("stopTime")) {
			r.setValidityStop(source.get("stopTime").toString());
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
		if(source.isEmpty()) {
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
			r.setValidityStart(source.get("startTime").toString());
		} else {
			throw new MetadataMalformedException("startTime");
		}
		if (source.containsKey("stopTime")) {
			r.setValidityStop(source.get("stopTime").toString());
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

    public LevelSegmentMetadata getLevelSegment(ProductFamily family, String productName) throws Exception{
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
        if(source.isEmpty()) {
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
            r.setValidityStart(source.get("startTime").toString());
        } else {
            throw new MetadataMalformedException("startTime");
        }
        if (source.containsKey("stopTime")) {
            r.setValidityStop(source.get("stopTime").toString());
        } else {
            throw new MetadataMalformedException("stopTime");
        }
        if (source.containsKey("dataTakeId")) {
            r.setDatatakeId(source.get("dataTakeId").toString());
        } else {
            throw new MetadataMalformedException("dataTakeId");
        }
        if(source.containsKey("polarisation")) {
            r.setPolarisation(source.get("polarisation").toString());
        } else {
            throw new MetadataMalformedException("polarisation");
        }
        if(source.containsKey("productConsolidation")) {
            r.setConsolidation(source.get("productConsolidation").toString());
        } else {
            throw new MetadataMalformedException("productConsolidation");
        }
        return r;
    }
}
