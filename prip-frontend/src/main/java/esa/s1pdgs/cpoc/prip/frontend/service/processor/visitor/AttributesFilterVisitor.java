package esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor;

import java.util.ArrayList;
import java.util.Collections;
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

import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDoubleFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripFilterOperatorException;
import esa.s1pdgs.cpoc.prip.model.filter.PripIntegerFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterTerm;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter.Function;

public class AttributesFilterVisitor implements ExpressionVisitor<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductsFilterVisitor.class);
	
	private String type = "";
	private List<String> fieldNames = new ArrayList<String>();
	
	private final List<PripQueryFilterTerm> filters = new ArrayList<>();
	
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
		
		String value = null;
		
		if ("att/Name".equals(leftOperand) && operator == BinaryOperatorKind.EQ) {
			String fieldName = "attr_" + removeQuotes(rightOperand) + "_" + type;
			if (!fieldNames.contains(fieldName)) {
				fieldNames.add(fieldName);
			}
		} else if ("att/Name".equals(rightOperand) && operator == BinaryOperatorKind.EQ) {
			String fieldName = "attr_" + removeQuotes(leftOperand)  + "_" + type;
			if (!fieldNames.contains(fieldName)) {
				fieldNames.add(fieldName);
			}
		} else if ("att/Value".equals(leftOperand)) {
			value = rightOperand;
			if (null != operator && StringUtil.isNotEmpty(value)) {
				filters.add(buildFilter(value, operator));
			}
		} else if ("att/Value".equals(rightOperand)) {
			value = leftOperand;
			if (null != operator && StringUtil.isNotEmpty(value)) {
				filters.add(buildFilter(value, operator));
			}
		}

		return null;
	}
	
	private static String removeQuotes(String str) {
		return StringUtil.removeTrailing(StringUtil.removeLeading(str, "'", "\""), "'","\"");
	}
	
	private PripQueryFilterTerm buildFilter(String value, BinaryOperatorKind op) throws ExpressionVisitException {
		PripQueryFilterTerm filter = null;
		try {
			switch (this.type) {
			case "string":
				filter = new PripTextFilter("placeholder", PripTextFilter.Function.fromString(op.name()), value);
				break;
			case "date":
				filter = new PripDateTimeFilter("placeholder", PripRangeValueFilter.RelationalOperator.fromString(op.name()),
						ProductsFilterVisitor.convertToLocalDateTime(value));
				break;
			case "long":
				filter = new PripIntegerFilter("placeholder", PripRangeValueFilter.RelationalOperator.fromString(op.name()),
						Long.valueOf(value));
				break;
			case "double":
				filter = new PripDoubleFilter("placeholder", PripRangeValueFilter.RelationalOperator.fromString(op.name()),
						Double.valueOf(value));
				break;
			case "boolean":
				filter = new PripBooleanFilter("placeholder", PripBooleanFilter.Function.fromString(op.name()),
						Boolean.valueOf(value));
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

	public List<PripQueryFilterTerm> getFilters() {
		if (fieldNames.size() == 0) {
			return Collections.emptyList();
		} else if (fieldNames.size() == 1) {
			String fieldName = fieldNames.get(0);
			for (PripQueryFilterTerm filter: filters) {
				filter.setFieldName(fieldName);				
			}
			return filters;
		} else {
			return Collections.singletonList(new PripTextFilter("productFamily", Function.EQUALS, "'n/a'"));
		}
	}

}
