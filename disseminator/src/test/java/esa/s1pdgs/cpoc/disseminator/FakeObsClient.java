package esa.s1pdgs.cpoc.disseminator;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadFile;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadFile;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

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
	public int downloadObject(ObsDownloadObject object) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void downloadObjects(List<ObsDownloadObject> objects) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadObjects(List<ObsDownloadObject> objects, boolean parralel)
			throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public int uploadObject(ObsUploadObject object) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void uploadObjects(List<ObsUploadObject> objects) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadObjects(List<ObsUploadObject> objects, boolean parralel)
			throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getShutdownTimeoutS() throws ObsServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDownloadExecutionTimeoutS() throws ObsServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getUploadExecutionTimeoutS() throws ObsServiceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<ObsObject> getListOfObjectsOfTimeFrameOfFamily(Date timeFrameBegin, Date timeFrameEnd,
			ProductFamily obsFamily) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File downloadFile(ProductFamily family, String key, String targetDir) throws ObsException, ObsUnknownObject {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void downloadFilesPerBatch(List<ObsDownloadFile> filesToDownload) throws AbstractCodedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadFile(ProductFamily family, String key, File file) throws ObsException {
		// TODO Auto-generated method stub

	}

	@Override
	public void move(ObsObject from, ProductFamily to) throws ObsException {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadFilesPerBatch(List<ObsUploadFile> filesToUpload) throws AbstractCodedException {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, ObsObject> listInterval(ProductFamily family, Date intervalStart, Date intervalEnd)
			throws SdkClientException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, InputStream> getAllAsInputStream(ProductFamily family, String keyPrefix)
			throws SdkClientException {
		return null;
	}

}
