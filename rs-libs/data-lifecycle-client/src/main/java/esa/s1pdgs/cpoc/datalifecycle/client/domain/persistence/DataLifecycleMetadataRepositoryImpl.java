/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence;

import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.AVAILABLE_IN_LTA;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.EVICTION_DATE_IN_COMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.EVICTION_DATE_IN_UNCOMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.LAST_DATA_REQUEST;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.LAST_INSERTION_IN_COMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.LAST_INSERTION_IN_UNCOMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.LAST_MODIFIED;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.PATH_IN_COMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.PATH_IN_UNCOMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.PERSISTENT_IN_COMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.PERSISTENT_IN_UNCOMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.PRODUCT_FAMILY_IN_COMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.PRODUCT_FAMILY_IN_UNCOMPRESSED_STORAGE;
import static esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME.PRODUCT_NAME;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.datalifecycle.client.config.DlmEsClientConfiguration;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata.FIELD_NAME;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleSortTerm;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleSortTerm.DataLifecycleSortOrder;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleBooleanFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleQueryFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleRangeValueFilter;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleTextFilter;

/**
 * Data lifecycle metadata repository implementation.
 */
@Repository
public class DataLifecycleMetadataRepositoryImpl implements DataLifecycleMetadataRepository {
	
	private static final Logger LOG = LogManager.getLogger(DataLifecycleMetadataRepositoryImpl.class);

	private final RestHighLevelClient elasticsearchClient;
	private final DlmEsClientConfiguration config;
	private String elasticsearchIndex;
	private int searchResultLimit;

	// --------------------------------------------------------------------------

	@Autowired
	public DataLifecycleMetadataRepositoryImpl(DlmEsClientConfiguration config, @Qualifier("dlmEsClient") RestHighLevelClient elasticsearchClient) {
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
		this.save(metadata.getProductName(), metadata, Collections.emptyMap(), false, false);
	}

	@Override
	public void saveAndRefresh(DataLifecycleMetadata metadata) throws DataLifecycleMetadataRepositoryException {
		this.save(metadata.getProductName(), metadata, Collections.emptyMap(), false, true);
	}
		
	@Override
	public DataLifecycleMetadata updateAndGet(@NonNull String productName, @NonNull Map<String, Object> updateMetadata) throws DataLifecycleMetadataRepositoryException {
		this.updateAndRefresh(productName, updateMetadata);
		return this.findByProductName(productName)
				.orElseThrow(() -> new DataLifecycleMetadataRepositoryException("error reading product data after saving"));
	}

	@Override
	public void upsert(final DataLifecycleMetadata insertMetadata, final Map<String, Object> updateMetadata)
			throws DataLifecycleMetadataRepositoryException {
		this.save(insertMetadata.getProductName(), insertMetadata, updateMetadata, false, false);
	}

	@Override
	public void upsertAndRefresh(final DataLifecycleMetadata insertMetadata, final Map<String, Object> updateMetadata)
			throws DataLifecycleMetadataRepositoryException {
		this.save(insertMetadata.getProductName(), insertMetadata, updateMetadata, false, true);
	}
	
	@Override
	public void update(final String productName, final Map<String, Object> updateMetadata)
			throws DataLifecycleMetadataRepositoryException {
		this.save(productName, null, updateMetadata, true, false);
	}

	@Override
	public void updateAndRefresh(final String productName, final Map<String, Object> updateMetadata)
			throws DataLifecycleMetadataRepositoryException {
		this.save(productName, null, updateMetadata, true, true);
	}
	
	private void save(final String productName, final DataLifecycleMetadata insertMetadata, final Map<String, Object> updateMetadata,
			final boolean pureUpdate, final boolean refreshImmediately) throws DataLifecycleMetadataRepositoryException {
		if (null != insertMetadata && !insertMetadata.getProductName().equals(productName)) {
			throw new IllegalStateException(String.format("Productname %s does not match %s", productName, insertMetadata));
		}
		final WriteRequest<?> request;
		if (!updateMetadata.isEmpty()) {
			final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MILLIS);
			updateMetadata.put(DataLifecycleMetadata.FIELD_NAME.LAST_MODIFIED.fieldName(), (Object)now);

			if (pureUpdate) {
				request = new UpdateRequest(this.elasticsearchIndex, productName)
						.doc(updateMetadata);
			} else if (null != insertMetadata) {
				insertMetadata.setLastModified(now);
				request = new UpdateRequest(this.elasticsearchIndex, productName)
						.doc(updateMetadata)
						.upsert(insertMetadata.toJson().toString(), XContentType.JSON);
			} else {
				throw new IllegalArgumentException("insert metadata missing for upsert!");
			}
		} else if (null != insertMetadata) {
			request = new IndexRequest(this.elasticsearchIndex).id(productName)
					.source(insertMetadata.toJson().toString(), XContentType.JSON);
		} else {
			throw new IllegalArgumentException("nothing to save: at least insert metadata or update metadata is required");
		}
		if(refreshImmediately) {
			request.setRefreshPolicy(RefreshPolicy.IMMEDIATE); // <-- we want the changes to be available immediately
		}

		LOG.debug("product data lifecycle metadata save request ("
				+ this.elasticsearchClient.getLowLevelClient().getNodes().get(0).getHost() + "): " + request);

		try {
			final DocWriteResponse response;
			if (request instanceof UpdateRequest) {
				response = this.elasticsearchClient.update((UpdateRequest)request, RequestOptions.DEFAULT);
			} else {
				response = this.elasticsearchClient.index((IndexRequest)request, RequestOptions.DEFAULT);
			}

			if (DocWriteResponse.Result.CREATED == response.getResult()
					|| DocWriteResponse.Result.UPDATED == response.getResult()) {
				LOG.debug("successfully saved product data lifecycle metadata " + insertMetadata);
			} else {
				final ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
				if (shardInfo.getFailed() > 0) {
					final StringBuilder errBuilder = new StringBuilder(
							"data lifecycle metadata could not be saved successfully for product: "
									+ productName);
					for (final ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
						errBuilder.append("\nsaving error: " + failure.reason());
					}
					throw new DataLifecycleMetadataRepositoryException(errBuilder.toString());
				}

				throw new DataLifecycleMetadataRepositoryException(
						"data lifecycle metadata could not be saved successfully (but no error message returned from persistence) for product: "
								+ productName);
			}
		} catch (final IOException e) {
			throw new DataLifecycleMetadataRepositoryException("error saving product data lifecycle metadata ("
					+ productName + "): " + e.getMessage(), e);
		}
	}
	
	@Override
	public Optional<DataLifecycleMetadata> findByProductName(String name) throws DataLifecycleMetadataRepositoryException {
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		queryBuilder.must(QueryBuilders.termQuery(PRODUCT_NAME.fieldName() + ".keyword", name));

		final List<DataLifecycleMetadata> result = this.queryWithOffset(queryBuilder, Optional.empty(), Optional.empty(), null);

		if (result.isEmpty()) {
			return Optional.empty();
		}
		
		if (result.size() > 1) {
			throw new DataLifecycleMetadataRepositoryException(
					"inconsistent data -> more than one product data lifecycle metadata entry found for: " + name);
		}
		
		return Optional.of(result.get(0));
	}

	@Override
	public List<DataLifecycleMetadata> findByProductNames(@NonNull List<String> productNames) throws DataLifecycleMetadataRepositoryException {
		if (productNames.size() > this.searchResultLimit) {
			throw new DataLifecycleMetadataRepositoryException(
					"the products requested (" + productNames.size() + ") exceed the search result limit of " + this.searchResultLimit
							+ ". no paging is supported for this function yet!");
		}
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		productNames.forEach(name -> queryBuilder.should(QueryBuilders.termQuery(PRODUCT_NAME.fieldName() + ".keyword", name)));

		return this.queryWithOffset(queryBuilder, Optional.empty(), Optional.empty(), null);
	}

	@Override
	public List<DataLifecycleMetadata> findByEvictionDateBefore(LocalDateTime timestamp, Optional<Integer> top, Optional<Integer> skip,
			List<DataLifecycleSortTerm> sortTerms) throws DataLifecycleMetadataRepositoryException {
		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

		queryBuilder.should(QueryBuilders.rangeQuery(EVICTION_DATE_IN_UNCOMPRESSED_STORAGE.fieldName()).lt(timestamp));
		// OR
		queryBuilder.should(QueryBuilders.rangeQuery(EVICTION_DATE_IN_COMPRESSED_STORAGE.fieldName()).lt(timestamp));

		return this.queryWithOffset(queryBuilder, top, skip, sortTerms);
	}

	@Override
	public List<DataLifecycleMetadata> findWithFilters(List<DataLifecycleQueryFilter> filters, Optional<Integer> top, Optional<Integer> skip,
			List<DataLifecycleSortTerm> sortTerms) throws DataLifecycleMetadataRepositoryException {
		LOG.info("finding data lifecycle metadata with filters {}", filters);

		final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		buildQueryWithFilters(filters, queryBuilder);

		return this.queryWithOffset(queryBuilder, top, skip, sortTerms);
	}

	// --------------------------------------------------------------------------

	private List<DataLifecycleMetadata> queryWithOffset(final BoolQueryBuilder queryBuilder, final Optional<Integer> top, final Optional<Integer> skip,
			final List<DataLifecycleSortTerm> sortTerms) throws DataLifecycleMetadataRepositoryException {

		final List<DataLifecycleMetadata> result = new ArrayList<>();
		if (skip.orElse(0) <= 0 || skip.orElse(0) + top.orElse(0) <= this.searchResultLimit) {
			// Paging through less than searchResultLimit -> default behaviour
			LOG.info("Handling query with skip={} and top={} (max-search-hits={}) -> Use elastic classical pagination", skip.orElse(0), top.orElse(0),
					this.searchResultLimit);
			result.addAll(mapToMetadata(this.query(queryBuilder, top, skip, sortTerms)));
		} else {
			// Paging through more than searchResultLimit ->
			// 1. iterate to offset (first page by using default mechanism, each further page by using search_after)
			// 2. search_after(offset)
			Integer offset = skip.orElse(0);
			Integer pageSize = offset > this.searchResultLimit ? this.searchResultLimit : offset;
			List<SearchHit> offsetList = this.queryOffset(queryBuilder, Optional.of(pageSize), Optional.of(0), sortTerms, false, null);
			if (CollectionUtil.isEmpty(offsetList)) { // running out of results while 'scrolling' to offset
				return Collections.emptyList();
			}
			SearchHit offsetSearchHit = offsetList.get(offsetList.size() - 1);

			while (offset > this.searchResultLimit) {
				offsetList = this.queryOffset(queryBuilder, top, Optional.of(pageSize), sortTerms, true, offsetSearchHit.getSortValues());
				if (CollectionUtil.isEmpty(offsetList)) { // running out of results while 'scrolling' to offset
					return Collections.emptyList();
				}
				offsetSearchHit = offsetList.get(offsetList.size() - 1);
				offset = offset - offsetList.size();
				pageSize = offset > this.searchResultLimit ? this.searchResultLimit : offset;
			}

			LOG.info("Handling query with skip={} and top={} (max-search-hits={}) -> Use elastic search_after", skip.orElse(0), top.orElse(0),
					this.searchResultLimit);
			offsetList = this.queryOffset(queryBuilder, top, Optional.of(pageSize), sortTerms, true, offsetSearchHit.getSortValues());
			result.addAll(mapToMetadata(offsetList));
		}

		return result;
	}

	private List<SearchHit> query(final BoolQueryBuilder queryBuilder, final Optional<Integer> top, final Optional<Integer> skip,
			final List<DataLifecycleSortTerm> sortTerms) throws DataLifecycleMetadataRepositoryException {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		if (null != queryBuilder) {
			sourceBuilder.query(queryBuilder);
		}

		configurePaging(top, skip, this.searchResultLimit, sourceBuilder);
		configureSorting(sortTerms, sourceBuilder);

		return this.search(sourceBuilder);
	}

	private List<SearchHit> queryOffset(final BoolQueryBuilder queryBuilder, final Optional<Integer> top, final Optional<Integer> skip,
			final List<DataLifecycleSortTerm> sortTerms, final boolean searchAfter, final Object[] searchAfterOffset)
					throws DataLifecycleMetadataRepositoryException {
		final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		if (null != queryBuilder) {
			sourceBuilder.query(queryBuilder);
		}

		configurePaging(top, searchAfter ? Optional.of(0) : skip, this.searchResultLimit, sourceBuilder);
		configureSorting(sortTerms, sourceBuilder);

		if (searchAfter) {
			sourceBuilder.searchAfter(searchAfterOffset);
		}
		
		return this.search(sourceBuilder);
	}

	private List<SearchHit> search(final SearchSourceBuilder sourceBuilder) {
		final SearchRequest searchRequest = new SearchRequest(this.elasticsearchIndex);
		searchRequest.source(sourceBuilder);

		LOG.debug("search request: {}", searchRequest);
		try {
			final SearchResponse searchResponse = this.elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
			LOG.debug("response: {}", searchResponse);

			return Arrays.asList(searchResponse.getHits().getHits());
		} catch (final IOException e) {
			LOG.warn("error while finding data lifecycle metadata", e);
		}

		return Collections.emptyList();
	}

	// --------------------------------------------------------------------------

	private static void buildQueryWithFilters(final List<? extends DataLifecycleQueryFilter> filters, final BoolQueryBuilder queryBuilder) {
		for (final DataLifecycleQueryFilter filter : CollectionUtil.nullToEmpty(filters)) {
			if (filter instanceof DataLifecycleRangeValueFilter) {
				buildQueryWithRangeValueFilter((DataLifecycleRangeValueFilter<?>) filter, queryBuilder);
			} else if (filter instanceof DataLifecycleTextFilter) {
				buildQueryWithTextFilter((DataLifecycleTextFilter) filter, queryBuilder);
			} else if (filter instanceof DataLifecycleBooleanFilter) {
				buildQueryWithBooleanFilter((DataLifecycleBooleanFilter) filter, queryBuilder);
			} else {
				throw new IllegalArgumentException(String.format("filter type not supported: %s", filter.getClass().getSimpleName()));
			}
		}
	}

	private static void buildQueryWithRangeValueFilter(final DataLifecycleRangeValueFilter<?> filter, final BoolQueryBuilder queryBuilder) {
		switch (filter.getOperator()) {
		case LT:
			queryBuilder.must(QueryBuilders.rangeQuery(filter.getFieldName()).lt(filter.getValue()));
			break;
		case LE:
			queryBuilder.must(QueryBuilders.rangeQuery(filter.getFieldName()).lte(filter.getValue()));
			break;
		case GT:
			queryBuilder.must(QueryBuilders.rangeQuery(filter.getFieldName()).gt(filter.getValue()));
			break;
		case GE:
			queryBuilder.must(QueryBuilders.rangeQuery(filter.getFieldName()).gte(filter.getValue()));
			break;
		case EQ:
			queryBuilder.must(QueryBuilders.termQuery(filter.getFieldName(), filter.getValue()));
			break;
		case NE:
			queryBuilder.mustNot(QueryBuilders.termQuery(filter.getFieldName(), filter.getValue()));
			break;
		default:
			throw new IllegalArgumentException(String.format("filter operator not supported: %s", filter.getOperator().name()));
		}
	}

	private static void buildQueryWithTextFilter(final DataLifecycleTextFilter filter, final BoolQueryBuilder queryBuilder) {
		switch (filter.getFunction()) {
		case MATCHES_REGEX:
			queryBuilder.must(QueryBuilders.regexpQuery(filter.getFieldName(), filter.getText()));
			break;
		case STARTS_WITH:
			queryBuilder.must(QueryBuilders.wildcardQuery(filter.getFieldName(), String.format("%s*", filter.getText())));
			break;
		case ENDS_WITH:
			queryBuilder.must(QueryBuilders.wildcardQuery(filter.getFieldName(), String.format("*%s", filter.getText())));
			break;
		case CONTAINS:
			queryBuilder.must(QueryBuilders.wildcardQuery(filter.getFieldName(), String.format("*%s*", filter.getText())));
			break;
		case EQUALS:
			queryBuilder.must(QueryBuilders.matchQuery(filter.getFieldName(), filter.getText()).fuzziness(Fuzziness.ZERO).operator(Operator.AND));
			break;
		default:
			throw new IllegalArgumentException(String.format("not supported filter function: %s", filter.getFunction().name()));
		}
	}

	private static void buildQueryWithBooleanFilter(final DataLifecycleBooleanFilter filter, final BoolQueryBuilder queryBuilder) {
		switch (filter.getFunction()) {
		case EQUALS:
			queryBuilder.must(QueryBuilders.termQuery(filter.getFieldName(), filter.getValue().booleanValue()));
			break;
		case EQUALS_NOT:
			queryBuilder.mustNot(QueryBuilders.termQuery(filter.getFieldName(), filter.getValue().booleanValue()));
			break;
		default:
			throw new IllegalArgumentException(String.format("not supported filter function: %s", filter.getFunction().name()));
		}
	}

	private static void configurePaging(final Optional<Integer> top, final Optional<Integer> skip, final int searchResultLimit,
			final SearchSourceBuilder sourceBuilder) throws DataLifecycleMetadataRepositoryException {
		if (skip.isPresent()) {
			if (skip.get() >= 0) {
				sourceBuilder.from(skip.get());
			} else {
				throw new DataLifecycleMetadataRepositoryException("invalid search result offset (skip): value of " + skip.get() + " is not >= 0");
			}
		}

		if (top.isPresent()) {
			if (0 < top.get() && top.get() <= searchResultLimit) {
				sourceBuilder.size(top.get());
			} else {
				throw new DataLifecycleMetadataRepositoryException("invalid page size: value of " + top.get() + " is not > 0 and <= " + searchResultLimit);
			}
		} else {
			sourceBuilder.size(searchResultLimit);
		}
	}

	private static void configureSorting(final List<DataLifecycleSortTerm> sortTerms, final SearchSourceBuilder sourceBuilder) {
		boolean sortedByTieBraker = false;
		if (CollectionUtil.isNotEmpty(sortTerms)) {
			for (final DataLifecycleSortTerm sortTerm : sortTerms) {
				final FIELD_NAME sortField = sortTerm.getSortFieldName();
				final DataLifecycleSortOrder sortOrder = sortTerm.getSortOrderOrDefault(DataLifecycleSortOrder.ASCENDING);

				sourceBuilder.sort(getSortFieldName(sortField), sortOrderFor(sortOrder.abbreviation()));

				if (PRODUCT_NAME == sortField) {
					sortedByTieBraker = true;
				}
			}
		} else {
			// when no sorting is specified, sort by last modified descending
			sourceBuilder.sort(LAST_MODIFIED.fieldName(), SortOrder.DESC);
		}

		if (!sortedByTieBraker) {
			sourceBuilder.sort(getSortFieldName(PRODUCT_NAME), SortOrder.ASC);
		}
	}

	private static String getSortFieldName(final DataLifecycleMetadata.FIELD_NAME sorField) {
		if (DataLifecycleMetadata.FIELD_TYPE.TEXT == sorField.fieldType()) {
			return sorField.fieldName() + ".keyword";
		} else {
			return sorField.fieldName();
		}
	}

	private static SortOrder sortOrderFor(final String sortOrder) {
		if (SortOrder.ASC.name().equalsIgnoreCase(sortOrder) || SortOrder.ASC.toString().equalsIgnoreCase(sortOrder)) {
			return SortOrder.ASC;
		}
		if (SortOrder.DESC.name().equalsIgnoreCase(sortOrder) || SortOrder.DESC.toString().equalsIgnoreCase(sortOrder)) {
			return SortOrder.DESC;
		}

		throw new IllegalArgumentException(String.format("sort order not supported: %s", sortOrder));
	}

	private static List<DataLifecycleMetadata> mapToMetadata(final List<SearchHit> searchHits) {
		return CollectionUtil.nullToEmpty(searchHits).stream().map(DataLifecycleMetadataRepositoryImpl::mapToMetadata).collect(Collectors.toList());
	}

	private static DataLifecycleMetadata mapToMetadata(final SearchHit hit) {
		final Map<String, Object> sourceAsMap = hit.getSourceAsMap();
		final DataLifecycleMetadata metadata = new DataLifecycleMetadata();

		metadata.setProductName((String) sourceAsMap.get(PRODUCT_NAME.fieldName()));
		metadata.setPathInUncompressedStorage((String) sourceAsMap.get(PATH_IN_UNCOMPRESSED_STORAGE.fieldName()));
		metadata.setPathInCompressedStorage((String) sourceAsMap.get(PATH_IN_COMPRESSED_STORAGE.fieldName()));
		metadata.setPersistentInUncompressedStorage((Boolean) sourceAsMap.get(PERSISTENT_IN_UNCOMPRESSED_STORAGE.fieldName()));
		metadata.setPersistentInCompressedStorage((Boolean) sourceAsMap.get(PERSISTENT_IN_COMPRESSED_STORAGE.fieldName()));
		metadata.setAvailableInLta((Boolean) sourceAsMap.get(AVAILABLE_IN_LTA.fieldName()));

		final String evictionDateInUncompressedStorage = (String) sourceAsMap.get(EVICTION_DATE_IN_UNCOMPRESSED_STORAGE.fieldName());
		metadata.setEvictionDateInUncompressedStorage(
				(null != evictionDateInUncompressedStorage) ? DateUtils.parse(evictionDateInUncompressedStorage) : null);

		final String evictionDateInCompressedStorage = (String) sourceAsMap.get(EVICTION_DATE_IN_COMPRESSED_STORAGE.fieldName());
		metadata.setEvictionDateInCompressedStorage(
				(null != evictionDateInCompressedStorage) ? DateUtils.parse(evictionDateInCompressedStorage) : null);

		final String productFamilyInUncompressedStorage = (String) sourceAsMap.get(PRODUCT_FAMILY_IN_UNCOMPRESSED_STORAGE.fieldName());
		metadata.setProductFamilyInUncompressedStorage(
				(null != productFamilyInUncompressedStorage) ? ProductFamily.fromValue(productFamilyInUncompressedStorage) : null);

		final String productFamilyInCompressedStorage = (String) sourceAsMap.get(PRODUCT_FAMILY_IN_COMPRESSED_STORAGE.fieldName());
		metadata.setProductFamilyInCompressedStorage(
				(null != productFamilyInCompressedStorage) ? ProductFamily.fromValue(productFamilyInCompressedStorage) : null);
		
		final String lastInsertionInUncompressedStorage = (String) sourceAsMap.get(LAST_INSERTION_IN_UNCOMPRESSED_STORAGE.fieldName());
		metadata.setLastInsertionInUncompressedStorage(
				(null != lastInsertionInUncompressedStorage) ? DateUtils.parse(lastInsertionInUncompressedStorage) : null);
		
		final String lastInsertionInCompressedStorage = (String) sourceAsMap.get(LAST_INSERTION_IN_COMPRESSED_STORAGE.fieldName());
		metadata.setLastInsertionInCompressedStorage(
				(null != lastInsertionInCompressedStorage) ? DateUtils.parse(lastInsertionInCompressedStorage) : null);

		final String lastModified = (String) sourceAsMap.get(LAST_MODIFIED.fieldName());
		metadata.setLastModified((null != lastModified) ? DateUtils.parse(lastModified) : null);

		final String lastDataRequest = (String) sourceAsMap.get(LAST_DATA_REQUEST.fieldName());
		metadata.setLastDataRequest((null != lastDataRequest) ? DateUtils.parse(lastDataRequest) : null);

		LOG.debug("mapped product data lifecycle metadata from search result: " + metadata);
		return metadata;
	}

}
