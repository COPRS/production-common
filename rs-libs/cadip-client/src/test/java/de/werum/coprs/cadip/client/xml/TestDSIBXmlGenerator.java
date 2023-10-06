package de.werum.coprs.cadip.client.xml;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestDSIBXmlGenerator {

	@Test
	public void testDSIBGenerator() {
		List<String> filenames = new ArrayList<>();

		for (int c = 0; c <= 46; c++) {
			filenames.add("DCS_04_S1B_20200318035405020741_ch1_DSDB_" + String.format("%05d", c) + ".raw");
		}
		String result = DSIBXmlGenerator.generate("S1B_20200318035405020741", filenames, "2020-01-20T16:29:33Z",
				"2020-01-20T16:35:47Z", 13082456436L);
		System.out.println(result);
	}
}
