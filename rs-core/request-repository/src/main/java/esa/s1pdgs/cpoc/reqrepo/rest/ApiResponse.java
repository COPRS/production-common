package esa.s1pdgs.cpoc.reqrepo.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * Message container in case of successfull operations
 */
public class ApiResponse {
	private String entity;
	private String action;
	private List<String> idsWithSuccess = new ArrayList<>();
	private List<String> idsSkipped = new ArrayList<>();
	
	public ApiResponse() {
	}
	
	public ApiResponse(String entity, String action, List<String> idsWithSuccess, List<String> idsSkipped) {
		this.entity = entity;
		this.action = action;
		this.idsWithSuccess = idsWithSuccess;
		this.idsSkipped = idsSkipped;
	}
	
	public String getEntity() {
		return entity;
	}
	
	public void setEntity(String entity) {
		this.entity = entity;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	public List<String> getIdsWithSuccess() {
		return idsWithSuccess;
	}
	
	public void setIdsWithSuccess(List<String> idsWithSuccess) {
		this.idsWithSuccess = idsWithSuccess;
	}
	
	public List<String> getIdsSkipped() {
		return idsSkipped;
	}
	
	public void setIdsSkipped(List<String> idsSkipped) {
		this.idsSkipped = idsSkipped;
	}

	@Override
	public String toString() {
		return "ApiResponse [entity=" + entity + ", action=" + action + ", idsWithSuccess=" + idsWithSuccess
				+ ", idsSkipped=" + idsSkipped + "]";
	}
}
