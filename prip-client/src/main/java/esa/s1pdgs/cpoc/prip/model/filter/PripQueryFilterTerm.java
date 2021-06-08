package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.Objects;

/**
 * An abstract filter for querying the persistence repository.
 */
public abstract class PripQueryFilterTerm implements PripQueryFilter {

	private String fieldName;

	// --------------------------------------------------------------------------

	public PripQueryFilterTerm(String fieldName) {
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

		final PripQueryFilterTerm other = (PripQueryFilterTerm) obj;
		return Objects.equals(this.fieldName, other.fieldName);
	}

	// --------------------------------------------------------------------------

	public abstract PripQueryFilterTerm copy();

	// --------------------------------------------------------------------------

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

}
