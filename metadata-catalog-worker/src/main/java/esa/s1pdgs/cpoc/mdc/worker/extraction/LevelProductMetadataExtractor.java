package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.io.File;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class LevelProductMetadataExtractor extends AbstractMetadataExtractor {

	public LevelProductMetadataExtractor(
			final EsServices esServices, 
			final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, 
			final String localDirectory,
			final ProcessConfiguration processConfiguration, 
			final ObsClient obsClient) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
	}

	@Override
	public final JSONObject extract(final ReportingFactory reportingFactory, final GenericMessageDto<CatalogJob> message)
			throws AbstractCodedException {
        final CatalogJob job = message.getBody();        
        final ProductFamily family = message.getBody().getProductFamily();
        
        final File metadataFile = downloadMetadataFileToLocalFolder(reportingFactory, family, job.getKeyObjectStorage());
        try {
        	final OutputFileDescriptor descriptor = extractFromFilename(
        			() -> fileDescriptorBuilder.buildOutputFileDescriptor(metadataFile, job, job.getProductFamily())
        	);
        	return extractFromFile(
        			() -> mdBuilder.buildOutputFileMetadata(descriptor, metadataFile, job)); 
        }
        finally {
        	FileUtils.delete(metadataFile.getPath());
        }
	}
}
