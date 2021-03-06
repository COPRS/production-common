package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.io.File;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.config.RfiConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.OutputFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.rfi.RfiAnnotationExtractor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdc.worker.service.EsServices;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public final class LevelProductMetadataExtractor extends AbstractMetadataExtractor {
	
	private final RfiAnnotationExtractor rfiAnnotationExtractor;
	private final RfiConfiguration rfiConfiguration;

	public LevelProductMetadataExtractor(
			final EsServices esServices, 
			final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, 
			final String localDirectory,
			final ProcessConfiguration processConfiguration, 
			final RfiConfiguration rfiConfiguration,
			final ObsClient obsClient,
			final XmlConverter xmlConverter) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
		this.rfiConfiguration = rfiConfiguration;
		this.rfiAnnotationExtractor = new RfiAnnotationExtractor(processConfiguration, rfiConfiguration, obsClient, xmlConverter);
	}

	@Override
	public final JSONObject extract(final ReportingFactory reportingFactory, final GenericMessageDto<CatalogJob> message)
			throws AbstractCodedException {
        final CatalogJob job = message.getBody();        
        final ProductFamily family = message.getBody().getProductFamily();
        
        final File metadataFile = downloadMetadataFileToLocalFolder(reportingFactory, family, job.getKeyObjectStorage());
        final JSONObject metadata;
        try {
        	final OutputFileDescriptor descriptor = fileDescriptorBuilder.buildOutputFileDescriptor(
        			metadataFile, 
        			job, 
        			family
        	);
        	metadata = mdBuilder.buildOutputFileMetadata(descriptor, metadataFile, job); 
        }
        finally {
        	FileUtils.delete(metadataFile.getPath());
        }
        
        /*
         * S1OPS-464 (S1PRO-2675)
         */
        if(rfiConfiguration.isEnabled()) {
        	rfiAnnotationExtractor.addRfiMetadata(reportingFactory, job.getKeyObjectStorage(), family, localDirectory, metadata);
        }
        return metadata;
	}
}
