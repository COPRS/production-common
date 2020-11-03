package esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor;

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

public class AttributesFilterVisitor implements ExpressionVisitor<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductsFilterVisitor.class);
	
	private String type = "";
	private String fieldName = "";
	private String value = "";
	private BinaryOperatorKind op = null;
	
	public AttributesFilterVisitor(String type) {
		this.type = type;
	}
	
	@Override
	public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, Object right)
			throws ExpressionVisitException, ODataApplicationException {

		final String leftOperand = ProductsFilterVisitor.operandToString(left);
		final String rightOperand = ProductsFilterVisitor.operandToString(right);
		LOGGER.debug("got left operand: {} operator: {} right operand: {}", leftOperand, operator, rightOperand);
		
		
		System.out.println(String.format("in converted: got left operand: %s operator: %s right operand: %s", leftOperand, operator, rightOperand));
		
		if ("att/Name".equals(leftOperand) && operator == BinaryOperatorKind.EQ) {
			fieldName = "attr_" + rightOperand + "_" + type;
		} else if ("att/Name".equals(rightOperand) && operator == BinaryOperatorKind.EQ) {
			fieldName = "attr_" + leftOperand + "_" + type;
		}
		
		if ("att/Value".equals("leftOperand")) {
			value = rightOperand;
			op = operator;
		} else if ("att/Value".equals("rightOperand")) {
			value = leftOperand;
			op = operator;
		}
	
		return null;
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
		System.out.println("literal:" + literal);
		return literal;
	}

	@Override
	public Object visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
		System.out.println("member:" + member);
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

}
