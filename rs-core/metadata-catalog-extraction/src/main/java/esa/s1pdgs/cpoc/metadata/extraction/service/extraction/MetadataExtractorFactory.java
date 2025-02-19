/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractor;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractorImpl;
import esa.s1pdgs.cpoc.metadata.extraction.config.MdcWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.extraction.config.MdcWorkerConfigurationProperties.CategoryConfig;
import esa.s1pdgs.cpoc.metadata.extraction.config.MetadataExtractorConfig;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.config.RfiConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.ExtractMetadata;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;

@Component
public class MetadataExtractorFactory {
	private final MetadataExtractorConfig extractorConfig;
	private final XmlConverter xmlConverter;
	private final ObsClient obsClient;
	private final ProcessConfiguration processConfiguration;
	private final RfiConfiguration rfiConfiguration;

	@Autowired
	public MetadataExtractorFactory(final MetadataExtractorConfig extractorConfig,
			final XmlConverter xmlConverter, final ObsClient obsClient, final ProcessConfiguration processConfiguration,
			final MdcWorkerConfigurationProperties properties, final RfiConfiguration rfiConfiguration) {
		this.extractorConfig = extractorConfig;
		this.xmlConverter = xmlConverter;
		this.obsClient = obsClient;
		this.processConfiguration = processConfiguration;
		this.rfiConfiguration = rfiConfiguration;
	}

	public MetadataExtractor newMetadataExtractorFor(final ProductCategory category, final CategoryConfig config) {		
		final FileDescriptorBuilder fileDescriptorBuilder = new FileDescriptorBuilder(
				new File(config.getLocalDirectory()), 
				Pattern.compile(config.getPatternConfig(), Pattern.CASE_INSENSITIVE)
		);
		
		final ExtractMetadata extract = new ExtractMetadata(
				extractorConfig.getTypeOverlap(), 
				extractorConfig.getTypeSliceLength(),
				extractorConfig.getFieldTypes(),
				extractorConfig.getPacketStoreTypes(),
				extractorConfig.getPacketstoreTypeTimelinesses(),
				extractorConfig.getTimelinessPriorityFromHighToLow(),
				extractorConfig.getXsltDirectory(),
				xmlConverter
		);		
		final MetadataBuilder mdBuilder = new MetadataBuilder(extract);
		
		switch (category){
		    case AUXILIARY_FILES:
		    	return new AuxMetadataExtractor(
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration, 
		    			obsClient
		    	);
		    case EDRS_SESSIONS:
		    	return new EdrsMetadataExtractor(
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration, 
		    			obsClient,
		    			newPathMetadataExtractor(config)
		    	);
		    case PLANS_AND_REPORTS:
		    	return new PlanAndReportMetadataExtractor(
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration, 
		    			obsClient
		    	);
		    case LEVEL_SEGMENTS:
		    	return new LevelSegmentMetadataExtractor(
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration, 
		    			obsClient
		    	);
		    case LEVEL_PRODUCTS:
		    	return new LevelProductMetadataExtractor(
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration,
		    			rfiConfiguration,
		    			obsClient,
		    			xmlConverter
		    	);
		    case SPP_PRODUCTS:
		    	return new SppProductMetadataExtractor(
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration, 
		    			obsClient
		    	);
		    case SPP_MBU_PRODUCTS:
		    	return new SppMbuProductMetadataExtractor(
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			processConfiguration, 
		    			obsClient
		    			);
		    case S2_AUX:
		    	return new S2AuxMetadataExtractor(
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(),
		    			config.getEnableExtractionFromProductName(),
		    			processConfiguration, 
		    			obsClient);
		    case S2_PRODUCTS:
		    	return new S2ProductMetadataExtractor(
		    			mdBuilder, 
		    			fileDescriptorBuilder, 
		    			config.getLocalDirectory(), 
		    			config.getEnableExtractionFromProductName(),
		    			processConfiguration, 
		    			obsClient);
		    case S3_AUX:
				return new S3AuxMetadataExtractor(
						mdBuilder, 
						fileDescriptorBuilder,
						config.getLocalDirectory(), 
						processConfiguration, 
						obsClient
				);
		    case S3_PRODUCTS:
		    	return new S3LevelProductMetadataExtractor(
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
								ProductCategory.PLANS_AND_REPORTS,
								ProductCategory.LEVEL_SEGMENTS, 
								ProductCategory.LEVEL_PRODUCTS,
								ProductCategory.S2_PRODUCTS,
								ProductCategory.S3_AUX,
								ProductCategory.S3_PRODUCTS
						)
				)
		);
	}

	public static final PathMetadataExtractor newPathMetadataExtractor(final CategoryConfig config) {
		if (config.getPathPattern() == null) {
			return PathMetadataExtractor.NULL;
		}
		return new PathMetadataExtractorImpl(Pattern.compile(config.getPathPattern(), Pattern.CASE_INSENSITIVE),
				config.getPathMetadataElements());
	}
}
