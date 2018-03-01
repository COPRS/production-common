package fr.viveris.s1pdgs.mdcatalog.services.es;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
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

import fr.viveris.s1pdgs.mdcatalog.model.MetadataFile;

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
	@Autowired
	private RestHighLevelClient restHighLevelClient;

	/**
	 * Index type for elastic search
	 */
	@Value("${elasticsearch.index-type}")
	private String indexType;

	/**
	 * Check if a given metadata already exist
	 * 
	 * @param product
	 * @return
	 * @throws Exception
	 */
	public boolean isMetadataExist(JSONObject product) throws Exception {
		try {
			String productType = product.getString("productType").toLowerCase();
			String productName = product.getString("productName");

			GetRequest getRequest = new GetRequest(productType, indexType, productName);

			GetResponse response = restHighLevelClient.get(getRequest);
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
	// TODO use Exception
	public void createMetadata(JSONObject product) throws Exception {
		try {
			String productType = product.getString("productType").toLowerCase();
			String productName = product.getString("productName");

			IndexRequest request = new IndexRequest(productType, indexType, productName).source(product.toString(),
					XContentType.JSON);

			IndexResponse response = restHighLevelClient.index(request);
			if (response.status() != RestStatus.CREATED) {
				throw new Exception(String.format("Metadata not created for %s: %s %s", productName, response.status(),
						response.getResult()));
			}
		} catch (JSONException je) {
			throw new Exception(je.getMessage());
		} catch (IOException io) {
			throw new Exception(io.getMessage());
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Function which return the product that correspond to the lastValCover specification
	 * If there is no corresponding product return null
	 * 
	 * @param productType
	 * @param beginDate
	 * @param endDate
	 * @param satelliteId
	 * 
	 * @return the key object storage of the chosen product
	 * @throws Exception 
	 */
	public MetadataFile lastValCover(String productType, String beginDate, String endDate, String satelliteId) throws Exception {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
		sourceBuilder.query(QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("validityStartTime").lt(beginDate))
				.must(QueryBuilders.rangeQuery("validityStopTime").gt(endDate))
				.must(QueryBuilders.termQuery("satelliteId.keyword", satelliteId)));
		sourceBuilder.size(1);
		sourceBuilder.sort(new FieldSortBuilder("creationTime").order(SortOrder.DESC)); 

		SearchRequest searchRequest = new SearchRequest(productType.toLowerCase());
		searchRequest.types(indexType);
		searchRequest.source(sourceBuilder);
		try {
			SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
			if (searchResponse.getHits().totalHits >= 1) {
				Map<String, Object> source = searchResponse.getHits().getAt(0).getSourceAsMap();
				MetadataFile r = new MetadataFile();
				r.setProductName(source.get("productName").toString());
				r.setProductType(productType);
				r.setKeyObjectStorage(source.get("url").toString());
				r.setValidityStart(source.get("validityStartTime").toString());
				r.setValidityStop(source.get("validityStopTime").toString());
				return r;
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}
	
	/**
	 * Function which return the product that correspond to the lastValCover specification
	 * If there is no corresponding product return null
	 * 
	 * @param productType
	 * @param beginDate
	 * @param endDate
	 * @param satelliteId
	 * 
	 * @return the key object storage of the chosen product
	 * @throws Exception 
	 */
	public MetadataFile get(String productType, String productName) throws Exception {
		try {
			GetRequest getRequest = new GetRequest(productType.toLowerCase(), indexType, productName);

			GetResponse response = restHighLevelClient.get(getRequest);
			
			if (response.isExists()) {
				Map<String, Object> source = response.getSourceAsMap();
				MetadataFile r = new MetadataFile();
				r.setProductName(source.get("productName").toString());
				r.setProductType(productType);
				r.setKeyObjectStorage(source.get("url").toString());
				r.setValidityStart(source.get("validityStartTime").toString());
				r.setValidityStop(source.get("validityStopTime").toString());
				return r;
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
		return null;
	}

}
