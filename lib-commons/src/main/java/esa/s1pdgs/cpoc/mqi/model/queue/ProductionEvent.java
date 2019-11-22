package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class ProductionEvent extends AbstractMessage {
	private ProductFamily productFamily;
	private String keyObjectStorage;
	private String mode = null;
	private OQCFlag oqcFlag = OQCFlag.NOT_CHECKED;

	public ProductionEvent() {
		super();
	}

	public ProductionEvent(String productName, String keyObjectStorage, ProductFamily family) {
		this(productName, keyObjectStorage, family, null);
	}

	public ProductionEvent(String productName, String keyObjectStorage, ProductFamily family, String mode) {
		this(keyObjectStorage, family, mode, OQCFlag.NOT_CHECKED);
	}
	
	public ProductionEvent(String keyObjectStorage, ProductFamily family, String mode, OQCFlag oqcFlag) {
		this.keyObjectStorage = keyObjectStorage;
		this.mode = mode;
		this.oqcFlag = oqcFlag;
	}
	
	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
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
	
	public OQCFlag getOqcFlag() {
		return oqcFlag;
	}

	public void setOqcFlag(OQCFlag oqcFlag) {
		this.oqcFlag = oqcFlag;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"{keyObjectStorage: %s, family: %s, mode: %s, oqcFlag: %s, hostname: %s, creationDate: %s}",
				keyObjectStorage, productFamily, mode, oqcFlag.toString(), getHostname(),
				getCreationDate());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(keyObjectStorage, productFamily, mode, oqcFlag, getHostname(),
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
			ProductionEvent other = (ProductionEvent) obj;
			// field comparison
			ret = Objects.equals(keyObjectStorage, other.keyObjectStorage)
					&& Objects.equals(productFamily, other.getProductFamily()) 
					&& Objects.equals(mode, other.mode)
					&& Objects.equals(oqcFlag, other.oqcFlag) 
					&& Objects.equals(getHostname(), other.getHostname())
					&& Objects.equals(getCreationDate(), other.getCreationDate());
		}
		return ret;
	}

}
