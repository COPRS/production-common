package fr.viveris.s1pdgs.mdcatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {

	@Bean
	public CommonsRequestLoggingFilter logFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(10000);
		filter.setIncludeHeaders(false);
		filter.setIncludeClientInfo(true);
		filter.setBeforeMessagePrefix("[MONITOR] [Step 0] Call ");
		filter.setAfterMessagePrefix("[MONITOR] [Step 0] End ");
		return filter;
	}

}
