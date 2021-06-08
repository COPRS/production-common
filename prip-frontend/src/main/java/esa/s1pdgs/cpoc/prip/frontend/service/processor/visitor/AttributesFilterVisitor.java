package esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDoubleFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripFilterOperatorException;
import esa.s1pdgs.cpoc.prip.model.filter.PripIntegerFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterList.LogicalOperator;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterTerm;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;

public class AttributesFilterVisitor implements ExpressionVisitor<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductsFilterVisitor.class);

	private static final String ATT_NAME = "att/Name";
	private static final String PLACEHOLDER = "PLACEHOLDER";

	private String type = "";

	private final FilterStack filterStack = new FilterStack();
	private String fieldname;

	// --------------------------------------------------------------------------
	
	public AttributesFilterVisitor(String type) {
		this.type = type;
	}
	
	// --------------------------------------------------------------------------
	
	@Override
	public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, Object right)
			throws ExpressionVisitException, ODataApplicationException {
		
		final String leftOperand = ProductsFilterVisitor.operandToString(left);
		final String rightOperand = ProductsFilterVisitor.operandToString(right);
		LOGGER.debug(String.format("AttributesFilterVisitor got: %s %s %s", leftOperand, operator, rightOperand));

		switch (operator) {
		case OR:
		case AND:
			this.filterStack.applyOperator(operator);
			break;
		default: {
			if (ATT_NAME.equals(leftOperand) && operator == BinaryOperatorKind.EQ) { // e.g. att/Name eq 'productType'
				final String convertedFieldName = "attr_" + removeQuotes(rightOperand) + "_" + this.type;

				if (null != this.fieldname && !this.fieldname.equals(convertedFieldName)) {
					final String msg = String.format("AttributesFilterVisitor: invalid query, only one value for %s allowed per clause, but was: %s",
							ATT_NAME, this.fieldname + ", " + convertedFieldName);
					LOGGER.error(msg);
					throw new ODataApplicationException(msg, HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
				}

				this.fieldname = convertedFieldName;
				this.filterStack.push(this.buildFilter(leftOperand, operator, rightOperand));
			} else if (ATT_NAME.equals(rightOperand) && operator == BinaryOperatorKind.EQ) {
				final String convertedFieldName = "attr_" + removeQuotes(leftOperand) + "_" + this.type;

				if (null != this.fieldname && !this.fieldname.equals(convertedFieldName)) {
					final String msg = String.format("AttributesFilterVisitor: invalid query, only one value for %s allowed per clause, but was: %s", ATT_NAME,
							this.fieldname + ", " + convertedFieldName);
					LOGGER.error(msg);
					throw new ODataApplicationException(msg, HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
				}

				this.fieldname = convertedFieldName;
				this.filterStack.push(this.buildFilter(rightOperand, operator, leftOperand));
			} else if ("att/Value".equals(leftOperand)) {
				final String value = rightOperand;
				if (null != operator && StringUtil.isNotEmpty(value)) {
					this.filterStack.push(this.buildFilter(value, operator));
				} else {
					final String msg = String.format("AttributesFilterVisitor: incomplete value expression (missing operator or operand): %s %s %s", ATT_NAME,
							operator != null ? operator : "[MISSING OPERATOR]", StringUtil.isNotEmpty(value) ? value : "[MISSING VALUE]");
					LOGGER.error(msg);
					throw new ODataApplicationException(msg, HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
				}
			} else if ("att/Value".equals(rightOperand)) {
				final String value = leftOperand;
				if (null != operator && StringUtil.isNotEmpty(value)) {
					this.filterStack.push(this.buildFilter(value, operator));
				} else {
					final String msg = String.format("AttributesFilterVisitor: incomplete value expression (missing operator or operand): %s %s %s", ATT_NAME,
							operator != null ? operator : "[MISSING OPERATOR]", StringUtil.isNotEmpty(value) ? value : "[MISSING VALUE]");
					LOGGER.error(msg);
					throw new ODataApplicationException(msg, HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
				}
			} else if ((ATT_NAME.equals(leftOperand) || ATT_NAME.equals(rightOperand)) && operator != BinaryOperatorKind.EQ) {
				final String msg = String.format("AttributesFilterVisitor: unsupported operator '%s' for '%s': only '%s' allowed", operator, ATT_NAME,
						BinaryOperatorKind.EQ);
				LOGGER.error(msg);
				throw new ODataApplicationException(msg, HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
			}
		}
		}

		return null;
	}

	private static String removeQuotes(String str) {
		return StringUtil.removeTrailing(StringUtil.removeLeading(str, "'", "\""), "'","\"");
	}
	
	private PripQueryFilterTerm buildFilter(String value, BinaryOperatorKind op) throws ExpressionVisitException {
		return this.buildFilter(PLACEHOLDER, op, value);
	}

	private PripQueryFilterTerm buildFilter(final String fieldName, final BinaryOperatorKind op, final String value) throws ExpressionVisitException {

		if (ATT_NAME.equals(fieldName)) {
			return new PripTextFilter(fieldName, PripTextFilter.Function.fromString(op.name()), value);
		}

		final PripQueryFilterTerm filter;
		try {
			switch (this.type) {
			case "string":
				filter = new PripTextFilter(fieldName, PripTextFilter.Function.fromString(op.name()), value);
				break;
			case "date":
				filter = new PripDateTimeFilter(fieldName, PripRangeValueFilter.RelationalOperator.fromString(op.name()),
						ProductsFilterVisitor.convertToLocalDateTime(value));
				break;
			case "long":
				filter = new PripIntegerFilter(fieldName, PripRangeValueFilter.RelationalOperator.fromString(op.name()), Long.valueOf(value));
				break;
			case "double":
				filter = new PripDoubleFilter(fieldName, PripRangeValueFilter.RelationalOperator.fromString(op.name()), Double.valueOf(value));
				break;
			case "boolean":
				filter = new PripBooleanFilter(fieldName, PripBooleanFilter.Function.fromString(op.name()), Boolean.valueOf(value));
				break;
			default:
				throw new ExpressionVisitException("unsupported type: " + this.type);
			}
		} catch (PripFilterOperatorException e) {
			throw new ExpressionVisitException(e.getMessage());
		}

		return filter;
	}

	@Override
	public Object visitUnaryOperator(UnaryOperatorKind operator, Object operand)
			throws ExpressionVisitException, ODataApplicationException {		
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitMethodCall(MethodKind methodCall, List<Object> parameters)
			throws ExpressionVisitException, ODataApplicationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression)
			throws ExpressionVisitException, ODataApplicationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
		return literal;
	}

	@Override
	public Object visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
		return member;
	}

	@Override
	public Object visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLambdaReference(String variableName) throws ExpressionVisitException, ODataApplicationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitEnum(EdmEnumType type, List<String> enumValues)
			throws ExpressionVisitException, ODataApplicationException {
		throw new UnsupportedOperationException();
	}
	
	// --------------------------------------------------------------------------

	public PripQueryFilter getFilter() throws ExpressionVisitException, ODataApplicationException {
		if (this.filterStack.size() == 1) {
			final PripQueryFilter filter = this.filterStack.pop();
			LOGGER.trace(String.format("AttributesFilterVisitor returns (unconverted): %s", filter));

			if (isFilterTerm(filter)) { // one term is not enough, we need at least a list with two terms (attribute name + attribute value)
				final String msg = "AttributesFilterVisitor: incomplete query, attribute name or value missing";
				LOGGER.error(msg);
				throw new ODataApplicationException(msg, HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
			}

			return this.convertFilterStructure((PripQueryFilterList) filter);
		} else {
			final String reason = this.filterStack.isEmpty() ? "empty filter stack after traversing expression tree"
					: "more than one result on filter stack after traversing expression tree";
			final String msg = "AttributesFilterVisitor: incomplete filter expression, " + reason;
			LOGGER.error(msg);
			throw new ODataApplicationException(msg, HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
		}
	}

	private PripQueryFilter convertFilterStructure(final PripQueryFilterList parentFilterList) throws ODataApplicationException {
		final List<PripQueryFilter> copyList = new ArrayList<>();
		final List<PripQueryFilter> childFilters = parentFilterList.getFilterList();

		for (final PripQueryFilter childFilter : CollectionUtil.nullToEmpty(childFilters)) {
			if (isFilterList(childFilter)) {
				final PripQueryFilterList childFilterList = (PripQueryFilterList) childFilter;

				// dig deeper
				final PripQueryFilter filtersFromDeeperLevels = this.convertFilterStructure(childFilterList);
				if (null != filtersFromDeeperLevels) {
					copyList.add(filtersFromDeeperLevels);
				}

			} else if (isFilterTerm(childFilter)) {
				final PripQueryFilterTerm childFilterTerm = (PripQueryFilterTerm) childFilter;

				if (!ATT_NAME.equals(childFilterTerm.getFieldName())) { // don't copy att/Name filters, we just use their value stored in this.fieldname
					final PripQueryFilterTerm copy = childFilterTerm.copy();

					if (PLACEHOLDER.equals(copy.getFieldName())) { // replace with real field name
						copy.setFieldName(this.fieldname);
					}

					copyList.add(copy);
				}
			}
		}

		if (copyList.isEmpty()) {
			return null;
		} else if (1 == copyList.size()) {
			return copyList.get(0); // direct return = the filter subtree gets shortened
		} else if (LogicalOperator.OR == parentFilterList.getOperator()) {
			return PripQueryFilterList.matchAny(copyList);
		} else if (LogicalOperator.AND == parentFilterList.getOperator()) {
			return PripQueryFilterList.matchAll(copyList);
		} else {
			throw new ODataApplicationException(String.format("unsupported operator: %s", parentFilterList.getOperator()),
					HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
		}
	}

	private static boolean isFilterList(final PripQueryFilter filter) {
		return filter instanceof PripQueryFilterList;
	}

	private static boolean isFilterTerm(final PripQueryFilter filter) {
		return filter instanceof PripQueryFilterTerm;
	}

}
