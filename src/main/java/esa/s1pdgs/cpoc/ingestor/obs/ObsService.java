package esa.s1pdgs.cpoc.ingestor.obs;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.ingestor.exceptions.ObjectStorageException;
import esa.s1pdgs.cpoc.ingestor.files.model.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
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
	 * @param client
	 */
	@Autowired
	public ObsService(ObsClient client) {
		this.client = client;
	}

	/**
	 * Check if given file exist in OBS
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
			throw new ObjectStorageException(key, key, family, exc);
		}
	}

	/**
	 * Upload a file in object storage
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
			throw new ObjectStorageException(key, key, family, exc);
		}
	}

	/**
	 * Get ObsFamily from ProductFamily
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
		default:
			ret = ObsFamily.UNKNOWN;
			break;
		}
		return ret;
	}
}
