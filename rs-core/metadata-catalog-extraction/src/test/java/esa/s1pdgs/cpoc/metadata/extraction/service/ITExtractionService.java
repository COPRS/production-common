package esa.s1pdgs.cpoc.metadata.extraction.service;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.UUID;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.seqno.SequenceNumbers;
import org.elasticsearch.index.shard.ShardId;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.ElasticsearchDAO;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.queue.OQCFlag;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan("esa.s1pdgs.cpoc")
public class ITExtractionService {

	@MockBean
	ObsClient mockObsClient;
	
	@MockBean
	RestHighLevelClient mockRestHighLevelClient;
	
	@MockBean
	ElasticsearchDAO mockElasticsearchDAO;

	@Autowired
	ExtractionService extractionService;
	
	private static GetResponse newGetResponse_withExistsFalse() {
		final boolean exists = false;
		return new GetResponse(new GetResult("index", "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO,
				SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, exists, null, null, null));
	}
	
	private static IndexResponse newIndexResponse_withCreatedTrue() {
		final boolean created = true;
		return new IndexResponse(new ShardId("foo", "bar", 23), "type", "id", SequenceNumbers.UNASSIGNED_SEQ_NO,
				SequenceNumbers.UNASSIGNED_PRIMARY_TERM, 0L, created);
	}
	
	private static CatalogJob newCatalogJob(final String productName,
			final ProductFamily productFamily, final String timeliness) {
		return new CatalogJob(productName, productName, productFamily, "NOMINAL", OQCFlag.NOT_CHECKED,
				timeliness, UUID.randomUUID());
	}
	
	@BeforeEach
	public void beforeEach() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void testExtractionService_onS2AUX_shallPersistValidRecord() throws IOException {
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.zip",
				ProductFamily.S2_AUX, "NRT")); // timeliness will only be persisted if not null
		
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));

		assertEquals("S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.zip",
				indexRequest.id());
		
		assertEquals("S2A_OPER_AUX_SADATA_EPAE_20190222T003515_V20190221T190438_20190221T204519_A019158_WF_LN.zip",
				metadata.getString("productName"));
		assertEquals(ProductFamily.S2_AUX.name(), metadata.getString("productFamily"));
		assertEquals("S2", metadata.getString("missionId"));
		assertEquals("A", metadata.getString("satelliteId"));
		assertEquals("OPER", metadata.getString("productClass"));
		assertEquals("AUX_SADATA", metadata.getString("productType"));
		assertEquals("2019-02-22T00:35:15.000000Z", metadata.getString("creationTime"));
		assertEquals("2019-02-21T19:04:38.000000Z", metadata.getString("validityStartTime"));
		assertEquals("2019-02-21T20:45:19.000000Z", metadata.getString("validityStopTime"));
		assertEquals("019158", metadata.getString("absoluteOrbit"));
		assertEquals("F", metadata.getString("completenessId"));
		assertEquals("N", metadata.getString("degradationId"));
		assertEquals("NRT", metadata.getString("timeliness"));
		assertTrue(metadata.has("insertionTime"));
		assertEquals(14, metadata.length());
	}
	
	@Test
	public void testExtractionService_onS2GIP_shallPersistValidRecord() throws IOException {
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S2B_OPER_GIP_R2DEFI_MPC__20170206T103039_V20170101T000000_21000101T000000_B8A.TGZ",
				ProductFamily.S2_AUX, "NRT")); // timeliness will only be persisted if not null
		
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));

		assertEquals("S2B_OPER_GIP_R2DEFI_MPC__20170206T103039_V20170101T000000_21000101T000000_B8A.TGZ",
				indexRequest.id());
		
		assertEquals("S2B_OPER_GIP_R2DEFI_MPC__20170206T103039_V20170101T000000_21000101T000000_B8A.TGZ", metadata.getString("productName"));
		assertEquals(ProductFamily.S2_AUX.name(), metadata.getString("productFamily"));
		assertEquals("S2", metadata.getString("missionId"));
		assertEquals("B", metadata.getString("satelliteId"));
		assertEquals("OPER", metadata.getString("productClass"));
		assertEquals("GIP_R2DEFI", metadata.getString("productType"));
		assertEquals("2017-02-06T10:30:39.000000Z", metadata.getString("creationTime"));
		assertEquals("2017-01-01T00:00:00.000000Z", metadata.getString("validityStartTime"));
		assertEquals("2100-01-01T00:00:00.000000Z", metadata.getString("validityStopTime"));
		assertEquals("8A", metadata.getString("bandIndexId"));
		assertEquals("NRT", metadata.getString("timeliness"));
		assertTrue(metadata.has("insertionTime"));
		assertEquals(12, metadata.length());
	}
	
	@Test
	public void testExtractionService_onHKTM_shallPersistValidRecord() throws IOException {
		doReturn(newGetResponse_withExistsFalse()).when(mockElasticsearchDAO).get(Mockito.any(GetRequest.class));
		doReturn(newIndexResponse_withCreatedTrue()).when(mockElasticsearchDAO).index(Mockito.any(IndexRequest.class));
		ArgumentCaptor<IndexRequest> argumentCaptor = ArgumentCaptor.forClass(IndexRequest.class);

		extractionService.apply(newCatalogJob(
				"S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.tar",
				ProductFamily.S2_HKTM, "NRT")); // timeliness will only be persisted if not null
		
		verify(mockElasticsearchDAO).index(argumentCaptor.capture());
		IndexRequest indexRequest = argumentCaptor.getValue();
		JSONObject metadata = new JSONObject(indexRequest.source().utf8ToString());
		System.out.println(metadata.toString(4));

		assertEquals("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.tar",
				indexRequest.id());
		
		assertEquals("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.tar", metadata.getString("productName"));
		assertEquals(ProductFamily.S2_HKTM.name(), metadata.getString("productFamily"));
		assertEquals("S2A_OPER_PRD_HKTM___20191203T051837_20191203T051842_0001.tar", metadata.getString("productName"));
		assertEquals("S2", metadata.getString("missionId"));
		assertEquals("A", metadata.getString("satelliteId"));
		assertEquals("OPER", metadata.getString("productClass"));
		assertEquals("PRD_HKTM__", metadata.getString("productType"));
		assertEquals("2019-12-03T05:18:37.000000Z", metadata.getString("validityStartTime"));
		assertEquals("2019-12-03T05:18:42.000000Z", metadata.getString("validityStopTime"));
		assertEquals("NRT", metadata.getString("timeliness"));
		assertTrue(metadata.has("insertionTime"));
		assertEquals(10, metadata.length());
	}
	
}
