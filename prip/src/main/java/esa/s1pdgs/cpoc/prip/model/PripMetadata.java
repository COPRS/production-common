package esa.s1pdgs.cpoc.prip.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class PripMetadata {

	public static final String DEFAULT_CONTENTTYPE = "file/zip";
	public static final int DEFAULT_EVICTION_DAYS = 7;


	public enum FIELD_NAMES {
		ID("id"), OBS_KEY("obsKey"), NAME("name"), CONTENT_TYPE("contentType"), CONTENT_LENGTH("contentLength"),
		CREATION_DATE("creationDate"), EVICTION_DATE("evictionDate"), CHECKSUM("checksum");

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
				"{\"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":\"%s\", \"%s\":%s}",
				FIELD_NAMES.ID.fieldName(), id, 
				FIELD_NAMES.OBS_KEY.fieldName(), obsKey, 
				FIELD_NAMES.NAME.fieldName(), name, 
				FIELD_NAMES.CONTENT_TYPE.fieldName(), contentType,
				FIELD_NAMES.CONTENT_LENGTH.fieldName(), contentLength,
				FIELD_NAMES.CREATION_DATE.fieldName(), DateUtils.formatToMetadataDateTimeFormat(creationDate),
				FIELD_NAMES.EVICTION_DATE.fieldName(), DateUtils.formatToMetadataDateTimeFormat(evictionDate), 
				FIELD_NAMES.CHECKSUM.fieldName(), checksums);
	}

}
