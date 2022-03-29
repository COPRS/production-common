package esa.s1pdgs.cpoc.mdc.worker.es.model;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import esa.s1pdgs.cpoc.metadata.model.AuxMetadata;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class SearchMetadataTest {
	
	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		SearchMetadata obj = new SearchMetadata();
		obj.setProductName("name");
		obj.setProductType("type");
		obj.setKeyObjectStorage("kobs");
		obj.setValidityStart("start");
		obj.setValidityStop("stop");
		String str = obj.toJsonString();
		assertTrue(str.contains("\"productName\":\"name\""));
		assertTrue(str.contains("\"productType\":\"type\""));
		assertTrue(str.contains("\"keyObjectStorage\":\"kobs\""));
		assertTrue(str.contains("\"validityStart\":\"start\""));
		assertTrue(str.contains("\"validityStop\":\"stop\""));
	}

	@Test
	public void testSerialize() throws IOException {

		SearchMetadata metadata = new SearchMetadata(
				"S1B_OPER_AUX_RESORB_OPOD_20200121T223141_V20200121T183236_20200121T215006.EOF",
				"AUX_RESORB",
				"S1B_OPER_AUX_RESORB_OPOD_20200121T223141_V20200121T183236_20200121T215006",
				"2020-01-21T18:32:36.000000Z",
				"2020-01-21T21:50:06.000000Z",
				"S1",
				"S1B",
				"WILE");

		metadata.setFootprint(asList(asList(1d, 2d, 3d), asList(3d, 4d, 5d)));

		metadata.addAdditionalProperty("selectedOrbitFirstAzimuthTimeUtc", "2020-01-21T18:32:46.331273Z");

		StringWriter string = new StringWriter();
		DefaultPrettyPrinter printer = new DefaultPrettyPrinter("  ").createInstance();
		ObjectWriter writer = new ObjectMapper().writer(printer);
		writer.writeValue(string, metadata);
		System.out.println(string.toString());

		ObjectReader reader = new ObjectMapper().reader().forType(SearchMetadata.class);
		SearchMetadata parsed = reader.readValue(string.toString());

		System.out.println("parsed: " + parsed);

		assertThat(parsed, is(equalTo(metadata)));

	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SearchMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
