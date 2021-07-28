package standalone.de.werum.csgrs.nativeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("de.werum.csgrs.nativeapi")
public class StandaloneApplication {

	public static void main(String[] args) {

		/*
		 * RS Native API Standalone Application
		 *
		 * How to get it running:
		 *
		 * 1. Start StandaloneApplication as Java Application
		 *
		 * 2. Ping API:
		 *    $ curl -v -o - -H "Accept: application/json" "http://localhost:8080/api/v1/ping" | jq
		 */

		SpringApplication.run(StandaloneApplication.class, args);
	}

}
