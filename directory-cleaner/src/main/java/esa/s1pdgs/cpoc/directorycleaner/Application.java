package esa.s1pdgs.cpoc.directorycleaner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import esa.s1pdgs.cpoc.directorycleaner.config.DirectoryCleanerProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Application implements CommandLineRunner {

	@Autowired
	private DirectoryCleanerProperties config;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// at this point we only use this for myocean
		final DirectoryCleaner directoryCleaner = DirectoryCleanerFactory.newDirectoryCleaner("myocean", this.config);
		directoryCleaner.cleanDirectories();
	}

}
