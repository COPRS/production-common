package esa.s1pdgs.cpoc.ingestion.trigger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

public class AuxipConfiguration {

    private String start;
    private int timeWindowSec;
    private int timeWindowOverlapSec;
    private int offsetFromNowSec;
    private int maxPageSize;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public int getTimeWindowSec() {
        return timeWindowSec;
    }

    public void setTimeWindowSec(int timeWindowSec) {
        this.timeWindowSec = timeWindowSec;
    }

    public int getTimeWindowOverlapSec() {
        return timeWindowOverlapSec;
    }

    public void setTimeWindowOverlapSec(int timeWindowOverlapSec) {
        this.timeWindowOverlapSec = timeWindowOverlapSec;
    }

    public int getOffsetFromNowSec() {
        return offsetFromNowSec;
    }

    public void setOffsetFromNowSec(int offsetFromNowSec) {
        this.offsetFromNowSec = offsetFromNowSec;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }
}
