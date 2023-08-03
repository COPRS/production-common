package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.io.File;
import java.util.List;
<<<<<<< HEAD
import java.util.stream.Collectors;
=======
import java.util.regex.Matcher;
import java.util.regex.Pattern;
>>>>>>> main

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

<<<<<<< HEAD
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
		
		/*
		 * For the L1C TCI products we are having a special case. These won't be provided as normal products containing a metadata file that can be
		 * used, but instead providing a single jp2 product that does contain an embedded Product Inventory file. If we identify such a product
		 * a special handling will be executed.
		 */
		if (ProductFamily.S2_L1C_TC.equals(catalogJob.getProductFamily()) || 
				(ProductFamily.S2_L2A_TC.equals(catalogJob.getProductFamily()))) {
			LOG.info("Incoming job is a S2 L1C TC product and metadata will be extracted from jp2");
			
			// The metadata is embedded in the actual product, so instead of the metadata
			final File productFile = downloadMetadataFileToLocalFolder(reportingFactory, catalogJob.getProductFamily(),
					catalogJob.getKeyObjectStorage());
	
			try {
				final S2FileDescriptor descriptor = fileDescriptorBuilder.buildS2FileDescriptor(catalogJob);

				// Build metadata from file and extracted
				final ProductMetadata metadata = mdBuilder.buildS2L1TCIMetadata(descriptor, productFile, catalogJob);
				return metadata;
			} finally {
				FileUtils.delete(productFile.getPath());
			}
		}

		// In all other cases download all .xml files and use xslt to extract necessary information
		final List<File> metadataFiles = downloadS2MetadataFilesToLocalFolder(reportingFactory,
				catalogJob.getProductFamily(), catalogJob.getKeyObjectStorage());

=======
		// When HKTM, skip download and extract metadata from filename
		if (enableExtractionFromProductName && ProductFamily.S2_HKTM.equals(catalogJob.getProductFamily())) {
			LOG.trace("Extracting metadata from product name: {}", catalogJob.getProductName());
			final ProductMetadata metadata = S2ProductNameUtil.extractMetadata(catalogJob.getProductName());
			metadata.put("productFamily", catalogJob.getProductFamily().name());
			metadata.put("url", catalogJob.getKeyObjectStorage());
			return metadata;
		}

		final File metadataFile = downloadS2MetadataFileToLocalFolder(
				processConfiguration.getManifestFilenames().get("s2"), reportingFactory, catalogJob.getProductFamily(),
				catalogJob.getKeyObjectStorage());

>>>>>>> main
		try {
			final S2FileDescriptor descriptor = fileDescriptorBuilder.buildS2FileDescriptor(catalogJob);

			// Build metadata from file and extracted
<<<<<<< HEAD
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
=======
			final ProductMetadata metadata = mdBuilder.buildS2ProductFileMetadata(descriptor, metadataFile, catalogJob);
			return metadata;
		} finally {
			FileUtils.delete(metadataFile.getPath());
		}
	}

	private File downloadS2MetadataFileToLocalFolder(final String metadataFileName,
			final ReportingFactory reportingFactory, final ProductFamily family, final String keyObs) {
		// Replace placeholder
		String fileName = replacePlaceholderS2PRODUCTNAME(metadataFileName, keyObs);
		
		// make sure that keyObs contains the metadata file
		final String metadataKeyObs = keyObs + "/" + fileName;

		try {
			final List<File> files = Retries
					.performWithRetries(
							() -> obsClient.download(
									Collections.singletonList(
											new ObsDownloadObject(family, metadataKeyObs, this.localDirectory)),
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

	/*
	 * Replace the String [S2PRODUCTNAME] with the expected filename for the
	 * metadata file which is context sensitive to the productname
	 */
	protected String replacePlaceholderS2PRODUCTNAME(String fileName, String productKeyObs) {
		// Replace string "[S2PRODUCTNAME]" with productkey (OBS-Key without extension)
		// The product name contains the string "_MSI_" which has to be replaced with
		// "_MTD_" to find the metadata file
		String alteredProductName = productKeyObs.replace("_MSI_", "_MTD_");

		// If the product name contains a processingBaseline it has to be removed in
		// order to retrieve the correct metadata file name
		Pattern pattern = Pattern.compile("^(.*?)(_N[0-9]{2}\\.?[0-9]{2})(.*?)$");
		Matcher matcher = pattern.matcher(alteredProductName);
		if (matcher.matches()) {
			alteredProductName = matcher.group(1) + matcher.group(3);
		}

		return fileName.replace("[S2PRODUCTNAME]", alteredProductName);
>>>>>>> main
	}
}
