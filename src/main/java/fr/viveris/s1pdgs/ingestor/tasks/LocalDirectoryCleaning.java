package fr.viveris.s1pdgs.ingestor.tasks;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LocalDirectoryCleaning {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalDirectoryCleaning.class);

	private final String sessionLocalDirectory;

	/**
	 * Local directory for reading the configuration files
	 */
	private final String configLocalDirectory;

	@Autowired
	public LocalDirectoryCleaning(@Value("${file.session-files.local-directory}") final String sessionLocalDirectory,
			@Value("${file.config-files.local-directory}") final String configLocalDirectory) {
		this.sessionLocalDirectory = sessionLocalDirectory;
		this.configLocalDirectory = configLocalDirectory;
	}

	@Scheduled(fixedDelayString = "${file.config-files.clean-empty-directory-rate}")
	public void cleanConfigFilesDirectory() {
		try {
			Runtime.getRuntime().exec("find " + this.configLocalDirectory + "* -cmin +1 -delete");
		} catch (IOException e) {
			LOGGER.error("Error during removing empty directories for config files", e.getMessage());
		}
	}

	@Scheduled(fixedDelayString = "${file.session-files.clean-empty-directory-rate}")
	public void cleanSessionFilesDirectory() {
		try {
			Runtime.getRuntime().exec("find " + this.sessionLocalDirectory + "* -cmin +1 -delete");
		} catch (IOException e) {
			LOGGER.error("Error during removing empty directories for ERDS session files", e.getMessage());
		}
	}
}
