
package esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence;

import java.time.LocalDateTime;
import java.util.List;
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

	DataLifecycleMetadata saveAndGet(@NonNull DataLifecycleMetadata metadata) throws DataLifecycleMetadataRepositoryException;

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
