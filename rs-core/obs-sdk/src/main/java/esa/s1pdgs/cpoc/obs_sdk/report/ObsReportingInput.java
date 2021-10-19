package esa.s1pdgs.cpoc.obs_sdk.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class ObsReportingInput extends AbstractFilenameReportingProduct implements ReportingInput {
	@JsonProperty("bucket_string")
	private String bucketName;
	
	public ObsReportingInput(final ReportingFilenameEntries entries, final String bucketName) {
		super(entries);
		this.bucketName = bucketName;
	}

	public String getBucketName() {
		return bucketName;
	}
}
