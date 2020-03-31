package standalone.prip.frontend.obs;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.FileObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.StreamObsUploadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

// dummy impl - doin nothin
public class FakeObsClient implements ObsClient {	
	@Override
	public boolean exists(final ObsObject object) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void uploadStreams(final List<StreamObsUploadObject> objects, final ReportingFactory reportingFactory)
			throws AbstractCodedException, ObsEmptyFileException {
		// TODO Auto-generated method stub		
	}

	@Override
	public boolean prefixExists(final ObsObject object) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<File> download(final List<ObsDownloadObject> objects, final ReportingFactory reportingFactory) throws AbstractCodedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void upload(final List<FileObsUploadObject> objects, final ReportingFactory reportingFactory) throws AbstractCodedException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(final ProductFamily obsFamily,
			final Date timeFrameBegin, final Date timeFrameEnd) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void move(final ObsObject from, final ProductFamily to) throws ObsException {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, ObsObject> listInterval(final ProductFamily family, final Date intervalStart, final Date intervalEnd)
			throws SdkClientException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, InputStream> getAllAsInputStream(final ProductFamily family, final String keyPrefix)
			throws SdkClientException {
		return null;
	}
	
	@Override
	public void validate(final ObsObject object) throws ObsServiceException {
	    // TODO Auto-generated method stub

	}

	@Override
	public long size(final ObsObject object) throws ObsException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getChecksum(final ObsObject object) throws ObsException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public URL createTemporaryDownloadUrl(final ObsObject object, final long expirationTimeInSeconds)
			throws ObsException, ObsServiceException {
		URL url = null;
		try {
			url = new URL("http://www.example.org");
		} catch (final MalformedURLException e) {
		}
		return url;
	}	

}
