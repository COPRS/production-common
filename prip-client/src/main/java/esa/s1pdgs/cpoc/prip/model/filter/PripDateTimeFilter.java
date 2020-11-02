package esa.s1pdgs.cpoc.prip.model;

import java.time.LocalDateTime;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class PripDateTimeFilter {

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

	private LocalDateTime dateTime;
	private Operator operator;
	private PripMetadata.FIELD_NAMES fieldName;

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public PripMetadata.FIELD_NAMES getFieldName() {
		return fieldName;
	}

	public void setFieldName(PripMetadata.FIELD_NAMES fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public String toString() {
		return String.format("{\"%s\":\"%s\"}", (operator == null) ? null : operator.getOperator(),
				(dateTime == null) ? null : DateUtils.formatToMetadataDateTimeFormat(dateTime));
	}

	@Override
	public int hashCode() {
		return Objects.hash(dateTime, fieldName, operator);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PripDateTimeFilter))
			return false;
		PripDateTimeFilter other = (PripDateTimeFilter) obj;
		return Objects.equals(dateTime, other.dateTime) && fieldName == other.fieldName && operator == other.operator;
	}

}
