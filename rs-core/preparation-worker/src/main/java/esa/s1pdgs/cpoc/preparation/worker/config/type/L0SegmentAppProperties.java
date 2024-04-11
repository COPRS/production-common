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

package esa.s1pdgs.cpoc.preparation.worker.config.type;


import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app-l0-segment")
public class L0SegmentAppProperties {

    /**
     * The regular expression in Java format
     */
    private String nameRegexpPattern;
    
    private String blacklistPattern;
    
    /**
     * The regular expression in Java format
     */
    private Map<String, Integer> nameRegexpGroups;

    public L0SegmentAppProperties() {
        super();
        nameRegexpGroups = new HashMap<>();
    }

    /**
     * @return the nameRegexpPattern
     */
    public String getNameRegexpPattern() {
        return nameRegexpPattern;
    }

    /**
     * @param nameRegexpPattern the nameRegexpPattern to set
     */
    public void setNameRegexpPattern(String nameRegexpPattern) {
        this.nameRegexpPattern = nameRegexpPattern;
    }

    /**
     * @return
     */
    public String getBlacklistPattern() {
		return blacklistPattern;
	}

	/**
	 * @param blacklistPattern
	 */
	public void setBlacklistPattern(String blacklistPattern) {
		this.blacklistPattern = blacklistPattern;
	}

	/**
     * @return the nameRegexpGroups
     */
    public Map<String, Integer> getNameRegexpGroups() {
        return nameRegexpGroups;
    }

    /**
     * @param nameRegexpGroups the nameRegexpGroups to set
     */
    public void setNameRegexpGroups(Map<String, Integer> nameRegexpGroups) {
        this.nameRegexpGroups = nameRegexpGroups;
    }

}
