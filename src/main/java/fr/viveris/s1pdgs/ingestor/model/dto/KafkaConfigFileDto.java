package fr.viveris.s1pdgs.ingestor.model.dto;

import java.util.Objects;


/**
 * DTO object for publishing in topic "metadata"
 * @author Cyrielle Gailliard
 *
 */
public class KafkaConfigFileDto {
	/**
	 * ObjectKeyStore of the metadata to index
	 */
	private String metadataToIndex;
	
	/**
	 * Default constructor
	 */
	public KafkaConfigFileDto() {
	}

	/**
	 * Constructor from all fields
	 * @param action
	 * @param metadata
	 */
	public KafkaConfigFileDto(String metadataToIndex) {
		this.metadataToIndex = metadataToIndex;
	}
	

	public String getMetadataToIndex() {
		return metadataToIndex;
	}

	public void setMetadataToIndex(String metadataToIndex) {
		this.metadataToIndex = metadataToIndex;
	}


	/**
	 * String formatting (JSON format)
	 */
	public String toString() {
		String info = String.format("{'metadataToIndex': %s}", metadataToIndex);
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
		KafkaConfigFileDto meta = (KafkaConfigFileDto) o;
		// field comparison
		return Objects.equals(metadataToIndex, meta.getMetadataToIndex());
	}

	@Override
	public int hashCode() {
		return Objects.hash(metadataToIndex);
	}
}
