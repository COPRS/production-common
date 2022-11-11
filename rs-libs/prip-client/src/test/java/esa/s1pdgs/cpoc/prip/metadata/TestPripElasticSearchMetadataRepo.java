package esa.s1pdgs.cpoc.prip.metadata;

import static esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList.matchAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.RecoverableDataAccessException;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter.RelationalOperator;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;

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
		repo = new PripElasticSearchMetadataRepo(restHighLevelClient, 1000);
	}

	@Test
	public void testSave() throws IOException {

		final ShardInfo shardInfo = new ShardInfo(1, 1);
		indexResponse.setShardInfo(shardInfo);
		final Result result = DocWriteResponse.Result.CREATED;

		doReturn(result).when(indexResponse).getResult();
		doReturn(indexResponse).when(restHighLevelClient).index(Mockito.any(), Mockito.any());

		repo.save(createPripMetadata());

		verify(restHighLevelClient, times(1)).index(Mockito.any(), Mockito.any());

	}

	@Test
	public void testSaveWithFailure() throws IOException {
		final ShardId shardId = new ShardId("", "", 0);
		final ShardInfo.Failure failure = new ShardInfo.Failure(shardId, "", new IOException("testexception"),
				RestStatus.CONFLICT, false);
		final ShardInfo shardInfo = new ShardInfo(1, 0, failure);
		indexResponse.setShardInfo(shardInfo);
		final Result result = DocWriteResponse.Result.NOOP;

		doReturn(result).when(indexResponse).getResult();
		doReturn(shardInfo).when(indexResponse).getShardInfo();
		doReturn(indexResponse).when(restHighLevelClient).index(Mockito.any(), Mockito.any());

		try {
			repo.save(createPripMetadata());
		} catch (RuntimeException re) {
			assertEquals("could not save PRIP metadata", re.getMessage());
		}
		verify(restHighLevelClient, times(1)).index(Mockito.any(), Mockito.any());

	}

	@Test
	public void testFindAll() throws IOException {

		final SearchHit[] hits = new SearchHit[2];

		final PripMetadata pripMetadata1 = createPripMetadata();
		final BytesReference source1 = new BytesArray(pripMetadata1.toString());
		final SearchHit h1 = new SearchHit(1);
		h1.sourceRef(source1);
		hits[0] = h1;

		final PripMetadata pripMetadata2 = createPripMetadata();
		final BytesReference source2 = new BytesArray(pripMetadata2.toString());
		final SearchHit h2 = new SearchHit(1);
		h2.sourceRef(source2);
		hits[1] = h2;
		
		final TotalHits totalHits = new TotalHits(2, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 0);

		doReturn(searchHits).when(searchResponse).getHits();

		doReturn(searchResponse).when(restHighLevelClient).search(Mockito.any(), Mockito.any());

		final List<PripMetadata> result = repo.findAll(Optional.empty(), Optional.empty());

		assertTrue(result.contains(pripMetadata1));
		assertTrue(result.contains(pripMetadata2));
	}

	@Test
	public void testFindAllNoResult() throws IOException {

		final SearchHit[] hits = new SearchHit[0];
		final TotalHits totalHits = new TotalHits(0, Relation.EQUAL_TO);

		final SearchHits searchHits = new SearchHits(hits, totalHits, 0);

		doReturn(searchHits).when(searchResponse).getHits();

		doReturn(searchResponse).when(restHighLevelClient).search(Mockito.any(), Mockito.any());

		final List<PripMetadata> result = repo.findAll(Optional.empty(), Optional.empty());

		assertEquals(0, result.size());
	}

	@Test
	public void testFindByCreationDate() throws IOException {

		final SearchHit[] hits = new SearchHit[2];

		final PripMetadata pripMetadata1 = createPripMetadata();
		final BytesReference source1 = new BytesArray(pripMetadata1.toString());
		final SearchHit h1 = new SearchHit(1);
		h1.sourceRef(source1);
		hits[0] = h1;

		final PripMetadata pripMetadata2 = createPripMetadata();
		final BytesReference source2 = new BytesArray(pripMetadata2.toString());
		final SearchHit h2 = new SearchHit(1);
		h2.sourceRef(source2);
		hits[1] = h2;

		final TotalHits totalHits = new TotalHits(2, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 0);

		doReturn(searchHits).when(searchResponse).getHits();
		doReturn(searchResponse).when(restHighLevelClient).search(Mockito.any(), Mockito.any());

		final PripDateTimeFilter f1 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f1.setValue(LocalDateTime.of(2019, 10, 16, 10, 48, 52));
		f1.setRelationalOperator(RelationalOperator.LT);

		final PripDateTimeFilter f2 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f2.setValue(LocalDateTime.of(2019, 10, 16, 10, 48, 50));
		f2.setRelationalOperator(RelationalOperator.GT);

		final List<PripMetadata> result = repo.findWithFilter(matchAll(f1, f2), Optional.empty(), Optional.empty());

		assertTrue(result.contains(pripMetadata1));
		assertTrue(result.contains(pripMetadata2));
	}

	@Test
	public void testFindByProductName() throws IOException {

		final SearchHit[] hits = new SearchHit[2];

		final PripMetadata pripMetadata1 = createPripMetadata();
		final BytesReference source1 = new BytesArray(pripMetadata1.toString());
		final SearchHit h1 = new SearchHit(1);
		h1.sourceRef(source1);
		hits[0] = h1;

		final PripMetadata pripMetadata2 = createPripMetadata();
		final BytesReference source2 = new BytesArray(pripMetadata2.toString());
		final SearchHit h2 = new SearchHit(1);
		h2.sourceRef(source2);
		hits[1] = h2;

		final TotalHits totalHits = new TotalHits(2, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 0);

		doReturn(searchHits).when(searchResponse).getHits();
		doReturn(searchResponse).when(restHighLevelClient).search(Mockito.any(), Mockito.any());

		final PripTextFilter f1 = new PripTextFilter(FIELD_NAMES.NAME);
		f1.setFunction(PripTextFilter.Function.STARTS_WITH);
		f1.setText("S1B".toLowerCase());

		final PripTextFilter f2 = new PripTextFilter(FIELD_NAMES.NAME);
		f2.setFunction(PripTextFilter.Function.CONTAINS);
		f2.setText("1SS".toLowerCase());

		final PripTextFilter f3 = new PripTextFilter(FIELD_NAMES.NAME);
		f3.setFunction(PripTextFilter.Function.CONTAINS);
		f3.setText("_001027_".toLowerCase());
		
		final PripTextFilter f4 = new PripTextFilter(FIELD_NAMES.NAME.fieldName(), PripTextFilter.Function.ENDS_WITH,
				"productName");

		final List<PripMetadata> result = repo.findWithFilter(matchAll(f1, f2, f3, f4), Optional.empty(), Optional.empty());

		assertTrue(result.contains(pripMetadata1));
		assertTrue(result.contains(pripMetadata2));
	}

	@Test
	public void testFindByCreationDateAndName() throws IOException {

		final SearchHit[] hits = new SearchHit[2];

		final PripMetadata pripMetadata1 = createPripMetadata();
		final BytesReference source1 = new BytesArray(pripMetadata1.toString());
		final SearchHit h1 = new SearchHit(1);
		h1.sourceRef(source1);
		hits[0] = h1;

		final PripMetadata pripMetadata2 = createPripMetadata();
		final BytesReference source2 = new BytesArray(pripMetadata2.toString());
		final SearchHit h2 = new SearchHit(1);
		h2.sourceRef(source2);
		hits[1] = h2;

		final TotalHits totalHits = new TotalHits(2, Relation.EQUAL_TO);
		final SearchHits searchHits = new SearchHits(hits, totalHits, 0);

		doReturn(searchHits).when(searchResponse).getHits();
		doReturn(searchResponse).when(restHighLevelClient).search(Mockito.any(), Mockito.any());

		final PripDateTimeFilter f1 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f1.setValue(LocalDateTime.of(2019, 10, 16, 10, 48, 53));
		f1.setRelationalOperator(RelationalOperator.LT);

		final PripDateTimeFilter f2 = new PripDateTimeFilter(FIELD_NAMES.CREATION_DATE);
		f2.setValue(LocalDateTime.of(2019, 10, 16, 10, 48, 50));
		f2.setRelationalOperator(RelationalOperator.GT);

		final PripTextFilter n1 = new PripTextFilter(FIELD_NAMES.NAME);
		n1.setFunction(PripTextFilter.Function.STARTS_WITH);
		n1.setText("S1B".toLowerCase());

		final PripTextFilter n2 = new PripTextFilter(FIELD_NAMES.NAME);
		n2.setFunction(PripTextFilter.Function.CONTAINS);
		n2.setText("1SS".toLowerCase());

		final PripTextFilter n3 = new PripTextFilter(FIELD_NAMES.NAME);
		n3.setFunction(PripTextFilter.Function.CONTAINS);
		n3.setText("_001170_".toLowerCase());

		final List<PripMetadata> result = repo.findWithFilter(matchAll(f1, f2, n1, n2, n3), Optional.empty(), Optional.empty());

		assertTrue(result.contains(pripMetadata1));
		assertTrue(result.contains(pripMetadata2));
	}
	
	@Test(expected=RecoverableDataAccessException.class)
	public void testFindById_OnIOException_ShallThrowRecoverableDataAccessException() throws IOException {
		doThrow(IOException.class).when(restHighLevelClient).search(Mockito.any(), Mockito.any());
		repo.findById("1");
	}

	@Test(expected=RecoverableDataAccessException.class)
	public void testFindByName_OnIOException_ShallThrowRecoverableDataAccessException() throws IOException {
		doThrow(IOException.class).when(restHighLevelClient).search(Mockito.any(), Mockito.any());
		repo.findByName("1");		
	}

	@Test(expected=RecoverableDataAccessException.class)
	public void testFindAll_OnIOException_ShallThrowRecoverableDataAccessException() throws IOException {
		doThrow(IOException.class).when(restHighLevelClient).search(Mockito.any(), Mockito.any());
		repo.findAll(Optional.empty(), Optional.empty());
	}

	@Test(expected=RecoverableDataAccessException.class)
	public void testFindWithFilter_OnIOException_ShallThrowRecoverableDataAccessException() throws IOException {
		doThrow(IOException.class).when(restHighLevelClient).search(Mockito.any(), Mockito.any());
		PripTextFilter filter = new PripTextFilter(FIELD_NAMES.NAME.fieldName(), PripTextFilter.Function.CONTAINS, "foobar");
		repo.findWithFilter(filter, Optional.empty(), Optional.empty());
	}

	private PripMetadata createPripMetadata() {
		final LocalDateTime creationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		final Checksum checksum1 = new Checksum();
		checksum1.setAlgorithm(Checksum.DEFAULT_ALGORITHM);
		checksum1.setValue("000000000000000000000");

		final Checksum checksum2 = new Checksum();
		checksum2.setAlgorithm("SHA1");
		checksum2.setValue("111111111111111111111");

		final List<Checksum> checksums = new ArrayList<>();
		checksums.add(checksum1);
		checksums.add(checksum2);

		final PripMetadata pripMetadata = new PripMetadata();
		pripMetadata.setId(UUID.randomUUID());
		pripMetadata.setObsKey("productionEvent/keyObjectStorage");
		pripMetadata.setName("productionEvent.productName");
		pripMetadata.setProductFamily(ProductFamily.AUXILIARY_FILE_ZIP);
		pripMetadata.setContentType(PripMetadata.DEFAULT_CONTENTTYPE);
		pripMetadata.setContentLength(0);
		pripMetadata.setContentDateStart(creationDate);
		pripMetadata.setContentDateEnd(creationDate);
		pripMetadata.setCreationDate(creationDate);
		pripMetadata.setEvictionDate(creationDate.plusDays(PripMetadata.DEFAULT_EVICTION_DAYS));
		pripMetadata.setChecksums(checksums);
		
		final Map<String,Object> attributes = new LinkedHashMap<>();
		attributes.put("attr_name1_string", "value1");
		attributes.put("attr_name2_string", "1");
		attributes.put("attr_name3_long", 2L);
		attributes.put("attr_name4_double", 0.4);
		attributes.put("attr_name5_double", 1.0);
		attributes.put("attr_name6_boolean", true);
		attributes.put("attr_name7_boolean", false);
		attributes.put("attr_name8_date", LocalDateTime.of(2000, 1, 1, 0, 0));
		
		pripMetadata.setAttributes(attributes);
		
		pripMetadata.setBrowseKeys(List.of("foo.png"));
		return pripMetadata;
	}

}
