package esa.s1pdgs.cpoc.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import esa.s1pdgs.cpoc.metadata.model.MissionId;
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
		
		private MissionId mission;
		
		public Builder(final ReportAppender appender, final MissionId mission) {			
			this.appender = appender;
			this.uid = UUID.randomUUID();
			this.mission = mission;
		}
		
		@Override
		public final Reporting.Builder predecessor(final UUID predecessor) {
			predecessorUid = predecessor;
			return this;
		}

		@Override
		public final Reporting.Builder root(final UUID root) {
			rootUid = root;
			return this;
		}
		
		@Override
		public Reporting.Builder parent(final UUID parent) {
			parentUid = parent;
			return this;
		}

		@Override
		public Reporting.Builder addTags(final Collection<String> tags) {
			tags.addAll(tags);
			return this;
		}

		@Override
		public Reporting newReporting(final String task) {
			taskName = task;
			assertRootIsSet();
			return new ReportAdapter(this);
		}
		
		private void assertRootIsSet() {
			// if no external rood uid is provided, this uid is the root id
			if (rootUid == null) {
				rootUid = uid;
			}	
		}
	}	
	
	private final List<String> tags;	
	private final ReportAppender appender;
	private final String taskName;
	private final UUID predecessorUid;
	private final UUID rootUid;
	private final UUID parentUid;
	private final UUID uid;
	private final MissionId mission;
	
	private long actionStart;
	private ReportingInput input = ReportingInput.NULL;
	
	ReportAdapter(final Builder builder) {
		tags           	= builder.tags;
		appender       	= builder.appender;
		taskName       	= builder.taskName;
		predecessorUid 	= builder.predecessorUid;
		rootUid        	= builder.rootUid;
		parentUid      	= builder.parentUid;
		uid            	= builder.uid;
		mission         = builder.mission;
		actionStart    	= 0L;	
		input			= ReportingInput.NULL;
	}
	
	final String toString(final ReportingMessage mess) {
		// poor man solution to allow logback based string substitution
		return String.format(
				mess.getMessage().replaceAll(Pattern.quote("{}"), "%s"), 
				mess.getArgs()
		);
	}
	
	@Override
	public UUID getUid() {
		return uid;
	}

	@Override
	public final void begin(final ReportingInput in, final ReportingMessage reportingMessage) {
		actionStart = System.currentTimeMillis();
		input = in;
		final BeginTask task = new BeginTask(uid.toString(), taskName, in);
		if (predecessorUid != null) {
			task.setFollowsFromTask(predecessorUid.toString());
		}
		if (parentUid != null) {
			task.setChildOfTask(parentUid.toString());
		}		
		appender.report(new JacksonReportEntry(
				new Header(Level.INFO, mission), 
				new Message(toString(reportingMessage)),
				task
		));	
	}
	
	@Override
	public final void end(final ReportingOutput out, final ReportingMessage reportingMessage) {
		end(Level.INFO, out, reportingMessage, Collections.emptyMap());
	}
	
	@Override
	public void end(final ReportingOutput out, final ReportingMessage reportingMessage, final Map<String, String> quality) {
		end(Level.INFO, out, reportingMessage, quality);
	}	

	@Override
	public final void warning(final ReportingOutput out, final ReportingMessage reportingMessage) {
		end(Level.WARNING, out, reportingMessage, Collections.emptyMap());
	}
	
	private final void end(final Level level, final ReportingOutput out, final ReportingMessage reportingMessage, final Map<String, String> quality) {
		final long deltaTMillis = getDeltaMillis();
		final long transferAmount = reportingMessage.getTransferAmount();
		final EndTask endTask = new EndTask(
				uid.toString(), 
				taskName, 
				Status.OK, 
				calcDuration(deltaTMillis),
				out,
				input
		);
		if (transferAmount != 0) {
			endTask.setVolume(calcSize(transferAmount));
			endTask.setRate(calcRate(transferAmount, deltaTMillis));
		}
		if (!quality.isEmpty()) {
			endTask.setQuality(quality);
		}
		appender.report(new JacksonReportEntry(		
				new Header(level, mission), 
				new Message(toString(reportingMessage)),
				endTask
		));
		actionStart = 0;
		input = ReportingInput.NULL;
	}

	@Override
	public final void error(final ReportingMessage reportingMessage) {
		final long deltaTMillis = getDeltaMillis();
		final Task endTask = new EndTask(
				uid.toString(), 
				taskName, 
				Status.NOK, 
				calcDuration(deltaTMillis),
				ReportingOutput.NULL,
				input
		);
		appender.report(new JacksonReportEntry(		
				new Header(Level.ERROR, mission), 
				new Message(toString(reportingMessage)),
				endTask
		));
		actionStart = 0;
		input = ReportingInput.NULL;
	}
	
	@Override
	public final Reporting newReporting(final String taskName) {
		return new Builder(appender, mission)
				.root(rootUid)
				.parent(uid)				
				.addTags(tags)
				.newReporting(taskName);	
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
