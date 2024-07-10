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

package esa.s1pdgs.cpoc.preparation.worker.type.pdu.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.common.time.TimeInterval;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.metadata.model.S3Metadata;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.preparation.worker.config.ProcessProperties;
import esa.s1pdgs.cpoc.preparation.worker.config.type.PDUProperties.PDUTypeProperties;
import esa.s1pdgs.cpoc.preparation.worker.type.pdu.PDUProduct;

public class PDUFrameGenerator extends AbstractPDUGenerator implements PDUGenerator {

	private static final Logger LOGGER = LogManager.getLogger(PDUFrameGenerator.class);

	private final PDUTypeProperties settings;
	private final ProcessProperties processSettings;
	private final MetadataClient mdClient;

	public PDUFrameGenerator(final ProcessProperties processSettings, final PDUTypeProperties settings,
			final MetadataClient mdClient) {
		this.settings = settings;
		this.processSettings = processSettings;
		this.mdClient = mdClient;
	}

	@Override
	public List<AppDataJob> generateAppDataJobs(final IpfPreparationJob job, final int primaryCheckMaxTimelifeS) throws MetadataQueryException {
		final S3Metadata metadata = getMetadataForJobProduct(this.mdClient, job);

		// Check if this product is the first of its orbit
		if (checkIfFirstInOrbit(metadata, this.mdClient, job)) {
			// Product is first of orbit, generate PDU-Jobs
			LOGGER.debug("Product is first in orbit - generate PDUs with type FRAME");
			final S3Metadata firstOfLastOrbit = mdClient.performWithReindexOnNull(
					() -> mdClient.getFirstProductForOrbit(job.getProductFamily(),
							job.getCatalogEvent().getMetadataProductType(), metadata.getSatelliteId(),
							Long.parseLong(metadata.getAbsoluteStartOrbit()) - 1),
					job.getCatalogEvent().getMetadataProductType(), job.getProductFamily());

			String startTime = metadata.getAnxTime();
			if (firstOfLastOrbit != null) {
				startTime = firstOfLastOrbit.getAnx1Time();
			}

			List<TimeInterval> timeIntervals = generateTimeIntervals(startTime, metadata.getAnx1Time(),
					settings.getLengthInS(), settings.getMinPDULengthThreshold());

			List<AppDataJob> jobs = new ArrayList<>();
			Integer frameNumber = 1;
			for (TimeInterval interval : timeIntervals) {
				LOGGER.debug("Create AppDataJob for PDU time interval: [{}; {}]",
						DateUtils.formatToMetadataDateTimeFormat(interval.getStart()),
						DateUtils.formatToMetadataDateTimeFormat(interval.getStop()));
				AppDataJob appDataJob = AppDataJob.fromPreparationJob(job);
				appDataJob.setStartTime(DateUtils.formatToMetadataDateTimeFormat(interval.getStart()));
				appDataJob.setStopTime(DateUtils.formatToMetadataDateTimeFormat(interval.getStop()));

				appDataJob.getProduct().getMetadata().put(PDUProduct.FRAME_NUMBER, frameNumber.toString());

				if (processSettings.getProcessingGroup() != null) {
					appDataJob.setProcessingGroup(processSettings.getProcessingGroup());
				}
				
				// Calculate, when the Job will be timed out
				if (primaryCheckMaxTimelifeS != 0) {
					final Date creationDate = appDataJob.getGeneration().getCreationDate();
					final Date timeoutDate = new Date(
							creationDate.toInstant().toEpochMilli() + (primaryCheckMaxTimelifeS * 1000));
					appDataJob.setTimeoutDate(timeoutDate);
				}

				jobs.add(appDataJob);

				frameNumber++;
			}

			return jobs;
		}

		LOGGER.debug("Product is not first in orbit - skip PDU generation");
		return Collections.emptyList();
	}
}
