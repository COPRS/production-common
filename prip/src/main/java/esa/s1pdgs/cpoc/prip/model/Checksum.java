package esa.s1pdgs.cpoc.prip.model;

public class Checksum {

	public static final String DEFAULT_ALGORITHM = "MD5";

	public enum FIELD_NAMES {
		ALGORITHM("algorithm"), VALUE("value");

		private String fieldName;

		private FIELD_NAMES(String fieldName) {
			this.fieldName = fieldName;
		}

		public String fieldName() {
			return fieldName;
		}
	}

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
		return String.format("{\"%s\":\"%s\", \"%s\":\"%s\"}", FIELD_NAMES.ALGORITHM.fieldName, algorithm,
				FIELD_NAMES.VALUE.fieldName, value);
	}

}
