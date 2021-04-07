package esa.s1pdgs.cpoc.datalifecycle.client.domain.model;

public class RetentionPolicy {

	private String productFamily;
	private String filePattern;
	private int retentionTimeDays = -1;

	public String getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(String productFamily) {
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
