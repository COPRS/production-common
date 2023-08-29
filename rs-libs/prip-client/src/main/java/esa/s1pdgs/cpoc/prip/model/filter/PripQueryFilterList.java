package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import esa.s1pdgs.cpoc.common.utils.CollectionUtil;

/**
 * A list of filter terms (which may be filter terms and/or filter term lists again) for querying the PRIP persistence.
 */
public class PripQueryFilterList implements PripQueryFilter {

	private LogicalOperator operator;
	private List<PripQueryFilter> filterList; // can contain filter terms and/or lists

	public static enum LogicalOperator {
		AND, OR, NOT;

		public static LogicalOperator fromString(final String operator) {
			if (AND.name().equalsIgnoreCase(operator)) {
				return AND;
			}
			if (OR.name().equalsIgnoreCase(operator)) {
				return OR;
			}
			if (NOT.name().equalsIgnoreCase(operator)) {
				return NOT;
			}
			return null;
		}
	}

	// --------------------------------------------------------------------------

	@SafeVarargs
	public static final PripQueryFilterList matchAll(final PripQueryFilter... filters) {
		return new PripQueryFilterList(LogicalOperator.AND, CollectionUtil.toList(filters));
	}

	public static final PripQueryFilterList matchAll(final List<PripQueryFilter> filters) {
		return new PripQueryFilterList(LogicalOperator.AND, filters);
	}

	@SafeVarargs
	public static final PripQueryFilterList matchAny(final PripQueryFilter... filters) {
		return new PripQueryFilterList(LogicalOperator.OR, CollectionUtil.toList(filters));
	}

	public static final PripQueryFilterList matchAny(final List<PripQueryFilter> filters) {
		return new PripQueryFilterList(LogicalOperator.OR, filters);
	}
	
	@SafeVarargs
	public static final PripQueryFilterList not(final PripQueryFilter... filters) {
		return new PripQueryFilterList(LogicalOperator.NOT, CollectionUtil.toList(filters));
	}

	public static final PripQueryFilterList not(final List<PripQueryFilter> filters) {
		return new PripQueryFilterList(LogicalOperator.NOT, filters);
	}

	// --------------------------------------------------------------------------

	public PripQueryFilterList(final LogicalOperator operator, final List<PripQueryFilter> filters) {
		this.operator = Objects.requireNonNull(operator, "logical operator required!");

		if (null != filters) {
			this.filterList = new ArrayList<>(filters);
		} else {
			this.filterList = new ArrayList<>();
		}
	}

	// --------------------------------------------------------------------------

	@Override
	public int hashCode() {
		return Objects.hash(this.getOperator(), this.getFilterList());
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
		final PripQueryFilterList other = (PripQueryFilterList) obj;

		return Objects.equals(this.operator, other.operator) && Objects.equals(this.filterList, other.filterList);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		int count = 0;
		for (final PripQueryFilter filter : this.filterList) {
			if (++count > 1) {
				sb.append(" ").append(this.operator.name()).append(" ");
			}

			sb.append("(").append(filter.toString()).append(")");
		}

		return sb.toString();
	}

	// --------------------------------------------------------------------------

	public LogicalOperator getOperator() {
		return this.operator;
	}

	public void setOperator(LogicalOperator operator) {
		this.operator = operator;
	}

	public List<PripQueryFilter> getFilterList() {
		return this.filterList;
	}

	public void addFilter(final PripQueryFilter filter) {
		this.filterList.add(filter);
	}

	public void addFilter(final PripQueryFilter... filters) {
		this.addFilter(CollectionUtil.toList(filters));
	}

	public void addFilter(final Collection<? extends PripQueryFilter> filters) {
		this.filterList.addAll(filters);
	}

}
