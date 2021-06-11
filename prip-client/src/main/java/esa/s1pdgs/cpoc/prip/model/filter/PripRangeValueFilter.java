package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.Objects;

/**
 * Abstract range value filter for querying the persistence repository.
 */
public abstract class PripRangeValueFilter<T extends Object> extends PripQueryFilterTerm {
	
	private T value;
	private RelationalOperator relationalOperator;
	
	public enum RelationalOperator {
		LT("<"), //
		LE("<="), //
		GT(">"), //
		GE(">="), //
		EQ("="), //
		NE("<>");

		private String op;

		private RelationalOperator(String op) {
			this.op = op;
		}

		public String getOperator() {
			return this.op;
		}

		public static RelationalOperator fromString(String operator) {
			if (null == operator) {
				throw new IllegalArgumentException("operator is required");
			}
			
			if (LT.op.equals(operator) || LT.name().equalsIgnoreCase(operator)) {
				return LT;
			}
			if (LE.op.equals(operator) || LE.name().equalsIgnoreCase(operator)) {
				return LE;
			}
			if (GT.op.equals(operator) || GT.name().equalsIgnoreCase(operator)) {
				return GT;
			}
			if (GE.op.equals(operator) || GE.name().equalsIgnoreCase(operator)) {
				return GE;
			}
			if (EQ.op.equals(operator) || EQ.name().equalsIgnoreCase(operator) || "==".equals(operator)) {
				return EQ;
			}
			if (NE.op.equals(operator) || NE.name().equalsIgnoreCase(operator) || "!=".equals(operator)) {
				return NE;
			}

			throw new PripFilterOperatorException(String.format("operator not supported: %s", operator));
		}
		
		public RelationalOperator getInverse() { // used for switching operands: x < 3 --> 3 > x
			switch (this) {
			case LT:
				return GT;
			case LE:
				return GE;
			case GT:
				return LT;
			case GE:
				return LE;
			case EQ:
				return EQ;
			case NE:
				return NE;
			default:
				throw new PripFilterOperatorException(String.format("operator not supported: %s", this));
			}
		}
	}

	// --------------------------------------------------------------------------
	
	public PripRangeValueFilter(String fieldName) {
		super(fieldName);
	}

	protected PripRangeValueFilter(String fieldName, RelationalOperator operator, T value, boolean nested, String path) {
		super(fieldName, nested, path);

		this.relationalOperator = Objects.requireNonNull(operator, "relational operator is required!");
		this.value = Objects.requireNonNull(value, "value is required!");
	}

	public PripRangeValueFilter(String fieldName, RelationalOperator operator, T value) {
		this(fieldName, operator, value, false, null);
	}

	// --------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.value, this.relationalOperator);
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

		final PripRangeValueFilter<?> other = (PripRangeValueFilter<?>) obj;
		return super.equals(obj) && Objects.equals(this.value, other.value)
				&& Objects.equals(this.relationalOperator, other.relationalOperator);
	}
	
	@Override
	public String toString() {
		return this.getFieldName() + " " + (null != this.relationalOperator ? this.relationalOperator.op : "NO_OP") + " " + this.getValue();
	}

	// --------------------------------------------------------------------------

	public T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public RelationalOperator getRelationalOperator() {
		return this.relationalOperator;
	}

	public void setRelationalOperator(RelationalOperator operator) {
		this.relationalOperator = operator;
	}
	
}
