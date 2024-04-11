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

package de.werum.coprs.requestparkinglot.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Meta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;

@Service
public interface FailedProcessingRepo extends MongoRepository<FailedProcessing, String>{
	
	@Meta(allowDiskUse = true)
	public List<FailedProcessing> findAllByOrderByFailureDateAsc();
	
	public Optional<FailedProcessing> findById(String id);

	public void deleteById(String id);

}
