package esa.s1pdgs.cpoc.prip.worker.mapping;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.worker.configuration.ApplicationProperties.MetadataMapping;
import esa.s1pdgs.cpoc.prip.worker.configuration.ApplicationProperties.ProductTypeRegexp;
import esa.s1pdgs.cpoc.prip.worker.service.PripPublishingJobListener;

public class MdcToPripMapper {	

	private static final Logger LOGGER = LogManager.getLogger(PripPublishingJobListener.class);
	
	private final Map<Pattern,Map<String,PripAttribute>> mappingConfiguration;

	private class PripAttribute {
		private String name;
		private Type type;

		public PripAttribute(String name, Type type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}
		
		public Type getType() {
			return type;
		}

	}

	public MdcToPripMapper(final ProductTypeRegexp regexp, MetadataMapping metadataMapping) {
		mappingConfiguration = new HashMap<>();
		mappingConfiguration.put(Pattern.compile(regexp.getL0Std()), createMapping(metadataMapping.getL0Std()));
		mappingConfiguration.put(Pattern.compile(regexp.getL0Ann()), createMapping(metadataMapping.getL0Ann()));
		mappingConfiguration.put(Pattern.compile(regexp.getL0Cal()), createMapping(metadataMapping.getL0Cal()));
		mappingConfiguration.put(Pattern.compile(regexp.getL0Noise()), createMapping(metadataMapping.getL0Noise()));
		mappingConfiguration.put(Pattern.compile(regexp.getL0Gps()), createMapping(metadataMapping.getL0Gps()));
		mappingConfiguration.put(Pattern.compile(regexp.getL0Hktm()), createMapping(metadataMapping.getL0Hktm()));
		mappingConfiguration.put(Pattern.compile(regexp.getL1()), createMapping(metadataMapping.getL1()));
		mappingConfiguration.put(Pattern.compile(regexp.getL2()), createMapping(metadataMapping.getL2()));
		mappingConfiguration.put(Pattern.compile(regexp.getAuxSafe()), createMapping(metadataMapping.getAuxSafe()));
		mappingConfiguration.put(Pattern.compile(regexp.getAuxEof()), createMapping(metadataMapping.getAuxEof()));
	}
	
	private Map<String,PripAttribute> createMapping(final Map<String,String> mapping) {
		final Map<String,PripAttribute> result = new HashMap<>();
		for (Entry<String, String> entrySet : mapping.entrySet()) {
			final String from = entrySet.getValue();
			final Type type;
			int separatorPosition = entrySet.getKey().lastIndexOf('_');
			switch (entrySet.getKey().substring(separatorPosition + 1)) {
				case "string": type = Type.STRING; break;
				case "long": type = Type.LONG; break;
				case "double": type = Type.DOUBLE; break;
				case "date": type = Type.DATE; break;
				case "boolean": type = Type.BOOLEAN; break;
				default:
					LOGGER.error("Unsupported type extension specified for PRIP metadata mapping in {}", entrySet.getKey());
					throw new RuntimeException(String.format("Unsupported type extension specified for PRIP metadata mapping in %s", entrySet.getKey()));
			}
			final String to = entrySet.getKey();
			result.put(from, new PripAttribute(to, type));
		}
		return result;
	}
	
	public Map<String,Object> map(final String productName, final String productType,
			final Map<String, String> additionalProperties) {
		if (null != productType) {
			for (Pattern pattern : mappingConfiguration.keySet()) {
				if (pattern.matcher(productType).matches()) {
					Map<String, PripAttribute> mapping = mappingConfiguration.get(pattern);
					return map(additionalProperties, mapping);
				}
			}
		}
		LOGGER.warn("No matching product type regex found for productname {} with product type {} in set of regex {}",
				productName, productType, mappingConfiguration.keySet());
		return new LinkedHashMap<>();
	}
	
	private Map<String,Object> map(Map<String, String> additionalProperties, final Map<String, PripAttribute> mapping) {
		final Map<String,Object> result = new LinkedHashMap<>();
		for (Entry<String, PripAttribute> entrySet : mapping.entrySet()) {
			final String mdcFieldName = entrySet.getKey();
			final String attributeName = entrySet.getValue().getName();
			final Type type = entrySet.getValue().getType();
			Object attributeValue;
			if (additionalProperties.containsKey(mdcFieldName)) {
				final String inputValue = additionalProperties.get(mdcFieldName);
				if (null == inputValue || "".equals(inputValue) && type != Type.STRING) {
					attributeValue = null;
				} else {
					switch (type) {
						case STRING:
							try {
								JSONArray jsonArray = new JSONArray(inputValue);
								attributeValue = jsonArray.join(",").replace("\"", "");
							} catch (JSONException e) {
								attributeValue = inputValue;
							}
							break;
						case LONG: attributeValue = Long.parseLong(inputValue); break;
						case DOUBLE: attributeValue = Double.parseDouble(inputValue); break;
						case BOOLEAN: attributeValue = Boolean.parseBoolean(inputValue); break;
						case DATE: attributeValue = DateUtils.parse(inputValue); break;
						default:
							LOGGER.error("Unsupported attribute type: {}", type);
							throw new RuntimeException(String.format("Unsupported attribute type: %s", type));
					}
				}
				result.put(attributeName, attributeValue);
			}
		}
		return result;
	}
}
