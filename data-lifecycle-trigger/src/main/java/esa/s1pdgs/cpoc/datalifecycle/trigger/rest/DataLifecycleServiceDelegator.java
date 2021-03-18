package esa.s1pdgs.cpoc.datalifecycle.trigger.rest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model.DataLifecycleMetadata;
import esa.s1pdgs.cpoc.datalifecycle.trigger.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.datalifecycle.trigger.rest.model.Product;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.DataLifecycleMetadataNotFoundException;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.DataLifecycleService;
import esa.s1pdgs.cpoc.datalifecycle.trigger.service.DataLifecycleTriggerInternalServerErrorException;

@Component
public class DataLifecycleServiceDelegator {

	private final DataLifecycleService dataLifecycleService;

	@Autowired
	public DataLifecycleServiceDelegator(DataLifecycleService dataLifecycleService) {
		this.dataLifecycleService = dataLifecycleService;
	}

	// *************************************************************************

	/**
	 * @see DataLifecycleTriggerRestController#getProducts(String, String, boolean,
	 *      String, String, boolean, String, String, boolean, Integer, Integer)
	 */
	public List<Product> getProducts(String namePattern, boolean persistentInUncompressedStorage,
			String minimalEvictionTimeInUncompressedStorage, String maximalEvictionTimeInUncompressedStorage,
			boolean persistentIncompressedStorage, String minimalEvictionTimeInCompressedStorage,
			String maximalEvictionTimeInCompressedStorage, boolean availableInLta, Integer pageSize,
			Integer pageNumber) throws DataLifecycleMetadataRepositoryException {
		List<DataLifecycleMetadata> result = this.dataLifecycleService.getProducts(namePattern,
				persistentInUncompressedStorage, convertDateTime(minimalEvictionTimeInUncompressedStorage),
				convertDateTime(maximalEvictionTimeInUncompressedStorage), persistentIncompressedStorage,
				convertDateTime(minimalEvictionTimeInCompressedStorage),
				convertDateTime(maximalEvictionTimeInCompressedStorage), availableInLta, pageSize, pageNumber);

		List<Product> mappedResult = new ArrayList<>();
		if (result != null) {
			for (DataLifecycleMetadata dataLifecycleMetadata : result) {
				mappedResult.add(this.convertToProduct(dataLifecycleMetadata));
			}
		}
		return mappedResult;
	}

	/**
	 * @see DataLifecycleTriggerRestController#postProducts(String, ProductPostDto)
	 */
	public List<Product> getProducts(List<String> productnames) throws DataLifecycleMetadataRepositoryException {
		List<DataLifecycleMetadata> result = this.dataLifecycleService.getProducts(productnames);
		List<Product> mappedResult = new ArrayList<>();
		if (result != null) {
			for (DataLifecycleMetadata dataLifecycleMetadata : result) {
				mappedResult.add(this.convertToProduct(dataLifecycleMetadata));
			}
		}
		return mappedResult;
	}

	
	/**
	 * @throws DataLifecycleMetadataRepositoryException 
	 * @see DataLifecycleTriggerRestController#deleteProducts(String, String)
	 */
	public void deleteProducts(String operatorName) throws DataLifecycleTriggerInternalServerErrorException {
		this.dataLifecycleService.evict(operatorName);
	}

	/**
	 * @throws DataLifecycleMetadataNotFoundException
	 * @throws DataLifecycleTriggerInternalServerErrorException 
	 * @see DataLifecycleTriggerRestController#getProduct(String, String)
	 */
	public Product getProduct(String productname) throws DataLifecycleMetadataNotFoundException, DataLifecycleTriggerInternalServerErrorException {
		DataLifecycleMetadata result = this.dataLifecycleService.getProduct(productname);

		Product mappedResult = null;
		if (result != null) {
				mappedResult = this.convertToProduct(result);
		}
		return mappedResult;
	}

	/**
	 * @throws DataLifecycleMetadataNotFoundException
	 * @throws DataLifecycleTriggerInternalServerErrorException 
	 * @see DataLifecycleTriggerRestController#deleteProduct(String, String,
	 *      boolean, boolean, String)
	 */
	public void deleteProduct(String productname, boolean forceCompressed, boolean forceUncompressed,
			String operatorName) throws DataLifecycleMetadataNotFoundException, DataLifecycleTriggerInternalServerErrorException {
		this.dataLifecycleService.evict(productname, forceCompressed, forceUncompressed, operatorName);
	}

	/**
	 * @throws DataLifecycleMetadataNotFoundException 
	 * @throws DataLifecycleMetadataRepositoryException 
	 * @see DataLifecycleTriggerRestController#patchProduct(String, String, ProductPatchDto)
	 */
	public Product patchProduct(String productname, String operatorName, String evictionTimeInUncompressedStorage,
			String evictionTimeInCompressedStorage) throws DataLifecycleTriggerInternalServerErrorException, DataLifecycleMetadataNotFoundException {
		return convertToProduct(
				this.dataLifecycleService.updateRetention(productname, convertDateTime(evictionTimeInCompressedStorage),
						convertDateTime(evictionTimeInUncompressedStorage), operatorName));
	}

	// *************************************************************************
	
	private Product convertToProduct(DataLifecycleMetadata dataLifecycleMetadata) {
		if (dataLifecycleMetadata == null) {
			return null;
		} else {
			Product result = new Product();
			result.setProductname(dataLifecycleMetadata.getProductName());
			result.setPersistentInUncompressedStorage(dataLifecycleMetadata.getPersistentInUncompressedStorage());
			result.setPersistentInCompressedStorage(dataLifecycleMetadata.getPersistentInCompressedStorage());
			result.setPathInUncompressedStorage(dataLifecycleMetadata.getPathInUncompressedStorage());
			result.setPathInCompressedStorage(dataLifecycleMetadata.getPathInCompressedStorage());
			result.setLastModificationTime(dataLifecycleMetadata.getLastModifiedAsString());
			result.setEvictionTimeInUncompressedStorage(
					dataLifecycleMetadata.getEvictionDateInUncompressedStorageAsString());
			result.setEvictionTimeInCompressedStorage(
					dataLifecycleMetadata.getEvictionDateInCompressedStorageAsString());
			result.setProductFamilyInUncompressedStorage(dataLifecycleMetadata.getProductFamilyInUncompressedStorageAsString());
			result.setProductFamilyInCompressedStorage(dataLifecycleMetadata.getProductFamilyInCompressedStorageAsString());
			result.setAvailableInLta(dataLifecycleMetadata.getAvailableInLta());
			return result;
		}
	}

	public final static LocalDateTime convertDateTime(String dateTimeAsString) {
		return (null != dateTimeAsString) ? DateUtils.parse(dateTimeAsString) : null;
	}


}
