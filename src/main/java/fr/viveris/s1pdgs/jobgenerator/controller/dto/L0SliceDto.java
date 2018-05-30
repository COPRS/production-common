package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import java.util.Objects;

/**
 * DTO class for L0 slices
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L0SliceDto {
	/**
	 * AbstractProduct name of the metadata to index
	 */
	private String productName;

	/**
	 * ObjectkeyStorage of the metatdata to index
	 */
	private String keyObjectStorage;

	/**
	 * Default constructor
	 */
	public L0SliceDto() {
		super();
	}

	/**
	 * @param productName
	 * @param keyObjectStorage
	 */
	public L0SliceDto(final String productName, final String keyObjectStorage) {
		this();
		this.productName = productName;
		this.keyObjectStorage = keyObjectStorage;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName
	 *            the productName to set
	 */
	public void setProductName(final String productName) {
		this.productName = productName;
	}

	/**
	 * @return the keyObjectStorage
	 */
	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	/**
	 * @param keyObjectStorage
	 *            the keyObjectStorage to set
	 */
	public void setKeyObjectStorage(final String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	/**
	 * To string
	 */
	@Override
	public String toString() {
		return String.format("{productName: %s, keyObjectStorage: %s}", productName, keyObjectStorage);
	}

	/**
	 * Hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(keyObjectStorage, productName);
	}

	/**
	 * Equals
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			L0SliceDto other = (L0SliceDto) obj;
			ret = Objects.equals(keyObjectStorage, other.keyObjectStorage)
					&& Objects.equals(productName, other.productName);
		}
		return ret;
	}

}
