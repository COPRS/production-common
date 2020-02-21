package esa.s1pdgs.cpoc.prip.frontend.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingInput;

public class PripReportingInput implements ReportingInput {
	
	@JsonProperty("product_name_string")
	private String productName;	
	
	@JsonProperty("user_name_string")
	private String userName;
	
	public PripReportingInput() {
		
	}
	
	public PripReportingInput(final String productName, final String userName) {
		this.productName = productName;
		this.userName = userName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}
}
