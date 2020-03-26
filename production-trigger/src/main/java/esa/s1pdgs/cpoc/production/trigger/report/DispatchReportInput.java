package esa.s1pdgs.cpoc.production.trigger.report;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.production.trigger.service.L0SegmentConsumer;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;

public class DispatchReportInput extends FilenameReportingInput {
	
	@JsonProperty("job_id_long")
	private long jobId;
	
	@JsonProperty("input_type_string")
	private String inputType;
	
	public DispatchReportInput(
			final List<String> filenames, 
			final List<String> segments, 
			final long jobId, 
			final String inputType
	) {
		super(filenames, segments);
		this.jobId = jobId;
		this.inputType = inputType;
	}
	
	public static final DispatchReportInput newInstance(final long jobId, final String filename, final String inputType) {
        if (inputType.equals(L0SegmentConsumer.TYPE)) {
        	return new DispatchReportInput(
        			Collections.emptyList(),
        			Collections.singletonList(filename),
        			jobId, 
        			inputType
        	);
        }
        return new DispatchReportInput(    		
    			Collections.singletonList(filename),
    			Collections.emptyList(),
    			jobId, 
    			inputType
    	); 
	}
	


	public String getInputType() {
		return inputType;
	}

	public void setInputType(final String inputType) {
		this.inputType = inputType;
	}

	public long getJobId() {
		return jobId;
	}

	public void setJobId(final long jobId) {
		this.jobId = jobId;
	}	
}
