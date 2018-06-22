package fr.viveris.s1pdgs.libs.obs_sdk.s3;

import fr.viveris.s1pdgs.libs.obs_sdk.AbstractObsClient;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsClient;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsDownloadObject;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsObject;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsServiceException;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsUploadObject;
import fr.viveris.s1pdgs.libs.obs_sdk.SdkClientException;

/**
 * <p>
 * Implement of the object storage client using AmazonS3
 * </p>
 * To configure, use the file {@link S3Configuration#CONFIG_FILE}
 * 
 * @author Viveris Technologies
 */
public class S3ObsClient extends AbstractObsClient {

    /**
     * Configuration
     */
    protected final S3Configuration configuration;

    /**
     * Amazon S3 client
     */
    protected final S3ObsServices s3Services;

    /**
     * Default constructor
     * 
     * @throws ObsServiceException
     */
    public S3ObsClient() throws ObsServiceException {
        super();
        configuration = new S3Configuration();
        s3Services = new S3ObsServices(configuration.defaultS3Client());
    }

    /**
     * Constructor using fields
     * 
     * @param configuration
     * @param s3Services
     * @throws ObsServiceException
     */
    protected S3ObsClient(final S3Configuration configuration,
            final S3ObsServices s3Services) throws ObsServiceException {
        super();
        this.configuration = configuration;
        this.s3Services = s3Services;
    }

    /**
     * @see ObsClient#doesObjectExist(ObsObject)
     */
    @Override
    public boolean doesObjectExist(final ObsObject object)
            throws SdkClientException, ObsServiceException {
        return s3Services.exist(
                configuration.getBucketForFamily(object.getFamily()),
                object.getKey());
    }

    /**
     * @see ObsClient#doesPrefixExist(ObsObject)
     */
    @Override
    public boolean doesPrefixExist(final ObsObject object)
            throws SdkClientException, ObsServiceException {
        return s3Services.getNbObjects(
                configuration.getBucketForFamily(object.getFamily()),
                object.getKey()) > 0 ? true : false;
    }

    /**
     * @see ObsClient#downloadObject(ObsDownloadObject)
     */
    @Override
    public int downloadObject(final ObsDownloadObject object)
            throws SdkClientException, ObsServiceException {
        return s3Services.downloadObjectsWithPrefix(
                configuration.getBucketForFamily(object.getFamily()),
                object.getKey(), object.getTargetDir(),
                object.isIgnoreFolders());
    }

    /**
     * @see ObsClient#doesObjectExist(ObsObject)
     */
    @Override
    public int uploadObject(final ObsUploadObject object)
            throws SdkClientException, ObsServiceException {
        int nbUpload;
        if (object.getFile().isDirectory()) {
            nbUpload = s3Services.uploadDirectory(
                    configuration.getBucketForFamily(object.getFamily()),
                    object.getKey(), object.getFile());
        } else {
            s3Services.uploadFile(
                    configuration.getBucketForFamily(object.getFamily()),
                    object.getKey(), object.getFile());
            nbUpload = 1;
        }
        return nbUpload;
    }

    /**
     * @see ObsClient#getShutdownTimeoutS()
     */
    @Override
    public int getShutdownTimeoutS() throws ObsServiceException {
        return configuration
                .getIntOfConfiguration(S3Configuration.TM_S_SHUTDOWN);
    }

    /**
     * @see ObsClient#getDownloadExecutionTimeoutS()
     */
    @Override
    public int getDownloadExecutionTimeoutS() throws ObsServiceException {
        return configuration
                .getIntOfConfiguration(S3Configuration.TM_S_DOWN_EXEC);
    }

    /**
     * @see ObsClient#getUploadExecutionTimeoutS()
     */
    @Override
    public int getUploadExecutionTimeoutS() throws ObsServiceException {
        return configuration
                .getIntOfConfiguration(S3Configuration.TM_S_UP_EXEC);
    }

}
