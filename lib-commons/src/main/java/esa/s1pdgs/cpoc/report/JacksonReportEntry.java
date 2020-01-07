package esa.s1pdgs.cpoc.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import esa.s1pdgs.cpoc.report.message.Header;
import esa.s1pdgs.cpoc.report.message.Message;
import esa.s1pdgs.cpoc.report.message.Task;

public class JacksonReportEntry implements ReportEntry {
	private final Header header;
	private final Task task;
	private final Message message;
	
	public JacksonReportEntry(final Header header, final Task task, final Message message) {
		this.header = header;
		this.task = task;
		this.message = message;
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

	public Task getTask() {
		return task;
	}

	public Message getMessage() {
		return message;
	}
}
