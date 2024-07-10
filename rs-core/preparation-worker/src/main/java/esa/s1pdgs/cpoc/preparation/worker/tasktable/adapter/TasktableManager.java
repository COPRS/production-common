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

package esa.s1pdgs.cpoc.preparation.worker.tasktable.adapter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.Assert;

public class TasktableManager {
	private final List<File> tasktables;
		
	public TasktableManager(
			final List<File> tasktables
	) {
		this.tasktables = tasktables;
	}

	public static final TasktableManager of(final File directory) {
		Assert.isTrue(directory != null, "No tasktable directory specified");
		Assert.isTrue(directory.isDirectory(), "No tasktable directory specified");

        final File[] taskTableFiles = directory.listFiles(parameter -> parameter.isFile());
		Assert.isTrue(taskTableFiles != null, "Tasktable listing is null (IOError)");
		
		return new TasktableManager(Arrays.asList(taskTableFiles));
	}
	
	public final List<File> tasktables() {
		return tasktables;
	}	
	
	public final int size() {
		return tasktables.size();
	}
}
