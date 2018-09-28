package esa.s1pdgs.cpoc.jobgenerator.model.metadata;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.jobgenerator.model.metadata.SearchMetadata;
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
	public void testToString() {
		SearchMetadataQuery query = new SearchMetadataQuery(12, "retrievalMode", 0.0, 1.5, "productType", ProductFamily.L0_SLICE,"NRT");
		SearchMetadataQuery query2 = new SearchMetadataQuery(1, "retrievalode", 2.1, 1.3, "productype", ProductFamily.L0_SLICE,"NRT");
		SearchMetadata result = new SearchMetadata("name", "type", "kobs", "start", "stop");

		SearchMetadataResult obj = new SearchMetadataResult(query);
		obj.setResult(result);

		String str = obj.toString();
		assertTrue(str.contains("query: " + query.toString()));
		assertTrue(str.contains("result: " + result.toString()));

		obj.setQuery(query2);

		str = obj.toString();
		assertTrue(str.contains("query: " + query2.toString()));
		assertTrue(str.contains("result: " + result.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(SearchMetadataResult.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
