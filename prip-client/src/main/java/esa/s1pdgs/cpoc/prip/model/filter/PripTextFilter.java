package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.Objects;

import esa.s1pdgs.cpoc.prip.model.PripMetadata;

/**
 * Text filter for querying the persistence repository.
 */
public class PripTextFilter extends PripQueryFilter {

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
	
	// --------------------------------------------------------------------------

	public PripTextFilter(String fieldName) {
		super(fieldName);
	}
	
	public PripTextFilter(PripMetadata.FIELD_NAMES fieldName) {
		this(fieldName.fieldName());
	}

	public PripTextFilter(PripMetadata.FIELD_NAMES fieldName, Function function, String text) {
		this(fieldName);

		this.function = Objects.requireNonNull(function);
		this.text = Objects.requireNonNull(text);
	}

	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.function, this.text);
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

		final PripTextFilter other = (PripTextFilter) obj;
		return super.equals(obj) && Objects.equals(this.function, other.function)
				&& Objects.equals(this.text, other.text);
	}
	
	@Override
	public String toString() {
		return String.format("{\"%s\": \"%s\"}", (null == this.function) ? null : this.function.name(), this.text);
	}
	
	// --------------------------------------------------------------------------

	public Function getFunction() {
		return this.function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
