package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;

public class ProductMetadata {

	public static final Gson GSON = new Gson().newBuilder().serializeNulls().create();

	public static ProductMetadata ofJson(String json) {
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		final ProductMetadata productMetadata = new ProductMetadata();
		productMetadata.data = new Gson().newBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
				.create().fromJson(json, type);
		return productMetadata;
	}

	@SuppressWarnings("unchecked")
	public static ProductMetadata ofXml(String xml) throws IOException, JsonSyntaxException {
		final XmlMapper xmlMapper = new XmlMapper();
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<requiredOuterContainer>").append(xml).append("</requiredOuterContainer>");
		final ProductMetadata productMetadata = new ProductMetadata();
		productMetadata.data = (Map<String, Object>) convertTypes(xmlMapper.readValue(stringBuilder.toString().getBytes(), Object.class));
		return productMetadata;
	}

	private static Object convertTypes(Object obj) {
		if (obj instanceof String) {
			final String s = (String)obj;
			if (s.length() > 1 && s.charAt(0) == '0' && s.charAt(1) != '.') {
				return s;
			} else {
				try {
					return Long.parseLong(s);
				} catch (NumberFormatException e2) {
					try {
						return Double.parseDouble(s);
					} catch (NumberFormatException e3) {
						if ("true".equals(s) || "True".equals(s) || "TRUE".equals(s)) {
							return true;
						} else if ("false".equals(s) || "False".equals(s) || "FALSE".equals(s)) {
							return false;
						}
					}
				}
			}
		} else if (obj instanceof List) {			
			@SuppressWarnings("unchecked")
			final List<Object> list = (List<Object>)obj;
			for (int idx = 0; idx < list.size(); idx++) {
				list.set(idx, convertTypes(list.get(idx)));
			}
		} else if (obj instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> map = (Map<String, Object>)obj;
			for (String key : map.keySet()) {
				map.put(key, convertTypes(map.get(key)));
			}
		}
		return obj;
	}

	private Map<String, Object> data = new HashMap<>(); 

	public void put(String key, Object value) throws MetadataMalformedException {
		if ((value instanceof Double && (((Double)value).isInfinite() || ((Double)value).isNaN())) 
			|| (value instanceof Float && (((Float)value).isInfinite() || ((Float)value).isNaN()))) {
			throw new MetadataMalformedException(key, "non-finite numbers");
		}
		data.put(key, value);
	}

	public Object remove(String key) {
		return data.remove(key);
	}

	public boolean has(String key) {
		return data.containsKey(key);
	}

	public Object get(String key) throws MetadataMalformedException {
		if (!data.containsKey(key)) {
			throw new MetadataMalformedException(key, String.format(
					"Key '%s' not present", key));
		}
		return data.get(key);
	}

	public String getString(String key) throws MetadataMalformedException {
		final Object value = data.get(key);
		if (value instanceof String) {
			return (String)value;
		} else {
			throw new MetadataMalformedException(key, String.format(
					"Type error: '%s' is non-finite", value));
		}
	}

	public int getInt(String key) throws MetadataMalformedException {
		final Object value = data.get(key);
		try {
			if (value instanceof Integer) {
				return (Integer)value;
			} else if (value instanceof Number) {
				return ((Number) value).intValue();
			} else {
				return Integer.parseInt((String) value);
			}
		} catch (Exception e) {
			throw new MetadataMalformedException(key, String.format(
					"Type error: '%s' cannot be interpreted as integer", value));
		}
	}

	public long getLong(String key) throws MetadataMalformedException {
		final Object value = data.get(key);
		try {
			if (value instanceof Long) {
				return (Long)value;
			} else if (value instanceof Number) {
				return ((Number) value).longValue();
			} else {
				return Long.parseLong((String) value);
			}
		} catch (Exception e) {
			throw new MetadataMalformedException(key, String.format(
					"Type error: '%s' cannot be interpreted as long", value));
		}
	}

	public double getDouble(String key) throws MetadataMalformedException {
		final Object value = data.get(key);
		try {
			if (value instanceof Double) {
				return (Double)value;
			} else if (value instanceof Number) {
				return ((Number) value).doubleValue();
			} else {
				return Double.parseDouble((String) value);
			}
		} catch (Exception e) {
			throw new MetadataMalformedException(key, String.format(
					"Type error: '%s' cannot be interpreted as double", value));
		}
	}

	public boolean getBoolean(String key) throws MetadataMalformedException {
		final Object value = data.get(key);
		try {
			if (value instanceof Boolean) {
				return (Boolean)value;
			} else {
				return Boolean.parseBoolean((String) value);
			}
		} catch (Exception e) {
			throw new MetadataMalformedException(key, String.format(
					"Type error: '%s' cannot be interpreted as boolean", value));
		}
	}

	public Set<String> keys() {
		return data.keySet();
	}

	public int length() {
		return data.size();
	}

	public Map<String, Object> asMap() {
		return data;
	}

	public String toJson() {
		return GSON.toJson(data);
	}

	public String prettyPrint() {
		return new Gson().newBuilder().serializeNulls()
				.setPrettyPrinting().create().toJson(data).toString();
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

}
