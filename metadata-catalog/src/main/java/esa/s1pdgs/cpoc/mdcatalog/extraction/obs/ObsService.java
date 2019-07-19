package esa.s1pdgs.cpoc.mdcatalog.extraction.obs;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsParallelAccessException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3DownloadFile;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3UploadFile;

/**
 * Service for accessing to the OBS
 * 
 * @author Viveris Technologies
 * 
 */
@Service
public class ObsService {

	/**
	 * OBS client
	 */
	private final ObsClient client;

	/**
	 * Constructor
	 * 
	 * @param client
	 */
	@Autowired
    public ObsService(final ObsClient client) {
        this.client = client;
    }

    /**
     * Check if given file exist in OBS
     * 
     * @param family
     * @param key
     * @return
     * @throws ObsException
     */
	public boolean exist(final ProductFamily family, final String key)
            throws ObsException {
        final ObsObject object = new ObsObject(key, getObsFamily(family));
        try {
            return client.doesObjectExist(object);
        } catch (SdkClientException exc) {
            throw new ObsException(family, key, exc);
        }
    }

	/**
	 * Download a file
	 * 
	 * @param key
	 * @param family
	 * @param targetDir
	 * @return
	 * @throws ObsException
	 * @throws ObsUnknownObject
	 */
	public File downloadFile(final ProductFamily family, final String key, final String targetDir)
			throws ObsException, ObsUnknownObject {
		// If case of session we ignore folder in the key
		String id = key;
		if (family == ProductFamily.EDRS_SESSION) {
			int lastIndex = key.lastIndexOf('/');
			if (lastIndex != -1 && lastIndex < key.length() - 1) {
				id = key.substring(lastIndex + 1);
			}
		}
		// Download object
		ObsDownloadObject object = new ObsDownloadObject(key, getObsFamily(family), targetDir);
		try {
			int nbObjects = client.downloadObject(object);
			if (nbObjects <= 0) {
				throw new ObsUnknownObject(family, key);
			}
		} catch (SdkClientException exc) {
			throw new ObsException(family, key, exc);
		}
		// Get file
		return new File(targetDir + id);
	}
	
	/**
     * Download files per batch
     * 
     * @param filesToDownload
     * @throws AbstractCodedException
     */
    public void downloadFilesPerBatch(
            final List<S3DownloadFile> filesToDownload)
            throws AbstractCodedException {
        // Build objects
        List<ObsDownloadObject> objects = filesToDownload.stream()
                .map(file -> new ObsDownloadObject(file.getKey(),
                        getObsFamily(file.getFamily()), file.getTargetDir()))
                .collect(Collectors.toList());
        // Download
        try {
            client.downloadObjects(objects, true);
        } catch (SdkClientException exc) {
            throw new ObsParallelAccessException(exc);
        }
    }
    
	/**
	 * Upload a file in object storage
	 * 
	 * @param family
	 * @param key
	 * @param file
	 * @throws ObsException
	 */
	public void uploadFile(final ProductFamily family, final String key, final File file) throws ObsException {
		ObsUploadObject object = new ObsUploadObject(key, getObsFamily(family), file);
		try {
			client.uploadObject(object);
		} catch (SdkClientException exc) {
			throw new ObsException(family, key, exc);
		}
	}
	
	/**
     * Upload files per batch
     * 
     * @param filesToUpload
     * @throws AbstractCodedException
     */
    public void uploadFilesPerBatch(final List<S3UploadFile> filesToUpload)
            throws AbstractCodedException {

        // Build objects
        List<ObsUploadObject> objects = filesToUpload.stream()
                .map(file -> new ObsUploadObject(file.getKey(),
                        getObsFamily(file.getFamily()), file.getFile()))
                .collect(Collectors.toList());
        // Upload
        try {
            client.uploadObjects(objects, true);
        } catch (SdkClientException exc) {
            throw new ObsParallelAccessException(exc);
        }
    }

    public Map<String,ObsObject> listInterval(final ProductFamily family, Date intervalStart, Date intervalEnd) throws SdkClientException {
    	ObsFamily obsFamily = getObsFamily(family);
    	
    	List<ObsObject> results = client.getListOfObjectsOfTimeFrameOfFamily(intervalStart, intervalEnd, obsFamily);
    	Map<String, ObsObject> map = results.stream()
    		      .collect(Collectors.toMap(ObsObject::getKey, obsObject -> obsObject));
    	    	
    	return map;
    }

    /**
     * Get ObsFamily from ProductFamily
     * 
     * @param family
     * @return
     */
    protected ObsFamily getObsFamily(final ProductFamily family) {
        ObsFamily ret;
        switch (family) {
            case AUXILIARY_FILE:
                ret = ObsFamily.AUXILIARY_FILE;
                break;
            case EDRS_SESSION:
                ret = ObsFamily.EDRS_SESSION;
                break;
            case L0_SLICE:
                ret = ObsFamily.L0_SLICE;
                break;
            case L0_SEGMENT:
                ret = ObsFamily.L0_SEGMENT;
                break;
            case L0_BLANK:
                ret = ObsFamily.L0_BLANK;
                break;
            case L0_ACN:
                ret = ObsFamily.L0_ACN;
                break;
            case L1_SLICE:
                ret = ObsFamily.L1_SLICE;
                break;
            case L1_ACN:
                ret = ObsFamily.L1_ACN;
                break;
            case L2_SLICE:
            	ret = ObsFamily.L2_SLICE;
            	break;
            case L2_ACN:
            	ret = ObsFamily.L2_ACN;
            	break;
            	
            // COMPRESSED PRODUCTS
            case AUXILIARY_FILE_ZIP:
                ret = ObsFamily.AUXILIARY_FILE_ZIP;
                break;
            case L0_SLICE_ZIP:
                ret = ObsFamily.L0_SLICE_ZIP;
                break;
            case L0_SEGMENT_ZIP:
                ret = ObsFamily.L0_SEGMENT_ZIP;
                break;
            case L0_BLANK_ZIP:
                ret = ObsFamily.L0_BLANK_ZIP;
                break;
            case L0_ACN_ZIP:
                ret = ObsFamily.L0_ACN_ZIP;
                break;
            case L1_SLICE_ZIP:
                ret = ObsFamily.L1_SLICE_ZIP;
                break;
            case L1_ACN_ZIP:
                ret = ObsFamily.L1_ACN_ZIP;
                break;
            case L2_SLICE_ZIP:
            	ret = ObsFamily.L2_SLICE_ZIP;
            	break;
            case L2_ACN_ZIP:
            	ret = ObsFamily.L2_ACN_ZIP;
            	break;
            default:
                ret = ObsFamily.UNKNOWN;
                break;
        }
        return ret;
    }
}
