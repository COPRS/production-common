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

package esa.s1pdgs.cpoc.ingestion.trigger.name;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

public class DirectoryProductNameEvaluator implements ProductNameEvaluator {

	@Override
	public String evaluateFrom(InboxEntry entry) {
		
		if (entry.getRelativePath().indexOf("/") != -1) {
			return entry.getRelativePath().substring(0, entry.getRelativePath().indexOf("/"));
		} else return entry.getRelativePath();
	}

}
