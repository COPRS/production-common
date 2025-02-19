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
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public abstract class AbstractMetadataExtractor implements MetadataExtractor {	
	protected final Logger logger = LogManager.getLogger(getClass());
	
	protected final MetadataBuilder mdBuilder;
	protected final FileDescriptorBuilder fileDescriptorBuilder; 
    protected final String localDirectory;
    protected final ProcessConfiguration processConfiguration;
    protected final ObsClient obsClient;

	public AbstractMetadataExtractor(
			final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, 
			final String localDirectory,
			final ProcessConfiguration processConfiguration, 
			final ObsClient obsClient
	) {
		this.mdBuilder = mdBuilder;
		this.fileDescriptorBuilder = fileDescriptorBuilder;
		this.localDirectory = localDirectory;
		this.processConfiguration = processConfiguration;
		this.obsClient = obsClient;
	}

	final File downloadMetadataFileToLocalFolder(
    		final ReportingFactory reportingFactory,  
    		final ProductFamily family, 
    		final String keyObs
    ) 
    	throws AbstractCodedException
    {
		// make sure that keyObs contains the metadata file
		final String metadataKeyObs = getMetadataKeyObs(keyObs);
		
		try {
			final List<File> files = Retries.performWithRetries(
					() -> obsClient.download(Collections.singletonList(new ObsDownloadObject(family, metadataKeyObs, this.localDirectory)), reportingFactory), 
					"Download of metadata file " + metadataKeyObs + " to " + localDirectory, 
					processConfiguration.getNumObsDownloadRetries(), 
					processConfiguration.getSleepBetweenObsRetriesMillis()
			);
			if (files.size() != 1) {
				throw new IllegalArgumentException(
						String.format("Expected to download one metadata file '%s', but found: %s", metadataKeyObs, files.size())
				);
			}
			final File metadataFile = files.get(0);
			logger.debug("Downloaded metadata file {} to {}", metadataKeyObs, metadataFile);
			return metadataFile;
		} catch (final Exception e) {
			throw new RuntimeException(e);     
		}
    }
    
	private final String getMetadataKeyObs(final String productKeyObs) {
		for (String manifestExt : processConfiguration.getManifestFilenames().keySet()) {
			if (productKeyObs.toLowerCase().endsWith(manifestExt)) {
				// Replace string "[PRODUCTNAME]" with productkey (OBS-Key without extension)
				int lastIndexOfPoint = productKeyObs.lastIndexOf(".");
				String productKey = productKeyObs;
				if (lastIndexOfPoint != -1) {
					productKey = productKeyObs.substring(0, lastIndexOfPoint);
				}
				
				String manifestFilename = processConfiguration.getManifestFilenames().get(manifestExt)
						.replace("[PRODUCTNAME]", productKey);

				return productKeyObs + "/" + manifestFilename;
			}
		}

		return productKeyObs;
	}
}
