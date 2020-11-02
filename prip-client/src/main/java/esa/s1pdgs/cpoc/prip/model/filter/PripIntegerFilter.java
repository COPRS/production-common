package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.Objects;

/**
 * Integer filter for querying the persistence repository.
 */
public class PripIntegerFilter extends PripQueryFilter {

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
			return op;
		}

		public Operator fromString(String operator) {
			if (null == operator) {
				throw new IllegalArgumentException("operator is required");
			}
			
			if (LT.op.equals(operator)) {
				return LT;
			}
			if (LE.op.equals(operator)) {
				return LE;
			}
			if (GT.op.equals(operator)) {
				return GT;
			}
			if (GE.op.equals(operator)) {
				return GE;
			}
			if (EQ.op.equals(operator)) {
				return EQ;
			}
			if (NE.op.equals(operator) || "!=".equals(operator)) {
				return NE;
			}

			throw new IllegalArgumentException(String.format("operator not supported: %s", operator));
		}
	}
	
	// --------------------------------------------------------------------------

	private Long value;
	private Operator operator;
	
	// --------------------------------------------------------------------------
	
	public PripIntegerFilter(String fieldName) {
		super(fieldName);
	}
	
	public PripIntegerFilter(String fieldName, Operator operator, Long value) {
		this(fieldName);

		this.operator = Objects.requireNonNull(operator, "operator is required!");
		this.value = Objects.requireNonNull(value, "value is required!");
	}
	
	// --------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.operator ? this.operator.op : "NO_OP") + " " + this.value;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.operator, this.value);
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

		final PripIntegerFilter other = (PripIntegerFilter) obj;
		return super.equals(obj) && Objects.equals(this.operator, other.operator)
				&& Objects.equals(this.value, other.value);
	}
	
	// --------------------------------------------------------------------------

	public Operator getOperator() {
		return this.operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Long getValue() {
		return this.value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

}
