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
	
	public JobReportingInput(final ReportingFilenameEntries entries, final String jobOrderUuid) {
		super(entries);
		this.jobOrderUuid = jobOrderUuid;
	}
//	
//	@JsonIgnore
//	public static final JobReportingInput newInstance(
//			final List<String> filenames,
//			final String jobOrderUuid, 
//			final ApplicationLevel level
//	) {		
//		// we just care about the L0_SEGMENT scenario. Everything else is just reported as filename,
//		// so we set the family here to BLANK to avoid confusion of declaring it L0_SLICE for L1 or L2
//		// productions
//		final ProductFamily family = (level == ApplicationLevel.L0_SEGMENT) ?
//				ProductFamily.L0_SEGMENT :
//				ProductFamily.BLANK;
//		
//		final List<ReportingFilenameEntry> reportingEntries = filenames.stream()
//				.map(name -> new ReportingFilenameEntry(family, name))
//				.collect(Collectors.toList());
//		
//		return new JobReportingInput(new ReportingFilenameEntries(reportingEntries), jobOrderUuid);
//	}
	
	@JsonIgnore
	public static final JobReportingInput newInstance(
			final List<ReportingFilenameEntry> entries,
			final String jobOrderUuid
	) {		
		return new JobReportingInput(new ReportingFilenameEntries(entries), jobOrderUuid);
	}
	

	public String getJobOrderUuid() {
		return jobOrderUuid;
	}

	public void setJobOrderUuid(final String jobOrderUuid) {
		this.jobOrderUuid = jobOrderUuid;
	}
}
