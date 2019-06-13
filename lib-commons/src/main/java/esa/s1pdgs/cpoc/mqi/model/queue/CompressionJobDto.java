package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressionJobDto {

	private String productName;
	private ProductFamily family = ProductFamily.AUXILIARY_FILE;
	private String keyObjectStorage;

	public CompressionJobDto(final String productName, final ProductFamily family, final String keyObjectStorage) {
		this.productName = productName;
		this.family = family;
		this.keyObjectStorage = keyObjectStorage;
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

	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}

	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}
	
	// TODO: Overwrite default object methods 

}
