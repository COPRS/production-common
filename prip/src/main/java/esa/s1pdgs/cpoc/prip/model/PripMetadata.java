package esa.s1pdgs.cpoc.prip.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class PripMetadata {

	public static final String DEFAULT_CONTENTTYPE = "file/zip";
	public static final int DEFAULT_EVICTION_DAYS = 7;

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
				"{\"id\": \"%s\", \"obsKey\": \"%s\", \"name\": \"%s\", \"contentType\": \"%s\", \"contentLength\": \"%s\", \"creationDate\": \"%s\", \"evictionDate\": \"%s\", \"checksum\": %s}",
				id, obsKey, name, contentType, contentLength, DateUtils.formatToMetadataDateTimeFormat(creationDate),
				DateUtils.formatToMetadataDateTimeFormat(evictionDate), checksums);
	}

}
