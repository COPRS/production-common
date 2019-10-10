package esa.s1pdgs.cpoc.prip.service.metadata;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	}

	@Override
	public List<PripMetadata> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PripMetadata findById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
