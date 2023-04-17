package esa.s1pdgs.cpoc.ebip.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.ebip.client.EdipClientFactory;
import esa.s1pdgs.cpoc.ebip.client.apacheftp.ApacheFtpEdipClientFactory;
import esa.s1pdgs.cpoc.ebip.client.apacheftp.RobustFtpEdipClientFactory;

@Configuration
public class EdipClientConfiguration {
	
	private final EdipClientConfigurationProperties config;
	
	@Autowired
	public EdipClientConfiguration(final EdipClientConfigurationProperties config) {
		this.config = config;
	}
	
	@Bean
	public EdipClientFactory edipClientFactory() {

		if (config.isEnableRobustFtpClient()) {
			return new RobustFtpEdipClientFactory(config);
		} else {
			return new ApacheFtpEdipClientFactory(config);
		}
	}

}
