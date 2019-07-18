package esa.s1pdgs.cpoc.inbox.config;

public class InboxConfiguration {
	private String directory;
	private String ignoreRegex;
	private String topic;
	
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	public String getIgnoreRegex() {
		return ignoreRegex;
	}
	public void setIgnoreRegex(String ignoreRegex) {
		this.ignoreRegex = ignoreRegex;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	@Override
	public String toString() {
		return "InboxConfiguration [directory=" + directory + ", ignoreRegex=" + ignoreRegex + ", topic=" + topic + "]";
	}
}