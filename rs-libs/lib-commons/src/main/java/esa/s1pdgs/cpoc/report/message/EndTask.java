/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.report.message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.MissingOutput;
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
	private Double durationSec;
	
	@JsonProperty("data_rate_mebibytes_sec")
	private Double rate;
	
	@JsonProperty("data_volume_mebibytes")
	private Double volume;
	
	// mandatory, but default is just empty
	private ReportingOutput output = ReportingOutput.NULL;
	
	private ReportingInput input = ReportingInput.NULL;
	
	@JsonProperty("missing_output")
	private List<MissingOutput> missingOutputs = new ArrayList<>();
	
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

	public Double getDurationSec() {
		return durationSec;
	}

	public void setDurationSec(final Double durationSec) {
		this.durationSec = durationSec;
	}

	public ReportingOutput getOutput() {
		return output;
	}

	public void setOutput(final ReportingOutput output) {
		this.output = output;
	}
	
	public List<MissingOutput> getMissingOutputs() {
		return missingOutputs;
	}

	public void setMissingOutputs(List<MissingOutput> missingOutputs) {
		this.missingOutputs = missingOutputs;
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
	
	public Double getRate() {
		return rate;
	}

	public void setRate(final Double rate) {
		this.rate = rate;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(final Double volume) {
		this.volume = volume;
	}
}
