/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.dlq.manager.model.routing;

import static esa.s1pdgs.cpoc.dlq.manager.model.routing.ActionType.NO_ACTION;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.dlq.manager.configuration.DlqManagerConfigurationProperties.RoutingProperties;


public class RoutingTable {
	
	private static final Logger LOGGER = LogManager.getLogger(RoutingTable.class);
	
	private TreeMap<Integer,List<Rule>> rules = new TreeMap<>();
	
	public static RoutingTable of(Map<String, RoutingProperties> routing) {
		RoutingTable routingTable = new RoutingTable();
		LOGGER.info("Parsing routing table");
		if (routing.isEmpty()) {
			LOGGER.error("No routing rules found");
			throw new RuleParsingException("No routing rules found");
		}
		for (Entry<String, RoutingProperties> ruleEntry : routing.entrySet()) {
			LOGGER.debug("Parsing rule configuration {}", ruleEntry);
			RoutingProperties ruleConfiguration = ruleEntry.getValue();
			Rule rule = new Rule();
			rule.setErrorTitle(ruleConfiguration.getErrorTitle());
			rule.setErrorID(ruleConfiguration.getErrorId());
			rule.setActionType(ruleConfiguration.getActionType());
			rule.setTargetTopic(ruleConfiguration.getTargetTopic());
			rule.setMaxRetry(ruleConfiguration.getMaxRetry());
			rule.setComment(ruleConfiguration.getComment());
			final int priority = ruleConfiguration.getPriority();
			rule.setPriority(priority);
			
			if (!routingTable.rules.containsKey(priority)) {
				routingTable.rules.put(priority, new ArrayList<>());				
			}
			routingTable.rules.get(priority).add(rule);
		}
		LOGGER.info("Routing table: {}", routingTable);
		return routingTable;
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
