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

package esa.s1pdgs.cpoc.xml.model.tasktable.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used the to route<br/>
 * Used for mapping the file routing.xml in java objects
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "level_products_to")
@XmlAccessorType(XmlAccessType.NONE)
public class LevelProductsRouteTo {

	/**
	 * List of task tables (XML filenames)
	 */
	@XmlElementWrapper(name = "task_tables")
	@XmlElement(name = "task_table")
	private List<String> taskTables;

	/**
	 * Default constructor
	 */
	public LevelProductsRouteTo() {
		this.taskTables = new ArrayList<>();
	}

	/**
	 * Constructor using fields
	 * @param taskTables
	 */
	public LevelProductsRouteTo(final Collection<String> taskTables) {
		this();
		this.taskTables.addAll(taskTables);
	}

	/**
	 * @return the taskTables
	 */
	public List<String> getTaskTables() {
		return taskTables;
	}

	/**
	 * @param taskTables the taskTables to set
	 */
	public void setTaskTables(final List<String> taskTables) {
		this.taskTables = taskTables;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{taskTables: %s}", taskTables);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(taskTables);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (this == obj) {
			ret = true;
		} else if (obj == null || getClass() != obj.getClass()) {
			ret = false;
		} else {
			LevelProductsRouteTo other = (LevelProductsRouteTo) obj;
			ret = Objects.equals(taskTables, other.taskTables);
		}
		return ret;
	}

}
