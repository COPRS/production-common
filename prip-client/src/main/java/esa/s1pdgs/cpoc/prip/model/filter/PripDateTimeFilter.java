package esa.s1pdgs.cpoc.prip.model.filter;

import java.time.LocalDateTime;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;

/**
 * Date time filter for querying the persistence repository.
 */
public class PripDateTimeFilter extends PripQueryFilter {

	public enum Operator {
		LT("<"), GT(">"), LE("<="), GE(">=");

		private String o;

		private Operator(String o) {
			this.o = o;
		}

		public String getOperator() {
			return o;
		}

		public Operator fromString(String operator) {
			if (operator == null) {
				throw new IllegalArgumentException("operator is null");
			}
			if (operator.equals(LT.o)) {
				return LT;
			}
			if (operator.equals(LE.o)) {
				return LE;
			}
			if (operator.equals(GT.o)) {
				return GT;
			}
			if (operator.equals(GE.o)) {
				return GE;
			}
			throw new IllegalArgumentException(String.format("operator not supported: %s", operator));
		}
	}
	
	// --------------------------------------------------------------------------

	private LocalDateTime dateTime;
	private Operator operator;
	
	// --------------------------------------------------------------------------
	
	public PripDateTimeFilter(String fieldName) {
		super(fieldName);
	}
	
	public PripDateTimeFilter(PripMetadata.FIELD_NAMES fieldName) {
		this(fieldName.fieldName());
	}
	
	public PripDateTimeFilter(String fieldName, Operator operator, LocalDateTime dateTime) {
		this(fieldName);

		this.operator = Objects.requireNonNull(operator, "operator is required!");
		this.dateTime = Objects.requireNonNull(dateTime, "datetime value is required!");
	}
	
	// --------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return String.format("{\"%s\":\"%s\"}", (operator == null) ? null : operator.getOperator(),
				(dateTime == null) ? null : DateUtils.formatToMetadataDateTimeFormat(dateTime));
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(this.dateTime, this.operator);
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

		final PripDateTimeFilter other = (PripDateTimeFilter) obj;
		return super.equals(obj) && Objects.equals(this.operator, other.operator)
				&& Objects.equals(this.dateTime, other.dateTime);
	}
	
	// --------------------------------------------------------------------------

	public LocalDateTime getDateTime() {
		return this.dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Operator getOperator() {
		return this.operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

}
