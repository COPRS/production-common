package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.io.File;

import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.path.PathMetadataExtractor;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.Reporting.Factory;

public class EdrsMetadataExtractor extends AbstractMetadataExtractor {	
	private final PathMetadataExtractor pathExtractor;
	
	public EdrsMetadataExtractor(
			final EsServices esServices, 
			final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, 
			final String localDirectory,
			final ProcessConfiguration processConfiguration, 
			final ObsClient obsClient,
			final PathMetadataExtractor pathExtractor
	) {
		super(esServices, mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
		this.pathExtractor = pathExtractor;
	}
	
	@Override
	public JSONObject extract(final Factory reportingFactory, final GenericMessageDto<CatalogJob> message)
			throws AbstractCodedException {
		final CatalogJob catJob = message.getBody();
        final ProductFamily family = ProductFamily.EDRS_SESSION;        
        final File product = new File(this.localDirectory, catJob.getKeyObjectStorage());

        final EdrsSessionFileDescriptor edrsFileDescriptor = extractFromFilename(
        		reportingFactory, 
        		() -> fileDescriptorBuilder.buildEdrsSessionFileDescriptor(
        				product, 
        				pathExtractor.metadataFrom(catJob),
        				catJob
        		)
        );        
        // Only when it is a DSIB
        if (edrsFileDescriptor.getEdrsSessionFileType() == EdrsSessionFileType.SESSION)
        {
        	downloadMetadataFileToLocalFolder(reportingFactory, family, catJob.getKeyObjectStorage());

			final String dsibName = new File(edrsFileDescriptor.getRelativePath()).getName();			
			final File dsib = new File(localDirectory, dsibName);
        	try {
    			return extractFromFile(
    	        		reportingFactory,
    	        		() -> mdBuilder.buildEdrsSessionFileMetadata(edrsFileDescriptor, dsib)
    	        );
        	}
        	finally {
        		FileUtils.delete(dsib.getPath());
        	}
        } 
        // RAW files        
        return extractFromFile(
        		reportingFactory,
        		() -> mdBuilder.buildEdrsSessionFileRaw(edrsFileDescriptor)
        );
	}

}
