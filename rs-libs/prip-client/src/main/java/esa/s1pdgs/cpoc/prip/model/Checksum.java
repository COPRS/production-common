package esa.s1pdgs.cpoc.prip.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class Checksum {

	public static final String DEFAULT_ALGORITHM = "MD5";

	public enum FIELD_NAMES {
		ALGORITHM("algorithm"), VALUE("value"), DATE("checksum_date");

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
	private LocalDateTime date;

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
	
	public LocalDateTime getDate() {
		return this.date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	
	public Map<String, Object> asMap() {
		final Map<String, Object> map = new HashMap<>();
		map.put(FIELD_NAMES.ALGORITHM.fieldName, algorithm);
		map.put(FIELD_NAMES.VALUE.fieldName, value);
		if (null != date) {
			map.put(FIELD_NAMES.DATE.fieldName, DateUtils.formatToOdataDateTimeFormat(date));
		}
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
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
		final Checksum other = (Checksum) obj;
		
		if (algorithm == null) {
			if (other.algorithm != null) {
				return false;
			}
		} else if (!algorithm.equals(other.algorithm)) {
			return false;
		}
		
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		
		return true;
	}

}
