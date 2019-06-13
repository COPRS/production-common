package esa.s1pdgs.cpoc.compression.file;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.compression.model.obs.S3UploadFile;
import esa.s1pdgs.cpoc.compression.obs.ObsService;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

public class FileUploader {
    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(FileUploader.class);
    
    private final String workingDir;
    /**
     * OBS service
     */
    private final ObsService obsService;
    
    public FileUploader(final ObsService obsService, String workingDir) {
    	 this.obsService = obsService;
    	 this.workingDir = workingDir;
    }
    		
    public void processOutput() {
//    	 final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "FileUploader");
//    	 final Reporting reporting = reportingFactory.newReporting(0);
//    	 
//    	 try {
////    		 File workDir = new File(workingDir+"")
////    		 S3UploadFile uploadBatch = new S3UploadFile(family, key, );
//// 			// Upload per batch the output        
//// 			processProducts(reportingFactory, uploadBatch, outputToPublish);			
//// 			 // Publish reports
////// 	        processReports(reportToPublish);
//// 	        
//// 	        reporting.reportStopWithTransfer("End handling of output {} " + listoutputs, size);
// 		} catch (AbstractCodedException e) {
// 			reporting.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
// 			throw e;
// 		}        
    }
    
    final void processProducts(
    		final Reporting.Factory reportingFactory,
    		final S3UploadFile uploadBatch) {
    	
    }

}
