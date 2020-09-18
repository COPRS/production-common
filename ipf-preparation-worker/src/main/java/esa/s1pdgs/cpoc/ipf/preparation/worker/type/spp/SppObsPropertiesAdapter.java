package esa.s1pdgs.cpoc.ipf.preparation.worker.type.spp;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.ipf.preparation.worker.config.SppObsProperties;

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

        LOG.debug("OBS processing should wait until {}", actualTimeout);

        return Instant.now().isBefore(actualTimeout);
    }

    private static Instant max(Instant one, Instant other) {
        if (one.isAfter(other)) {
            return one;
        }

        return other;
    }
}
