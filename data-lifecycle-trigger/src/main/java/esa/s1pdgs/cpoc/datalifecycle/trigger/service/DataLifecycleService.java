package esa.s1pdgs.cpoc.datalifecycle.trigger.service;

import java.time.LocalDateTime;

import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;

public interface DataLifecycleService {

	/**
	 * Triggers the deletion of all products from obs which have an eviction date in the past.
	 *
	 * @param operatorName the name of the initiating operator
	 */
	void evict(String operatorName);

	/**
	 * Triggers the deletion of the product with the given product name from obs if it has an eviction date in the past or when {@code forceCompressed} and/or {@code forceUncompressed} are {@code true}.
	 *
	 * @param productname       the name of the product to evict, not {@code null}
	 * @param forceCompressed   {@code true} to force eviction ignoring the eviction date for the product in compressed storage, {@code false} to evict only when eviction date is in the past
	 * @param forceUncompressed {@code true} to force eviction ignoring the eviction date for the product in uncompressed storage, {@code false} to evict only when eviction date is in the past
	 * @param operatorName      the name of the initiating operator
	 */
	void evict(String productname, boolean forceCompressed, boolean forceUncompressed, String operatorName);

	/**
	 * Updates the retention behaviour of the product with the given product name by setting the given eviction dates.
	 *
	 * @param productname                       the name of the product to update retention, not {@code null}
	 * @param evictionTimeInCompressedStorage   UTC timestamp to set as new eviction date for the given product in compressed storage, if {@code null} will keep current eviction date
	 * @param evictionTimeInUncompressedStorage UTC timestamp to set as new eviction date for the given product in compressed storage, if {@code null} will keep current eviction date
	 * @param operatorName                      the name of the initiating operator
	 * @return the data lifecycle metadata for the updated product
	 */
	DataLifecycleMetadata updateRetention(String productname, LocalDateTime evictionTimeInCompressedStorage,
			LocalDateTime evictionTimeInUncompressedStorage, String operatorName);

	/**
	 * Ensures the product with the given product name is available for the next {@code hoursToReserve} hours. When necessary the products eviction date is updated. If the product is not available a data
	 * request will be triggered to bring it back.
	 *
	 * @param productname    the name of the product to reserve, not {@code null}
	 * @param hoursToReserve the hours to reserve the product (from now)
	 * @param operatorName   the name of the initiating operator
	 * @return the data lifecycle metadata for the product, or {@code null} when product not available (a data request will be triggered to bring the product back)
	 */
	DataLifecycleMetadata reserve(String productname, int hoursToReserve, String operatorName);

}
