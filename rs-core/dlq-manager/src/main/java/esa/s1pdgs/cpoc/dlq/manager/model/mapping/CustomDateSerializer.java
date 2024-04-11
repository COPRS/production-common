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

package esa.s1pdgs.cpoc.dlq.manager.model.mapping;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class CustomDateSerializer extends JsonSerializer<Date> {

	@Override
	public void serialize(Date date, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		String dateValue = DateUtils.formatToOdataDateTimeFormat(date.toInstant().atZone(ZoneId.of("Z")).toLocalDateTime());
	    String text = "{\"$date\":\"" + dateValue + "\"}";
	    gen.writeRawValue(text);
	}

}
