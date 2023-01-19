package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.Objects;

/**
 * Boolean filter for querying the persistence repository.
 */
public class PripBooleanFilter extends PripQueryFilterTerm {

	public enum Function {
		EQ("is"), //
		NE("is not");
		
		private String functionName;
		
		private Function(String functionName) {
			this.functionName = functionName;
		}

		public String getFunctionName() {
			return this.functionName;
		}
		
		public static Function fromString(String function) {
		   for (Function f : Function.values()) {
		      if (f.functionName.equalsIgnoreCase(function) ||
		            f.name().equalsIgnoreCase(function)) {
		         return f;
		      }
			}
			throw new PripFilterOperatorException(String.format("boolean function not supported: %s", function));
		}
	}
	
	// --------------------------------------------------------------------------

	private Function function;
	private Boolean value;
	
	// --------------------------------------------------------------------------

	public PripBooleanFilter(String fieldName) {
		super(fieldName);
	}

	private PripBooleanFilter(String fieldName, Function function, Boolean value, boolean nested, String path) {
		super(fieldName, nested, path);

		this.function = Objects.requireNonNull(function);
		this.value = (Objects.requireNonNull(value));
	}

	public PripBooleanFilter(String fieldName, Function function, Boolean value) {
		this(fieldName, function, value, false, null);
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

	@Override
	public PripBooleanFilter copy() {
		return new PripBooleanFilter(this.getFieldName(), this.getFunction(), this.getValue(), this.isNested(), this.getPath());
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
