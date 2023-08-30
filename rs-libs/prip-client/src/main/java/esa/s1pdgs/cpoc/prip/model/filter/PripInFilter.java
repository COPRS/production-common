package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.List;
import java.util.Objects;

import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public class PripInFilter extends PripQueryFilterTerm {

	public enum Function {
		IN("in");

		private String functionName;

		private Function(String functionName) {
			this.functionName = functionName;
		}

		public String getFunctionName() {
			return this.functionName;
		}

		public static Function fromString(String function) {
			for (Function f : Function.values()) {
				if (f.functionName.equalsIgnoreCase(function) || f.name().equalsIgnoreCase(function)) {
					return f;
				}
			}
			throw new PripFilterOperatorException(String.format("terms filter function not supported: %s", function));
		}

	}
	
	private Function function;
	private List<Object> terms;

	public PripInFilter(String fieldName) {
		super(fieldName);
	}

	public PripInFilter(PripMetadata.FIELD_NAMES fieldName) {
		this(fieldName.fieldName());
	}
	
	public PripInFilter(String fieldName, Function function, List<Object> terms) {
		super(fieldName, false, null);
		this.function = Objects.requireNonNull(function);
		this.terms = Objects.requireNonNull(terms);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.function, this.terms);
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

		final PripInFilter other = (PripInFilter) obj;
		return super.equals(obj) && Objects.equals(this.function, other.function)
				&& Objects.equals(this.terms, other.terms);
	}
	
	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.function ? this.function.name() : "NO_FUNCTION") + " "
				+ this.terms;
	}
	
	public Function getFunction() {
		return this.function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}
	
	public List<Object> getTerms() {
		return this.terms;
	}
	
	public void setTerms(List<Object> terms) {
		this.terms = terms;
	}

}
