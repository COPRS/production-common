package esa.s1pdgs.cpoc.prip.model;

import org.json.JSONObject;

public class Checksum {

	public static final String DEFAULT_ALGORITHM = "MD5";

	public enum FIELD_NAMES {
		ALGORITHM("algorithm"), VALUE("value");

		private String fieldName;

		FIELD_NAMES(String fieldName) {
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
		JSONObject json = new JSONObject();
		json.put(FIELD_NAMES.ALGORITHM.fieldName, algorithm);
		json.put(FIELD_NAMES.VALUE.fieldName, value);
		return json.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Checksum other = (Checksum) obj;
		if (algorithm == null) {
			if (other.algorithm != null)
				return false;
		} else if (!algorithm.equals(other.algorithm))
			return false;
		if (value == null) {
			return other.value == null;
		} else return value.equals(other.value);
	}

}
