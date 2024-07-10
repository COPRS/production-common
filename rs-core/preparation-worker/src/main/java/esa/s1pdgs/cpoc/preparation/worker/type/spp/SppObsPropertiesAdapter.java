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

package esa.s1pdgs.cpoc.preparation.worker.type.spp;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.preparation.worker.config.type.SppObsProperties;

public class SppObsPropertiesAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SppObsPropertiesAdapter.class);

    private final SppObsProperties properties;

    private SppObsPropertiesAdapter(SppObsProperties properties) {
        this.properties = properties;
    }

    public static SppObsPropertiesAdapter of(SppObsProperties properties) {
        return new SppObsPropertiesAdapter(properties);
    }

    public boolean shouldWait(AppDataJob job) {
        AuxResorbProduct auxResorb = AuxResorbProduct.of(job);

        Duration durationFromStart = Duration.ofSeconds(properties.getObsTimeoutSec());
        Duration minimalWaitingDuration = Duration.ofSeconds(properties.getMinimalWaitingTimeSec());

        Instant timoutFromProductStart = Instant.parse(auxResorb.getStartTime()).plus(durationFromStart);
        Instant minimalTimeout = job.getCreationDate().toInstant().plus(minimalWaitingDuration);


        Instant actualTimeout = max(timoutFromProductStart, minimalTimeout);

        LOG.debug("OBS processing for product {} of job {} should wait until {}",
                auxResorb.getProductName(), job.getId(), actualTimeout);

        return Instant.now().isBefore(actualTimeout);
    }

    private static Instant max(Instant one, Instant other) {
        if (one.isAfter(other)) {
            return one;
        }

        return other;
    }
}
