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
		private final UUID uid;
		
		private String taskName;		
		private UUID predecessorUid;
		private UUID rootUid;
		private UUID parentUid;

		Builder(
				final ReportAppender appender, 
				final UUID predecessor, 
				final UUID rootUid, 
				final UUID parentUid, 
				final UUID uid
		) {
			this.appender = appender;
			this.predecessorUid = predecessorUid;
			this.rootUid = rootUid == null ? uid : rootUid;
			this.parentUid = parentUid;
			this.uid = uid;
		}
		
		public Builder(final ReportAppender appender) {
			this(appender, null, null, null, UUID.randomUUID());
		}

		@Override
		public final Reporting.Builder predecessor(final UUID predecessor) {
			this.predecessorUid = predecessorUid;
			return this;
		}

		@Override
		public Reporting.Builder addTags(final Collection<String> tags) {
			tags.addAll(tags);
			return this;
		}

		@Override
		public Reporting newTaskReporting(String taskName) {
			this.taskName = taskName;
			return new ReportAdapter(this);
		}
		
		@Override
		public void newEventReporting(final ReportingMessage reportingMessage) {
			ReportAdapter reportAdapter = new ReportAdapter(this);
			reportAdapter.appender.report(new JacksonReportEntry(
					new Header(Level.INFO), 
					new Message(reportAdapter.toString(reportingMessage)),
					null
			));			
		}
	}	
	
	public final class ChildFactory implements Reporting.ChildFactory {
		private final ReportAppender parentAppender;
		private final UUID rootUid;
		private final UUID parentUid;
		private final List<String> parentTags;
		public ChildFactory(UUID rootUid, UUID parentUid, ReportAppender parentAppender, List<String> parentTags) {
			this.rootUid = rootUid;
			this.parentUid = parentUid;
			this.parentAppender = parentAppender;
			this.parentTags = parentTags;
		}
		
		public final Reporting newChild(final String taskName) {
			return new Builder(parentAppender, null, rootUid, parentUid, UUID.randomUUID())
					.addTags(parentTags)
					.newTaskReporting(taskName);			
		}
	}
	
	private final List<String> tags;	
	private final ReportAppender appender;
	private final String taskName;
	private final UUID predecessorUid;
	private final UUID rootUid;
	private final UUID parentUid;
	private final UUID uid;
	private final ChildFactory childFactory;
	
	private long actionStart;
	
	ReportAdapter(final Builder builder) {
		tags           = builder.tags;
		appender       = builder.appender;
		taskName       = builder.taskName;
		predecessorUid = builder.predecessorUid;
		rootUid        = builder.rootUid;
		parentUid      = builder.parentUid;
		uid            = builder.uid;
		actionStart    = 0L;
		childFactory   = new ChildFactory(rootUid, uid, appender, tags);			
	}
	
	final String toString(final ReportingMessage mess) {
		// poor man solution to allow logback based string substitution
		return String.format(
				mess.getMessage().replaceAll(Pattern.quote("{}"), "%s"), 
				mess.getArgs()
		);
	}
	
	@Override
	public UUID getRootUID() {
		return rootUid;
	}
	
	@Override
	public final void begin(final ReportingInput in, final ReportingMessage reportingMessage) {
		actionStart = System.currentTimeMillis();
		final BeginTask task = new BeginTask(uid.toString(), taskName, in);
		if (predecessorUid != null) {
			task.setFollowsFromTask(parentUid.toString());
		}
		if (parentUid != null) {
			task.setChildOfTask(parentUid.toString());
		}		
		appender.report(new JacksonReportEntry(
				new Header(Level.INFO), 
				new Message(toString(reportingMessage)),
				task
		));	
	}
	
	@Override
	public final void end(final ReportingOutput out, final ReportingMessage reportingMessage) {
		final long deltaTMillis = getDeltaMillis();
		final long transferAmount = reportingMessage.getTransferAmount();
		final Task endTask = new EndTask(
				uid.toString(), 
				taskName, 
				Status.OK, 
				calcDuration(deltaTMillis),
				out
		);
		if (transferAmount != 0) {
			endTask.setVolume(calcSize(transferAmount));
			endTask.setRate(calcRate(transferAmount, deltaTMillis));
		}
		appender.report(new JacksonReportEntry(		
				new Header(Level.INFO), 
				new Message(toString(reportingMessage)),
				endTask
		));
		actionStart = 0;
	}

	@Override
	public final void error(final ReportingMessage reportingMessage) {
		final Task endTask = new EndTask(
				uid.toString(), 
				taskName, 
				Status.NOK, 
				0,
				ReportingOutput.NULL
		);
		appender.report(new JacksonReportEntry(		
				new Header(Level.ERROR), 
				new Message(toString(reportingMessage)),
				endTask
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

	@Override
	public ChildFactory getChildFactory() {
		return childFactory;
	}
	
}
