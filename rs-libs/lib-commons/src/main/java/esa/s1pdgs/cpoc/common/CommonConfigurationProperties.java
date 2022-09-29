package esa.s1pdgs.cpoc.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties("common")
public class CommonConfigurationProperties {
	
	
	private String rsChainName;
	private String rsChainVersion;
	
	public String getRsChainName() {
		return rsChainName;
	}
	public void setRsChainName(String rsChainName) {
		this.rsChainName = rsChainName;
	}
	public String getRsChainVersion() {
		return rsChainVersion;
	}
	public void setRsChainVersion(String rsChainVersion) {
		this.rsChainVersion = rsChainVersion;
	}
	
	@Override
	public String toString() {
		return "CommonConfigurationProperties [rsChainName=" + rsChainName + ", rsChainVersion=" + rsChainVersion + "]";
	}
	
	

}
