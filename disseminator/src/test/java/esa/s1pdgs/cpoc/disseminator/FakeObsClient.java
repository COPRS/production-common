package esa.s1pdgs.cpoc.disseminator;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;

// dummy impl - doin nothin
public abstract class FakeObsClient implements ObsClient {	
	@Override
	public boolean exists(ObsObject object) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean prefixExists(ObsObject object) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<File> download(final List<ObsDownloadObject> objects, Reporting.ChildFactory reportingChildFactory) throws AbstractCodedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void upload(final List<ObsUploadObject> objects, Reporting.ChildFactory reportingChildFactory) throws AbstractCodedException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(ProductFamily obsFamily,
			Date timeFrameBegin, Date timeFrameEnd) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void move(ObsObject from, ProductFamily to) throws ObsException {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, ObsObject> listInterval(ProductFamily family, Date intervalStart, Date intervalEnd, Reporting.ChildFactory reportingChildFactory)
			throws SdkClientException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, InputStream> getAllAsInputStream(ProductFamily family, String keyPrefix, Reporting.ChildFactory reportingChildFactory)
			throws SdkClientException {
		return null;
	}
	
	@Override
	public void validate(ObsObject object, Reporting.ChildFactory reportingChildFactory) throws ObsServiceException {
	    // TODO Auto-generated method stub

	}

	@Override
	public long size(ObsObject object) throws ObsException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getChecksum(ObsObject object) throws ObsException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public URL createTemporaryDownloadUrl(ObsObject object, long expirationTimeInSeconds)
			throws ObsException, ObsServiceException {
		// TODO Auto-generated method stub
		return null;
	}	
	

}
