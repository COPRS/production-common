package esa.s1pdgs.cpoc.prip.service.metadata;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.model.Checksum;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

@Service
public class PripElasticSearchMetadataRepo implements PripMetadataRepository {

	private static final Logger LOGGER = LogManager.getLogger(PripElasticSearchMetadataRepo.class);
	private static final String ES_INDEX = "prip";
	private static final String ES_PRIP_TYPE = "metadata";

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	@Override
	public void save(PripMetadata pripMetadata) {

		LOGGER.info("saving PRIP metadata");
		LOGGER.debug("saving PRIP metadata {}", pripMetadata);

		IndexRequest request = new IndexRequest(ES_INDEX, ES_PRIP_TYPE, pripMetadata.getName())
				.source(pripMetadata.toString(), XContentType.JSON);
		try {
			restHighLevelClient.index(request);
		} catch (IOException e) {
			LOGGER.warn("could not save PRIP metadata", e);
		}
		LOGGER.info("saving PRIP matadata successful");
	}

	@Override
	public List<PripMetadata> findAll() {
		LOGGER.info("finding PRIP metadata");

		List<PripMetadata> metadata = new ArrayList<PripMetadata>();
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		SearchRequest searchRequest = new SearchRequest(ES_INDEX);
		searchRequest.types(ES_PRIP_TYPE);
		searchRequest.source(sourceBuilder);

		try {
			SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
			LOGGER.debug("response {}", searchResponse);

			for (SearchHit hit : searchResponse.getHits().getHits()) {

				PripMetadata pm = new PripMetadata();
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();

				pm.setId(UUID.fromString((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.ID.fieldName())));
				pm.setObsKey((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.OBS_KEY.fieldName()));
				pm.setName((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.NAME.fieldName()));
				pm.setContentType((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CONTENT_TYPE.fieldName()));
				pm.setContentLength(Long.valueOf((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CONTENT_LENGTH.fieldName())));
				pm.setCreationDate(DateUtils.parse((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.CREATION_DATE.fieldName())));
				pm.setEvictionDate(DateUtils.parse((String) sourceAsMap.get(PripMetadata.FIELD_NAMES.EVICTION_DATE.fieldName())));

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
				metadata.add(pm);
			}

		} catch (IOException e) {
			LOGGER.warn("error while finding PRIP metadata", e);
		}
		LOGGER.info("finding PRIP metadata successful, number of hits {}", metadata.size());
		return metadata;
	}

	@Override
	public PripMetadata findById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
