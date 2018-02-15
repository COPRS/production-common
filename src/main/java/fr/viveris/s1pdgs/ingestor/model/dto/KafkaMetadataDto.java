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
	 * file descriptor of the metadata to index
	 */
	private String metadataToIndex;
	
	/**
	 * Family type of the metadata to index (RAW/SESSION/AUX/MPL)
	 */
	private String familyType;

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
	public KafkaMetadataDto(String action, String metadataToIndex, String familyType) {
		this.action = action;
		this.metadataToIndex = metadataToIndex;
		this.familyType = familyType;
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

	public String getMetadataToIndex() {
		return metadataToIndex;
	}

	public void setMetadataToIndex(String metadataToIndex) {
		this.metadataToIndex = metadataToIndex;
	}

	public String getFamilyType() {
		return familyType;
	}

	public void setFamilyType(String familyType) {
		this.familyType = familyType;
	}

	/**
	 * String formatting (JSON format)
	 */
	public String toString() {
		String info = String.format("{'action': %s, 'metadataToIndex': %, 'metadata': %s}", action, metadataToIndex, familyType);
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
		return Objects.equals(action, meta.getAction()) && Objects.equals(metadataToIndex, meta.getMetadataToIndex());
	}

	@Override
	public int hashCode() {
		return Objects.hash(action, metadataToIndex);
	}
}
