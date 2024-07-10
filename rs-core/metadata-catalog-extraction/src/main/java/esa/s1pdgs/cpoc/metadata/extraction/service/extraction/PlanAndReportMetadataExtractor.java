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

import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.extraction.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model.ProductMetadata;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class PlanAndReportMetadataExtractor extends AbstractMetadataExtractor {
	
	private final static Pattern MISSION_ID_PATTERN = Pattern.compile("^([a-z][0-9])[0-9a-z_]_.*$", Pattern.CASE_INSENSITIVE);
	
	public PlanAndReportMetadataExtractor(
			final MetadataBuilder mdBuilder,
			final FileDescriptorBuilder fileDescriptorBuilder, 
			final String localDirectory,
			final ProcessConfiguration processConfiguration, 
			final ObsClient obsClient
	) {
		super(mdBuilder, fileDescriptorBuilder, localDirectory, processConfiguration, obsClient);
	}

	@Override
	public ProductMetadata extract(ReportingFactory reportingFactory, CatalogJob message)
			throws AbstractCodedException {
		ProductMetadata metadata = new ProductMetadata();		
		metadata.put("productFamily", message.getProductFamily().name());
		metadata.put("productName", message.getProductName());
		metadata.put("productType", message.getProductFamily().name());
		
		if (message.getCreationDate() != null) {
			String formattedCreationDate = DateUtils.formatToMetadataDateTimeFormat(
					message.getCreationDate().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
			metadata.put("insertionTime", formattedCreationDate);
		}
		metadata.put("url", message.getKeyObjectStorage());
		
		Matcher matcher = MISSION_ID_PATTERN.matcher(message.getProductName());
		if (matcher.matches()) {
			metadata.put(MissionId.FIELD_NAME, matcher.group(1));			
		} else {
			metadata.put(MissionId.FIELD_NAME, esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage.NOT_DEFINED);
		}
		
		return metadata;
	}

}
