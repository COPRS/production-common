package esa.s1pdgs.cpoc.mdc.trigger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.appstatus.AppStatus;

@Configuration
public class MdcTriggerConfiguration {
	@Bean
	public AppStatus appStatus() {		
		// TODO replace with real impl
		return AppStatus.NULL;
	}
}
