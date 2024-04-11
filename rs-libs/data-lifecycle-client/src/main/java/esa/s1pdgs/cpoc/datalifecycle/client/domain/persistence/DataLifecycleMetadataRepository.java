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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.lang.NonNull;

import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleSortTerm;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter.DataLifecycleQueryFilter;

/**
 * Data lifecycle metadata repository interface.
 */
public interface DataLifecycleMetadataRepository {

	void save(@NonNull DataLifecycleMetadata metadata) throws DataLifecycleMetadataRepositoryException;

	void saveAndRefresh(@NonNull DataLifecycleMetadata metadata) throws DataLifecycleMetadataRepositoryException;

	void upsert(@NonNull DataLifecycleMetadata insertMetadata, @NonNull Map<String, Object> updateMetadata)
			throws DataLifecycleMetadataRepositoryException;

	void upsertAndRefresh(@NonNull DataLifecycleMetadata insertMetadata, @NonNull Map<String, Object> updateMetadata)
			throws DataLifecycleMetadataRepositoryException;

	void update(@NonNull String productName, Map<String, Object> updateMetadata) throws DataLifecycleMetadataRepositoryException;

	void updateAndRefresh(@NonNull String productName, @NonNull Map<String, Object> updateMetadata) throws DataLifecycleMetadataRepositoryException;

	DataLifecycleMetadata updateAndGet(@NonNull String productName, @NonNull Map<String, Object> updateMetadata) throws DataLifecycleMetadataRepositoryException;
	
	Optional<DataLifecycleMetadata> findByProductName(@NonNull String productName) throws DataLifecycleMetadataRepositoryException;

	List<DataLifecycleMetadata> findByProductNames(@NonNull List<String> productNames) throws DataLifecycleMetadataRepositoryException;

	/**
	 * Returns all data lifecycle metadata entries that have at least one eviction date before the given timestamp.
	 *
	 * @param timestamp the timestamp
	 * @param top       for paging
	 * @param skip      for paging
	 * @param sortTerms to order by attributes
	 * @return all data lifecycle metadata entries that have at least one eviction date before the given timestamp.
	 * @throws DataLifecycleMetadataRepositoryException on repository error
	 */
	List<DataLifecycleMetadata> findByEvictionDateBefore(@NonNull LocalDateTime timestamp, Optional<Integer> top, Optional<Integer> skip,
			List<DataLifecycleSortTerm> sortTerms) throws DataLifecycleMetadataRepositoryException;

	/**
	 * Querying the repository with filters.
	 *
	 * @param filters   to narrow the query
	 * @param top       for paging
	 * @param skip      for paging
	 * @param sortTerms to order by attributes
	 * @return the search result
	 * @throws DataLifecycleMetadataRepositoryException on repository error
	 */
	List<DataLifecycleMetadata> findWithFilters(List<DataLifecycleQueryFilter> filters, Optional<Integer> top, Optional<Integer> skip,
			List<DataLifecycleSortTerm> sortTerms) throws DataLifecycleMetadataRepositoryException;

}
