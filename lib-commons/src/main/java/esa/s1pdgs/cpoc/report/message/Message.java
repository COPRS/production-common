package esa.s1pdgs.cpoc.report.message;

public class Message {
	private String content;
	
	public Message() {
	}
	
	public Message(final String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}	
}
