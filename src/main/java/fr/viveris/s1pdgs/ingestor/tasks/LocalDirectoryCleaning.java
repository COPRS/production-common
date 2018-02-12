package fr.viveris.s1pdgs.ingestor.tasks;

import java.io.File;

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

	private final long configMinAge;

	private final long sessionMinAge;

	@Autowired
	public LocalDirectoryCleaning(@Value("${file.session-files.local-directory}") final String sessionLocalDirectory,
			@Value("${file.session-files.clean-empty-min-age}") final long sessionMinAge,
			@Value("${file.config-files.local-directory}") final String configLocalDirectory,
			@Value("${file.config-files.clean-empty-min-age}") final long configMinAge) {
		this.sessionLocalDirectory = sessionLocalDirectory;
		this.configLocalDirectory = configLocalDirectory;
		this.configMinAge = configMinAge;
		this.sessionMinAge = sessionMinAge;
	}

	@Scheduled(fixedDelayString = "${file.config-files.clean-empty-directory-rate}")
	public void cleanConfigFilesDirectory() {
		try {
			long lastModifiedMin = System.currentTimeMillis() - configMinAge;
			File file = new File(this.configLocalDirectory);
			File[] files = file.listFiles();
			if (files != null) {
				for (File child : files) {
					this.recursiveDelete(child, lastModifiedMin);
				}
			}
		} catch (SecurityException e) {
			LOGGER.error("Error during removing empty directories for config files", e.getMessage());
		}
	}

	@Scheduled(fixedDelayString = "${file.session-files.clean-empty-directory-rate}")
	public void cleanSessionFilesDirectory() {
		try {
			long lastModifiedMin = System.currentTimeMillis() - sessionMinAge;
			File file = new File(this.sessionLocalDirectory);
			File[] files = file.listFiles();
			if (files != null) {
				for (File child : files) {
					this.recursiveDelete(child, lastModifiedMin);
				}
			}
		} catch (SecurityException e) {
			LOGGER.error("Error during removing empty directories for ERDS session files", e.getMessage());
		}
	}

	private long recursiveDelete(File file, long lastModifiedMin) throws SecurityException {
		long nbFiles = 0;
		if (file != null && file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File child : files) {
					if (child.isDirectory()) {
						nbFiles += recursiveDelete(child, lastModifiedMin);
					} else {
						nbFiles += 1;
					}
				}
			}
			if (nbFiles == 0 && file.lastModified() <= lastModifiedMin) {
				file.delete();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Remove empty directory {}", file.getPath());
				}
			}
		}

		return nbFiles;
	}
}
