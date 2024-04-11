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

package esa.s1pdgs.cpoc.xml.model.tasktable;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTable;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTablePool;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableTask;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInputAlternative.TaskTableInputAltKey;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class TaskTableObjectsTest {

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTable() {
		EqualsVerifier.forClass(TaskTable.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTableInputAltKey() {
		EqualsVerifier.forClass(TaskTableInputAltKey.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTablePool() {
		EqualsVerifier.forClass(TaskTablePool.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDtoTaskTableTask() {
		EqualsVerifier.forClass(TaskTableTask.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
