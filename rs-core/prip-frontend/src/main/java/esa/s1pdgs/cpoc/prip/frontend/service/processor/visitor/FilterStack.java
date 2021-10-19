package esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterTerm;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList.LogicalOperator;

public class FilterStack {

	private static final Logger LOGGER = LoggerFactory.getLogger(FilterStack.class);

	private final Deque<PripQueryFilter> filterStack = new ArrayDeque<>();

	// --------------------------------------------------------------------------

	public FilterStack() {
		super();
	}

	// --------------------------------------------------------------------------

	@Override
	public String toString() {
		return this.filterStack.toString();
	}

	// --------------------------------------------------------------------------

	public void push(final PripQueryFilter filter) {
		LOGGER.debug(String.format("push filter on stack: %s", filter));
		this.filterStack.push(filter);
		LOGGER.debug(String.format("stack after push: %s", this.filterStack));
	}

	public PripQueryFilter pop() {
		final PripQueryFilter filter = this.filterStack.pop();
		LOGGER.debug(String.format("pop filter from stack: %s", filter));
		LOGGER.debug(String.format("stack after pop: %s", this.filterStack));
		return filter;
	}

	public void applyOperator(final BinaryOperatorKind operator) throws ExpressionVisitException {
		final PripQueryFilter lastStackElement = this.filterStack.pop();
		final PripQueryFilter secondLastStackElement = this.filterStack.pop();

		LOGGER.debug(String.format("applying '%s' operator on operands: %s, %s", operator, secondLastStackElement, lastStackElement));

		if (isFilterTerm(lastStackElement) && isFilterTerm(secondLastStackElement)) {
			// if both elements are filter terms ... add them to a newly created list and push the list onto the stack
			if (BinaryOperatorKind.AND == operator) {
				this.push(PripQueryFilterList.matchAll(secondLastStackElement, lastStackElement));
			} else if (BinaryOperatorKind.OR == operator) {
				this.push(PripQueryFilterList.matchAny(secondLastStackElement, lastStackElement));
			}
		} else if (isFilterTerm(lastStackElement) && isFilterList(secondLastStackElement)) {
			// if one element is a filter term and the other a filter list ...
			// ... and if the operators are the same ...
			if (LogicalOperator.fromString(operator.toString()) == ((PripQueryFilterList) secondLastStackElement).getOperator()) {
				// ... add the term to the list and push the list onto the stack
				this.push(addToList((PripQueryFilterList) secondLastStackElement, lastStackElement));
			} else /* ... and the operators differ ... */ {
				// ... create a new list using the operator, add both to the new list and push the new list back on stack
				if (BinaryOperatorKind.AND == operator) {
					this.push(PripQueryFilterList.matchAll(secondLastStackElement, lastStackElement));
				} else if (BinaryOperatorKind.OR == operator) {
					this.push(PripQueryFilterList.matchAny(secondLastStackElement, lastStackElement));
				}
			}
		} else if (isFilterList(lastStackElement) && isFilterTerm(secondLastStackElement)) {
			// if one element is a filter term and the other a filter list ...
			// ... and if the operators are the same ...
			if (LogicalOperator.fromString(operator.toString()) == ((PripQueryFilterList) lastStackElement).getOperator()) {
				// ... add the term to the list and push the list onto the stack
				this.push(addToList((PripQueryFilterList) lastStackElement, secondLastStackElement));
			} else /* ... and the operators differ ... */ {
				// ... create a new list using the operator, add both to the new list and push the new list back on stack
				if (BinaryOperatorKind.AND == operator) {
					this.push(PripQueryFilterList.matchAll(secondLastStackElement, lastStackElement));
				} else if (BinaryOperatorKind.OR == operator) {
					this.push(PripQueryFilterList.matchAny(secondLastStackElement, lastStackElement));
				}
			}
		} else if (isFilterList(lastStackElement) && isFilterList(secondLastStackElement)) {
			// if both elements are filter lists ...

			final LogicalOperator parsedOperator = LogicalOperator.fromString(operator.toString());
			final PripQueryFilterList lastStackElementList = (PripQueryFilterList) lastStackElement;
			final PripQueryFilterList secondLastStackElementList = (PripQueryFilterList) secondLastStackElement;
			// ... and if the three operators are the same ...
			if (parsedOperator == lastStackElementList.getOperator() && parsedOperator == secondLastStackElementList.getOperator()) {
				// ... unify the list and push the unified List back on stack
				if (BinaryOperatorKind.AND == operator) {
					final PripQueryFilterList newAndList = PripQueryFilterList.matchAll();
					newAndList.addFilter(secondLastStackElementList.getFilterList());
					newAndList.addFilter(lastStackElementList.getFilterList());
					this.push(newAndList);
				} else if (BinaryOperatorKind.OR == operator) {
					final PripQueryFilterList newOrList = PripQueryFilterList.matchAny();
					newOrList.addFilter(secondLastStackElementList.getFilterList());
					newOrList.addFilter(lastStackElementList.getFilterList());
					this.push(newOrList);
				}
			} else {
				// ... create a new list using the operator, add both to the new list and push the new list back on stack
				if (BinaryOperatorKind.AND == operator) {
					this.push(PripQueryFilterList.matchAll(secondLastStackElementList, lastStackElementList));
				} else if (BinaryOperatorKind.OR == operator) {
					this.push(PripQueryFilterList.matchAny(secondLastStackElementList, lastStackElementList));
				}
			}
		}
	}

	public boolean isEmpty() {
		return this.filterStack.isEmpty();
	}

	public int size() {
		return this.filterStack.size();
	}

	// --------------------------------------------------------------------------

	private static boolean isFilterList(final PripQueryFilter filter) {
		return filter instanceof PripQueryFilterList;
	}

	private static boolean isFilterTerm(final PripQueryFilter filter) {
		return filter instanceof PripQueryFilterTerm;
	}

	private static PripQueryFilterList addToList(final PripQueryFilterList list, final PripQueryFilter... filtersToAdd) {
		list.addFilter(filtersToAdd);
		return list;
	}

}
