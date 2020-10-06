
package esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.datalifecycle.trigger.config.EsClientConfiguration;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;

/**
 * Data lifecycle metadata repository implementation.
 */
@Repository
public class DataLifecycleMetadataRepositoryImpl implements DataLifecycleMetadataRepository {
	
	private static final Logger LOG = LogManager.getLogger(DataLifecycleMetadataRepositoryImpl.class);

	private final RestHighLevelClient elasticsearchClient;
	private final EsClientConfiguration config;
	private String elasticsearchIndex;
	private int searchResultLimit;

	// --------------------------------------------------------------------------

	@Autowired
	public DataLifecycleMetadataRepositoryImpl(EsClientConfiguration config, RestHighLevelClient elasticsearchClient) {
		this.config = config;
		this.elasticsearchClient = elasticsearchClient;
	}

	@PostConstruct
	public void init() {
		this.elasticsearchIndex = this.config.getEsIndexName();
		this.searchResultLimit = this.config.getEsSearchResultLimit();
	}

	// --------------------------------------------------------------------------
	
	@Override
	public void save(DataLifecycleMetadata metadata) throws DataLifecycleMetadataRepositoryException {
		final IndexRequest request = new IndexRequest(this.elasticsearchIndex).id(metadata.getProductName())
				.source(metadata.toString(), XContentType.JSON);
		LOG.debug("product data lifecycle metadata save request ("
				+ this.elasticsearchClient.getLowLevelClient().getNodes().get(0).getHost() + "): " + request);

		try {
			final IndexResponse response = this.elasticsearchClient.index(request, RequestOptions.DEFAULT);

			if (DocWriteResponse.Result.CREATED == response.getResult()
					|| DocWriteResponse.Result.UPDATED == response.getResult()) {
				LOG.debug("successfully saved product data lifecycle metadata " + metadata);
			} else {
				final ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
				if (shardInfo.getFailed() > 0) {
					final StringBuilder errBuilder = new StringBuilder(
							"data lifecycle metadata could not be saved successfully for product: "
									+ metadata.getProductName());
					for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
						errBuilder.append("\nsaving error: " + failure.reason());
					}
					throw new DataLifecycleMetadataRepositoryException(errBuilder.toString());
				}
			}
		} catch (IOException e) {
			throw new DataLifecycleMetadataRepositoryException("error saving product data lifecycle metadata ("
					+ metadata.getProductName() + "): " + e.getMessage(), e);
		}
	}
	
	@Override
	public Optional<DataLifecycleMetadata> findByProductName(String name) throws DataLifecycleMetadataRepositoryException {
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		queryBuilder.must(
				QueryBuilders.termQuery(DataLifecycleMetadata.FIELD_NAME.PRODUCT_NAME.fieldName() + ".keyword", name));
		
		final List<DataLifecycleMetadata> result = this.query(queryBuilder, null, null, null, null);
		
		if (result.isEmpty()) {
			return Optional.empty();
		}
		
		if (result.size() > 1) {
			throw new DataLifecycleMetadataRepositoryException(
					"inconsistent data -> more than one product data lifecycle metadata entry found for: " + name);
		}
		
		return Optional.of(result.get(0));
	}

	// --------------------------------------------------------------------------
	
	private List<DataLifecycleMetadata> query(BoolQueryBuilder queryBuilder, Integer top, Integer skip,
			String sortFieldName, SortOrder sortOrder) throws DataLifecycleMetadataRepositoryException {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		if (null != queryBuilder) {
			sourceBuilder.query(queryBuilder);
		}

		// paging
		if (null != skip && 0 <= skip) {
			sourceBuilder.from(skip);
		}
		if (null != top && 0 <= top && top <= this.searchResultLimit) {
			sourceBuilder.size(top);
		} else {
			sourceBuilder.size(this.searchResultLimit);
		}

		// sorting
		if (null != sortFieldName && !sortFieldName.isEmpty()) {
			if (null == sortOrder) {
				sortOrder = SortOrder.ASC;
			}
			sourceBuilder.sort(sortFieldName, sortOrder);
		}

		final SearchRequest searchRequest = new SearchRequest(this.elasticsearchIndex);
		searchRequest.source(sourceBuilder);

		final List<DataLifecycleMetadata> result = new ArrayList<>();
		LOG.debug("product data lifecycle metadata search request ("
				+ this.elasticsearchClient.getLowLevelClient().getNodes().get(0).getHost() + "): " + searchRequest);

		try {
			final SearchResponse searchResponse = this.elasticsearchClient.search(searchRequest,
					RequestOptions.DEFAULT);
			LOG.debug("product data lifecycle metadata search response: " + searchResponse);

			for (SearchHit hit : searchResponse.getHits().getHits()) {
				result.add(this.mapToMetadata(hit));
			}
		} catch (Exception e) {
			throw new DataLifecycleMetadataRepositoryException("error searching for product data lifecycle metadata ("
					+ (null != e.getMessage() ? e.getMessage() : e.getClass().getSimpleName()) + ")", e);
		}

		LOG.debug("found " + result.size() + " elements on product data lifecycle metadata search");
		if (result.size() == this.searchResultLimit) {
			LOG.warn(
					"the number of elements found on product data lifecycle metadata search equals the configured limit."
							+ " it may be the case that more elements exist in elasticsearch but are not returned because of the limit!");
		}

		return result;
	}

	private DataLifecycleMetadata mapToMetadata(SearchHit hit) {
		final Map<String, Object> sourceAsMap = hit.getSourceAsMap();
		final DataLifecycleMetadata metadata = new DataLifecycleMetadata();

		metadata.setProductName((String) sourceAsMap.get(DataLifecycleMetadata.FIELD_NAME.PRODUCT_NAME.fieldName()));
		metadata.setPathInUncompressedStorage(
				(String) sourceAsMap.get(DataLifecycleMetadata.FIELD_NAME.PATH_IN_UNCOMPRESSED_STORAGE.fieldName()));
		metadata.setPathInCompressedStorage(
				(String) sourceAsMap.get(DataLifecycleMetadata.FIELD_NAME.PATH_IN_COMPRESSED_STORAGE.fieldName()));
		metadata.setEvictionDateInUncompressedStorage(DateUtils.parse((String) sourceAsMap
				.get(DataLifecycleMetadata.FIELD_NAME.EVICTION_DATE_IN_UNCOMPRESSED_STORAGE.fieldName())));
		metadata.setEvictionDateInCompressedStorage(DateUtils.parse((String) sourceAsMap
				.get(DataLifecycleMetadata.FIELD_NAME.EVICTION_DATE_IN_COMPRESSED_STORAGE.fieldName())));
		metadata.setPersistentInUncompressedStorage((Boolean) sourceAsMap
				.get(DataLifecycleMetadata.FIELD_NAME.PERSISTENT_IN_UNCOMPRESSED_STORAGE.fieldName()));
		metadata.setPersistentInCompressedStorage((Boolean) sourceAsMap
				.get(DataLifecycleMetadata.FIELD_NAME.PERSISTENT_IN_COMPRESSED_STORAGE.fieldName()));
		metadata.setAvailableInLta(
				(Boolean) sourceAsMap.get(DataLifecycleMetadata.FIELD_NAME.AVAILABLE_IN_LTA.fieldName()));
		
		LOG.debug("mapped product data lifecycle metadata from search result: " + metadata);
		return metadata;
	}

}
