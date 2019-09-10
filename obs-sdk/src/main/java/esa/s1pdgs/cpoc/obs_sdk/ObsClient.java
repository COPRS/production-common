package esa.s1pdgs.cpoc.obs_sdk;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;

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
    boolean exists(ObsObject object)
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
    boolean prefixExists(ObsObject object)
            throws SdkClientException, ObsServiceException;

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
	List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(ProductFamily obsFamily, Date timeFrameBegin, Date timeFrameEnd)
			throws SdkClientException, ObsServiceException;
	
	List<File> download(final List<ObsDownloadObject> objects) throws AbstractCodedException;
	
	void upload(final List<ObsUploadObject> objects) throws AbstractCodedException;
	
	void move(final ObsObject from, final ProductFamily to) throws ObsException;

	Map<String,ObsObject> listInterval(final ProductFamily family, Date intervalStart, Date intervalEnd) throws SdkClientException;
	
    Map<String, InputStream> getAllAsInputStream(final ProductFamily family, final String keyPrefix) throws SdkClientException;

    /**
     * Performing a validation check on the given product. All checksum of the product manifest are verified and it
     * is checked if there are superfluous files stored for directory products.
     * @param object
     * A ObsObject containing family and key of the object that shall be validated
     * @throws ObsServiceException
     * If a consistency issue is found an exception is raised providing the product name it occured
     * and the violation being found
     * @throws ObsValidationException 
     */
    void validate(ObsObject object) throws ObsServiceException, ObsValidationException;
}
