package esa.s1pdgs.cpoc.mdc.worker.extraction.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

@JsonInclude(Include.NON_NULL) // following SD-05-2100-1 S1PRO-REPORT-API only include non null attributes
public class MetadataExtractionReportingOutput implements ReportingOutput {

	@JsonProperty("product_sensing_start_date")
	private String productSensingStartDate; // S1PRO-1678
	
	@JsonProperty("product_sensing_stop_date")
	private String productSensingStopDate; // S1PRO-1678

	@JsonProperty("product_consolidation_string")
	private String productConsolidation; // S1PRO-1247
	
	@JsonProperty("product_sensing_consolidation_string")
	private String productSensingConsolidation; // S1PRO-1247
	
	@JsonProperty("channel_identifier_short")
	private int channelIdentifierShort; // S1PRO-1840
	
	@JsonProperty("raw_count_short")
	private int rawCountShort; // S1PRO-1840
	
	// --------------------------------------------------------------------------
	
	public MetadataExtractionReportingOutput() {
	}
	
	// --------------------------------------------------------------------------
	
	public MetadataExtractionReportingOutput withSensingStart(String sensingStartDate) {
		this.productSensingStartDate = sensingStartDate;
		return this;
	}
	
	public MetadataExtractionReportingOutput withSensingStop(String sensingStopDate) {
		this.productSensingStopDate = sensingStopDate;
		return this;
	}
	
	public MetadataExtractionReportingOutput withConsolidation(String consolidation) {
		this.productConsolidation = consolidation;
		return this;
	}
	
	public MetadataExtractionReportingOutput withSensingConsolidation(String sensingConsolidation) {
		this.productSensingConsolidation = sensingConsolidation;
		return this;
	}
	
	public ReportingOutput build() {
		if (null != this.productSensingStartDate || null != this.productSensingStopDate
				|| null != this.productConsolidation || null != this.productSensingConsolidation) {
			return this;
		} else {
			// following SD-05-2100-1 S1PRO-REPORT-API only trace what's there, no data -> no output
			return ReportingOutput.NULL;
		}
	}
	
	// --------------------------------------------------------------------------
	
	public String getProductSensingStartDate() {
		return productSensingStartDate;
	}

	public void setProductSensingStartDate(String productSensingStartDate) {
		this.productSensingStartDate = productSensingStartDate;
	}

	public String getProductSensingStopDate() {
		return productSensingStopDate;
	}

	public void setProductSensingStopDate(String productSensingStopDate) {
		this.productSensingStopDate = productSensingStopDate;
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

	public int getChannelIdentifierShort() {
		return channelIdentifierShort;
	}
	
	public void setChannelIdentifierShort(int channelIdentifierShort) {
		this.channelIdentifierShort = channelIdentifierShort;
	}
	
	public int getRawCountShort() {
		return rawCountShort;
	}
	
	public void setRawCountShort(int rawCountShort) {
		this.rawCountShort = rawCountShort;
	}
}
