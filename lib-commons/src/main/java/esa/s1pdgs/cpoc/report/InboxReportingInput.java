package esa.s1pdgs.cpoc.report;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InboxReportingInput implements ReportingInput {	
	
	@JsonProperty("inbox_name_string")
	private String name;
	
	@JsonProperty("inbox_relative_path_string")
	private String relativePath;
	
	@JsonProperty("inbox_pickup_path_string")
	private String pickupPath;

	public InboxReportingInput(String name, String relativePath, String pickupPath) {
		this.name = name;
		this.relativePath = relativePath;
		this.pickupPath = pickupPath;
	}

	public InboxReportingInput() {
		this(null, null, null);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getPickupPath() {
		return pickupPath;
	}

	public void setPickupPath(String pickupPath) {
		this.pickupPath = pickupPath;
	}
}
