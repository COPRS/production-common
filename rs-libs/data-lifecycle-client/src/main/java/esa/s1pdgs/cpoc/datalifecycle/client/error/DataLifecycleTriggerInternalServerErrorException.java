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

package esa.s1pdgs.cpoc.datalifecycle.client.error;

/**
 * Signaling that something went wrong in the data lifecycle trigger.
 */
public class DataLifecycleTriggerInternalServerErrorException extends Exception {

	private static final long serialVersionUID = -5343969326183198146L;

	// --------------------------------------------------------------------------

	public DataLifecycleTriggerInternalServerErrorException(String string) {
		super(string);
	}

	public DataLifecycleTriggerInternalServerErrorException(String string, Throwable e) {
		super(string, e);
	}

}
