package esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor;

import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.End;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.EvictionDate;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceLambdaAll;
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
import org.apache.olingo.server.core.uri.UriResourceFunctionImpl;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties;
import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripGeometryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripIntegerFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterTerm;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter.RelationalOperator;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter.Function;

public class ProductsFilterVisitor implements ExpressionVisitor<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductsFilterVisitor.class);

	private static final Map<String, FIELD_NAMES> PRIP_DATETIME_PROPERTY_FIELD_NAMES;
	private static final Map<String, FIELD_NAMES> PRIP_TEXT_PROPERTY_FIELD_NAMES;
	private static final Map<String, FIELD_NAMES> PRIP_INTEGER_PROPERTY_FIELD_NAMES;
	private static final Map<String, FIELD_NAMES> PRIP_SUPPORTED_PROPERTY_FIELD_NAMES;
	private static final List<String> PRIP_PRODUCTION_TYPES;
	private static final List<MethodKind> SUPPORTED_METHODS;

	static {
		PRIP_DATETIME_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(PublicationDate.name(), FIELD_NAMES.CREATION_DATE);
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(EvictionDate.name(), FIELD_NAMES.EVICTION_DATE);
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(ContentDate.name() + "/" + Start.name(), FIELD_NAMES.CONTENT_DATE_START);
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(ContentDate.name() + "/" + End.name(), FIELD_NAMES.CONTENT_DATE_END);

		PRIP_TEXT_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_TEXT_PROPERTY_FIELD_NAMES.put(Name.name(), FIELD_NAMES.NAME);
		PRIP_TEXT_PROPERTY_FIELD_NAMES.put(ProductionType.name(), FIELD_NAMES.PRODUCTION_TYPE);

		PRIP_INTEGER_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_INTEGER_PROPERTY_FIELD_NAMES.put(EntityTypeProperties.ContentLength.name(), FIELD_NAMES.CONTENT_LENGTH);

		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.putAll(PRIP_DATETIME_PROPERTY_FIELD_NAMES);
		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.putAll(PRIP_TEXT_PROPERTY_FIELD_NAMES);
		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.putAll(PRIP_INTEGER_PROPERTY_FIELD_NAMES);

		PRIP_PRODUCTION_TYPES = Arrays.asList(esa.s1pdgs.cpoc.prip.model.ProductionType.values()).stream()
				.map(v -> v.getName()).collect(Collectors.toList());

		SUPPORTED_METHODS = Arrays.asList(MethodKind.CONTAINS, MethodKind.STARTSWITH, MethodKind.ENDSWITH,
				MethodKind.GEOINTERSECTS);
	}

	private final FilterStack filterStack = new FilterStack();

	public ProductsFilterVisitor() {
	}

	@Override
	public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, Object right)
			throws ExpressionVisitException, ODataApplicationException {

		final String leftOperand = operandToString(left);
		final String rightOperand = operandToString(right);

		LOGGER.debug("got: {} {} {}", leftOperand, operator, rightOperand);

		switch (operator) {
		case OR:
		case AND:
			this.filterStack.applyOperator(operator);
			break;
		case GT:
		case GE:
		case LT:
		case LE:
		case EQ: {
			final PripQueryFilterTerm filter = createFilter(operator, left, right, leftOperand, rightOperand);
			if (null == filter) {
				return null;
			}

			this.filterStack.push(filter);
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

		LOGGER.debug("got method: {}", methodCall.name());

		if (parameters.size() != 2 || !(parameters.get(0) instanceof Member)
				|| !(parameters.get(1) instanceof Literal)) {
			throw new ODataApplicationException(
					"Invalid or unsupported parameter(s): " + StringUtil.makeListString(",", parameters),
					HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
		}

		final Member field = (Member) parameters.get(0);
		final String odataFieldname = memberText(field);

		if (!isTextField(odataFieldname)) {
			throw new ODataApplicationException("Unsupported field name: " + odataFieldname,
					HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
		}

		final String pripFieldName = mapToPripFieldName(odataFieldname).orElse(null); // we already checked if text
																						// field
		final Function filterFunction = PripTextFilter.Function.fromString(methodCall.name());
		final String literal = ((Literal) parameters.get(1)).getText();
		final String text = literal.substring(1, literal.length() - 1);
		final PripTextFilter textFilter = new PripTextFilter(pripFieldName, filterFunction, text);

		this.filterStack.push(textFilter);

		return null;
	}

	@Override
	public Object visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression)
			throws ExpressionVisitException, ODataApplicationException {
		throw new ODataApplicationException(
				"Unsupported lambda expression: " + lambdaFunction + " / " + lambdaVariable + " / " + expression,
				HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
	}

	@Override
	public Object visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
		return literal;
	}

	@Override
	public Object visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {

		Object result = member;
		String type = "*";
		boolean ignored = true;
		LOGGER.debug(String.format("iterating: %s", member.getResourcePath().getUriResourceParts()));

		for (UriResource uriResource : member.getResourcePath().getUriResourceParts()) {
			LOGGER.debug(String.format("           %s (%s)", uriResource, uriResource.getKind()));
			if (uriResource instanceof UriResourceNavigation) {
				UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) uriResource;
				String typeRepresentation = uriResourceNavigation.getSegmentValue(true);
				switch (typeRepresentation.substring(typeRepresentation.lastIndexOf(".") + 1)) {
				case EdmProvider.ET_STRING_ATTRIBUTE_NAME:
					type = "string";
					break;
				case EdmProvider.ET_INTEGER_ATTRIBUTE_NAME:
					type = "long";
					break;
				case EdmProvider.ET_DOUBLE_ATTRIBUTE_NAME:
					type = "double";
					break;
				case EdmProvider.ET_BOOLEAN_ATTRIBUTE_NAME:
					type = "boolean";
					break;
				case EdmProvider.ET_DATE_ATTRIBUTE_NAME:
					type = "date";
					break;
				default:
				}
				LOGGER.debug(String.format("               %s -> %s", typeRepresentation, type));
			} else if (uriResource instanceof UriResourceLambdaAny) {
				final AttributesFilterVisitor filterExpressionVisitor = new AttributesFilterVisitor(type);
				final UriResourceLambdaAny any = (UriResourceLambdaAny) uriResource;
				final Expression expression = any.getExpression();
				LOGGER.debug(String.format("               visit AttributesFilterVisitor(%s) for: %s", type, expression));
				
				expression.accept(filterExpressionVisitor);
				final PripQueryFilter filter = filterExpressionVisitor.getFilter();
				LOGGER.debug(String.format("AttributesFilterVisitor returns: %s", filter));
				
				if (null != filter) {
					this.filterStack.push(filter);
					ignored = false;
				}
			} else if (uriResource instanceof UriResourceFunctionImpl) {
				UriResourceFunctionImpl uriResourceFunctionImpl = (UriResourceFunctionImpl) uriResource;
				if (EdmProvider.FUNCTION_INTERSECTS_FQN
						.equals(uriResourceFunctionImpl.getFunction().getFullQualifiedName()) ||EdmProvider.FUNCTION_WITHIN_FQN
						.equals(uriResourceFunctionImpl.getFunction().getFullQualifiedName()) || EdmProvider.FUNCTION_DISJOINT_FQN
						.equals(uriResourceFunctionImpl.getFunction().getFullQualifiedName())) {
					this.filterStack.push(
							handleGeometricFunction(uriResourceFunctionImpl.getParameters(), uriResourceFunctionImpl.getFunction().getFullQualifiedName()));
					ignored = false;
				}
			} else if (uriResource instanceof UriResourceLambdaAll) {
				throw new ODataApplicationException("Unsupported lambda expression on filter expression: all",
						HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
			}
		}
		LOGGER.debug(String.format("           %s.", ignored ? "visitMember: ignored" : "visitMember: done"));
		return result;
	}

	/**
	 * Handle intersect function
	 *
	 * @param parameters
	 * @return PripGeometryFilter
	 * @throws ODataApplicationException
	 * @throws ExpressionVisitException
	 */
	private static PripGeometryFilter handleGeometricFunction(List<UriParameter> parameters, FullQualifiedName functionName)
			throws ODataApplicationException, ExpressionVisitException {

		String geoPropertyFilterString = null;
		if (parameters != null) {
			for (UriParameter uriParameter : parameters) {
				if (uriParameter != null && "geo_polygon".equalsIgnoreCase(uriParameter.getName())) {
					geoPropertyFilterString = parameters.get(1).getText();
				}
			}
		}

		Geometry geometry = null;
		if (StringUtil.isNotEmpty(geoPropertyFilterString)) {
			try {
				geometry = asGeometry(parameters.get(1));
			} catch (ParseException e) {
				throw new ODataApplicationException("Invalid geography parameter",
						HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
			}
		} else {
			throw new ODataApplicationException(
					"Invalid parameter for function "
							+ functionName.getFullQualifiedNameAsString(),
					HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
		}

		if (EdmProvider.FUNCTION_INTERSECTS_FQN.equals(functionName)) {
			return new PripGeometryFilter(FIELD_NAMES.FOOTPRINT.fieldName(), PripGeometryFilter.Function.INTERSECTS,
					geometry);
		} else if (EdmProvider.FUNCTION_WITHIN_FQN.equals(functionName)) {
			return new PripGeometryFilter(FIELD_NAMES.FOOTPRINT.fieldName(), PripGeometryFilter.Function.WITHIN,
					geometry);
		} else if (EdmProvider.FUNCTION_DISJOINT_FQN.equals(functionName)) {
			return new PripGeometryFilter(FIELD_NAMES.FOOTPRINT.fieldName(), PripGeometryFilter.Function.DISJOINTS,
					geometry);
		} else {
			// Default: Intersection filter
			return new PripGeometryFilter(FIELD_NAMES.FOOTPRINT.fieldName(), PripGeometryFilter.Function.INTERSECTS,
					geometry); 
		}
	}

	private static Geometry asGeometry(UriParameter uriParameter) throws ParseException {
		// geography'SRID=4326;POLYGON((-127.89734578345 45.234534534,-127.89734578345
		// 45.234534534,-127.89734578345 45.234534534,-127.89734578345 45.234534534))'
		Pattern pattern = Pattern.compile("(geometry|geography)'\\s*SRID\\s*=\\s*([0-9]{1,5})\\s*;\\s*(.*)'");
		String text = uriParameter.getText();
		Matcher matcher = pattern.matcher(text);

		if (!matcher.matches()) {
			throw new ParseException("Invalid structered parameter");
		}

		matcher.group(1);
		String geoSRID = matcher.group(2);
		String geoGML = matcher.group(3);

		WKTReader wkt = new WKTReader();
		Geometry geo;
		geo = wkt.read(geoGML);
		geo.setSRID(Integer.valueOf(geoSRID));

		return geo;
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
		} else if (operand instanceof List) {
			List list = (List) operand;
			if (!list.isEmpty()) {
				Object first = list.get(0);
				if (first instanceof String) {
					result = (String) first;
				}
			}
		}
		return result;
	}

	public static LocalDateTime convertToLocalDateTime(String datetime) throws ExpressionVisitException {
		try {
			Instant instant = Instant.ofEpochMilli(
					EdmDateTimeOffset.getInstance().valueOfString(datetime, false, 0, 1000, 0, false, Long.class));
			return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		} catch (EdmPrimitiveTypeException e) {
			throw new ExpressionVisitException("Invalid or unsupported date time operand: " + datetime);
		}
	}

	public PripQueryFilter getFilter() throws ODataApplicationException {
		if (this.filterStack.isEmpty()) {
			return null;
		} else if (this.filterStack.size() == 1) {
			return this.filterStack.pop();
		} else {
			final String msg = String.format("Incomplete filter expression: more than one result on filter stack after traversing expression tree -> %s",
					this.filterStack);
			LOGGER.error(msg);
			throw new ODataApplicationException(msg, HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
		}
	}

	public static String memberText(Member member) {
		String text = "";
		List<UriResource> uriResourceParts = member.getResourcePath().getUriResourceParts();
		for (int idx = 0; idx < uriResourceParts.size(); idx++) {
			text += (idx > 0 ? "/" : "") + uriResourceParts.get(idx).getSegmentValue();
		}
		return text;
	}

	private static boolean isDateComparison(final Object leftOperandExpression, final String leftOperandString, final Object rightOperandExpression,
			final String rightOperandString) {
		return isDateComparison(leftOperandExpression, leftOperandString) || isDateComparison(rightOperandExpression, rightOperandString);
	}

	private static boolean isDateComparison(final Object operandExpression, final String operandString) {
		return operandExpression instanceof Member && isDateField(operandString);
	}

	private static boolean isDateField(String odataFieldName) {
		return PRIP_DATETIME_PROPERTY_FIELD_NAMES.containsKey(odataFieldName);
	}

	private static boolean isTextComparison(final Object leftOperandExpression, final String leftOperandString, final Object rightOperandExpression,
			final String rightOperandString) {
		return isTextComparison(leftOperandExpression, leftOperandString) || isTextComparison(rightOperandExpression, rightOperandString);
	}

	private static boolean isTextComparison(final Object operandExpression, final String operandString) {
		return operandExpression instanceof Member && isTextField(operandString);
	}

	private static boolean isTextField(String odataFieldName) {
		return PRIP_TEXT_PROPERTY_FIELD_NAMES.containsKey(odataFieldName);
	}

	private static boolean isIntegerComparison(final Object leftOperandExpression, final String leftOperandString, final Object rightOperandExpression,
			final String rightOperandString) {
		return isIntegerComparison(leftOperandExpression, leftOperandString) || isIntegerComparison(rightOperandExpression, rightOperandString);
	}

	private static boolean isIntegerComparison(final Object operandExpression, final String operandString) {
		return operandExpression instanceof Member && isIntegerField(operandString);
	}

	private static boolean isIntegerField(String odataFieldName) {
		return PRIP_INTEGER_PROPERTY_FIELD_NAMES.containsKey(odataFieldName);
	}

	private static Optional<String> mapToPripFieldName(String odataFieldName) {
		if (PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.containsKey(odataFieldName)) {
			return Optional.of(PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.get(odataFieldName).fieldName());
		}

		return Optional.empty();
	}

	private static PripQueryFilterTerm createFilter(final BinaryOperatorKind operator, final Object left, final Object right, final String leftOperand,
			final String rightOperand) throws ODataApplicationException, ExpressionVisitException {

		if (isTextComparison(left, leftOperand, right, rightOperand)) {
			return createTextFilter(Function.fromString(operator.name()), left, right, leftOperand, rightOperand);
		} else if (isDateComparison(left, leftOperand, right, rightOperand)) {
			return createDateTimeFilter(RelationalOperator.fromString(operator.name()), left, right, leftOperand, rightOperand);
		} else if (isIntegerComparison(left, leftOperand, right, rightOperand)) {
			return createIntegerFilter(RelationalOperator.fromString(operator.name()), left, right, leftOperand, rightOperand);
		} else {
			final String msg = String.format("Unsupported operation: %s %s %s", leftOperand != null ? leftOperand : "''", operator,
					rightOperand != null ? rightOperand : "''");
			LOGGER.error(msg);
			throw new ODataApplicationException(msg, HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
		}
	}

	private static PripDateTimeFilter createDateTimeFilter(RelationalOperator operator, Object left, Object right,
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

	private static PripTextFilter createTextFilter(Function function, Object left, Object right, String leftOperand,
			String rightOperand) throws ODataApplicationException {

		if (left instanceof Member && right instanceof Literal) {
			if (isTextField(leftOperand)) {
				return new PripTextFilter(mapToPripFieldName(leftOperand).orElse(null), function,
						rightOperand.substring(1, rightOperand.length() - 1));
			}
		} else if (left instanceof Literal && right instanceof Member) {
			if (isTextField(rightOperand)) {
				return new PripTextFilter(mapToPripFieldName(rightOperand).orElse(null), function,
						leftOperand.substring(1, leftOperand.length() - 1));
			}
		} else if (left instanceof Member && right instanceof List || left instanceof List && right instanceof Member) {
			final String memberText = left instanceof Member ? leftOperand : rightOperand;
			final String firstListElementText = left instanceof List ? leftOperand : rightOperand;

			// BEGIN WORKAROUND TO RETURN EMPTY RESULT FOR ProductionType !=
			// systematic_production
			if (ProductionType.name().equals("ProductionType")
					&& !SYSTEMATIC_PRODUCTION.getName().equals(firstListElementText)) {
				return new PripTextFilter(FIELD_NAMES.NAME.fieldName(), Function.EQUALS, "NOTEXISTINGPRODUCTNAME");
			} else {
				return null;
			}
			// END WORKAROUND TO RETURN EMPTY RESULT FOR ProductionType !=
			// systematic_production
		}

		throw new ODataApplicationException("Invalid or unsupported operand(s): " + leftOperand + " "
				+ function.getFunctionName() + " " + rightOperand, HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
	}

	private static PripIntegerFilter createIntegerFilter(final RelationalOperator operator, final Object left, final Object right, final String leftOperand,
			final String rightOperand) throws ODataApplicationException, ExpressionVisitException {

		if (left instanceof Member && right instanceof Literal) {
			if (isIntegerField(leftOperand)) {
				return new PripIntegerFilter(mapToPripFieldName(leftOperand).orElse(null), operator, Long.valueOf(rightOperand));
			}
		} else if (left instanceof Literal && right instanceof Member) {
			if (isIntegerField(rightOperand)) {
				return new PripIntegerFilter(mapToPripFieldName(rightOperand).orElse(null), operator.getInverse(), Long.valueOf(leftOperand));
			}
		}

		throw new ODataApplicationException("Invalid or unsupported operand(s): " + leftOperand + " " + operator.getOperator() + " " + rightOperand,
				HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
	}

}
