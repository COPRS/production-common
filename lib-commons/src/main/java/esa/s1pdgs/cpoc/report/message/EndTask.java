package esa.s1pdgs.cpoc.report.message;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.Reporting.Event;
import esa.s1pdgs.cpoc.report.Reporting.Status;
import esa.s1pdgs.cpoc.report.ReportingOutput;

@JsonInclude(Include.NON_NULL)
public class EndTask extends Task {
	private Status status;
	
	@JsonProperty("error_code")
	private int errorCode;
	
	@JsonProperty("duration_in_seconds")
	private double durationSec;
	
	// mandatory, but default is just empty
	private ReportingOutput output = ReportingOutput.NULL;
	
	// default: empty
	private Map<String,String> quality = new LinkedHashMap<>();
	
	public EndTask() {
		super();
	}

	public EndTask(
			final String uid, 
			final String name, 
			final Status status,
			final double duration,
			final ReportingOutput output
	) {
		super(uid, name, Event.END);
		this.status = status;
		this.errorCode = status.errCode();
		this.output = output;
		this.durationSec = duration;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(final Status status) {
		this.status = status;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(final int errorCode) {
		this.errorCode = errorCode;
	}

	public double getDurationSec() {
		return durationSec;
	}

	public void setDurationSec(final double durationSec) {
		this.durationSec = durationSec;
	}

	public ReportingOutput getOutput() {
		return output;
	}

	public void setOutput(final ReportingOutput output) {
		this.output = output;
	}

	public Map<String, String> getQuality() {
		return quality;
	}

	public void setQuality(final Map<String, String> quality) {
		this.quality = quality;
	}
}
