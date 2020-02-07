package esa.s1pdgs.cpoc.obs_sdk;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

/**
 * Callable to download a file / folder from the OBS
 * 
 * @author Viveris Technologies
 */
public class ObsDownloadCallable implements Callable<List<File>> {

    /**
     * OBS client
     */
    private final ObsClient obsClient;

    /**
     * Objects to download
     */
    private final ObsDownloadObject object;
    
    private final ReportingFactory reportingFactory;
    
    /**
     * Default constructor
     * 
     * @param obsClient
     * @param object
     */
    public ObsDownloadCallable(final ObsClient obsClient,
            final ObsDownloadObject object, final ReportingFactory reportingFactory) {
        this.obsClient = obsClient;
        this.object = object;
        this.reportingFactory = reportingFactory;
    }

    /**
     * Call
     * @throws AbstractCodedException 
     */
    @Override
    public List<File> call() throws ObsServiceException, AbstractCodedException {
        final List<File> files = obsClient.download(Arrays.asList(object), reportingFactory);
		if (files.size() <= 0) {
			throw new ObsServiceException(
					String.format("Unknown object %s with family %s", object.getKey(), object.getFamily()));
		}
		return files;
    }

}
