package esa.s1pdgs.cpoc.mdc.worker.extraction.report;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingInput;

public class TimelinessReportingInput implements ReportingInput {	
	@JsonProperty("datatake_id_string")
	private String datatakeId;
	
	@JsonProperty("packet_store_strings")
	private List<String> packetStore;
	
	@JsonProperty("satellite_string")
	private String satellite;
	
	public TimelinessReportingInput() {
		
	}
	
	public TimelinessReportingInput(final String datatakeId, final List<String> packetStore, final String satellite) {
		this.datatakeId = datatakeId;
		this.packetStore = packetStore;
		this.satellite = satellite;
	}

	public String getDatatakeId() {
		return datatakeId;
	}
	
	public void setDatatakeId(final String datatakeId) {
		this.datatakeId = datatakeId;
	}
	
	public List<String> getPacketStore() {
		return packetStore;
	}
	
	public void setPacketStore(final List<String> packetStore) {
		this.packetStore = packetStore;
	}
	
	public String getSatellite() {
		return satellite;
	}
	
	public void setSatellite(final String satellite) {
		this.satellite = satellite;
	}
}
