package esa.s1pdgs.cpoc.prip.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
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
		PRODUCTION_TYPE("productionType", PripMetadata::getProductionType),
		FOOTPRINT("footprint", PripMetadata::getFootprint);

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
		
		public static FIELD_NAMES fromString(String fieldName) {
			if (CREATION_DATE.fieldName().equalsIgnoreCase(fieldName) || CREATION_DATE.name().equalsIgnoreCase(fieldName)) {
				return CREATION_DATE;
			}
			if (EVICTION_DATE.fieldName().equalsIgnoreCase(fieldName) || EVICTION_DATE.name().equalsIgnoreCase(fieldName)) {
				return EVICTION_DATE;
			}
			if ("ContentDate/Start".equalsIgnoreCase(fieldName) || CONTENT_DATE_START.fieldName().equalsIgnoreCase(fieldName) || CONTENT_DATE_START.name().equalsIgnoreCase(fieldName)) {
				return CONTENT_DATE_START;
			}
			if ("ContentDate/End".equalsIgnoreCase(fieldName) || CONTENT_DATE_END.fieldName().equalsIgnoreCase(fieldName) || CONTENT_DATE_END.name().equalsIgnoreCase(fieldName)) {
				return CONTENT_DATE_END;
			}
			if (NAME.fieldName().equalsIgnoreCase(fieldName) || NAME.name().equalsIgnoreCase(fieldName)) {
				return NAME;
			}
			if (CONTENT_LENGTH.fieldName().equalsIgnoreCase(fieldName) || CONTENT_LENGTH.name().equalsIgnoreCase(fieldName)) {
				return CONTENT_LENGTH;
			}
			if (PRODUCT_FAMILY.fieldName().equalsIgnoreCase(fieldName) || PRODUCT_FAMILY.name().equalsIgnoreCase(fieldName)) {
				return PRODUCT_FAMILY;
			}
			if (PRODUCTION_TYPE.fieldName().equalsIgnoreCase(fieldName) || PRODUCTION_TYPE.name().equalsIgnoreCase(fieldName)) {
				return PRODUCTION_TYPE;
			}
			if (FOOTPRINT.fieldName().equalsIgnoreCase(fieldName) || FOOTPRINT.name().equalsIgnoreCase(fieldName)) {
				return FOOTPRINT;
			}
			if (CHECKSUM.fieldName().equalsIgnoreCase(fieldName) || CHECKSUM.name().equalsIgnoreCase(fieldName)) {
				return CHECKSUM;
			}
			if (CONTENT_TYPE.fieldName().equalsIgnoreCase(fieldName) || CONTENT_TYPE.name().equalsIgnoreCase(fieldName)) {
				return CONTENT_TYPE;
			}
			if (ID.fieldName().equalsIgnoreCase(fieldName) || ID.name().equalsIgnoreCase(fieldName)) {
				return ID;
			}
			if (OBS_KEY.fieldName().equalsIgnoreCase(fieldName) || OBS_KEY.name().equalsIgnoreCase(fieldName)) {
				return OBS_KEY;
			}

			throw new IllegalArgumentException(String.format("field name not supported: %s", fieldName));
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
	
	private GeoShapePolygon footprint;
	
	private Map<String, Object> attributes;

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
	
	public GeoShapePolygon getFootprint() {
		return footprint;
	}

	public void setFootprint(GeoShapePolygon footprint) {
		this.footprint = footprint;
	}
	
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public JSONObject toJson() {
		final JSONObject json = new JSONObject();

		Arrays.stream(FIELD_NAMES.values()).forEach(field -> {
			// FIXME: Find a generic solution to support both String and JSON sub elements
			if (field.fieldName.equals(FIELD_NAMES.FOOTPRINT.fieldName)) {
				if (null != getFootprint()) {
					GeoShapePolygon footprint = (GeoShapePolygon)field.toJsonAccessor().apply(this);
					json.put(field.fieldName(), footprint.toJson());
				} else {
					json.put(field.fieldName(), (JSONObject)null);
				}
			} else if (FIELD_NAMES.CHECKSUM.fieldName.equals(field.fieldName)) {
				final JSONArray checksumArray = new JSONArray();
				CollectionUtil.nullToEmpty(this.checksums).forEach(checksum -> checksumArray.put(checksum.toJson()));
				json.put(field.fieldName(), checksumArray);
			} else {
				json.put(field.fieldName(), field.toJsonAccessor().apply(this));
			}
		});
		
		if (null != attributes) {
			for (Entry<String, Object> attribute : attributes.entrySet()) {
				String name = attribute.getKey();
				Object value = attribute.getValue();				
				if (value instanceof LocalDateTime) {
					json.put(name, DateUtils.formatToOdataDateTimeFormat((LocalDateTime)value));					
				} else {
					json.put(name, value);
				}
			}
		}

		return json;
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(checksums, contentDateEnd, contentDateStart, contentLength, contentType, creationDate,
				evictionDate, id, name, obsKey, productFamily, productionType, footprint, attributes);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PripMetadata))
			return false;
		
		final PripMetadata other = (PripMetadata) obj;
		return Objects.equals(checksums, other.checksums) && Objects.equals(contentDateEnd, other.contentDateEnd)
				&& Objects.equals(contentDateStart, other.contentDateStart) && contentLength == other.contentLength
				&& Objects.equals(contentType, other.contentType) && Objects.equals(creationDate, other.creationDate)
				&& Objects.equals(evictionDate, other.evictionDate) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name) && Objects.equals(obsKey, other.obsKey)
				&& productFamily == other.productFamily && productionType == other.productionType
				&& Objects.equals(footprint, other.footprint)
				&& Objects.equals(attributes, other.attributes);
	}

}
