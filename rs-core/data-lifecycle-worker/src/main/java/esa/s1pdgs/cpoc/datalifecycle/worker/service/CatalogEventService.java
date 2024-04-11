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
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class CatalogEventService implements Consumer<CatalogEvent> {

	private static final Logger LOG = LogManager.getLogger(CatalogEventService.class);

	private final CommonConfigurationProperties commonProperties;
	private final DataLifecycleWorkerConfigurationProperties configurationProperties;
	private final DataLifecycleMetadataRepository metadataRepo;
	private final DataLifecycleUpdater updater;

	@Autowired
	public CatalogEventService(
			final CommonConfigurationProperties commonProperties,
			final DataLifecycleWorkerConfigurationProperties configurationProperties,
			final DataLifecycleMetadataRepository metadataRepo) {
		this.commonProperties = commonProperties;
		this.configurationProperties = configurationProperties;
		this.metadataRepo = metadataRepo;
		this.updater = new DataLifecycleUpdater(this.configurationProperties.getRetentionPolicies().values(),
				this.configurationProperties.getShorteningEvictionTimeAfterCompression(), this.metadataRepo);
	}

	@Override
	public void accept(CatalogEvent catalogEvent) {

		LOG.debug("updating data-lifecycle index, got message: {}", catalogEvent);
		
		final MissionId mission = MissionId.valueOf((String) catalogEvent.getMetadata().get(MissionId.FIELD_NAME));
		
		final Reporting reporting = ReportingUtils.newReportingBuilder(mission)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.predecessor(catalogEvent.getUid())
				.newReporting("DataLifecycleWorker");
		
		reporting.begin(
				ReportingUtils.newFilenameReportingInputFor(catalogEvent.getProductFamily(), catalogEvent.getKeyObjectStorage()),
				new ReportingMessage("Handling event for %s", catalogEvent.getKeyObjectStorage()));
		
		try {
			updater.updateMetadata(catalogEvent);
		} catch (DataLifecycleMetadataRepositoryException | InterruptedException e) {
			LOG.error(e);
			reporting.error(new ReportingMessage("Error handling event for %s: on %s -> %s", catalogEvent.getKeyObjectStorage(),
					catalogEvent.getClass().getSimpleName(), LogUtils.toString(e)));
			return;
		}
		
		reporting.end(new ReportingMessage("End handling event for %s", catalogEvent.getKeyObjectStorage()));

	}

}
