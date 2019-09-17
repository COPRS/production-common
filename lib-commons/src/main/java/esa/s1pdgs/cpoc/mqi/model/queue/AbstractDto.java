package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Date;

import esa.s1pdgs.cpoc.common.ProductFamily;

public abstract class AbstractDto {

    private String productName;

    private ProductFamily family = ProductFamily.BLANK;

    private Date creationDate;

    private String hostname;

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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}
