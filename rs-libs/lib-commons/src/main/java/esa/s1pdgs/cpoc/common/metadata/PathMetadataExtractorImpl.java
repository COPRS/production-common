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

package esa.s1pdgs.cpoc.common.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PathMetadataExtractorImpl implements PathMetadataExtractor {		
	private static final Logger LOG = LogManager.getLogger(PathMetadataExtractorImpl.class);
	
	private final Pattern pattern;
	private final Map<String,Integer> metadataKeyToGroup;
	
	public PathMetadataExtractorImpl(final Pattern pattern, final Map<String, Integer> metadataKeyToGroup) {
		this.pattern = pattern;
		this.metadataKeyToGroup = metadataKeyToGroup;
	}
	
	@Override
	public final Map<String,String> metadataFrom(final String relativePath) {
		final Map<String,String> result = new HashMap<>();
		
		final Matcher matcher = pattern.matcher(relativePath);
		if (matcher.matches()) {
			for (final Map.Entry<String, Integer> entry : metadataKeyToGroup.entrySet()) {
				final String value = matcher.group(entry.getValue());
				LOG.debug("{} evaluates on {} to {}", entry, relativePath, value);
				result.put(entry.getKey(), value);
			}
		}
		return result;			
	}		
}