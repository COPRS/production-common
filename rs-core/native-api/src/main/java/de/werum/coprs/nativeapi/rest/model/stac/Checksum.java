package de.werum.coprs.nativeapi.rest.model.stac;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Checksum {

	@JsonProperty("Algorithm")
	private String algorithm;

	@JsonProperty("Value")
	private String value;

	@JsonProperty("ChecksumDate")
	private String date;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.algorithm == null) ? 0 : this.algorithm.hashCode());
		result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
		result = prime * result + ((this.date == null) ? 0 : this.date.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Checksum other = (Checksum) obj;

		if (this.algorithm == null) {
			if (other.algorithm != null) {
				return false;
			}
		} else if (!this.algorithm.equals(other.algorithm)) {
			return false;
		}

		if (this.value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!this.value.equals(other.value)) {
			return false;
		}

		if (this.date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!this.date.equals(other.date)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [value=" + this.value + ", algorithm=" + this.algorithm + ", date=" + this.date + "]";
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDate() {
		return this.date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
