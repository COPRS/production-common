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

package esa.s1pdgs.cpoc.datalifecycle.worker.service;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepository;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence.DataLifecycleMetadataRepositoryException;
import esa.s1pdgs.cpoc.datalifecycle.worker.config.DataLifecycleWorkerConfigurationProperties;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionEvent;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class CompressionEventService implements Consumer<CompressionEvent> {

	private static final Logger LOG = LogManager.getLogger(CompressionEventService.class);
	
	private final CommonConfigurationProperties commonProperties;
	private final DataLifecycleWorkerConfigurationProperties configurationProperties;
	private final DataLifecycleMetadataRepository metadataRepo;
	private final DataLifecycleUpdater updater;
	
	@Autowired
	public CompressionEventService(final CommonConfigurationProperties commonProperties,
			final DataLifecycleWorkerConfigurationProperties configurationProperties,
			final DataLifecycleMetadataRepository metadataRepo) {
		this.commonProperties = commonProperties;
		this.configurationProperties = configurationProperties;
		this.metadataRepo = metadataRepo;
		this.updater = new DataLifecycleUpdater(this.configurationProperties.getRetentionPolicies().values(),
				this.configurationProperties.getShorteningEvictionTimeAfterCompression(), this.metadataRepo);
	}
	
	@Override
	public void accept(CompressionEvent compressionEvent) {

		LOG.debug("updating data-lifecycle index, got message: {}", compressionEvent);
		
		final MissionId mission = MissionId.fromFileName(compressionEvent.getKeyObjectStorage());
		
		final Reporting reporting = ReportingUtils.newReportingBuilder(mission)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(compressionEvent.getUid())
				.newReporting("DataLifecycleWorker");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(compressionEvent.getProductFamily(), compressionEvent.getKeyObjectStorage()),
				new ReportingMessage("Handling event for %s", compressionEvent.getKeyObjectStorage()));
		
		try {
			updater.updateMetadata(compressionEvent);
		} catch (DataLifecycleMetadataRepositoryException | InterruptedException e) {
			LOG.error(e);
			reporting.error(new ReportingMessage("Error handling event for %s: on %s -> %s", compressionEvent.getKeyObjectStorage(),
					compressionEvent.getClass().getSimpleName(), LogUtils.toString(e)));
			return;
		}
		
		reporting.end(new ReportingMessage("End handling event for %s", compressionEvent.getKeyObjectStorage()));

	}
}
