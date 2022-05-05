package esa.s1pdgs.cpoc.datalifecycle.worker.rest.model;

import java.util.List;

public class ProductPostDto {
	private List<String> productnames;

	public ProductPostDto() {
	}

	public List<String> getProductnames() {
		return productnames;
	}

	public void setProductnames(List<String> productnames) {
		this.productnames = productnames;
	}

	@Override
	public String toString() {
		return "ProductPostDto [productnames=" + productnames + "]";
	}

	
}
