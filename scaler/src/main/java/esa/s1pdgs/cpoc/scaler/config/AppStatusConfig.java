package esa.s1pdgs.cpoc.scaler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.appstatus.rest.AppStatusRestController;

@Configuration
public class AppStatusConfig  {	
	@Bean
	public AppStatusRestController appStatusRestController(
			@Autowired final AppStatus appStat			
	) {
		return new AppStatusRestController(appStat);
	}
}
