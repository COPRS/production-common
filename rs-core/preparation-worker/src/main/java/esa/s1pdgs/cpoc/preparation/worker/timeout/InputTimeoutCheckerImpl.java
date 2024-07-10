/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.timeout;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.preparation.worker.config.PreparationWorkerProperties.InputWaitingConfig;
import esa.s1pdgs.cpoc.xml.model.tasktable.TaskTableInput;

public final class InputTimeoutCheckerImpl implements InputTimeoutChecker {
	private static final Logger LOG = LogManager.getLogger(InputTimeoutChecker.class);

	private final List<InputWaitingConfig> configs;
	private final Supplier<LocalDateTime> timeSupplier;

	public InputTimeoutCheckerImpl(final List<InputWaitingConfig> configs, final Supplier<LocalDateTime> timeSupplier) {
		this.configs = configs;
		this.timeSupplier = timeSupplier;
	}

	@Override
	public final boolean isTimeoutExpiredFor(final AppDataJob job, final TaskTableInput input) {
		LOG.debug("checking for timeouts using: {}", configs);
		try {
			final LocalDateTime now = timeSupplier.get();
			final LocalDateTime timeoutDate = timeoutFor(job, input);
			
			if (timeoutDate == null) {
				return true;
			} else {
				return !now.isBefore(timeoutDate);
			}
			
		} catch (final Exception e) {
			LOG.error("Exception on evaluating timeout, assuming there's no timeout configured.", e);
		}
		// per default, timeout is expired instantly
		return true;
	}

	@Override
	public LocalDateTime timeoutFor(AppDataJob job, TaskTableInput input) {
		try {
			for (final InputWaitingConfig config : configs) {
				if (isMatchingConfiguredInputIdRegex(config, input) && isMatchingConfiguredTimeliness(config, job)) {

					// job creation relative minimal waiting time (wait at least this time! this is
					// for development/testing purposes)
					final LocalDateTime jobCreationTime = new Timestamp(job.getCreationDate().getTime())
							.toLocalDateTime();
					final LocalDateTime jobCreationRelativeMinimalWaitingTimeMoment = jobCreationTime
							.plusSeconds(config.getWaitingFromIngestionInSeconds());

					// sensing relative timeout (for waiting for missing inputs)
					final LocalDateTime sensingStart = DateUtils.parse(job.getStartTime());
					final LocalDateTime sensingRelativeTimeoutMoment = sensingStart
							.plusSeconds(config.getWaitingFromDownlinkInSeconds());

					// return minimum of both
					return jobCreationRelativeMinimalWaitingTimeMoment
							.isBefore(sensingRelativeTimeoutMoment) ? jobCreationRelativeMinimalWaitingTimeMoment
									: sensingRelativeTimeoutMoment;		
				}
			}

		} catch (final Exception e) {
			LOG.error("Exception on evaluating timeout, assuming there's no timeout configured.", e);
		}
		
		// In case of error: return null
		return null;
	}

	final boolean isMatchingConfiguredTimeliness(final InputWaitingConfig config, final AppDataJob job) {
		final String timeliness = (String) job.getCatalogEvents().get(0).getMetadata().get("timeliness");
		return timeliness.matches(Optional.ofNullable(config.getTimelinessRegexp()).orElse(".*"));
	}

	final boolean isMatchingConfiguredInputIdRegex(final InputWaitingConfig config, final TaskTableInput input) {
		return Optional.ofNullable(input.getId()).orElse("")
				.matches(Optional.ofNullable(config.getInputIdRegexp()).orElse(".*"));
	}

}
