/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.prip.frontend.service.processor;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;

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
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.core.uri.UriResourceWithKeysImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.RecoverableDataAccessException;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CompressionEventUtil;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.prip.frontend.report.PripReportingInput;
import esa.s1pdgs.cpoc.prip.frontend.report.PripReportingOutput;
import esa.s1pdgs.cpoc.prip.frontend.service.edm.EdmProvider;
import esa.s1pdgs.cpoc.prip.frontend.service.mapping.MappingUtil;
import esa.s1pdgs.cpoc.prip.frontend.utils.OlingoUtil;
import esa.s1pdgs.cpoc.prip.metadata.PripMetadataRepository;
import esa.s1pdgs.cpoc.prip.model.PripMetadata;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class ProductEntityProcessor implements EntityProcessor, MediaEntityProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductEntityProcessor.class);
	
	private final CommonConfigurationProperties commonProperties;
	private OData odata;
	private ServiceMetadata serviceMetadata;
	private final PripMetadataRepository pripMetadataRepository;
	private final ObsClient obsClient;
	private final long downloadUrlExpirationTimeInSeconds;
	private final String username;

	public ProductEntityProcessor(final CommonConfigurationProperties commonProperties,
			final PripMetadataRepository pripMetadataRepository, final ObsClient obsClient,
			final long downloadUrlExpirationTimeInSeconds, final String username) {
		this.commonProperties = commonProperties;
		this.pripMetadataRepository = pripMetadataRepository;
		this.obsClient = obsClient;
		this.downloadUrlExpirationTimeInSeconds = downloadUrlExpirationTimeInSeconds;
		this.username = username;
	}
	
	@Override
	public void init(final OData odata, final ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void readEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
	      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {	   
		final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
		final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
		final EdmEntitySet rootEdmEntitySet = uriResourceEntitySet.getEntitySet();
		if (EdmProvider.ES_PRODUCTS_NAME.equals(rootEdmEntitySet.getName())) {
   		switch (resourceParts.size()) {
   		   case 1: serveProduct(request, response, uriInfo, responseFormat, rootEdmEntitySet); break;
   		   case 2: final EdmEntitySet secondLevelEdmEntitySet = OlingoUtil.getNavigationTargetEntitySet(rootEdmEntitySet,
   		                  ((UriResourceNavigation) resourceParts.get(1)).getProperty());
   		         if (EdmProvider.QUICKLOOK_SET_NAME.equals(secondLevelEdmEntitySet.getName())) {
   		            serveQuicklook(request, response, uriInfo, responseFormat, secondLevelEdmEntitySet);
   		            break;
   		         }
   		   default: throw new ODataApplicationException("Resource not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
   		}
		}
	}
	
	private void serveProduct(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
         final ContentType responseFormat, final EdmEntitySet edmEntitySet)
               throws ODataApplicationException, ODataLibraryException {
	   final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
	   final List<UriParameter> keyPredicates = ((UriResourceEntitySet)resourceParts.get(0)).getKeyPredicates();
      final String uuid = keyPredicates.get(0).getText().replace("'", "");
      try {
         final PripMetadata foundPripMetadata = pripMetadataRepository.findById(uuid);
         if (null != foundPripMetadata) {
            final Entity entity = MappingUtil.pripMetadataToEntity(foundPripMetadata, request.getRawBaseUri());
            final InputStream serializedContent = serializeEntity(entity, edmEntitySet, uriInfo.getExpandOption(), responseFormat);
            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
            LOGGER.debug("Serving product metadata for id {}", uuid);
         } else {
            response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
            LOGGER.debug("No product metadata found with id {}", uuid);
         }
      } catch (RecoverableDataAccessException e) {
         throw new ODataApplicationException(HttpStatusCode.SERVICE_UNAVAILABLE.getInfo(),
               HttpStatusCode.SERVICE_UNAVAILABLE.getStatusCode(), Locale.ROOT);
      }
   }
	
	private void serveQuicklook(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
         final ContentType responseFormat, final EdmEntitySet edmEntitySet) throws ODataApplicationException, ODataLibraryException {
	   final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
	   final List<UriParameter> keyPredicates = ((UriResourceEntitySet)resourceParts.get(0)).getKeyPredicates();
	   final ExpandOption expandOption = (ExpandOption) uriInfo.getExpandOption();
	   final String uuid = keyPredicates.get(0).getText().replace("'", "");
	   try {
         final PripMetadata foundPripMetadata = pripMetadataRepository.findById(uuid);
         if (null != foundPripMetadata) {
            final UriResourceWithKeysImpl uriResourceWithKeys = (UriResourceWithKeysImpl) resourceParts.get(1);
            final List<UriParameter> quicklookKeyPredicates = uriResourceWithKeys.getKeyPredicates();
            final String quicklookId = quicklookKeyPredicates.get(0).getText().replace("'", "");

            if (foundPripMetadata.getBrowseKeys().contains(quicklookId)) {
               final Entity entity = MappingUtil.quicklookEntityOf(quicklookId);
               
               final InputStream serializedContent = serializeEntity(entity, edmEntitySet, expandOption, responseFormat);
               response.setContent(serializedContent);
               response.setStatusCode(HttpStatusCode.OK.getStatusCode());
               response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
               LOGGER.debug("Serving quicklook metadata for id {}", quicklookId);
            } else {
               response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
               response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
               LOGGER.debug("No quicklook metadata found with id {}", quicklookId);
            }
         } else {
            response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
            LOGGER.debug("No product metadata found with id {}", uuid);
         }
      } catch (RecoverableDataAccessException e) {
         throw new ODataApplicationException(HttpStatusCode.SERVICE_UNAVAILABLE.getInfo(),
               HttpStatusCode.SERVICE_UNAVAILABLE.getStatusCode(), Locale.ROOT);
      }	   
	}

	@Override
	public void readMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
	   final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
      final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
      final EdmEntitySet rootEdmEntitySet = uriResourceEntitySet.getEntitySet();
      if (EdmProvider.ES_PRODUCTS_NAME.equals(rootEdmEntitySet.getName())) {
         switch (resourceParts.size() - 1 /* not counting the "$value" part */ ) {
            case 1: serveProductDownload(request, response, uriInfo, responseFormat); break;
            case 2: final EdmEntitySet secondLevelEdmEntitySet = OlingoUtil.getNavigationTargetEntitySet(rootEdmEntitySet,
                           ((UriResourceNavigation) resourceParts.get(1)).getProperty());
                  if (EdmProvider.QUICKLOOK_SET_NAME.equals(secondLevelEdmEntitySet.getName())) {
                     serveQuicklookDownload(request, response, uriInfo, responseFormat);
                     break;
                  }
            default: throw new ODataApplicationException("Resource not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
         }
      }      
	}
    
	public void serveProductDownload(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
         final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
	   final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
	   final List<UriParameter> keyPredicates = ((UriResourceEntitySet)resourceParts.get(0)).getKeyPredicates();
		final String uuid = keyPredicates.get(0).getText().replace("'", "");
		try {
			final PripMetadata foundPripMetadata = pripMetadataRepository.findById(uuid);
			if (null != foundPripMetadata) {
				final Reporting reporting = ReportingUtils
						.newReportingBuilder(MissionId.fromFileName(foundPripMetadata.getObsKey()))
						.rsChainName(commonProperties.getRsChainName())
						.rsChainVersion(commonProperties.getRsChainVersion())
						.newReporting("PripTempDownloadUrl");
				
				reporting.begin(
						PripReportingInput.newInstance(
								foundPripMetadata.getObsKey(), 
								username, 
								foundPripMetadata.getProductFamily()
						),
						new ReportingMessage(
								"Creating temporary download URL for obsKey %s for user %s", 
								foundPripMetadata.getObsKey(), 
								username
						)
				);
				final URL url;
				try {
					url = obsClient.createTemporaryDownloadUrl(
							new ObsObject(
									foundPripMetadata.getProductFamily(),
									foundPripMetadata.getObsKey()
							), 
							downloadUrlExpirationTimeInSeconds
					);
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
		} catch (RecoverableDataAccessException e) {
			throw new ODataApplicationException(HttpStatusCode.SERVICE_UNAVAILABLE.getInfo(),
					HttpStatusCode.SERVICE_UNAVAILABLE.getStatusCode(), Locale.ROOT);
		}
	}

	public void serveQuicklookDownload(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
         final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
	   final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
      final List<UriParameter> keyPredicates = ((UriResourceEntitySet)resourceParts.get(0)).getKeyPredicates();
      final String uuid = keyPredicates.get(0).getText().replace("'", "");
      try {
         final PripMetadata foundPripMetadata = pripMetadataRepository.findById(uuid);
         if (null != foundPripMetadata) {
            final UriResourceWithKeysImpl uriResourceWithKeys = (UriResourceWithKeysImpl)resourceParts.get(1);
            final List<UriParameter> quicklookKeyPredicates = uriResourceWithKeys.getKeyPredicates();
            final String quicklookId = quicklookKeyPredicates.get(0).getText().replace("'", "");
            
            if (foundPripMetadata.getBrowseKeys().contains(quicklookId)) {
               final ProductFamily productFamily = CompressionEventUtil.removeZipSuffixFromProductFamily(
                     foundPripMetadata.getProductFamily());
            
               final Reporting reporting = ReportingUtils
                     .newReportingBuilder(MissionId.fromFileName(quicklookId))
                     .rsChainName(commonProperties.getRsChainName())
                     .rsChainVersion(commonProperties.getRsChainVersion())
                     .newReporting("PripTempQuicklookUrl");
               
               reporting.begin(
                     PripReportingInput.newInstance(
                           quicklookId, 
                           username, 
                           productFamily
                     ),
                     new ReportingMessage(
                           "Creating temporary quicklook URL for obsKey %s for user %s", 
                           quicklookId, 
                           username
                     )
               );
               final URL url;
               try {
                  url = obsClient.createTemporaryDownloadUrl(
                        new ObsObject(
                              productFamily,
                              quicklookId
                        ), 
                        downloadUrlExpirationTimeInSeconds
                  );
                  final String urlString = url.toString();
                  reporting.end(
                        new PripReportingOutput(urlString),
                        new ReportingMessage("Temporary quicklook URL for obsKey %s for user %s", quicklookId, username)
                  );
               } catch (ObsException | ObsServiceException e) {
                  LOGGER.error("Could not create temporary quicklook URL for product with id '{}'", uuid);
                  reporting.error(
                        new ReportingMessage(
                              "Error on creating quicklook URL for obsKey %s for user %s: %s",
                              quicklookId,
                              username,
                              LogUtils.toString(e)
                        )
                  );
                  response.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
                  return;
               }
               
               response.setStatusCode(HttpStatusCode.TEMPORARY_REDIRECT.getStatusCode());
               response.setHeader(HttpHeader.LOCATION, url.toString());
               response.setHeader("Content-Disposition", "attachment; filename=\"" + quicklookId + "\"");
               LOGGER.debug("Redirecting to quicklook image for id '{}'", quicklookId);
            } else {
               response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
               response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
               LOGGER.debug("No quicklook image found with id '{}'", quicklookId);
            }
         } else {
            response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
            LOGGER.debug("No product metadata found with id '{}'", uuid);
         }
      } catch (RecoverableDataAccessException e) {
         throw new ODataApplicationException(HttpStatusCode.SERVICE_UNAVAILABLE.getInfo(),
               HttpStatusCode.SERVICE_UNAVAILABLE.getStatusCode(), Locale.ROOT);
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
	
	private InputStream serializeEntity(Entity entity, EdmEntitySet edmEntitySet, ExpandOption expandOption,
	      final ContentType responseFormat) throws ODataLibraryException {
      final ContextURL contextUrl = OlingoUtil.getContextUrl(edmEntitySet, edmEntitySet.getEntityType(), true);
      final EntitySerializerOptions options = EntitySerializerOptions.with()
            .contextURL(contextUrl).expand(expandOption).build();
	   final ODataSerializer serializer = odata.createSerializer(responseFormat);
      final SerializerResult serializerResult = serializer.entity(serviceMetadata,
            edmEntitySet.getEntityType(), entity, options);
	   return serializerResult.getContent();
	}
	
}
