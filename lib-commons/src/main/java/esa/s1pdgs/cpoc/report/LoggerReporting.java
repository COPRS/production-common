package esa.s1pdgs.cpoc.report;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

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
	public final void begin(final String comment, final Object... objects)
	{
		actionStart = System.currentTimeMillis();
		report(Level.INFO, Event.begin, comment, objects);	
	}
	
	@Override
	public final void intermediate(final String comment, final Object... objects)
	{
		report(Level.DEBUG, Event.intermediate, comment, objects);	
	}
	
	@Override
	public final void end(final String comment, final Object... objects) {
		final long deltaTMillis =  System.currentTimeMillis() - actionStart;
		report(Level.INFO, Event.end, additionalJsonFields(0, deltaTMillis, 0L), comment, objects);	
	}
	
	@Override
	public void endWithTransfer(final String comment, final long transferAmount, final Object... objects) {		
		final long deltaTMillis = System.currentTimeMillis() - actionStart;
		report(Level.INFO, Event.end, additionalJsonFields(0, deltaTMillis, transferAmount), comment, objects);
	}

	@Override
	public final void error(final String comment, final Object... objects) {
		final long deltaTMillis = System.currentTimeMillis() - actionStart;		
		report(Level.ERROR, Event.end, additionalJsonFields(1, deltaTMillis, 0L), comment, objects);	
	}
	
	final void report(final Level level, final Event thisEvent, final Map<String,String> addProps, final String message, final Object... objects) {	
		for (final Map.Entry<String,String> entry : addProps.entrySet()) {
			ThreadContext.put(entry.getKey(), entry.getValue());	
		}		
		ThreadContext.put("jsonAdditional", toJson(addProps));		
		report(level, thisEvent, message, objects);	
		ThreadContext.remove("jsonAdditional");
		for (final String key : addProps.keySet()) {
			ThreadContext.remove(key);
		}	
	}
		
	final void report(final Level level, final Event thisEvent, final String message, final Object... objects) {		
		ThreadContext.put("uid", uid);
		ThreadContext.put("taskName", taskName);
		ThreadContext.put("step", Integer.toString(step));
		ThreadContext.put("event", thisEvent.toString());
		logger.log(level, message, objects);
		ThreadContext.remove("uid");
		ThreadContext.remove("taskName");
		ThreadContext.remove("step");
		ThreadContext.remove("event");
	}
	
	static final Map<String,String> additionalJsonFields(final int errorCode, final long deltaTMillis, long transferAmount) {		
		final String status = (errorCode == 0) ? Status.OK.toString() : Status.NOK.toString();
		
		final Map<String,String> elements = new HashMap<>();
		elements.put("status", quote(status));
		elements.put("error_code", String.valueOf(errorCode));
		elements.put("duration_in_seconds", duration(deltaTMillis));
		elements.put("output", "[]");
		elements.put("quality", "[]");
		
		// data_rate_mebibytes_sec
		// data_volume_mebibytes
		if (transferAmount != 0) {
			elements.put("data_rate_mebibytes_sec", rate(transferAmount, deltaTMillis));
			elements.put("data_volume_mebibytes", size(transferAmount));
		}		
		return elements;
	}
		
	static final String toJson(final Map<String,String> elements) {
		final StringBuilder stringBuilder = new StringBuilder();
		
		for (final Map.Entry<String,String> entry : elements.entrySet()) {
			stringBuilder.append(',').append(quote(entry.getKey())).append(':').append(entry.getValue());
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
