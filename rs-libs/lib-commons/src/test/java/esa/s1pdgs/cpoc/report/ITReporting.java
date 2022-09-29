package esa.s1pdgs.cpoc.report;

import java.util.Collections;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.report.message.output.JobOrderReportingOutput;

public final class ITReporting {
	private static final Logger LOG = LogManager.getLogger(ITReporting.class);
	
	@Test
	public final void testReportingVsLogging() {
		Reporting uut = ReportingUtils.newReportingBuilder(MissionId.S1).newReporting("test");
		
		uut.begin(new ReportingMessage("Foo"));		
		uut.end(new ReportingMessage(42000L, "baz"));	
		
		uut = uut.newReporting("test.child");
		uut.begin(new ReportingMessage("Foo"));		
		uut.end(new JobOrderReportingOutput(UUID.randomUUID().toString(), Collections.singletonMap("foo_string", "bar")), new ReportingMessage(42000L, "baz"));
		
		uut = ReportingUtils.newReportingBuilder(MissionId.S1).newReporting("test2");
		uut.begin(new ReportingMessage("Foo"));

		
		
		uut.end(ReportingUtils.newFilenameReportingOutputFor(ProductFamily.BLANK, "fooBar.txt"), new ReportingMessage(230000000L,"Foo"));
		
		//LOG.debug("foo bar");
	}
	
	// Mock RFI reporting	
	static class RfiOutput implements ReportingOutput {
		private String l1_product_name_string;
		private String rfi_mitigation_performed_string;
		private int rfi_number_rfi_detected_integer;
		private String rfi_mitigation_applied_string;

		public RfiOutput(final String l1_product_name_string, final String rfi_mitigation_performed_string,
				final int rfi_number_rfi_detected_integer, final String rfi_mitigation_applied_string) {
			this.l1_product_name_string = l1_product_name_string;
			this.rfi_mitigation_performed_string = rfi_mitigation_performed_string;
			this.rfi_number_rfi_detected_integer = rfi_number_rfi_detected_integer;
			this.rfi_mitigation_applied_string = rfi_mitigation_applied_string;
		}
		
		public String getL1_product_name_string() {
			return l1_product_name_string;
		}

		public void setL1_product_name_string(final String l1_product_name_string) {
			this.l1_product_name_string = l1_product_name_string;
		}



		public String getRfi_mitigation_performed_string() {
			return rfi_mitigation_performed_string;
		}

		public void setRfi_mitigation_performed_string(final String rfi_mitigation_performed_string) {
			this.rfi_mitigation_performed_string = rfi_mitigation_performed_string;
		}

		public int getRfi_number_rfi_detected_integer() {
			return rfi_number_rfi_detected_integer;
		}

		public void setRfi_number_rfi_detected_integer(final int rfi_number_rfi_detected_integer) {
			this.rfi_number_rfi_detected_integer = rfi_number_rfi_detected_integer;
		}

		public String getRfi_mitigation_applied_string() {
			return rfi_mitigation_applied_string;
		}

		public void setRfi_mitigation_applied_string(final String rfi_mitigation_applied_string) {
			this.rfi_mitigation_applied_string = rfi_mitigation_applied_string;
		}
	}
	
	@Test
	public final void testRfiReporting() {
		final Reporting uut = ReportingUtils.newReportingBuilder(MissionId.S3).newReporting("RfiMitigation");
		uut.begin( 
				new ReportingMessage("Start extraction of RFI metadata from product <PRODUCT_NAME>")
		);		
		
		final RfiOutput output = new RfiOutput(
				"<FILENAME>",
				"BasedOnNoiseMeas",
				42,
				"TimeAndFrequency"
		);
		
		uut.end(
				output,
				new ReportingMessage("End extraction of RFI metadata from product <PRODUCT_NAME>")
		);	
	}
	
	@Test
	public final void testNoRfiReporting() {
		final Reporting uut = ReportingUtils.newReportingBuilder(MissionId.S3).rsChainName("metadata").rsChainVersion("1.0.0").newReporting("RfiMitigation");
		uut.begin( 
				new ReportingMessage("Start extraction of RFI metadata from product <PRODUCT_NAME>")
		);		
		
		final RfiOutput output = new RfiOutput(
				"<FILENAME>",
				"Not Supported",
				0,
				"None"
		);
		
		uut.end(
				output,
				new ReportingMessage("End extraction of RFI metadata from product <PRODUCT_NAME>")
		);	
	}
}
