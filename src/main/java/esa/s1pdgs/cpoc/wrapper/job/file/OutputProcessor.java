package esa.s1pdgs.cpoc.wrapper.job.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobOutputDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.wrapper.job.model.mqi.FileQueueMessage;
import esa.s1pdgs.cpoc.wrapper.job.model.mqi.ObsQueueMessage;
import esa.s1pdgs.cpoc.wrapper.job.model.obs.S3UploadFile;
import esa.s1pdgs.cpoc.wrapper.job.mqi.OutputProcuderFactory;
import esa.s1pdgs.cpoc.wrapper.job.obs.ObsService;

/**
 * Process outputs according their family: - publication in message queue system
 * if needed - upload in OBS if needed
 * 
 * @author Viveris Technologies
 */
public class OutputProcessor {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(OutputProcessor.class);

    /**
     * Cannot be a key in obs
     */
    protected static final String NOT_KEY_OBS = "IT_IS_NOT_A_KEY";

    /**
     * ISIP extension
     */
    protected static final String EXT_ISIP = "ISIP";

    /**
     * ISIP extension
     */
    protected static final String EXT_SAFE = "SAFE";

    /**
     * OBS service
     */
    private final ObsService obsService;

    /**
     * Output producer factory for message queue system
     */
    private final OutputProcuderFactory procuderFactory;

    /**
     * Working directory
     */
    private final String workDirectory;

    /**
     * Name of the file where the outputs are listed from the working directory
     */
    private final String listFile;

    /**
     * Input message
     */
    private final GenericMessageDto<LevelJobDto> inputMessage;

    /**
     * List of authorized and family correspondance define in the job
     */
    private final List<LevelJobOutputDto> authorizedOutputs;

    /**
     * Size of the batch for upload in OBS
     */
    private final int sizeUploadBatch;

    /**
     * Prefix before each monitor logs
     */
    private final String prefixMonitorLogs;

    /**
     * Application level
     */
    private final ApplicationLevel appLevel;

    /**
     * Constructor
     * 
     * @param obsService
     * @param outputProcuderFactory
     * @param workDirectory
     * @param authorizedOutputs
     * @param listFile
     * @param sizeS3UploadBatch
     * @param prefixMonitorLogs
     */
    public OutputProcessor(final ObsService obsService,
            final OutputProcuderFactory procuderFactory,
            final GenericMessageDto<LevelJobDto> inputMessage,
            final String listFile, final int sizeUploadBatch,
            final String prefixMonitorLogs, final ApplicationLevel appLevel) {
        this.obsService = obsService;
        this.procuderFactory = procuderFactory;
        this.listFile = listFile;
        this.inputMessage = inputMessage;
        this.authorizedOutputs = inputMessage.getBody().getOutputs();
        this.workDirectory = inputMessage.getBody().getWorkDirectory();
        this.sizeUploadBatch = sizeUploadBatch;
        this.prefixMonitorLogs = prefixMonitorLogs;
        this.appLevel = appLevel;
    }

    /**
     * Extract the list of outputs from a file
     * 
     * @return
     * @throws InternalErrorException
     */
    private List<String> extractFiles() throws InternalErrorException {
        LOGGER.info("{} 1 - Extracting list of outputs", prefixMonitorLogs);
        try {
            return Files.lines(Paths.get(listFile))
                    .collect(Collectors.toList());
        } catch (IOException ioe) {
            throw new InternalErrorException("Cannot parse result list file "
                    + listFile + ": " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Sort outputs and convert them into object for message queue system or OBS
     * according the output define in the job they match
     * 
     * @param lines
     * @param uploadBatch
     * @param outputToPublish
     * @param reportToPublish
     * @throws UnknownFamilyException
     */
    protected void sortOutputs(final List<String> lines,
            final List<S3UploadFile> uploadBatch,
            final List<ObsQueueMessage> outputToPublish,
            final List<FileQueueMessage> reportToPublish)
            throws UnknownFamilyException {

        LOGGER.info("{} 2 - Starting organizing outputs", prefixMonitorLogs);

        for (String line : lines) {

            // Extract the product name, the complete filepath, job output and
            // the mode
            String productName = getProductName(line);
            String filePath = getFilePath(line, productName);
            LevelJobOutputDto matchOutput = getMatchOutput(productName);

            // If match process output
            if (matchOutput == null) {
                LOGGER.warn(
                        "Output {} ignored because no found matching regular expression",
                        productName);
            } else {
                ProductFamily family =
                        ProductFamily.fromValue(matchOutput.getFamily());
                switch (family) {
                    case L0_REPORT:
                    case L1_REPORT:
                        // If report, put in a cache to send report
                        LOGGER.info(
                                "Output {} is considered as belonging to the family {}",
                                productName, matchOutput.getFamily());
                        reportToPublish.add(new FileQueueMessage(family,
                                productName, new File(filePath)));
                        break;
                    case L0_SLICE:
                        // Specific case of the L0 wrapper
                        if (appLevel == ApplicationLevel.L0) {
                            if (line.contains("NRT")) {
                                LOGGER.info(
                                        "Output {} is considered as belonging to the family {}",
                                        productName, matchOutput.getFamily());
                                uploadBatch.add(new S3UploadFile(family,
                                        productName, new File(filePath)));
                                outputToPublish.add(new ObsQueueMessage(family,
                                        productName, productName, "NRT"));
                            } else if (line.contains("FAST24")) {
                                LOGGER.info(
                                        "Output {} is considered as belonging to the family {}",
                                        productName, ProductFamily.L0_SEGMENT);
                                uploadBatch.add(new S3UploadFile(
                                        ProductFamily.L0_SEGMENT, productName,
                                        new File(filePath)));
                                outputToPublish.add(new ObsQueueMessage(
                                        ProductFamily.L0_SEGMENT, productName,
                                        productName, "FAST24"));
                            } else {
                                LOGGER.warn(
                                        "Output {} ignored because unknown mode",
                                        productName);
                            }
                        } else {
                            LOGGER.info(
                                    "Output {} is considered as belonging to the family {}",
                                    productName, matchOutput.getFamily());
                            uploadBatch.add(new S3UploadFile(family,
                                    productName, new File(filePath)));
                            outputToPublish.add(new ObsQueueMessage(family,
                                    productName, productName, inputMessage.getBody().getProductProcessMode()));
                        }
                        break;
                    case L0_ACN:
                    case L1_SLICE:
                    case L1_ACN:
                        // If compatible object storage, put in a cache to
                        // upload per batch
                        LOGGER.info(
                                "Output {} is considered as belonging to the family {}",
                                productName, matchOutput.getFamily());
                        uploadBatch.add(new S3UploadFile(family, productName,
                                new File(filePath)));
                        outputToPublish.add(new ObsQueueMessage(family,
                                productName, productName, inputMessage.getBody().getProductProcessMode()));
                        break;
                    case BLANK:
                        LOGGER.info("Output {} will be ignored", productName);
                        break;
                    default:
                        throw new UnknownFamilyException(
                                "Family not managed in output processor ",
                                family.name());
                }
            }

        }
    }

    /**
     * Extract the product name from the line of the result file
     * 
     * @param line
     * @return
     */
    private String getProductName(final String line) {
        // Extract the product name and the complete filepath
        // First, remove the first directory (NRT or REPORT)
        String productName = line;
        int index = line.indexOf('/');
        if (index != -1) {
            productName = line.substring(index + 1);
        }
        // Second: if file ISIP, retrieve only .SAFE
        if (productName.toUpperCase().endsWith(EXT_ISIP)) {
            productName = productName.substring(0,
                    productName.length() - EXT_ISIP.length()) + EXT_SAFE;
        }
        return productName;
    }

    /**
     * Build the path for the object to upload. In most of the cases, it is the
     * concatenation of the working directory and the line. But for .ISIP files,
     * we considers the .SAFE file is the .ISIP directory
     * 
     * @param line
     * @param productName
     * @return
     */
    private String getFilePath(final String line, final String productName) {
        String filePath = workDirectory + line;
        // Second: if file ISIP, retrieve only .SAFE
        if (line.toUpperCase().endsWith("ISIP")) {
            filePath = filePath + File.separator + productName;
        }
        return filePath;
    }

    /**
     * Search if a output defined in the job matches with the product name
     * 
     * @param productName
     * @return
     */
    private LevelJobOutputDto getMatchOutput(final String productName) {
        for (LevelJobOutputDto jobOutputDto : authorizedOutputs) {
            if (Pattern.matches(
                    jobOutputDto.getRegexp().substring(workDirectory.length()),
                    productName)) {
                return jobOutputDto;
            }
        }
        return null;
    }

    /**
     * Process product: upload in OBS and publish in message queue system per
     * batch
     * 
     * @param uploadBatch
     * @param outputToPublish
     * @throws AbstractCodedException
     */
    protected void processProducts(final List<S3UploadFile> uploadBatch,
            final List<ObsQueueMessage> outputToPublish)
            throws AbstractCodedException {
        LOGGER.info(
                "[REPORT] {} [s1pdgsTask {}Processing] [subTask outputCopy] [START] 3 - Starting processing object storage compatible outputs",
                prefixMonitorLogs, this.appLevel);

        double size = Double.valueOf(uploadBatch.size());
        double nbPool = Math.ceil(size / sizeUploadBatch);

        for (int i = 0; i < nbPool; i++) {
            int lastIndex =
                    Math.min((i + 1) * sizeUploadBatch, uploadBatch.size());
            List<S3UploadFile> sublist =
                    uploadBatch.subList(i * sizeUploadBatch, lastIndex);

            if (i > 0) {
                this.publishAccordingUploadFiles(i - 1, sublist.get(0).getKey(),
                        outputToPublish);
            }
            LOGGER.info("{} 3 - Uploading batch {} ", prefixMonitorLogs, i);
            if (Thread.currentThread().isInterrupted()) {
                throw new InternalErrorException(
                        "The current thread as been interrupted");
            } else {
                this.obsService.uploadFilesPerBatch(sublist);
            }
        }
        String listoutputs = "";
        for (int i = 0; i < size; i++) {
            listoutputs = listoutputs + " " + uploadBatch.get(i).getKey();
        }
        LOGGER.info(
                "[REPORT] {} [s1pdgsTask {}Processing] [subTask outputCopy] [STOP OK] 3 - Publishing KAFKA messages for the last batch [outputs {}]",
                this.prefixMonitorLogs, this.appLevel, listoutputs);
        publishAccordingUploadFiles(nbPool - 1, NOT_KEY_OBS, outputToPublish);
    }

    /**
     * Public uploaded files, i.e. unitl the output to publish is the next key
     * to upload
     * 
     * @param nbBatch
     * @param nextKeyUpload
     * @param outputToPublish
     * @throws AbstractCodedException
     */
    private void publishAccordingUploadFiles(final double nbBatch,
            final String nextKeyUpload,
            final List<ObsQueueMessage> outputToPublish)
            throws AbstractCodedException {
        LOGGER.info("{} 3 - Publishing KAFKA messages for batch {}",
                prefixMonitorLogs, nbBatch);
        Iterator<ObsQueueMessage> iter = outputToPublish.iterator();
        boolean stop = false;
        while (!stop && iter.hasNext()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InternalErrorException(
                        "The current thread as been interrupted");
            } else {
                ObsQueueMessage msg = iter.next();
                if (nextKeyUpload.startsWith(msg.getKeyObs())) {
                    stop = true;
                } else {
                    LOGGER.info("{} 3 - Publishing message for output {}",
                            prefixMonitorLogs, msg.getProductName());
                    try {
                        procuderFactory.sendOutput(msg, inputMessage);
                    } catch (MqiPublicationError ace) {
                        LOGGER.error("{} [code {}] {}", prefixMonitorLogs,
                                ace.getCode().getCode(), ace.getLogMessage());
                    }
                    iter.remove();
                }
            }
        }
    }

    /**
     * Publish reports in message queue system
     * 
     * @param reportToPublish
     * @throws AbstractCodedException
     */
    protected void processReports(final List<FileQueueMessage> reportToPublish)
            throws AbstractCodedException {

        LOGGER.info(
                "{} 4 - Starting processing not object storage compatible outputs",
                prefixMonitorLogs);
        if (!reportToPublish.isEmpty()) {
            for (FileQueueMessage msg : reportToPublish) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InternalErrorException(
                            "The current thread as been interrupted");
                } else {
                    LOGGER.info("{} 4 - Publishing KAFKA message for output {}",
                            prefixMonitorLogs, msg.getProductName());
                    try {
                        procuderFactory.sendOutput(msg, inputMessage);
                    } catch (MqiPublicationError ace) {
                        String message = String.format("%s [code %d] %s",
                                prefixMonitorLogs, ace.getCode().getCode(),
                                ace.getLogMessage());
                        LOGGER.error(message);
                        procuderFactory.sendError(message);
                    }
                }
            }
        }
    }

    /**
     * Function which process all the output of L0 process
     * 
     * @throws ObsException
     * @throws IOException
     */
    public void processOutput() throws AbstractCodedException {

        // Extract files
        List<String> lines = extractFiles();

        // Sort outputs
        List<S3UploadFile> uploadBatch = new ArrayList<>();
        List<ObsQueueMessage> outputToPublish = new ArrayList<>();
        List<FileQueueMessage> reportToPublish = new ArrayList<>();
        sortOutputs(lines, uploadBatch, outputToPublish, reportToPublish);

        // Upload per batch the output
        processProducts(uploadBatch, outputToPublish);

        // Publish reports
        processReports(reportToPublish);
    }

}
