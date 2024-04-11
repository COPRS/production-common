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

package esa.s1pdgs.cpoc.preparation.worker.db;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;

/**
 * Access class to AppDataJob in mongo DB
 * 
 * @author Viveris Technologies
 */
@Service
public interface AppDataJobRepository extends MongoRepository<AppDataJob, Long> {
	@Query(value = "{ 'catalogEvents.uid' : ?0, 'pod' : ?1, 'state' : { $ne: 'TERMINATED' } }")
	List<AppDataJob> findByCatalogEventsUid(final String uid, final String podName);

	@Query(value = "{ 'product.metadata.dataTakeId' : ?0, 'product.metadata.productType' : { $ne: 'RF_RAW__0S' }, 'state' : { $ne: 'TERMINATED' } }")
	List<AppDataJob> findByProductDataTakeId_NonRfc(final String dataTakeId);

	@Query(value = "{ 'product.metadata.dataTakeId' : ?0, 'product.metadata.productType' : 'RF_RAW__0S', 'state' : { $ne: 'TERMINATED' } }")
	List<AppDataJob> findByProductDataTakeId_Rfc(final String dataTakeId);

	@Query(value = "{ 'productName' : { $regex : ?0 }, 'pod' : ?1, 'state' : { $ne: 'TERMINATED' } }")
	List<AppDataJob> findByProductType(final String productType, final String podName);

	@Query(value = "{ 'triggerProducts' : ?0, 'pod' : ?1, 'state' : { $ne: 'TERMINATED' }, 'generation.state' : { $ne: 'SENT' } }")
	List<AppDataJob> findByTriggerProduct(final String productType, final String podName);

	@Query(value = "{ 'product.metadata.sessionId' : ?0, 'state' : { $ne: 'TERMINATED' } }")
	List<AppDataJob> findByProductSessionId(final String sessionId);

	@Query(value = "{ 'pod': ?1, 'timeoutDate' : { $lt: ?0 }, 'generation.state' : { $ne: 'SENT' } }")
	List<AppDataJob> findTimeoutJobs(final Date timeoutThreshhold, final String podName);
	
	@Query(value = "{ 'state': ?0, 'pod': ?1, 'lastUpdateDate': { $lt: ?2 } }")
	List<AppDataJob> findByStateAndLastUpdateDateLessThan(final String state, final String podName, final Date lastUpdated);
	
	@Query(value = "{ 'generation.state' : { $ne: 'SENT' }, 'state' : { $ne: 'TERMINATED' }, 'pod': ?0 }", count = true)
	Long countByPod(final String podName);
}
