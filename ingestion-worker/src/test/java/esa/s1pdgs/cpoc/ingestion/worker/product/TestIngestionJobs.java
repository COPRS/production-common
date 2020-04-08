package esa.s1pdgs.cpoc.ingestion.worker.product;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class TestIngestionJobs {

	@Test
	public void testToUri_OnFile() throws InternalErrorException, URISyntaxException {
		IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setProductName("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		ingestionJob.setRelativePath("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		ingestionJob.setPickupBaseURL("file:///data/inbox/AUX/");
		URI result = IngestionJobs.toUri(ingestionJob);
		URI expected = new URI("file:///data/inbox/AUX/S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		assertEquals(expected, result);
	}

	@Test
	public void testToUri_OnXBIP() throws InternalErrorException, URISyntaxException {
		IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setProductName("DCS_01_20200404173230031981_dat/ch_1/DCS_01_20200404173230031981_ch1_DSDB_00021.raw");
		ingestionJob.setRelativePath("S1A/DCS_01_20200404173230031981_dat/ch_1/DCS_01_20200404173230031981_ch1_DSDB_00021.raw");
		ingestionJob.setPickupBaseURL("https://cgs03.sentinel1.eo.esa.int/NOMINAL");
		URI result = IngestionJobs.toUri(ingestionJob);
		URI expected = new URI("https://cgs03.sentinel1.eo.esa.int/NOMINAL/S1A/DCS_01_20200404173230031981_dat/ch_1/DCS_01_20200404173230031981_ch1_DSDB_00021.raw");
		assertEquals(expected, result);
	}

}
