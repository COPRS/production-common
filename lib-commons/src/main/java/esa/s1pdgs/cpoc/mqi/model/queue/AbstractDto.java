package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public abstract class AbstractDto {

    private String productName;
    
    private ProductFamily family = ProductFamily.BLANK;
    
	public AbstractDto() {
	}

	public AbstractDto(String productName, ProductFamily family) {
		this.productName = productName;
		this.family = family;
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
}
