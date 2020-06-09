package esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor;

import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.End;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Name;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.PublicationDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Start;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
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

import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.PripDateTimeFilter.Operator;
import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;
import esa.s1pdgs.cpoc.prip.model.PripTextFilter;
import esa.s1pdgs.cpoc.prip.model.PripTextFilter.Function;

public class ProductsFilterVisitor implements ExpressionVisitor<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductsFilterVisitor.class);

	private List<PripDateTimeFilter> pripDateTimeFilters;
	private List<PripTextFilter> pripTextFilters;
	
	private Map<String,FIELD_NAMES> pripDateTimePropertyFieldNames;	
	private Map<String,FIELD_NAMES> pripTextPropertyFieldNames;	

	public ProductsFilterVisitor() {
		pripDateTimeFilters = new ArrayList<>();
		pripTextFilters = new ArrayList<>();
		
		pripDateTimePropertyFieldNames = new HashMap<>();
		pripDateTimePropertyFieldNames.put(PublicationDate.name(), FIELD_NAMES.CREATION_DATE);
		pripDateTimePropertyFieldNames.put(ContentDate.name() + "/" + Start.name(), FIELD_NAMES.CONTENT_DATE_START);
		pripDateTimePropertyFieldNames.put(ContentDate.name() + "/" + End.name(), FIELD_NAMES.CONTENT_DATE_END);

		pripTextPropertyFieldNames = new HashMap<>();
		pripTextPropertyFieldNames.put(Name.name(), FIELD_NAMES.NAME);
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

		String leftOperand = "";
		String rightOperand = "";

		if (left instanceof Member) {
			leftOperand = memberText((Member) left);
		} else if (left instanceof Literal) {
			leftOperand = ((Literal) left).getText();
		}

		if (right instanceof Member) {
			rightOperand = memberText((Member) right);
		} else if (right instanceof Literal) {
			rightOperand = ((Literal) right).getText();
		}

		LOGGER.debug("got left operand: {} operator: {} right operand: {} ", leftOperand, operator, rightOperand);

		switch (operator) {
		case AND:
			break;
		case GT://leftOperand = "ContentDate/Start"; /////////////////////////////////////////////////// DEBUG
			PripDateTimeFilter pripDateTimeFilter1 = new PripDateTimeFilter();
			// one side must be a DateTime Field, the other a literal
			if (left instanceof Member && pripDateTimePropertyFieldNames.containsKey(leftOperand) && right instanceof Literal) {
				pripDateTimeFilter1.setDateTime(convertToLocalDateTime(rightOperand));
				pripDateTimeFilter1.setOperator(Operator.GT);
				pripDateTimeFilter1.setFieldName(pripDateTimePropertyFieldNames.get(leftOperand));
			} else if (right instanceof Member && pripDateTimePropertyFieldNames.containsKey(rightOperand) && left instanceof Literal) {
				pripDateTimeFilter1.setDateTime(convertToLocalDateTime(leftOperand));
				pripDateTimeFilter1.setOperator(Operator.LT);
				pripDateTimeFilter1.setFieldName(pripDateTimePropertyFieldNames.get(rightOperand));
			} else {
				throw new ExpressionVisitException("Invalid or unsupported operand");
			}
			pripDateTimeFilters.add(pripDateTimeFilter1);
			LOGGER.debug("using filter {} ", pripDateTimeFilter1);
			break;
		case LT:
			PripDateTimeFilter pripDateTimeFilter2 = new PripDateTimeFilter();
			// one side must be DateTime Field, the other a literal
			if (left instanceof Member && pripDateTimePropertyFieldNames.containsKey(leftOperand) && right instanceof Literal) {
				pripDateTimeFilter2.setDateTime(convertToLocalDateTime(rightOperand));
				pripDateTimeFilter2.setOperator(Operator.LT);
				pripDateTimeFilter2.setFieldName(pripDateTimePropertyFieldNames.get(leftOperand));
			} else if (right instanceof Member && pripDateTimePropertyFieldNames.containsKey(rightOperand) && left instanceof Literal) {
				pripDateTimeFilter2.setDateTime(convertToLocalDateTime(leftOperand));
				pripDateTimeFilter2.setOperator(Operator.GT);
				pripDateTimeFilter2.setFieldName(pripDateTimePropertyFieldNames.get(rightOperand));
			} else {
				throw new ExpressionVisitException("Invalid or unsupported operand");
			}
			pripDateTimeFilters.add(pripDateTimeFilter2);
			LOGGER.debug("using filter {} ", pripDateTimeFilter2);
			break;
		case EQ:
			PripTextFilter textFilter = new PripTextFilter();
			if (left instanceof Member && right instanceof Literal) {
				textFilter.setFunction(Function.EQUALS);
				textFilter.setText(rightOperand);
				
			} else {
				throw new ExpressionVisitException("Invalid or unsupported operand");
			}
			pripTextFilters.add(textFilter);
			LOGGER.debug("using filter {} ", textFilter);
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

		if (!methodCall.equals(MethodKind.CONTAINS) && !methodCall.equals(MethodKind.STARTSWITH)) {
			return null;
		}
		
		LOGGER.debug("got method {}", methodCall.name());
		
		if (parameters.size() == 2 && parameters.get(0) instanceof Member && parameters.get(1) instanceof Literal
				&& pripTextPropertyFieldNames.containsKey(memberText((Member) parameters.get(0))) ) {
			
			FIELD_NAMES fieldName = pripTextPropertyFieldNames.get(memberText((Member) parameters.get(0)));
			PripTextFilter textFilter = new PripTextFilter();
			textFilter.setFieldName(fieldName);
			if (methodCall.equals(MethodKind.CONTAINS)) {
				textFilter.setFunction(PripTextFilter.Function.CONTAINS);
			} else if (methodCall.equals(MethodKind.STARTSWITH)) {
				textFilter.setFunction(PripTextFilter.Function.STARTS_WITH);
			}
			String s = ((Literal) parameters.get(1)).getText();
			textFilter.setText(s.substring(1, s.length() - 1));
			pripTextFilters.add(textFilter);
			LOGGER.debug("using filter {} ", textFilter);
			
		} else {
			throw new ExpressionVisitException("Invalid or unsupported parameter");
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
		String text = "";
		List<UriResource> uriResourceParts = member.getResourcePath().getUriResourceParts();
		for (int idx = 0; idx < uriResourceParts.size(); idx++) {
			text += (idx > 0 ? "/" : "") + uriResourceParts.get(idx).getSegmentValue();
		}
		return text;
	}
	
	public static LocalDateTime convertToLocalDateTime(String datetime) throws ExpressionVisitException {
		try {
			Instant instant = Instant.ofEpochMilli(EdmDateTimeOffset.getInstance().valueOfString(datetime, false, 0, 1000, 0, false, Long.class));
			return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		} catch (EdmPrimitiveTypeException e) {
			throw new ExpressionVisitException("Invalid or unsupported operand");
		}
	}

}
