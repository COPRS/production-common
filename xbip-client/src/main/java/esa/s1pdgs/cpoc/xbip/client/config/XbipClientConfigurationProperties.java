package esa.s1pdgs.cpoc.xbip.client.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource({"classpath:xbip.properties"})
@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "xbip")
public class XbipClientConfigurationProperties {
	public static class XbipHostConfiguration {
		private String user;
		private String pass;
		private boolean trustSelfSignedCertificate = false;
		
		public boolean isTrustSelfSignedCertificate() {
			return trustSelfSignedCertificate;
		}

		public void setTrustSelfSignedCertificate(final boolean trustSelfSignedCertificate) {
			this.trustSelfSignedCertificate = trustSelfSignedCertificate;
		}

		public String getUser() {
			return user;
		}

		public void setUser(final String user) {
			this.user = user;
		}

		public String getPass() {
			return pass;
		}

		public void setPass(final String pass) {
			this.pass = pass;
		}

		@Override
		public String toString() {
			return "XbipHostConfiguration [user=" + user + ", pass=****, trustSelfSignedCertificate="
					+ trustSelfSignedCertificate + "]";
		}
	}
	
	private String foo;	
	private Map<String,XbipHostConfiguration> hostConfigs;	
	private Map<String,String> context;

	public Map<String, XbipHostConfiguration> getHostConfigs() {
		return hostConfigs;
	}

	public void setHostConfigs(final Map<String, XbipHostConfiguration> hostConfigs) {
		this.hostConfigs = hostConfigs;
	}
	
	public String getFoo() {
		return foo;
	}

	public void setFoo(final String foo) {
		this.foo = foo;
	}

	public Map<String, String> getContext() {
		return context;
	}

	public void setContext(final Map<String, String> context) {
		this.context = context;
	}

	@Override
	public String toString() {
		return "XbipClientConfigurationProperties [foo=" + foo + ", hostConfigs=" + hostConfigs + ", context=" + context
				+ "]";
	}

	
	
	
	
}
