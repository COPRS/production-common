package fr.viveris.s1pdgs.ingestor.model.dto;

import java.util.Objects;

/**
 * DTO object for publishing in topic "metadata"
 * @author Cyrielle Gailliard
 *
 */
public class KafkaMetadataDto {
	/**
	 * Metadata action (CRUD)
	 */
	private String action;
	/**
	 * Metadata to publish in JSON format
	 */
	private String metadata;

	/**
	 * Default constructor
	 */
	public KafkaMetadataDto() {
	}

	/**
	 * Constructor from all fields
	 * @param action
	 * @param metadata
	 */
	public KafkaMetadataDto(String action, String metadata) {
		this.action = action;
		this.metadata = metadata;
	}
	

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the metadata
	 */
	public String getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata the metadata to set
	 */
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	/**
	 * String formatting (JSON format)
	 */
	public String toString() {
		String info = String.format("{'action': %s, 'metadata': %s}", action, metadata);
		return info;
	}

	@Override
	public boolean equals(Object o) {
		// self check
		if (this == o)
			return true;
		// null check
		if (o == null)
			return false;
		// type check and cast
		if (getClass() != o.getClass())
			return false;
		KafkaMetadataDto meta = (KafkaMetadataDto) o;
		// field comparison
		return Objects.equals(action, meta.getAction()) && Objects.equals(metadata, meta.getMetadata());
	}

	@Override
	public int hashCode() {
		return Objects.hash(action, metadata);
	}
}
