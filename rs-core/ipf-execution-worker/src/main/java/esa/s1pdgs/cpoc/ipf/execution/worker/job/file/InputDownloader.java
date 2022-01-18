package esa.s1pdgs.cpoc.ipf.execution.worker.job.file;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ApplicationLevel;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.errors.UnknownFamilyException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.ipf.execution.worker.job.process.PoolExecutorCallable;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobInputDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

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
    private final ObsClient obsClient;

    /**
     * Path to the local working directory
     */
    private final String localWorkingDir;

    /**
     * List of all the inputs
     */
    private final List<LevelJobInputDto> inputs;

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
     * XSLT file to apply on the joborder
     */
    private final String jobOrderXslt;
    
    /**
     * Constructor
     * 
     * @param obsClient
     * @param localWorkingDir
     * @param inputs
     * @param sizeS3DownloadBatch
     * @param prefixMonitorLogs
     * @param poolProcessorExecutor
     * @param appLevel
     */
    public InputDownloader(final ObsClient obsClient,
            final String localWorkingDir, final List<LevelJobInputDto> inputs,
            final int sizeDownBatch, final String prefixMonitorLogs,
            final PoolExecutorCallable poolProcExecutor,
            final ApplicationLevel appLevel, final String jobOrderXslt) {
        this.obsClient = obsClient;
        this.localWorkingDir = localWorkingDir;
        this.inputs = inputs;
        this.sizeDownBatch = sizeDownBatch;
        this.poolProcExecutor = poolProcExecutor;
        this.appLevel = appLevel;
        this.prefixMonitorLogs = prefixMonitorLogs;
        this.jobOrderXslt = jobOrderXslt;
    }

    /**
     * Prepare the working directory by downloading all needed inputs
     * 
     * @throws AbstractCodedException
     */
    public List<ObsDownloadObject> processInputs(final ReportingFactory reportingFactory) throws AbstractCodedException {
        initializeDownload();
        final List<ObsDownloadObject> result = sortInputs(); // also creates necessary directories      
        
        final List<ObsDownloadObject> downloadToBatch = result.stream()
        		.filter(o -> o.getFamily() != ProductFamily.INVALID)
        		.collect(Collectors.toList());        
		downloadInputs(downloadToBatch, reportingFactory);
        completeDownload();	      	
        return result;
    }

    /**
     * Create the working directory and the status file
     * 
     * @throws InternalErrorException
     */
    private void initializeDownload() throws InternalErrorException {
        LOGGER.info("{} 1 - Creating working directory", prefixMonitorLogs);
        final File workingDir = new File(localWorkingDir);
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
    private final void completeDownload() throws InternalErrorException {
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
    protected List<ObsDownloadObject> sortInputs()
            throws InternalErrorException, UnknownFamilyException {
        LOGGER.info("{} 3 - Starting organizing inputs", prefixMonitorLogs);

        final List<ObsDownloadObject> downloadToBatch = new ArrayList<>();

        for (final LevelJobInputDto input : inputs) {
            // Check if a directory shall be created
            final File parent = (new File(input.getLocalPath())).getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            // Upload input if in message else wait to list all input and
            // download them from
            // object storage per batch
            switch (input.getFamily()) {
                case "JOB_ORDER":
                    LOGGER.info("Job order will be stored in {}",
                            input.getLocalPath());
                    String fileContent = input.getContentRef();
                    if (jobOrderXslt != null && !jobOrderXslt.isEmpty()) {
                    	try {
                    		LOGGER.info("Transforming job order with xslt from file '{}'", jobOrderXslt);
                    		fileContent = transformJobOrder(fileContent);
                    	} catch (TransformerException e) {
                    		LOGGER.error("Failed to transform job order with xslt. Maybe there is an issue with the xslt file ({})?", jobOrderXslt);
                    		throw new InternalErrorException("An exception occured while transforming the Joborder: " + e.getMessage());
                    	}
                    }
                    
                    FileUtils.writeFile(input.getLocalPath(),
                            fileContent);
                    break;
                case "EDRS_SESSION":
                case "AUXILIARY_FILE":
                case "L0_SLICE":
                case "L0_ACN":
                case "L0_SEGMENT":
                case "L1_SLICE":
                case "L1_ACN":
                case "L2_ACN":
                case "L2_SLICE":
                case "S3_GRANULES":
                case "S3_AUX":
                case "S3_L0":
                case "S3_L1_NRT":
                case "S3_L1_STC":
                case "S3_L1_NTC":
                case "S3_L2_NRT":
                case "S3_L2_STC":
                case "S3_L2_NTC":
                case "S3_CAL":
                case "S3_PUG":
                    LOGGER.info("Input {}-{} will be stored in {}",
                            input.getFamily(), input.getContentRef(),
                            input.getLocalPath());
                    ObsDownloadObject downloadObj = toObsDlObject(input);
                    
                    /*
                     *  Adding a detection if a job order contains multiple times the same input
                     *  This is a valid scenario for S3 productions, but might result in ObsParallelExceptions.
                     *  As the product is already downloaded the IPF shall work fine.
                     *  
                     */
                    
                    if (downloadToBatch.contains(downloadObj)) {
                    	LOGGER.warn("Detected duplicate input {} and prevent downloading it.", input.getLocalPath());
                    } else {
                    	downloadToBatch.add(downloadObj);	
                    }
                    
                    break;
                case "BLANK":
                    LOGGER.info("Input {} will be ignored", input.getContentRef());
                    break;
                default:
                    throw new UnknownFamilyException(
                            "Family not managed in input downloader ",
                            input.getFamily());
            }

        }
        return downloadToBatch;
    }

	private final ObsDownloadObject toObsDlObject(final LevelJobInputDto input) {
		final ProductFamily family = ProductFamily.fromValue(input.getFamily());		
		final File localFile = new File(input.getLocalPath());

		if (input.getContentRef() != null) {
			return new ObsDownloadObject(
					family,
					input.getContentRef(),
					localFile.getParent()
			);
		} else if (family == ProductFamily.EDRS_SESSION) {
			LOGGER.warn("Missing RAW {}. Trying to process without it...", localFile.getName());
			// mark product as invalid but provide the chunk name for logging 
			return new ObsDownloadObject(
					ProductFamily.INVALID,
					localFile.getName(),
					null				
			);
		} else {
			throw new IllegalArgumentException(
					String.format("Missing OBS reference in input %s", input)
			);
		}
	}	

    /**
     * Download input from OBS per batch. If we have download 2 raw, the
     * processor executor can start launch proceses
     * 
     * @param downloadToBatch
     * @throws AbstractCodedException
     */
    private final void downloadInputs(final List<ObsDownloadObject> downloadToBatch, final ReportingFactory reportingFactory)
            throws AbstractCodedException {

        final int numberOfBatches = (int) Math.ceil(((double) downloadToBatch.size()) / ((double) sizeDownBatch));

        int nbUploadedRaw = 0;
        for (int i = 0; i < numberOfBatches; i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InternalErrorException("The current thread as been interrupted");
            } else {
                LOGGER.info("{} 4 - Starting downloading batch {}", prefixMonitorLogs, i);
                final int lastIndex = Math.min((i + 1) * sizeDownBatch, downloadToBatch.size());                
                final List<ObsDownloadObject> subListS3 = downloadToBatch.subList(i * sizeDownBatch, lastIndex);
                this.obsClient.download(subListS3, reportingFactory);
                if (appLevel == ApplicationLevel.L0 && nbUploadedRaw < 2) {
                    nbUploadedRaw += subListS3.stream().filter(
                            file -> file.getFamily() == ProductFamily.EDRS_SESSION)
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
    
    /**
     * Transform given jobOrder with the xslt in variable jobOrderXslt
     * @throws TransformerException 
     */
    private final String transformJobOrder(String jobOrder) throws TransformerException {
    	TransformerFactory factory = TransformerFactory.newInstance();
    	Source xslt  = new StreamSource(new File(jobOrderXslt));
    	Transformer transformer = factory.newTransformer(xslt);
    	
    	StringWriter outputWriter = new StringWriter();
    	transformer.transform(new StreamSource(new StringReader(jobOrder)), new StreamResult(outputWriter));
    	return outputWriter.toString();
    }
}
