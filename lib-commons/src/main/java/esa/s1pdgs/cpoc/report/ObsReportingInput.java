package esa.s1pdgs.cpoc.report;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObsReportingInput implements ReportingInput {
	@JsonProperty("bucket_string")
	private String bucketName;
	
	@JsonProperty("obs_key_string")
	private String obsKey;
		
	public ObsReportingInput(String bucketName, String obsKey) {
		this.bucketName = bucketName;
		this.obsKey = obsKey;
	}
	
	public ObsReportingInput() {
		this(null, null);
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getObsKey() {
		return obsKey;
	}

	public void setObsKey(String obsKey) {
		this.obsKey = obsKey;
	}
}
