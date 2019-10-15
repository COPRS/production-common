package esa.s1pdgs.cpoc.prip.model;

public class PripTextFilter {

	public enum Function {
		STARTS_WITH, CONTAINS
	}

	private Function function;
	private String text;

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
