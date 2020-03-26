package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;

public class JobReportingInput extends FilenameReportingInput {	
	@JsonProperty("job_order_id_string")
	private String jobOrderUuid;
	
	public JobReportingInput(final List<String> filenames, final List<String> segments, final String jobOrderUuid) {
		super(filenames, segments);
		this.jobOrderUuid = jobOrderUuid;
	}
	
	public static final JobReportingInput newInstance(
			final List<String> filenames,
			final String jobOrderUuid, 
			final ApplicationLevel level
	) {
		if (level == ApplicationLevel.L0_SEGMENT) {
			return new JobReportingInput(
					Collections.emptyList(),
					filenames,
					jobOrderUuid
			);
		}
		return new JobReportingInput(			
				filenames,
				Collections.emptyList(),
				jobOrderUuid
		);
	}

	public String getJobOrderUuid() {
		return jobOrderUuid;
	}

	public void setJobOrderUuid(final String jobOrderUuid) {
		this.jobOrderUuid = jobOrderUuid;
	}
}
