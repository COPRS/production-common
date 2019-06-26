package esa.s1pdgs.cpoc.mqi.model.queue;

public abstract class AbstractDto {

    private String productName;
    
	public AbstractDto() {
	}

	public AbstractDto(String productName) {
		this.productName = productName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
}
