package esa.s1pdgs.cpoc.prip.service.metadata;

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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripDateTimeIntervalFilter;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripTextFilter;

@Service
public class PripElasticSearchMetadataRepo implements PripMetadataRepository {

	private static final Logger LOGGER = LogManager.getLogger(PripElasticSearchMetadataRepo.class);
	private static final String ES_INDEX = "prip";
	private static final String ES_PRIP_TYPE = "metadata";
	private static final int DEFAULT_MAX_HITS = 100;

	private final RestHighLevelClient restHighLevelClient;

	@Autowired
	public PripElasticSearchMetadataRepo(RestHighLevelClient restHighLevelClient) {
		this.restHighLevelClient = restHighLevelClient;
	}

	@Override
	public void save(PripMetadata pripMetadata) {

		LOGGER.info("saving PRIP metadata");
		LOGGER.debug("saving PRIP metadata {}", pripMetadata);

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
	public List<PripMetadata> findAll() {
		LOGGER.info("finding PRIP metadata");

		List<PripMetadata> metadata = new ArrayList<>();
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.size(DEFAULT_MAX_HITS);
		SearchRequest searchRequest = new SearchRequest(ES_INDEX);
		searchRequest.types(ES_PRIP_TYPE);
		searchRequest.source(sourceBuilder);

		try {
			SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
			LOGGER.trace("response {}", searchResponse);

			for (SearchHit hit : searchResponse.getHits().getHits()) {
				metadata.add(mapSearchHitToPripMetadata(hit));
			}

		} catch (IOException e) {
			LOGGER.warn("error while finding PRIP metadata", e);
		}
		LOGGER.info("finding PRIP metadata successful, number of hits {}", metadata.size());
		return metadata;
	}

	@Override
	public PripMetadata findById(String id) {

		LOGGER.info("finding PRIP metadata with id {}", id);

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.termQuery(PripMetadata.FIELD_NAMES.ID.fieldName(), id));

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
	public List<PripMetadata> findByCreationDate(List<PripDateTimeIntervalFilter> creationDateIntervals) {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

		for (PripDateTimeIntervalFilter interval : creationDateIntervals) {
			if (interval.getDateTimeStart().isAfter(interval.getDateTimeStop())) {
				throw new IllegalArgumentException("creationDateStart is after creationDateStop");
			}

			LOGGER.info("finding PRIP metadata within creationDate interval {} and {}", interval.getDateTimeStart(),
					interval.getDateTimeStop());

			queryBuilder.must(QueryBuilders.rangeQuery(PripMetadata.FIELD_NAMES.CREATION_DATE.fieldName())
					.from(DateUtils.formatToMetadataDateTimeFormat(interval.getDateTimeStart()))
					.to(DateUtils.formatToMetadataDateTimeFormat(interval.getDateTimeStop())));

		}
		sourceBuilder.query(queryBuilder);

		SearchRequest searchRequest = new SearchRequest(ES_INDEX);
		searchRequest.types(ES_PRIP_TYPE);
		searchRequest.source(sourceBuilder);

		List<PripMetadata> metadata = new ArrayList<>();

		try {
			SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
			LOGGER.trace("response {}", searchResponse);

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

	@Override
	public List<PripMetadata> findByProductName(List<PripTextFilter> nameFilters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PripMetadata> findByCreationDateAndProductName(List<PripDateTimeIntervalFilter> creationDateIntervals,
			List<PripTextFilter> nameFilters) {
		// TODO Auto-generated method stub
		return null;
	}

}
