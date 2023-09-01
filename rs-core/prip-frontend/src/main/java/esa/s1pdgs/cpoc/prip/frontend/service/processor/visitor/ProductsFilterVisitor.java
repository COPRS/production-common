package esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor;

import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ContentDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.End;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.EvictionDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.OriginDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Id;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Name;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Online;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ProductionType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.PublicationDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Start;
import static esa.s1pdgs.cpoc.prip.model.ProductionType.SYSTEMATIC_PRODUCTION;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;

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
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
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
import org.apache.olingo.server.core.uri.queryoption.expression.LiteralImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MemberImpl;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties;
import esa.s1pdgs.cpoc.prip.model.PripMetadata.FIELD_NAMES;
import esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripGeometryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripInFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripIntegerFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterTerm;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter.RelationalOperator;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter.Function;

public class ProductsFilterVisitor implements ExpressionVisitor<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductsFilterVisitor.class);

	public static final Pattern ODATA_EWKT_EXTRACTION_PATTERN = Pattern.compile("(geometry|geography)'\\s*SRID\\s*=\\s*([0-9]{1,5})\\s*;\\s*(.*)'");

	private static final Map<String, FIELD_NAMES> PRIP_DATETIME_PROPERTY_FIELD_NAMES;
	private static final Map<String, FIELD_NAMES> PRIP_TEXT_PROPERTY_FIELD_NAMES;
	private static final Map<String, FIELD_NAMES> PRIP_INTEGER_PROPERTY_FIELD_NAMES;
	private static final Map<String, FIELD_NAMES> PRIP_BOOLEAN_PROPERTY_FIELD_NAMES;
	private static final Map<String, FIELD_NAMES> PRIP_SUPPORTED_PROPERTY_FIELD_NAMES;
	private static final List<String> PRIP_PRODUCTION_TYPES;
	private static final List<MethodKind> SUPPORTED_METHODS;

	static {
		PRIP_DATETIME_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(PublicationDate.name(), FIELD_NAMES.CREATION_DATE);
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(EvictionDate.name(), FIELD_NAMES.EVICTION_DATE);
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(OriginDate.name(), FIELD_NAMES.ORIGIN_DATE);
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(ContentDate.name() + "/" + Start.name(), FIELD_NAMES.CONTENT_DATE_START);
		PRIP_DATETIME_PROPERTY_FIELD_NAMES.put(ContentDate.name() + "/" + End.name(), FIELD_NAMES.CONTENT_DATE_END);

		PRIP_TEXT_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_TEXT_PROPERTY_FIELD_NAMES.put(Id.name(), FIELD_NAMES.ID);
		PRIP_TEXT_PROPERTY_FIELD_NAMES.put(Name.name(), FIELD_NAMES.NAME);
		PRIP_TEXT_PROPERTY_FIELD_NAMES.put(ProductionType.name(), FIELD_NAMES.PRODUCTION_TYPE);

		PRIP_INTEGER_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_INTEGER_PROPERTY_FIELD_NAMES.put(ProductProperties.ContentLength.name(), FIELD_NAMES.CONTENT_LENGTH);

		PRIP_BOOLEAN_PROPERTY_FIELD_NAMES = new HashMap<>();
      PRIP_BOOLEAN_PROPERTY_FIELD_NAMES.put(ProductProperties.Online.name(), FIELD_NAMES.ONLINE);

		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES = new HashMap<>();
		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.putAll(PRIP_DATETIME_PROPERTY_FIELD_NAMES);
		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.putAll(PRIP_TEXT_PROPERTY_FIELD_NAMES);
		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.putAll(PRIP_INTEGER_PROPERTY_FIELD_NAMES);
		PRIP_SUPPORTED_PROPERTY_FIELD_NAMES.putAll(PRIP_BOOLEAN_PROPERTY_FIELD_NAMES);

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
			filterStack.applyOperator(operator);
			break;
		case GT:
		case GE:
		case LT:
		case LE:
		case NE:
		case EQ:
			final PripQueryFilterTerm filter = createFilter(operator, left, right, leftOperand, rightOperand);
			if (null != filter) {
			   filterStack.push(filter);
			}
			break;
		default:
			throw new UnsupportedOperationException("Operator " + operator + " not supported!");
		}

		return null;
	}

	@Override
	public Object visitUnaryOperator(UnaryOperatorKind operator, Object operand)
			throws ExpressionVisitException, ODataApplicationException {

		if (operator == UnaryOperatorKind.NOT) {
			filterStack.applyNot();

		} else {
			throw new UnsupportedOperationException(
					String.format("Unsupported unary operator %s on filter expression", operator.name()));
		}

		return null;
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
					BAD_REQUEST.getStatusCode(), null);
		}

		final Member field = (Member) parameters.get(0);
		final String odataFieldname = memberText(field);

		if (!isTextField(odataFieldname)) {
			throw new ODataApplicationException("Unsupported field name: " + odataFieldname,
					BAD_REQUEST.getStatusCode(), null);
		}

		final String pripFieldName = mapToPripFieldName(odataFieldname).orElse(null); // we already checked if text
																						// field
		final Function filterFunction = PripTextFilter.Function.fromString(methodCall.name());
		final String literal = ((Literal) parameters.get(1)).getText();
		final String text = getStringData(literal);
		final PripTextFilter textFilter = new PripTextFilter(pripFieldName, filterFunction, text);

		this.filterStack.push(textFilter);

		return null;
	}

	@Override
	public Object visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression)
			throws ExpressionVisitException, ODataApplicationException {
		throw new ODataApplicationException(
				"Unsupported lambda expression: " + lambdaFunction + " / " + lambdaVariable + " / " + expression,
				BAD_REQUEST.getStatusCode(), Locale.ROOT);
	}

	@Override
	public Object visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
		return literal;
	}

	@Override
	public Object visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
		Object result = member;
		String odataTypeName = null;
		boolean ignored = true;
		LOGGER.debug(String.format("iterating: %s", member.getResourcePath().getUriResourceParts()));

		for (UriResource uriResource : member.getResourcePath().getUriResourceParts()) {
			LOGGER.debug(String.format("           %s (%s)", uriResource, uriResource.getKind()));
			if (uriResource instanceof UriResourceNavigation) {
				UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) uriResource;
				odataTypeName = uriResourceNavigation.getSegmentValue(true);
			} else if (uriResource instanceof UriResourceLambdaAny) {
			   final String fieldNameSuffix; // database fieldname suffix (for e.g. att_foobar_string)
            switch (odataTypeName.substring(odataTypeName.lastIndexOf(".") + 1)) {
            case EdmProvider.ET_STRING_ATTRIBUTE_NAME:
               fieldNameSuffix = "string";
               break;
            case EdmProvider.ET_INTEGER_ATTRIBUTE_NAME:
               fieldNameSuffix = "long";
               break;
            case EdmProvider.ET_DOUBLE_ATTRIBUTE_NAME:
               fieldNameSuffix = "double";
               break;
            case EdmProvider.ET_BOOLEAN_ATTRIBUTE_NAME:
               fieldNameSuffix = "boolean";
               break;
            case EdmProvider.ET_DATE_ATTRIBUTE_NAME:
               fieldNameSuffix = "date";
               break;
            default:
               final String msg = String.format("Unsupported Type: %s" + odataTypeName);
               LOGGER.error(msg);
               throw new IllegalArgumentException(msg);
            }
            LOGGER.debug(String.format("               %s -> %s", odataTypeName, fieldNameSuffix));
				final AttributesFilterVisitor filterExpressionVisitor =
				      new AttributesFilterVisitor(fieldNameSuffix);
				final UriResourceLambdaAny any = (UriResourceLambdaAny) uriResource;
				final Expression expression = any.getExpression();
				LOGGER.debug(String.format("               visit AttributesFilterVisitor(%s) for: %s", fieldNameSuffix, expression));
				
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
						BAD_REQUEST.getStatusCode(), Locale.ROOT);
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
				if (uriParameter != null && "geo_shape".equalsIgnoreCase(uriParameter.getName())) {
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
						BAD_REQUEST.getStatusCode(), Locale.ROOT);
			}
		} else {
			throw new ODataApplicationException(
					"Invalid parameter for function "
							+ functionName.getFullQualifiedNameAsString(),
					BAD_REQUEST.getStatusCode(), Locale.ROOT);
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

	public static Geometry asGeometry(final UriParameter uriParameter) throws ParseException {
		// geography'SRID=4326;POINT(44.8571 20.3411)'
		// geography'SRID=4326;LINESTRING(44.8571 20.3411, 11.4484 49.9204, 2.4321 32.3625)'
		// geography'SRID=4326;POLYGON((-127.89734578345 45.234534534,-127.89734578345 45.234534534,-127.89734578345 45.234534534,-127.89734578345 45.234534534))'

		final Matcher matcher = ODATA_EWKT_EXTRACTION_PATTERN.matcher(uriParameter.getText());

		if (!matcher.matches()) {
			throw new ParseException("Invalid parameter format");
		}

		matcher.group(1); // geometry or geography for flat-earth or round-earth coordinate reference systems (CRS), must match with SRID
		final String srid = matcher.group(2);
		final String wkt = matcher.group(3);

		final WKTReader wktReader = new WKTReader();
		final Geometry geo = wktReader.read(wkt);
		geo.setSRID(Integer.valueOf(srid));

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
	public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, List<Object> right)
			throws ExpressionVisitException, ODataApplicationException {
		if (operator != BinaryOperatorKind.IN) {
			throw new UnsupportedOperationException("Operator " + operator + " not supported!");
		}
		if (!(left instanceof MemberImpl)) {
			throw new UnsupportedOperationException("Operand type of" + left + " not supported!");
		}
		
		final String pripFieldName = mapToPripFieldName(memberText((MemberImpl) left)).orElse(null);
		
		List<Object> listObjects = new ArrayList<>();
		
		for (Object o:right) {
			if (!(o instanceof LiteralImpl)) {
				throw new UnsupportedOperationException("Type of " + o + " not supported!");
			}
		    listObjects.add(((LiteralImpl) o).getText().replace("'", ""));
		}

		final PripQueryFilterTerm filter = new PripInFilter(pripFieldName, PripInFilter.Function.IN, listObjects);
		filterStack.push(filter);
		return null;
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
	   // converts objects into text form, preserving surrounding quotes if present.
	   // use getStringData() to extract absolute string values in a second step where applicable. 
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
			throw new ODataApplicationException(msg, BAD_REQUEST.getStatusCode(), null);
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
	
	private static boolean isBooleanComparison(final Object leftOperandExpression, final String leftOperandString, final Object rightOperandExpression,
         final String rightOperandString) {
      return isBooleanComparison(leftOperandExpression, leftOperandString) || isIntegerComparison(rightOperandExpression, rightOperandString);
   }

   private static boolean isBooleanComparison(final Object operandExpression, final String operandString) {
      return operandExpression instanceof Member && isBooleanField(operandString);
   }

   private static boolean isBooleanField(String odataFieldName) {
      return PRIP_BOOLEAN_PROPERTY_FIELD_NAMES.containsKey(odataFieldName);
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
      } else if (isBooleanComparison(left, leftOperand, right, rightOperand)) {
         return createBooleanFilter(esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter.Function
               .fromString(operator.name()), left, right, leftOperand, rightOperand);
		} else {
			final String msg = String.format("Unsupported operation: %s %s %s", leftOperand != null ? leftOperand : "''", operator,
					rightOperand != null ? rightOperand : "''");
			LOGGER.error(msg);
			throw new ODataApplicationException(msg, BAD_REQUEST.getStatusCode(), null);
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
				return new PripDateTimeFilter(mapToPripFieldName(rightOperand).orElse(null), operator.getHorizontallyFlippedOperator(),
						convertToLocalDateTime(leftOperand));
			}
		}

		throw new ODataApplicationException(
				"Invalid or unsupported operand(s): " + leftOperand + " " + operator.getOperator() + " " + rightOperand,
				BAD_REQUEST.getStatusCode(), null);
	}

	private static PripTextFilter createTextFilter(Function function, Object left, Object right, String leftOperand,
			String rightOperand) throws ODataApplicationException {

		if (left instanceof Member && right instanceof Literal) {
			if (isTextField(leftOperand)) {
				return new PripTextFilter(mapToPripFieldName(leftOperand).orElse(null), function,
						getStringData(rightOperand));
			}
		} else if (left instanceof Literal && right instanceof Member) {
			if (isTextField(rightOperand)) {
				return new PripTextFilter(mapToPripFieldName(rightOperand).orElse(null), function,
						getStringData(leftOperand));
			}
		} else if (left instanceof Member && right instanceof List || left instanceof List && right instanceof Member) {
			final String memberText = left instanceof Member ? leftOperand : rightOperand;
			final String firstListElementText = left instanceof List ? leftOperand : rightOperand;

			// BEGIN WORKAROUND TO RETURN EMPTY RESULT FOR ProductionType !=
			// systematic_production
			if (ProductionType.name().equals("ProductionType")
					&& !SYSTEMATIC_PRODUCTION.getName().equals(firstListElementText)) {
				return new PripTextFilter(FIELD_NAMES.NAME.fieldName(), Function.EQ, "NOTEXISTINGPRODUCTNAME");
			} else {
				return null;
			}
			// END WORKAROUND TO RETURN EMPTY RESULT FOR ProductionType !=
			// systematic_production
		}

		throw new ODataApplicationException("Invalid or unsupported operand(s): " + leftOperand + " "
				+ function.getFunctionName() + " " + rightOperand, BAD_REQUEST.getStatusCode(), null);
	}

	private static PripIntegerFilter createIntegerFilter(final RelationalOperator operator, final Object left, final Object right, final String leftOperand,
			final String rightOperand) throws ODataApplicationException, ExpressionVisitException {
		if (left instanceof Member && right instanceof Literal) {
			if (isIntegerField(leftOperand)) {
				return new PripIntegerFilter(mapToPripFieldName(leftOperand).orElse(null), operator, Long.valueOf(rightOperand));
			}
		} else if (left instanceof Literal && right instanceof Member) {
			if (isIntegerField(rightOperand)) {
				return new PripIntegerFilter(mapToPripFieldName(rightOperand).orElse(null), operator.getHorizontallyFlippedOperator(), Long.valueOf(leftOperand));
			}
		}

		throw new ODataApplicationException("Invalid or unsupported operand(s): " + leftOperand + " " + operator.getOperator() + " " + rightOperand,
				BAD_REQUEST.getStatusCode(), null);
	}
	
	private static PripBooleanFilter createBooleanFilter(
	      final esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter.Function function,
	      final Object left, final Object right, final String leftOperand, final String rightOperand)
	            throws ODataApplicationException, ExpressionVisitException {
      if (left instanceof Member && right instanceof Literal) {
         if (isBooleanField(leftOperand)) {
            return newPripBooleanFilter(mapToPripFieldName(leftOperand).orElse(null), function,
                  Boolean.valueOf(rightOperand));
         }
      } else if (left instanceof Literal && right instanceof Member) {
         if (isBooleanField(rightOperand)) {
            return newPripBooleanFilter(mapToPripFieldName(rightOperand).orElse(null), function,
                  Boolean.valueOf(leftOperand));
         }
      }

      throw new ODataApplicationException("Invalid or unsupported operand(s): " + leftOperand +
            " " + function + " " + rightOperand, BAD_REQUEST.getStatusCode(), null);
   }
	
	private static PripBooleanFilter newPripBooleanFilter(final String fieldName,
	      final PripBooleanFilter.Function function, final boolean value) {
	   // RS-400: By default if not specified otherwise, products are online.
	   // This means a query for online EQ true has to return {online=true, online=null} (instead of
	   // {online=true}) and a query for online NE true has to return only {online=false} (instead of
	   // {online=false, online=null}).
	   // Note: Latter results having online=null will use the default (true) as set in
	   // esa.s1pdgs.cpoc.prip.model.PripMetadata without an explicit mapping step. 
	   if (value && PripBooleanFilter.Function.EQ == function
	            && Online.name().equalsIgnoreCase(fieldName)) {
	      // rewrite query to find {online=true, online=null} when searching for online EQ true
	      LOGGER.debug("Rewriting '{} {} {}' to '{} {} {}'", fieldName, function, value, fieldName,
	            PripBooleanFilter.Function.NE, false);
         return new PripBooleanFilter(fieldName, PripBooleanFilter.Function.NE, false);  	     
	   } else if (value && PripBooleanFilter.Function.NE == function
	         && Online.name().equalsIgnoreCase(fieldName)) {
	      // rewrite query to find {online=false} when searching for online NE true
         LOGGER.debug("Rewriting '{} {} {}' to '{} {} {}'", fieldName, function, value, fieldName,
               PripBooleanFilter.Function.EQ, false);
	      return new PripBooleanFilter(fieldName, PripBooleanFilter.Function.EQ, false);  
	   }
	   return new PripBooleanFilter(fieldName, function, value);
	}
	
	public static String getStringData(String odataStringParameter) {
	   // converts an odata string parameter WITH its surrounding single quotes to plain text WITHOUT
	   // surrounding single quotes. single quotes inside of the string, which are represented by
	   // two following single quotes are unescaped to one single quote each. see also:
	   // https://docs.oasis-open.org/odata/odata/v4.01/cs01/abnf/odata-abnf-construction-rules.txt
	   
	   if (odataStringParameter.length() >= 1 && odataStringParameter.charAt(0) != '\'') {
	      // workaround to handle GUID as text
	      return odataStringParameter;
	   }
	   
	   return odataStringParameter.substring(1, odataStringParameter.length() - 1).replace("''", "'");
	}
}
