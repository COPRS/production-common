package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.Objects;

import esa.s1pdgs.cpoc.prip.model.PripMetadata;

/**
 * Boolean filter for querying the persistence repository.
 */
public class PripBooleanFilter extends PripQueryFilter {

	public enum Function {
		EQUALS("is"), //
		EQUALS_NOT("is not");
		
		private String functionName;
		
		private Function(String functionName) {
			this.functionName = functionName;
		}

		public String getFunctionName() {
			return this.functionName;
		}
		
		public static Function fromString(String function) {
			if (EQUALS.functionName.equalsIgnoreCase(function) || EQUALS.name().equalsIgnoreCase(function)
					|| "eq".equalsIgnoreCase(function) || "==".equals(function)) {
				return EQUALS;
			}
			if (EQUALS_NOT.functionName.equalsIgnoreCase(function) || EQUALS_NOT.name().equalsIgnoreCase(function)
					|| "neq".equalsIgnoreCase(function) || "!=".equals(function)) {
				return EQUALS_NOT;
			}

			throw new IllegalArgumentException(String.format("boolean function not supported: %s", function));
		}
	}
	
	// --------------------------------------------------------------------------

	private Function function;
	private Boolean value;
	
	// --------------------------------------------------------------------------

	public PripBooleanFilter(String fieldName) {
		super(fieldName);
	}
	
	public PripBooleanFilter(PripMetadata.FIELD_NAMES fieldName) {
		this(fieldName.fieldName());
	}

	public PripBooleanFilter(PripMetadata.FIELD_NAMES fieldName, Function function, Boolean value) {
		this(fieldName);

		this.function = Objects.requireNonNull(function);
		this.setValue(Objects.requireNonNull(value));
	}

	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.function, this.getValue());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}

		final PripBooleanFilter other = (PripBooleanFilter) obj;
		return super.equals(obj) && Objects.equals(this.function, other.function)
				&& Objects.equals(this.getValue(), other.getValue());
	}
	
	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.function ? this.function.functionName : "NO_FUNCTION") + " "
				+ this.getValue();
	}
	
	// --------------------------------------------------------------------------

	public Function getFunction() {
		return this.function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public Boolean getValue() {
		return this.value;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

}
