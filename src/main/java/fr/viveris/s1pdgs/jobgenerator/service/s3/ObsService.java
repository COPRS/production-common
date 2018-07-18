package fr.viveris.s1pdgs.jobgenerator.service.s3;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.jobgenerator.exception.ObjectStorageException;
import fr.viveris.s1pdgs.jobgenerator.exception.ObsUnknownObjectException;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

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
	public ObsService(ObsClient client) {
		this.client = client;
	}

	/**
	 * Check if given file exist in OBS
	 * 
	 * @param family
	 * @param key
	 * @return
	 * @throws ObjectStorageException
	 */
	public boolean exist(ProductFamily family, String key) throws ObjectStorageException {
		ObsObject object = new ObsObject(key, getObsFamily(family));
		try {
			return client.doesObjectExist(object);
		} catch (SdkClientException exc) {
			throw new ObjectStorageException(family, key, exc);
		}
	}

	/**
	 * Download a file
	 * 
	 * @param key
	 * @param family
	 * @param targetDir
	 * @return
	 * @throws ObjectStorageException
	 * @throws ObsUnknownObjectException
	 */
	public File downloadFile(ProductFamily family, String key, String targetDir)
			throws ObjectStorageException, ObsUnknownObjectException {
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
				throw new ObsUnknownObjectException(family, key);
			}
		} catch (SdkClientException exc) {
			throw new ObjectStorageException(family, key, exc);
		}
		// Get file
		return new File(targetDir + id);
	}

	/**
	 * Upload a file in object storage
	 * 
	 * @param family
	 * @param key
	 * @param file
	 * @throws ObjectStorageException
	 */
	public void uploadFile(ProductFamily family, String key, File file) throws ObjectStorageException {
		ObsUploadObject object = new ObsUploadObject(key, getObsFamily(family), file);
		try {
			client.uploadObject(object);
		} catch (SdkClientException exc) {
			throw new ObjectStorageException(family, key, exc);
		}
	}

	/**
	 * Get ObsFamily from ProductFamily
	 * 
	 * @param family
	 * @return
	 */
	protected ObsFamily getObsFamily(ProductFamily family) {
		ObsFamily ret;
		switch (family) {
		case AUXILIARY_FILE:
			ret = ObsFamily.AUXILIARY_FILE;
			break;
		case EDRS_SESSION:
			ret = ObsFamily.EDRS_SESSION;
			break;
		case L0_PRODUCT:
			ret = ObsFamily.L0_PRODUCT;
			break;
		case L0_ACN:
			ret = ObsFamily.L0_ACN;
			break;
		case L1_PRODUCT:
			ret = ObsFamily.L1_PRODUCT;
			break;
		case L1_ACN:
			ret = ObsFamily.L1_ACN;
			break;
		default:
			ret = ObsFamily.UNKNOWN;
			break;
		}
		return ret;
	}
}
