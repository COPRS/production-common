package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.io.File;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.elastic.EsServices;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

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
	public final JSONObject extract(final ReportingFactory reportingFactory, final CatalogJob job)
			throws AbstractCodedException {   
        final String productName = job.getProductName();
        final ProductFamily family = job.getProductFamily();
        
        logger.debug("starting to download metadatafile for for product: {}", productName);        
        final File metadataFile = downloadMetadataFileToLocalFolder(reportingFactory, family, job.getKeyObjectStorage());
        try {
            logger.debug("segment metadata file dowloaded:{} for product: {}", metadataFile.getAbsolutePath(), productName);
            
        	final OutputFileDescriptor l0SegmentDesc = fileDescriptorBuilder.buildOutputFileDescriptor(metadataFile, job, job.getProductFamily());
        			
        	logger.debug("OutputFileDescriptor:{} for product: {}", l0SegmentDesc.toString(), productName);    	
        	return mdBuilder.buildL0SegmentOutputFileMetadata(l0SegmentDesc, metadataFile, reportingFactory); 
        }
        finally {
        	FileUtils.delete(metadataFile.getPath());
        }
	}
}
