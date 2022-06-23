package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataIgnoredFileException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S2FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.util.S2ProductNameUtil;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class S2ProductMetadataExtractor extends AbstractMetadataExtractor {

	private static final Logger LOG = LogManager.getLogger(S2AuxMetadataExtractor.class);

	private final boolean enableExtractionFromProductName;

	public S2ProductMetadataExtractor(final EsServices esServices, final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, final String localDirectory,
			final boolean enableExtractionFromProductName, final ProcessConfiguration processConfiguration,
			final ObsClient obsClient) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
		this.enableExtractionFromProductName = enableExtractionFromProductName;
	}

	@Override
	public JSONObject extract(ReportingFactory reportingFactory, CatalogJob catalogJob) throws AbstractCodedException {		

		// When HKTM, skip download and extract metadata from filename
		if (enableExtractionFromProductName && ProductFamily.S2_HKTM.equals(catalogJob.getProductFamily())) {
			LOG.trace("Extracting metadata from product name: {}", catalogJob.getKeyObjectStorage());
			JSONObject metadata = S2ProductNameUtil.extractMetadata(catalogJob.getKeyObjectStorage());
			metadata.put("productFamily", catalogJob.getProductFamily().name());
			return metadata;
		}
		
		final File safeMetadataFile = downloadS2MetadataFileToLocalFolder(processConfiguration.getManifestFilenames().get("safe"), reportingFactory, catalogJob.getProductFamily(),
				catalogJob.getKeyObjectStorage());

		// Ignored if directory
		if (safeMetadataFile.isDirectory()) {
			throw new MetadataIgnoredFileException(safeMetadataFile.getName());
		}
		
		final File inventoryMetadataFile = downloadS2MetadataFileToLocalFolder(processConfiguration.getManifestFilenames().get("inventory"), reportingFactory, catalogJob.getProductFamily(),
				catalogJob.getKeyObjectStorage());

		// Ignored if directory
		if (inventoryMetadataFile.isDirectory()) {
			throw new MetadataIgnoredFileException(inventoryMetadataFile.getName());
		}
		
		try {
			final S2FileDescriptor descriptor = fileDescriptorBuilder.buildS2FileDescriptor(catalogJob);

			// Build metadata from file and extracted
			final JSONObject obj = mdBuilder.buildS2ProductFileMetadata(descriptor, safeMetadataFile, inventoryMetadataFile, catalogJob);

			return obj;
		} finally {
			FileUtils.delete(safeMetadataFile.getPath());
			FileUtils.delete(inventoryMetadataFile.getPath());
		}
	}
	
	private File downloadS2MetadataFileToLocalFolder(
			final String metadataFileName,
    		final ReportingFactory reportingFactory,  
    		final ProductFamily family, 
    		final String keyObs) {

		// make sure that keyObs contains the metadata file
		final String metadataKeyObs = keyObs + "/" + metadataFileName;
		
		try {
			final List<File> files = Retries.performWithRetries(
					() -> obsClient.download(Collections.singletonList(new ObsDownloadObject(family, metadataKeyObs, this.localDirectory)), reportingFactory), 
					"Download of metadata file " + metadataKeyObs + " to " + localDirectory, 
					processConfiguration.getNumObsDownloadRetries(), 
					processConfiguration.getSleepBetweenObsRetriesMillis()
			);
			if (files.size() != 1) {
				throw new IllegalArgumentException(
						String.format("Expected to download one metadata file '%s', but found: %s", metadataKeyObs, files.size())
				);
			}
			final File metadataFile = files.get(0);
			logger.debug("Downloaded metadata file {} to {}", metadataKeyObs, metadataFile);
			return metadataFile;
		} catch (final Exception e) {
			throw new RuntimeException(e);     
		}
	}
	
}
