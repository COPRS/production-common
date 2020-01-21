package esa.s1pdgs.cpoc.prip.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripTextFilter;

@Service
public class PripElasticSearchMetadataRepo implements PripMetadataRepository {

	private static final Logger LOGGER = LogManager.getLogger(PripElasticSearchMetadataRepo.class);
	private static final String ES_INDEX = "prip";
	private static final String ES_PRIP_TYPE = "metadata";
	private final int maxSearchHits;

	private final RestHighLevelClient restHighLevelClient;

	@Autowired
	public PripElasticSearchMetadataRepo(RestHighLevelClient restHighLevelClient,
			@Value("${prip-client.repository.max-search-hits}") final int maxSearchHits) {
		this.restHighLevelClient = restHighLevelClient;
		this.maxSearchHits = maxSearchHits;
	}

	@Override
	public void save(PripMetadata pripMetadata) {

		LOGGER.info("saving PRIP metadata {}", pripMetadata);

		IndexRequest request = new IndexRequest(ES_INDEX, ES_PRIP_TYPE, pripMetadata.getName())
				.source(pripMetadata.toString(), XContentType.JSON);
		try {
			IndexResponse indexResponse = restHighLevelClient.index(request);

			if (indexResponse.getResult() == DocWriteResponse.Result.CREATED
					|| indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
				LOGGER.info("saving PRIP metadata successful");
			} else {
				ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
				if (shardInfo.getFailed() > 0) {
					LOGGER.error("could not save PRIP metadata");
					for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
						String reason = failure.reason();
						LOGGER.error(reason);
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("could not save PRIP metadata", e);
		}
	}

	@Override
	public PripMetadata findById(String id) {

		LOGGER.info("finding PRIP metadata with id {}", id);

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.matchQuery(PripMetadata.FIELD_NAMES.ID.fieldName(), id));

		SearchRequest searchRequest = new SearchRequest(ES_INDEX);
		searchRequest.types(ES_PRIP_TYPE);
		searchRequest.source(sourceBuilder);

		PripMetadata pripMetadata = null;

		try {
			SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
			LOGGER.trace("response {}", searchResponse);

			if (searchResponse.getHits().getHits().length > 0) {
				pripMetadata = mapSearchHitToPripMetadata(searchResponse.getHits().getHits()[0]);
			} else {
				LOGGER.warn("PRIP metadata with id {} not found", id);
			}

		} catch (IOException e) {
			LOGGER.warn("error while finding PRIP metadata", e);
		}
		LOGGER.info("finding PRIP metadata successful");
		return pripMetadata;
	}

	@Override
	public List<PripMetadata> findAll() {
		LOGGER.info("finding PRIP metadata");
		return query(null);
	}

	@Override
	public List<PripMetadata> findByCreationDate(List<PripDateTimeFilter> creationDateFilters) {
		LOGGER.info("finding PRIP metadata with creationDate filters {}", creationDateFilters);
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		buildQueryWithDateTimeFilters(creationDateFilters, queryBuilder, PripMetadata.FIELD_NAMES.CREATION_DATE);
		return query(queryBuilder);
	}

	@Override
	public List<PripMetadata> findByProductName(List<PripTextFilter> nameFilters) {
		LOGGER.info("finding PRIP metadata with name filters {}", nameFilters);
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		buildQueryWithTextFilters(nameFilters, queryBuilder, PripMetadata.FIELD_NAMES.NAME);
		return query(queryBuilder);
	}

	@Override
	public List<PripMetadata> findByCreationDateAndProductName(List<PripDateTimeFilter> creationDateFilters,
			List<PripTextFilter> nameFilters) {
		LOGGER.info("finding PRIP metadata with creationDate filters {} and name filters {}", creationDateFilters,
				nameFilters);
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		buildQueryWithDateTimeFilters(creationDateFilters, queryBuilder, PripMetadata.FIELD_NAMES.CREATION_DATE);
		buildQueryWithTextFilters(nameFilters, queryBuilder, PripMetadata.FIELD_NAMES.NAME);
		return query(queryBuilder);
	}

	private void buildQueryWithDateTimeFilters(List<PripDateTimeFilter> dateTimeFilters, BoolQueryBuilder queryBuilder,
			PripMetadata.FIELD_NAMES fieldName) {

		for (PripDateTimeFilter filter : dateTimeFilters) {

			RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName.fieldName());

			switch (filter.getOperator()) {
			case LT:
				rangeQueryBuilder.lt(filter.getDateTime());
				break;
			case GT:
				rangeQueryBuilder.gt(filter.getDateTime());
				break;
			default:
				throw new IllegalArgumentException(
						String.format("not supported filter operator: %s", filter.getOperator().name()));
			}
			queryBuilder.must(rangeQueryBuilder);
		}
	}

	private void buildQueryWithTextFilters(List<PripTextFilter> textFilters, BoolQueryBuilder queryBuilder,
			PripMetadata.FIELD_NAMES fieldName) {

		for (PripTextFilter filter : textFilters) {

			switch (filter.getFunction()) {
			case STARTS_WITH:
				queryBuilder.must(QueryBuilders.wildcardQuery(fieldName.fieldName(),
						String.format("%s*", filter.getText().toLowerCase())));
				break;
			case CONTAINS:
				queryBuilder.must(QueryBuilders.wildcardQuery(fieldName.fieldName(),
						String.format("*%s*", filter.getText().toLowerCase())));
				break;
			default:
				throw new IllegalArgumentException(
						String.format("not supported filter function: %s", filter.getFunction().name()));
			}
		}
	}

	private List<PripMetadata> query(BoolQueryBuilder queryBuilder) {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		if (queryBuilder != null) {
			sourceBuilder.query(queryBuilder);
		}
		sourceBuilder.size(maxSearchHits);
		sourceBuilder.sort(PripMetadata.FIELD_NAMES.CREATION_DATE.fieldName(), SortOrder.ASC);

		SearchRequest searchRequest = new SearchRequest(ES_INDEX);
		searchRequest.types(ES_PRIP_TYPE);
		searchRequest.source(sourceBuilder);

		List<PripMetadata> metadata = new ArrayList<>();

		LOGGER.debug("search request: {}", searchRequest);

		try {
			SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
			LOGGER.debug("response: {}", searchResponse);

			for (SearchHit hit : searchResponse.getHits().getHits()) {
				metadata.add(mapSearchHitToPripMetadata(hit));
			}

		} catch (IOException e) {
			LOGGER.warn("error while finding PRIP metadata", e);
		}
		LOGGER.info("finding PRIP metadata successful, number of hits {}", metadata.size());
		return metadata;
	}

	private PripMetadata mapSearchHitToPripMetadata(SearchHit hit) {
		Map<String, Object> sourceAsMap = hit.getSourceAsMap();
		PripMetadata pm = new PripMetadata();

		pm.setId(UUID.fromString((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.ID.fieldName())));
		pm.setObsKey((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.OBS_KEY.fieldName()));
		pm.setName((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.NAME.fieldName()));
		pm.setProductFamily(
				ProductFamily.valueOf((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.PRODUCT_FAMILY.fieldName())));
		pm.setContentType((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CONTENT_TYPE.fieldName()));
		pm.setContentLength(
				Long.valueOf((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CONTENT_LENGTH.fieldName())));
		pm.setCreationDate(
				DateUtils.parse((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CREATION_DATE.fieldName())));
		pm.setEvictionDate(
				DateUtils.parse((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.EVICTION_DATE.fieldName())));

		List<Checksum> checksumList = new ArrayList<>();
		for (Map<String, Object> c : (List<Map<String, Object>>) sourceAsMap
				.get(PripMetadata.FIELD_NAMES.CHECKSUM.fieldName())) {
			Checksum checksum = new Checksum();
			checksum.setAlgorithm((String) c.get(Checksum.FIELD_NAMES.ALGORITHM.fieldName()));
			checksum.setValue((String) c.get(Checksum.FIELD_NAMES.VALUE.fieldName()));
			checksumList.add(checksum);
		}
		pm.setChecksums(checksumList);

		LOGGER.debug("hit {}", pm);
		return pm;
	}

}
