package esa.s1pdgs.cpoc.dlq.manager.model.routing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.Gson;

public class Rule {
	
	public final static String PROPERTY_NAME_ERROR_TITLE = "error-title";
	public final static String PROPERTY_NAME_ERROR_ID = "error-id";
	public final static String PROPERTY_NAME_ACTION_TYPE = "action-type";
	public final static String PROPERTY_NAME_TARGET_TOPIC = "target-topic";
	public final static String PROPERTY_NAME_MAX_RETRY = "max-retry";
	public final static String PROPERTY_NAME_PRIORITY = "priority";
	public final static String PROPERTY_NAME_COMMENT = "comment";
	
	private String errorTitle;
	private String errorId;
	private ActionType actionType;
	private String targetTopic = "";
	private int maxRetry;
	private String comment = "";
	private Pattern regexPattern; 
	
	public boolean matches(String text) {
		return regexPattern.matcher(text).find();
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
		regexPattern = Pattern.compile(errorId, Pattern.MULTILINE);
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
		Map<String, Object> map = new HashMap<>();
		map.put(PROPERTY_NAME_ERROR_TITLE, errorTitle);
		map.put(PROPERTY_NAME_ERROR_ID, errorId);
		map.put(PROPERTY_NAME_ACTION_TYPE, actionType);
		map.put(PROPERTY_NAME_TARGET_TOPIC, targetTopic);
		map.put(PROPERTY_NAME_MAX_RETRY, maxRetry);
		map.put(PROPERTY_NAME_PRIORITY, comment);
		map.put(PROPERTY_NAME_COMMENT, regexPattern);
		return new Gson().toJson(map);
	}

}
