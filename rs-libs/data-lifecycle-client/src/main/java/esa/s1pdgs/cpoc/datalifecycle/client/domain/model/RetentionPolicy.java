package esa.s1pdgs.cpoc.datalifecycle.client.domain.model;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class RetentionPolicy {

	private ProductFamily productFamily;
	private String filePattern;
	private int retentionTimeDays = -1;

	public ProductFamily getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(ProductFamily productFamily) {
		this.productFamily = productFamily;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	public int getRetentionTimeDays() {
		return this.retentionTimeDays;
	}

	public void setRetentionTimeDays(int retentionTimeDays) {
		this.retentionTimeDays = retentionTimeDays;
	}

	@Override
	public String toString() {
		return String.format("RetentionPolicy [productFamily=%s, filePattern=%s, retentionTimeDays=%s]", productFamily,
				filePattern, retentionTimeDays);
	}

}
