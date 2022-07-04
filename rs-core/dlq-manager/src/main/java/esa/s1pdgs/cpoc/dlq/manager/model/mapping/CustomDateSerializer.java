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
