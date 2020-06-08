package esa.s1pdgs.cpoc.prip.model;

import java.util.Objects;

public class PripTextFilter {

	public enum Function {
		STARTS_WITH, CONTAINS, EQUALS
	}

	private Function function;
	private String text;
	private PripMetadata.FIELD_NAMES fieldName;

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
