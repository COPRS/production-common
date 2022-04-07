package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.io.File;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.S3FileDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class S3AuxMetadataExtractor extends AbstractMetadataExtractor {

	public S3AuxMetadataExtractor(
			final EsServices esServices, 
			final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, 
			final String localDirectory,
			final ProcessConfiguration processConfiguration, 
			final ObsClient obsClient) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
	}

	@Override
	public JSONObject extract(ReportingFactory reportingFactory, CatalogJob job)
			throws AbstractCodedException {
		final File metadataFile = downloadMetadataFileToLocalFolder(
				reportingFactory,
				job.getProductFamily(),
				job.getKeyObjectStorage()
		);
		try {
			final S3FileDescriptor descriptor = fileDescriptorBuilder.buildS3FileDescriptor(metadataFile, job, job.getProductFamily());

			// Build metadata from file and extracted
			final JSONObject obj = mdBuilder.buildS3AuxFileMetadata(descriptor, metadataFile, job);
			
			return obj;
		}
		finally
		{
			FileUtils.delete(metadataFile.getPath());
		}
	}

}
