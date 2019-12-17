package esa.s1pdgs.cpoc.mqi.client;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class MqiMessageFilter {

	private ProductFamily productFamily;
	private String matchRegex;

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public String getMatchRegex() {
		return matchRegex;
	}

	public void setMatchRegex(String matchRegex) {
		this.matchRegex = matchRegex;
	}

}
