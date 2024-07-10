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

import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class WhitelistRegexRelativePathInboxFilter implements InboxFilter {
	private final Pattern pattern;

	public WhitelistRegexRelativePathInboxFilter(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean accept(InboxEntry entry) {
		return pattern.matcher(entry.getRelativePath()).matches();
	}

	@Override
	public String toString() {
		return "WhitelistRegexRelativePathInboxFilter [pattern=" + pattern + "]";
	}
}
