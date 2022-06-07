package esa.s1pdgs.cpoc.dlq.manager.model.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class TestRoutingTable {

	@Test
	public void testNoRulesFound_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Collections.emptyMap());
		});
		assertEquals("No routing rules found", e.getMessage());
	}
	
}
