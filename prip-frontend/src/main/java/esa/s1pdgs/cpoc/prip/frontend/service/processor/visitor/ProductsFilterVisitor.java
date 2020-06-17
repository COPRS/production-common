package esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor;

import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.End;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Name;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ProductionType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.PublicationDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Start;
import static esa.s1pdgs.cpoc.prip.model.ProductionType.SYSTEMATIC_PRODUCTION;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	private List<MethodKind> supportedMethods;
	
	private List<PripDateTimeFilter> pripDateTimeFilters;
	private List<PripTextFilter> pripTextFilters;
	
	private Map<String,FIELD_NAMES> pripDateTimePropertyFieldNames;	
	private Map<String,FIELD_NAMES> pripTextPropertyFieldNames;
	
	private List<String> pripProductionTypes;

	public ProductsFilterVisitor() {

		supportedMethods = Arrays.asList(MethodKind.CONTAINS, MethodKind.STARTSWITH);
		
		pripDateTimeFilters = new ArrayList<>();
		pripTextFilters = new ArrayList<>();

		pripDateTimePropertyFieldNames = new HashMap<>();
		pripDateTimePropertyFieldNames.put(PublicationDate.name(), FIELD_NAMES.CREATION_DATE);
		pripDateTimePropertyFieldNames.put(ContentDate.name() + "/" + Start.name(), FIELD_NAMES.CONTENT_DATE_START);
		pripDateTimePropertyFieldNames.put(ContentDate.name() + "/" + End.name(), FIELD_NAMES.CONTENT_DATE_END);

		pripTextPropertyFieldNames = new HashMap<>();
		pripTextPropertyFieldNames.put(Name.name(), FIELD_NAMES.NAME);
		pripTextPropertyFieldNames.put(ProductionType.name(), FIELD_NAMES.PRODUCTION_TYPE);

		pripProductionTypes = Arrays.asList(esa.s1pdgs.cpoc.prip.model.ProductionType.values()).stream()
				.map(v -> v.getName()).collect(Collectors.toList());
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

		String leftOperand = operandToString(left);
		String rightOperand = operandToString(right);

		LOGGER.debug("got left operand: {} operator: {} right operand: {} ", leftOperand, operator, rightOperand);

		switch (operator) {
		case AND:
			break;
		case GE:
			PripDateTimeFilter pripDateTimeFilter1 = new PripDateTimeFilter();
			// one side must be a DateTime Field, the other a literal
			if (left instanceof Member && pripDateTimePropertyFieldNames.containsKey(leftOperand) && right instanceof Literal) {
				pripDateTimeFilter1.setDateTime(convertToLocalDateTime(rightOperand));
				pripDateTimeFilter1.setOperator(Operator.GE);
				pripDateTimeFilter1.setFieldName(pripDateTimePropertyFieldNames.get(leftOperand));
			} else if (right instanceof Member && pripDateTimePropertyFieldNames.containsKey(rightOperand) && left instanceof Literal) {
				pripDateTimeFilter1.setDateTime(convertToLocalDateTime(leftOperand));
				pripDateTimeFilter1.setOperator(Operator.LE);
				pripDateTimeFilter1.setFieldName(pripDateTimePropertyFieldNames.get(rightOperand));
			} else {
				throw new ExpressionVisitException("Invalid or unsupported operand");
			}
			pripDateTimeFilters.add(pripDateTimeFilter1);
			LOGGER.debug("using filter {} ", pripDateTimeFilter1);
			break;
		case GT:
			PripDateTimeFilter pripDateTimeFilter2 = new PripDateTimeFilter();
			// one side must be a DateTime Field, the other a literal
			if (left instanceof Member && pripDateTimePropertyFieldNames.containsKey(leftOperand) && right instanceof Literal) {
				pripDateTimeFilter2.setDateTime(convertToLocalDateTime(rightOperand));
				pripDateTimeFilter2.setOperator(Operator.GT);
				pripDateTimeFilter2.setFieldName(pripDateTimePropertyFieldNames.get(leftOperand));
			} else if (right instanceof Member && pripDateTimePropertyFieldNames.containsKey(rightOperand) && left instanceof Literal) {
				pripDateTimeFilter2.setDateTime(convertToLocalDateTime(leftOperand));
				pripDateTimeFilter2.setOperator(Operator.LT);
				pripDateTimeFilter2.setFieldName(pripDateTimePropertyFieldNames.get(rightOperand));
			} else {
				throw new ExpressionVisitException("Invalid or unsupported operand");
			}
			pripDateTimeFilters.add(pripDateTimeFilter2);
			LOGGER.debug("using filter {} ", pripDateTimeFilter2);
			break;
		case LE:
			PripDateTimeFilter pripDateTimeFilter3 = new PripDateTimeFilter();
			// one side must be DateTime Field, the other a literal
			if (left instanceof Member && pripDateTimePropertyFieldNames.containsKey(leftOperand) && right instanceof Literal) {
				pripDateTimeFilter3.setDateTime(convertToLocalDateTime(rightOperand));
				pripDateTimeFilter3.setOperator(Operator.LE);
				pripDateTimeFilter3.setFieldName(pripDateTimePropertyFieldNames.get(leftOperand));
			} else if (right instanceof Member && pripDateTimePropertyFieldNames.containsKey(rightOperand) && left instanceof Literal) {
				pripDateTimeFilter3.setDateTime(convertToLocalDateTime(leftOperand));
				pripDateTimeFilter3.setOperator(Operator.GE);
				pripDateTimeFilter3.setFieldName(pripDateTimePropertyFieldNames.get(rightOperand));
			} else {
				throw new ExpressionVisitException("Invalid or unsupported operand");
			}
			pripDateTimeFilters.add(pripDateTimeFilter3);
			LOGGER.debug("using filter {} ", pripDateTimeFilter3);
			break;
		case LT:
			PripDateTimeFilter pripDateTimeFilter4 = new PripDateTimeFilter();
			// one side must be DateTime Field, the other a literal
			if (left instanceof Member && pripDateTimePropertyFieldNames.containsKey(leftOperand) && right instanceof Literal) {
				pripDateTimeFilter4.setDateTime(convertToLocalDateTime(rightOperand));
				pripDateTimeFilter4.setOperator(Operator.LT);
				pripDateTimeFilter4.setFieldName(pripDateTimePropertyFieldNames.get(leftOperand));
			} else if (right instanceof Member && pripDateTimePropertyFieldNames.containsKey(rightOperand) && left instanceof Literal) {
				pripDateTimeFilter4.setDateTime(convertToLocalDateTime(leftOperand));
				pripDateTimeFilter4.setOperator(Operator.GT);
				pripDateTimeFilter4.setFieldName(pripDateTimePropertyFieldNames.get(rightOperand));
			} else {
				throw new ExpressionVisitException("Invalid or unsupported operand");
			}
			pripDateTimeFilters.add(pripDateTimeFilter4);
			LOGGER.debug("using filter {} ", pripDateTimeFilter4);
			break;
		case EQ:
			PripTextFilter textFilter = new PripTextFilter();
			if (left instanceof Member && right instanceof Literal) {
				textFilter.setFunction(Function.EQUALS);
				textFilter.setText(rightOperand.substring(1, rightOperand.length() - 1));
				textFilter.setFieldName(pripTextPropertyFieldNames.get(leftOperand));
			} else if(left instanceof Literal && right instanceof Member) {
				textFilter.setFunction(Function.EQUALS);
				textFilter.setText(leftOperand.substring(1, leftOperand.length() - 1));
				textFilter.setFieldName(pripTextPropertyFieldNames.get(rightOperand));
			} else if (left instanceof Member && right instanceof List || left instanceof List && right instanceof Member) {
				String memberText = left instanceof Member ? leftOperand : rightOperand;
				String firstListElementText = left instanceof List ? leftOperand : rightOperand;
				// BEGIN WORKAROUND TO RETURN EMPTY RESULT FOR ProductionType != systematic_production
				if (ProductionType.name().equals("ProductionType") && !SYSTEMATIC_PRODUCTION.getName().equals(firstListElementText)) {
					textFilter.setFunction(Function.EQUALS);					
					textFilter.setText("NOTEXISTINGPRODUCTNAME");
					textFilter.setFieldName(FIELD_NAMES.NAME);
				} else {
					return null;
				}
				// END WORKAROUND TO RETURN EMPTY RESULT FOR ProductionType != systematic_production
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

	public String operandToString(Object operand) {
		String result = "";
		if (operand instanceof Member) {
			result = memberText((Member) operand);
		} else if (operand instanceof Literal) {
			result = ((Literal) operand).getText();
		} else if (operand instanceof List){
			List list = (List)operand;
			if (!list.isEmpty()) {
				Object first = list.get(0);
				if (first instanceof String) {
					result = (String)first;
				}
			}
		}
		return result;
	}

	@Override
	public Object visitUnaryOperator(UnaryOperatorKind operator, Object operand)
			throws ExpressionVisitException, ODataApplicationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitMethodCall(MethodKind methodCall, List<Object> parameters)
			throws ExpressionVisitException, ODataApplicationException {
		
		if (!supportedMethods.contains(methodCall)) {
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
		ArrayList<Object> acceptedEnumsValues = new ArrayList<>();
		if (ProductionType.name().equals(type.getName()) && pripProductionTypes.contains(enumValues.get(0))) {
			acceptedEnumsValues.add(enumValues.get(0));
		}
		return acceptedEnumsValues;
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
