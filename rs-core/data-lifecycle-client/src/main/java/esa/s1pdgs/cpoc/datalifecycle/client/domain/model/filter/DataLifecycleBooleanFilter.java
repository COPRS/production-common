package esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter;

import java.util.Objects;

import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.DataLifecycleMetadata;

/**
 * Boolean filter for querying the persistence repository.
 */
public class DataLifecycleBooleanFilter extends DataLifecycleQueryFilter {

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
	}

	// --------------------------------------------------------------------------

	private Function function;
	private Boolean value;

	// --------------------------------------------------------------------------

	public DataLifecycleBooleanFilter(String fieldName) {
		super(fieldName);
	}

	public DataLifecycleBooleanFilter(DataLifecycleMetadata.FIELD_NAME fieldName) {
		super(fieldName.fieldName());
	}

	public DataLifecycleBooleanFilter(DataLifecycleMetadata.FIELD_NAME fieldName, Function function, Boolean value) {
		this(fieldName);

		this.function = Objects.requireNonNull(function);
		this.value = (Objects.requireNonNull(value));
	}

	public DataLifecycleBooleanFilter(String fieldName, Function function, Boolean value) {
		this(fieldName);

		this.function = Objects.requireNonNull(function);
		this.value = (Objects.requireNonNull(value));
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

		final DataLifecycleBooleanFilter other = (DataLifecycleBooleanFilter) obj;
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
