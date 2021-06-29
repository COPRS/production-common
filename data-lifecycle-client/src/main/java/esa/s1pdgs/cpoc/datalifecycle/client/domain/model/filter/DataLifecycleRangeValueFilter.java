package esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter;

import java.util.Objects;

/**
 * Abstract range value filter for querying the persistence repository.
 */
public abstract class DataLifecycleRangeValueFilter<T extends Object> extends DataLifecycleQueryFilter {

	private T value;
	private Operator operator;

	public enum Operator {
		LT("<"), //
		LE("<="), //
		GT(">"), //
		GE(">="), //
		EQ("="), //
		NE("<>");

		private String op;

		private Operator(String op) {
			this.op = op;
		}

		public String getOperator() {
			return this.op;
		}
	}

	// --------------------------------------------------------------------------

	public DataLifecycleRangeValueFilter(String fieldName) {
		super(fieldName);
	}

	public DataLifecycleRangeValueFilter(String fieldName, Operator operator, T value) {
		this(fieldName);

		this.operator = Objects.requireNonNull(operator, "operator is required!");
		this.value = Objects.requireNonNull(value, "value is required!");
	}

	// --------------------------------------------------------------------------

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.value, this.operator);
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

		final DataLifecycleRangeValueFilter<?> other = (DataLifecycleRangeValueFilter<?>) obj;
		return super.equals(obj) && Objects.equals(this.value, other.value)
				&& Objects.equals(this.operator, other.operator);
	}

	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.operator ? this.operator.op : "NO_OP") + " " + this.getValue();
	}

	// --------------------------------------------------------------------------

	public T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public Operator getOperator() {
		return this.operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

}
