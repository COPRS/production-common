package esa.s1pdgs.cpoc.ipf.preparation.worker.timeout;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.InputWaitingConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public final class InputTimeoutCheckerImpl implements InputTimeoutChecker {	
	private static final Logger LOG = LogManager.getLogger(InputTimeoutChecker.class);
	
	private final List<InputWaitingConfig> configs;
	private final Supplier<LocalDateTime> timeSupplier;
	
	public InputTimeoutCheckerImpl(final List<InputWaitingConfig> configs, final Supplier<LocalDateTime> timeSupplier) {
		this.configs = configs;
		this.timeSupplier = timeSupplier;
	}

	@Override
	public final boolean isTimeoutExpiredFor(final AppDataJob<CatalogEvent> job, final TaskTableInput input) {
		try {
			for (final InputWaitingConfig config : configs) {
				if (isMatchingConfiguredInputIdRegex(config, input) && 
					isMatchingConfiguredTimeliness(config, job)) {

					final LocalDateTime now = timeSupplier.get();
					
					// job creation relative minimal waiting time (wait at least this time! this is for development/testing purposes)
					final LocalDateTime jobCreationTime = new Timestamp(job.getCreationDate().getTime()).toLocalDateTime();
					final LocalDateTime jobCreationRelativeMinimalWaitingTimeMoment = jobCreationTime.plusSeconds(config.getWaitingFromIngestionInSeconds());
					
					// sensing relative timeout (for waiting for missing inputs)
					final LocalDateTime sensingStart = DateUtils.parse(job.getProduct().getStartTime());
					final LocalDateTime sensingRelativeTimeoutMoment = sensingStart.plusSeconds(config.getWaitingFromDownlinkInSeconds());
					
					return !(now.isBefore(jobCreationRelativeMinimalWaitingTimeMoment) ||
							 now.isBefore(sensingRelativeTimeoutMoment));
				}
			}

		} catch (final Exception e) {
			LOG.error("Exception on evaluating timeout, assuming there's no timeout configured.", e);
		}		
		// per default, timeout is expired instantly
		return true;
	}

	final boolean isMatchingConfiguredTimeliness(final InputWaitingConfig config, final AppDataJob<CatalogEvent> job) {
		final String timeliness = (String) job.getMessages().get(0)
				.getBody().getMetadata().get("timeliness");
		return timeliness.matches(Optional.ofNullable(config.getTimelinessRegexp()).orElse(".*"));
	}

	final boolean isMatchingConfiguredInputIdRegex(final InputWaitingConfig config, final TaskTableInput input) {
		return Optional.ofNullable(input.getId())
				.orElse("")
				.matches(Optional.ofNullable(config.getInputIdRegexp()).orElse(".*"));
	}

}
