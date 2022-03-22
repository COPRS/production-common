package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "stringAttributes", "integerAttributes", "doubleAttributes", "dateTimeOffsetAttributes", "booleanAttributes" })
public class AdditionalAttributes implements Serializable {

	private static final long serialVersionUID = 1536516682561061309L;

	@JsonProperty("StringAttributes")
	private final Map<String, String> stringAttributes = new TreeMap<>();

	@JsonProperty("IntegerAttributes")
	private final Map<String, Long> integerAttributes = new TreeMap<>();

	@JsonProperty("DoubleAttributes")
	private final Map<String, Double> doubleAttributes = new TreeMap<>();

	@JsonProperty("DateTimeOffsetAttributes")
	private final Map<String, String> dateTimeOffsetAttributes = new TreeMap<>();

	@JsonProperty("BooleanAttributes")
	private final Map<String, Boolean> booleanAttributes = new TreeMap<>();

	public void addStringAttribute(final String attributeName, final String value) {
		this.stringAttributes.put(attributeName, value);
	}

	public Map<String, String> getStringAttributes() {
		return this.stringAttributes;
	}

	public void addIntegerAttribute(final String attributeName, final long value) {
		this.integerAttributes.put(attributeName, value);
	}

	public Map<String, Long> getIntegerAttributes() {
		return this.integerAttributes;
	}

	public void addDoubleAttribute(final String attributeName, final double value) {
		this.doubleAttributes.put(attributeName, value);
	}

	public Map<String, Double> getDoubleAttributes() {
		return this.doubleAttributes;
	}

	public void addDateAttribute(final String attributeName, final String value) {
		this.dateTimeOffsetAttributes.put(attributeName, value);
	}

	public Map<String, String> getDateTimeOffsetAttributes() {
		return this.dateTimeOffsetAttributes;
	}

	public void addBooleanAttribute(final String attributeName, final boolean value) {
		this.booleanAttributes.put(attributeName, value);
	}

	public Map<String, Boolean> getBooleanAttributes() {
		return this.booleanAttributes;
	}

}
