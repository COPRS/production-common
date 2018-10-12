package esa.s1pdgs.cpoc.mdcatalog.es;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchResponseSections;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.mdcatalog.es.ElasticsearchDAO;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.es.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.mdcatalog.es.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.mdcatalog.es.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.mdcatalog.es.model.SearchMetadata;


public class EsServicesTest{

	@InjectMocks
	private EsServices esServices;
	
	@Mock
	private ElasticsearchDAO elasticsearchDAO;
	
	private final static String INDEX_TYPE = "metadata";
	
	
	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);
		esServices = new EsServices(elasticsearchDAO, INDEX_TYPE);
		
	}
	
	private void mockGetRequest(GetResponse response) throws IOException {
		doReturn(response).when(elasticsearchDAO).get(Mockito.any(GetRequest.class));
	}
	
	private void mockGetRequestThrowIOException() throws IOException {
		doThrow(IOException.class).when(elasticsearchDAO).get(Mockito.any(GetRequest.class));
	}
	
	private void mockIndexRequest(IndexResponse response) throws IOException {
		doReturn(response).when(elasticsearchDAO).index(Mockito.any(IndexRequest.class));
	}
	
	private void mockIndexRequestThrowIOException() throws IOException {
		doThrow(IOException.class).when(elasticsearchDAO).index(Mockito.any(IndexRequest.class));
	}
	
	private void mockSearchRequest(SearchResponse response) throws IOException {
		doReturn(response).when(elasticsearchDAO).search(Mockito.any(SearchRequest.class));
	}
	
	private void mockSearchRequestThrowIOException() throws IOException {
		doThrow(IOException.class).when(elasticsearchDAO).search(Mockito.any(SearchRequest.class));
	}
	
	@Test
	public void isMetadataExistTrueTest() throws IOException {
		// Product
		JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
		product.put("productFamily", "AUXILIARY_FILE");
		
		//Result with boolean at true for isExist
		GetResult getResult = new GetResult("index", "type", "id", 0, true, null, null);
		GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		try {
			Boolean result = esServices.isMetadataExist(product);
			assertTrue("Metadata is present in Elasticsearch", result);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public void isMetadataExistFalseTest() throws IOException {
		// Product
		JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
        product.put("productFamily", "L0_SLICE");
		
		//Result with boolean at false for isExist
		GetResult getResult = new GetResult("index", "type", "id", 0, false, null, null);
		GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		try {
			Boolean result = esServices.isMetadataExist(product);
			assertFalse("Metadata is not present in Elasticsearch", result);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void isMetadataExistBadProductTest() throws Exception {
		// Product
		JSONObject product = new JSONObject();
		product.put("productname", "name");
		product.put("productType", "type");
		
		//Result with boolean at false for isExist
		GetResult getResult = new GetResult("index", "type", "id", 0, false, null, null);
		GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		esServices.isMetadataExist(product);
	}
	
	@Test(expected = Exception.class)
	public void isMetadataExistIOExceptionTest() throws Exception {
		// Product
		JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
		
		//Mocking the get Request
		this.mockGetRequestThrowIOException();
		
		esServices.isMetadataExist(product);
	}
	
	@Test
	public void createMetadataTest() throws IOException {
		// Product
		JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
		product.put("productFamily", "L0_SLICE");
		
		//Result
		IndexResponse response = new IndexResponse(new ShardId(new Index("name", "uuid"),5), "type", "id", 0, 0, 0, true);
		
		//Mocking the get Request
		this.mockIndexRequest(response);
		
		try {
			esServices.createMetadata(product);
			assertTrue("Metadata is create in Elasticsearch", true);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void createMetadataBadProductTest() throws Exception {
		// Product
		JSONObject product = new JSONObject();
		product.put("productname", "name");
		product.put("productType", "type");
		
		//Result
		IndexResponse response = new IndexResponse(new ShardId(new Index("name", "uuid"),5), "type", "id", 0, 0, 0, true);
		
		//Mocking the get Request
		this.mockIndexRequest(response);
		
		esServices.createMetadata(product);
	}
	
	@Test(expected = Exception.class)
	public void createMetadataIOExceptionTest() throws Exception {
		// Product
		JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
		
		//Mocking the get Request
		this.mockIndexRequestThrowIOException();
		
		esServices.createMetadata(product);
	}
	
	@Test
	public void lastValCoverTest() throws IOException {
		// Product
		SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("validityStartTime");
		expectedResult.setValidityStop("validityStopTime");
		
		//Response
		BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"validityStartTime\",\"validityStopTime\":"
		        + "\"validityStopTime\", \"productType\": \"product_type\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHit[] hits = {hit};
		SearchHits searchHits = new SearchHits(hits, 1, 1.0F);
		SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			SearchMetadata result = esServices.lastValCover("type", ProductFamily.L0_ACN, 
			        "beginDate", "endDate", "satelliteId", 6, "NRT");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public void lastValCoverAuxResorbTest() throws IOException {
		// Product
		SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("aux_res");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("validityStartTime");
		expectedResult.setValidityStop("validityStopTime");
		
		//Response
		BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"validityStartTime\",\"validityStopTime\":"
		        + "\"validityStopTime\", \"productType\": \"aux_res\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHit[] hits = {hit};
		SearchHits searchHits = new SearchHits(hits, 1, 1.0F);
		SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			SearchMetadata result = esServices.lastValCover("aux_res", ProductFamily.AUXILIARY_FILE, "beginDate", 
			        "endDate", "satelliteId", -1, "NRT");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void lastValCoverIOExceptionTest() throws Exception {
		this.mockSearchRequestThrowIOException();
		esServices.lastValCover("type", ProductFamily.BLANK, "beginDate", "endDate", "satelliteId", -1, "NRT");
	}
	
	@Test
	public void lastValCoverNoHitTest() throws IOException {
		//Response
		BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"validityStartTime\",\"validityStopTime\":"
		        + "\"validityStopTime\", \"productType\": \"product_type\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHit[] hits = {hit};
		SearchHits searchHits = new SearchHits(hits, 0, 1.0F);
		SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			SearchMetadata result = esServices.lastValCover("type", ProductFamily.L0_ACN, "beginDate", 
			        "endDate", "satelliteId", 6, "NRT");
			assertEquals("Search metadata are not equals", null, result);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public void getEdrsSessionTest() throws IOException {
		//Expected result
		EdrsSessionMetadata expectedResult = new EdrsSessionMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("validityStartTime");
		expectedResult.setValidityStop("validityStopTime");
		
		//Response 
		BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"validityStartTime\",\"validityStopTime\":"
		        + "\"validityStopTime\", \"productType\": \"product_type\"}");
		GetResult getResult = new GetResult("index", "type", "id", 0, true, source, null);
		GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		try {
			EdrsSessionMetadata result = esServices.getEdrsSession("type", "name");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void getEdrsSessionNoHitTest() throws Exception {
		BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"validityStartTime\",\"validityStopTime\":"
		        + "\"validityStopTime\", \"productType\": \"product_type\"}");
		GetResult getResult = new GetResult("index", "type", "id", 0, false, source, null);
		GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		esServices.getEdrsSession("type", "name");
	}
	
	@Test(expected = Exception.class)
	public void getEdrsSessionIOExceptionTest() throws Exception {
		this.mockGetRequestThrowIOException();
		esServices.getEdrsSession("type", "name");
	}
	
	@Test
	public void getL0SliceTest() throws IOException {
		//Expected result
		L0SliceMetadata expectedResult = new L0SliceMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("validityStartTime");
		expectedResult.setValidityStop("validityStopTime");
		expectedResult.setInstrumentConfigurationId(0);
		expectedResult.setNumberSlice(2);
		expectedResult.setDatatakeId("datatakeId");
		
		//Response 
		BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"sliceNumber\":2, "
		        + "\"dataTakeId\":\"datatakeId\","
		        + "\"productType\": \"product_type\"}");
		GetResult getResult = new GetResult("index", "type", "id", 0, true, source, null);
		GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		try {
			L0SliceMetadata result = esServices.getL0Slice("name");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void getL0SliceNoHitTest() throws Exception {
		BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"sliceNumber\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		GetResult getResult = new GetResult("index", "type", "id", 0, false, source, null);
		GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		esServices.getL0Slice("name");
	}
	
	@Test
	public void getL0SliceMalformedTest() throws Exception {
		//MISSING URL
		BytesReference source = new BytesArray("{\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"sliceNumber\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		GetResult getResult = new GetResult("index", "type", "id", 0, true, source, null);
		GetResponse getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"url", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING startTime
		source = new BytesArray("{\"url\""
		        + ":\"url\",\"stopTime\":\"validityStopTime\", "
		        + "\"instrumentConfigurationId\":0, \"sliceNumber\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		getResult = new GetResult("index", "type", "id", 0, true, source, null);
		getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"startTime", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING stopTime
		source = new BytesArray("{\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\","
		        + " \"instrumentConfigurationId\":0, \"sliceNumber\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		getResult = new GetResult("index", "type", "id", 0, true, source, null);
		getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"stopTime", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING instrumentConfigurationId
		source = new BytesArray("{\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"sliceNumber\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		getResult = new GetResult("index", "type", "id", 0, true, source, null);
		getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"instrumentConfigurationId", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING sliceNumber
		source = new BytesArray("{\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		getResult = new GetResult("index", "type", "id", 0, true, source, null);
		getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"sliceNumber", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING dataTakeId
		source = new BytesArray("{\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"sliceNumber\":2,"
                + "\"productType\": \"product_type\"}");
		getResult = new GetResult("index", "type", "id", 0, true, source, null);
		getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"dataTakeId", ((MetadataMalformedException) e).getMissingField());
		}
        //MISSING product type
        source = new BytesArray("{\"url\""
                + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
                + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"sliceNumber\":2}");
        getResult = new GetResult("index", "type", "id", 0, true, source, null);
        getResponse = new GetResponse(getResult);
        this.mockGetRequest(getResponse);
        try {
            esServices.getL0Slice("name");
            fail("An exception should occur");
        } catch (Exception e) {
            assertEquals("Raised exception shall concern name",
                    "productType", ((MetadataMalformedException) e).getMissingField());
        }
		
	}
	
	@Test
	public void getL0AcnTest() throws IOException {
		//Expected result
		L0AcnMetadata expectedResult = new L0AcnMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("validityStartTime");
		expectedResult.setValidityStop("validityStopTime");
		expectedResult.setInstrumentConfigurationId(0);
		expectedResult.setNumberOfSlices(2);
		expectedResult.setDatatakeId("datatakeId");
		
		//Response
		BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
                + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
                + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2, "
                + "\"dataTakeId\":\"datatakeId\", \"productFamily\":\"l0_acn\","
                + "\"productType\": \"product_type\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHit[] hits = {hit};
		SearchHits searchHits = new SearchHits(hits, 1, 1.0F);
		SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			L0AcnMetadata result = esServices.getL0Acn("l0_acnA", "datatakeId", "NRT");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public void getL0AcnNoHitTest() throws Exception {
		BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"L0AcnMetadata\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHit[] hits = {hit};
		SearchHits searchHits = new SearchHits(hits, 0, 0.0F);
		SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			L0AcnMetadata result = esServices.getL0Acn("lo_acnc", "datatakeId", "NRT");
			assertEquals("Search metadata are not equals", null, result);
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void getL0AcnIOExceptionTest() throws Exception {
		this.mockSearchRequestThrowIOException();
		esServices.getL0Acn("l0_acn_n", "datatakeId", "NRT");
	}
	
	@Test
	public void getL0AcnMalformedTest() throws Exception {
		//MISSING productName
		BytesReference source = new BytesArray("{\"url\":\"url\",\"startTime\":\"validityStartTime\","
				+ "\"stopTime\":\"validityStopTime\", \"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2, "
				+ "\"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHit[] hits = {hit};
		SearchHits searchHits = new SearchHits(hits, 1, 1.0F);
		SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
			esServices.getL0Acn("l0_acn_0", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"productName", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING URL
		source = new BytesArray("{\"productName\":\"name\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, 1, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"url", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING startTime
		source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"stopTime\":\"validityStopTime\", \"instrumentConfigurationId\":0, "
		        + "\"totalNumberOfSlice\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, 1, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"startTime", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING stopTime
		source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\","
		        + "\"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, 1, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"stopTime", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING instrumentConfigurationId
		source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"totalNumberOfSlice\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, 1, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"instrumentConfigurationId", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING totalNumberOfSlice
		source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0,\"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, 1, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"totalNumberOfSlice", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING dataTakeId
		source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2,"
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, 1, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (Exception e) {
			assertEquals("Raised exception shall concern name",
					"dataTakeId", ((MetadataMalformedException) e).getMissingField());
		}
        //MISSING dataTakeId
        source = new BytesArray("{\"productName\":\"name\",\"url\""
                + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
                + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2}");
        hit = new SearchHit(1);
        hit.sourceRef(source);
        hits[0] = hit;
        searchHits = new SearchHits(hits, 1, 1.0F);
        searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
        response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        this.mockSearchRequest(response);
        try {
            esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
            fail("An exception should occur");
        } catch (Exception e) {
            assertEquals("Raised exception shall concern name",
                    "productType", ((MetadataMalformedException) e).getMissingField());
        }
		
	}
}
