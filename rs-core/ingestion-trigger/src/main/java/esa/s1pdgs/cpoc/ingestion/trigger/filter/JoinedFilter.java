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

import java.util.Arrays;
import java.util.List;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class JoinedFilter implements InboxFilter {	
	
	private final List<InboxFilter> filters;
	
	public JoinedFilter(final InboxFilter ... filters) {
		this(Arrays.asList(filters));
	}

	public JoinedFilter(final List<InboxFilter> filters) {
		this.filters = filters;
	}

	@Override
	public final boolean accept(final InboxEntry entry) {
		for (final InboxFilter filter : filters) {
			if (!filter.accept(entry)) {
				LOG.trace("Entry '{}' is ignored by {}", entry, filter);
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "JoinedFilter [filters=" + filters + "]";
	}
}
