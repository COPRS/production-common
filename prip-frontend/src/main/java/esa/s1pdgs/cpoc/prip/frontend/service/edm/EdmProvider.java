package esa.s1pdgs.cpoc.prip.frontend.service.edm;

import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Algorithm;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Checksums;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentLength;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ContentType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.End;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.EvictionDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Id;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Name;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.ProductionType;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.PublicationDate;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Start;
import static esa.s1pdgs.cpoc.prip.frontend.service.edm.EntityTypeProperties.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;

public class EdmProvider extends org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider {

	public static final String SERVICE_NAMESPACE = "S1PDGS";

	// Types
	public static final FullQualifiedName INT_32_TYPE_FQN = EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
	public static final FullQualifiedName INT_64_TYPE_FQN = EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
	public static final FullQualifiedName STRING_TYPE_FQN = EdmPrimitiveTypeKind.String.getFullQualifiedName();
	public static final FullQualifiedName DATE_TIME_OFFSET_TYPE_FQN = EdmPrimitiveTypeKind.DateTimeOffset
			.getFullQualifiedName();
	public static final FullQualifiedName CHECKSUM_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE, Checksums.name());
	
	public static final FullQualifiedName CONTENT_DATE_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE, ContentDate.name());
	
	public static final FullQualifiedName PRODUCTION_TYPE_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE, ProductionType.name());

	// EDM Container
	public static final String CONTAINER_NAME = "PRIPData";
	public static final FullQualifiedName CONTAINER = new FullQualifiedName(SERVICE_NAMESPACE, CONTAINER_NAME);

	// Entity Types Names
	public static final String ET_PRODUCT_NAME = "Product";
	public static final FullQualifiedName ET_PRODUCT_FQN = new FullQualifiedName(SERVICE_NAMESPACE, ET_PRODUCT_NAME);

	// Entity Set Names
	public static final String ES_PRODUCTS_NAME = "Products";

	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
		if (entityTypeName.equals(ET_PRODUCT_FQN)) {
			CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(ET_PRODUCT_NAME);

			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName(EntityTypeProperties.Id.name());

			entityType.setKey(Collections.singletonList(propertyRef));

			List<CsdlProperty> properties = new ArrayList<>();

			properties.add(new CsdlProperty().setName(Id.name()).setType(STRING_TYPE_FQN));
			properties.add(new CsdlProperty().setName(Name.name()).setType(STRING_TYPE_FQN));
			properties.add(new CsdlProperty().setName(ContentType.name()).setType(STRING_TYPE_FQN));
			properties.add(new CsdlProperty().setName(ContentLength.name()).setType(INT_64_TYPE_FQN));
			properties.add(new CsdlProperty().setName(PublicationDate.name()).setType(DATE_TIME_OFFSET_TYPE_FQN));
			properties.add(new CsdlProperty().setName(EvictionDate.name()).setType(DATE_TIME_OFFSET_TYPE_FQN));
			properties.add(new CsdlProperty().setName(Checksums.name()).setType(CHECKSUM_TYPE_FQN).setCollection(true));
			properties.add(new CsdlProperty().setName(ProductionType.name()).setType(PRODUCTION_TYPE_TYPE_FQN));
			properties.add(new CsdlProperty().setName(ContentDate.name()).setType(CONTENT_DATE_TYPE_FQN));

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
			entityType.setProperties(properties);
			return entityType;
		}
		if (complexTypeName.equals(CONTENT_DATE_TYPE_FQN)) {
			CsdlComplexType entityType = new CsdlComplexType();
			entityType.setName(CHECKSUM_TYPE_FQN.getName());
			List<CsdlProperty> properties = new ArrayList<>();
			properties.add(new CsdlProperty().setName(Start.name())
					.setType(DATE_TIME_OFFSET_TYPE_FQN));
			properties.add(new CsdlProperty().setName(End.name())
					.setType(DATE_TIME_OFFSET_TYPE_FQN));
			entityType.setProperties(properties);
			return entityType;
		}
		return null;
	}

	@Override
	public CsdlEnumType getEnumType(final FullQualifiedName enumTypeName) throws ODataException {
		if(enumTypeName.equals(PRODUCTION_TYPE_TYPE_FQN)) {
			CsdlEnumType enumType = new CsdlEnumType();
			enumType.setName(PRODUCTION_TYPE_TYPE_FQN.getName());
			enumType.setUnderlyingType(INT_32_TYPE_FQN);
			List<CsdlEnumMember> productionTypeMembers = Arrays.asList(esa.s1pdgs.cpoc.prip.model.ProductionType.values()).stream()
					.map(v -> new CsdlEnumMember().setName(v.getName()).setValue(Integer.toString(v.getValue()))).collect(Collectors.toList());
			enumType.setMembers(productionTypeMembers);
			return enumType;
		}
		return null;
	}
	
	@Override
	public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
		if (entityContainer.equals(CONTAINER)) {
			if (entitySetName.equals(ES_PRODUCTS_NAME)) {
				CsdlEntitySet entitySet = new CsdlEntitySet();
				entitySet.setName(ES_PRODUCTS_NAME);
				entitySet.setType(ET_PRODUCT_FQN);

				return entitySet;
			}
		}

		return null;
	}

	@Override
	public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
		if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
			CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
			entityContainerInfo.setContainerName(CONTAINER);
			return entityContainerInfo;
		}

		return null;
	}

	@Override
	public List<CsdlSchema> getSchemas() throws ODataException {
		List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();

		List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		entityTypes.add(getEntityType(ET_PRODUCT_FQN));

		List<CsdlComplexType> complexTypes = new ArrayList<CsdlComplexType>();
		complexTypes.add(getComplexType(CHECKSUM_TYPE_FQN));

		List<CsdlEnumType> enumTypes = new ArrayList<CsdlEnumType>();
		enumTypes.add(getEnumType(PRODUCTION_TYPE_TYPE_FQN));
		
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(SERVICE_NAMESPACE);
		schema.setEntityTypes(entityTypes);
		schema.setComplexTypes(complexTypes);
		schema.setEnumTypes(enumTypes);
		schema.setEntityContainer(getEntityContainer());

		schemas.add(schema);

		return schemas;
	}

	@Override
	public CsdlEntityContainer getEntityContainer() throws ODataException {
		List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
		entitySets.add(getEntitySet(CONTAINER, ES_PRODUCTS_NAME));

		CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		entityContainer.setName(CONTAINER_NAME);
		entityContainer.setEntitySets(entitySets);

		return entityContainer;
	}

}
