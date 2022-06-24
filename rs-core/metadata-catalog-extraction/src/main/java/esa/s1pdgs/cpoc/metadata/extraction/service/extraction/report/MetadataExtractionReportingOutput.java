package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.report;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

@JsonInclude(Include.NON_NULL) // following SD-05-2100-1 S1PRO-REPORT-API only include non null attributes
public class MetadataExtractionReportingOutput implements ReportingOutput {

    public static class EffectiveDownlink {
        @JsonProperty("start_date")
        private String startDate; // S1PRO-2036
        
        @JsonProperty("stop_date")
        private String stopDate; // S1PRO-2036

		public String getStartDate() {
			return startDate;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		public String getStopDate() {
			return stopDate;
		}

		public void setStopDate(String stopDate) {
			this.stopDate = stopDate;
		}
    }
    
    @JsonProperty("effective_downlink")
    private EffectiveDownlink effectiveDownlink; // S1PRO-2036

    @JsonProperty("station_string")
    private String stationString; // S1PRO-2036
    
    @JsonProperty("mission_identifier_string")
    private String missionIdentifierString; // S1PRO-2036
	
    @JsonProperty("type_string")
    private String typeString; // S1PRO-2036
    
	@JsonProperty("product_sensing_start_date")
	private String productSensingStartDate; // S1PRO-1678
	
	@JsonProperty("product_sensing_stop_date")
	private String productSensingStopDate; // S1PRO-1678

	@JsonProperty("product_consolidation_string")
	private String productConsolidation; // S1PRO-1247
	
	@JsonProperty("product_sensing_consolidation_string")
	private String productSensingConsolidation; // S1PRO-1247
	
	@JsonProperty("channel_identifier_short")
	private Integer channelIdentifierShort; // S1PRO-1840
	
	@JsonProperty("raw_count_short")
	private Integer rawCountShort; // S1PRO-1840
	
	@JsonProperty("timeliness_name_string")
	private String timelinessName; // RS-407
	
	@JsonProperty("timeliness_value_seconds_integer")
	private Integer timelinessValueSeconds; // RS-407
	
	@JsonProperty("product_metadata_custom_object")
	private Map<String, Object> productMetadataCustomObject = new HashMap<>(); // RS-407
	
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

	public Integer getChannelIdentifierShort() {
		return channelIdentifierShort;
	}
	
	public void setChannelIdentifierShort(Integer channelIdentifierShort) {
		this.channelIdentifierShort = channelIdentifierShort;
	}
	
	public Integer getRawCountShort() {
		return rawCountShort;
	}
	
	public void setRawCountShort(Integer rawCountShort) {
		this.rawCountShort = rawCountShort;
	}
	
	public EffectiveDownlink getEffectiveDownlink() {
		return effectiveDownlink;
	}
	
	public void setEffectiveDownlink(EffectiveDownlink effectiveDownlink) {
		this.effectiveDownlink = effectiveDownlink;
	}

	public String getStationString() {
		return stationString;
	}
	
	public void setStationString(String stationString) {
		this.stationString = stationString;
	}
	
	public String getMissionIdentifierString() {
		return missionIdentifierString;
	}
	
	public void setMissionIdentifierString(String missionIdentifierString) {
		this.missionIdentifierString = missionIdentifierString;
	}
	
	public String getTypeString() {
		return typeString;
	}
	
	public void setTypeString(String typeString) {
		this.typeString = typeString;
	}

	public String getTimelinessName() {
		return timelinessName;
	}

	public void setTimelinessName(String timelinessName) {
		this.timelinessName = timelinessName;
	}

	public Integer getTimelinessValueSeconds() {
		return timelinessValueSeconds;
	}

	public void setTimelinessValueSeconds(Integer timelinessValueSeconds) {
		this.timelinessValueSeconds = timelinessValueSeconds;
	}

	public Map<String, Object> getProductMetadataCustomObject() {
		return productMetadataCustomObject;
	}

	public void setProductMetadataCustomObject(Map<String, Object> productMetadataCustomObject) {
		this.productMetadataCustomObject = productMetadataCustomObject;
	}
	
}
