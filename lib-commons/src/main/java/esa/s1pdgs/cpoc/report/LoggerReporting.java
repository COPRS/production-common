package esa.s1pdgs.cpoc.report;


import java.math.BigDecimal;
import java.math.RoundingMode;
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
		
	private long actionStart = 0;

	public LoggerReporting(Logger logger, String uid, String taskName, int step) {
		this.logger = logger;
		this.uid = uid;
		this.taskName = taskName;
		this.step = step;
	}
	
	@Override
	public final void begin(final ReportingMessage reportingMessage) {
		begin(ReportingInput.NULL, reportingMessage);
	}
	
	@Override
	public final void end(final ReportingMessage reportingMessage) {
		end(ReportingOutput.NULL, reportingMessage);
	}

	@Override
	public final void begin(final ReportingInput in, final ReportingMessage reportingMessage) {
		actionStart = System.currentTimeMillis();
		report(Level.INFO, Event.begin, Collections.singletonMap("input", in), reportingMessage);	
	}
	
	@Override
	public final void intermediate(final ReportingMessage reportingMessage) {
		report(Level.DEBUG, Event.intermediate, reportingMessage);	
	}
	
	@Override
	public final void end(final ReportingOutput out, final ReportingMessage reportingMessage) {
	    long deltaTMillis = 0;
		if (actionStart != 0L) {			
			deltaTMillis =  System.currentTimeMillis() - actionStart;		
		}
		report(Level.INFO, Event.end, additionalEndJsonFields(0, deltaTMillis, reportingMessage.getTransferAmount(), out), reportingMessage);	
	}

	@Override
	public final void error(final ReportingMessage reportingMessage) {
		// no start time defined? --> this is just an informational error
		if (actionStart == 0L) {
			report(Level.ERROR, Event.intermediate, reportingMessage);	
			return;
		}
		final long deltaTMillis = System.currentTimeMillis() - actionStart;		
		report(Level.ERROR, Event.end, additionalEndJsonFields(1, deltaTMillis, 0L, ReportingOutput.NULL), reportingMessage);	
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
		elements.put("duration_in_seconds", calcDuration(deltaTMillis));
		elements.put("output", output);
		elements.put("quality", Collections.emptyList());
		
		// data_rate_mebibytes_sec
		// data_volume_mebibytes
		if (transferAmount != 0) {
			elements.put("data_rate_mebibytes_sec", calcRate(transferAmount, deltaTMillis));
			elements.put("data_volume_mebibytes", calcSize(transferAmount));
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
	
	static final String duration(final long deltaTMillis) {
		// duration in seconds with millisecond granularity
		return String.format("%.6f", calcDuration(deltaTMillis));
	}
	
	static final String size(final long sizeByte) {
		// calculate size in MiB
		return String.format("%.3f", calcSize(sizeByte));
	}
	
	static final String rate(final long sizeByte, final long deltaTMillis) {
		return String.format("%.3f", calcRate(sizeByte, deltaTMillis));
	}
	
	private static final double calcDuration(final long deltaTMillis) {
		return new BigDecimal(deltaTMillis / 1000.0)
				.setScale(6, RoundingMode.FLOOR)
				.doubleValue();
	}
	
	private static final double calcSize(final long sizeByte) {
		return new BigDecimal(sizeByte / 1048576.0)
				.setScale(3, RoundingMode.FLOOR)
				.doubleValue();
	}
	
	private static final double calcRate(final long sizeByte, final long deltaTMillis) {
		return new BigDecimal(((double) sizeByte / 1048576.0) / ((double) deltaTMillis / 1000.0))
				.setScale(3, RoundingMode.FLOOR)
				.doubleValue();
	}
	
	private static final String quote(final String value) {
		return new StringBuilder().append('"').append(value).append('"').toString();
	}
}
