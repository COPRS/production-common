package esa.s1pdgs.cpoc.report;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ReportingOutput {
	@JsonIgnore 
	public static final ReportingOutput NULL = new ReportingOutput() {};
}
