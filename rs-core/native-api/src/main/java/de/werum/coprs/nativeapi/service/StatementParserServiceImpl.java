package de.werum.coprs.nativeapi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.werum.coprs.nativeapi.config.NativeApiProperties;

@Service
public class StatementParserServiceImpl {
	private static final Logger LOG = LogManager.getLogger(StatementParserServiceImpl.class);

	public enum StatementType {
		SINGLE, RANGE, ARRAY
	}

	public static class Config {
		public String parameter;
		public StatementType type;

		public List<String> variables = new ArrayList<>();
		public List<String> statements = new ArrayList<>();

		public String toString() {
			return "parameter=" + parameter + ", type=" + type.toString() + " ,variables=" + variables.toString()
					+ ", statements=" + statements.toString();
		}
	}

	private Map<String, Config> parsedConfigs = new HashMap<>();

	@Autowired
	private NativeApiProperties properties;

	@PostConstruct
	public void init() {
		LOG.debug("Loading LUT table from configurations...");
		parseConfig(properties.getLutConfigs());
		LOG.info("Parsed {} LUT table configurations", parsedConfigs.size());
	}

	StatementType determinateType(String statement) {
		if (statement.contains("/")) {
			return StatementType.RANGE;
		} else if (statement.startsWith("[") && statement.endsWith("]")) {
			return StatementType.ARRAY;
		} else {
			return StatementType.SINGLE;
		}
	}

	void parseConfig(final Map<String, List<String>> configs) {
		parsedConfigs = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : configs.entrySet()) {
			/*
			 * Configuration is using a key and a value like: parameter={var} We are
			 * spliting at the equal character. Left side is they parameter name, right side
			 * is the statements
			 */
			String[] splits = entry.getKey().split("=");
//			System.out.println("splits:"+splits[0]+"=> "+splits[1]);
			if (splits.length != 2) {
				LOG.error("Configuration for key '{}' seems to be invalid and will be ignored", entry.getKey());
				continue;
			}

			Config config = new Config();
			/*
			 * We are extracting all the variables that are found in the statements and also
			 * check if they are available in the odata statement. If they are not
			 * contained, we assume this to be an error and a possible dangling variable.
			 */
			for (String statement : entry.getValue()) {
				Pattern pattern = Pattern.compile("(?<=\\{).+?(?=\\})");
				Matcher matcher = pattern.matcher(statement);
				while (matcher.find()) {
					String variable = matcher.group();
					/*
					 * if (!Pattern.compile(".*\\{" + variable +
					 * "\\}.*").matcher(statement).matches()) {
					 * LOG.error("Variable '{}' is set, but not contained in odata statement: {}",
					 * variable, entry.getValue()); // The statement is invalid and will be ignored
					 * return; }
					 */
					config.variables.add(variable);
				}
			}

			config.parameter = splits[0];
			config.type = determinateType(splits[1]);
			config.statements = entry.getValue();

			parsedConfigs.put(config.parameter, config);
			LOG.debug("Parsed configuration: {}", config);
		}
	}

	public String buildOdataQuery(Map<String, String> parameters) {
		List<String> oDataStatements = new ArrayList<>();

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			Config config = parsedConfigs.get(entry.getKey());
			if (config == null) {
				break;
			}

			if (config.type == StatementType.SINGLE) {
				String value = entry.getValue();
				String odata = config.statements.get(0).replaceAll("\\{" + config.variables.get(0) + "\\}", value);
				oDataStatements.add(odata);
			} else if (config.type == StatementType.RANGE) {
				if (config.statements.size() != 2) {
					throw new IllegalArgumentException(
							"Ranged query expected 2 statements in odata query, but found " + config.statements.size());
				}
				if (config.variables.size() != 2) {
					throw new IllegalArgumentException(
							"Ranged query expected 2 variables, but found " + config.variables.size());
				}
				
				/*
				 * A open range parameter can be either expressed as a simple slash or as  ..
				 * we are normalize it as it eases the split operation afterwards.
				 */
				String rangeParam = entry.getValue();
				if (rangeParam.startsWith("/") && rangeParam.endsWith("/")) {
					throw new IllegalArgumentException("You cannot use a ranged query with open boundaries to both sides");
				}
				if (rangeParam.endsWith("/")) {
					rangeParam += "..";
				}
				
				if (rangeParam.startsWith("/")) {
					rangeParam = ".."+rangeParam;
				}
				
				String[] values = rangeParam.split("/");

				String min = values[0];
				String max = values[1];

				/*
				 * System.out.println(min); System.out.println(max);
				 * System.out.println(config.variables.get(0));
				 * System.out.println(config.variables.get(1));
				 */
				if (!min.equals("..")) {
					oDataStatements.add(config.statements.get(0).replaceAll("\\{" + config.variables.get(0) + "\\}", min));	
				}
				
				if (!max.equals("..")) {
					oDataStatements.add(config.statements.get(1).replaceAll("\\{" + config.variables.get(1) + "\\}", max));	
				}				
			}
		}

		// Concat
		return String.join(" and ", oDataStatements);
	}
}
