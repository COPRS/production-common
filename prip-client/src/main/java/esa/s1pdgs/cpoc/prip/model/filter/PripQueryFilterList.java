package esa.s1pdgs.cpoc.prip.model.filter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A list of filter terms (which may be filter terms and/or filter term lists again) for querying the PRIP persistence.
 */
public class PripQueryFilterList implements PripQueryFilter {

	private LogicalOperator operator;
	private LinkedList<PripQueryFilter> filterList; // can contain filter terms and/or lists

	public static enum LogicalOperator {
		AND, OR
	}

	// --------------------------------------------------------------------------

	@SafeVarargs
	public static final <F extends PripQueryFilter> PripQueryFilterList matchAll(F... filters) {
		return new PripQueryFilterList(LogicalOperator.AND, Arrays.asList(filters));
	}

	public static final <F extends PripQueryFilter> PripQueryFilterList matchAll(List<F> filters) {
		return new PripQueryFilterList(LogicalOperator.AND, filters);
	}

	@SafeVarargs
	public static final <F extends PripQueryFilter> PripQueryFilterList matchAny(F... filters) {
		return new PripQueryFilterList(LogicalOperator.OR, Arrays.asList(filters));
	}

	public static final <F extends PripQueryFilter> PripQueryFilterList matchAny(List<F> filters) {
		return new PripQueryFilterList(LogicalOperator.OR, filters);
	}

	// --------------------------------------------------------------------------

	public PripQueryFilterList(final LogicalOperator operator, final List<? extends PripQueryFilter> filters) {
		this.operator = Objects.requireNonNull(operator, "logical operator required!");

		if (null != filters) {
			this.filterList = new LinkedList<>(filters);
		} else {
			this.filterList = new LinkedList<>();
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

	public LinkedList<PripQueryFilter> getFilterList() {
		return this.filterList;
	}

}
