package esa.s1pdgs.cpoc.ipf.preparation.worker.type.edrs;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobFile;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;

public class TestEdrsSessionProductValidator {
	
	private final EdrsSessionProductValidator uut = new EdrsSessionProductValidator();
	
	@Test
	public final void testAssertIsComplete_OnEverythingAvailable_ShallContinue() throws Exception {
		uut.assertIsComplete(newProduct(
				"dsib1.xml",
				"dsib2.xml",
				Arrays.asList(new AppDataJobFile("raw1_1", "channel1Raw")),
				Arrays.asList(new AppDataJobFile("raw2_1", "channel2Raw"))
		));
	}
	
	@Test
	public final void testAssertIsComplete_OnOneMissingChunk_ShallThrowException() throws Exception {
		final EdrsSessionProduct product = newProduct(
				"dsib1.xml",
				"dsib2.xml",
				Arrays.asList(new AppDataJobFile("raw1_1", null)),
				Arrays.asList(new AppDataJobFile("raw2_1", "channel2Raw"))
		);		
		runTestOnAndExpect(product, 1);	
	}

	@Test
	public final void testAssertIsComplete_OnMultipleMissingChunk_ShallThrowException() throws Exception {
		final EdrsSessionProduct product = newProduct(
				"dsib1.xml",
				"dsib2.xml",
				Arrays.asList(new AppDataJobFile("raw1_1", null)),
				Arrays.asList(new AppDataJobFile("raw2_1", null))
		);			
		runTestOnAndExpect(product, 2);	
	}
	
	@Test
	public final void testAssertIsComplete_OnBothMissingDsibs_ShallThrowException() throws Exception {
		final EdrsSessionProduct product = newProduct(null,null,null,null);
		runTestOnAndExpect(product, 1);	
	}

	@Test
	public final void testAssertIsComplete_OnChannelTwoMissingCompletely_ShallContinue() throws Exception {
		final EdrsSessionProduct product = newProduct(
				"dsib1.xml",
				null,
				Arrays.asList(new AppDataJobFile("raw1_1", "chunk")),
				null
		);
		uut.assertIsComplete(product);
	}
	
	@Test
	public final void testAssertIsComplete_OnChannelTwoMissingButChunksAvailble_ShallThrowException() throws Exception {
		final EdrsSessionProduct product = newProduct(
				"dsib1.xml",
				null,
				Arrays.asList(new AppDataJobFile("raw1_1", "chunk")),
				Arrays.asList(new AppDataJobFile("raw2_1", "chunk"))
		);		
		runTestOnAndExpect(product, 1);	
	}
	
	@Test
	public final void testAssertIsComplete_OnChannelOneMissingCompletely_ShallContinue() throws Exception {
		final EdrsSessionProduct product = newProduct(
				null,
				"dsib2.xml",
				null,
				Arrays.asList(new AppDataJobFile("raw2_1", "chunk"))
		);
		uut.assertIsComplete(product);
	}
	
	@Test
	public final void testAssertIsComplete_OnChannelOneMissingButChunksAvailble_ShallThrowException() throws Exception {
		final EdrsSessionProduct product = newProduct(
				null,
				"dsib2.xml",
				Arrays.asList(new AppDataJobFile("raw1_1", null)),
				Arrays.asList(new AppDataJobFile("raw2_1", "chunk"))
		);		
		runTestOnAndExpect(product, 1);		
	}
	
	private final void runTestOnAndExpect(final EdrsSessionProduct product, final int expectedNumErrors)
	{
		try {
			uut.assertIsComplete(product);
			fail();
		} catch (final IpfPrepWorkerInputsMissingException e) {
			assertEquals(expectedNumErrors, e.getMissingMetadata().size());
		}
	}

	private final EdrsSessionProduct newProduct(
			final String dsib1, 
			final String dsib2, 
			final List<AppDataJobFile> raws1, 
			final List<AppDataJobFile> raws2
	) {
		final EdrsSessionProduct product = EdrsSessionProduct.of(new AppDataJobProduct());
		product.setProductName("testProduct");
		if (dsib1 != null) {
			product.setDsibForChannel(1, dsib1);	
		}
		if (dsib2 != null) {
			product.setDsibForChannel(2, dsib2);	
		}
		if (raws1 != null) {
			product.setRawsForChannel(1, raws1);	
		}
		if (raws2 != null) {
			product.setRawsForChannel(2, raws2);	
		}
		return product;
	}
	
}
