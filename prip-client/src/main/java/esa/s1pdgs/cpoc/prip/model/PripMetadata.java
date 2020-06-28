package esa.s1pdgs.cpoc.prip.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class PripMetadata {

	public static final String DEFAULT_CONTENTTYPE = "application/octet-stream";
	public static final int DEFAULT_EVICTION_DAYS = 7;

	public enum FIELD_NAMES {
		ID("id", PripMetadata::getId),
		OBS_KEY("obsKey", PripMetadata::getObsKey),
		NAME("name", PripMetadata::getName),
		PRODUCT_FAMILY("productFamily", m -> m.getProductFamily() != null ? m.getProductFamily().name() : null),
		CONTENT_TYPE("contentType", PripMetadata::getContentType),
		CONTENT_LENGTH("contentLength", PripMetadata::getContentLength),
		CONTENT_DATE_START("contentDateStart", m -> (m.getContentDateStart() != null) ? DateUtils.formatToOdataDateTimeFormat(m.getContentDateStart()) : null),
		CONTENT_DATE_END("contentDateEnd", m -> (m.getContentDateEnd() != null) ? DateUtils.formatToOdataDateTimeFormat(m.getContentDateEnd()) : null),
		CREATION_DATE("creationDate",
				m -> (m.getCreationDate() != null) ? DateUtils.formatToOdataDateTimeFormat(m.getCreationDate()) : null),
		EVICTION_DATE("evictionDate",
				m -> (m.getEvictionDate() == null) ? null : DateUtils.formatToOdataDateTimeFormat(m.getEvictionDate())),
		CHECKSUM("checksum", PripMetadata::getChecksums),
		PRODUCTION_TYPE("productionType", PripMetadata::getProductionType);

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
	
	private ProductionType productionType;

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

	public ProductionType getProductionType() {
		return productionType;
	}

	public void setProductionType(ProductionType productionType) {
		this.productionType = productionType;
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
		return Objects.hash(checksums, contentDateEnd, contentDateStart, contentLength, contentType, creationDate,
				evictionDate, id, name, obsKey, productFamily, productionType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PripMetadata))
			return false;
		PripMetadata other = (PripMetadata) obj;
		return Objects.equals(checksums, other.checksums) && Objects.equals(contentDateEnd, other.contentDateEnd)
				&& Objects.equals(contentDateStart, other.contentDateStart) && contentLength == other.contentLength
				&& Objects.equals(contentType, other.contentType) && Objects.equals(creationDate, other.creationDate)
				&& Objects.equals(evictionDate, other.evictionDate) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name) && Objects.equals(obsKey, other.obsKey)
				&& productFamily == other.productFamily && productionType == other.productionType;
	}

}
