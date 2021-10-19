package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.Objects;

/**
 * An abstract filter for querying the persistence repository.
 */
public abstract class PripQueryFilterTerm implements PripQueryFilter, NestableQueryFilter {

	private String fieldName;

	private boolean nested = false;
	private String path;

	// --------------------------------------------------------------------------

	public PripQueryFilterTerm(String fieldName) {
		this.fieldName = Objects.requireNonNull(fieldName, "field name required!");
	}

	protected PripQueryFilterTerm(final String fieldName, final boolean nested, final String path) {
		this(fieldName);

		this.setNested(nested);
		this.setPath(path);
	}

	// --------------------------------------------------------------------------

	@Override
	public void makeNested(final String path) {
		this.path = Objects.requireNonNull(path);
		this.nested = true;
	}

	@Override
	public boolean isNested() {
		return this.nested;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	// --------------------------------------------------------------------------

	@Override
	public int hashCode() {
		return Objects.hash(this.fieldName, this.nested, this.path);
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
		return Objects.equals(this.fieldName, other.fieldName) && Objects.equals(this.nested, other.nested) && Objects.equals(this.path, other.path);
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

	protected void setPath(final String path) {
		this.path = path;
	}

	protected void setNested(final boolean nested) {
		this.nested = nested;
	}

}
