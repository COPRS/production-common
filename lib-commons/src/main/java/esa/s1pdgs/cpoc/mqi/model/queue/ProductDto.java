package esa.s1pdgs.cpoc.mqi.model.queue;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class ProductDto {

    private String productName;
    private String keyObjectStorage;    
    private ProductFamily family;
    private String mode = null;
    
    public ProductDto() {
		
	}
    
	public ProductDto(String productName, String keyObjectStorage, ProductFamily family) {
		this.productName = productName;
		this.keyObjectStorage = keyObjectStorage;
		this.family = family;
	}

	public String getProductName() {
		return productName;
	}
	
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public String getKeyObjectStorage() {
		return keyObjectStorage;
	}
	
	public void setKeyObjectStorage(String keyObjectStorage) {
		this.keyObjectStorage = keyObjectStorage;
	}
	
	public ProductFamily getFamily() {
		return family;
	}
	
	public void setFamily(ProductFamily family) {
		this.family = family;
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
}
