package esa.s1pdgs.cpoc.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.report.message.BeginTask;
import esa.s1pdgs.cpoc.report.message.EndTask;
import esa.s1pdgs.cpoc.report.message.Header;
import esa.s1pdgs.cpoc.report.message.Level;
import esa.s1pdgs.cpoc.report.message.Message;
import esa.s1pdgs.cpoc.report.message.Task;

public final class ReportAdapter implements Reporting {
	public static final class Builder implements Reporting.Builder {			
		private final List<String> tags = new ArrayList<String>();

		private final ReportAppender appender;
		private final UUID id;
		private final String actionName;
		
		private UUID predecessor;
		private UUID parent;

		Builder(
				final ReportAppender appender, 
				final String actionName, 
				final UUID predecessor, 
				final UUID parent, 
				final UUID id
		) {
			this.appender = appender;
			this.actionName = actionName;
			this.predecessor = predecessor;
			this.parent = parent;
			this.id = id;
		}
		
		public Builder(final ReportAppender appender, final String actionName) {
			this(appender, actionName, null, null, UUID.randomUUID());
		}

		@Override
		public final Reporting.Builder predecessor(final UUID predecessor) {
			this.predecessor = predecessor;
			return this;
		}

		@Override
		public Reporting.Builder addTags(final Collection<String> tags) {
			tags.addAll(tags);
			return this;
		}

		@Override
		public Reporting newReporting() {
			return new ReportAdapter(this);
		}
		
		@Override
		public void newTriggerComponentReporting(final ReportingMessage reportingMessage) {
			ReportAdapter reportAdapter = new ReportAdapter(this);
			reportAdapter.appender.report(new JacksonReportEntry(
					new Header(reportAdapter.actionName, Level.INFO), 
					null,
					new Message(reportAdapter.toString(reportingMessage))
			));
			
		}
	}	
	private final List<String> tags;	
	private final ReportAppender appender;
	private final String actionName;
	private final UUID predecessor;
	private final UUID parent;
	private final UUID id;
	
	private long actionStart;
	
	ReportAdapter(final Builder builder) {
		tags 		= builder.tags;
		appender 	= builder.appender;
		actionName 	= builder.actionName;
		predecessor = builder.predecessor;
		parent 		= builder.parent;
		id 			= builder.id;
		actionStart = 0L;
	}

	@Override
	public final Reporting newChild(final String childActionName) {
		return new Builder(appender, childActionName, null, id, UUID.randomUUID())
				.addTags(tags)
				.newReporting();
	}
	
	final String toString(final ReportingMessage mess) {
		// poor man solution to allow logback based string substitution
		return String.format(
				mess.getMessage().replaceAll(Pattern.quote("{}"), "%s"), 
				mess.getArgs()
		);
	}
	
	@Override
	public final void begin(final ReportingInput in, final ReportingMessage reportingMessage) {
		actionStart = System.currentTimeMillis();
		final BeginTask task = new BeginTask(id.toString(), actionName, in);
		if (predecessor != null) {
			task.setFollowsFromTask(parent.toString());
		}
		if (parent != null) {
			task.setChildOfTask(parent.toString());
		}		
		appender.report(new JacksonReportEntry(
				new Header(actionName, Level.INFO), 
				task, 
				new Message(toString(reportingMessage))
		));	
	}
	
	@Override
	public final void end(final ReportingOutput out, final ReportingMessage reportingMessage) {
		final long deltaTMillis = getDeltaMillis();
		final long transferAmount = reportingMessage.getTransferAmount();
		final Task endTask = new EndTask(
				id.toString(), 
				actionName, 
				Status.OK, 
				calcDuration(deltaTMillis),
				out
		);
		if (transferAmount != 0) {
			endTask.setVolume(calcSize(transferAmount));
			endTask.setRate(calcRate(transferAmount, deltaTMillis));
		}
		appender.report(new JacksonReportEntry(		
				new Header(actionName, Level.INFO), 
				endTask, 
				new Message(toString(reportingMessage))
		));
		actionStart = 0;
	}

	@Override
	public final void error(final ReportingMessage reportingMessage) {
		final Task endTask = new EndTask(
				id.toString(), 
				actionName, 
				Status.NOK, 
				0,
				ReportingOutput.NULL
		);
		appender.report(new JacksonReportEntry(		
				new Header(actionName, Level.ERROR), 
				endTask, 
				new Message(toString(reportingMessage))
		));
		actionStart = 0;
	}
		
	private final long getDeltaMillis() {
		// S1PRO-752: avoid obscurely high durations if someone didn't call begin()
		if (actionStart == 0L) {			
			return 0;		
		}
		return System.currentTimeMillis() - actionStart;
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
		return new BigDecimal((sizeByte / 1048576.0) / (deltaTMillis / 1000.0))
				.setScale(3, RoundingMode.FLOOR)
				.doubleValue();
	}
	
}
