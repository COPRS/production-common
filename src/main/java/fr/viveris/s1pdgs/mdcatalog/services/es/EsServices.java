package fr.viveris.s1pdgs.mdcatalog.services.es;

import java.io.IOException;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

}
