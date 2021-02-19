package esa.s1pdgs.cpoc.directorycleaner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import esa.s1pdgs.cpoc.directorycleaner.config.DirectoryCleanerProperties;

@SpringBootApplication
@EnableConfigurationProperties
//@ComponentScan("esa.s1pdgs.cpoc")
public class Application implements CommandLineRunner {

	private static final Logger LOGGER = LogManager.getLogger(Application.class);

	@Autowired
	private DirectoryCleanerProperties config;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOGGER.info("starting directory cleaner ...");

		// at this point we only use this for myocean
		final DirectoryCleaner directoryCleaner = DirectoryCleanerFactory.newDirectoryCleaner("myocean", this.config);
		directoryCleaner.cleanDirectories();

		LOGGER.info(" ... directory cleaner finished");
	}

}
