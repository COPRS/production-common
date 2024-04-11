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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "l0asp")
public class AspProperties {
	
	private int waitingTimeHoursMinimalFast;
	private int waitingTimeHoursNominalFast;
	private int waitingTimeHoursMinimalNrtPt;
	private int waitingTimeHoursNominalNrtPt;
	
	private boolean disableTimeout;
	
	// --------------------------------------------------------------------------
	
	public int getWaitingTimeHoursMinimalFast() {
		return this.waitingTimeHoursMinimalFast;
	}
	
	public void setWaitingTimeHoursMinimalFast(int waitingTimeHoursMinimalFast) {
		this.waitingTimeHoursMinimalFast = waitingTimeHoursMinimalFast;
	}
	
	public int getWaitingTimeHoursNominalFast() {
		return this.waitingTimeHoursNominalFast;
	}
	
	public void setWaitingTimeHoursNominalFast(int waitingTimeHoursNominalFast) {
		this.waitingTimeHoursNominalFast = waitingTimeHoursNominalFast;
	}
	
	public int getWaitingTimeHoursMinimalNrtPt() {
		return this.waitingTimeHoursMinimalNrtPt;
	}
	
	public void setWaitingTimeHoursMinimalNrtPt(int waitingTimeHoursMinimalNrtPt) {
		this.waitingTimeHoursMinimalNrtPt = waitingTimeHoursMinimalNrtPt;
	}
	
	public int getWaitingTimeHoursNominalNrtPt() {
		return this.waitingTimeHoursNominalNrtPt;
	}
	
	public void setWaitingTimeHoursNominalNrtPt(int waitingTimeHoursNominalNrtPt) {
		this.waitingTimeHoursNominalNrtPt = waitingTimeHoursNominalNrtPt;
	}

	public boolean isDisableTimeout() {
		return this.disableTimeout;
	}

	public void setDisableTimeout(boolean disableTimeout) {
		this.disableTimeout = disableTimeout;
	}
	
}
