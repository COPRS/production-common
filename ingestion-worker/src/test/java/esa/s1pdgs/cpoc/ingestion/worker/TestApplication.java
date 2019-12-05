package esa.s1pdgs.cpoc.ingestion.worker;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class TestApplication {			
	
	@Autowired 
	private IngestionWorkerService uut;
	
	@Test
	public void testRegexConfigAux() throws Exception {		

		final String auxProduct = "S1__AUX_WAV_V20181002T060000_G20180927T143827.SAFE";
		
		final IngestionJob job = new IngestionJob();
		job.setKeyObjectStorage(auxProduct);	
		
		final ProductFamily actualFamily = uut.getFamilyFor(job);
		assertEquals(ProductFamily.AUXILIARY_FILE, actualFamily);
	}
}
