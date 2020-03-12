package esa.s1pdgs.cpoc.ipf.preparation.worker.timeout;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.IpfPreparationWorkerSettings.InputWaitingConfig;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.TaskTableInput;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public final class InputTimeoutCheckerImpl implements InputTimeoutChecker {	
	private final InputWaitingConfig config;
	private final Supplier<LocalDateTime> timeSupplier;
	
	public InputTimeoutCheckerImpl(final InputWaitingConfig config, final Supplier<LocalDateTime> timeSupplier) {
		this.config = config;
		this.timeSupplier = timeSupplier;
	}

	@Override
	public final boolean isTimeoutExpiredFor(final AppDataJob<CatalogEvent> job, final TaskTableInput input) {
		if (isMatchingConfiguredInputIdRegex(input) &&
			isMatchingConfiguredTimeliness(job)) {			
			final LocalDateTime sensingStart = DateUtils.parse(
					job.getProduct().getStartTime());
			return timeSupplier.get().isAfter(sensingStart.plusSeconds(config.getWaitingInSeconds()));
		}
		// per default, timeout is expired instantly
		return true;
	}
	
	private final boolean isMatchingConfiguredTimeliness(final AppDataJob<CatalogEvent> job) {
		final String timeliness = (String) job.getMessages().get(0)
				.getBody().getMetadata().get("timeliness");
		return timeliness.matches(Optional.ofNullable(config.getTimelinessRegexp()).orElse(".*"));
	}
	
	private final boolean isMatchingConfiguredInputIdRegex(final TaskTableInput input) {
		return Optional.ofNullable(input.getId()).orElse("")
				.matches(Optional.ofNullable(config.getInputIdRegexp()).orElse(".*"));
	}

}
