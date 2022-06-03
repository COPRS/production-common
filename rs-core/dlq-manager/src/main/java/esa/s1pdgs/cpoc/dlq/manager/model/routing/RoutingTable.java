package esa.s1pdgs.cpoc.dlq.manager.model.routing;

import static esa.s1pdgs.cpoc.dlq.manager.model.routing.ActionType.NO_ACTION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RoutingTable {
	
	private static final Logger LOGGER = LogManager.getLogger(RoutingTable.class);

	public final static String PROPERTY_NAME_ERROR_TITLE = "error-title";
	public final static String PROPERTY_NAME_ERROR_ID = "error-id";
	public final static String PROPERTY_NAME_ACTION_TYPE = "action-type";
	public final static String PROPERTY_NAME_TARGET_TOPIC = "target-topic";
	public final static String PROPERTY_NAME_MAX_RETRY = "max-retry";
	public final static String PROPERTY_NAME_PRIORITY = "priority";
	public final static String PROPERTY_NAME_COMMENT = "comment";
	
	private TreeMap<Integer,List<Rule>> rules = new TreeMap<>();
	
	public static RoutingTable of(Map<String, Map<String, String>> routing) {
		RoutingTable routingTable = new RoutingTable();
		LOGGER.info("Parsing routing table");
		for (Entry<String, Map<String, String>> ruleEntry : routing.entrySet()) {
			LOGGER.debug("Parsing rule configuration {}", ruleEntry);
			Map<String, String> ruleConfiguration = ruleEntry.getValue();
			validateRuleConfiguration(ruleConfiguration);
			Rule rule = new Rule();
			rule.setErrorTitle(getPropertyValue(PROPERTY_NAME_ERROR_TITLE, ruleConfiguration));
			rule.setErrorID(getPropertyValue(PROPERTY_NAME_ERROR_ID, ruleConfiguration));
			rule.setActionType(ActionType.fromValue(getPropertyValue(PROPERTY_NAME_ACTION_TYPE, ruleConfiguration)));
			rule.setTargetTopic(getPropertyValue(PROPERTY_NAME_TARGET_TOPIC, ruleConfiguration));
			rule.setMaxRetry(Integer.parseInt(getPropertyValue(PROPERTY_NAME_MAX_RETRY, ruleConfiguration)));
			rule.setComment(getPropertyValue(PROPERTY_NAME_COMMENT, ruleConfiguration));
			final int priority = Integer.parseInt(getPropertyValue(PROPERTY_NAME_PRIORITY, ruleConfiguration));
			if (!routingTable.rules.containsKey(priority)) {
				routingTable.rules.put(priority, new ArrayList<>());				
			}
			routingTable.rules.get(priority).add(rule);
		}
		LOGGER.info("Routing table: {}", routingTable);
		return routingTable;
	}
	
	private static void validateRuleConfiguration(Map<String, String> ruleConfiguration) {
		Map<String,Boolean> isPresent = new HashMap<>();
		isPresent.put(unifyCase(PROPERTY_NAME_ERROR_TITLE), false);
		isPresent.put(unifyCase(PROPERTY_NAME_ERROR_ID), false);
		isPresent.put(unifyCase(PROPERTY_NAME_ACTION_TYPE), false);
		isPresent.put(unifyCase(PROPERTY_NAME_TARGET_TOPIC), false);
		isPresent.put(unifyCase(PROPERTY_NAME_MAX_RETRY), false);
		isPresent.put(unifyCase(PROPERTY_NAME_PRIORITY), false);
		isPresent.put(unifyCase(PROPERTY_NAME_COMMENT), false);
		
		for (Entry<String, String> kv : ruleConfiguration.entrySet()) {
			final String key = unifyCase(kv.getKey());
			if (isPresent.containsKey(key)) {
				if (isPresent.get(key)) {
					throw new IllegalArgumentException(String.format("Duplicate property '%s' in '%s'",
							kv.getKey(), ruleConfiguration));					
				}
				isPresent.put(key, true);
			} else {
				throw new IllegalArgumentException(String.format("Invalid property '%s' in '%s'",
						kv.getKey(), ruleConfiguration));
			}
		}

		for (String property : Arrays.asList(PROPERTY_NAME_ERROR_TITLE, PROPERTY_NAME_ERROR_ID,
				PROPERTY_NAME_ACTION_TYPE, PROPERTY_NAME_TARGET_TOPIC, PROPERTY_NAME_MAX_RETRY,
				PROPERTY_NAME_PRIORITY, PROPERTY_NAME_COMMENT)) {
			if (!isPresent.get(unifyCase(property))) {
				throw new IllegalArgumentException(String.format("Missing property '%s' in '%s'",
						property, ruleConfiguration));
			}			
		}
		
		try {
			ActionType.fromValue(getPropertyValue(
					PROPERTY_NAME_ACTION_TYPE, ruleConfiguration));
		} catch (Exception e) {			
			throw new IllegalArgumentException(String.format("Invalid value for '%s' in '%s'",
					PROPERTY_NAME_ACTION_TYPE, ruleConfiguration), e);
		}
		
		try {
			Integer.parseInt(getPropertyValue(PROPERTY_NAME_MAX_RETRY, ruleConfiguration));
		} catch (Exception e) {			
			throw new IllegalArgumentException(String.format("Invalid value for '%s' in '%s'",
					PROPERTY_NAME_ACTION_TYPE, ruleConfiguration), e);
		}
		
		try {
			Integer.parseInt(getPropertyValue(PROPERTY_NAME_PRIORITY, ruleConfiguration));
		} catch (Exception e) {			
			throw new IllegalArgumentException(String.format("Invalid value for '%s' in '%s'",
					PROPERTY_NAME_ACTION_TYPE, ruleConfiguration), e);
		}
	}
	
	private static String unifyCase(String s) {
		return s.toLowerCase().replace("-", "");
	}
	
	private static String getPropertyValue(String propertyName, Map<String, String> properties) {
		final String keyword = unifyCase(propertyName);
		for (Entry<String, String> kv : properties.entrySet()) {
			if (keyword.equals(unifyCase(kv.getKey()))) {
				return kv.getValue();
			}
		}
		return "";
	}
	
	private RoutingTable() {
	}
	
	public Optional<Rule> findRule(String text) {
		for (List<Rule> currentPriorityLevel : rules.descendingMap().values()) {
			for (Rule rule : currentPriorityLevel) {
				if (NO_ACTION != rule.getActionType() && rule.matches(text)) {
					return Optional.of(rule);
				}
			}
		}
		return Optional.empty();
	}
	
	public int size() {
		return rules.values().stream().mapToInt(List::size).sum();
	}
	
	@Override
	public String toString() {
		return rules.values().stream().flatMap(List::stream)
		        .collect(Collectors.toList()).toString();
	}
}
