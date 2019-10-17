package esa.s1pdgs.cpoc.prip.service.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripTextFilter;
import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter.Operator;

public class TestPripElasticSearchMetadataRepo {

	@Mock
	public RestHighLevelClient restHighLevelClient;

	@Mock
	IndexResponse indexResponse;

	@Mock
	SearchResponse searchResponse;

	public PripElasticSearchMetadataRepo repo;

	@Before
	public void init() {

		MockitoAnnotations.initMocks(this);
		repo = new PripElasticSearchMetadataRepo(restHighLevelClient);
	}

	@Test
	public void testSave() throws IOException {

		ShardInfo shardInfo = new ShardInfo(1, 1);
		indexResponse.setShardInfo(shardInfo);
		Result result = DocWriteResponse.Result.CREATED;

		doReturn(result).when(indexResponse).getResult();
		doReturn(indexResponse).when(restHighLevelClient).index(Mockito.any());

		repo.save(createPripMetadata());

		verify(restHighLevelClient, times(1)).index(Mockito.any());

	}

	@Test
	public void testSaveWithFailure() throws IOException {

		ShardInfo.Failure failure = new ShardInfo.Failure(null, "", new IOException("testexception"),
				RestStatus.CONFLICT, false);
		ShardInfo shardInfo = new ShardInfo(1, 0, failure);
		indexResponse.setShardInfo(shardInfo);
		Result result = DocWriteResponse.Result.NOOP;

		doReturn(result).when(indexResponse).getResult();
		doReturn(shardInfo).when(indexResponse).getShardInfo();
		doReturn(indexResponse).when(restHighLevelClient).index(Mockito.any());

		repo.save(createPripMetadata());
		verify(restHighLevelClient, times(1)).index(Mockito.any());

	}

	@Test
	public void testFindAll() throws IOException {

		SearchHit[] hits = new SearchHit[2];

		PripMetadata pripMetadata1 = createPripMetadata();
		BytesReference source1 = new BytesArray(pripMetadata1.toString());
		SearchHit h1 = new SearchHit(1);
		h1.sourceRef(source1);
		hits[0] = h1;

		PripMetadata pripMetadata2 = createPripMetadata();
		BytesReference source2 = new BytesArray(pripMetadata2.toString());
		SearchHit h2 = new SearchHit(1);
		h2.sourceRef(source2);
		hits[1] = h2;

		SearchHits searchHits = new SearchHits(hits, 2, 0);

		doReturn(searchHits).when(searchResponse).getHits();

		doReturn(searchResponse).when(restHighLevelClient).search(Mockito.any());

		List<PripMetadata> result = repo.findAll();

		assertTrue(result.contains(pripMetadata1));
		assertTrue(result.contains(pripMetadata2));
	}

	@Test
	public void testFindAllNoResult() throws IOException {

		SearchHit[] hits = new SearchHit[0];

		SearchHits searchHits = new SearchHits(hits, 0, 0);

		doReturn(searchHits).when(searchResponse).getHits();

		doReturn(searchResponse).when(restHighLevelClient).search(Mockito.any());

		List<PripMetadata> result = repo.findAll();

		assertEquals(0, result.size());
	}

	@Test
	public void testFindAllWithFailure() throws IOException {

		doThrow(new IOException("testexecption")).when(restHighLevelClient).search(Mockito.any());
		List<PripMetadata> result = repo.findAll();
		assertEquals(0, result.size());
	}

	@Test
	public void testFindByCreationDate() throws IOException {

		SearchHit[] hits = new SearchHit[2];

		PripMetadata pripMetadata1 = createPripMetadata();
		BytesReference source1 = new BytesArray(pripMetadata1.toString());
		SearchHit h1 = new SearchHit(1);
		h1.sourceRef(source1);
		hits[0] = h1;

		PripMetadata pripMetadata2 = createPripMetadata();
		BytesReference source2 = new BytesArray(pripMetadata2.toString());
		SearchHit h2 = new SearchHit(1);
		h2.sourceRef(source2);
		hits[1] = h2;

		SearchHits searchHits = new SearchHits(hits, 2, 0);

		doReturn(searchHits).when(searchResponse).getHits();
		doReturn(searchResponse).when(restHighLevelClient).search(Mockito.any());

		List<PripDateTimeFilter> creationDateIntervals = new ArrayList<>();

		PripDateTimeFilter f1 = new PripDateTimeFilter();
		f1.setDateTime(LocalDateTime.of(2019, 10, 16, 10, 48, 52));
		f1.setOperator(Operator.LT);

		PripDateTimeFilter f2 = new PripDateTimeFilter();
		f2.setDateTime(LocalDateTime.of(2019, 10, 16, 10, 48, 50));
		f2.setOperator(Operator.GT);

		creationDateIntervals.add(f1);
		creationDateIntervals.add(f2);

		List<PripMetadata> result = repo.findByCreationDate(creationDateIntervals);

		assertTrue(result.contains(pripMetadata1));
		assertTrue(result.contains(pripMetadata2));
	}

	@Test
	public void testFindByProductName() throws IOException {

		SearchHit[] hits = new SearchHit[2];

		PripMetadata pripMetadata1 = createPripMetadata();
		BytesReference source1 = new BytesArray(pripMetadata1.toString());
		SearchHit h1 = new SearchHit(1);
		h1.sourceRef(source1);
		hits[0] = h1;

		PripMetadata pripMetadata2 = createPripMetadata();
		BytesReference source2 = new BytesArray(pripMetadata2.toString());
		SearchHit h2 = new SearchHit(1);
		h2.sourceRef(source2);
		hits[1] = h2;

		SearchHits searchHits = new SearchHits(hits, 2, 0);

		doReturn(searchHits).when(searchResponse).getHits();
		doReturn(searchResponse).when(restHighLevelClient).search(Mockito.any());

		List<PripTextFilter> nameFilters = new ArrayList<>();

		PripTextFilter f1 = new PripTextFilter();
		f1.setFunction(PripTextFilter.Function.STARTS_WITH);
		f1.setText("S1B".toLowerCase());

		PripTextFilter f2 = new PripTextFilter();
		f2.setFunction(PripTextFilter.Function.CONTAINS);
		f2.setText("1SS".toLowerCase());

		PripTextFilter f3 = new PripTextFilter();
		f3.setFunction(PripTextFilter.Function.CONTAINS);
		f3.setText("_001027_".toLowerCase());

		nameFilters.add(f1);
		nameFilters.add(f2);
		nameFilters.add(f3);

		List<PripMetadata> result = repo.findByProductName(nameFilters);

		assertTrue(result.contains(pripMetadata1));
		assertTrue(result.contains(pripMetadata2));
	}

	@Test
	public void testFindByCreationDateAndName() throws IOException {

		SearchHit[] hits = new SearchHit[2];

		PripMetadata pripMetadata1 = createPripMetadata();
		BytesReference source1 = new BytesArray(pripMetadata1.toString());
		SearchHit h1 = new SearchHit(1);
		h1.sourceRef(source1);
		hits[0] = h1;

		PripMetadata pripMetadata2 = createPripMetadata();
		BytesReference source2 = new BytesArray(pripMetadata2.toString());
		SearchHit h2 = new SearchHit(1);
		h2.sourceRef(source2);
		hits[1] = h2;

		SearchHits searchHits = new SearchHits(hits, 2, 0);

		doReturn(searchHits).when(searchResponse).getHits();
		doReturn(searchResponse).when(restHighLevelClient).search(Mockito.any());

		List<PripDateTimeFilter> creationDateFilters = new ArrayList<>();

		PripDateTimeFilter f1 = new PripDateTimeFilter();
		f1.setDateTime(LocalDateTime.of(2019, 10, 16, 10, 48, 53));
		f1.setOperator(Operator.LT);

		PripDateTimeFilter f2 = new PripDateTimeFilter();
		f2.setDateTime(LocalDateTime.of(2019, 10, 16, 10, 48, 50));
		f2.setOperator(Operator.GT);

		creationDateFilters.add(f1);
		creationDateFilters.add(f2);

		List<PripTextFilter> nameFilters = new ArrayList<>();

		PripTextFilter n1 = new PripTextFilter();
		n1.setFunction(PripTextFilter.Function.STARTS_WITH);
		n1.setText("S1B".toLowerCase());

		PripTextFilter n2 = new PripTextFilter();
		n2.setFunction(PripTextFilter.Function.CONTAINS);
		n2.setText("1SS".toLowerCase());

		PripTextFilter n3 = new PripTextFilter();
		n3.setFunction(PripTextFilter.Function.CONTAINS);
		n3.setText("_001170_".toLowerCase());

		nameFilters.add(n1);
		nameFilters.add(n2);
		nameFilters.add(n3);

		List<PripMetadata> result = repo.findByCreationDateAndProductName(creationDateFilters, nameFilters);

		assertTrue(result.contains(pripMetadata1));
		assertTrue(result.contains(pripMetadata2));

	}

	private PripMetadata createPripMetadata() {
		LocalDateTime creationDate = LocalDateTime.now();

		Checksum checksum1 = new Checksum();
		checksum1.setAlgorithm(Checksum.DEFAULT_ALGORITHM);
		checksum1.setValue("000000000000000000000");

		Checksum checksum2 = new Checksum();
		checksum2.setAlgorithm("SHA1");
		checksum2.setValue("111111111111111111111");

		List<Checksum> checksums = new ArrayList<>();
		checksums.add(checksum1);
		checksums.add(checksum2);

		PripMetadata pripMetadata = new PripMetadata();
		pripMetadata.setId(UUID.randomUUID());
		pripMetadata.setObsKey("productDto/keyObjectStorage");
		pripMetadata.setName("productDto.productName");
		pripMetadata.setProductFamily(ProductFamily.AUXILIARY_FILE_ZIP);
		pripMetadata.setContentType(PripMetadata.DEFAULT_CONTENTTYPE);
		pripMetadata.setContentLength(0);
		pripMetadata.setCreationDate(creationDate);
		pripMetadata.setEvictionDate(creationDate.plusDays(PripMetadata.DEFAULT_EVICTION_DAYS));
		pripMetadata.setChecksums(checksums);
		return pripMetadata;
	}

}
