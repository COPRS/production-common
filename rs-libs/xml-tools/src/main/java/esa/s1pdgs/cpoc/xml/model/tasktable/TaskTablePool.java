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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 */
@XmlRootElement(name = "Pool")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTablePool {

	/**
	 * 
	 */
	@XmlElement(name = "Detached")
	private boolean detached;

	/**
	 * 
	 */
	@XmlElement(name = "Killing_Signal")
	private int killingSignal;

	/**
	 * 
	 */
	@XmlElementWrapper(name = "List_of_Tasks")
	@XmlElement(name = "Task")
	private List<TaskTableTask> tasks;

	/**
	 * 
	 */
	public TaskTablePool() {
		super();
		detached = false;
		killingSignal = 9;
		this.tasks = new ArrayList<>();
	}

	/**
	 * @return the detached
	 */
	public boolean isDetached() {
		return detached;
	}

	/**
	 * @param detached
	 *            the detached to set
	 */
	public void setDetached(final boolean detached) {
		this.detached = detached;
	}

	/**
	 * @return the killingSignal
	 */
	public int getKillingSignal() {
		return killingSignal;
	}

	/**
	 * @param killingSignal
	 *            the killingSignal to set
	 */
	public void setKillingSignal(final int killingSignal) {
		this.killingSignal = killingSignal;
	}

	/**
	 * @return the tasks
	 */
	public List<TaskTableTask> getTasks() {
		return tasks;
	}

	public Stream<TaskTableTask> tasks() {
		if(tasks == null) {
			return Stream.empty();
		}

		return tasks.stream();
	}

	/**
	 * @param task
	 *            the task to add
	 */
	public void addTask(final TaskTableTask task) {
		this.tasks.add(task);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(detached, killingSignal, tasks);
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
			TaskTablePool other = (TaskTablePool) obj;
			ret = detached == other.detached && killingSignal == other.killingSignal
					&& Objects.equals(tasks, other.tasks);
		}
		return ret;
	}
}
