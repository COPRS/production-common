package esa.s1pdgs.cpoc.ingestion.filter.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.ingestion.filter.config.IngestionFilterConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.filter.config.IngestionFilterConfigurationProperties.FilterProperties;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class IngestionFilterService implements Function<List<IngestionJob>, List<IngestionJob>> {

	private static final Logger LOG = LogManager.getLogger(IngestionFilterService.class);
	
	private final IngestionFilterConfigurationProperties properties;

	@Autowired
	public IngestionFilterService(final IngestionFilterConfigurationProperties properties) {
		this.properties = properties;
	}

	@Override
	public List<IngestionJob> apply(List<IngestionJob> ingestionJobs) {
		List<IngestionJob> filteredJobs = new ArrayList<>();
		
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
					filterProperties.getCronDefinition().setTimeZone(TimeZone.getTimeZone("UTC"));
					messageShouldBeProcessed = filterProperties.getCronDefinition().isSatisfiedBy(lastModifiedDate);
				} else {
					LOG.warn("message does not have last modification date {} for ", productName);
					messageShouldBeProcessed = false;
				}
			}
			
			if (messageShouldBeProcessed) {
				LOG.info("IngestionJob should be processed for {} with last modification date {}", productName, lastModifiedDate);
				filteredJobs.add(ingestionJob);
			} else {
				LOG.info("IngestionJob should be ignored for {} with lastmodification date {}", productName, lastModifiedDate);
			}
		}
		
		return filteredJobs;
	}	
}
