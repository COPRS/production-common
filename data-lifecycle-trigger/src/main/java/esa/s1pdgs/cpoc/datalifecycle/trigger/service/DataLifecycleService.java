package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.LocalDateTime;
import java.util.List;

import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.error.DataLifecycleMetadataNotFoundException;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.error.DataLifecycleTriggerBadRequestException;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.error.DataLifecycleTriggerInternalServerErrorException;

public interface DataLifecycleService {

	/**
	 * Triggers the deletion of all products from obs which have an eviction date in the past.
	 *
	 * @param operatorName the name of the initiating operator
	 * @throws DataLifecycleTriggerInternalServerErrorException on internal server error
	 */
	void evict(String operatorName) throws DataLifecycleTriggerInternalServerErrorException;

	/**
	 * Triggers the deletion of the product with the given product name from obs if it has an eviction date in the past or when {@code forceCompressed} and/or {@code forceUncompressed} are {@code true}.
	 *
	 * @param productname       the name of the product to evict, not {@code null}
	 * @param forceCompressed   {@code true} to force eviction ignoring the eviction date for the product in compressed storage, {@code false} to evict only when eviction date is in the past
	 * @param forceUncompressed {@code true} to force eviction ignoring the eviction date for the product in uncompressed storage, {@code false} to evict only when eviction date is in the past
	 * @param operatorName      the name of the initiating operator
	 * @throws DataLifecycleTriggerInternalServerErrorException on internal server error
	 * @throws DataLifecycleMetadataNotFoundException           when no data lifecycle metadata is found for the given product name
	 */
	void evict(String productname, boolean forceCompressed, boolean forceUncompressed, String operatorName)
			throws DataLifecycleTriggerInternalServerErrorException, DataLifecycleMetadataNotFoundException;

	/**
	 * Updates the retention behaviour of the product with the given product name by setting the given eviction dates. If the eviction date is {@code null} the product will be freezed by setting the
	 * eviction date far in the future. If the product is not available a data request will be triggered to bring the product back.
	 *
	 * @param productname                       the name of the product to update retention, not {@code null}
	 * @param evictionTimeInCompressedStorage   UTC timestamp to set as new eviction date for the given product in compressed storage, {@code null} for freeze
	 * @param evictionTimeInUncompressedStorage UTC timestamp to set as new eviction date for the given product in compressed storage, {@code null} for freeze
	 * @param operatorName                      the name of the initiating operator
	 * @return the data lifecycle metadata for the updated product
	 * @throws DataLifecycleTriggerInternalServerErrorException on internal server error
	 * @throws DataLifecycleMetadataNotFoundException           when no data lifecycle metadata is found for the given product name
	 */
	DataLifecycleMetadata updateRetention(String productname, LocalDateTime evictionTimeInCompressedStorage,
			LocalDateTime evictionTimeInUncompressedStorage, String operatorName)
					throws DataLifecycleTriggerInternalServerErrorException, DataLifecycleMetadataNotFoundException;

	List<DataLifecycleMetadata> getProducts(String namePattern, Boolean persistentInUncompressedStorage, LocalDateTime minimalEvictionTimeInUncompressedStorage,
			LocalDateTime maximalEvictionTimeInUncompressedStorage, Boolean persistentIncompressedStorage, LocalDateTime minimalEvictionTimeInCompressedStorage,
			LocalDateTime maximalEvictionTimeInCompressedStorage, Boolean availableInLta, Integer pageSize, Integer pageNumber)
					throws DataLifecycleTriggerInternalServerErrorException, DataLifecycleTriggerBadRequestException;

	List<DataLifecycleMetadata> getProducts(List<String> productnames) throws DataLifecycleTriggerInternalServerErrorException;

	DataLifecycleMetadata getProduct(String productname) throws DataLifecycleMetadataNotFoundException, DataLifecycleTriggerInternalServerErrorException;

}
