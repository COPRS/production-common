package esa.s1pdgs.cpoc.dlq.manager.model.routing;

import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_ACTION_TYPE;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_COMMENT;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_ERROR_ID;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_ERROR_TITLE;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_MAX_RETRY;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_PRIORITY;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_TARGET_TOPIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
	
	@Test
	public void testValidRuleTable() {
		Map<String, Map<String, String>> routing = new HashMap<>();
		routing.put("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "Restart", //
				PROPERTY_NAME_TARGET_TOPIC, "foo", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1", //
				PROPERTY_NAME_COMMENT, "foo" //
		));
		routing.put("rule2", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "delete", //
				PROPERTY_NAME_TARGET_TOPIC, "", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1", //
				PROPERTY_NAME_COMMENT, "" //
		));
		routing.put("rule3", Map.of( //
				PROPERTY_NAME_ERROR_TITLE.toUpperCase(), "foo", //
				PROPERTY_NAME_ERROR_ID.toUpperCase(), "foo", //
				PROPERTY_NAME_ACTION_TYPE.toUpperCase(), "no-action", //
				PROPERTY_NAME_MAX_RETRY.toUpperCase(), "1", //
				PROPERTY_NAME_PRIORITY.toUpperCase(), "1" //
		));
		routing.put("rule4", Map.of( //
				PROPERTY_NAME_ERROR_TITLE.toUpperCase().replace("-", ""), "foo", //
				PROPERTY_NAME_ERROR_ID.toUpperCase().replace("-", ""), "foo", //
				PROPERTY_NAME_ACTION_TYPE.toUpperCase().replace("-", ""), "NO_ACTION", //
				PROPERTY_NAME_MAX_RETRY.toUpperCase().replace("-", ""), "1", //
				PROPERTY_NAME_PRIORITY.toUpperCase().replace("-", ""), "1" //
		));
		
		RoutingTable.of(routing);
	}
	
	@Test
	public void testRuleMissingErrorTitle_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Missing property '%s'",
				PROPERTY_NAME_ERROR_TITLE)));
	}

	@Test
	public void testRuleMissingErrorID_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Missing property '%s'",
				PROPERTY_NAME_ERROR_ID)));
	}

	@Test
	public void testRuleMissingActionType_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Missing property '%s'",
				PROPERTY_NAME_ACTION_TYPE)));
	}
	
	@Test
	public void testRuleMissingMaxRetry_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Missing property '%s'",
				PROPERTY_NAME_MAX_RETRY)));
	}
	
	@Test
	public void testRuleMissingPriority_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Missing property '%s'",
				PROPERTY_NAME_PRIORITY)));
	}
	
	@Test
	public void testRuleWithInvalidActionType_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "invalid", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Invalid value for '%s'",
				PROPERTY_NAME_ACTION_TYPE)));
	}
	
	@Test
	public void testRuleWithInvalidMaxRetry_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "one", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Invalid value for '%s'",
				PROPERTY_NAME_MAX_RETRY)));
	}
	
	@Test
	public void testRuleWithInvalidPriority_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "one" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Invalid value for '%s'",
				PROPERTY_NAME_PRIORITY)));
	}
	
	@Test
	public void testRuleWithInvalidAdditionalProperty_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1", //
				"additional-foo-property", "foo" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Invalid property '%s'",
				"additional-foo-property")));
	}
	
	@Test
	public void testRuleWithDuplicateErrorTitle_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_TITLE.toUpperCase(), "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Duplicate property '%s'",
				PROPERTY_NAME_ERROR_TITLE)));
	}
	
	@Test
	public void testRuleWithDuplicateErrorID_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ERROR_ID.toUpperCase(), "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Duplicate property '%s'",
				PROPERTY_NAME_ERROR_ID)));
	}
	
	@Test
	public void testRuleWithDuplicateActionType_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_ACTION_TYPE.toUpperCase(), "restart", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Duplicate property '%s'",
				PROPERTY_NAME_ACTION_TYPE)));
	}
	
	@Test
	public void testRuleWithDuplicateTargetTopic_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_TARGET_TOPIC, "foo", //
				PROPERTY_NAME_TARGET_TOPIC.toUpperCase(), "foo", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Duplicate property '%s'",
				PROPERTY_NAME_TARGET_TOPIC)));
	}
	
	@Test
	public void testRuleWithDuplicateMaxRetry_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_MAX_RETRY.toUpperCase(), "1", //
				PROPERTY_NAME_PRIORITY, "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Duplicate property '%s'",
				PROPERTY_NAME_MAX_RETRY)));
	}
	
	@Test
	public void testRuleWithDuplicatePriority_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1", //
				PROPERTY_NAME_PRIORITY.toUpperCase(), "1" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Duplicate property '%s'",
				PROPERTY_NAME_PRIORITY)));
	}
	
	@Test
	public void testRuleWithDuplicateComment_ShallThrowException() {
		RuleParsingException e = assertThrows(
				RuleParsingException.class, //
				() -> { RoutingTable.of(Map.of("rule1", Map.of( //
				PROPERTY_NAME_ERROR_TITLE, "foo", //
				PROPERTY_NAME_ERROR_ID, "foo", //
				PROPERTY_NAME_ACTION_TYPE, "restart", //
				PROPERTY_NAME_MAX_RETRY, "1", //
				PROPERTY_NAME_PRIORITY, "1", //
				PROPERTY_NAME_COMMENT, "foo", //
				PROPERTY_NAME_COMMENT.toUpperCase(), "foo" //
		)));});
		assertTrue(e.getMessage().startsWith(String.format("Duplicate property '%s'",
				PROPERTY_NAME_COMMENT)));
	}
	
}
