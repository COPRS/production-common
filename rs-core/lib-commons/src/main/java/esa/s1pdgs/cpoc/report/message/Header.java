package esa.s1pdgs.cpoc.report.message;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Header {
	private String type = "REPORT";	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date timestamp = new Date();
	private Level level;
	private String workflow = "NOMINAL";
	@JsonProperty("debug_mode")
	private Boolean debug;
	@JsonProperty("tag_list")
	private List<String> tags;
	
	public Header() {

	}
	
	public Header(final Level level) {
		this.level = level;
	}

	public String getType() {
		return type;
	}
		
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public void setLevel(final Level level) {
		this.level = level;
	}
	
	public String getWorkflow() {
		return workflow;
	}
	
	public void setWorkflow(final String workflow) {
		this.workflow = workflow;
	}

	public Boolean getDebug() {
		return debug;
	}

	public void setDebug(final Boolean debug) {
		this.debug = debug;
	}

	public List<String> getTags() {
		return tags;
	}
	
	public void setTags(final List<String> tags) {
		this.tags = tags;
	}
}
