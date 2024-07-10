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

package de.werum.coprs.requestparkinglot.service;

import java.util.Arrays;
import java.util.List;

import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;

public interface RequestParkingLot {	

	@SuppressWarnings("java:S2386")
	public static final List<MessageState> PROCESSING_STATE_LIST = Arrays.asList(MessageState.values());

	List<FailedProcessing> getFailedProcessings();

	FailedProcessing getFailedProcessingById(String id);

	void restartAndDeleteFailedProcessing(String id) throws AllowedActionNotAvailableException;

	void resubmitAndDeleteFailedProcessing(String id) throws AllowedActionNotAvailableException;

	void deleteFailedProcessing(String id);

	long getFailedProcessingsCount();
}
