package esa.s1pdgs.cpoc.prip.frontend.service.processor;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.MediaEntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.prip.frontend.report.PripReportingInput;
import esa.s1pdgs.cpoc.prip.frontend.report.PripReportingOutput;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class ProductEntityProcessor implements EntityProcessor, MediaEntityProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductEntityProcessor.class);
	
	private OData odata;
	private ServiceMetadata serviceMetadata;
	private PripMetadataRepository pripMetadataRepository;
	private ObsClient obsClient;
	private long downloadUrlExpirationTimeInSeconds;

	public ProductEntityProcessor(final PripMetadataRepository pripMetadataRepository,
			final ObsClient obsClient, final long downloadUrlExpirationTimeInSeconds) {
		this.pripMetadataRepository = pripMetadataRepository;
		this.obsClient = obsClient;
		this.downloadUrlExpirationTimeInSeconds = downloadUrlExpirationTimeInSeconds;
	}
	
	@Override
	public void init(final OData odata, final ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void readEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo, final ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
		final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		
		final List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		if (EdmProvider.ES_PRODUCTS_NAME.equals(edmEntitySet.getName()) && keyPredicates.size() >= 1) {
			final String uuid = keyPredicates.get(0).getText().replace("'", "");
			final PripMetadata foundPripMetadata = pripMetadataRepository.findById(uuid);
			if (null != foundPripMetadata) {
				final Entity entity = MappingUtil.pripMetadataToEntity(foundPripMetadata, request.getRawBaseUri());
							
				final ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
				final EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
				
				final ODataSerializer serializer = odata.createSerializer(responseFormat);
				final SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), entity, options);
				final InputStream entityStream = serializerResult.getContent();
				
				response.setContent(entityStream);
				response.setStatusCode(HttpStatusCode.OK.getStatusCode());
				response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
				LOGGER.debug("Serving product metadata for id {}", uuid);
			} else {
				response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());				
				response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
				LOGGER.debug("No product metadata found with id {}", uuid);
			}
		}
	}

	@Override
	public void readMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		
		final List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		if (EdmProvider.ES_PRODUCTS_NAME.equals(edmEntitySet.getName()) && keyPredicates.size() >= 1) {
			final String uuid = keyPredicates.get(0).getText().replace("'", "");
			final PripMetadata foundPripMetadata = pripMetadataRepository.findById(uuid);
			if (null != foundPripMetadata) {		
				final Reporting reporting = ReportingUtils.newReportingBuilder()
						.newReporting("PripTempDownloadUrl");
				// currently used username
				final String username = "anonymous";
				
				reporting.begin(
						new PripReportingInput(foundPripMetadata.getObsKey(), username),
						new ReportingMessage("Creating temporary download URL for obsKey %s for user %s", foundPripMetadata.getObsKey(), username)
				);
				URL url;
				try {
					url = obsClient.createTemporaryDownloadUrl(new ObsObject(foundPripMetadata.getProductFamily(),
							foundPripMetadata.getObsKey()), downloadUrlExpirationTimeInSeconds);
					final String urlString = url.toString();
					reporting.end(
							new PripReportingOutput(urlString),
							new ReportingMessage("Temporary download URL for obsKey %s for user %s", foundPripMetadata.getObsKey(), username)
					);
				} catch (ObsException | ObsServiceException e) {
					LOGGER.error("Could not create temporary download URL for product with id '{}'", uuid);
					reporting.error(
							new ReportingMessage(
									"Error on creating download URL for obsKey %s for user %s: %s",
									foundPripMetadata.getObsKey(),
									username,
									LogUtils.toString(e)
							)
					);
					response.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
					return;
				}
				
				response.setStatusCode(HttpStatusCode.TEMPORARY_REDIRECT.getStatusCode());
				response.setHeader(HttpHeader.LOCATION, url.toString());
				response.setHeader("Content-Disposition", "attachment; filename=\"" + foundPripMetadata.getName() + "\"");
				LOGGER.debug("Redirecting to product data for id '{}'", uuid);
			} else {
				response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());				
				response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
				LOGGER.debug("No product metadata found with id '{}'", uuid);
			}
		}
	}
	
	@Override
	public void createEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo, final ContentType requestFormat,
			final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		// Not supported
	}

	@Override
	public void updateEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo, final ContentType requestFormat,
			final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		// Not supported
	}

	@Override
	public void deleteEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {
		// Not supported
	}

	@Override
	public void createMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType requestFormat, final ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
		// Not supported
	}

	@Override
	public void updateMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType requestFormat, final ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
		// Not supported
	}

	@Override
	public void deleteMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {
		// Not supported
	}
	
}
