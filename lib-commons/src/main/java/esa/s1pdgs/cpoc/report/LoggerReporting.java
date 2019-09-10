package esa.s1pdgs.cpoc.report;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * not thread safe
 * @author stiege
 *
 */
public final class LoggerReporting implements Reporting  {
	public static final class Factory implements Reporting.Factory {			
		private final Logger logger;	
		private final String actionName;
		private final UUID uuid;
			
		public Factory(final String action) {
			this.logger = REPORT_LOG;
			this.actionName = action;
			this.uuid = UUID.randomUUID();
		}

		@Override
		public final Reporting newReporting(final int step) {
			return new LoggerReporting(logger, uuid.toString(), actionName, step);
		}		
	}
	
	private static final List<Class<?>> nativeClasses = Arrays.asList(String.class, Integer.class, Long.class);
	
	private final Logger logger;
	private final String uid;
	private final String taskName;
	private final int step;
		
	private long actionStart;

	public LoggerReporting(Logger logger, String uid, String taskName, int step) {
		this.logger = logger;
		this.uid = uid;
		this.taskName = taskName;
		this.step = step;
	}

	@Override
	public final void begin(final ReportingMessage reportingMessage) {
		actionStart = System.currentTimeMillis();
		report(Level.INFO, Event.begin, Collections.singletonMap("input", reportingMessage.getInput()), reportingMessage);	
	}
	
	@Override
	public final void intermediate(final ReportingMessage reportingMessage) {
		report(Level.DEBUG, Event.intermediate, reportingMessage);	
	}
	
	@Override
	public final void end(final ReportingMessage reportingMessage) {
		final long deltaTMillis =  System.currentTimeMillis() - actionStart;		
		report(Level.INFO, Event.end, additionalEndJsonFields(0, deltaTMillis, reportingMessage.getTransferAmount(), reportingMessage.getOutput()), reportingMessage);	
	}

	@Override
	public final void error(final ReportingMessage reportingMessage) {
		final long deltaTMillis = System.currentTimeMillis() - actionStart;		
		report(Level.ERROR, Event.end, additionalEndJsonFields(1, deltaTMillis, 0L, reportingMessage.getOutput()), reportingMessage);	
	}
	
	final void report(final Level level, final Event thisEvent, final Map<String,Object> addProps, final ReportingMessage reportingMessage) {	
		for (final Map.Entry<String, Object> entry : addProps.entrySet()) {
			if (nativeClasses.contains(entry.getValue().getClass())) {
				ThreadContext.put(entry.getKey(), entry.getValue().toString());	
			}
		}
		ThreadContext.put("jsonAdditional", toJson(addProps));		
		report(level, thisEvent, reportingMessage);	
		ThreadContext.clearAll();	
	}
		
	final void report(final Level level, final Event thisEvent, final ReportingMessage reportingMessage) {		
		ThreadContext.put("uid", uid);
		ThreadContext.put("taskName", taskName);
		ThreadContext.put("step", Integer.toString(step));
		ThreadContext.put("event", thisEvent.toString());
		logger.log(level, reportingMessage.getMessage(), reportingMessage.getArgs());
		ThreadContext.clearAll();	
	}
	
	static final Map<String,Object> additionalEndJsonFields(
			final int errorCode, 
			final long deltaTMillis, 
			final long transferAmount,
			final ReportingOutput output
	) {		
		final String status = (errorCode == 0) ? Status.OK.toString() : Status.NOK.toString();
		
		final Map<String,Object> elements = new HashMap<>();
		elements.put("status", status);
		elements.put("error_code", errorCode);
		elements.put("duration_in_seconds", duration(deltaTMillis));
		elements.put("output", output);
		elements.put("quality", Collections.emptyList());
		
		// data_rate_mebibytes_sec
		// data_volume_mebibytes
		if (transferAmount != 0) {
			elements.put("data_rate_mebibytes_sec", rate(transferAmount, deltaTMillis));
			elements.put("data_volume_mebibytes", size(transferAmount));
		}		
		return elements;
	}
	
		
	static final String toJson(final Map<String,Object> elements) {		
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		
		final StringBuilder stringBuilder = new StringBuilder();		
		for (final Map.Entry<String,Object> entry : elements.entrySet()) {			
			try {
				stringBuilder.append(',').append(quote(entry.getKey())).append(':').append(objectMapper.writeValueAsString(entry.getValue()));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		return stringBuilder.toString();
	}
	
	static final String duration(final long deltaTMillis)
	{
		// duration in seconds with millisecond granularity
		return String.format("%.6f", deltaTMillis / 1000.0);
	}
	
	static final String size(final long sizeByte)
	{
		// calculate size in MiB
		return String.format("%.3f", sizeByte / 1048576.0);
	}
	
	static final String rate(final long sizeByte, final long deltaTMillis)
	{
		return String.format("%.3f", (sizeByte / 1048576.0) / (deltaTMillis / 1000.0));
	}
	
	private static final String quote(final String value) {
		return new StringBuilder().append('"').append(value).append('"').toString();
	}
}
