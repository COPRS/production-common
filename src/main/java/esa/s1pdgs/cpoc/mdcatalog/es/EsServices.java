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
			String productType = product.get("productType").toString().toLowerCase();
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
			String productType = product.get("productType").toString().toLowerCase();
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
	public SearchMetadata lastValCover(String productType, String beginDate, String endDate, String satelliteId,
			int instrumentConfId) throws Exception {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		if (instrumentConfId != -1 && !productType.toLowerCase().startsWith("aux_res")) {
			sourceBuilder
					.query(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("validityStartTime").lt(beginDate))
							.must(QueryBuilders.rangeQuery("validityStopTime").gt(endDate))
							.must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId))
							.must(QueryBuilders.termQuery("instrumentConfigurationId", instrumentConfId)));
		} else {
			sourceBuilder
					.query(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("validityStartTime").lt(beginDate))
							.must(QueryBuilders.rangeQuery("validityStopTime").gt(endDate))
							.must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId)));
		}
		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC));

		SearchRequest searchRequest = new SearchRequest(productType.toLowerCase());
		searchRequest.types(indexType);
		searchRequest.source(sourceBuilder);
		try {
			SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (searchResponse.getHits().totalHits >= 1) {
				Map<String, Object> source = searchResponse.getHits().getAt(0).getSourceAsMap();
				SearchMetadata r = new SearchMetadata();
				r.setProductName(source.get("productName").toString());
				r.setProductType(productType);
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
		Map<String, Object> source = this.getRequest(productType, productName);
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

	public L0SliceMetadata getL0Slice(String productType, String productName) throws Exception{
		Map<String, Object> source = this.getRequest(productType, productName);
		return this.extractInfoForL0Slice(source, productType, productName);
	}

	public L0AcnMetadata getL0Acn(String productType, String datatakeId) throws Exception {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("dataTakeId.keyword", datatakeId)));

		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC));

		SearchRequest searchRequest = new SearchRequest(productType.toLowerCase());
		searchRequest.types(indexType);
		searchRequest.source(sourceBuilder);
		try {
			SearchResponse searchResponse = elasticsearchDAO.search(searchRequest);
			if (searchResponse.getHits().totalHits >= 1) {
				return this.extractInfoForL0ACN(searchResponse.getHits().getAt(0).getSourceAsMap(), productType, datatakeId);
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}

	private Map<String, Object> getRequest(String productType, String productName) throws Exception {
		try {
			GetRequest getRequest = new GetRequest(productType.toLowerCase(), indexType, productName);

			GetResponse response = elasticsearchDAO.get(getRequest);

			if (response.isExists()) {
				return response.getSourceAsMap();
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return new HashMap<>();
	}

	private L0AcnMetadata extractInfoForL0ACN(Map<String, Object> source, String productType, String dataTakeID)
			throws MetadataMalformedException {
		L0AcnMetadata r = new L0AcnMetadata();
		r.setProductType(productType);
		if (source.containsKey("productName")) {
			r.setProductName(source.get("productName").toString());
		} else {
			throw new MetadataMalformedException("productName");
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

	private L0SliceMetadata extractInfoForL0Slice(Map<String, Object> source, String productType, String productName)
			throws MetadataMalformedException, MetadataNotPresentException {

		L0SliceMetadata r = new L0SliceMetadata();
		r.setProductType(productType);
		if(source.isEmpty()) {
			throw new MetadataNotPresentException(productName);
		}
		if (source.containsKey("productName")) {
			r.setProductName(source.get("productName").toString());
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
