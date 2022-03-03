package esa.s1pdgs.cpoc.obs_sdk;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnrecoverableException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class UnrecoverableErrorAwareObsClient implements ObsClient {

    private final ObsClient obsClient;
    private final UnrecoverableErrorInterceptor errorInterceptor;

    public UnrecoverableErrorAwareObsClient(ObsClient obsClient, UnrecoverableErrorInterceptor errorInterceptor) {
        this.obsClient = obsClient;
        this.errorInterceptor = errorInterceptor;
    }

    @Override
    public boolean exists(ObsObject object) throws SdkClientException {

        return obsClient.exists(object);
    }

    @Override
    public boolean prefixExists(ObsObject object) throws SdkClientException {
        return obsClient.prefixExists(object);
    }

    @Override
    public List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(ProductFamily obsFamily, Date timeFrameBegin, Date timeFrameEnd) throws SdkClientException {
        return obsClient.getObsObjectsOfFamilyWithinTimeFrame(obsFamily, timeFrameBegin, timeFrameEnd);
    }

    @Override
    public List<File> download(List<ObsDownloadObject> objects, ReportingFactory reportingFactory) throws AbstractCodedException {
        return obsClient.download(objects, reportingFactory);
    }

    @Override
    public void upload(List<FileObsUploadObject> objects, ReportingFactory reportingFactory) throws AbstractCodedException, ObsEmptyFileException {

        performObsCall(() -> {
            obsClient.upload(objects, reportingFactory);
            return null;
        });
    }

    @Override
    public void uploadStreams(List<StreamObsUploadObject> objects, ReportingFactory reportingFactory) throws AbstractCodedException, ObsEmptyFileException {
        performObsCall(() -> {
            obsClient.uploadStreams(objects, reportingFactory);
            return null;
        });
    }

    @Override
    public void move(ObsObject from, ProductFamily to) throws ObsException, ObsServiceException {
        obsClient.move(from, to);
    }

    @Override
    public void delete(ObsObject object) throws ObsException, ObsServiceException {
        obsClient.delete(object);
    }

    @Override
    public Map<String, ObsObject> listInterval(ProductFamily family, Date intervalStart, Date intervalEnd) throws SdkClientException {
        return obsClient.listInterval(family, intervalStart, intervalEnd);
    }

    @Override
    public List<String> list(ProductFamily family, String keyPrefix) throws SdkClientException {
        return obsClient.list(family, keyPrefix);
    }

    @Override
    public InputStream getAsStream(ProductFamily family, String key) throws SdkClientException {
        return obsClient.getAsStream(family, key);
    }

    @Override
    public void validate(ObsObject object) throws ObsServiceException, ObsValidationException {
        obsClient.validate(object);
    }

    @Override
    public long size(ObsObject object) throws ObsException {
        return obsClient.size(object);
    }

    @Override
    public String getChecksum(ObsObject object) throws ObsException {
        return obsClient.getChecksum(object);
    }

    @Override
    public Instant getChecksumDate(ObsObject object) throws ObsException {
        return obsClient.getChecksumDate(object);
    }

    @Override
    public void setExpirationTime(ObsObject object, Instant expirationTime) throws ObsServiceException {
        obsClient.setExpirationTime(object, expirationTime);
    }

    @Override
    public ObsObjectMetadata getMetadata(ObsObject object) throws ObsServiceException {
        return obsClient.getMetadata(object);
    }

    @Override
    public URL createTemporaryDownloadUrl(ObsObject object, long expirationTimeInSeconds) throws ObsException, ObsServiceException {
        return obsClient.createTemporaryDownloadUrl(object, expirationTimeInSeconds);
    }

    private <T> T performObsCall(final ObsCall<T> obsCall) throws ObsEmptyFileException, AbstractCodedException {
        try {
            return obsCall.perform();
        } catch (ObsUnrecoverableException e) {
            errorInterceptor.handle(e);
            throw e;
        }
    }

    private interface ObsCall<T> {
        T perform() throws ObsEmptyFileException, AbstractCodedException;
    }

    public interface UnrecoverableErrorInterceptor {
        void handle(ObsUnrecoverableException e) throws ObsUnrecoverableException;
    }
}
