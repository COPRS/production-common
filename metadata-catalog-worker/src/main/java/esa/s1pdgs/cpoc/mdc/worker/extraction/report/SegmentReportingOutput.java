package esa.s1pdgs.cpoc.mdc.worker.extraction.report;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class SegmentReportingOutput implements ReportingOutput {
	public static class Segment {
		@JsonProperty("segment_string")
		private String segmentName;
		
		@JsonProperty("product_consolidation_string")
		private String productConsolidation;
		
		@JsonProperty("product_sensing_consolidation_string")
		private String productSensingConsolidation;
		
		public Segment(final String segmentName, final String productConsolidation, final String productSensingConsolidation) {
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

		public void setSegmentName(final String segmentName) {
			this.segmentName = segmentName;
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
	
	@JsonProperty("segment_objects")
	private List<Segment> segments;

	public SegmentReportingOutput(final Segment segment) {
		this.segments = Collections.singletonList(segment);
	}
	
	public SegmentReportingOutput(final String segmentName, final String productConsolidation, final String productSensingConsolidation) {
		this(new Segment(segmentName, productConsolidation, productSensingConsolidation));
	}

	public List<Segment> getSegments() {
		return segments;
	}

	public void setSegments(final List<Segment> segments) {
		this.segments = segments;
	}
}
