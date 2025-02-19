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

package esa.s1pdgs.cpoc.ingestion.trigger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "auxip")
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
