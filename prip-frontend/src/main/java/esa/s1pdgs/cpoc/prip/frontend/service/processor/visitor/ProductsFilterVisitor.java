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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceLambdaAny;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
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
import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter.Operator;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter.Function;

public class ProductsFilterVisitor implements ExpressionVisitor<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductsFilterVisitor.class);

	private static final Map<String,FIELD_NAMES> PRIP_DATETIME_PROPERTY_FIELD_NAMES;	
	private static final Map<String,FIELD_NAMES> PRIP_TEXT_PROPERTY_FIELD_NAMES;
	private static final Map<String,FIELD_NAMES> PRIP_SUPPORTED_PROPERTY_FIELD_NAMES;
	private static final List<String> PRIP_PRODUCTION_TYPES;
	private static final List<MethodKind> SUPPORTED_METHODS;
	
	static {
		PRIP_DATETIME_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(PublicationDate.name(), FIELD_NAMES.CREATION_DATE);
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(ContentDate.name() + "/" + Start.name(), FIELD_NAMES.CONTENT_DATE_START);
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(ContentDate.name() + "/" + End.name(), FIELD_NAMES.CONTENT_DATE_END);
		
		PRIP_TEXT_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_TEXT_PROPERTY_FIELD_NAMES.put(Name.name(), FIELD_NAMES.NAME);
		PRIP_TEXT_PROPERTY_FIELD_NAMES.put(ProductionType.name(), FIELD_NAMES.PRODUCTION_TYPE);
		
		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.putAll(PRIP_DATETIME_PROPERTY_FIELD_NAMES);
		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.putAll(PRIP_TEXT_PROPERTY_FIELD_NAMES);
		
		PRIP_PRODUCTION_TYPES = Arrays.asList(esa.s1pdgs.cpoc.prip.model.ProductionType.values()).stream()
				.map(v -> v.getName()).collect(Collectors.toList());
		
		SUPPORTED_METHODS = Arrays.asList(MethodKind.CONTAINS, MethodKind.STARTSWITH, MethodKind.ENDSWITH);
	}
	
	private final List<PripQueryFilter> queryFilters = new ArrayList<>();

	public ProductsFilterVisitor() {
	}
	
	@Override
	public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, Object right)
			throws ExpressionVisitException, ODataApplicationException {

		final String leftOperand = operandToString(left);
		final String rightOperand = operandToString(right);

		LOGGER.debug("got left operand: {} operator: {} right operand: {}", leftOperand, operator, rightOperand);
		
		switch (operator) {
		case AND:
			break;
		case GT:
		case GE:
		case LT:
		case LE:
		{
			final PripDateTimeFilter pripDateTimefilter = createDateTimeFilter(Operator.fromString(operator.name()),
					left, right, leftOperand, rightOperand);
			this.queryFilters.add(pripDateTimefilter);
			LOGGER.debug("using filter: {} ", pripDateTimefilter);
			break;
		}
		case EQ:
		{
			final PripTextFilter textFilter = createTextFilter(Function.fromString(operator.name()), left, right,
					leftOperand, rightOperand);
			if (null == textFilter) {
				return null;
			}
			this.queryFilters.add(textFilter);
			LOGGER.debug("using filter: {} ", textFilter);
			break;
		}
		default:
			throw new UnsupportedOperationException("Operator " + operator + " not supported!");
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

		if (!SUPPORTED_METHODS.contains(methodCall)) {
			return null;
		}

		LOGGER.debug("got method {}", methodCall.name());

		if (parameters.size() != 2 || !(parameters.get(0) instanceof Member)
				|| !(parameters.get(1) instanceof Literal)) {
			throw new ODataApplicationException(
					"Invalid or unsupported parameter(s): " + StringUtil.makeListString(",", parameters),
					HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
		}

		final Member field = (Member) parameters.get(0);
		final String odataFieldname = this.memberText(field);

		if (!isTextField(odataFieldname)) {
			throw new ODataApplicationException("Unsupported field name: " + odataFieldname,
					HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
		}

		final String pripFieldName = mapToPripFieldName(odataFieldname).orElse(null); // we already checked if text field
		final Function filterFunction = PripTextFilter.Function.fromString(methodCall.name());
		final String literal = ((Literal) parameters.get(1)).getText();
		final String text = literal.substring(1, literal.length() - 1);
		final PripTextFilter textFilter = new PripTextFilter(pripFieldName, filterFunction, text);

		this.queryFilters.add(textFilter);
		LOGGER.debug("using filter: {} ", textFilter);

		return null;
	}

	@Override
	public Object visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression)
			throws ExpressionVisitException, ODataApplicationException {

		if ("ANY".equals(lambdaFunction)) {
			final String type = "string"; // TODO: get correct type...
			final AttributesFilterVisitor filterExpressionVisitor = new AttributesFilterVisitor(type);
			final Object visitResult = expression.accept(filterExpressionVisitor);
			System.out.println("visit result: " + visitResult);
			return visitResult;
		}

		throw new ODataApplicationException("Unsupported lambda expression on filter expression",
				HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
	}

	@Override
	public Object visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
		return literal;
	}

	@Override
	public Object visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
		String type = "*";
		for (UriResource uriResource : member.getResourcePath().getUriResourceParts()) {
			if (uriResource instanceof UriResourceNavigation) {				
				UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) uriResource;
				String tmp = uriResourceNavigation.getSegmentValue(true);
				switch(tmp.substring(tmp.lastIndexOf(".") + 1)) {
					case EdmProvider.ET_STRING_ATTRIBUTE_NAME: type = "string"; break;
					case EdmProvider.ET_INTEGER_ATTRIBUTE_NAME: type = "long"; break;
					case EdmProvider.ET_DOUBLE_ATTRIBUTE_NAME: type = "double"; break;
					case EdmProvider.ET_BOOLEAN_ATTRIBUTE_NAME: type = "boolean"; break;
					case EdmProvider.ET_DATE_ATTRIBUTE_NAME: type = "date"; break;
					default:
				}
				System.out.println("Type: " +  type);
			} else if (uriResource instanceof UriResourceLambdaAny) {
				final UriResourceLambdaAny any = (UriResourceLambdaAny) uriResource;
				List<PripQueryFilter> filters = (List<PripQueryFilter>)visitLambdaExpression(
						"ANY", any.getLambdaVariable(), any.getExpression());
				System.out.println("FILTERS TO ADD: " + filters); // TODO add filters to other filters...
			}
		}
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
		if (ProductionType.name().equals(type.getName()) && PRIP_PRODUCTION_TYPES.contains(enumValues.get(0))) {
			acceptedEnumsValues.add(enumValues.get(0));
		}
		return acceptedEnumsValues;
	}
	
	public static String operandToString(Object operand) {
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

	public static LocalDateTime convertToLocalDateTime(String datetime) throws ExpressionVisitException {
		try {
			Instant instant = Instant.ofEpochMilli(EdmDateTimeOffset.getInstance().valueOfString(datetime, false, 0, 1000, 0, false, Long.class));
			return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		} catch (EdmPrimitiveTypeException e) {
			throw new ExpressionVisitException("Invalid or unsupported operand");
		}
	}
	
	public List<PripQueryFilter> getQueryFilters() {
		return this.queryFilters;
	}
	
	public static String memberText(Member member) {
		String text = "";
		List<UriResource> uriResourceParts = member.getResourcePath().getUriResourceParts();
		for (int idx = 0; idx < uriResourceParts.size(); idx++) {
			text += (idx > 0 ? "/" : "") + uriResourceParts.get(idx).getSegmentValue();
		}
		return text;
	}
	
	private static boolean isDateField(String odataFieldName) {
		return PRIP_DATETIME_PROPERTY_FIELD_NAMES.containsKey(odataFieldName);
	}

	private static boolean isTextField(String odataFieldName) {
		return PRIP_TEXT_PROPERTY_FIELD_NAMES.containsKey(odataFieldName);
	}
	
	private static Optional<String> mapToPripFieldName(String odataFieldName) {
		if (PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.containsKey(odataFieldName)) {
			return Optional.of(PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.get(odataFieldName).fieldName());
		}

		return Optional.empty();
	}
	
	private static PripDateTimeFilter createDateTimeFilter(Operator operator, Object left, Object right,
			String leftOperand, String rightOperand) throws ODataApplicationException, ExpressionVisitException {
		
		if (left instanceof Member && right instanceof Literal) {
			if (isDateField(leftOperand)) {
				return new PripDateTimeFilter(mapToPripFieldName(leftOperand).orElse(null), operator,
						convertToLocalDateTime(rightOperand));
			}
		} else if (left instanceof Literal && right instanceof Member) {
			if (isDateField(rightOperand)) {
				return new PripDateTimeFilter(mapToPripFieldName(rightOperand).orElse(null), operator.getInverse(),
						convertToLocalDateTime(leftOperand));
			}
		}

		throw new ODataApplicationException(
				"Invalid or unsupported operand(s): " + leftOperand + " " + operator.getOperator() + " " + rightOperand,
				HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
	}
	
	private static PripTextFilter createTextFilter(Function function, Object left, Object right,
			String leftOperand, String rightOperand) throws ODataApplicationException {
		
		if (left instanceof Member && right instanceof Literal) {
			if (isTextField(leftOperand)) {
				return new PripTextFilter(mapToPripFieldName(leftOperand).orElse(null), function,
						rightOperand.substring(1, rightOperand.length() - 1));
			}
		} else if(left instanceof Literal && right instanceof Member) {
			if (isTextField(rightOperand)) {
				return new PripTextFilter(mapToPripFieldName(rightOperand).orElse(null), function,
						leftOperand.substring(1, leftOperand.length() - 1));
			}
		} else if (left instanceof Member && right instanceof List || left instanceof List && right instanceof Member) {
			final String memberText = left instanceof Member ? leftOperand : rightOperand;
			final String firstListElementText = left instanceof List ? leftOperand : rightOperand;
			
			// BEGIN WORKAROUND TO RETURN EMPTY RESULT FOR ProductionType != systematic_production
			if (ProductionType.name().equals("ProductionType") && !SYSTEMATIC_PRODUCTION.getName().equals(firstListElementText)) {
				return new PripTextFilter(FIELD_NAMES.NAME.fieldName(), Function.EQUALS, "NOTEXISTINGPRODUCTNAME");
			} else {
				return null;
			}
			// END WORKAROUND TO RETURN EMPTY RESULT FOR ProductionType != systematic_production
		}
		
		throw new ODataApplicationException(
				"Invalid or unsupported operand(s): " + leftOperand + " " + function.getFunctionName() + " " + rightOperand,
				HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
	}

}
