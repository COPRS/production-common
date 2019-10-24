package esa.s1pdgs.cpoc.prip.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class PripMetadata {

	public static final String DEFAULT_CONTENTTYPE = "application/zip";
	public static final int DEFAULT_EVICTION_DAYS = 7;

	public enum FIELD_NAMES {
		ID("id"), OBS_KEY("obsKey"), NAME("name"), PRODUCT_FAMILY("productFamily"), CONTENT_TYPE("contentType"),
		CONTENT_LENGTH("contentLength"), CREATION_DATE("creationDate"), EVICTION_DATE("evictionDate"),
		CHECKSUM("checksum");

		private String fieldName;

		private FIELD_NAMES(String fieldName) {
			this.fieldName = fieldName;
		}

		public String fieldName() {
			return fieldName;
		}
	}

	private UUID id;

	private String obsKey;

	private String name;

	private ProductFamily productFamily;

	private String contentType;

	private long contentLength;

	private LocalDateTime creationDate;

	private LocalDateTime evictionDate;

	private List<Checksum> checksums;

	public PripMetadata() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getObsKey() {
		return obsKey;
	}

	public void setObsKey(String obsKey) {
		this.obsKey = obsKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public LocalDateTime getEvictionDate() {
		return evictionDate;
	}

	public void setEvictionDate(LocalDateTime evictionDate) {
		this.evictionDate = evictionDate;
	}

	public List<Checksum> getChecksums() {
		return checksums;
	}

	public void setChecksums(List<Checksum> checksums) {
		this.checksums = checksums;
	}

	@Override
	public String toString() {
		return String.format(
				"{\"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":%s}",
				FIELD_NAMES.ID.fieldName(), id, FIELD_NAMES.OBS_KEY.fieldName(), obsKey, FIELD_NAMES.NAME.fieldName(),
				name, FIELD_NAMES.PRODUCT_FAMILY.fieldName(), (productFamily == null) ? null : productFamily.name(),
				FIELD_NAMES.CONTENT_TYPE.fieldName(), contentType, FIELD_NAMES.CONTENT_LENGTH.fieldName(),
				contentLength, FIELD_NAMES.CREATION_DATE.fieldName(),
				(creationDate == null) ? null : DateUtils.formatToMetadataDateTimeFormat(creationDate),
				FIELD_NAMES.EVICTION_DATE.fieldName(),
				(evictionDate == null) ? null : DateUtils.formatToMetadataDateTimeFormat(evictionDate),
				FIELD_NAMES.CHECKSUM.fieldName(), checksums);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((checksums == null) ? 0 : checksums.hashCode());
		result = prime * result + (int) (contentLength ^ (contentLength >>> 32));
		result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((evictionDate == null) ? 0 : evictionDate.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((obsKey == null) ? 0 : obsKey.hashCode());
		result = prime * result + ((productFamily == null) ? 0 : productFamily.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PripMetadata other = (PripMetadata) obj;
		if (checksums == null) {
			if (other.checksums != null)
				return false;
		} else if (!checksums.equals(other.checksums))
			return false;
		if (contentLength != other.contentLength)
			return false;
		if (contentType == null) {
			if (other.contentType != null)
				return false;
		} else if (!contentType.equals(other.contentType))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (evictionDate == null) {
			if (other.evictionDate != null)
				return false;
		} else if (!evictionDate.equals(other.evictionDate))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (obsKey == null) {
			if (other.obsKey != null)
				return false;
		} else if (!obsKey.equals(other.obsKey))
			return false;
		if (productFamily != other.productFamily)
			return false;
		return true;
	}

}
