package esa.s1pdgs.cpoc.prip.service.edm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;

public class EdmProvider extends org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider {

	public static final String SERVICE_NAMESPACE = "S1PDGS";

	// Types
	public static final FullQualifiedName INT_64_TYPE_FQN = EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
	public static final FullQualifiedName STRING_TYPE_FQN = EdmPrimitiveTypeKind.String.getFullQualifiedName();
	public static final FullQualifiedName DATE_TIME_OFFSET_TYPE_FQN = EdmPrimitiveTypeKind.DateTimeOffset
			.getFullQualifiedName();
	public static final FullQualifiedName CHECKSUM_TYPE_FQN = new FullQualifiedName(SERVICE_NAMESPACE, "Checksum");

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
			propertyRef.setName("Id");

			entityType.setKey(Collections.singletonList(propertyRef));

			List<CsdlProperty> properties = new ArrayList<>();

			properties.add(new CsdlProperty().setName("Id").setType(STRING_TYPE_FQN));
			properties.add(new CsdlProperty().setName("Name").setType(STRING_TYPE_FQN));
			properties.add(new CsdlProperty().setName("ContentType").setType(STRING_TYPE_FQN));
			properties.add(new CsdlProperty().setName("ContentLength").setType(INT_64_TYPE_FQN));
			properties.add(new CsdlProperty().setName("CreationDate").setType(DATE_TIME_OFFSET_TYPE_FQN));
			properties.add(new CsdlProperty().setName("EvictionDate").setType(DATE_TIME_OFFSET_TYPE_FQN));
			properties.add(new CsdlProperty().setName("Checksums").setType(CHECKSUM_TYPE_FQN).setCollection(true));

			entityType.setProperties(properties);
			
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
			properties.add(new CsdlProperty().setName("Algorithm")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));
			properties.add(
					new CsdlProperty().setName("Value").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));
			entityType.setProperties(properties);
			return entityType;
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

		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(SERVICE_NAMESPACE);
		schema.setEntityTypes(entityTypes);
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
