package esa.s1pdgs.cpoc.ingestion.trigger.config;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class InboxConfiguration {
	private String directory;
	private String matchRegex;
	private String ignoreRegex;
	private String topic;
	private ProductFamily family = ProductFamily.BLANK;
	private int productInDirectoryLevel = 1;

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(final String directory) {
		this.directory = directory;
	}

	public String getMatchRegex() {
		return matchRegex;
	}

	public void setMatchRegex(final String matchRegex) {
		this.matchRegex = matchRegex;
	}

	public String getIgnoreRegex() {
		return ignoreRegex;
	}

	public void setIgnoreRegex(final String ignoreRegex) {
		this.ignoreRegex = ignoreRegex;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(final String topic) {
		this.topic = topic;
	}
	
	public int getProductInDirectoryLevel() {
		return productInDirectoryLevel;
	}

	public void setProductInDirectoryLevel(final int productInDirectoryLevel) {
		this.productInDirectoryLevel = productInDirectoryLevel;
	}
	
	public ProductFamily getFamily() {
		return family;
	}

	public void setFamily(final ProductFamily family) {
		this.family = family;
	}

	@Override
	public String toString() {
		return "InboxConfiguration [directory=" + directory + ", matchRegex=" + matchRegex + ", ignoreRegex="
				+ ignoreRegex + ", topic=" + topic + ", productInDirectoryLevel=" + productInDirectoryLevel + 
				", family=" + family + "]";
	}
}