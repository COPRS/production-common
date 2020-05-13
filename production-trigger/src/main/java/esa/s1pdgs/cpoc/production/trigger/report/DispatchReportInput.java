package esa.s1pdgs.cpoc.production.trigger.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.production.trigger.service.L0SegmentConsumer;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class DispatchReportInput extends AbstractFilenameReportingProduct implements ReportingInput  {	
	@JsonProperty("job_id_long")
	private long jobId;
	
	@JsonProperty("input_type_string")
	private String inputType;
	
	public DispatchReportInput(final ReportingFilenameEntries entries, final long jobId, final String inputType) {
		super(entries);
		this.jobId = jobId;
		this.inputType = inputType;
	}
	
	@JsonIgnore
	public static final DispatchReportInput newInstance(final long jobId, final String filename, final String inputType) {        
		final ProductFamily family = inputType.equals(L0SegmentConsumer.TYPE) ?
				ProductFamily.L0_SEGMENT :
			    ProductFamily.BLANK; // we only care about segments here, everything else will be reported as 'filename'
		
		return new DispatchReportInput(
				new ReportingFilenameEntries(
						new ReportingFilenameEntry(family, filename)), 
				jobId, 
    			inputType
		);
	}
	
	public String getInputType() {
		return inputType;
	}

	public long getJobId() {
		return jobId;
	}
}
