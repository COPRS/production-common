package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MaskExtractor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class AuxMetadataExtractor extends AbstractMetadataExtractor {	
	private static final List<String> AUX_ECE_TYPES = Arrays.asList("AMV_ERRMAT", "AMH_ERRMAT");
	
	public AuxMetadataExtractor(
			final EsServices esServices, 
			final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, 
			final String localDirectory,
			final ProcessConfiguration processConfiguration, 
			final ObsClient obsClient) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
	}

	@Override
	public JSONObject extract(final ReportingFactory reportingFactory, final GenericMessageDto<CatalogJob> message) throws AbstractCodedException {
		final CatalogJob job = message.getBody();
		final File metadataFile = downloadMetadataFileToLocalFolder(
				reportingFactory,
				ProductFamily.AUXILIARY_FILE, 
				job.getKeyObjectStorage()
		);
		try {			
			final AuxDescriptor configFileDesc = fileDescriptorBuilder.buildAuxDescriptor(metadataFile);

			// Build metadata from file and extracted
			final JSONObject obj = mdBuilder.buildConfigFileMetadata(configFileDesc, metadataFile);

			/*
			 * In case we are having a land mask file, we are uploading the geo shape information
			 * to a dedicated elastic search index
			 */
			// TODO: Having this logic in the Auxiliary Extract might not be the best place, maybe a new one for masks would be better
			if (configFileDesc.getProductType().equals("MSK__LAND_")) {			
				try {					
					final List<JSONObject> landMasks = new MaskExtractor().extract(metadataFile);
					logger.info("Uploading {} land mask polygons", landMasks.size());
					int c=0;
					for (final JSONObject land : landMasks) {
						String id = configFileDesc.getProductName() + "/features/" + c;
						logger.debug("Uploading land mask {}", id);
						logger.trace("land mask json: {}",land.toString());
						esServices.createLandmaskGeoMetadata(land, id);
						logger.debug("Finished uploading land mask {}", id);
						c++;
					}
				} catch (final Exception ex) {
					logger.error("An error occurred while ingesting land mask documents: {}", LogUtils.toString(ex));
					throw new InternalErrorException(Exceptions.messageOf(ex), ex);
				}
			} else if (configFileDesc.getProductType().equals("MSK_OCEAN_")) {
				try {
					final List<JSONObject> oceanMasks = new MaskExtractor().extract(metadataFile);
					logger.info("Uploading {} ocean mask polygons", oceanMasks.size());
					int c=0;
					for (final JSONObject oceanmask : oceanMasks) {
						String id = configFileDesc.getProductName() + "/features/" + c;
						logger.debug("Uploading ocean mask {}", id);
						logger.trace("oceans mask json: {}",oceanmask.toString());
						esServices.createOceanMaskGeoMetadata(oceanmask, id);
						logger.debug("Finished uploading ocean mask {}", id);
						c++;
					}
				} catch (final Exception ex) {
					logger.error("An error occurred while ingesting ocean mask documents: {}", LogUtils.toString(ex));
					throw new InternalErrorException(Exceptions.messageOf(ex), ex);
				}
			} else if (configFileDesc.getProductType().equals("MSK_OVRPAS")) {
				try {
					final List<JSONObject> overpassMasks = new MaskExtractor().extract(metadataFile);
					logger.info("Uploading {} overpass mask polygons", overpassMasks.size());
					int c=0;
					for (final JSONObject overpass : overpassMasks) {
						String id = configFileDesc.getProductName() + "/features/" + c;
						logger.debug("Uploading overpass mask {}", id);
						logger.trace("overpass mask json: {}",overpass.toString());
						esServices.createOverpassMaskGeoMetadata(overpass, id);
						logger.debug("Finished uploading overpass mask {}", id);
						c++;
					}
				} catch (final Exception ex) {
					logger.error("An error occurred while ingesting overpass mask documents: {}", LogUtils.toString(ex));
					throw new InternalErrorException(Exceptions.messageOf(ex), ex);
				}
			}
			return obj;
		}
		finally
		{
			FileUtils.delete(metadataFile.getPath());
		}
		

	}
}
