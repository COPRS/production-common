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
	private int priority;
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
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
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
		Map<String, Object> map = new HashMap<>();
		map.put(PROPERTY_NAME_ERROR_TITLE, errorTitle);
		map.put(PROPERTY_NAME_ERROR_ID, errorId);
		map.put(PROPERTY_NAME_ACTION_TYPE, actionType);
		map.put(PROPERTY_NAME_TARGET_TOPIC, targetTopic);
		map.put(PROPERTY_NAME_MAX_RETRY, maxRetry);
		map.put(PROPERTY_NAME_PRIORITY, priority);
		map.put(PROPERTY_NAME_COMMENT, comment);
		return new Gson().toJson(map);
	}

}
