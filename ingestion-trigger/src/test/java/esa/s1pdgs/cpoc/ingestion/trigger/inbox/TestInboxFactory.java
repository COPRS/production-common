package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.InboxConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.name.ProductNameEvaluator;

public class TestInboxFactory {
	@Test
	public final void testNewProductNameEvaluatorFor_DefaultConfig() {
		final InboxConfiguration defConfig = new InboxConfiguration();
		defConfig.setFamily(ProductFamily.EDRS_SESSION);
		
		final InboxFactory uut = new InboxFactory(null, null, null, null);
			
		final ProductNameEvaluator eval = uut.newProductNameEvaluatorFor(defConfig);
		assertEquals(
				"L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSDB_00025.raw", 
				eval.evaluateFrom(Paths.get("S1B/L20180724144436762001030/ch02/DCS_02_L20180724144436762001030_ch2_DSDB_00025.raw"))
		);
	}
}
