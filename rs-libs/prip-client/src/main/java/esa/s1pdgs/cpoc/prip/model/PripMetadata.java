package esa.s1pdgs.cpoc.prip.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import com.google.gson.Gson;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class PripMetadata {

	public static final String DEFAULT_CONTENTTYPE = "application/octet-stream";
	public static final int DEFAULT_EVICTION_DAYS = 7;
	public static final Gson GSON = new Gson().newBuilder().serializeNulls().create();

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
		ORIGIN_DATE("originDate",
				m -> (m.getOriginDate() != null) ? DateUtils.formatToOdataDateTimeFormat(m.getOriginDate()) : null),
		CHECKSUM("checksum", PripMetadata::getChecksums),
		PRODUCTION_TYPE("productionType", PripMetadata::getProductionType),
		FOOTPRINT("footprint", PripMetadata::getFootprint),
		BROWSE_KEYS("browseKeys", PripMetadata::getBrowseKeys),
		ONLINE("online", PripMetadata::isOnline);

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
	
	private LocalDateTime originDate;

	private List<Checksum> checksums;
	
	private ProductionType productionType;
	
	private PripGeoShape footprint;
	
	private List<String> browseKeys;
	
	private boolean online = true; // RS-400: by default if not specified otherwise, products are online. see also: esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor.ProductsFilterVisitor.newPripBooleanFilter(String, Function, boolean)
	
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
	
	public LocalDateTime getOriginDate() {
		return originDate;
	}

	public void setOriginDate(LocalDateTime originDate) {
		this.originDate = originDate;
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
	
	public PripGeoShape getFootprint() {
		return footprint;
	}

	public void setFootprint(PripGeoShape footprint) {
		this.footprint = footprint;
	}
	
	public List<String> getBrowseKeys() {
		return browseKeys;
	}

	public void setBrowseKeys(List<String> browseKeys) {
		this.browseKeys = browseKeys;
	}
	
	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	public String toJson() {
		final Map<String, Object> map = new HashMap<>();

		Arrays.stream(FIELD_NAMES.values()).forEach(field -> {
			if (field.fieldName.equals(FIELD_NAMES.FOOTPRINT.fieldName)) {
				if (null != getFootprint()) {
					PripGeoShape footprint = (PripGeoShape)field.toJsonAccessor().apply(this);
					map.put(field.fieldName(), footprint.asMap());
				} else {
					map.put(field.fieldName(), null);
				}
			} else if (FIELD_NAMES.CHECKSUM.fieldName.equals(field.fieldName)) {
				final List<Map<String,Object>> checksums = new ArrayList<>();
				CollectionUtil.nullToEmpty(this.checksums).forEach(checksum -> checksums.add(checksum.asMap()));
				map.put(field.fieldName(), checksums);
			} else {
				map.put(field.fieldName(), field.toJsonAccessor().apply(this));
			}
		});
		
		if (null != attributes) {
			for (Entry<String, Object> attribute : attributes.entrySet()) {
				String name = attribute.getKey();
				Object value = attribute.getValue();				
				if (value instanceof LocalDateTime) {
					map.put(name, DateUtils.formatToOdataDateTimeFormat((LocalDateTime)value));					
				} else {
					map.put(name, value);
				}
			}
		}

		return GSON.toJson(map);
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(checksums, contentDateEnd, contentDateStart, contentLength, contentType, creationDate, originDate,
				evictionDate, id, name, obsKey, productFamily, productionType, footprint, browseKeys, online, attributes);
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
				&& Objects.equals(originDate, other.originDate)
				&& Objects.equals(name, other.name) && Objects.equals(obsKey, other.obsKey)
				&& productFamily == other.productFamily && productionType == other.productionType
				&& Objects.equals(footprint, other.footprint)
				&& Objects.equals(browseKeys, other.browseKeys)
				&& Objects.equals(online, other.online)
				&& Objects.equals(attributes, other.attributes);
	}

}
