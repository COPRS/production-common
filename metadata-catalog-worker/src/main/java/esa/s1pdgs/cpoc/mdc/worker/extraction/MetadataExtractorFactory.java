package esa.s1pdgs.cpoc.mdc.worker.extraction;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mdc.worker.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.mdc.worker.config.MdcWorkerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.mdc.worker.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.mdc.worker.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdc.worker.es.EsServices;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdc.worker.extraction.path.PathMetadataExtractor;
import esa.s1pdgs.cpoc.mdc.worker.extraction.path.PathMetadataExtractorImpl;
import esa.s1pdgs.cpoc.mdc.worker.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Component
public class MetadataExtractorFactory {	
    private final EsServices esServices;
    private final MetadataExtractorConfig extractorConfig;
    private final XmlConverter xmlConverter;
    private final ObsClient obsClient;
    private final ProcessConfiguration processConfiguration;
    
    @Autowired
	public MetadataExtractorFactory(
			final EsServices esServices, 
			final MetadataExtractorConfig extractorConfig,
			final XmlConverter xmlConverter, 
			final ObsClient obsClient, 
			final ProcessConfiguration processConfiguration,
			final MdcWorkerConfigurationProperties properties
	) {
		this.esServices = esServices;
		this.extractorConfig = extractorConfig;
		this.xmlConverter = xmlConverter;
		this.obsClient = obsClient;
		this.processConfiguration = processConfiguration;
	}

	public MetadataExtractor newMetadataExtractorFor(final ProductCategory category, final CategoryConfig config) {
		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(
				new File(config.getLocalDirectory()), 
				Pattern.compile(config.getPatternConfig(), Pattern.CASE_INSENSITIVE)
		);
		
		final ExtractMetadata extract = new ExtractMetadata(
				extractorConfig.getTypeOverlap(), 
				extractorConfig.getTypeSliceLength(),
				extractorConfig.getXsltDirectory(), 
				xmlConverter
		);		
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);
		
		switch (category){
		    case AUXILIARY_FILES:
		    	return new AuxMetadataExtractor(
		    			esServices, 
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration, 
		    			obsClient
		    	);
		    case EDRS_SESSIONS:
		    	return new EdrsMetadataExtractor(
		    			esServices, 
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration, 
		    			obsClient,
		    			newPathMetadataExtractor(config)
		    	);
		    case LEVEL_SEGMENTS:
		    	return new LevelSegmentMetadataExtractor(
		    			esServices, 
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration, 
		    			obsClient
		    	);
		    case LEVEL_PRODUCTS:
		    	return new LevelProductMetadataExtractor(
		    			esServices, 
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration, 
		    			obsClient
		    	);
			default:
				// fall through
		}
		throw new IllegalArgumentException(
				String.format(
						"No MetadataExtractor available for category %s. Available are: %s", 
						category,
						Arrays.asList(
								ProductCategory.AUXILIARY_FILES, 
								ProductCategory.EDRS_SESSIONS, 
								ProductCategory.LEVEL_SEGMENTS, 
								ProductCategory.LEVEL_PRODUCTS
						)
				)
		);
	}
	
	private final PathMetadataExtractor newPathMetadataExtractor(final CategoryConfig config) {
		if (config.getPathPattern() == null) {
			return PathMetadataExtractor.NULL;
		}
		return new PathMetadataExtractorImpl(
					Pattern.compile(config.getPathPattern(), Pattern.CASE_INSENSITIVE), 
					config.getPathMetadataElements()
		);
	}
}
