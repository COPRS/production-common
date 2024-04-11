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

package esa.s1pdgs.cpoc.ingestion.filter.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import esa.s1pdgs.cpoc.ingestion.filter.config.IngestionFilterConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.filter.config.IngestionFilterConfigurationProperties.FilterProperties;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class IngestionFilterService implements Function<List<IngestionJob>, List<Message<IngestionJob>>> {

	private static final Logger LOG = LogManager.getLogger(IngestionFilterService.class);
	
	private final IngestionFilterConfigurationProperties properties;

	@Autowired
	public IngestionFilterService(final IngestionFilterConfigurationProperties properties) {
		this.properties = properties;
		
		if (properties.getConfig() != null) {
			LOG.info("The following {} filter(s) are used by the filter app:", properties.getConfig().size());
			properties.getConfig().forEach((missionId,filter) -> LOG.info("For MissioId {} -> {}",missionId, filter.getCronDefinition()));
		} else {
			LOG.info("The filter app does not have any filters configured!");
		}
		
	}

	@Override
	public List<Message<IngestionJob>> apply(List<IngestionJob> ingestionJobs) {
		List<Message<IngestionJob>> filteredJobs = new ArrayList<>();
		
		for (IngestionJob ingestionJob : ingestionJobs) {
			
			final String productName;
			if ("auxip".equalsIgnoreCase(ingestionJob.getInboxType())) {
				productName = ingestionJob.getRelativePath();
			} else {
				productName = ingestionJob.getProductName();
			}
			
			MissionId mission = MissionId.valueOf(ingestionJob.getMissionId());
			
			FilterProperties filterProperties = properties.getConfig().get(mission);
			
			// When no filter for mission is defined: All messages are allowed
			boolean messageShouldBeProcessed = true;
	
			Date lastModifiedDate = ingestionJob.getLastModified();
			
			if (filterProperties != null) {
				if (lastModifiedDate != null) {
					// Check if the last modification timestamp of the the file is in defined reoccuring timespans
					CronExpression expression;
					try {
						expression = new CronExpression(filterProperties.getCronDefinition());
					} catch (ParseException e) {
						LOG.error("Invalid cron expression found {}. Please refer to application properties", filterProperties.getCronDefinition(), e);
						throw new RuntimeException(e);
					}
					
					expression.setTimeZone(TimeZone.getTimeZone("UTC"));
					messageShouldBeProcessed = expression.isSatisfiedBy(lastModifiedDate);
				} else {
					LOG.warn("message does not have last modification date {} for ", productName);
					messageShouldBeProcessed = false;
				}
			}
			
			if (messageShouldBeProcessed) {
				LOG.info("IngestionJob should be processed for {} with last modification date {}", productName, lastModifiedDate);
				filteredJobs.add(MessageBuilder.withPayload(ingestionJob).build());
			} else {
				LOG.info("IngestionJob should be ignored for {} with lastmodification date {}", productName, lastModifiedDate);
			}
		}
		
		// Prevent empty array messages on kafka topic
		if (filteredJobs.isEmpty()) {
			return null;
		}
		
		return filteredJobs;
	}	
}
