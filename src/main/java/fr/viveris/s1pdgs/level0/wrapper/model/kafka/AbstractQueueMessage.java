package fr.viveris.s1pdgs.level0.wrapper.model.kafka;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;

public abstract class AbstractQueueMessage {
	
	protected ProductFamily family;
	
	protected String productName;

	/**
	 * @param family
	 * @param productName
	 */
	public AbstractQueueMessage(ProductFamily family, String productName) {
		super();
		this.family = family;
		this.productName = productName;
	}

	/**
	 * @return the family
	 */
	public ProductFamily getFamily() {
		return family;
	}

	/**
	 * @param family the family to set
	 */
	public void setFamily(ProductFamily family) {
		this.family = family;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((productName == null) ? 0 : productName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractQueueMessage other = (AbstractQueueMessage) obj;
		if (family != other.family)
			return false;
		if (productName == null) {
			if (other.productName != null)
				return false;
		} else if (!productName.equals(other.productName))
			return false;
		return true;
	}
	
}
