package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.MaskType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MaskExtractor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class AuxMetadataExtractor extends AbstractMetadataExtractor {
	private static final List<String> AUX_ECE_TYPES = Arrays.asList("AMV_ERRMAT", "AMH_ERRMAT");

	public AuxMetadataExtractor(final EsServices esServices, final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, final String localDirectory,
			final ProcessConfiguration processConfiguration, final ObsClient obsClient) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
	}

	@Override
	public JSONObject extract(final ReportingFactory reportingFactory, final CatalogJob job)
			throws AbstractCodedException {
		final File metadataFile = downloadMetadataFileToLocalFolder(reportingFactory, ProductFamily.AUXILIARY_FILE,
				job.getKeyObjectStorage());
		try {
			final AuxDescriptor configFileDesc = fileDescriptorBuilder.buildAuxDescriptor(metadataFile);

			// Build metadata from file and extracted
			final JSONObject obj = mdBuilder.buildConfigFileMetadata(configFileDesc, metadataFile);

			/*
			 * In case we are having a land mask file, we are uploading the geo shape
			 * information to a dedicated elastic search index
			 */
			// TODO: Having this logic in the Auxiliary Extract might not be the best place,
			// maybe a new one for masks would be better

			try {
				// the auxiliary file might be a mask file
				final MaskType maskType = MaskType.of(configFileDesc.getProductType());
				try {
					final List<JSONObject> featureCollection = new MaskExtractor().extract(metadataFile);
					logger.info("Uploading {} {} polygons", featureCollection.size(), maskType.toString());
					int featureNumber = 0;
					for (final JSONObject feature : featureCollection) {
						String id = configFileDesc.getProductName() + "/features/" + featureNumber;
						logger.debug("Uploading {} {}", maskType, id);
						logger.trace("{} json: {}", maskType, feature.toString());
						esServices.createMaskFootprintData(maskType, feature, id);
						logger.debug("Finished uploading {} {}", maskType, id);
						featureNumber++;
					}
				} catch (final Exception ex) {
					logger.error("An error occurred while ingesting {} documents: {}", maskType, LogUtils.toString(ex));
					throw new InternalErrorException(Exceptions.messageOf(ex), ex);
				}
			} catch (final Exception e) {
				// the auxiliary file is not a mask file
			}
			return obj;
		} finally {
			FileUtils.delete(metadataFile.getPath());
		}

	}
}
