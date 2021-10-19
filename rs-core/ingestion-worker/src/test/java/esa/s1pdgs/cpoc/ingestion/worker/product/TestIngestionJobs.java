package esa.s1pdgs.cpoc.ingestion.worker.product;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class TestIngestionJobs {

	@Test
	public void testToUri_OnFile() throws InternalErrorException, URISyntaxException {
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setProductName("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		ingestionJob.setRelativePath("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		ingestionJob.setPickupBaseURL("file:///data/inbox/AUX/");
		final URI result = IngestionJobs.toUri(ingestionJob);
		final URI expected = new URI("file:///data/inbox/AUX/S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");
		assertEquals(expected, result);
	}

	@Test
	public void testToUri_OnXBIP() throws InternalErrorException, URISyntaxException {
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setProductName("DCS_01_20200404173230031981_dat/ch_1/DCS_01_20200404173230031981_ch1_DSDB_00021.raw");
		ingestionJob.setRelativePath("S1A/DCS_01_20200404173230031981_dat/ch_1/DCS_01_20200404173230031981_ch1_DSDB_00021.raw");
		ingestionJob.setPickupBaseURL("https://cgs03.sentinel1.eo.esa.int/NOMINAL");
		final URI result = IngestionJobs.toUri(ingestionJob);
		final URI expected = new URI("https://cgs03.sentinel1.eo.esa.int/NOMINAL/S1A/DCS_01_20200404173230031981_dat/ch_1/DCS_01_20200404173230031981_ch1_DSDB_00021.raw");
		assertEquals(expected, result);
	}

	@Test
	public void testToUri_OnAuxip() throws URISyntaxException {
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setInboxType("auxip");
		ingestionJob.setProductName(UUID.randomUUID().toString());
		ingestionJob.setRelativePath("S1A_OPER_AUX_PREORB_OPOD_20201008T000649_V20201007T231318_20201008T054818.EOF.zip");
		ingestionJob.setPickupBaseURL("https://prip.sentinel1.eo.esa.int:443/prif/odata");
		final URI result = IngestionJobs.toUri(ingestionJob);
		final URI expected = new URI("https://prip.sentinel1.eo.esa.int:443/prif/odata");
		System.out.println(expected.getPath());
		assertEquals(expected, result);
	}
	
	@Test
	public final void testFilename_OnSlashes_ShallReturnLastElement() {
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setKeyObjectStorage("DCS_01_20200404173230031981_dat/ch_1/DCS_01_20200404173230031981_ch1_DSDB_00021.raw");		
		assertEquals("DCS_01_20200404173230031981_ch1_DSDB_00021.raw", IngestionJobs.filename(ingestionJob));
	}
	
	@Test
	public final void testFilename_OnPlainFilename_ShallReturnLastElement() {
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setKeyObjectStorage("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF");	
		assertEquals("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.EOF", IngestionJobs.filename(ingestionJob));
	}
	
	@Test
	public final void testFilename_OnTrailingSlash_ShallIgnoreTrailingSlash() {
		final IngestionJob ingestionJob = new IngestionJob();
		ingestionJob.setKeyObjectStorage("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.SAFE/");		
		assertEquals("S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.SAFE", IngestionJobs.filename(ingestionJob));
	}
	
	@Test
	public final void testBasePath_NoSubdirectory_ShallReturnRootPath() throws URISyntaxException {		
		final String name = "S1__OPER_MSK__LAND__V20140403T210200_G20190711T113000.SAFE";
		final URI newProd = new URI(
				"https://prip.sentinel1.eo.esa.int:443/" + name
		);
		assertEquals("/", IngestionJobs.basePath(newProd, name).toString());
	}
}
