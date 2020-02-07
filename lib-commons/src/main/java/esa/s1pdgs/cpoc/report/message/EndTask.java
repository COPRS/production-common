package esa.s1pdgs.cpoc.report.message;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.Reporting.Event;
import esa.s1pdgs.cpoc.report.Reporting.Status;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.ReportingOutput;

@JsonInclude(Include.NON_NULL)
public class EndTask extends Task {
	private Status status;
	
	@JsonProperty("error_code")
	private int errorCode;
	
	@JsonProperty("duration_in_seconds")
	private double durationSec;
	
	@JsonProperty("data_rate_mebibytes_sec")
	private double rate;
	
	@JsonProperty("data_volume_mebibytes")
	private double volume;
	
	// mandatory, but default is just empty
	private ReportingOutput output = ReportingOutput.NULL;
	
	private ReportingInput input = ReportingInput.NULL;
	
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
			final ReportingOutput output,
			final ReportingInput input
	) {
		super(uid, name, Event.END);
		this.status = status;
		this.errorCode = status.errCode();
		this.output = output;
		this.durationSec = duration;
		this.input = input;
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
	
	public ReportingInput getInput() {
		return input;
	}

	public void setInput(final ReportingInput input) {
		this.input = input;
	}

	public Map<String, String> getQuality() {
		return quality;
	}

	public void setQuality(final Map<String, String> quality) {
		this.quality = quality;
	}
	
	public double getRate() {
		return rate;
	}

	public void setRate(final double rate) {
		this.rate = rate;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(final double volume) {
		this.volume = volume;
	}
}
