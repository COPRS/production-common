package esa.s1pdgs.cpoc.datalifecycle.client.domain.model.filter;

import java.util.Objects;

/**
 * An abstract filter for querying the persistence repository.
 */
public abstract class DataLifecycleQueryFilter {

	private String fieldName;

	// --------------------------------------------------------------------------

	public DataLifecycleQueryFilter(String fieldName) {
		this.fieldName = Objects.requireNonNull(fieldName, "field name required!");
	}

	// --------------------------------------------------------------------------

	@Override
	public int hashCode() {
		return Objects.hash(this.fieldName);
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

		final DataLifecycleQueryFilter other = (DataLifecycleQueryFilter) obj;
		return Objects.equals(this.fieldName, other.fieldName);
	}

	// --------------------------------------------------------------------------

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

}
