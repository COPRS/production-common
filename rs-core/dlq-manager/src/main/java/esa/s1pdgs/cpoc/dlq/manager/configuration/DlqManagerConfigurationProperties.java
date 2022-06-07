package esa.s1pdgs.cpoc.dlq.manager.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import esa.s1pdgs.cpoc.dlq.manager.model.routing.ActionType;

@Component
@Validated
@ConfigurationProperties(prefix = "dlq-manager")
public class DlqManagerConfigurationProperties {

	public static class RoutingProperties {
		private String errorTitle;
		private String errorId;
		private ActionType actionType;
		private String targetTopic = "";
		private Integer maxRetry;
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
