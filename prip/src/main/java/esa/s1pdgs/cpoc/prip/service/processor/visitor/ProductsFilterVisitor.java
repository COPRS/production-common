package esa.s1pdgs.cpoc.prip.service.processor.visitor;

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

import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter.Operator;
import esa.s1pdgs.cpoc.prip.model.PripTextFilter;

public class ProductsFilterVisitor implements ExpressionVisitor<Object> {

	private List<PripDateTimeFilter> pripDateTimeFilters;
	private List<PripTextFilter> pripTextFilters;

	public ProductsFilterVisitor() {
		pripDateTimeFilters = new ArrayList<>();
		pripTextFilters = new ArrayList<>();
	}

	public List<PripDateTimeFilter> getPripDateTimeFilters() {
		return pripDateTimeFilters;
	}

	public List<PripTextFilter> getPripTextFilters() {
		return pripTextFilters;
	}

	@Override
	public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, Object right)
			throws ExpressionVisitException, ODataApplicationException {
		switch (operator) {
		case AND: break;
		case GT:
			PripDateTimeFilter pripDateTimefilter1 = new PripDateTimeFilter();
			// one side must be CreationDate, the other a literal
			if (left instanceof Member && memberText((Member) left).equals("CreationDate") && right instanceof Literal) {
				pripDateTimefilter1.setDateTime(DateUtils.parse(((Literal)right).getText()));
				pripDateTimefilter1.setOperator(Operator.GT);
			} else if (right instanceof Member && memberText((Member) right).equals("CreationDate") && left instanceof Literal) {				
				pripDateTimefilter1.setDateTime(DateUtils.parse(((Literal)left).getText()));
				pripDateTimefilter1.setOperator(Operator.LT);
			} else {
				throw new ExpressionVisitException("Invalid expression");
			}
			pripDateTimeFilters.add(pripDateTimefilter1);
			break;
		case LT:
			PripDateTimeFilter pripDateTimefilter2 = new PripDateTimeFilter();
			// one side must be CreationDate, the other a literal
			if (left instanceof Member && memberText((Member) left).equals("CreationDate") && right instanceof Literal) {
				pripDateTimefilter2.setDateTime(DateUtils.parse(((Literal)right).getText()));
				pripDateTimefilter2.setOperator(Operator.LT);
			} else if (right instanceof Member && memberText((Member) right).equals("CreationDate") && left instanceof Literal) {
				pripDateTimefilter2.setDateTime(DateUtils.parse(((Literal)left).getText()));
				pripDateTimefilter2.setOperator(Operator.GT);
			} else {
				throw new ExpressionVisitException("Invalid expression");
			}
			pripDateTimeFilters.add(pripDateTimefilter2);
			break;
		default:
			throw new UnsupportedOperationException();
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
		if (methodCall.name().equals("CONTAINS") || methodCall.name().equals("STARTSWITH")) {
			if (parameters.size() == 2 && parameters.get(0) instanceof Member
					&& memberText((Member) parameters.get(0)).equals("Name") && parameters.get(1) instanceof Literal) {
				PripTextFilter textFilter = new PripTextFilter();
				if (methodCall.name().equals("CONTAINS")) {
					textFilter.setFunction(PripTextFilter.Function.CONTAINS);
				} else if (methodCall.name().equals("STARTSWITH")) {
					textFilter.setFunction(PripTextFilter.Function.STARTS_WITH);
				}
				String s = ((Literal) parameters.get(1)).getText();
				textFilter.setText(s.substring(1, s.length() - 1));
				pripTextFilters.add(textFilter);
			} else {
				throw new ExpressionVisitException("Invalid expression");
			}
		}
		return null;
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

	private String memberText(Member member) {
		return member.getResourcePath().getUriResourceParts().get(0).getSegmentValue();
	}

}
