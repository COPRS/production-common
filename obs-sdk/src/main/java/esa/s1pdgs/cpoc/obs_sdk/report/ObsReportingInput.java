package esa.s1pdgs.cpoc.obs_sdk.report;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;

public class ObsReportingInput extends FilenameReportingInput {
	@JsonProperty("bucket_string")
	private String bucketName;
	
	public ObsReportingInput(
			final List<String> filenames, 
			final List<String> segments, 
			final String bucketName
	) {
		super(filenames, segments);
		this.bucketName = bucketName;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(final String bucketName) {
		this.bucketName = bucketName;
	}
}
