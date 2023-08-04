package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.io.File;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.AuxDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class AuxMetadataExtractor extends AbstractMetadataExtractor {

	public AuxMetadataExtractor(final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, final String localDirectory,
			final ProcessConfiguration processConfiguration, final ObsClient obsClient) {
		super(mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
	}

	@Override
	public ProductMetadata extract(final ReportingFactory reportingFactory, final CatalogJob job)
			throws AbstractCodedException {
		final File metadataFile = downloadMetadataFileToLocalFolder(reportingFactory, ProductFamily.AUXILIARY_FILE,
				job.getKeyObjectStorage());
		try {
			final AuxDescriptor configFileDesc = fileDescriptorBuilder.buildAuxDescriptor(metadataFile);

			// Build metadata from file and extracted
			return mdBuilder.buildConfigFileMetadata(configFileDesc, metadataFile);
		} finally {
			FileUtils.delete(metadataFile.getPath());
		}

	}
}
