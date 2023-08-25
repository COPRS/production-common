package esa.s1pdgs.cpoc.prip.frontend.service.edm;

import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Algorithm;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Checksum;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ChecksumDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ContentDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ContentLength;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ContentType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.End;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.EvictionDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.OriginDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Id;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Name;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Online;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.ProductionType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.PublicationDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Start;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Value;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.ProductProperties.Footprint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;

public class EdmProvider extends org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider {

	public static final String SERVICE_NAMESPACE = "OData.CSC";

	// Types
	public static final FullQualifiedName INT_32_TYPE_FQN = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
	public static final FullQualifiedName INT_64_TYPE_FQN = EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
	public static final FullQualifiedName STRING_TYPE_FQN = EdmPrimitiveTypeKind.String.getFullQualifiedName();
	public static final FullQualifiedName DOUBLE_TYPE_FQN = EdmPrimitiveTypeKind.Double.getFullQualifiedName();
	public static final FullQualifiedName BOOLEAN_TYPE_FQN = EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
	public static final FullQualifiedName GUID_TYPE_FQN = EdmPrimitiveTypeKind.Guid.getFullQualifiedName();
	public static final FullQualifiedName DATE_TIME_OFFSET_TYPE_FQN = EdmPrimitiveTypeKind.DateTimeOffset
			.getFullQualifiedName();
	public static final FullQualifiedName GEOSHAPE_GEOGRAPHY_TYPE_FQN = EdmPrimitiveTypeKind.Geography
			.getFullQualifiedName();
	
	public static final FullQualifiedName CHECKSUM_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE, "Checksum");
	public static final FullQualifiedName TIMERANGE_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE, "TimeRange");
	public static final FullQualifiedName PRODUCTION_TYPE_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
			"ProductionType");

	public static final FullQualifiedName FUNCTION_INTERSECTS_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
			"Intersects");
	public static final FullQualifiedName FUNCTION_WITHIN_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
			"Within");
	public static final FullQualifiedName FUNCTION_DISJOINT_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
			"Disjoints");
	
	
	// EDM Container
	public static final String CONTAINER_NAME = "PRIPData";
	public static final FullQualifiedName CONTAINER = new FullQualifiedName(SERVICE_NAMESPACE, CONTAINER_NAME);

	// Entity Types
	public static final String ET_PRODUCT_NAME = "Product";
	public static final FullQualifiedName ET_PRODUCT_FQN = new FullQualifiedName(SERVICE_NAMESPACE, ET_PRODUCT_NAME);
	public static final String ET_ATTRIBUTE_NAME = "Attribute";
	public static final FullQualifiedName ATTRIBUTE_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
			ET_ATTRIBUTE_NAME);
	public static final String ET_STRING_ATTRIBUTE_NAME = "StringAttribute";
	public static final FullQualifiedName STRING_ATTRIBUTE_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
			ET_STRING_ATTRIBUTE_NAME);
	public static final String ET_INTEGER_ATTRIBUTE_NAME = "IntegerAttribute";
	public static final FullQualifiedName INTEGER_ATTRIBUTE_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
			ET_INTEGER_ATTRIBUTE_NAME);
	public static final String ET_DOUBLE_ATTRIBUTE_NAME = "DoubleAttribute";
	public static final FullQualifiedName DOUBLE_ATTRIBUTE_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
			ET_DOUBLE_ATTRIBUTE_NAME);
	public static final String ET_DATE_ATTRIBUTE_NAME = "DateTimeOffsetAttribute";
	public static final FullQualifiedName DATE_ATTRIBUTE_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
			ET_DATE_ATTRIBUTE_NAME);
	public static final String ET_BOOLEAN_ATTRIBUTE_NAME = "BooleanAttribute";
	public static final FullQualifiedName BOOLEAN_ATTRIBUTE_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
			ET_BOOLEAN_ATTRIBUTE_NAME);
	public static final String ET_QUICKLOOK_NAME = "Quicklook";
   public static final FullQualifiedName QUICKLOOK_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE,
         ET_QUICKLOOK_NAME);

	// Entity Set Names
	public static final String ES_PRODUCTS_NAME = "Products";
	public static final String ATTRIBUTES_SET_NAME = "Attributes";
	public static final String STRING_ATTRIBUTES_SET_NAME = "StringAttributes";
	public static final String INTEGER_ATTRIBUTES_SET_NAME = "IntegerAttributes";
	public static final String DOUBLE_ATTRIBUTES_SET_NAME = "DoubleAttributes";
	public static final String DATE_ATTRIBUTES_SET_NAME = "DateTimeOffsetAttributes";
	public static final String BOOLEAN_ATTRIBUTES_SET_NAME = "BooleanAttributes";
	public static final String QUICKLOOK_SET_NAME = "Quicklooks";

	public static final String[] ATTRIBUTES_TYPE_NAMES = new String[] { STRING_ATTRIBUTES_SET_NAME,
			INTEGER_ATTRIBUTES_SET_NAME, DOUBLE_ATTRIBUTES_SET_NAME, BOOLEAN_ATTRIBUTES_SET_NAME,
			DATE_ATTRIBUTES_SET_NAME };
	
	public static final String ACTION_FILTERLIST = "FilterList";
	public static final FullQualifiedName FILTERLIST_ACTION_FQN = new FullQualifiedName(SERVICE_NAMESPACE, ACTION_FILTERLIST);
	public static final String PARAMETER_FILTERPRODUCTS = "FilterProducts";
	public static final FullQualifiedName PROPERTY_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE, "Property");
	 
	// --------------------------------------------------------------------------

	@Override
	public List<CsdlAction> getActions(FullQualifiedName actionName) throws ODataException {
		
		List<CsdlAction> actions = new ArrayList<CsdlAction>();
		
		if(actionName.equals(FILTERLIST_ACTION_FQN)) {
		       
		       final List<CsdlParameter> parameters = new ArrayList<CsdlParameter>();
		       CsdlParameter parameter = new CsdlParameter();
		       parameter.setName(ES_PRODUCTS_NAME);
		       parameter.setType(ET_PRODUCT_FQN);
		       parameter.setCollection(true);
		       parameters.add(parameter);
		       parameter = new CsdlParameter();
		       parameter.setName(PARAMETER_FILTERPRODUCTS);
		       parameter.setType(PROPERTY_TYPE_FQN);
		       parameter.setCollection(true);
		       parameters.add(parameter);
		       
		       final CsdlAction action = new CsdlAction();
		       action.setName(FILTERLIST_ACTION_FQN.getName());
		       action.setBound(true);
		       action.setParameters(parameters);
		       action.setReturnType(new CsdlReturnType().setType(ET_PRODUCT_FQN).setCollection(true));
		       actions.add(action);
		       
		       return actions;
		    }
		
		return null;
	}
	
	/**
	 * @see org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider#getFunctions(org.apache.olingo.commons.api.edm.FullQualifiedName)
	 */
	@Override
	public List<CsdlFunction> getFunctions(FullQualifiedName functionName) throws ODataException {
		// Edm.Boolean intersects(Edm.GeographyPolygon,Edm.GeographyPolygon)

		// It is allowed to overload functions, so we have to provide a list of
		// Functions for each function name
		final List<CsdlFunction> functions = new ArrayList<CsdlFunction>();

		if (functionName.equals(FUNCTION_INTERSECTS_FQN)) {
			// Create parameters
			final List<CsdlParameter> parameters = new ArrayList<CsdlParameter>();
			CsdlParameter parameter = new CsdlParameter();
			parameter = new CsdlParameter();
			parameter.setName("geo_property");
			parameter.setType(EdmPrimitiveTypeKind.Geography.getFullQualifiedName());
			parameter.setCollection(false);
			parameters.add(parameter);
			parameter = new CsdlParameter();
			parameter.setName("geo_shape");
			parameter.setType(EdmPrimitiveTypeKind.Geography.getFullQualifiedName());
			parameter.setCollection(false);
			parameters.add(parameter);

			// Create the Csdl Function
			final CsdlFunction function = new CsdlFunction();
			function.setName(FUNCTION_INTERSECTS_FQN.getName());
			function.setBound(false);
			function.setParameters(parameters);
			function.setReturnType(new CsdlReturnType().setType(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName())
					.setCollection(false));
			functions.add(function);

			return functions;
		} 
		if (functionName.equals(FUNCTION_WITHIN_FQN)) {
			// Create parameters
			final List<CsdlParameter> parameters = new ArrayList<CsdlParameter>();
			CsdlParameter parameter = new CsdlParameter();
			parameter = new CsdlParameter();
			parameter.setName("geo_property");
			parameter.setType(EdmPrimitiveTypeKind.Geography.getFullQualifiedName());
			parameter.setCollection(false);
			parameters.add(parameter);
			parameter = new CsdlParameter();
			parameter.setName("geo_shape");
			parameter.setType(EdmPrimitiveTypeKind.Geography.getFullQualifiedName());
			parameter.setCollection(false);
			parameters.add(parameter);

			// Create the Csdl Function
			final CsdlFunction function = new CsdlFunction();
			function.setName(FUNCTION_WITHIN_FQN.getName());
			function.setBound(false);
			function.setParameters(parameters);
			function.setReturnType(new CsdlReturnType().setType(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName())
					.setCollection(false));
			functions.add(function);

			return functions;
		}
		if (functionName.equals(FUNCTION_DISJOINT_FQN)) {
			// Create parameters
			final List<CsdlParameter> parameters = new ArrayList<CsdlParameter>();
			CsdlParameter parameter = new CsdlParameter();
			parameter = new CsdlParameter();
			parameter.setName("geo_property");
			parameter.setType(EdmPrimitiveTypeKind.Geography.getFullQualifiedName());
			parameter.setCollection(false);
			parameters.add(parameter);
			parameter = new CsdlParameter();
			parameter.setName("geo_shape");
			parameter.setType(EdmPrimitiveTypeKind.Geography.getFullQualifiedName());
			parameter.setCollection(false);
			parameters.add(parameter);

			// Create the Csdl Function
			final CsdlFunction function = new CsdlFunction();
			function.setName(FUNCTION_DISJOINT_FQN.getName());
			function.setBound(false);
			function.setParameters(parameters);
			function.setReturnType(new CsdlReturnType().setType(EdmPrimitiveTypeKind.Boolean.getFullQualifiedName())
					.setCollection(false));
			functions.add(function);

			return functions;
		}

		return null;
	}

	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
		if (ET_PRODUCT_FQN.equals(entityTypeName)) {
			final CsdlEntityType entityType = new CsdlEntityType();

			final CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName(ProductProperties.Id.name());

			final List<CsdlProperty> properties = new ArrayList<>();
			properties.add(new CsdlProperty().setName(Id.name()).setType(GUID_TYPE_FQN));
			properties.add(new CsdlProperty().setName(Name.name()).setType(STRING_TYPE_FQN));
			properties.add(new CsdlProperty().setName(Online.name()).setType(BOOLEAN_TYPE_FQN));
			properties.add(new CsdlProperty().setName(ContentType.name()).setType(STRING_TYPE_FQN));
			properties.add(new CsdlProperty().setName(ContentLength.name()).setType(INT_64_TYPE_FQN));
			properties.add(new CsdlProperty().setName(PublicationDate.name()).setType(DATE_TIME_OFFSET_TYPE_FQN));
			properties.add(new CsdlProperty().setName(EvictionDate.name()).setType(DATE_TIME_OFFSET_TYPE_FQN));
			properties.add(new CsdlProperty().setName(OriginDate.name()).setType(DATE_TIME_OFFSET_TYPE_FQN));
			properties.add(new CsdlProperty().setName(Checksum.name()).setType(CHECKSUM_TYPE_FQN).setCollection(true));
			properties.add(new CsdlProperty().setName(ProductionType.name()).setType(PRODUCTION_TYPE_TYPE_FQN));
			properties.add(new CsdlProperty().setName(ContentDate.name()).setType(TIMERANGE_TYPE_FQN));
			properties.add(new CsdlProperty().setName(Footprint.name()).setType(GEOSHAPE_GEOGRAPHY_TYPE_FQN));

			entityType.setName(ET_PRODUCT_NAME);
			entityType.setKey(Collections.singletonList(propertyRef));
			entityType.setProperties(properties);
			entityType.setHasStream(true);

			entityType.setNavigationProperties(Arrays.asList(
					new CsdlNavigationProperty().setName(ATTRIBUTES_SET_NAME).setType(ATTRIBUTE_TYPE_FQN)
							.setCollection(true),
					new CsdlNavigationProperty().setName(STRING_ATTRIBUTES_SET_NAME).setType(STRING_ATTRIBUTE_TYPE_FQN)
							.setCollection(true),
					new CsdlNavigationProperty().setName(INTEGER_ATTRIBUTES_SET_NAME)
							.setType(INTEGER_ATTRIBUTE_TYPE_FQN).setCollection(true),
					new CsdlNavigationProperty().setName(DOUBLE_ATTRIBUTES_SET_NAME).setType(DOUBLE_ATTRIBUTE_TYPE_FQN)
							.setCollection(true),
					new CsdlNavigationProperty().setName(BOOLEAN_ATTRIBUTES_SET_NAME)
							.setType(BOOLEAN_ATTRIBUTE_TYPE_FQN).setCollection(true),
					new CsdlNavigationProperty().setName(DATE_ATTRIBUTES_SET_NAME).setType(DATE_ATTRIBUTE_TYPE_FQN)
							.setCollection(true),
			      new CsdlNavigationProperty().setName(QUICKLOOK_SET_NAME).setType(QUICKLOOK_TYPE_FQN)
                     .setCollection(true)));

			return entityType;
		} else if (ATTRIBUTE_TYPE_FQN.equals(entityTypeName)) {
			final CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(ET_ATTRIBUTE_NAME);
			entityType.setAbstract(true);

			final CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("Name");

			final List<CsdlProperty> properties = new ArrayList<>();
			properties.add(new CsdlProperty().setName("Name").setType(STRING_TYPE_FQN));
			properties.add(new CsdlProperty().setName("ValueType").setType(STRING_TYPE_FQN));

			entityType.setKey(Collections.singletonList(propertyRef));
			entityType.setProperties(properties);

			return entityType;
		} else if (STRING_ATTRIBUTE_TYPE_FQN.equals(entityTypeName)) {
			final CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(ET_STRING_ATTRIBUTE_NAME);

			final List<CsdlProperty> properties = new ArrayList<>();
			properties.add(new CsdlProperty().setName("Value").setType(STRING_TYPE_FQN));

			entityType.setBaseType(ATTRIBUTE_TYPE_FQN);
			entityType.setProperties(properties);

			return entityType;
		} else if (INTEGER_ATTRIBUTE_TYPE_FQN.equals(entityTypeName)) {
			final CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(ET_INTEGER_ATTRIBUTE_NAME);

			final List<CsdlProperty> properties = new ArrayList<>();
			properties.add(new CsdlProperty().setName("Value").setType(INT_64_TYPE_FQN));

			entityType.setBaseType(ATTRIBUTE_TYPE_FQN);
			entityType.setProperties(properties);

			return entityType;
		} else if (DOUBLE_ATTRIBUTE_TYPE_FQN.equals(entityTypeName)) {
			final CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(ET_DOUBLE_ATTRIBUTE_NAME);

			final List<CsdlProperty> properties = new ArrayList<>();
			properties.add(new CsdlProperty().setName("Value").setType(DOUBLE_TYPE_FQN));

			entityType.setBaseType(ATTRIBUTE_TYPE_FQN);
			entityType.setProperties(properties);

			return entityType;
		} else if (BOOLEAN_ATTRIBUTE_TYPE_FQN.equals(entityTypeName)) {
			final CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(ET_BOOLEAN_ATTRIBUTE_NAME);

			final List<CsdlProperty> properties = new ArrayList<>();
			properties.add(new CsdlProperty().setName("Value").setType(BOOLEAN_TYPE_FQN));

			entityType.setBaseType(ATTRIBUTE_TYPE_FQN);
			entityType.setProperties(properties);

			return entityType;
		} else if (DATE_ATTRIBUTE_TYPE_FQN.equals(entityTypeName)) {
			final CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(ET_DATE_ATTRIBUTE_NAME);

			final List<CsdlProperty> properties = new ArrayList<>();
			properties.add(new CsdlProperty().setName("Value").setType(DATE_TIME_OFFSET_TYPE_FQN).setPrecision(3));

			entityType.setBaseType(ATTRIBUTE_TYPE_FQN);
			entityType.setProperties(properties);

			return entityType;
		} else if (QUICKLOOK_TYPE_FQN.equals(entityTypeName)) {
         final CsdlEntityType entityType = new CsdlEntityType();
         entityType.setName(ET_QUICKLOOK_NAME);

         final CsdlPropertyRef propertyRef = new CsdlPropertyRef();
         propertyRef.setName(QuicklookProperties.Image.name());
         entityType.setKey(Collections.singletonList(propertyRef));

         final List<CsdlProperty> properties = new ArrayList<>();
         properties.add(new CsdlProperty().setName(QuicklookProperties.Image.name()).setType(STRING_TYPE_FQN));
         entityType.setProperties(properties);
         entityType.setHasStream(true);

         return entityType;
      }

		return null;
	}

	@Override
	public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) throws ODataException {
		if (complexTypeName.equals(CHECKSUM_TYPE_FQN)) {
			CsdlComplexType entityType = new CsdlComplexType();
			entityType.setName(CHECKSUM_TYPE_FQN.getName());
			List<CsdlProperty> properties = new ArrayList<>();
			properties.add(new CsdlProperty().setName(Algorithm.name())
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));
			properties.add(new CsdlProperty().setName(Value.name())
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));
			properties.add(new CsdlProperty().setName(ChecksumDate.name())
					.setType(DATE_TIME_OFFSET_TYPE_FQN));
			entityType.setProperties(properties);
			return entityType;
		}
		if (complexTypeName.equals(TIMERANGE_TYPE_FQN)) {
			CsdlComplexType entityType = new CsdlComplexType();
			entityType.setName(TIMERANGE_TYPE_FQN.getName());
			List<CsdlProperty> properties = new ArrayList<>();
			properties.add(new CsdlProperty().setName(Start.name()).setType(DATE_TIME_OFFSET_TYPE_FQN));
			properties.add(new CsdlProperty().setName(End.name()).setType(DATE_TIME_OFFSET_TYPE_FQN));
			entityType.setProperties(properties);
			return entityType;
		} 
		
		if(complexTypeName.equals(PROPERTY_TYPE_FQN)) {
	         List<CsdlProperty> properties = new ArrayList<>();
	         properties.add(new CsdlProperty().setName("Name").setType(STRING_TYPE_FQN).setNullable(false));
	         properties.add(new CsdlProperty().setName("Value").setType(STRING_TYPE_FQN).setNullable(false));

	         CsdlComplexType entityType = new CsdlComplexType();
	         entityType.setName(PROPERTY_TYPE_FQN.getName());
	         entityType.setProperties(properties);
	         return entityType;
	      }
		
//		if (complexTypeName.equals(FUNCTION_INTERSECTS_FQN)) {
//			CsdlComplexType entityType = new CsdlComplexType();
//			entityType.setName(FUNCTION_INTERSECTS_FQN);
		// TODO
//			return entityType;
//		}
		return null;
	}

	@Override
	public CsdlEnumType getEnumType(final FullQualifiedName enumTypeName) throws ODataException {
		if (enumTypeName.equals(PRODUCTION_TYPE_TYPE_FQN)) {
			CsdlEnumType enumType = new CsdlEnumType();
			enumType.setName(PRODUCTION_TYPE_TYPE_FQN.getName());
			enumType.setUnderlyingType(INT_32_TYPE_FQN);
			List<CsdlEnumMember> productionTypeMembers = Arrays
					.asList(esa.s1pdgs.cpoc.prip.model.ProductionType.values()).stream()
					.map(v -> new CsdlEnumMember().setName(v.getName()).setValue(Integer.toString(v.getValue())))
					.collect(Collectors.toList());
			enumType.setMembers(productionTypeMembers);
			return enumType;
		}
		return null;
	}

	@Override
	public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
		if (!CONTAINER.equals(entityContainer)) {
			return null;
		}

		final CsdlEntitySet entitySet = new CsdlEntitySet();

		if (entitySetName.equals(ES_PRODUCTS_NAME)) {
			entitySet.setName(ES_PRODUCTS_NAME);
			entitySet.setType(ET_PRODUCT_FQN);

			final List<CsdlNavigationPropertyBinding> bindings = new ArrayList<>();

			final CsdlNavigationPropertyBinding attributesBinding = new CsdlNavigationPropertyBinding();
			attributesBinding.setPath(ATTRIBUTES_SET_NAME);
			attributesBinding.setTarget(ATTRIBUTES_SET_NAME);
			bindings.add(attributesBinding);

			final CsdlNavigationPropertyBinding stringAttributesBinding = new CsdlNavigationPropertyBinding();
			stringAttributesBinding.setPath(STRING_ATTRIBUTES_SET_NAME);
			stringAttributesBinding.setTarget(STRING_ATTRIBUTES_SET_NAME);
			bindings.add(stringAttributesBinding);

			final CsdlNavigationPropertyBinding doubleAttributesBinding = new CsdlNavigationPropertyBinding();
			doubleAttributesBinding.setPath(DOUBLE_ATTRIBUTES_SET_NAME);
			doubleAttributesBinding.setTarget(DOUBLE_ATTRIBUTES_SET_NAME);
			bindings.add(doubleAttributesBinding);

			final CsdlNavigationPropertyBinding booleanAttributesBinding = new CsdlNavigationPropertyBinding();
			booleanAttributesBinding.setPath(BOOLEAN_ATTRIBUTES_SET_NAME);
			booleanAttributesBinding.setTarget(BOOLEAN_ATTRIBUTES_SET_NAME);
			bindings.add(booleanAttributesBinding);

			final CsdlNavigationPropertyBinding integerAttributesBinding = new CsdlNavigationPropertyBinding();
			integerAttributesBinding.setPath(INTEGER_ATTRIBUTES_SET_NAME);
			integerAttributesBinding.setTarget(INTEGER_ATTRIBUTES_SET_NAME);
			bindings.add(integerAttributesBinding);

			final CsdlNavigationPropertyBinding dateAttributesBinding = new CsdlNavigationPropertyBinding();
			dateAttributesBinding.setPath(DATE_ATTRIBUTES_SET_NAME);
			dateAttributesBinding.setTarget(DATE_ATTRIBUTES_SET_NAME);
			bindings.add(dateAttributesBinding);
			
			final CsdlNavigationPropertyBinding quicklookBinding = new CsdlNavigationPropertyBinding();
			quicklookBinding.setPath(QUICKLOOK_SET_NAME);
			quicklookBinding.setTarget(QUICKLOOK_SET_NAME);
         bindings.add(quicklookBinding);

			entitySet.setNavigationPropertyBindings(bindings);
		} else if (entitySetName.equals(ATTRIBUTES_SET_NAME)) {
			entitySet.setName(ATTRIBUTES_SET_NAME);
			entitySet.setType(ATTRIBUTE_TYPE_FQN);
			entitySet.setIncludeInServiceDocument(false);
		} else if (entitySetName.equals(STRING_ATTRIBUTES_SET_NAME)) {
			entitySet.setName(STRING_ATTRIBUTES_SET_NAME);
			entitySet.setType(STRING_ATTRIBUTE_TYPE_FQN);
			entitySet.setIncludeInServiceDocument(false);
		} else if (entitySetName.equals(INTEGER_ATTRIBUTES_SET_NAME)) {
			entitySet.setName(INTEGER_ATTRIBUTES_SET_NAME);
			entitySet.setType(INTEGER_ATTRIBUTE_TYPE_FQN);
			entitySet.setIncludeInServiceDocument(false);
		} else if (entitySetName.equals(DOUBLE_ATTRIBUTES_SET_NAME)) {
			entitySet.setName(DOUBLE_ATTRIBUTES_SET_NAME);
			entitySet.setType(DOUBLE_ATTRIBUTE_TYPE_FQN);
			entitySet.setIncludeInServiceDocument(false);
		} else if (entitySetName.equals(BOOLEAN_ATTRIBUTES_SET_NAME)) {
			entitySet.setName(BOOLEAN_ATTRIBUTES_SET_NAME);
			entitySet.setType(BOOLEAN_ATTRIBUTE_TYPE_FQN);
			entitySet.setIncludeInServiceDocument(false);
		} else if (entitySetName.equals(DATE_ATTRIBUTES_SET_NAME)) {
			entitySet.setName(DATE_ATTRIBUTES_SET_NAME);
			entitySet.setType(DATE_ATTRIBUTE_TYPE_FQN);
			entitySet.setIncludeInServiceDocument(false);
      } else if (entitySetName.equals(QUICKLOOK_SET_NAME)) {
         entitySet.setName(QUICKLOOK_SET_NAME);
         entitySet.setType(QUICKLOOK_TYPE_FQN);
         entitySet.setIncludeInServiceDocument(false);
		} else {
			return null;
		}

		return entitySet;
	}

	@Override
	public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
		// This method is invoked when displaying the Service Document at e.g.
		// http://localhost:8080/DemoService/DemoService.svc
		if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
			CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
			entityContainerInfo.setContainerName(CONTAINER);
			return entityContainerInfo;
		}

		return null;
	}

	@Override
	public List<CsdlSchema> getSchemas() throws ODataException {
		final List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();

		final List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		entityTypes.add(this.getEntityType(ET_PRODUCT_FQN));
		entityTypes.add(this.getEntityType(ATTRIBUTE_TYPE_FQN));
		entityTypes.add(this.getEntityType(STRING_ATTRIBUTE_TYPE_FQN));
		entityTypes.add(this.getEntityType(INTEGER_ATTRIBUTE_TYPE_FQN));
		entityTypes.add(this.getEntityType(DOUBLE_ATTRIBUTE_TYPE_FQN));
		entityTypes.add(this.getEntityType(BOOLEAN_ATTRIBUTE_TYPE_FQN));
		entityTypes.add(this.getEntityType(DATE_ATTRIBUTE_TYPE_FQN));
		entityTypes.add(this.getEntityType(QUICKLOOK_TYPE_FQN));

		final List<CsdlComplexType> complexTypes = new ArrayList<CsdlComplexType>();
		complexTypes.add(getComplexType(CHECKSUM_TYPE_FQN));
		complexTypes.add(getComplexType(TIMERANGE_TYPE_FQN));
		complexTypes.add(getComplexType(PROPERTY_TYPE_FQN));
		
		final List<CsdlEnumType> enumTypes = new ArrayList<CsdlEnumType>();
		enumTypes.add(getEnumType(PRODUCTION_TYPE_TYPE_FQN));

		final CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(SERVICE_NAMESPACE);
		schema.setEntityTypes(entityTypes);
		schema.setComplexTypes(complexTypes);
		schema.setEnumTypes(enumTypes);
		schema.setEntityContainer(this.getEntityContainer());

		schema.setFunctions(getFunctions(FUNCTION_INTERSECTS_FQN));
		
		schema.setActions(getActions(FILTERLIST_ACTION_FQN));
		
		schemas.add(schema);

		return schemas;
	}

	@Override
	public CsdlEntityContainer getEntityContainer() throws ODataException {
		final List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();

		this.addEntitySet(entitySets, this.getEntitySet(CONTAINER, ES_PRODUCTS_NAME));
		this.addEntitySet(entitySets, this.getEntitySet(CONTAINER, ATTRIBUTES_SET_NAME));
		this.addEntitySet(entitySets, this.getEntitySet(CONTAINER, DATE_ATTRIBUTES_SET_NAME));
		this.addEntitySet(entitySets, this.getEntitySet(CONTAINER, STRING_ATTRIBUTES_SET_NAME));
		this.addEntitySet(entitySets, this.getEntitySet(CONTAINER, INTEGER_ATTRIBUTES_SET_NAME));
		this.addEntitySet(entitySets, this.getEntitySet(CONTAINER, DOUBLE_ATTRIBUTES_SET_NAME));
		this.addEntitySet(entitySets, this.getEntitySet(CONTAINER, BOOLEAN_ATTRIBUTES_SET_NAME));
		this.addEntitySet(entitySets, this.getEntitySet(CONTAINER, QUICKLOOK_SET_NAME));

		final CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		entityContainer.setName(CONTAINER_NAME);
		entityContainer.setEntitySets(entitySets);

		return entityContainer;
	}

	// --------------------------------------------------------------------------

	private void addEntitySet(List<CsdlEntitySet> entitySets, CsdlEntitySet entitySet) {
		if (null != entitySet) {
			entitySets.add(entitySet);
		}
	}

}
