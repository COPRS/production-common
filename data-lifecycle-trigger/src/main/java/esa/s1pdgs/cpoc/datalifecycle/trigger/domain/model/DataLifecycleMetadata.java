
package esa.s1pdgs.cpoc.datalifecycle.trigger.domain.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.utils.DateUtils;

/**
 * Data lifecycle metadata class.
 */
public class DataLifecycleMetadata {

	public static enum FIELD_NAME {
		PRODUCT_NAME("ProductName", DataLifecycleMetadata::getProductName), //
		PATH_IN_UNCOMPRESSED_STORAGE("PathInUncompressedStorage", DataLifecycleMetadata::getPathInUncompressedStorage), //
		PATH_IN_COMPRESSED_STORAGE("PathInCompressedStorage", DataLifecycleMetadata::getPathInCompressedStorage), //
		EVICTION_DATE_IN_UNCOMPRESSED_STORAGE("EvictionDateInUncompressedStorage",
				DataLifecycleMetadata::getEvictionDateInUncompressedStorageAsString), //
		EVICTION_DATE_IN_COMPRESSED_STORAGE("EvictionDateInCompressedStorage",
				DataLifecycleMetadata::getEvictionDateInCompressedStorageAsString), //
		PERSISTENT_IN_UNCOMPRESSED_STORAGE("PersistentInUncompressedStorage",
				DataLifecycleMetadata::getPersistentInUncompressedStorage), //
		PERSISTENT_IN_COMPRESSED_STORAGE("PersistentInCompressedStorage",
				DataLifecycleMetadata::getPersistentInCompressedStorage), //
		AVAILABLE_IN_LTA("AvailableInLta", DataLifecycleMetadata::getAvailableInLta),
		LAST_MODIFIED("LastModified", DataLifecycleMetadata::getLastModifiedAsString); //

		private final String fieldName;
		private final Function<DataLifecycleMetadata, Object> toJsonAccessor;

		private FIELD_NAME(String fieldName, Function<DataLifecycleMetadata, Object> toJsonAccessor) {
			this.fieldName = fieldName;
			this.toJsonAccessor = toJsonAccessor;
		}

		public String fieldName() {
			return this.fieldName;
		}
		
		public Function<DataLifecycleMetadata, Object> toJsonAccessor() {
			return this.toJsonAccessor;
		}

		public static FIELD_NAME fromString(String fieldName) {
			Objects.requireNonNull(fieldName, "field name must not be null!");

			if (PRODUCT_NAME.fieldName.equals(fieldName)) {
				return PRODUCT_NAME;
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
			
			throw new IllegalArgumentException(String.format("field name not supported: %s", fieldName));
		}
	}

	// --------------------------------------------------------------------------

	private String productName;

	private String pathInUncompressedStorage;
	
	private String pathInCompressedStorage;
	
	private LocalDateTime evictionDateInUncompressedStorage;
	
	private LocalDateTime evictionDateInCompressedStorage;
	
	private Boolean persistentInUncompressedStorage;
	
	private Boolean persistentInCompressedStorage;
	
	private Boolean availableInLta;
	
	private LocalDateTime lastModified;
	
	// --------------------------------------------------------------------------

	public DataLifecycleMetadata() {
	}

	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return Objects.hash(this.productName, this.pathInUncompressedStorage, this.pathInCompressedStorage,
				this.evictionDateInUncompressedStorage, this.evictionDateInCompressedStorage,
				this.persistentInUncompressedStorage, this.persistentInCompressedStorage, this.availableInLta,
				this.lastModified);
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
				&& Objects.equals(this.pathInUncompressedStorage, other.pathInUncompressedStorage)
				&& Objects.equals(this.pathInCompressedStorage, other.pathInCompressedStorage)
				&& Objects.equals(this.evictionDateInUncompressedStorage, other.evictionDateInUncompressedStorage)
				&& Objects.equals(this.evictionDateInCompressedStorage, other.evictionDateInCompressedStorage)
				&& Objects.equals(this.persistentInUncompressedStorage, other.persistentInUncompressedStorage)
				&& Objects.equals(this.persistentInCompressedStorage, other.persistentInCompressedStorage)
				&& Objects.equals(this.availableInLta, other.availableInLta)
				&& Objects.equals(this.lastModified, other.lastModified);
	}
	
	@Override
	public String toString() {
		return this.toJson().toString();
	}
	
	// --------------------------------------------------------------------------
	
	public JSONObject toJson() {
		final JSONObject json = new JSONObject();

		Arrays.stream(FIELD_NAME.values()).forEach(field -> {
			final Object value = field.toJsonAccessor().apply(this);
			json.put(field.fieldName(), null != value ? value : JSONObject.NULL);
		});

		return json;
	}

	// --------------------------------------------------------------------------
	
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
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
	
}
