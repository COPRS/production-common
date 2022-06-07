package esa.s1pdgs.cpoc.dlq.manager.model.routing;

import static esa.s1pdgs.cpoc.dlq.manager.model.routing.ActionType.NO_ACTION;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_ACTION_TYPE;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_COMMENT;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_ERROR_ID;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_ERROR_TITLE;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_MAX_RETRY;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_PRIORITY;
import static esa.s1pdgs.cpoc.dlq.manager.model.routing.Rule.PROPERTY_NAME_TARGET_TOPIC;

import java.util.ArrayList;
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
	
	private final static List<String> ALL_PROPERTIES = List.of(PROPERTY_NAME_ERROR_TITLE,
			PROPERTY_NAME_ERROR_ID, PROPERTY_NAME_ACTION_TYPE, PROPERTY_NAME_TARGET_TOPIC,
			PROPERTY_NAME_MAX_RETRY, PROPERTY_NAME_PRIORITY, PROPERTY_NAME_COMMENT);
	
	private final static List<String> MANDATORY_PROPERTIES =  List.of(PROPERTY_NAME_ERROR_TITLE,
			PROPERTY_NAME_ERROR_ID, PROPERTY_NAME_ACTION_TYPE, PROPERTY_NAME_MAX_RETRY,
			PROPERTY_NAME_PRIORITY);
	
	private TreeMap<Integer,List<Rule>> rules = new TreeMap<>();
	
	public static RoutingTable of(Map<String, Map<String, String>> routing) {
		RoutingTable routingTable = new RoutingTable();
		LOGGER.info("Parsing routing table");
		if (routing.isEmpty()) {
			throw new RuleParsingException("No routing rules found");
		}
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
		isPresent.put(PROPERTY_NAME_ERROR_TITLE, false);
		isPresent.put(PROPERTY_NAME_ERROR_ID, false);
		isPresent.put(PROPERTY_NAME_ACTION_TYPE, false);
		isPresent.put(PROPERTY_NAME_TARGET_TOPIC, false);
		isPresent.put(PROPERTY_NAME_MAX_RETRY, false);
		isPresent.put(PROPERTY_NAME_PRIORITY, false);
		isPresent.put(PROPERTY_NAME_COMMENT, false);
		
		for (Entry<String, String> kv : ruleConfiguration.entrySet()) {
			final String key = formatCase(kv.getKey());
			if (isPresent.containsKey(key)) {
				if (isPresent.get(key)) {
					throw new RuleParsingException(String.format("Duplicate property '%s' in '%s'",
							key, ruleConfiguration));					
				}
				isPresent.put(key, true);
			} else {
				throw new RuleParsingException(String.format("Invalid property '%s' in '%s'",
						kv.getKey(), ruleConfiguration));
			}
		}

		// PROPERTY_NAME_TARGET_TOPIC and PROPERTY_NAME_COMMENT can be omitted
		for (String property : MANDATORY_PROPERTIES) {
			if (!isPresent.get(property)) {
				throw new RuleParsingException(String.format("Missing property '%s' in '%s'",
						property, ruleConfiguration));
			}			
		}
		
		try {
			ActionType.fromValue(getPropertyValue(
					PROPERTY_NAME_ACTION_TYPE, ruleConfiguration));
		} catch (Exception e) {			
			throw new RuleParsingException(String.format("Invalid value for '%s' in '%s'",
					PROPERTY_NAME_ACTION_TYPE, ruleConfiguration), e);
		}
		
		try {
			Integer.parseInt(getPropertyValue(PROPERTY_NAME_MAX_RETRY, ruleConfiguration));
		} catch (Exception e) {			
			throw new RuleParsingException(String.format("Invalid value for '%s' in '%s'",
					PROPERTY_NAME_MAX_RETRY, ruleConfiguration), e);
		}
		
		try {
			Integer.parseInt(getPropertyValue(PROPERTY_NAME_PRIORITY, ruleConfiguration));
		} catch (Exception e) {			
			throw new RuleParsingException(String.format("Invalid value for '%s' in '%s'",
					PROPERTY_NAME_PRIORITY, ruleConfiguration), e);
		}
	}
	
	private static String formatCase(String propertyName) {
		final String lowerKeyword = propertyName.toLowerCase();
		for (String property : ALL_PROPERTIES) {
			final String lowerProp = property.toLowerCase();
			final String lowerPropWithoutHyphen = lowerProp.replace("-", "");
			if (lowerProp.equals(lowerKeyword) || lowerPropWithoutHyphen.equals(lowerKeyword)) {
				return property;
			}
		}		
		return propertyName;
	}

	private static String getPropertyValue(String propertyName, Map<String, String> properties) {
		final String lowerProp = propertyName.toLowerCase();
		final String lowerPropWithoutHyphen = lowerProp.replace("-", "");
		for (Entry<String, String> kv : properties.entrySet()) {
			String lowerKey = kv.getKey().toLowerCase();
			if (lowerProp.equals(lowerKey) || lowerPropWithoutHyphen.equals(lowerKey)) {
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
