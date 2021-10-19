package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public class JobReportingInput extends AbstractFilenameReportingProduct implements ReportingInput {	
	@JsonProperty("job_order_id_string")
	private String jobOrderUuid;
	
	@JsonProperty("ipf_release_string")
	private String ipfVersion;
	
	public JobReportingInput(final ReportingFilenameEntries entries, final String jobOrderUuid,
			final String ipfVersion) {
		super(entries);
		this.jobOrderUuid = jobOrderUuid;
		this.ipfVersion = ipfVersion;
	}
	
	@JsonIgnore
	public static final JobReportingInput newInstance(
			final List<ReportingFilenameEntry> entries,
			final String jobOrderUuid,
			final String ipfVersion
	) {		
		return new JobReportingInput(new ReportingFilenameEntries(entries), jobOrderUuid, ipfVersion);
	}

	public String getJobOrderUuid() {
		return jobOrderUuid;
	}

	public void setJobOrderUuid(final String jobOrderUuid) {
		this.jobOrderUuid = jobOrderUuid;
	}

	public String getIpfVersion() {
		return ipfVersion;
	}

	public void setIpfVersion(String ipfVersion) {
		this.ipfVersion = ipfVersion;
	}

}
