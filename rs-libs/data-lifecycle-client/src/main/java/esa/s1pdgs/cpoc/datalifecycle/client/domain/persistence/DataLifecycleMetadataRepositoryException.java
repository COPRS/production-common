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

package esa.s1pdgs.cpoc.datalifecycle.client.domain.persistence;

import esa.s1pdgs.cpoc.datalifecycle.client.error.DataLifecycleTriggerInternalServerErrorException;

/**
 * Signaling that something went wrong interacting with the persistence.
 */
public class DataLifecycleMetadataRepositoryException extends DataLifecycleTriggerInternalServerErrorException {

	private static final long serialVersionUID = -7218466810827809091L;

	// --------------------------------------------------------------------------

	public DataLifecycleMetadataRepositoryException(String string) {
		super(string);
	}

	public DataLifecycleMetadataRepositoryException(String string, Throwable e) {
		super(string, e);
	}

}
