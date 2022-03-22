package esa.s1pdgs.cpoc.report.message.output;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class RfiOutput implements ReportingOutput {
	
	@JsonProperty("l1_product_name_string")
	private String l1ProductName;
	
	@JsonProperty("rfi_mitigation_performed_string")
	private String rfiMitigationPerformed;
	
	@JsonProperty("rfi_number_polarisations_detected_integer")
	private int rfiNbPolarisationsDetected;
	
	@JsonProperty("rfi_number_polarisations_mitigated_integer")
	private int rfiNbPolarisationsMitigated;

	public String getL1ProductName() {
		return l1ProductName;
	}

	public void setL1ProductName(String l1ProductName) {
		this.l1ProductName = l1ProductName;
	}

	public String getRfiMitigationPerformed() {
		return rfiMitigationPerformed;
	}

	public void setRfiMitigationPerformed(String rfiMitigationPerformed) {
		this.rfiMitigationPerformed = rfiMitigationPerformed;
	}

	public int getRfiNbPolarisationsDetected() {
		return rfiNbPolarisationsDetected;
	}

	public void setRfiNbPolarisationsDetected(int rfiNbPolarisationsDetected) {
		this.rfiNbPolarisationsDetected = rfiNbPolarisationsDetected;
	}

	public int getRfiNbPolarisationsMitigated() {
		return rfiNbPolarisationsMitigated;
	}

	public void setRfiNbPolarisationsMitigated(int rfiNbPolarisationsMitigated) {
		this.rfiNbPolarisationsMitigated = rfiNbPolarisationsMitigated;
	}
	
}
