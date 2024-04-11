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

package esa.s1pdgs.cpoc.preparation.worker.type;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.IpfPrepWorkerInputsMissingException;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.queue.util.CatalogEventAdapter;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.DiscardedException;
import esa.s1pdgs.cpoc.preparation.worker.model.exception.TimedOutException;
import esa.s1pdgs.cpoc.preparation.worker.service.AppCatJobService;
import esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter.TaskTableAdapter;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrder;

public interface ProductTypeAdapter {	
	Logger LOGGER = LogManager.getLogger(ProductTypeAdapter.class);
	
	default Product mainInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdpter) throws IpfPrepWorkerInputsMissingException, DiscardedException {
		return Product.nullProduct(job);
	}
	
	default void validateInputSearch(final AppDataJob job, final TaskTableAdapter tasktableAdpter) throws IpfPrepWorkerInputsMissingException, DiscardedException, TimedOutException {
		// default implementation: don't validate
	}
	
	default void updateTimeout(AppDataJob job, final TaskTableAdapter taskTableAdapter) {
		// default implementation: timeout is already at the correct value
	}
	
	List<AppDataJob> createAppDataJobs(final IpfPreparationJob job) throws Exception;
	
    void customJobOrder(final AppDataJob job, final JobOrder jobOrder);
	
    void customJobDto(final AppDataJob job, final IpfExecutionJob dto);	
    
	// default implementation. Only required for S1 special scenarios (session, segments)
	default Optional<AppDataJob> findAssociatedJobFor(
			final AppCatJobService appCat, 
			final CatalogEventAdapter catEvent,
			final AppDataJob job
    ) throws AbstractCodedException {
		return Optional.empty();
	}
}
