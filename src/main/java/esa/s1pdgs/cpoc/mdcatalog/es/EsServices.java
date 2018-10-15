package esa.s1pdgs.cpoc.mdcatalog.es;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
     * Logger

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
}
