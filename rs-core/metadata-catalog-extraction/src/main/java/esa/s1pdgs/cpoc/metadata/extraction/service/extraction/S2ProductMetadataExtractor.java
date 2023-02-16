package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

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
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S2FileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.util.S2ProductNameUtil;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class S2ProductMetadataExtractor extends AbstractMetadataExtractor {

	private static final Logger LOG = LogManager.getLogger(S2AuxMetadataExtractor.class);

	private final boolean enableExtractionFromProductName;

	public S2ProductMetadataExtractor(final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, final String localDirectory,
			final boolean enableExtractionFromProductName, final ProcessConfiguration processConfiguration,
			final ObsClient obsClient) {
		super(mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
		this.enableExtractionFromProductName = enableExtractionFromProductName;
	}

	@Override
	public ProductMetadata extract(ReportingFactory reportingFactory, CatalogJob catalogJob)
			throws AbstractCodedException {

		// When HKTM, extract information from manifest.safe
		if (ProductFamily.S2_HKTM.equals(catalogJob.getProductFamily())) {
			final File metadataFile = downloadMetadataFileToLocalFolder(reportingFactory, catalogJob.getProductFamily(),
					catalogJob.getKeyObjectStorage());
			
			try {
				final S2FileDescriptor descriptor = fileDescriptorBuilder.buildS2FileDescriptor(catalogJob);

				// Build metadata from file and extracted
				final ProductMetadata metadata = mdBuilder.buildS2HKTMFileMetadata(descriptor, metadataFile, catalogJob);
				return metadata;
			} finally {
					FileUtils.delete(metadataFile.getPath());
			}
		}

		// In all other cases download all .xml files and use xslt to extract necessary information
		final List<File> metadataFiles = downloadS2MetadataFilesToLocalFolder(reportingFactory,
				catalogJob.getProductFamily(), catalogJob.getKeyObjectStorage());

		try {
			final S2FileDescriptor descriptor = fileDescriptorBuilder.buildS2FileDescriptor(catalogJob);

			// Build metadata from file and extracted
			final ProductMetadata metadata = mdBuilder.buildS2ProductFileMetadata(descriptor, metadataFiles, catalogJob);
			return metadata;
		} finally {
			for (File metadataFile : metadataFiles) {
				FileUtils.delete(metadataFile.getPath());
			}
		}
	}

	private List<File> downloadS2MetadataFilesToLocalFolder(final ReportingFactory reportingFactory,
			final ProductFamily family, final String keyObs) {
		try {
			// Retrieve list of xml files from obs
			List<String> filenameList = obsClient.list(family, keyObs);

			List<String> metadataFilenames = filenameList.stream().filter((filename -> filename.endsWith(".xml")))
					.collect(Collectors.toList());
			
			final List<File> metadataFiles = Retries
					.performWithRetries(
							() -> obsClient
									.download(
											metadataFilenames.stream()
													.map(filename -> new ObsDownloadObject(family, filename,
															this.localDirectory))
													.collect(Collectors.toList()),
											reportingFactory),
							"Download of metadata files " + metadataFilenames.toString() + " to " + localDirectory,
							processConfiguration.getNumObsDownloadRetries(),
							processConfiguration.getSleepBetweenObsRetriesMillis());
			if (metadataFiles.size() == 0) {
				throw new IllegalArgumentException(
						String.format("Expected to download at least one metadata file, but found none"));
			}
			logger.debug("Downloaded metadata files {} to {}", metadataFilenames.toString(), metadataFiles.toString());
			return metadataFiles;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
