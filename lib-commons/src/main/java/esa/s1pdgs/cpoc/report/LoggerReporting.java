package esa.s1pdgs.cpoc.report;


import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

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
		
		private String family = null;
		private String productName = null;
			
		public Factory(final Logger logger, final String action) {
			this.logger = logger;
			this.actionName = action;
			this.uuid = UUID.randomUUID();
		}
		
		@Override
		public final Factory product(final String family, final String productName)
		{
			this.family = family;
			this.productName = productName;
			return this;
		}
		
		@Override
		public final Reporting newReporting(final int step) {
			final StringBuilder prefixBuilder = new StringBuilder();
			
			prefixBuilder
				.append("[REPORT] [").append(uuid).append("] ")
				.append("[s1pdgsTask ").append(actionName).append("] ");
				
			if (family != null) {
				prefixBuilder.append("[family ").append(family).append("] ");
			}
			
			if (productName != null) {
				prefixBuilder.append("[productName ").append(productName).append("] ");
			}
			return new LoggerReporting(logger, prefixBuilder.toString() + "[step " + step + "] ");
		}		
	}

	private final Logger logger;
	private final String prefix;
	
	private long actionStart;
	
	public LoggerReporting(Logger logger, String prefix) {
		this.logger = logger;
		this.prefix = prefix;
	}

	@Override
	public final void begin(String comment)
	{
		report(Level.INFO, "[START] " + comment);	
		actionStart = System.currentTimeMillis();
	}
	
	@Override
	public final void intermediate(String comment, final Object... objects)
	{
		report(Level.DEBUG, comment, objects);	
	}
	
	@Override
	public final void end(String comment)
	{
		final long stopTime = System.currentTimeMillis();
		final long deltaTMillis = stopTime - actionStart;
		
		report(Level.INFO, "[STOP OK] [DURATION " + duration(deltaTMillis) + "] " + comment);	
	}
	
	@Override
	public void endWithTransfer(String comment, long transferAmount) {
		
		final long stopTime = System.currentTimeMillis();
		final long deltaTMillis = stopTime - actionStart;
				
		report(Level.INFO, "[STOP OK] [DURATION " + duration(deltaTMillis) + 
				"] [SIZE " + size(transferAmount) +"] [RATE " + 
				rate(transferAmount, deltaTMillis)+ "] " + comment);			
	}

	@Override
	public final void error(String comment, final Object... objects) {
		final long stopTime = System.currentTimeMillis();
		final long deltaTMillis = stopTime - actionStart;
		
		report(Level.ERROR, "[STOP NOK] [DURATION " + duration(deltaTMillis) +"] " + comment, objects);	
	}
	
	final void report(final Level level, final String message, final Object... objects) {
		logger.log(level, prefix + message, objects);		
	}
	
	static final String duration(final long deltaTMillis)
	{
		// duration in seconds with millisecond granularity
		return String.format("%.3f", deltaTMillis / 1000.0) + " s";
	}
	
	static final String size(final long sizeByte)
	{
		// calculate size in MiB
		return String.format("%.3f", sizeByte / 1048576.0) + " MiB";
	}
	
	static final String rate(final long sizeByte, final long deltaTMillis)
	{
		return String.format("%.3f", (sizeByte / 1048576.0) / (deltaTMillis / 1000.0))+ " MiB/s";
	}
}
