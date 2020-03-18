package esa.s1pdgs.cpoc.report.message.input;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.ReportingInput;

public class ObsReportingInput implements ReportingInput {
	@JsonProperty("bucket_string")
	private String bucketName;
	
	@JsonProperty("product_family_string")
	private String productFamily;
	
	@JsonProperty("obs_key_string")
	private String obsKey;
		
	public ObsReportingInput(final String bucketName, final ProductFamily family, final String obsKey) {
		this.bucketName = bucketName;
		this.productFamily = family.toString();
		this.obsKey = obsKey;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(final String bucketName) {
		this.bucketName = bucketName;
	}

	public String getObsKey() {
		return obsKey;
	}

	public void setObsKey(final String obsKey) {
		this.obsKey = obsKey;
	}

	public String getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(final String productFamily) {
		this.productFamily = productFamily;
	}
}
