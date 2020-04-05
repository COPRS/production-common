package esa.s1pdgs.cpoc.ingestion.worker.product;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class TestIngestionJobs {

	@Test
	public void testToUri() throws InternalErrorException, URISyntaxException {
		IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setProductName("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		ingestionJob.setRelativePath("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		ingestionJob.setPickupBaseURL("file:///data/inbox/AUX/");
		URI result = IngestionJobs.toUri(ingestionJob);
		URI expected = new URI("file:///data/inbox/AUX/S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		assertEquals(expected, result);
	}

}
