package esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
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

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDoubleFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripIntegerFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;

public class AttributesFilterVisitor implements ExpressionVisitor<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductsFilterVisitor.class);
	
	private String type = "";
	private String fieldName = "";
	private String value = "";
	private BinaryOperatorKind op = null;
	
	private int count=0; // TODO remove; just for testing purposes
	
	private final List<PripQueryFilter> filters = new ArrayList<>();
	
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
		LOGGER.debug("got left operand: {} operator: {} right operand: {}", leftOperand, operator, rightOperand);
		
		System.out.println(String.format("AttributesFilterVisitor.visitBinaryOperator: %s %s %s", leftOperand, operator, rightOperand));
		
		if ("att/Name".equals(leftOperand)) {
			this.fieldName = "attr_" + removeQuotes(rightOperand) + "_" + type;
		} else if ("att/Name".equals(rightOperand)) {
			this.fieldName = "attr_" + removeQuotes(leftOperand)  + "_" + type;
		}
		
		if ("att/Value".equals(leftOperand)) {
			value = rightOperand;
			op = operator;
		} else if ("att/Value".equals(rightOperand)) {
			value = leftOperand;
			op = operator;
		}
		
		System.out.println("count: " + ++this.count);
		System.out.println("type: " + this.type);
		System.out.println("fieldname: " + this.fieldName);
		System.out.println("operator: " + this.op);
		System.out.println("value: " + this.value);
		
		if (StringUtil.isNotEmpty(this.fieldName) && null != this.op && StringUtil.isNotEmpty(this.value)) {
			final PripQueryFilter filter = this.buildFilter();
			
			if (null != filter && !this.filters.contains(filter)) {
				this.filters.add(filter);
			}
			System.out.println("filters: " + this.filters);
		}

		return null;
	}
	
	private static String removeQuotes(String str) {
		return StringUtil.removeTrailing(StringUtil.removeLeading(str, "'", "\""), "'","\"");
	}
	
	private PripQueryFilter buildFilter() throws ExpressionVisitException {
		System.out.println(String.format("build %s filter: %s %s %s", this.type, this.fieldName,
				(null != this.op ? this.op.name() : null), this.value));
		// TODO not all operators work for all types/filters; throw bad request?
		PripQueryFilter filter = null;
		switch (this.type) {
		case "string":
			filter = new PripTextFilter(this.fieldName, PripTextFilter.Function.fromString(this.op.name()), this.value);
			break;
		case "date":
			filter = new PripDateTimeFilter(this.fieldName, PripRangeValueFilter.Operator.fromString(this.op.name()),
					ProductsFilterVisitor.convertToLocalDateTime(this.value));
			break;
		case "long":
			filter = new PripIntegerFilter(this.fieldName, PripRangeValueFilter.Operator.fromString(this.op.name()),
					Long.valueOf(this.value));
			break;
		case "double":
			filter = new PripDoubleFilter(this.fieldName, PripRangeValueFilter.Operator.fromString(this.op.name()),
					Double.valueOf(this.value));
			break;
		case "boolean":
			filter = new PripBooleanFilter(this.fieldName, PripBooleanFilter.Function.fromString(this.op.name()),
					Boolean.valueOf(this.value));
			break;
		default:
			break;
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
		System.out.println("AttributesFilterVisitor.visitLiteral: " + literal);
		return literal;
	}

	@Override
	public Object visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
		System.out.println("AttributesFilterVisitor.visitMember: " + member);
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

	public List<PripQueryFilter> getFilters() {
		return this.filters;
	}

}
