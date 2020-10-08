package esa.s1pdgs.cpoc.disseminator;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.FileObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObjectMetadata;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.StreamObsUploadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

// dummy impl - doing nothing
public abstract class FakeObsClient implements ObsClient {	
	@Override
	public boolean exists(final ObsObject object) throws SdkClientException {
		return false;
	}
	
	@Override
	public void uploadStreams(final List<StreamObsUploadObject> objects, final ReportingFactory reportingFactory) {
	}

	@Override
	public boolean prefixExists(final ObsObject object) throws SdkClientException {
		return false;
	}

	@Override
	public List<File> download(final List<ObsDownloadObject> objects, final ReportingFactory reportingFactory) {
		return null;
	}

	@Override
	public void upload(final List<FileObsUploadObject> objects, final ReportingFactory reportingFactory) {
	}

	@Override
	public List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(final ProductFamily obsFamily,
			final Date timeFrameBegin, final Date timeFrameEnd) {
		return null;
	}

	@Override
	public void move(final ObsObject from, final ProductFamily to) {

	}

	@Override
	public Map<String, ObsObject> listInterval(final ProductFamily family, final Date intervalStart, final Date intervalEnd) {
		return null;
	}

	@Override
	public void validate(final ObsObject object) {

	}

	@Override
	public long size(final ObsObject object) {
		return 0;
	}

	@Override
	public String getChecksum(final ObsObject object) {
		return null;
	}

	@Override
	public void setExpirationTime(ObsObject object, Instant expirationTime) {
	}

	@Override
	public ObsObjectMetadata getMetadata(ObsObject object) {
		return null;
	}

	@Override
	public URL createTemporaryDownloadUrl(final ObsObject object, final long expirationTimeInSeconds) {
		return null;
	}

	@Override
	public InputStream getAsStream(ProductFamily family, String key) {
		return null;
	}

	@Override
	public List<String> list(ProductFamily family, String keyPrefix) {
		return null;
	}
}
