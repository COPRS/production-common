package esa.s1pdgs.cpoc.jobgenerator.model.metadata;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.metadata.model.SearchMetadata;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataQuery;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadataResult;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class SearchMetadataResultTest {

	/**
	 * Test toString
	 */
	@Test
	public void testToJsonString() {
		SearchMetadataQuery query = new SearchMetadataQuery(12, "retrievalMode", 0.0, 1.5, "productType", ProductFamily.L0_SLICE);
		SearchMetadataQuery query2 = new SearchMetadataQuery(1, "retrievalode", 2.1, 1.3, "productype", ProductFamily.L0_SLICE);
		SearchMetadata result = new SearchMetadata("name", "type", "kobs", "start", "stop", "mission", "satellite", "station");

		SearchMetadataResult obj = new SearchMetadataResult(query);
		obj.setResult(Arrays.asList(result));

		String str = obj.toJsonString();
		assertTrue(str.contains("query: "));
		assertTrue(str.contains("result: "));

		obj.setQuery(query2);

		str = obj.toJsonString();
		assertTrue(str.contains("query: "));
		assertTrue(str.contains("result: "));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SearchMetadataResult.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
