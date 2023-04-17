package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
<<<<<<< HEAD
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S2FileDescriptor;
=======
>>>>>>> main
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.util.S2ProductNameUtil;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class S2AuxMetadataExtractor extends AbstractMetadataExtractor {

	private static final Logger LOG = LogManager.getLogger(S2AuxMetadataExtractor.class);

	private final boolean enableExtractionFromProductName;

	public S2AuxMetadataExtractor(final MetadataBuilder mdBuilder, final FileDescriptorBuilder fileDescriptorBuilder,
			final String localDirectory, final boolean enableExtractionFromProductName,
			final ProcessConfiguration processConfiguration, final ObsClient obsClient) {
		super(mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
		this.enableExtractionFromProductName = enableExtractionFromProductName;
	}

	@Override
<<<<<<< HEAD
	public ProductMetadata extract(ReportingFactory reportingFactory, CatalogJob catalogJob)
			throws AbstractCodedException {
		if (catalogJob.getProductFamily() == ProductFamily.S2_SAD) {
			// For SAD use XSLT to extract information from Inventory_Metadata.xml
			final File metadataFile = downloadInventoryMetadataFileToLocalFolder(reportingFactory,
					catalogJob.getProductFamily(), catalogJob.getKeyObjectStorage());

			try {
				final S2FileDescriptor descriptor = fileDescriptorBuilder.buildS2FileDescriptor(catalogJob);

				// Build metadata from file and extracted
				final ProductMetadata metadata = mdBuilder.buildS2SADFileMetadata(descriptor, metadataFile,
						catalogJob);
				return metadata;
			} finally {
				FileUtils.delete(metadataFile.getPath());
			}
		} else {
			// For all other AUX, extract metadata from filename
=======
	public ProductMetadata extract(ReportingFactory reportingFactory, CatalogJob catalogJob) throws AbstractCodedException {
		if (enableExtractionFromProductName) {
>>>>>>> main
			LOG.trace("Extracting metadata from product name: {}", catalogJob.getProductName());
			ProductMetadata metadata = S2ProductNameUtil.extractMetadata(catalogJob.getProductName());
			metadata.put("productFamily", catalogJob.getProductFamily().name());
			metadata.put("url", catalogJob.getKeyObjectStorage());
			return metadata;
		}
	}

	private final File downloadInventoryMetadataFileToLocalFolder(final ReportingFactory reportingFactory,
			final ProductFamily family, final String keyObs) throws AbstractCodedException {
		// make sure that keyObs contains the metadata file
		final String metadataKeyObs = "Inventory_Metadata.xml";

		try {
			final List<File> files = Retries
					.performWithRetries(
							() -> obsClient.download(
									Collections.singletonList(
											new ObsDownloadObject(family, keyObs + "/" + metadataKeyObs, this.localDirectory)),
									reportingFactory),
							"Download of metadata file " + metadataKeyObs + " to " + localDirectory,
							processConfiguration.getNumObsDownloadRetries(),
							processConfiguration.getSleepBetweenObsRetriesMillis());
			if (files.size() != 1) {
				throw new IllegalArgumentException(String.format(
						"Expected to download one metadata file '%s', but found: %s", metadataKeyObs, files.size()));
			}
			final File metadataFile = files.get(0);
			logger.debug("Downloaded metadata file {} to {}", metadataKeyObs, metadataFile);
			return metadataFile;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
