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
	private String productName;
	
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
	public KafkaConfigFileDto(String productName) {
		this.productName = productName;
	}
	

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}


	/**
	 * String formatting (JSON format)
	 */
	public String toString() {
		String info = String.format("{'productName': %s}", productName);
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
		return Objects.equals(productName, meta.getProductName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(productName);
	}
}
