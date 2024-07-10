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

package esa.s1pdgs.cpoc.preparation.worker.model;

import java.util.Objects;

import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputMode;

/**
 * Product mode (available in task table)
 * @author Cyrielle Gailliard
 *
 */
// TODO clarify if ProcessingMode is more meaningful as my understanding is:
// there are several mode a processor can run NRT, SYSTEMATIC etc.
// when input selection is done, only inputs matching this mode are selected
public enum ProductMode {
	ALWAYS, SLICING, NON_SLICING, BLANK, NRT, NTC, STC;

	public boolean isCompatibleWithTaskTableMode(final TaskTableInputMode tMode) {
		return isCompatibleWithTaskTableMode(this, tMode);
	}

	/**
	 * Check if the mode in task table in compatible with the product mode
	 */
	public static boolean isCompatibleWithTaskTableMode(final ProductMode pMode, final TaskTableInputMode tMode) {
		if (pMode == null || tMode == null) {
			return false;
		}

		if (pMode.equals(ProductMode.ALWAYS) || tMode.equals(TaskTableInputMode.ALWAYS)) {
			return true;
		}

		return Objects.equals(pMode.name(), tMode.name());
	}
}
