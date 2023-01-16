package esa.s1pdgs.cpoc.prip.frontend.service.processor.visitor;

import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;
import static org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind.OR;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;
import org.apache.olingo.server.core.uri.queryoption.expression.LiteralImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.utils.StringUtil;
import esa.s1pdgs.cpoc.prip.model.filter.PripBooleanFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDateTimeFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripDoubleFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripFilterOperatorException;
import esa.s1pdgs.cpoc.prip.model.filter.PripIntegerFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripQueryFilterTerm;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripRangeValueFilter.RelationalOperator;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter;
import esa.s1pdgs.cpoc.prip.model.filter.PripTextFilter.Function;

public class AttributesFilterVisitor implements ExpressionVisitor<Object> {

   private static final Logger LOGGER = LoggerFactory.getLogger(ProductsFilterVisitor.class);

   private static final String ATT_NAME = "att/Name";
   private static final String ATT_VALUE = "att/Value";

   private static final String FIELD_TYPE_STRING = "string";
   
   private static final String MARKED_TO_IGNORE = "__PRIP_ATTR_VISITOR_IGNORE__";

   private static final List<MethodKind> SUPPORTED_METHODS = Arrays.asList(MethodKind.CONTAINS,
         MethodKind.STARTSWITH, MethodKind.ENDSWITH);

   private final String fieldNameSuffix;
   private String attributeName = null;
   private String fieldName = null;
   private final FilterStack filterStack = new FilterStack();
   
   
   public AttributesFilterVisitor(final String fieldNameSuffix) {
      this.fieldNameSuffix = fieldNameSuffix; // e.g. "string" for a field name "att_foobar_string"      
   }
   
   @Override
   public Object visitBinaryOperator(final BinaryOperatorKind operator, final Object left, 
         final Object right) throws ExpressionVisitException, ODataApplicationException {

      final String leftOperand = ProductsFilterVisitor.operandToString(left);
      final String rightOperand = ProductsFilterVisitor.operandToString(right);
      LOGGER.debug(String.format("AttributesFilterVisitor got: %s %s %s", leftOperand, operator, rightOperand));

      // on the first call only, olingo will pass the fieldname
      if (null == fieldName) {
         // olingo accepts att/Name on the left only and the operator will be always EQ.
         // multiple conditions on att/Name are not permitted by olingo (olingo will automatically
         // evaluate them to a boolean result). still we do a short validation for future safety:
         if (!ATT_NAME.equals(leftOperand) || operator != BinaryOperatorKind.EQ) {
               String msg = String.format("Invalid request: %s %s %s", leftOperand, operator, rightOperand);
               LOGGER.error(msg);
               throw new ODataApplicationException(msg, BAD_REQUEST.getStatusCode(), null);
         }
         attributeName = rightOperand.replace("'", "");
         fieldName = "attr_" + attributeName + "_" + fieldNameSuffix;
         return new LiteralImpl(MARKED_TO_IGNORE, EdmString.getInstance());
      } else if (MARKED_TO_IGNORE.equals(leftOperand) || MARKED_TO_IGNORE.equals(rightOperand)) {
         // due to it's recursive nature, the visitor comes back later to combine all previous
         // results. as the att/Name went into the fieldName instead of the filterStack, the
         // related logical operation to the filterStack have to be discarded. 
         if (operator == OR) {
            String msg = String.format("Invalid request: OR with %s not allowed (must be distinct)",
                  ATT_NAME);
            LOGGER.error(msg);
            throw new ODataApplicationException(msg, BAD_REQUEST.getStatusCode(), null);
         }
         return null;
      }
      
      // on subsequent calls, conditions on the value are added
      switch (operator) {
         case OR:
         case AND:
            filterStack.applyOperator(operator);
            break;
         case GT:
         case GE:
         case LT:
         case LE:
         case EQ:
            // validate operands
            if (StringUtil.isEmpty(leftOperand) || StringUtil.isEmpty(rightOperand) ||
                  !(ATT_VALUE.equals(leftOperand) || ATT_VALUE.equals(rightOperand))) {
               final String msg = String.format("AttributesFilterVisitor: incomplete value expression (missing operator or operand) on attribute %s: %s %s %s",
                     attributeName, StringUtil.isNotEmpty(leftOperand) ? leftOperand : "[MISSING VALUE]",
                     operator, StringUtil.isNotEmpty(rightOperand) ? rightOperand : "[MISSING VALUE]");
               LOGGER.error(msg);
               throw new ODataApplicationException(msg, BAD_REQUEST.getStatusCode(), null);
            }
            
            // create filter
            PripQueryFilter filter = null;
            if (ATT_VALUE.equals(leftOperand)) {
               filter = buildFilter(fieldName, operator, rightOperand, false);
            } else {
               filter = buildFilter(fieldName, operator, leftOperand, true);
            }
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
               BAD_REQUEST.getStatusCode(), null);
      }

      final Member field = (Member) parameters.get(0);
      final String odataFieldname = ProductsFilterVisitor.memberText(field);

      if (!FIELD_TYPE_STRING.equals(fieldNameSuffix)) {
         throw new ODataApplicationException("Unsupported field name: " + odataFieldname,
               BAD_REQUEST.getStatusCode(), null);
      }

      final Function filterFunction = PripTextFilter.Function.fromString(methodCall.name());
      final String literal = ((Literal) parameters.get(1)).getText();
      final String text = literal.substring(1, literal.length() - 1);
      final PripTextFilter textFilter = new PripTextFilter(fieldName, filterFunction, text);

      filterStack.push(textFilter);

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
   public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, List<Object> right)
         throws ExpressionVisitException, ODataApplicationException {
      throw new UnsupportedOperationException();
   }
   
   @Override
   public Object visitEnum(EdmEnumType type, List<String> enumValues)
         throws ExpressionVisitException, ODataApplicationException {
      throw new UnsupportedOperationException();
   }
   
   public PripQueryFilter getFilter() throws ODataApplicationException {
      if (filterStack.isEmpty()) {
         return null;
      } else if (filterStack.size() == 1) {
         return filterStack.pop();
      } else {
         final String msg = String.format("Incomplete filter expression: more than one result on filter stack after traversing expression tree -> %s",
               filterStack);
         LOGGER.error(msg);
         throw new ODataApplicationException(msg, BAD_REQUEST.getStatusCode(), null);
      }
   }
   
   private PripQueryFilterTerm buildFilter(final String fieldName, final BinaryOperatorKind op,
         final String value, final boolean flipOperator) throws ExpressionVisitException {
      try {
         switch (fieldNameSuffix) {
         case "string":
            return new PripTextFilter(fieldName, PripTextFilter.Function.fromString(op.name()),
                  value);
         case "date":
            return new PripDateTimeFilter(fieldName, getRelationalOperator(op, flipOperator),
                  ProductsFilterVisitor.convertToLocalDateTime(value));
         case "long":
            return new PripIntegerFilter(fieldName, getRelationalOperator(op, flipOperator),
                  Long.valueOf(value));
         case "double":
            return new PripDoubleFilter(fieldName, getRelationalOperator(op, flipOperator),
                  Double.valueOf(value));
         case "boolean":
            return new PripBooleanFilter(fieldName, PripBooleanFilter.Function.fromString(
                  op.name()), Boolean.valueOf(value));
         default:
            throw new ExpressionVisitException("unsupported type: " + fieldNameSuffix);
         }
      } catch (PripFilterOperatorException e) {
         throw new ExpressionVisitException(e.getMessage());
      }
   }
   
   private static RelationalOperator getRelationalOperator(final BinaryOperatorKind op,
         final boolean flipOperator) {
      final RelationalOperator relOp = PripRangeValueFilter.RelationalOperator.fromString(op.name());
      return flipOperator ? relOp.getHorizontallyFlippedOperator() : relOp;      
   }
}
