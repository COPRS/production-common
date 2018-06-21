package fr.viveris.s1pdgs.level0.wrapper.services.s3;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.level0.wrapper.model.ProductFamily;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.AbstractCodedException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObsParallelAccessException;
import fr.viveris.s1pdgs.level0.wrapper.model.exception.ObsUnknownObjectException;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3DownloadFile;
import fr.viveris.s1pdgs.level0.wrapper.model.s3.S3UploadFile;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsClient;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsDownloadObject;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsFamily;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsObject;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsUploadObject;
import fr.viveris.s1pdgs.libs.obs_sdk.SdkClientException;

/**
 * Service for accessing to the OBS
 * 
 * @author Viveris Technologies
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
     * @throws ObjectStorageException
     */
    public boolean exist(final ProductFamily family, final String key)
            throws ObjectStorageException {
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
    public File downloadFile(final ProductFamily family, final String key,
            final String targetDir)
            throws ObjectStorageException, ObsUnknownObjectException {
        // If case of session we ignore folder in the key
        String id = key;
        if (family == ProductFamily.RAW) {
            int lastIndex = key.lastIndexOf('/');
            if (lastIndex != -1 && lastIndex < key.length() - 1) {
                id = key.substring(lastIndex + 1);
            }
        }
        // Download object
        ObsDownloadObject object =
                new ObsDownloadObject(key, getObsFamily(family), targetDir);
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
     * @throws ObjectStorageException
     */
    public void uploadFile(final ProductFamily family, final String key,
            final File file) throws ObjectStorageException {
        ObsUploadObject object =
                new ObsUploadObject(key, getObsFamily(family), file);
        try {
            client.uploadObject(object);
        } catch (SdkClientException exc) {
            throw new ObjectStorageException(family, key, exc);
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

    /**
     * Get ObsFamily from ProductFamily
     * 
     * @param family
     * @return
     */
    protected ObsFamily getObsFamily(final ProductFamily family) {
        ObsFamily ret;
        switch (family) {
            case CONFIG:
                ret = ObsFamily.AUXILIARY_FILE;
                break;
            case RAW:
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
