package esa.s1pdgs.cpoc.obs_sdk.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;

public class ObsReportingInput extends FilenameReportingInput {
	@JsonProperty("bucket_string")
	private String bucketName;

	public ObsReportingInput(final String bucketName, final String filename) {
		super(filename);
		this.bucketName = bucketName;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(final String bucketName) {
		this.bucketName = bucketName;
	}
}
