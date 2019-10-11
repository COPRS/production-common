package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class ProductDto extends AbstractDto {

	private String keyObjectStorage;
	private String mode = null;
	private OQCFlag ocqFlag = OQCFlag.NOT_CHECKED;

	public ProductDto() {
		super();
	}

	public ProductDto(String productName, String keyObjectStorage, ProductFamily family) {
		this(productName, keyObjectStorage, family, null);
	}

	public ProductDto(String productName, String keyObjectStorage, ProductFamily family, String mode) {
		super(productName, family);
		this.keyObjectStorage = keyObjectStorage;
		this.mode = mode;
	}

	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public OQCFlag getOcqFlag() {
		return ocqFlag;
	}

	public void setOcqFlag(OQCFlag ocqFlag) {
		this.ocqFlag = ocqFlag;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"{productName: %s, keyObjectStorage: %s, family: %s, mode: %s, oqcFlag: %s, hostname: %s, creationDate: %s}",
				getProductName(), keyObjectStorage, getFamily(), mode, ocqFlag.toString(), getHostname(),
				getCreationDate());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getProductName(), keyObjectStorage, getFamily(), mode, ocqFlag, getHostname(),
				getCreationDate());
	}

	/**
	 * @see java.lang.Object#equals()
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			ProductDto other = (ProductDto) obj;
			// field comparison
			ret = Objects.equals(getProductName(), other.getProductName())
					&& Objects.equals(keyObjectStorage, other.keyObjectStorage)
					&& Objects.equals(getFamily(), other.getFamily()) 
					&& Objects.equals(mode, other.mode)
					&& Objects.equals(ocqFlag, other.ocqFlag) 
					&& Objects.equals(getHostname(), other.getHostname())
					&& Objects.equals(getCreationDate(), other.getCreationDate());
		}
		return ret;
	}

}
