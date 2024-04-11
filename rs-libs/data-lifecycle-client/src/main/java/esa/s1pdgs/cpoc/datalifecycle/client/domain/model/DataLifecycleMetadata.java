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

package esa.s1pdgs.cpoc.datalifecycle.client.domain.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.google.gson.Gson;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;

/**
 * Data lifecycle metadata class.
 */
public class DataLifecycleMetadata {
	
	public static final Gson GSON = new Gson().newBuilder().serializeNulls().create();

	public static enum FIELD_NAME {
		PRODUCT_NAME("ProductName", FIELD_TYPE.TEXT, DataLifecycleMetadata::getProductName), //
		PRODUCT_FAMILY_IN_UNCOMPRESSED_STORAGE("ProductFamilyInUncompressedStorage", FIELD_TYPE.TEXT,
				DataLifecycleMetadata::getProductFamilyInUncompressedStorageAsString), //
		PRODUCT_FAMILY_IN_COMPRESSED_STORAGE("ProductFamilyInCompressedStorage", FIELD_TYPE.TEXT,
				DataLifecycleMetadata::getProductFamilyInCompressedStorageAsString), //
		PATH_IN_UNCOMPRESSED_STORAGE("PathInUncompressedStorage", FIELD_TYPE.TEXT, //
				DataLifecycleMetadata::getPathInUncompressedStorage),
		PATH_IN_COMPRESSED_STORAGE("PathInCompressedStorage", FIELD_TYPE.TEXT, //
				DataLifecycleMetadata::getPathInCompressedStorage),
		EVICTION_DATE_IN_UNCOMPRESSED_STORAGE("EvictionDateInUncompressedStorage", FIELD_TYPE.DATE,
				DataLifecycleMetadata::getEvictionDateInUncompressedStorageAsString), //
		EVICTION_DATE_IN_COMPRESSED_STORAGE("EvictionDateInCompressedStorage", FIELD_TYPE.DATE,
				DataLifecycleMetadata::getEvictionDateInCompressedStorageAsString), //
		LAST_INSERTION_IN_UNCOMPRESSED_STORAGE("LastInsertionInUncompressedStorage", FIELD_TYPE.DATE, //
				DataLifecycleMetadata::getLastInsertionInUncompressedStorageAsString),
		LAST_INSERTION_IN_COMPRESSED_STORAGE("LastInsertionInCompressedStorage", FIELD_TYPE.DATE, //
				DataLifecycleMetadata::getLastInsertionInCompressedStorageAsString),
		PERSISTENT_IN_UNCOMPRESSED_STORAGE("PersistentInUncompressedStorage", FIELD_TYPE.BOOLEAN,
				DataLifecycleMetadata::getPersistentInUncompressedStorage), //
		PERSISTENT_IN_COMPRESSED_STORAGE("PersistentInCompressedStorage", FIELD_TYPE.BOOLEAN,
				DataLifecycleMetadata::getPersistentInCompressedStorage), //
		AVAILABLE_IN_LTA("AvailableInLta", FIELD_TYPE.BOOLEAN, //
				DataLifecycleMetadata::getAvailableInLta),
		LAST_MODIFIED("LastModified", FIELD_TYPE.DATE, //
				DataLifecycleMetadata::getLastModifiedAsString),
		LAST_DATA_REQUEST("LastDataRequest", FIELD_TYPE.DATE, //
				DataLifecycleMetadata::getLastDataRequestAsString);

		private final String fieldName;
		private final FIELD_TYPE fieldType;
		private final Function<DataLifecycleMetadata, Object> toJsonAccessor;

		private FIELD_NAME(String fieldName, FIELD_TYPE fieldType, Function<DataLifecycleMetadata, Object> toJsonAccessor) {
			this.fieldName = fieldName;
			this.fieldType = fieldType;
			this.toJsonAccessor = toJsonAccessor;
		}

		public String fieldName() {
			return this.fieldName;
		}

		public FIELD_TYPE fieldType() {
			return this.fieldType;
		}

		public Function<DataLifecycleMetadata, Object> toJsonAccessor() {
			return this.toJsonAccessor;
		}

		public static FIELD_NAME fromString(String fieldName) {
			Objects.requireNonNull(fieldName, "field name must not be null!");

			if (PRODUCT_NAME.fieldName.equals(fieldName)) {
				return PRODUCT_NAME;
			}
			if (PRODUCT_FAMILY_IN_UNCOMPRESSED_STORAGE.fieldName.equals(fieldName)) {
				return PRODUCT_FAMILY_IN_UNCOMPRESSED_STORAGE;
			}
			if (PRODUCT_FAMILY_IN_COMPRESSED_STORAGE.fieldName.equals(fieldName)) {
				return PRODUCT_FAMILY_IN_COMPRESSED_STORAGE;
			}
			if (PATH_IN_UNCOMPRESSED_STORAGE.fieldName.equals(fieldName)) {
				return PATH_IN_UNCOMPRESSED_STORAGE;
			}
			if (PATH_IN_COMPRESSED_STORAGE.fieldName.equals(fieldName)) {
				return PATH_IN_COMPRESSED_STORAGE;
			}
			if (EVICTION_DATE_IN_UNCOMPRESSED_STORAGE.fieldName.equals(fieldName)) {
				return EVICTION_DATE_IN_UNCOMPRESSED_STORAGE;
			}
			if (EVICTION_DATE_IN_COMPRESSED_STORAGE.fieldName.equals(fieldName)) {
				return EVICTION_DATE_IN_COMPRESSED_STORAGE;
			}
			if (LAST_INSERTION_IN_UNCOMPRESSED_STORAGE.fieldName.equals(fieldName)) {
				return LAST_INSERTION_IN_UNCOMPRESSED_STORAGE;
			}
			if (LAST_INSERTION_IN_COMPRESSED_STORAGE.fieldName.equals(fieldName)) {
				return LAST_INSERTION_IN_COMPRESSED_STORAGE;
			}
			if (PERSISTENT_IN_UNCOMPRESSED_STORAGE.fieldName.equals(fieldName)) {
				return PERSISTENT_IN_UNCOMPRESSED_STORAGE;
			}
			if (PERSISTENT_IN_COMPRESSED_STORAGE.fieldName.equals(fieldName)) {
				return PERSISTENT_IN_COMPRESSED_STORAGE;
			}
			if (AVAILABLE_IN_LTA.fieldName.equals(fieldName)) {
				return AVAILABLE_IN_LTA;
			}
			if (LAST_MODIFIED.fieldName.equals(fieldName)) {
				return LAST_MODIFIED;
			}
			if (LAST_DATA_REQUEST.fieldName.equals(fieldName)) {
				return LAST_DATA_REQUEST;
			}
			
			throw new IllegalArgumentException(String.format("field name not supported: %s", fieldName));
		}
	}

	public static enum FIELD_TYPE {
		TEXT, DATE, NUMBER, BOOLEAN;
	}

	// --------------------------------------------------------------------------

	private String productName;

	private ProductFamily productFamilyInUncompressedStorage;

	private ProductFamily productFamilyInCompressedStorage;

	private String pathInUncompressedStorage;
	
	private String pathInCompressedStorage;
	
	private LocalDateTime evictionDateInUncompressedStorage;
	
	private LocalDateTime evictionDateInCompressedStorage;
	
	private Boolean persistentInUncompressedStorage;
	
	private Boolean persistentInCompressedStorage;

	private LocalDateTime lastInsertionInUncompressedStorage; // the time when the path was inserted in the data lifecycle index (not in OBS)

	private LocalDateTime lastInsertionInCompressedStorage; // the time when the path was inserted in the data lifecycle index (not in OBS)

	private Boolean availableInLta;
	
	private LocalDateTime lastModified;
	
	private LocalDateTime lastDataRequest;
	
	// --------------------------------------------------------------------------

	public DataLifecycleMetadata() {
	}

	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return Objects.hash(this.productName, this.productFamilyInUncompressedStorage, this.productFamilyInCompressedStorage, this.pathInUncompressedStorage,
				this.pathInCompressedStorage, this.evictionDateInUncompressedStorage, this.evictionDateInCompressedStorage,
				this.lastInsertionInUncompressedStorage, this.lastInsertionInCompressedStorage, this.persistentInUncompressedStorage,
				this.persistentInCompressedStorage, this.availableInLta, this.lastModified, this.lastDataRequest);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}

		final DataLifecycleMetadata other = (DataLifecycleMetadata) obj;
		return Objects.equals(this.productName, other.productName)
				&& Objects.equals(this.productFamilyInUncompressedStorage, other.productFamilyInUncompressedStorage)
				&& Objects.equals(this.productFamilyInCompressedStorage, other.productFamilyInCompressedStorage)
				&& Objects.equals(this.pathInUncompressedStorage, other.pathInUncompressedStorage)
				&& Objects.equals(this.pathInCompressedStorage, other.pathInCompressedStorage)
				&& Objects.equals(this.evictionDateInUncompressedStorage, other.evictionDateInUncompressedStorage)
				&& Objects.equals(this.evictionDateInCompressedStorage, other.evictionDateInCompressedStorage)
				&& Objects.equals(this.lastInsertionInUncompressedStorage, other.lastInsertionInUncompressedStorage)
				&& Objects.equals(this.lastInsertionInCompressedStorage, other.lastInsertionInCompressedStorage)
				&& Objects.equals(this.persistentInUncompressedStorage, other.persistentInUncompressedStorage)
				&& Objects.equals(this.persistentInCompressedStorage, other.persistentInCompressedStorage)
				&& Objects.equals(this.availableInLta, other.availableInLta)
				&& Objects.equals(this.lastModified, other.lastModified)
				&& Objects.equals(this.getLastDataRequest(), other.getLastDataRequest());
	}
	
	@Override
	public String toString() {
		return toJson();
	}
	
	// --------------------------------------------------------------------------
	
	public String toJson() {
		Map<String, Object> map = new HashMap<>();
		Arrays.stream(FIELD_NAME.values()).forEach(field -> {
			final Object value = field.toJsonAccessor().apply(this);
			map.put(field.fieldName(), value);
		});

		return GSON.toJson(map);
	}

	// --------------------------------------------------------------------------
	
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public ProductFamily getProductFamilyInUncompressedStorage() {
		return this.productFamilyInUncompressedStorage;
	}

	public String getProductFamilyInUncompressedStorageAsString() {
		return (null != this.productFamilyInUncompressedStorage) ? this.productFamilyInUncompressedStorage.name() : null;
	}

	public void setProductFamilyInUncompressedStorage(ProductFamily productFamilyInUncompressedStorage) {
		this.productFamilyInUncompressedStorage = productFamilyInUncompressedStorage;
	}

	public ProductFamily getProductFamilyInCompressedStorage() {
		return this.productFamilyInCompressedStorage;
	}

	public String getProductFamilyInCompressedStorageAsString() {
		return (null != this.productFamilyInCompressedStorage) ? this.productFamilyInCompressedStorage.name() : null;
	}

	public void setProductFamilyInCompressedStorage(ProductFamily productFamilyInCompressedStorage) {
		this.productFamilyInCompressedStorage = productFamilyInCompressedStorage;
	}

	public String getPathInUncompressedStorage() {
		return pathInUncompressedStorage;
	}

	public void setPathInUncompressedStorage(String pathInUncompressedStorage) {
		this.pathInUncompressedStorage = pathInUncompressedStorage;
	}

	public String getPathInCompressedStorage() {
		return pathInCompressedStorage;
	}

	public void setPathInCompressedStorage(String pathInCompressedStorage) {
		this.pathInCompressedStorage = pathInCompressedStorage;
	}

	public LocalDateTime getEvictionDateInUncompressedStorage() {
		return evictionDateInUncompressedStorage;
	}
	
	public String getEvictionDateInUncompressedStorageAsString() {
		return (null != this.evictionDateInUncompressedStorage)
				? DateUtils.formatToMetadataDateTimeFormat(this.evictionDateInUncompressedStorage)
				: null;
	}

	public void setEvictionDateInUncompressedStorage(LocalDateTime evictionDateInUncompressedStorage) {
		this.evictionDateInUncompressedStorage = evictionDateInUncompressedStorage;
	}

	public LocalDateTime getEvictionDateInCompressedStorage() {
		return evictionDateInCompressedStorage;
	}
	
	public String getEvictionDateInCompressedStorageAsString() {
		return (null != this.evictionDateInCompressedStorage)
				? DateUtils.formatToMetadataDateTimeFormat(this.evictionDateInCompressedStorage)
				: null;
	}

	public void setEvictionDateInCompressedStorage(LocalDateTime evictionDateInCompressedStorage) {
		this.evictionDateInCompressedStorage = evictionDateInCompressedStorage;
	}

	public Boolean getPersistentInUncompressedStorage() {
		return persistentInUncompressedStorage;
	}

	public void setPersistentInUncompressedStorage(Boolean persistentInUncompressedStorage) {
		this.persistentInUncompressedStorage = persistentInUncompressedStorage;
	}

	public Boolean getPersistentInCompressedStorage() {
		return persistentInCompressedStorage;
	}

	public void setPersistentInCompressedStorage(Boolean persistentInCompressedStorage) {
		this.persistentInCompressedStorage = persistentInCompressedStorage;
	}

	public Boolean getAvailableInLta() {
		return availableInLta;
	}

	public void setAvailableInLta(Boolean availableInLta) {
		this.availableInLta = availableInLta;
	}

	public LocalDateTime getLastModified() {
		return this.lastModified;
	}
	
	public String getLastModifiedAsString() {
		return (null != this.lastModified)
				? DateUtils.formatToMetadataDateTimeFormat(this.lastModified)
				: null;
	}

	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}

	public LocalDateTime getLastDataRequest() {
		return this.lastDataRequest;
	}
	
	public String getLastDataRequestAsString() {
		return (null != this.lastDataRequest)
				? DateUtils.formatToMetadataDateTimeFormat(this.lastDataRequest)
				: null;
	}

	public void setLastDataRequest(LocalDateTime lastDataRequest) {
		this.lastDataRequest = lastDataRequest;
	}

	public LocalDateTime getLastInsertionInUncompressedStorage() {
		return this.lastInsertionInUncompressedStorage;
	}

	public String getLastInsertionInUncompressedStorageAsString() {
		return (null != this.lastInsertionInUncompressedStorage) ? DateUtils.formatToMetadataDateTimeFormat(this.lastInsertionInUncompressedStorage) : null;
	}

	public void setLastInsertionInUncompressedStorage(LocalDateTime lastInsertionInUncompressedStorage) {
		this.lastInsertionInUncompressedStorage = lastInsertionInUncompressedStorage;
	}

	public LocalDateTime getLastInsertionInCompressedStorage() {
		return this.lastInsertionInCompressedStorage;
	}

	public String getLastInsertionInCompressedStorageAsString() {
		return (null != this.lastInsertionInCompressedStorage) ? DateUtils.formatToMetadataDateTimeFormat(this.lastInsertionInCompressedStorage) : null;
	}

	public void setLastInsertionInCompressedStorage(LocalDateTime lastInsertionInCompressedStorage) {
		this.lastInsertionInCompressedStorage = lastInsertionInCompressedStorage;
	}

}
