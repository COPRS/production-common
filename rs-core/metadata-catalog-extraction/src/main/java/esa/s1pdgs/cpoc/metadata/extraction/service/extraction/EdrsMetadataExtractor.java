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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractor;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.EdrsSessionFileDescriptor;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class EdrsMetadataExtractor extends AbstractMetadataExtractor {
	private final PathMetadataExtractor pathExtractor;

	public EdrsMetadataExtractor(final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, final String localDirectory,
			final ProcessConfiguration processConfiguration, final ObsClient obsClient,
			final PathMetadataExtractor pathExtractor) {
		super(mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
		this.pathExtractor = pathExtractor;
	}

	@Override
	public ProductMetadata extract(final ReportingFactory reportingFactory, final CatalogJob catJob)
			throws AbstractCodedException {
		final ProductFamily family = ProductFamily.EDRS_SESSION;
		final File product = new File(this.localDirectory, catJob.getKeyObjectStorage());

		final EdrsSessionFileDescriptor edrsFileDescriptor = fileDescriptorBuilder
				.buildEdrsSessionFileDescriptor(product, additionalMetadataFor(catJob), catJob);
		// Only when it is a DSIB
		if (edrsFileDescriptor.getEdrsSessionFileType() == EdrsSessionFileType.SESSION) {
			downloadMetadataFileToLocalFolder(reportingFactory, family, catJob.getKeyObjectStorage());

			final String dsibName = new File(edrsFileDescriptor.getRelativePath()).getName();
			final File dsib = new File(localDirectory, dsibName);
			try {
				return mdBuilder.buildEdrsSessionFileMetadata(edrsFileDescriptor, dsib);
			} finally {
				FileUtils.delete(dsib.getPath());
			}
		}
		// RAW files
		return mdBuilder.buildEdrsSessionFileRaw(edrsFileDescriptor);
	}

	private Map<String, String> additionalMetadataFor(final CatalogJob catJob) {
		final Map<String, String> additionalMetadata = new LinkedHashMap<>();
		for (Entry<String, Object> entry : catJob.getMetadata().entrySet()) {
			if (entry.getValue() != null) {
				additionalMetadata.put(entry.getKey(), entry.getValue().toString());
			}
		}

		// S1OPS-971: This is a workaround to keep the old requests working: Only if
		// additionalMetadata
		// is already provided with the CatalogJob, the path evaluation will be omitted
		// Once all metadata path based extraction is moved to ingestion trigger and
		// there are no
		// old requests within the system, this conditional check can be removed.
		if (additionalMetadata.get(CatalogJob.ADDITIONAL_METADATA_FLAG_KEY) == null) {
			for (final Map.Entry<String, String> entry : pathExtractor.metadataFrom(catJob.getMetadataRelativePath())
					.entrySet()) {
				additionalMetadata.put(entry.getKey(), entry.getValue());
			}
		}
		return additionalMetadata;
	}
}
