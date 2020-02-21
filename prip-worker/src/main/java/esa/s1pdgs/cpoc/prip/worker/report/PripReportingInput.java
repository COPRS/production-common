package esa.s1pdgs.cpoc.prip.worker.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingInput;

public class PripReportingInput implements ReportingInput {
	
	@JsonProperty("product_name_string")
	private String productName;
	
	@JsonProperty("prip_storage_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date storeDate;
	
	public PripReportingInput() {
		
	}

	public PripReportingInput(final String productName, final Date storeDate) {
		this.productName = productName;
		this.storeDate = storeDate;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(final String productName) {
		this.productName = productName;
	}

	public Date getStoreDate() {
		return storeDate;
	}

	public void setStoreDate(final Date storeDate) {
		this.storeDate = storeDate;
	}
	
	

	

}
