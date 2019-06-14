package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressionJobDto {

	private String productName;
	private ProductFamily family = ProductFamily.AUXILIARY_FILE;
	private String objectStorageKey;

	public CompressionJobDto(final String productName, final ProductFamily family, final String objectStorageKey) {
		this.productName = productName;
		this.family = family;
		this.objectStorageKey = objectStorageKey;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public ProductFamily getFamily() {
		return family;
	}

	public void setFamily(ProductFamily family) {
		this.family = family;
	}

	public String getObjectStorageKey() {
		return objectStorageKey;
	}

	public void setObjectStorageKey(String objectStorageKey) {
		this.objectStorageKey = objectStorageKey;
	}


	
	// TODO: Overwrite default object methods 

}
