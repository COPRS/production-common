package esa.s1pdgs.cpoc.prip.model;

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
		return String.format("{\"%s\":\"%s\"}", operator.getOperator(), DateUtils.formatToMetadataDateTimeFormat(dateTime));
	}

}
