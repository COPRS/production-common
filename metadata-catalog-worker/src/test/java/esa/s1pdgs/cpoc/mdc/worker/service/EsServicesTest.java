package esa.s1pdgs.cpoc.mdc.worker.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchResponse.Clusters;
import org.elasticsearch.action.search.SearchResponseSections;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.internal.InternalSearchResponse;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.MaskType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.mdc.worker.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.worker.es.ElasticsearchDAO;
import esa.s1pdgs.cpoc.metadata.model.AuxMetadata;
import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;


public class EsServicesTest{

	@InjectMocks
	private EsServices esServices;
	
	@Mock
	private ElasticsearchDAO elasticsearchDAO;
	
	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);
		esServices = new EsServices(elasticsearchDAO, new MdcWorkerConfigurationProperties());
		
	}
	
	private void mockGetRequest(final GetResponse response) throws IOException {
		doReturn(response).when(elasticsearchDAO).get(Mockito.any(GetRequest.class));
	}
	
	private void mockGetRequestThrowIOException() throws IOException {
		doThrow(IOException.class).when(elasticsearchDAO).get(Mockito.any(GetRequest.class));
	}
	
	private void mockIndexRequest(final IndexResponse response) throws IOException {
		doReturn(response).when(elasticsearchDAO).index(Mockito.any(IndexRequest.class));
	}
	
	private void mockIndexRequestThrowIOException() throws IOException {
		doThrow(IOException.class).when(elasticsearchDAO).index(Mockito.any(IndexRequest.class));
	}
	
	private void mockSearchRequest(final SearchResponse response) throws IOException {
		doReturn(response).when(elasticsearchDAO).search(Mockito.any(SearchRequest.class));
	}
	
	private void mockSearchRequestThrowIOException() throws IOException {
		doThrow(IOException.class).when(elasticsearchDAO).search(Mockito.any(SearchRequest.class));
	}
	
	@Test
	public void isMetadataExistTrueTest() throws IOException {
		// Product
		final JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
		product.put("productFamily", "AUXILIARY_FILE");
		
		//Result with boolean at true for isExist
		final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, null, null, null);
		final GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		try {
			final Boolean result = esServices.isMetadataExist(product);
			assertTrue("Metadata is present in Elasticsearch", result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public void isMetadataExistFalseTest() throws IOException {
		// Product
		final JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
        product.put("productFamily", "L0_SLICE");
		
		//Result with boolean at false for isExist
		final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, false, null, null, null);
		final GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		try {
			final Boolean result = esServices.isMetadataExist(product);
			assertFalse("Metadata is not present in Elasticsearch", result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void isMetadataExistBadProductTest() throws Exception {
		// Product
		final JSONObject product = new JSONObject();
		product.put("productname", "name");
		product.put("productType", "type");
		
		//Result with boolean at false for isExist
		final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, false, null, null, null);
		final GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		esServices.isMetadataExist(product);
	}
	
	@Test(expected = Exception.class)
	public void isMetadataExistIOExceptionTest() throws Exception {
		// Product
		final JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
		
		//Mocking the get Request
		this.mockGetRequestThrowIOException();
		
		esServices.isMetadataExist(product);
	}
	
	@Test
	public void createMetadataTest() throws IOException {
		// Product
		final JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
		product.put("productFamily", "L0_SLICE");
		
		//Result
		final IndexResponse response = new IndexResponse(new ShardId(new Index("name", "uuid"),5), "type", "id", 0, 0, 0, true);
		
		//Mocking the get Request
		this.mockIndexRequest(response);
		
		try {
			esServices.createMetadata(product);
			assertTrue("Metadata is create in Elasticsearch", true);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void createMetadataBadProductTest() throws Exception {
		// Product
		final JSONObject product = new JSONObject();
		product.put("productname", "name");
		product.put("productType", "type");
		
		//Result
		final IndexResponse response = new IndexResponse(new ShardId(new Index("name", "uuid"),5), "type", "id", 0, 0, 0, true);
		
		//Mocking the get Request
		this.mockIndexRequest(response);
		
		esServices.createMetadata(product);
	}
	
	@Test(expected = Exception.class)
	public void createMetadataIOExceptionTest() throws Exception {
		// Product
		final JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
		
		//Mocking the get Request
		this.mockIndexRequestThrowIOException();
		
		esServices.createMetadata(product);
	}
	
	@Test
	public void lastValCoverTest() throws IOException {
		// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
		expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("validityStartTime", "2000-01-01T00:00:00.000000Z");
		    put("validityStopTime", "2001-01-01T00:00:00.000000Z");
		    put("productName", "name");
		    put("productType", "product_type");
		    put("url", "url");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"2000-01-01T00:00:00.000000Z\",\"validityStopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final SearchMetadata result = esServices.lastValCover("type", ProductFamily.L0_ACN, 
			        "beginDate", "endDate", "satelliteId", 6, "NRT");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public void closestStartValidityTest() throws Exception {
		// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2012-05-05T10:10:12.000120Z");
		expectedResult.setValidityStop("2019-05-05T10:10:12.001230Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {
		{
		    put("validityStartTime", "2012-05-05T10:10:12.000120Z");
		    put("validityStopTime", "2019-05-05T10:10:12.001230Z");
		    put("productName", "name");
		    put("productType", "product_type");
		    put("url", "url");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"2012-05-05T10:10:12.000120Z\",\"validityStopTime\":"
		        + "\"2019-05-05T10:10:12.001230Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);

		//"yyyy-MM-dd'T'HH:mm:ss.999999'Z'
		final SearchMetadata result = esServices.closestStartValidity("type", ProductFamily.L0_ACN, 
		        "2012-05-05T10:10:12.000120Z", "2019-05-05T10:10:12.001230Z", "A", 6, "FAST");
		assertEquals("Search metadata are not equals", expectedResult, result);

	}
	
	@Test
	public void closestStopValidityTest() throws IOException {
		// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2012-05-05T10:10:12.000120Z");
		expectedResult.setValidityStop("2019-05-05T10:10:12.001230Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("validityStartTime", "2012-05-05T10:10:12.000120Z");
		    put("validityStopTime", "2019-05-05T10:10:12.001230Z");
		    put("productName", "name");
		    put("productType", "product_type");
		    put("url", "url");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"2012-05-05T10:10:12.000120Z\",\"validityStopTime\":"
		        + "\"2019-05-05T10:10:12.001230Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final SearchMetadata result = esServices.closestStopValidity("type", ProductFamily.L2_ACN, 
			        "2012-05-05T10:10:12.000120Z", "2019-05-05T10:10:12.001230Z", "A", 6, "FAST", "NONE");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	
	@Test
	public void lastValCoverAuxResorbTest() throws IOException {
		// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("aux_res");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
		expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("validityStartTime", "2000-01-01T00:00:00.000000Z");
		    put("validityStopTime", "2001-01-01T00:00:00.000000Z");
		    put("productName", "name");
		    put("productType", "aux_res");
		    put("url", "url");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"2000-01-01T00:00:00.000000Z\",\"validityStopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"productType\": \"aux_res\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final SearchMetadata result = esServices.lastValCover("aux_res", ProductFamily.AUXILIARY_FILE, "beginDate", 
			        "endDate", "satelliteId", -1, "NRT");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void lastValCoverIOExceptionTest() throws Exception {
		this.mockSearchRequestThrowIOException();
		esServices.lastValCover("type", ProductFamily.L0_SLICE, "beginDate", "endDate", "satelliteId", -1, "NRT");
	}
	
	@Test
	public void lastValCoverNoHitTest() throws IOException {
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"validityStartTime\",\"validityStopTime\":"
		        + "\"validityStopTime\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(0, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final SearchMetadata result = esServices.lastValCover("type", ProductFamily.L0_ACN, "beginDate", 
			        "endDate", "satelliteId", 6, "NRT");
			assertEquals("Search metadata are not equals", null, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
    public void valIntersectTest() throws IOException {
        // Product
        final SearchMetadata r = new SearchMetadata();
        r.setProductName("name");
        r.setProductType("product_type");
        r.setKeyObjectStorage("url");
        r.setValidityStart("2000-01-01T00:00:00.000000Z");
        r.setValidityStop("2001-01-01T00:00:00.000000Z");
        r.setAdditionalProperties(new HashMap<String,String>() {{
		    put("startTime", "2000-01-01T00:00:00.000000Z");
		    put("stopTime", "2001-01-01T00:00:00.000000Z");
		    put("productName", "name");
		    put("productType", "product_type");
		    put("url", "url");
		}});
        
        final List<SearchMetadata> expectedResult = new ArrayList<>();
        expectedResult.add(r);
        
        //Response
        final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
                + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"productType\": \"product_type\"}");
        final SearchHit hit = new SearchHit(1);
        hit.sourceRef(source);
        final SearchHit[] hits = {hit};
        final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
        final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
        final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
        final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        
        //Mocking the search request
        this.mockSearchRequest(response);
        
        try {
            final List<SearchMetadata> result = esServices.valIntersect("beginDate", "endDate", "productType", ProductFamily.L0_SEGMENT, "processMode", "satelliteId");
            assertEquals("Search metadata are not equals", expectedResult, result);
        } catch (final Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test(expected = Exception.class)
    public void valIntersectIOExceptionTest() throws Exception {
        this.mockSearchRequestThrowIOException();
        esServices.valIntersect("beginDate", "endDate", "productType", ProductFamily.L0_SEGMENT, "processMode", "satelliteId");
    }
    
    @Test
    public void valIntersectNoHitTest() throws IOException {
        //Response
        final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
                + ":\"url\",\"validityStartTime\":\"validityStartTime\",\"validityStopTime\":"
                + "\"validityStopTime\", \"productType\": \"product_type\"}");
        final SearchHit hit = new SearchHit(1);
        hit.sourceRef(source);
        final SearchHit[] hits = {hit};
        final TotalHits totalHits = new TotalHits(0, Relation.EQUAL_TO);
        final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
        final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
        final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        
        //Mocking the search request
        this.mockSearchRequest(response);
        
        try {
            final List<SearchMetadata> result = esServices.valIntersect("beginDate", "endDate", "productType", ProductFamily.L0_SEGMENT, "processMode", "satelliteId");
            assertEquals("Search metadata are not equals", null, result);
        } catch (final Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void lastValIntersectTest() throws IOException {
        // Product
        final SearchMetadata expectedResult = new SearchMetadata();
        expectedResult.setProductName("name");
        expectedResult.setProductType("product_type");
        expectedResult.setKeyObjectStorage("url");
        expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
        expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
        expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("validityStartTime", "2000-01-01T00:00:00.000000Z");
		    put("validityStopTime", "2001-01-01T00:00:00.000000Z");
		    put("productName", "name");
		    put("productType", "product_type");
		    put("url", "url");
		}});
        
        //Response
        final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
                + ":\"url\",\"validityStartTime\":\"2000-01-01T00:00:00.000000Z\",\"validityStopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"productType\": \"product_type\"}");
        final SearchHit hit = new SearchHit(1);
        hit.sourceRef(source);
        final SearchHit[] hits = {hit};
        final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
        final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
        final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
        final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        
        //Mocking the search request
        this.mockSearchRequest(response);
        
        try {
            final SearchMetadata result = esServices.lastValIntersect("beginDate", "endDate", "productType", ProductFamily.L0_SEGMENT, "satelliteId");
            assertEquals("Search metadata are not equals", expectedResult, result);
        } catch (final Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test(expected = Exception.class)
    public void lastValIntersectIOExceptionTest() throws Exception {
        this.mockSearchRequestThrowIOException();
        esServices.lastValIntersect("beginDate", "endDate", "productType", ProductFamily.L0_SEGMENT, "satelliteId");
    }
    
    @Test
    public void lastValIntersectNoHitTest() throws IOException {
        //Response
        final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
                + ":\"url\",\"validityStartTime\":\"validityStartTime\",\"validityStopTime\":"
                + "\"validityStopTime\", \"productType\": \"product_type\"}");
        final SearchHit hit = new SearchHit(1);
        hit.sourceRef(source);
        final SearchHit[] hits = {hit};
        final TotalHits totalHits = new TotalHits(0, Relation.EQUAL_TO);
        final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
        final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
        final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        
        //Mocking the search request
        this.mockSearchRequest(response);
        
        try {
            final SearchMetadata result = esServices.lastValIntersect("beginDate", "endDate", "productType", ProductFamily.L0_SEGMENT, "satelliteId");
            assertEquals("Search metadata are not equals", null, result);
        } catch (final Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
	
	@Test
	public void getEdrsSessionTest() throws IOException {
		//Expected result
		final EdrsSessionMetadata expectedResult = new EdrsSessionMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setSessionId("session");
		expectedResult.setMissionId("mission");
		expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
		expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
		expectedResult.setStartTime("2000-01-01T00:00:00.000000Z");
		expectedResult.setStopTime("2001-01-01T00:00:00.000000Z");
		expectedResult.setSatelliteId("satellite");
		expectedResult.setStationCode("station");
		expectedResult.setRawNames(Collections.emptyList());
		expectedResult.setChannelId(1);
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
			put("validityStopTime", "2001-01-01T00:00:00.000000Z");
			put("stationCode", "station");
			put("missionId", "mission");
			put("satelliteId", "satellite");
			put("validityStartTime", "2000-01-01T00:00:00.000000Z");
			put("startTime", "2000-01-01T00:00:00.000000Z");
			put("stopTime", "2001-01-01T00:00:00.000000Z");
			put("sessionId", "session");
			put("productName", "name");
			put("url", "url");
			put("productType", "product_type");
			put("channelId", "1");
		}});
		
		//Response 
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"sessionId\":\"session\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":\"2001-01-01T00:00:00.000000Z\",\"validityStartTime\":\"2000-01-01T00:00:00.000000Z\",\"validityStopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"productType\": \"product_type\", \"missionId\":\"mission\",\"satelliteId\":\"satellite\",\"stationCode\":\"station\",\"channelId\":\"1\"}");
		
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);

		final SearchResponse emptyResponse = new SearchResponse(new InternalSearchResponse(
				new SearchHits(new SearchHit[0], new TotalHits(0L, TotalHits.Relation.EQUAL_TO), Float.NaN),
                InternalAggregations.EMPTY, null, null, false, null, 0), null, 0, 0, 0, 0L,
				ShardSearchFailure.EMPTY_ARRAY, null);
		
		//Mocking the search request
		Answer<SearchResponse> answer = new Answer<SearchResponse>() {
	        public SearchResponse answer(InvocationOnMock invocation) throws Throwable {
	        	List<String> indices = Arrays.asList(invocation.getArgument(0, SearchRequest.class).indices());
	        	if (indices.contains(EdrsSessionFileType.SESSION.name().toLowerCase())) {
	        		return response;
	        	} else {
	                return emptyResponse;
	        	}
	        }
	    };
		when(elasticsearchDAO.search(Mockito.any(SearchRequest.class))).thenAnswer(answer);
		
		try {
			final List<EdrsSessionMetadata> result = esServices.getEdrsSessionsFor("session");
			System.out.println(result.get(0).getAdditionalProperties());
			assertEquals("Search metadata are not equals", Collections.singletonList(expectedResult), result);
		} catch (final Exception e) {
			e.printStackTrace();
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public void getL0SliceTest() throws IOException {
		//Expected result
		final L0SliceMetadata expectedResult = new L0SliceMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
		expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
		expectedResult.setInstrumentConfigurationId(0);
		expectedResult.setNumberSlice(2);
		expectedResult.setDatatakeId("datatakeId");
		
		//Response 
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0, \"sliceNumber\":2, "
		        + "\"dataTakeId\":\"datatakeId\","
		        + "\"productType\": \"product_type\"}");
		final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
		final GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		try {
			final L0SliceMetadata result = esServices.getL0Slice("name");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void getL0SliceNoHitTest() throws Exception {
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"sliceNumber\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, false, source, null, null);
		final GetResponse getResponse = new GetResponse(getResult);
		
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
		GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
		GetResponse getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertEquals("Raised exception shall concern name",
					"url", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING startTime
		source = new BytesArray("{\"url\""
		        + ":\"url\",\"stopTime\":\"2001-01-01T00:00:00.000000Z\", "
		        + "\"instrumentConfigurationId\":0, \"sliceNumber\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
		getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertEquals("Raised exception shall concern name",
					"startTime", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING stopTime
		source = new BytesArray("{\"url\""
		        + ":\"url\",\"startTime\":\"2001-01-01T00:00:00.000000Z\","
		        + " \"instrumentConfigurationId\":0, \"sliceNumber\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
		getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertEquals("Raised exception shall concern name",
					"stopTime", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING instrumentConfigurationId
		source = new BytesArray("{\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"sliceNumber\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
		getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertEquals("Raised exception shall concern name",
					"instrumentConfigurationId", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING sliceNumber
		source = new BytesArray("{\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
		getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertEquals("Raised exception shall concern name",
					"sliceNumber", ((MetadataMalformedException) e).getMissingField());
		}
		//MISSING dataTakeId
		source = new BytesArray("{\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0, \"sliceNumber\":2,"
                + "\"productType\": \"product_type\"}");
		getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
		getResponse = new GetResponse(getResult);
		this.mockGetRequest(getResponse);
		try {
			esServices.getL0Slice("name");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertEquals("Raised exception shall concern name",
					"dataTakeId", ((MetadataMalformedException) e).getMissingField());
		}
        //MISSING product type
        source = new BytesArray("{\"url\""
                + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0, \"sliceNumber\":2}");
        getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
        getResponse = new GetResponse(getResult);
        this.mockGetRequest(getResponse);
        try {
            esServices.getL0Slice("name");
            fail("An exception should occur");
        } catch (final Exception e) {
            assertEquals("Raised exception shall concern name",
                    "productType", ((MetadataMalformedException) e).getMissingField());
        }
		
	}
	
	@Test
	public void getL0AcnTest() throws IOException {
		//Expected result
		final L0AcnMetadata expectedResult = new L0AcnMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
		expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
		expectedResult.setInstrumentConfigurationId(0);
		expectedResult.setNumberOfSlices(2);
		expectedResult.setDatatakeId("datatakeId");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{	    
		    put("productFamily", "l0_acn");
		    put("totalNumberOfSlice", "2");
		    put("instrumentConfigurationId", "0");
		    put("startTime", "2000-01-01T00:00:00.000000Z");
		    put("stopTime", "2001-01-01T00:00:00.000000Z");
		    put("productName", "name");
		    put("url", "url");
		    put("productType", "product_type");
		    put("dataTakeId", "datatakeId");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
                + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2, "
                + "\"dataTakeId\":\"datatakeId\", \"productFamily\":\"l0_acn\","
                + "\"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final L0AcnMetadata result = esServices.getL0Acn("l0_acnA", "datatakeId", "NRT");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public void getL0AcnNoHitTest() throws Exception {
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"validityStartTime\",\"stopTime\":"
		        + "\"validityStopTime\", \"instrumentConfigurationId\":0, \"L0AcnMetadata\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(0, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 0.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final L0AcnMetadata result = esServices.getL0Acn("lo_acnc", "datatakeId", "NRT");
			assertEquals("Search metadata are not equals", null, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test(expected = Exception.class)
	public void getL0AcnIOExceptionTest() throws Exception {
		this.mockSearchRequestThrowIOException();
		esServices.getL0Acn("l0_acn_n", "datatakeId", "NRT");
	}
	
	@Test
	public final void testRegex() {
		assertEquals(true, "aux_ece".matches(EsServices.REQUIRED_SATELLITE_ID_PATTERN));
		assertEquals(true, "aux_ins".matches(EsServices.REQUIRED_SATELLITE_ID_PATTERN));
		assertEquals(false, "foo_bar".matches(EsServices.REQUIRED_SATELLITE_ID_PATTERN));
	}
	
	@Test
	public void getL0AcnMalformedTest() throws Exception {
		//MISSING productName
		BytesReference source = new BytesArray("{\"url\":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\","
				+ "\"stopTime\":\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2, "
				+ "\"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHitsOne = new TotalHits(1, Relation.EQUAL_TO);
		SearchHits searchHits = new SearchHits(hits, totalHitsOne, 1.0F);
		SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
			esServices.getL0Acn("l0_acn_0", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertMissingFieldException(e, "productName");
		}
		//MISSING URL
		source = new BytesArray("{\"productName\":\"name\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, totalHitsOne, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertMissingFieldException(e, "url");
		}
		//MISSING startTime
		source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"stopTime\":\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0, "
		        + "\"totalNumberOfSlice\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, totalHitsOne, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertMissingFieldException(e, "startTime");
		}
		//MISSING stopTime
		source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\","
		        + "\"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, totalHitsOne, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertMissingFieldException(e, "stopTime");
		}
		//MISSING instrumentConfigurationId
		source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"totalNumberOfSlice\":2, \"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, totalHitsOne, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertMissingFieldException(e, "instrumentConfigurationId");
		}
		//MISSING totalNumberOfSlice
		source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0,\"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, totalHitsOne, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertMissingFieldException(e, "totalNumberOfSlice");
		}
		//MISSING dataTakeId
		source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2,"
                + "\"productType\": \"product_type\"}");
		hit = new SearchHit(1);
		hit.sourceRef(source);
		hits[0] = hit;
		searchHits = new SearchHits(hits, totalHitsOne, 1.0F);
		searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		this.mockSearchRequest(response);
		try {
		    esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
			fail("An exception should occur");
		} catch (final Exception e) {
			assertMissingFieldException(e, "dataTakeId");
		}
        //MISSING dataTakeId
        source = new BytesArray("{\"productName\":\"name\",\"url\""
                + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"instrumentConfigurationId\":0, \"totalNumberOfSlice\":2}");
        hit = new SearchHit(1);
        hit.sourceRef(source);
        hits[0] = hit;
        searchHits = new SearchHits(hits, totalHitsOne, 1.0F);
        searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
        response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        this.mockSearchRequest(response);
        try {
            esServices.getL0Acn("l0_acn_A", "datatakeId", "NRT");
            fail("An exception should occur");
        } catch (final Exception e) {
        	assertMissingFieldException(e, "productType");
        }		
	}
    
    private void assertMissingFieldException(final Exception e, final String element) {		
		final MetadataMalformedException cause = (MetadataMalformedException) e.getCause();		
		assertEquals("Raised exception shall concern name",element, cause.getMissingField());		
	}
    
    @Test
    public final void getSeaCoverageTest_OnIntersection_ShallReturnZero() throws Exception {
        final String content = "{\"productName\":\"name\","
                + "\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"productConsolidation\":\"FULL\", \"polarisation\":\"SV\", "
                + "\"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\","
                + "\"sliceCoordinates\": { \"type\":\"Polygon\","
                + "\"coordinates\": [[ [ 31.191409132621285, -22.2515096981724 ], "
                + "[ 30.65986535006709, -22.151567478119915 ], "
                + "[ 30.322883335091774, -22.27161183033393 ], "
                + "[ 29.839036899542972, -22.102216485281176 ], "
                + "[ 29.43218834810904, -22.091312758067588 ], "
                + "[ 28.794656202924212, -21.63945403410745 ], "
                + "[ 28.021370070108617, -21.485975030200585 ], "
                + "[ 27.72722781750326, -20.851801853114715 ], "
                + "[ 27.724747348753255, -20.499058526290387 ], "
                + "[ 27.296504754350508, -20.391519870691 ], "
                + "[ 26.164790887158485, -19.29308562589494 ], "
                + "[ 25.85039147309473, -18.714412937090536 ], "
                + "[ 25.649163445750162, -18.53602589281899 ], "
                + "[ 25.264225701608012, -17.736539808831417 ], "
                + "[ 26.381935255648926, -17.8460421688579 ], "
                + "[ 26.70677330903564, -17.961228936436484 ], "
                + "[ 27.04442711763073, -17.938026218337434 ], "
                + "[ 27.598243442502756, -17.290830580314008 ], "
                + "[ 28.467906121542683, -16.468400160388846 ], "
                + "[ 28.825868768028496, -16.389748630440614 ], "
                + "[ 28.947463413211263, -16.04305144619444 ], "
                + "[ 29.516834344203147, -15.644677829656388 ], "
                + "[ 30.274255812305107, -15.507786960515212 ], "
                + "[ 30.338954705534544, -15.880839125230244 ], "
                + "[ 31.173063999157677, -15.860943698797872 ], "
                + "[ 31.636498243951195, -16.071990248277885 ], "
                + "[ 31.8520406430406, -16.319417006091378 ], "
                + "[ 32.32823896661022, -16.392074069893752 ], "
                + "[ 32.847638787575846, -16.713398125884616 ], "
                + "[ 32.84986087416439, -17.97905730557718 ], "
                + "[ 32.65488569512715, -18.672089939043495 ], "
                + "[ 32.61199425632489, -19.419382826416275 ], "
                + "[ 32.772707960752626, -19.715592136313298 ], "
                + "[ 32.65974327976258, -20.304290052982317 ], "
                + "[ 32.50869306817344, -20.395292250248307 ], "
                + "[ 32.244988234188014, -21.116488539313693 ], "
                + "[ 31.191409132621285, -22.2515096981724 ] ]]}}";        
        final BytesReference source = new BytesArray(content);
        
        final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
        final GetResponse getResponse = new GetResponse(getResult);
        this.mockGetRequest(getResponse);

		final BytesReference hm = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"2012-05-05T10:10:12.000120Z\",\"validityStopTime\":"
		        + "\"2019-05-05T10:10:12.001230Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(hm);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,ShardSearchFailure.EMPTY_ARRAY,Clusters.EMPTY);
        this.mockSearchRequest(response);
        
        assertEquals(0, esServices.getSeaCoverage(ProductFamily.L0_SEGMENT, "name"));
    }
    
    @Test
    public final void getSeaCoverageTest_OnNonIntersection_ShallReturnHunderedPercent() throws Exception {
        final String content = "{\"productName\":\"name\","
                + "\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"productConsolidation\":\"FULL\", \"polarisation\":\"SV\", "
                + "\"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\","
                + "\"sliceCoordinates\": { \"type\":\"Polygon\","
                + "\"coordinates\": [[ [ 31.191409132621285, -22.2515096981724 ], "
                + "[ 30.65986535006709, -22.151567478119915 ], "
                + "[ 30.322883335091774, -22.27161183033393 ], "
                + "[ 29.839036899542972, -22.102216485281176 ], "
                + "[ 29.43218834810904, -22.091312758067588 ], "
                + "[ 28.794656202924212, -21.63945403410745 ], "
                + "[ 28.021370070108617, -21.485975030200585 ], "
                + "[ 27.72722781750326, -20.851801853114715 ], "
                + "[ 27.724747348753255, -20.499058526290387 ], "
                + "[ 27.296504754350508, -20.391519870691 ], "
                + "[ 26.164790887158485, -19.29308562589494 ], "
                + "[ 25.85039147309473, -18.714412937090536 ], "
                + "[ 25.649163445750162, -18.53602589281899 ], "
                + "[ 25.264225701608012, -17.736539808831417 ], "
                + "[ 26.381935255648926, -17.8460421688579 ], "
                + "[ 26.70677330903564, -17.961228936436484 ], "
                + "[ 27.04442711763073, -17.938026218337434 ], "
                + "[ 27.598243442502756, -17.290830580314008 ], "
                + "[ 28.467906121542683, -16.468400160388846 ], "
                + "[ 28.825868768028496, -16.389748630440614 ], "
                + "[ 28.947463413211263, -16.04305144619444 ], "
                + "[ 29.516834344203147, -15.644677829656388 ], "
                + "[ 30.274255812305107, -15.507786960515212 ], "
                + "[ 30.338954705534544, -15.880839125230244 ], "
                + "[ 31.173063999157677, -15.860943698797872 ], "
                + "[ 31.636498243951195, -16.071990248277885 ], "
                + "[ 31.8520406430406, -16.319417006091378 ], "
                + "[ 32.32823896661022, -16.392074069893752 ], "
                + "[ 32.847638787575846, -16.713398125884616 ], "
                + "[ 32.84986087416439, -17.97905730557718 ], "
                + "[ 32.65488569512715, -18.672089939043495 ], "
                + "[ 32.61199425632489, -19.419382826416275 ], "
                + "[ 32.772707960752626, -19.715592136313298 ], "
                + "[ 32.65974327976258, -20.304290052982317 ], "
                + "[ 32.50869306817344, -20.395292250248307 ], "
                + "[ 32.244988234188014, -21.116488539313693 ], "
                + "[ 31.191409132621285, -22.2515096981724 ] ]]}}";        
        final BytesReference source = new BytesArray(content);
        
        final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
        final GetResponse getResponse = new GetResponse(getResult);
        this.mockGetRequest(getResponse);


		final SearchHit hit = new SearchHit(0);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(0, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        this.mockSearchRequest(response);
        
        assertEquals(100, esServices.getSeaCoverage(ProductFamily.L0_SEGMENT, "name"));
    }
    
    
    @Test
    public final void getSeaCoverageTest_Integer_Coordinates() throws Exception {
        final String content = "{\"productName\":\"name\","
                + "\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"productConsolidation\":\"FULL\", \"polarisation\":\"SV\", "
                + "\"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\","
                + "\"sliceCoordinates\": { \"type\":\"Polygon\","
                + "\"coordinates\": [["
                + "[ -1, 52.6289 ], [ -1.4946, 54.5767 ], [ -5.2745, 54.2683 ], [ -4.6105, 52.3285 ], [ -1, 52.6289 ]"
                + "]]}}";        
        final BytesReference source = new BytesArray(content);
        
        final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
        final GetResponse getResponse = new GetResponse(getResult);
        this.mockGetRequest(getResponse);


		final SearchHit hit = new SearchHit(0);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(0, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        this.mockSearchRequest(response);
        
        assertEquals(100, esServices.getSeaCoverage(ProductFamily.L0_SEGMENT, "name"));
    }
    
    @Test
    public final void getOverpassCoverageTest_OnIntersection_ShallReturnZero() throws Exception {
        final String content = "{\"productName\":\"name\","
                + "\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"productConsolidation\":\"FULL\", \"polarisation\":\"SV\", "
                + "\"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\","
                + "\"sliceCoordinates\": { \"type\":\"Polygon\","
                + "\"coordinates\": [[ [ 31.191409132621285, -22.2515096981724 ], "
                + "[ 30.65986535006709, -22.151567478119915 ], "
                + "[ 30.322883335091774, -22.27161183033393 ], "
                + "[ 29.839036899542972, -22.102216485281176 ], "
                + "[ 29.43218834810904, -22.091312758067588 ], "
                + "[ 28.794656202924212, -21.63945403410745 ], "
                + "[ 28.021370070108617, -21.485975030200585 ], "
                + "[ 27.72722781750326, -20.851801853114715 ], "
                + "[ 27.724747348753255, -20.499058526290387 ], "
                + "[ 27.296504754350508, -20.391519870691 ], "
                + "[ 26.164790887158485, -19.29308562589494 ], "
                + "[ 25.85039147309473, -18.714412937090536 ], "
                + "[ 25.649163445750162, -18.53602589281899 ], "
                + "[ 25.264225701608012, -17.736539808831417 ], "
                + "[ 26.381935255648926, -17.8460421688579 ], "
                + "[ 26.70677330903564, -17.961228936436484 ], "
                + "[ 27.04442711763073, -17.938026218337434 ], "
                + "[ 27.598243442502756, -17.290830580314008 ], "
                + "[ 28.467906121542683, -16.468400160388846 ], "
                + "[ 28.825868768028496, -16.389748630440614 ], "
                + "[ 28.947463413211263, -16.04305144619444 ], "
                + "[ 29.516834344203147, -15.644677829656388 ], "
                + "[ 30.274255812305107, -15.507786960515212 ], "
                + "[ 30.338954705534544, -15.880839125230244 ], "
                + "[ 31.173063999157677, -15.860943698797872 ], "
                + "[ 31.636498243951195, -16.071990248277885 ], "
                + "[ 31.8520406430406, -16.319417006091378 ], "
                + "[ 32.32823896661022, -16.392074069893752 ], "
                + "[ 32.847638787575846, -16.713398125884616 ], "
                + "[ 32.84986087416439, -17.97905730557718 ], "
                + "[ 32.65488569512715, -18.672089939043495 ], "
                + "[ 32.61199425632489, -19.419382826416275 ], "
                + "[ 32.772707960752626, -19.715592136313298 ], "
                + "[ 32.65974327976258, -20.304290052982317 ], "
                + "[ 32.50869306817344, -20.395292250248307 ], "
                + "[ 32.244988234188014, -21.116488539313693 ], "
                + "[ 31.191409132621285, -22.2515096981724 ] ]]}}";        
        final BytesReference source = new BytesArray(content);
        
        final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
        final GetResponse getResponse = new GetResponse(getResult);
        this.mockGetRequest(getResponse);

		final BytesReference hm = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"2012-05-05T10:10:12.000120Z\",\"validityStopTime\":"
		        + "\"2019-05-05T10:10:12.001230Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(hm);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        this.mockSearchRequest(response);
        
        assertEquals(100, esServices.getOverpassCoverage(ProductFamily.L0_SEGMENT, "name"));
    }
    
    @Test
    public final void getOverpassCoverageTest_OnNonIntersection_ShallReturnHunderedPercent() throws Exception {
        final String content = "{\"productName\":\"name\","
                + "\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"productConsolidation\":\"FULL\", \"polarisation\":\"SV\", "
                + "\"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\","
                + "\"sliceCoordinates\": { \"type\":\"Polygon\","
                + "\"coordinates\": [[ [ 31.191409132621285, -22.2515096981724 ], "
                + "[ 30.65986535006709, -22.151567478119915 ], "
                + "[ 30.322883335091774, -22.27161183033393 ], "
                + "[ 29.839036899542972, -22.102216485281176 ], "
                + "[ 29.43218834810904, -22.091312758067588 ], "
                + "[ 28.794656202924212, -21.63945403410745 ], "
                + "[ 28.021370070108617, -21.485975030200585 ], "
                + "[ 27.72722781750326, -20.851801853114715 ], "
                + "[ 27.724747348753255, -20.499058526290387 ], "
                + "[ 27.296504754350508, -20.391519870691 ], "
                + "[ 26.164790887158485, -19.29308562589494 ], "
                + "[ 25.85039147309473, -18.714412937090536 ], "
                + "[ 25.649163445750162, -18.53602589281899 ], "
                + "[ 25.264225701608012, -17.736539808831417 ], "
                + "[ 26.381935255648926, -17.8460421688579 ], "
                + "[ 26.70677330903564, -17.961228936436484 ], "
                + "[ 27.04442711763073, -17.938026218337434 ], "
                + "[ 27.598243442502756, -17.290830580314008 ], "
                + "[ 28.467906121542683, -16.468400160388846 ], "
                + "[ 28.825868768028496, -16.389748630440614 ], "
                + "[ 28.947463413211263, -16.04305144619444 ], "
                + "[ 29.516834344203147, -15.644677829656388 ], "
                + "[ 30.274255812305107, -15.507786960515212 ], "
                + "[ 30.338954705534544, -15.880839125230244 ], "
                + "[ 31.173063999157677, -15.860943698797872 ], "
                + "[ 31.636498243951195, -16.071990248277885 ], "
                + "[ 31.8520406430406, -16.319417006091378 ], "
                + "[ 32.32823896661022, -16.392074069893752 ], "
                + "[ 32.847638787575846, -16.713398125884616 ], "
                + "[ 32.84986087416439, -17.97905730557718 ], "
                + "[ 32.65488569512715, -18.672089939043495 ], "
                + "[ 32.61199425632489, -19.419382826416275 ], "
                + "[ 32.772707960752626, -19.715592136313298 ], "
                + "[ 32.65974327976258, -20.304290052982317 ], "
                + "[ 32.50869306817344, -20.395292250248307 ], "
                + "[ 32.244988234188014, -21.116488539313693 ], "
                + "[ 31.191409132621285, -22.2515096981724 ] ]]}}";        
        final BytesReference source = new BytesArray(content);
        
        final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
        final GetResponse getResponse = new GetResponse(getResult);
        this.mockGetRequest(getResponse);


		final SearchHit hit = new SearchHit(0);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(0, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        this.mockSearchRequest(response);
        
        assertEquals(0, esServices.getOverpassCoverage(ProductFamily.L0_SEGMENT, "name"));
    }
    
    
    @Test
    public final void getOverpassCoverageTest_Integer_Coordinates() throws Exception {
        final String content = "{\"productName\":\"name\","
                + "\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
                + "\"2001-01-01T00:00:00.000000Z\", \"productConsolidation\":\"FULL\", \"polarisation\":\"SV\", "
                + "\"dataTakeId\":\"datatakeId\","
                + "\"productType\": \"product_type\","
                + "\"sliceCoordinates\": { \"type\":\"Polygon\","
                + "\"coordinates\": [["
                + "[ -1, 52.6289 ], [ -1.4946, 54.5767 ], [ -5.2745, 54.2683 ], [ -4.6105, 52.3285 ], [ -1, 52.6289 ]"
                + "]]}}";        
        final BytesReference source = new BytesArray(content);
        
        final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
        final GetResponse getResponse = new GetResponse(getResult);
        this.mockGetRequest(getResponse);


		final SearchHit hit = new SearchHit(0);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(0, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
        this.mockSearchRequest(response);
        
        assertEquals(0, esServices.getOverpassCoverage(ProductFamily.L0_SEGMENT, "name"));
    }
    
    @Test
    public final void productNameQuery() throws IOException {
    	
    	//Expected result
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductType("product_type");
		expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
		expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
		expectedResult.setFootprint(new ArrayList<>());
		expectedResult.setSwathtype("UNDEFINED");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {
			{
			    put("startTime", "2000-01-01T00:00:00.000000Z");
			    put("stopTime", "2001-01-01T00:00:00.000000Z");
			    put("productName", "name");
			    put("url", "url");
			    put("productType", "product_type");
			}});
		
		//Response 
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\","
		        + "\"productType\": \"product_type\"}");
		final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO, SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
		final GetResponse getResponse = new GetResponse(getResult);
		
		//Mocking the get Request
		this.mockGetRequest(getResponse);
		
		try {
			SearchMetadata result = esServices.productNameQuery("L0_SEGMENT", "name");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
    	
    }
    
    @Test
    public final void latestValidityClosest() throws IOException {
    	
    	// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2012-05-05T10:10:12.000120Z");
		expectedResult.setValidityStop("2019-05-05T10:10:12.001230Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("startTime", "2012-05-05T10:10:12.000120Z");
		    put("stopTime", "2019-05-05T10:10:12.001230Z");
		    put("productName", "name");
		    put("url", "url");
		    put("productType", "product_type");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2012-05-05T10:10:12.000120Z\",\"stopTime\":"
		        + "\"2019-05-05T10:10:12.001230Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final SearchMetadata result = esServices.latestValidityClosest("2012-05-05T10:10:12.000120Z", "2019-05-05T10:10:12.001230Z", "product_type", ProductFamily.L2_ACN, "FAST", "A");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
    }
    
	@Test
    public final void createMaskFootprintData() throws IOException {
    	
    	final IndexResponse response = new IndexResponse(new ShardId(new Index("name", "uuid"),5), "type", "id", 0, 0, 0, true);
    	this.mockIndexRequest(response);

		final JSONObject product = new JSONObject();
		product.put("productName", "name");
		product.put("productType", "type");
		product.put("productFamily", "L0_SLICE");

		try {
			esServices.createMaskFootprintData(MaskType.EW_SLC, product, "id");
		} catch (Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
    }
	
	@Test
	public final void valCover() throws IOException {
		// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
		expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("validityStartTime", "2000-01-01T00:00:00.000000Z");
		    put("validityStopTime", "2001-01-01T00:00:00.000000Z");
		    put("productName", "name");
		    put("productType", "product_type");
		    put("url", "url");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"2000-01-01T00:00:00.000000Z\",\"validityStopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final List<SearchMetadata> result = esServices.valCover("type", ProductFamily.L0_ACN, 
			        "beginDate", "endDate", "satelliteId", 6, "NRT");
			assertEquals("Search metadata are not equals", expectedResult, result.get(0));
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
    public final void latestValidity() throws IOException {
    	
    	// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2012-05-05T10:10:12.000120Z");
		expectedResult.setValidityStop("2019-05-05T10:10:12.001230Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("validityStartTime", "2012-05-05T10:10:12.000120Z");
		    put("validityStopTime", "2019-05-05T10:10:12.001230Z");
		    put("productName", "name");
		    put("url", "url");
		    put("productType", "product_type");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"2012-05-05T10:10:12.000120Z\",\"validityStopTime\":"
		        + "\"2019-05-05T10:10:12.001230Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final SearchMetadata result = esServices.latestValidity("2012-05-05T10:10:12.000120Z", "2019-05-05T10:10:12.001230Z", "product_type", ProductFamily.L2_ACN, "A");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public final void fullCoverage() throws IOException {
		// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2012-05-05T10:10:12.000120Z");
		expectedResult.setValidityStop("2019-05-05T10:10:12.001230Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("startTime", "2012-05-05T10:10:12.000120Z");
		    put("stopTime", "2019-05-05T10:10:12.001230Z");
		    put("productName", "name");
		    put("url", "url");
		    put("productType", "product_type");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2012-05-05T10:10:12.000120Z\",\"stopTime\":"
		        + "\"2019-05-05T10:10:12.001230Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final List<SearchMetadata> result = esServices.fullCoverage("2012-05-05T10:10:12.000120Z", "2019-05-05T10:10:12.001230Z", "product_type", ProductFamily.L2_ACN, "FAST", "A");
			assertEquals("Search metadata are not equals", expectedResult, result.get(0));
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
    public final void latestStopValidity() throws IOException {
    	
    	// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2012-05-05T10:10:12.000120Z");
		expectedResult.setValidityStop("2019-05-05T10:10:12.001230Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("validityStartTime", "2012-05-05T10:10:12.000120Z");
		    put("validityStopTime", "2019-05-05T10:10:12.001230Z");
		    put("productName", "name");
		    put("url", "url");
		    put("productType", "product_type");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"validityStartTime\":\"2012-05-05T10:10:12.000120Z\",\"validityStopTime\":"
		        + "\"2019-05-05T10:10:12.001230Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final SearchMetadata result = esServices.latestStopValidity("product_type", ProductFamily.L2_ACN, "A");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public final void latestValCoverClosest() throws IOException {
		// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
		expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("startTime", "2000-01-01T00:00:00.000000Z");
		    put("stopTime", "2001-01-01T00:00:00.000000Z");
		    put("productName", "name");
		    put("productType", "product_type");
		    put("url", "url");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final SearchMetadata result = esServices.latestValCoverClosest("2000-01-01T00:00:00.000000Z", "2001-01-01T00:00:00.000000Z", "type", ProductFamily.L0_ACN, 
			        "satelliteId");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public final void intervalQuery() throws IOException {
		// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
		expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
		expectedResult.setAdditionalProperties(new HashMap<String,String>() {{
		    put("startTime", "2000-01-01T00:00:00.000000Z");
		    put("stopTime", "2001-01-01T00:00:00.000000Z");
		    put("productName", "name");
		    put("productType", "product_type");
		    put("url", "url");
		}});
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"productType\": \"product_type\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final List<SearchMetadata> result = esServices.intervalQuery("2000-01-01T00:00:00.000000Z", "2001-01-01T00:00:00.000000Z", ProductFamily.L0_ACN, 
					"type");
			assertEquals("Search metadata are not equals", expectedResult, result.get(0));
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public final void intervalTypeQuery() throws IOException {
		// Product
		final SearchMetadata expectedResult = new SearchMetadata();
		expectedResult.setProductName("name");
		expectedResult.setProductType("product_type");
		expectedResult.setKeyObjectStorage("url");
		expectedResult.setValidityStart("2000-01-01T00:00:00.000000Z");
		expectedResult.setValidityStop("2001-01-01T00:00:00.000000Z");
		expectedResult.setMissionId("missionId");
		
		//Response
		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"url\""
		        + ":\"url\",\"startTime\":\"2000-01-01T00:00:00.000000Z\",\"stopTime\":"
		        + "\"2001-01-01T00:00:00.000000Z\", \"productType\": \"product_type\", \"missionId\": \"missionId\"}");
		final SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		final SearchHit[] hits = {hit};
		final TotalHits totalHits = new TotalHits(1, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 1.0F);
		final SearchResponseSections searchResponsSections = new SearchResponseSections(searchHits, null, null, false, Boolean.FALSE, null, 0);
		final SearchResponse response = new SearchResponse(searchResponsSections, "1", 1,1,0,25,null,null);
		
		//Mocking the search request
		this.mockSearchRequest(response);
		
		try {
			final List<SearchMetadata> result = esServices.intervalTypeQuery("2000-01-01T00:00:00.000000Z", "2001-01-01T00:00:00.000000Z", ProductFamily.L0_ACN, 
					"type", "satelliteId");
			assertEquals("Search metadata are not equals", expectedResult, result.get(0));
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
	@Test
	public final void auxiliaryQuery() throws IOException {

		final AuxMetadata expectedResult = new AuxMetadata("name", "type", "url", "2000-01-01T00:00:00.000000Z",
				"2001-01-01T00:00:00.000000Z", "missionId", "satelliteId", "UNDEFINED", new HashMap<String,String>() {{
				    put("validityStartTime", "2000-01-01T00:00:00.000000Z");
				    put("validityStopTime", "2001-01-01T00:00:00.000000Z");
				    put("productName", "name");
				    put("productType", "type");
				    put("url", "url");
				    put("missionId","missionId");
				    put("satelliteId","satelliteId");
				}});

		final BytesReference source = new BytesArray("{\"productName\":\"name\",\"productType\": \"type\","
				+ "\"url\":\"url\",\"validityStartTime\":\"2000-01-01T00:00:00.000000Z\","
				+ "\"validityStopTime\":\"2001-01-01T00:00:00.000000Z\",\"missionId\":\"missionId\","
				+ "\"satelliteId\":\"satelliteId\"}");

		// Result with boolean at true for isExist
		final GetResult getResult = new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO,
				SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, true, source, null, null);
		final GetResponse getResponse = new GetResponse(getResult);

		// Mocking the get Request
		this.mockGetRequest(getResponse);

		try {
			final AuxMetadata result = esServices.auxiliaryQuery("type", "name");
			assertEquals("Search metadata are not equals", expectedResult, result);
		} catch (final Exception e) {
			fail("Exception occurred: " + e.getMessage());
		}
	}
	
}
