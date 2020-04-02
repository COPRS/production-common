package esa.s1pdgs.cpoc.obs_sdk;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class ObsUploadStreamCallable implements Callable<Void> {

    /**
     * OBS client
     */
    private final ObsClient obsClient;

    /**
     * Objects to upload
     */
    private final StreamObsUploadObject object;

    private final ReportingFactory reportingFactory;

    /**
     * Constructor
     *  @param obsClient
     * @param object
     */
    public ObsUploadStreamCallable(final ObsClient obsClient,
                                   final StreamObsUploadObject object, final ReportingFactory reportingFactory) {
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
        obsClient.uploadStreams(Arrays.asList(object), reportingFactory);
        return null;
    }

}
