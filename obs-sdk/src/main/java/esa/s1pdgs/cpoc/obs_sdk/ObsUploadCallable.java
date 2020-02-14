package esa.s1pdgs.cpoc.obs_sdk;

import java.util.Arrays;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

/**
 * Callable to upload a file / folder in the OBS
 * 
 * @author Viveris Technologies
 */
public class ObsUploadCallable implements Callable<Void> {

    /**
     * OBS client
     */
    private final ObsClient obsClient;

    /**
     * Objects to upload
     */
    private final ObsUploadObject object;
    
    private final ReportingFactory reportingFactory;

    /**
     * Constructor
     * 
     * @param obsClient
     * @param object
     */
    public ObsUploadCallable(final ObsClient obsClient,
            final ObsUploadObject object, final ReportingFactory reportingFactory) {
        this.obsClient = obsClient;
        this.object = object;
        this.reportingFactory = reportingFactory;
    }

    /**
     * Call
     * @throws AbstractCodedException 
     * @throws ObsEmptyFileException 
     */
    @Override
    public Void call() throws AbstractCodedException, ObsEmptyFileException {
    	obsClient.upload(Arrays.asList(object), reportingFactory);
        return null;
    }

}
