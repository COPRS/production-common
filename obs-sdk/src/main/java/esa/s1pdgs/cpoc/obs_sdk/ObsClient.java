package esa.s1pdgs.cpoc.obs_sdk;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnknownObject;

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
    
	/**
	 * Gets the list of ObsObject's of an ObsFamily whose modification times in OBS are within the time frame provided.
	 * 
	 * @param timeFrameBegin as {@link Date}
	 * @param timeFrameEnd as {@link Date}
	 * @param obsFamily as {@link ObsFamily}
	 * @return list of ObsObject's, never null
	 * @throws SdkClientException
	 * @throws ObsServiceException
	 */
	List<ObsObject> getListOfObjectsOfTimeFrameOfFamily(Date timeFrameBegin, Date timeFrameEnd, ProductFamily obsFamily)
			throws SdkClientException, ObsServiceException;
	
	boolean exist(final ProductFamily family, final String key) throws ObsException;
	
	File downloadFile(final ProductFamily family, final String key, final String targetDir) throws ObsException, ObsUnknownObject;
	
	void downloadFilesPerBatch(final List<ObsDownloadFile> filesToDownload) throws AbstractCodedException; // TODO: Rename S3DownloadFile to be generic
	
	void uploadFile(final ProductFamily family, final String key, final File file) throws ObsException;
	
	void moveFile(final ProductFamily from, final ProductFamily to,final File file) throws ObsException;
	
	void uploadFilesPerBatch(final List<ObsUploadFile> filesToUpload)  throws AbstractCodedException; // TODO: Rename S3DownloadFile to be generic
	
	Map<String,ObsObject> listInterval(final ProductFamily family, Date intervalStart, Date intervalEnd) throws SdkClientException;
}
