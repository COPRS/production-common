package esa.s1pdgs.cpoc.mdc.worker.extraction.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class SegmentReportingOutput implements ReportingOutput {

	@JsonProperty("product_consolidation_string")
	private String productConsolidation;
	
	@JsonProperty("product_sensing_consolidation_string")
	private String productSensingConsolidation;

	public SegmentReportingOutput(final String productConsolidation, final String productSensingConsolidation) {
		this.productConsolidation = productConsolidation;
		this.productSensingConsolidation = productSensingConsolidation;
	}

	public String getProductConsolidation() {
		return productConsolidation;
	}

	public void setProductConsolidation(final String productConsolidation) {
		this.productConsolidation = productConsolidation;
	}

	public String getProductSensingConsolidation() {
		return productSensingConsolidation;
	}

	public void setProductSensingConsolidation(final String productSensingConsolidation) {
		this.productSensingConsolidation = productSensingConsolidation;
	}
}
