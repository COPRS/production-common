package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.io.File;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting;

public final class LevelSegmentMetadataExtractor extends AbstractMetadataExtractor {

	public LevelSegmentMetadataExtractor(
			final EsServices esServices, 
			final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, 
			final String localDirectory,
			final ProcessConfiguration processConfiguration, 
			final ObsClient obsClient
	) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
	}

	@Override
	public final JSONObject extract(final Reporting reporting, final GenericMessageDto<CatalogJob> message)
			throws AbstractCodedException {
        final CatalogJob job = message.getBody();    
        final String productName = job.getProductName();
        final ProductFamily family = message.getBody().getProductFamily();
        
        logger.debug("starting to download metadatafile for for product: {}", productName);        
        final File metadataFile = downloadMetadataFileToLocalFolder(reporting, family, job.getKeyObjectStorage());
        try {
            logger.debug("segment metadata file dowloaded:{} for product: {}", metadataFile.getAbsolutePath(), productName);
            
        	final OutputFileDescriptor l0SegmentDesc = extractFromFilename(
        			reporting, 
        			() -> fileDescriptorBuilder.buildOutputFileDescriptor(metadataFile, job, job.getProductFamily())
        	);
        	logger.debug("OutputFileDescriptor:{} for product: {}", l0SegmentDesc.toString(), productName);    	
        	return extractFromFile(
        			reporting, 
        			() -> mdBuilder.buildL0SegmentOutputFileMetadata(l0SegmentDesc, metadataFile)
        	); 
        }
        finally {
        	FileUtils.delete(metadataFile.getPath());
        }
	}
}
