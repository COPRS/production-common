package esa.s1pdgs.cpoc.prip.model;

public class Checksum {

	public static final String DEFAULT_ALGORITHM = "MD5";

	private String algorithm;
	private String value;

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("{\"algorithm\": \"%s\", \"value\": \"%s\"}", algorithm, value);
	}

}
