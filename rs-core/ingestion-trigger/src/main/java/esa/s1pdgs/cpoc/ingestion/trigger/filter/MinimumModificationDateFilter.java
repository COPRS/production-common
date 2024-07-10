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

package esa.s1pdgs.cpoc.ingestion.trigger.filter;

import java.util.Date;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class MinimumModificationDateFilter implements InboxFilter {	
	private final Date ignoreFilesBefore;
	
	public MinimumModificationDateFilter(final Date ignoreFilesBefore) {
		this.ignoreFilesBefore = ignoreFilesBefore;
	}

	@Override
	public final boolean accept(final InboxEntry entry) {
		final Date lastModified = entry.getLastModified();					
		// in doubt (i.e. if last modification date could not be determined) 
		// accept the entry
		if (lastModified == null) {
			return true;
		}
		return lastModified.after(ignoreFilesBefore);
	}

	@Override
	public String toString() {
		return "MinimumModificationDateFilter [ignoreFilesBefore=" + ignoreFilesBefore + "]";
	}
}
