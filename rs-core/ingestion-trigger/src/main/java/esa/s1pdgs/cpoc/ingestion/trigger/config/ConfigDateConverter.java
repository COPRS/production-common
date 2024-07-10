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

package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.utils.DateUtils;

@Component
@ConfigurationPropertiesBinding
public class ConfigDateConverter implements Converter<String, Date> {		
	static final Date DEFAULT_START_DATE = new Date(0);
	
	private static final Logger LOG = LogManager.getLogger(ConfigDateConverter.class);
	
	@Override
	public Date convert(final String source) {
		try {
			if (source != null) {
				LOG.debug("Trying to parse date value '{}'", source);
				final Instant instant = DateUtils.parse(source).toInstant(ZoneOffset.UTC);			
				LOG.debug("Got date {}", instant);
				return Date.from(instant);
			}
			else {
				LOG.debug("No date value specified. Using default...");
				// fall through
			}
		} catch (final Exception e) {
			LOG.error("Error parsing date value '{}': {}", source, e);
			// fall through
		}
		return DEFAULT_START_DATE;
	}
	
}
