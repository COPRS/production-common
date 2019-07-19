package esa.s1pdgs.cpoc.wrapper.job.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import esa.s1pdgs.cpoc.obs_sdk.s3.S3UploadFile;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.wrapper.job.model.mqi.FileQueueMessage;
import esa.s1pdgs.cpoc.wrapper.job.model.mqi.ObsQueueMessage;
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
    final long sortOutputs(final List<String> lines,
            final List<S3UploadFile> uploadBatch,
            final List<ObsQueueMessage> outputToPublish,
            final List<FileQueueMessage> reportToPublish)
            throws AbstractCodedException {

    	long productSize = 0;
    	
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
                
                final File file = new File(filePath);
                                
                switch (family) {
                    case L0_REPORT:
                    case L1_REPORT:
                    case L2_REPORT:
                    case L0_SEGMENT_REPORT:
                        // If report, put in a cache to send report
                        LOGGER.info(
                                "Output {} is considered as belonging to the family {}",
                                productName, matchOutput.getFamily());                   
                        reportToPublish.add(new FileQueueMessage(family, productName, file));
                        productSize += size(file);
                        break;
                    case L0_SLICE:
                        // Specific case of the L0 wrapper
                        if (appLevel == ApplicationLevel.L0) {
                            if (line.contains("NRT")) {
                                LOGGER.info(
                                        "Output {} is considered as belonging to the family {}",
                                        productName, matchOutput.getFamily());
                                
                                uploadBatch.add(new S3UploadFile(family, productName, file));
                                outputToPublish.add(new ObsQueueMessage(family, productName, productName, "NRT"));
                                productSize += size(file);
                                
                            } else if (line.contains("FAST24")) {
                                LOGGER.info(
                                        "Output {} is considered as belonging to the family {}",
                                        productName, ProductFamily.L0_SEGMENT);
                                uploadBatch.add(new S3UploadFile(ProductFamily.L0_SEGMENT, productName,file));
                                outputToPublish.add(new ObsQueueMessage( ProductFamily.L0_SEGMENT, productName, productName, "FAST24"));
                                productSize += size(file);
                            } else {
                                LOGGER.warn( "Output {} ignored because unknown mode", productName);
                            }
                        } else {
                            LOGGER.info(
                                    "Output {} is considered as belonging to the family {}",
                                    productName, matchOutput.getFamily());
                            uploadBatch.add(new S3UploadFile(family, productName, file));
                            outputToPublish.add( new ObsQueueMessage(family, productName, productName, inputMessage.getBody().getProductProcessMode()));
                            productSize += size(file);
                        }
                        break;
                    case L0_ACN:
                    case L0_BLANK:
                        // Specific case of the L0 wrapper
                        if (appLevel == ApplicationLevel.L0) {
                            if (line.contains("NRT")) {
                                LOGGER.info(
                                        "Output {} is considered as belonging to the family {}",
                                        productName, matchOutput.getFamily());
                                uploadBatch.add(new S3UploadFile(family,  productName, file));
                                outputToPublish.add(new ObsQueueMessage(family, productName, productName, "NRT"));
                                productSize += size(file);
                            } else if (line.contains("FAST24")) {
                                LOGGER.info(
                                        "Output {} is considered as belonging to the family {}",
                                        productName, matchOutput.getFamily());
                                uploadBatch.add(new S3UploadFile(family, productName, file));
                                outputToPublish.add(new ObsQueueMessage(family, productName, productName, "FAST24"));
                                productSize += size(file);
                            } else {
                                LOGGER.warn(
                                        "Output {} ignored because unknown mode",
                                        productName);
                            }
                        } else {
                            LOGGER.info(
                                    "Output {} is considered as belonging to the family {}",
                                    productName, matchOutput.getFamily());
                            uploadBatch.add(new S3UploadFile(family, productName,file));
                            outputToPublish.add(
                                    new ObsQueueMessage(family, productName,
                                            productName, inputMessage.getBody()
                                                    .getProductProcessMode()));
                            productSize += size(file);
                        }
                        break;
                    case L1_SLICE:
                    case L1_ACN:
                    case L2_SLICE:
                    case L2_ACN:
                        // If compatible object storage, put in a cache to
                        // upload per batch
                        LOGGER.info(
                                "Output {} is considered as belonging to the family {}",
                                productName, matchOutput.getFamily());
                        uploadBatch.add(new S3UploadFile(family, productName,
                                file));
                        outputToPublish.add(new ObsQueueMessage(family,
                                productName, productName, inputMessage.getBody()
                                        .getProductProcessMode()));
                        productSize += size(file);
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
        return productSize;
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
    final void processProducts(
    		final Reporting.Factory reportingFactory,
    		final List<S3UploadFile> uploadBatch,
            final List<ObsQueueMessage> outputToPublish)
            throws AbstractCodedException {

        double size = Double.valueOf(uploadBatch.size());
        double nbPool = Math.ceil(size / sizeUploadBatch);

        for (int i = 0; i < nbPool; i++) {
            int lastIndex = Math.min((i + 1) * sizeUploadBatch, uploadBatch.size());
            List<S3UploadFile> sublist =uploadBatch.subList(i * sizeUploadBatch, lastIndex);
            String listProducts = sublist.stream().map(S3UploadFile::getKey).collect(Collectors.joining(","));
            
            if (i > 0) {
                this.publishAccordingUploadFiles(reportingFactory, i - 1, sublist.get(0).getKey(), outputToPublish);
            }
            final Reporting report = reportingFactory
        			.product(null, null)            			
        			.newReporting(2);
            try { 
            	report.reportStart("Start uploading batch " + i + " of outputs " + listProducts);
            	
                if (Thread.currentThread().isInterrupted()) {
                    throw new InternalErrorException("The current thread as been interrupted");
                } 
                this.obsService.uploadFilesPerBatch(sublist);
                report.reportStop("End uploading batch " + i + " of outputs " + listProducts);
              
            } catch (AbstractCodedException e) {
    			report.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
                throw e;
            }
        }
        publishAccordingUploadFiles(reportingFactory, nbPool - 1, NOT_KEY_OBS, outputToPublish);
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
    private void publishAccordingUploadFiles(
    		final Reporting.Factory reportingFactory,
    		final double nbBatch,
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
            } 
            ObsQueueMessage msg = iter.next();
            if (nextKeyUpload.startsWith(msg.getKeyObs())) {
                stop = true;
            } else {
            	final Reporting report = reportingFactory
            			.product(null, msg.getProductName())            			
            			.newReporting(1);
            	
            	report.reportStart("Start publishing message");
                try {
                    procuderFactory.sendOutput(msg, inputMessage);
                    report.reportStop("End publishing message");
                } catch (MqiPublicationError ace) {                	
                	report.reportError("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage());
                }
                iter.remove();
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
        final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "OutputHandling");
        
        // Extract files
        List<String> lines = extractFiles();

        // Sort outputs
        List<S3UploadFile> uploadBatch = new ArrayList<>();
        List<ObsQueueMessage> outputToPublish = new ArrayList<>();
        List<FileQueueMessage> reportToPublish = new ArrayList<>();
    	final long size = sortOutputs(lines, uploadBatch, outputToPublish, reportToPublish);
        
        final String listoutputs = uploadBatch.stream().map(S3UploadFile::getKey)
                .collect(Collectors.joining(","));
        
        final Reporting reporting = reportingFactory.newReporting(0);
        reporting.reportStart("Start handling of outputs " + listoutputs);
        
        try {
			// Upload per batch the output        
			processProducts(reportingFactory, uploadBatch, outputToPublish);			
			 // Publish reports
	        processReports(reportToPublish);
	        
	        reporting.reportStopWithTransfer("End handling of outputs " + listoutputs, size);
		} catch (AbstractCodedException e) {
			reporting.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
			throw e;
		}       
    }    

    private long size(File file) throws InternalErrorException {
        try {
			final Path folder = file.toPath();
			return Files.walk(folder)
			  .filter(p -> p.toFile().isFile())
			  .mapToLong(p -> p.toFile().length())
			  .sum();
			
		} catch (IOException e) {
			// TODO to have the tests running without actual files
			return 0L;
		}
	}
}
