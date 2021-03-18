
package esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;

import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;

/**
 * Data lifecycle metadata repository interface.
 */
public interface DataLifecycleMetadataRepository {

	void save(@NonNull DataLifecycleMetadata metadata) throws DataLifecycleMetadataRepositoryException;

	Optional<DataLifecycleMetadata> findByProductName(@NonNull String productName) throws DataLifecycleMetadataRepositoryException;

	List<DataLifecycleMetadata> findByProductNames(@NonNull List<String> productNames) throws DataLifecycleMetadataRepositoryException;

	/**
	 * Returns all data lifecycle metadata entries that have at least one eviction date before the given timestamp.
	 * 
	 * @param timestamp the timestamp
	 * @return all data lifecycle metadata entries that have at least one eviction date before the given timestamp.
	 * @throws DataLifecycleMetadataRepositoryException on repository error
	 */
	List<DataLifecycleMetadata> findByEvictionDateBefore(@NonNull LocalDateTime timestamp) throws DataLifecycleMetadataRepositoryException;

}
