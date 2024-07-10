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

package esa.s1pdgs.cpoc.ingestion.trigger.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ingestion.trigger.inbox.Inbox;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public final class IngestionTriggerService implements Supplier<List<IngestionJob>>{		
	private static final Logger LOG = LoggerFactory.getLogger(IngestionTriggerService.class);
	
	private final List<Inbox> inboxes;
		
	public IngestionTriggerService(final List<Inbox> inboxes) {
		this.inboxes 	= inboxes;
	}
	
	public List<IngestionJob> get() {
		LOG.trace("Polling all");
		
    	List<IngestionJob> jobs = new ArrayList<>();
		
    	for (final Inbox inbox : inboxes) {
        	LOG.debug("Polling {}", inbox.description());
        	try {        	
        		List<IngestionJob> inboxJobs = inbox.poll();
				jobs.addAll(inboxJobs);
			} catch (final Exception e) {
				LOG.error(String.format("Failed polling %s", inbox), e);			
			}     
    	}
      	LOG.trace("Done polling all");
      	
      	if (jobs.isEmpty()) {
      		return null;
      	}
      	return jobs;
	}
}
