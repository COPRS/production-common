package esa.s1pdgs.cpoc.prip.worker.model;

import java.time.LocalDateTime;

import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class PripDateTimeFilter {

	public enum Operator {
		LT("<"), GT(">");

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
			if (operator.equals(GT.o)) {
				return GT;
			}
			throw new IllegalArgumentException(String.format("operator not supported: %s", operator));
		}
	}

	private LocalDateTime dateTime;
	private Operator operator;

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

	@Override
	public String toString() {
		return String.format("{\"%s\":\"%s\"}", (operator == null) ? null : operator.getOperator(),
				(dateTime == null) ? null : DateUtils.formatToMetadataDateTimeFormat(dateTime));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PripDateTimeFilter other = (PripDateTimeFilter) obj;
		if (dateTime == null) {
			if (other.dateTime != null)
				return false;
		} else if (!dateTime.equals(other.dateTime))
			return false;
		if (operator != other.operator)
			return false;
		return true;
	}

}
