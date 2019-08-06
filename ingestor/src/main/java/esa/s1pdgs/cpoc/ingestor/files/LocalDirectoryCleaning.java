package esa.s1pdgs.cpoc.ingestor.files;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.utils.LogUtils;

/**
 * Clean empty directories (since too long) of auxiliary and EDRS session FTP
 * directories
 * 
 * @author Cyrielle
 */
@Component
public class LocalDirectoryCleaning {

    private static final Logger LOGGER =
            LogManager.getLogger(LocalDirectoryCleaning.class);

    /**
     * Used directory for EDRS sessions
     */
    private final String sessionDir;

    /**
     * if file.lastmodified <= curretn milliseconds - min age AND dir is empty
     * => we can delete it
     */
    private final long sessionMinAge;

    /**
     * Used directory for auxiliary files
     */
    private final String auxiliaryDir;

    /**
     * if file.lastmodified <= curretn milliseconds - min age AND dir is empty
     * => we can delete it
     */
    private final long configMinAge;

    /**
     * Constructor
     * 
     * @param sessionLocalDirectory
     * @param sessionMinAge
     * @param configLocalDirectory
     * @param configMinAge
     */
    @Autowired
    public LocalDirectoryCleaning(
            @Value("${file.session-files.local-directory}") final String sessionDir,
            @Value("${file.session-files.clean-empty-min-age}") final long sessionMinAge,
            @Value("${file.auxiliary-files.local-directory}") final String auxiliaryDir,
            @Value("${file.auxiliary-files.clean-empty-min-age}") final long configMinAge) {
        this.sessionDir = sessionDir;
        this.auxiliaryDir = auxiliaryDir;
        this.configMinAge = configMinAge;
        this.sessionMinAge = sessionMinAge;
    }

    /**
     * Periodically clean the auxiliary files directory
     */
    @Scheduled(fixedDelayString = "${file.auxiliary-files.clean-empty-directory-rate}")
    public void cleanAuxiliaryFilesDirectory() {
        this.cleanDirectory(auxiliaryDir, configMinAge);
    }

    /**
     * Periodically clean the EDRS session directory
     */
    @Scheduled(fixedDelayString = "${file.session-files.clean-empty-directory-rate}")
    public void cleanSessionFilesDirectory() {
        this.cleanDirectory(sessionDir, sessionMinAge);
    }

    /**
     * If possible, clean a directory or its su-directories when they have not
     * been modified since minAge
     * 
     * @param directory
     * @param minAge
     */
    protected void cleanDirectory(final String directory, final long minAge) {
        try {
            long lastModifiedMin = System.currentTimeMillis() - minAge;
            File file = new File(directory);
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    this.recursiveEmptyDirectoriesDeletion(child,
                            lastModifiedMin);
                }
            }
        } catch (SecurityException e) {
            LOGGER.error("[code {}] [directory {}] [age {}] [msg {}]", 202,
                    directory, minAge, LogUtils.toString(e));
        }
    }

    /**
     * Recursively remove a directory and its sub-directories if they have not
     * been modified since min age
     * 
     * @param file
     * @param lastModifiedMin
     * @return
     * @throws SecurityException
     */
    protected long recursiveEmptyDirectoriesDeletion(final File file,
            final long lastModifiedMin) throws SecurityException {
        long nbFiles = 0;
        if (file != null && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (child.isDirectory()) {
                        nbFiles += recursiveEmptyDirectoriesDeletion(child,
                                lastModifiedMin);
                    } else {
                        nbFiles += 1;
                    }
                }
            }
            if (nbFiles == 0 && file.lastModified() <= lastModifiedMin) {
                file.delete();
            }
        }

        return nbFiles;
    }
}
