package esa.s1pdgs.cpoc.prip.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class PripMetadata {

	public static final String DEFAULT_CONTENTTYPE = "application/zip";
	public static final int DEFAULT_EVICTION_DAYS = 7;

	public enum FIELD_NAMES {
		ID("id", PripMetadata::getId),
		OBS_KEY("obsKey", PripMetadata::getObsKey),
		NAME("name", PripMetadata::getName),
		PRODUCT_FAMILY("productFamily", m -> m.getProductFamily() != null ? m.getProductFamily().name() : null),
		CONTENT_TYPE("contentType", PripMetadata::getContentType),
		CONTENT_LENGTH("contentLength", PripMetadata::getContentLength),
		CONTENT_DATE_START("contentDateStart", PripMetadata::getContentDateStart),
		CONTENT_DATE_END("contentDateEnd", PripMetadata::getContentDateEnd),
		CREATION_DATE("creationDate",
				m -> (m.getCreationDate() != null) ? DateUtils.formatToMetadataDateTimeFormat(m.getCreationDate()) : null),
		EVICTION_DATE("evictionDate",
				m -> (m.getEvictionDate() == null) ? null : DateUtils.formatToMetadataDateTimeFormat(m.getEvictionDate())),
		CHECKSUM("checksum", PripMetadata::getChecksums);

		private final String fieldName;
		private final Function<PripMetadata, Object> toJsonAccessor;

		FIELD_NAMES(String fieldName, Function<PripMetadata, Object> toJsonAccessor) {
			this.fieldName = fieldName;
			this.toJsonAccessor = toJsonAccessor;
		}

		public Function<PripMetadata, Object> toJsonAccessor() {
			return toJsonAccessor;
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

	private LocalDateTime contentDateStart;

	private LocalDateTime contentDateEnd;

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

	public LocalDateTime getContentDateStart() { return contentDateStart; }

	public void setContentDateStart(LocalDateTime contentDateStart) { this.contentDateStart = contentDateStart;}

	public LocalDateTime getContentDateEnd() {
		return contentDateEnd;
	}

	public void setContentDateEnd(LocalDateTime contentDateEnd) {
		this.contentDateEnd = contentDateEnd;
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

	public JSONObject toJson() {
		JSONObject json = new JSONObject();

		Arrays.stream(FIELD_NAMES.values()).forEach(field -> json.put(field.fieldName(), field.toJsonAccessor().apply(this)));

		return json;
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((checksums == null) ? 0 : checksums.hashCode());
		result = prime * result + (int) (contentLength ^ (contentLength >>> 32));
		result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result * (contentDateStart == null ? 0 : contentDateStart.hashCode());
		result = prime * result * (contentDateEnd == null ? 0 : contentDateEnd.hashCode());
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
		if (contentDateStart == null) {
			if(other.contentDateStart != null)
				return false;
		} else if(!contentDateStart.equals(other.contentDateStart))
			return false;
		if (contentDateEnd == null) {
			if(other.contentDateEnd != null)
				return false;
		} else if(!contentDateEnd.equals(other.contentDateEnd))
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
		return productFamily == other.productFamily;
	}

}
