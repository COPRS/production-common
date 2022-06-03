package esa.s1pdgs.cpoc.dlq.manager.model.routing;

import java.util.regex.Pattern;

public class Rule {
	private String errorTitle;
	private String errorId;
	private ActionType actionType;
	private String targetTopic;
	private int maxRetry;
	private String comment;
	private Pattern regexPattern; 
	
	public boolean matches(String text) {
		return regexPattern.matcher(text).matches();
	}
	
	public String getErrorTitle() {
		return errorTitle;
	}
	
	public void setErrorTitle(String errorTitle) {
		this.errorTitle = errorTitle;
	}
	
	public String getErrorId() {
		return errorId;
	}
	
	public void setErrorID(String errorId) {
		this.errorId = errorId;
		regexPattern = Pattern.compile(errorId);
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
	
	public int getMaxRetry() {
		return maxRetry;
	}
	
	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@Override
	public String toString() {
		return "Rule [errorTitle=" + errorTitle + ", errorId=" + errorId + ", actionType=" + actionType
				+ ", targetTopic=" + targetTopic + ", maxRetry=" + maxRetry + ", comment=" + comment + "]";
	}

}
