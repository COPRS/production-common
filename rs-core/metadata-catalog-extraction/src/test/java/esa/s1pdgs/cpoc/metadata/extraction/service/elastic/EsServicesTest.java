package esa.s1pdgs.cpoc.metadata.extraction.service.elastic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.MaskType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.metadata.extraction.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
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
		esServices = new EsServices(elasticsearchDAO);
		
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
	public void isMetadataExistTrueTest() throws IOException, MetadataMalformedException {
		// Product
		final ProductMetadata product = new ProductMetadata();
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
	public void isMetadataExistFalseTest() throws IOException, MetadataMalformedException {
		// Product
		final ProductMetadata product = new ProductMetadata();
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
		final ProductMetadata product = new ProductMetadata();
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
		final ProductMetadata product = new ProductMetadata();
		product.put("productName", "name");
		product.put("productType", "type");
		
		//Mocking the get Request
		this.mockGetRequestThrowIOException();
		
		esServices.isMetadataExist(product);
	}
	
	@Test
	public void createMetadataTest() throws IOException, MetadataMalformedException {
		// Product
		final ProductMetadata product = new ProductMetadata();
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
		final ProductMetadata product = new ProductMetadata();
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
		final ProductMetadata product = new ProductMetadata();
		product.put("productName", "name");
		product.put("productType", "type");
		
		//Mocking the get Request
		this.mockIndexRequestThrowIOException();
		
		esServices.createMetadata(product);
	}
}
