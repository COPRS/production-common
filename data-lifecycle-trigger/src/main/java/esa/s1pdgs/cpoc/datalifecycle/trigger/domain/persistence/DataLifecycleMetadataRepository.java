
package esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence;

import java.util.Optional;

import org.springframework.lang.NonNull;

import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;

/**
 * Data lifecycle metadata repository interface.
 */
public interface DataLifecycleMetadataRepository {

	void save(@NonNull DataLifecycleMetadata metadata) throws DataLifecycleMetadataRepositoryException;

	Optional<DataLifecycleMetadata> findByProductName(@NonNull String name) throws DataLifecycleMetadataRepositoryException;

}
