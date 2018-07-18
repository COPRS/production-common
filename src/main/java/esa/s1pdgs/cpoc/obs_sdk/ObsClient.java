package esa.s1pdgs.cpoc.obs_sdk;

import java.util.List;

/**
 * <p>
 * Provides an interface for accessing the S1-PDGS object storage interface
 * </p>
 * 
 * @author Viveris Technologies
 */
public interface ObsClient {

    /**
     * @param object
     *            its key is the name of the object that has to be checked and
     *            its family is the name of the family that presumably contains
     *            the object
     * @return result of the search
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    boolean doesObjectExist(ObsObject object)
            throws SdkClientException, ObsServiceException;

    /**
     * @param object
     *            its key is the prefix of the objects that has to be checked
     *            and its family is the name of the family that presumably
     *            contains the objects
     * @return
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    boolean doesPrefixExist(ObsObject object)
            throws SdkClientException, ObsServiceException;

    /**
     * <p>
     * Download objects with a given prefix from the OBS in a local directory
     * </p>
     * If only one object matches with the prefix, the object is download in a
     * local file. Else the objects are download in a directory named by the
     * prefix
     * 
     * @param object
     *            its key is the prefix of the objects that have to be download
     *            its family is the name of the family that contains the object
     *            its targetDir is the directory on which the match objects will
     *            be download download its ignoreFolders indicates if the
     *            folders in the objects keys shall be ignored or not
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    int downloadObject(ObsDownloadObject object)
            throws SdkClientException, ObsServiceException;

    /**
     * <p>
     * Download sequentially a list of objects.
     * </p>
     * 
     * @param objects
     * @throws SdkClientException
     * @throws ObsServiceException
     * @see ObsClient#downloadFiles(List, int)
     */
    void downloadObjects(List<ObsDownloadObject> objects)
            throws SdkClientException, ObsServiceException;

    /**
     * <p>
     * Download in parallel a list of objects.
     * </p>
     * 
     * @param objects
     * @param parralel
     *            true launch downloads in parallel
     * @throws SdkClientException
     * @throws ObsServiceException
     * @see ObsClient#downloadFiles(List, int)
     */
    void downloadObjects(List<ObsDownloadObject> objects, boolean parralel)
            throws SdkClientException, ObsServiceException;

    /**
     * @param object
     *            its key is the key of the file / directory that have to be
     *            upload its family is the name of the family that contains the
     *            object its file is the file / directory to upload in OBS
     * @return the number of upload objects
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    int uploadObject(ObsUploadObject object)
            throws SdkClientException, ObsServiceException;

    /**
     * @param objects
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    void uploadObjects(List<ObsUploadObject> objects)
            throws SdkClientException, ObsServiceException;

    /**
     * @param objects
     * @param parralel
     *            true launch downloads in parallel
     * @throws SdkClientException
     * @throws ObsServiceException
     */
    void uploadObjects(List<ObsUploadObject> objects, boolean parralel)
            throws SdkClientException, ObsServiceException;

    /**
     * Get the timeout for waiting threads termination in seconds
     * @return
     * @throws ObsServiceException
     */
    int getShutdownTimeoutS() throws ObsServiceException;

    /**
     * Get the timeout for download execution in seconds
     * @return
     * @throws ObsServiceException
     */
    int getDownloadExecutionTimeoutS() throws ObsServiceException;

    /**
     * Get the timeout for upload execution in seconds
     * @return
     * @throws ObsServiceException
     */
    int getUploadExecutionTimeoutS() throws ObsServiceException;

}
