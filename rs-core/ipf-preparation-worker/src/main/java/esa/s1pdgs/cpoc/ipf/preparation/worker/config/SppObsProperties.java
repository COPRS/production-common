package esa.s1pdgs.cpoc.ipf.preparation.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "spp")
public class SppObsProperties {

    private long minimalWaitingTimeSec;
    private long obsTimeoutSec;

    public long getMinimalWaitingTimeSec() {
        return minimalWaitingTimeSec;
    }

    public void setMinimalWaitingTimeSec(long minimalWaitingTimeSec) {
        this.minimalWaitingTimeSec = minimalWaitingTimeSec;
    }

    public long getObsTimeoutSec() {
        return obsTimeoutSec;
    }

    public void setObsTimeoutSec(long obsTimeoutSec) {
        this.obsTimeoutSec = obsTimeoutSec;
    }
}
