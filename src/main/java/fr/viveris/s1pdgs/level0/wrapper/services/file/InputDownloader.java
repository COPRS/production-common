package fr.viveris.s1pdgs.level0.wrapper.services.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobInputDto;
import fr.viveris.s1pdgs.level0.wrapper.model.ApplicationLevel;
import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.InternalErrorException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.UnknownFamilyException;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3DownloadFile;
import fr.viveris.s1pdgs.level0.wrapper.services.s3.ObsService;
import fr.viveris.s1pdgs.level0.wrapper.services.task.PoolExecutorCallable;
import fr.viveris.s1pdgs.level0.wrapper.utils.FileUtils;

/**
 * Class which create the local working directory and download all the inputs
 * files
 * 
 * @author Viveris Technologies
 */
public class InputDownloader {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(InputDownloader.class);

    /**
     * Status ongoing
     */
    protected static final String STATUS_CREATION = "ONGOING";

    /**
     * Status ongoing
     */
    protected static final String STATUS_COMPLETION = "COMPLETED";

    /**
     * Factory for accessing to the object storage
     */
    private final ObsService obsService;

    /**
     * Path to the local working directory
     */
    private final String localWorkingDir;

    /**
     * List of all the inputs
     */
    private final List<JobInputDto> inputs;

    /**
     * Batch size for downloading inputs from OBS
     */
    private final int sizeDownBatch;

    /**
     * Prefix to concatene to monitor logs
     */
    private final String prefixMonitorLogs;

    /**
     * Executor which executes processes. Shall be informed when all inputs are
     * download
     */
    private final PoolExecutorCallable poolProcExecutor;

    /**
     * Application level
     */
    private final ApplicationLevel appLevel;

    /**
     * Constructor
     * 
     * @param obsService
     * @param localWorkingDir
     * @param inputs
     * @param sizeS3DownloadBatch
     * @param prefixMonitorLogs
     * @param poolProcessorExecutor
     * @param appLevel
     */
    public InputDownloader(final ObsService obsService,
            final String localWorkingDir, final List<JobInputDto> inputs,
            final int sizeDownBatch, final String prefixMonitorLogs,
            final PoolExecutorCallable poolProcExecutor,
            final ApplicationLevel appLevel) {
        this.obsService = obsService;
        this.localWorkingDir = localWorkingDir;
        this.inputs = inputs;
        this.sizeDownBatch = sizeDownBatch;
        this.poolProcExecutor = poolProcExecutor;
        this.appLevel = appLevel;
        this.prefixMonitorLogs = prefixMonitorLogs;
    }

    /**
     * Prepare the working directory by downloading all needed inputs
     * 
     * @throws AbstractCodedException
     */
    public void processInputs() throws AbstractCodedException {

        // Initialize
        initializeDownload();

        // Create necessary directories and download input with content in
        // message
        List<S3DownloadFile> downloadToBatch = sortInputs();

        // Download input from object storage in batch
        downloadInputs(downloadToBatch);

        // Complete download
        completeDownload();
    }

    /**
     * Create the working directory and the status file
     * 
     * @throws InternalErrorException
     */
    private void initializeDownload() throws InternalErrorException {
        LOGGER.info("{} 1 - Creating working directory", prefixMonitorLogs);
        File workingDir = new File(localWorkingDir);
        workingDir.mkdirs();

        LOGGER.info("{} 2 - Creating status.txt file with ONGOING",
                prefixMonitorLogs);
        this.writeStatusFile(STATUS_CREATION);
    }

    /**
     * Update the status file with status COMPLETED and inform process executor
     * its finished
     * 
     * @throws InternalErrorException
     */
    protected void completeDownload() throws InternalErrorException {
        LOGGER.info("{} 5 - Updating status.txt file with COMPLETED",
                prefixMonitorLogs);
        this.writeStatusFile(STATUS_COMPLETION);
        poolProcExecutor.setActive(true);
    }

    /**
     * Write in status file
     * 
     * @param status
     * @throws InternalErrorException
     */
    private void writeStatusFile(final String status)
            throws InternalErrorException {
        FileUtils.writeFile(localWorkingDir + "Status.txt", status);
    }

    /**
     * Sort inputs: - if JOB => create the file - if RAW / CONFIG / L0_PRODUCT /
     * L0_ACN => convert into S3DownloadFile - if BLANK => ignore - else =>
     * throw exception
     * 
     * @return
     * @throws InternalErrorException
     * @throws UnknownFamilyException
     */
    protected List<S3DownloadFile> sortInputs()
            throws InternalErrorException, UnknownFamilyException {
        LOGGER.info("{} 3 - Starting organizing inputs", prefixMonitorLogs);

        List<S3DownloadFile> downloadToBatch = new ArrayList<>();

        for (JobInputDto input : inputs) {
            // Check if a directory shall be created
            File parent = (new File(input.getLocalPath())).getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            // Upload input if in message else wait to list all input and
            // download them from
            // object storage per batch
            switch (input.getFamily()) {
                case "JOB":
                    LOGGER.info("Job order will be stored in {}",
                            input.getLocalPath());
                    FileUtils.writeFile(input.getLocalPath(),
                            input.getContentRef());
                    break;
                case "RAW":
                case "CONFIG":
                case "L0_PRODUCT":
                case "L0_ACN":
                    LOGGER.info("Input {}-{} will be stored in {}",
                            input.getFamily(), input.getContentRef(),
                            input.getLocalPath());
                    downloadToBatch.add(new S3DownloadFile(
                            ProductFamily.fromValue(input.getFamily()),
                            input.getContentRef(),
                            (new File(input.getLocalPath()).getParent())));
                    break;
                case "BLANK":
                    LOGGER.info("Input {} will be ignored",
                            input.getContentRef());
                    break;
                default:
                    throw new UnknownFamilyException(
                            "Family not managed in input downloader ",
                            input.getFamily());
            }

        }
        return downloadToBatch;
    }

    /**
     * Download input from OBS per batch. If we have download 2 raw, the
     * processor executor can start launch proceses
     * 
     * @param downloadToBatch
     * @throws AbstractCodedException
     */
    protected void downloadInputs(final List<S3DownloadFile> downloadToBatch)
            throws AbstractCodedException {
        LOGGER.info("{} 4 - Starting downloading inputs from object storage",
                prefixMonitorLogs);
        double size = Double.valueOf(downloadToBatch.size());
        double nbPool = Math.ceil(size / sizeDownBatch);
        int nbUploadedRaw = 0;
        for (int i = 0; i < nbPool; i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InternalErrorException(
                        "The current thread as been interrupted");
            } else {
                LOGGER.info("{} 4 - Starting downloading batch {}",
                        prefixMonitorLogs, i);
                int lastIndex = Math.min((i + 1) * sizeDownBatch,
                        downloadToBatch.size());
                List<S3DownloadFile> subListS3 =
                        downloadToBatch.subList(i * sizeDownBatch, lastIndex);
                this.obsService.downloadFilesPerBatch(subListS3);
                if (appLevel == ApplicationLevel.L0 && nbUploadedRaw < 2) {
                    nbUploadedRaw += subListS3.stream().filter(
                            file -> file.getFamily() == ProductFamily.RAW)
                            .count();
                    if (nbUploadedRaw >= 2) {
                        // On suppose l'ordre de traitement des input:
                        // les 2 preemiers RAW sont le raw1
                        // du channel 1 et le raw 1 du channel 2
                        LOGGER.info("{} 4 - Setting process executor as active",
                                prefixMonitorLogs);
                        poolProcExecutor.setActive(true);
                    }

                }
            }
        }
    }
}
