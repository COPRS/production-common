package esa.s1pdgs.cpoc.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import esa.s1pdgs.cpoc.report.message.Header;
import esa.s1pdgs.cpoc.report.message.Message;
import esa.s1pdgs.cpoc.report.message.Task;

public class JacksonReportEntry implements ReportEntry {
	private final Header header;
	private final Message message;
	private final Task task;
	
	public JacksonReportEntry(final Header header, final Message message, final Task task) {
		this.header = header;
		this.message = message;
		this.task = task;
	}
	
	@Override
	public final String toJsonString() {
		try {
			final ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
			
			return objectMapper.writeValueAsString(this);
		} catch (final JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public Header getHeader() {
		return header;
	}

	public Message getMessage() {
		return message;
	}

	public Task getTask() {
		return task;
	}

}
