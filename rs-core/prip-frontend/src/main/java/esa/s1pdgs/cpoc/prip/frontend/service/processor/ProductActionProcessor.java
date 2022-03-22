package esa.s1pdgs.cpoc.prip.frontend.service.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.prefer.Preferences.Return;
import org.apache.olingo.server.api.prefer.PreferencesApplied;
import org.apache.olingo.server.api.processor.ActionEntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.RecoverableDataAccessException;

import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.prip.frontend.utils.OlingoUtil;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;

public class ProductActionProcessor implements ActionEntityCollectionProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductActionProcessor.class);

	private OData odata;
	private ServiceMetadata serviceMetadata;
	private final PripMetadataRepository pripMetadataRepository;

	public ProductActionProcessor(PripMetadataRepository pripMetadataRepository) {
		this.pripMetadataRepository = pripMetadataRepository;
	}

	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;

	}

	@Override
	public void processActionEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType requestFormat, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {

		OlingoUtil.validate(uriInfo);

		Map<String, Parameter> parameters = new HashMap<String, Parameter>();
		EdmAction action = null;
		EntityCollection collection = null;

		if (requestFormat == null) {
			throw new ODataApplicationException("The content type has not been set in the request.",
					HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
		}

		List<UriResource> resourcePaths = uriInfo.asUriInfoResource().getUriResourceParts();
		final ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
		UriResourceEntitySet boundEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		if (resourcePaths.size() > 1) {
			// Check if there is a navigation segment added after the bound parameter
			// if(resourcePaths.get(1) instanceof UriResourceAction) {
			// action = ((UriResourceAction) resourcePaths.get(2)).getAction();
			// throw new ODataApplicationException("Action " + action.getName() + " is not
			// yet implemented.",
			// HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
			// } else {
			action = ((UriResourceAction) resourcePaths.get(1)).getAction();
			parameters = deserializer.actionParameters(request.getBody(), action).getActionParameters();
			collection = processProductsByNameFilterListAction(action, parameters, request.getRawBaseUri());
			// }

			if (!EdmProvider.FILTERLIST_ACTION_FQN.equals(action.getFullQualifiedName())) {
				throw new ODataApplicationException("Action " + action.getName() + " is not yet implemented.",
						HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
			}
		}
		// Collections must never be null.
		// Not nullable return types must not contain a null value.
		if (collection == null || collection.getEntities().contains(null) && !action.getReturnType().isNullable()) {
			throw new ODataApplicationException("The action could not be executed.",
					HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
		}

		final Return returnPreference = odata.createPreferences(request.getHeaders(HttpHeader.PREFER)).getReturn();

		if (returnPreference == null || returnPreference == Return.REPRESENTATION) {
			final EdmEntitySet edmEntitySet = boundEntitySet.getEntitySet();
			final EdmEntityType type = (EdmEntityType) action.getReturnType().getType();
			final EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
					.contextURL(isODataMetadataNone(responseFormat) ? null
							: OlingoUtil.getContextUrl(action.getReturnedEntitySet(edmEntitySet), type, false))
					.build();
			response.setContent(odata.createSerializer(responseFormat)
					.entityCollection(serviceMetadata, type, collection, options).getContent());
			response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
			response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		} else {
			response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
		}

		if (returnPreference != null) {
			response.setHeader(HttpHeader.PREFERENCE_APPLIED,
					PreferencesApplied.with().returnRepresentation(returnPreference).build().toValueString());
		}

	}

	protected boolean isODataMetadataNone(final ContentType contentType) {
		return contentType.isCompatible(ContentType.APPLICATION_JSON) && ContentType.VALUE_ODATA_METADATA_NONE
				.equalsIgnoreCase(contentType.getParameter(ContentType.PARAMETER_ODATA_METADATA));
	}

	private EntityCollection processProductsByNameFilterListAction(EdmAction action, Map<String, Parameter> parameters,
			String rawBaseUri) throws ODataApplicationException {

		EntityCollection collection = new EntityCollection();

		if (EdmProvider.ACTION_FILTERLIST.equals(action.getName())) {

			if (parameters.get(EdmProvider.PARAMETER_FILTERPRODUCTS) != null
					&& parameters.get(EdmProvider.PARAMETER_FILTERPRODUCTS).getType() != null
					&& parameters.get(EdmProvider.PARAMETER_FILTERPRODUCTS).isCollection()
					&& parameters.get(EdmProvider.PARAMETER_FILTERPRODUCTS).getType()
							.equals(EdmProvider.PROPERTY_TYPE_FQN.toString())) {

				List<?> parameterlist = parameters.get(EdmProvider.PARAMETER_FILTERPRODUCTS).asCollection();

				List<String> productNameList = new ArrayList<>();
				if (parameterlist != null && parameterlist.size() > 0) {
					for (Object parameter : parameterlist) {
						Property property = ((ComplexValue) parameter).getValue().get(0);// typeName =
																							// OData.CSC.Property
						if (property != null && property.getValue() != null && property.getName().equals("Name"))
							productNameList.add(property.getValue().toString());
						else {
							LOGGER.debug("processProductsByNameFilterListAction - invalid Parameter");
							throw new ODataApplicationException("Invalid Parameters",
									HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
						}

					}

					collection = queryAndMapToEntities(productNameList, rawBaseUri);
				} else {
					LOGGER.debug("processProductsByNameFilterListAction - Parameter {} is missing ",
							EdmProvider.PARAMETER_FILTERPRODUCTS);

					StringBuffer msg = new StringBuffer("Parameter ").append(EdmProvider.PARAMETER_FILTERPRODUCTS)
							.append(" is missing ");
					throw new ODataApplicationException(msg.toString(), HttpStatusCode.BAD_REQUEST.getStatusCode(),
							Locale.ROOT);
				}
			} else {
				LOGGER.debug("processProductsByNameFilterListAction - no or invalid Parameters");
				throw new ODataApplicationException("no or invalid Parameters",
						HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
			}
		} else {
			LOGGER.debug("processProductsByNameFilterListAction - invalid Action {} " + action.getName());
			StringBuffer msg = new StringBuffer("invalid Action ").append("action.getName()");
			throw new ODataApplicationException(msg.toString(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
					Locale.ROOT);
		}
		return collection;

	}

	private EntityCollection queryAndMapToEntities(List<String> productNameList, String rawBaseUri)
			throws ODataApplicationException {

		EntityCollection entityCollection = new EntityCollection();
		for (String productName : productNameList) {
			PripMetadata metadata = null;
			try {
				metadata = this.pripMetadataRepository.findByName(productName);
			} catch (Exception e) {
				throw new ODataApplicationException(HttpStatusCode.SERVICE_UNAVAILABLE.getInfo(),
						HttpStatusCode.SERVICE_UNAVAILABLE.getStatusCode(), Locale.ROOT);
			}
			if (metadata != null) {
				entityCollection.getEntities().add(MappingUtil.pripMetadataToEntity(metadata, rawBaseUri));
			}
		}
		return entityCollection;
	}

}
