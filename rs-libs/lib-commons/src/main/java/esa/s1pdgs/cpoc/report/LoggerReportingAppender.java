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

package esa.s1pdgs.cpoc.report;

import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class LoggerReportingAppender implements ReportAppender {
	private static final Logger LOG = Reporting.REPORT_LOG;

	@Override
	public void report(final ReportEntry reportEntry) {
		LOG.info(reportEntry.toJsonString());
	}
	
	static final String toJson(final Map<String,Object> elements) {		
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		
		final StringBuilder stringBuilder = new StringBuilder();		
		for (final Map.Entry<String,Object> entry : elements.entrySet()) {			
			try {
				stringBuilder.append(',')
					.append(quote(entry.getKey()))
					.append(':')
					.append(objectMapper.writeValueAsString(entry.getValue()));
			} catch (final JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		return stringBuilder.toString();
	}
	
	private static final String quote(final String value) {
		return new StringBuilder().append('"').append(value).append('"').toString();
	}

}
