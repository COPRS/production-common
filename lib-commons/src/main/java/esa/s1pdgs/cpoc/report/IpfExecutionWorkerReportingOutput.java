package esa.s1pdgs.cpoc.report;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IpfExecutionWorkerReportingOutput extends FilenameReportingOutput {
	public static class Segment {
		@JsonProperty("filename_string")
		private String segmentName;
		
		@JsonProperty("product_consolidation_string")
		private String productConsolidation;
		
		@JsonProperty("product_sensing_consolidation_string")
		private String productSensingConsolidation;
		
		public Segment(String segmentName, String productConsolidation, String productSensingConsolidation) {
			this.segmentName = segmentName;
			this.productConsolidation = productConsolidation;
			this.productSensingConsolidation = productSensingConsolidation;
		}
		
		public Segment() {
			this(null,null,null);
		}

		public String getSegmentName() {
			return segmentName;
		}

		public void setSegmentName(String segmentName) {
			this.segmentName = segmentName;
		}

		public String getProductConsolidation() {
			return productConsolidation;
		}

		public void setProductConsolidation(String productConsolidation) {
			this.productConsolidation = productConsolidation;
		}

		public String getProductSensingConsolidation() {
			return productSensingConsolidation;
		}

		public void setProductSensingConsolidation(String productSensingConsolidation) {
			this.productSensingConsolidation = productSensingConsolidation;
		}
	}
	
	@JsonProperty("segment_objects")
	private List<Segment> segments;

	public IpfExecutionWorkerReportingOutput(List<String> filenames, List<Segment> segments) {
		super(filenames);
		this.segments = segments;
	}

	public List<Segment> getSegments() {
		return segments;
	}

	public void setSegments(List<Segment> segments) {
		this.segments = segments;
	}
}
