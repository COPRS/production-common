package esa.s1pdgs.cpoc.prip.model;

import java.util.Objects;

public class PripTextFilter {

	public enum Function {
		STARTS_WITH("startswith"), //
		ENDS_WITH("endswith"), //
		CONTAINS("contains"), //
		EQUALS("eq");
		
		private String functionName;
		
		private Function(String functionName) {
			this.functionName = functionName;
		}

		public String getFunctionName() {
			return this.functionName;
		}
		
		public static Function fromString(String function) {
			if (STARTS_WITH.functionName.equalsIgnoreCase(function) || STARTS_WITH.name().equalsIgnoreCase(function)) {
				return STARTS_WITH;
			}
			if (ENDS_WITH.functionName.equalsIgnoreCase(function) || ENDS_WITH.name().equalsIgnoreCase(function)) {
				return ENDS_WITH;
			}
			if (CONTAINS.functionName.equalsIgnoreCase(function) || CONTAINS.name().equalsIgnoreCase(function)) {
				return CONTAINS;
			}
			if (EQUALS.functionName.equalsIgnoreCase(function) || EQUALS.name().equalsIgnoreCase(function)) {
				return EQUALS;
			}

			throw new IllegalArgumentException(String.format("text filter function not supported: %s", function));
		}
	}
	
	// --------------------------------------------------------------------------

	private Function function;
	private String text;
	private PripMetadata.FIELD_NAMES fieldName;
	
	// --------------------------------------------------------------------------

	public PripTextFilter() {
		super();
	}

	public PripTextFilter(PripMetadata.FIELD_NAMES fieldName, Function function, String text) {
		this();

		this.fieldName = Objects.requireNonNull(fieldName);
		this.function = Objects.requireNonNull(function);
		this.text = Objects.requireNonNull(text);
	}

	// --------------------------------------------------------------------------

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public PripMetadata.FIELD_NAMES getFieldName() {
		return fieldName;
	}

	public void setFieldName(PripMetadata.FIELD_NAMES fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public String toString() {
		return String.format("{\"%s\":\"%s\"}", (function == null) ? null : function.name(), text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fieldName, function, text);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PripTextFilter))
			return false;
		PripTextFilter other = (PripTextFilter) obj;
		return fieldName == other.fieldName && function == other.function && Objects.equals(text, other.text);
	}
	
}
