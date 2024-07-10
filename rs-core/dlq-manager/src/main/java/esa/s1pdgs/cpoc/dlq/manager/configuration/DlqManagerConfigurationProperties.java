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

package esa.s1pdgs.cpoc.dlq.manager.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import esa.s1pdgs.cpoc.dlq.manager.model.routing.ActionType;

@Component
@Validated
@ConfigurationProperties(prefix = "dlq-manager")
public class DlqManagerConfigurationProperties {

	public static class RoutingProperties {

		@NotBlank
		private String errorTitle;

		@NotBlank
		private String errorId;

		@NotNull
		private ActionType actionType;

		private String targetTopic = "";
		
		@NotNull
		private Integer maxRetry;

		@NotNull
		private Integer priority;

		private String comment = "";

		public String getErrorTitle() {
			return errorTitle;
		}
		public void setErrorTitle(String errorTitle) {
			this.errorTitle = errorTitle;
		}
		public String getErrorId() {
			return errorId;
		}
		public void setErrorId(String errorId) {
			this.errorId = errorId;
		}
		public ActionType getActionType() {
			return actionType;
		}
		public void setActionType(ActionType actionType) {
			this.actionType = actionType;
		}
		public String getTargetTopic() {
			return targetTopic;
		}
		public void setTargetTopic(String targetTopic) {
			this.targetTopic = targetTopic;
		}
		public Integer getMaxRetry() {
			return maxRetry;
		}
		public void setMaxRetry(Integer maxRetry) {
			this.maxRetry = maxRetry;
		}
		public Integer getPriority() {
			return priority;
		}
		public void setPriority(Integer priority) {
			this.priority = priority;
		}
		public String getComment() {
			return comment;
		}
		public void setComment(String comment) {
			this.comment = comment;
		}
		@Override
		public String toString() {
			return "RoutingProperties [errorTitle=" + errorTitle + ", errorId=" + errorId + ", actionType=" + actionType
					+ ", targetTopic=" + targetTopic + ", maxRetry=" + maxRetry + ", priority=" + priority
					+ ", comment=" + comment + "]";
		}
	}
	
	private String hostname;
	private String parkingLotTopic;

	private Map<String, RoutingProperties> routing = new LinkedHashMap<>();
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}
	
	public String getParkingLotTopic() {
		return parkingLotTopic;
	}

	public void setParkingLotTopic(String parkingLotTopic) {
		this.parkingLotTopic = parkingLotTopic;
	}

	public Map<String, RoutingProperties> getRouting() {
		return routing;
	}

	public void setRouting(Map<String, RoutingProperties> routing) {
		this.routing = routing;
	}

	@Override
	public String toString() {
		return "DlqManagerConfigurationProperties [hostname=" + hostname + ", parkingLotTopic=" + parkingLotTopic
				+ ", routing=" + routing + "]";
	}
}
