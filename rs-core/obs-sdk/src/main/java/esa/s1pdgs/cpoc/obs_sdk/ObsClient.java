package esa.s1pdgs.cpoc.obs_sdk;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.report.ReportingProductFactory;
import esa.s1pdgs.cpoc.report.ReportingFactory;

/**
 * <p>
 * Provides an interface for accessing the S1-PDGS object storage interface
 * </p>
 *
 * @author Viveris Technologies
 */
public interface ObsClient {

    interface Factory {
        ObsClient newObsClient(ObsConfigurationProperties config, ReportingProductFactory factory);
    }

	Logger LOGGER = LogManager.getLogger(ObsClient.class);

    /**
     * @param object
     *            its key is the name of the object that has to be checked and
     *            its family is the name of the family that presumably contains
     *            the object
     * @return result of the search
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws IllegalArgumentException
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
     * @throws IllegalArgumentException
     */
    boolean prefixExists(ObsObject object)
            throws SdkClientException, ObsServiceException;

    /**
     * Gets the list of ObsObject's of an ObsFamily whose modification times in OBS are within the time frame provided.
     *
     * @param timeFrameBegin as {@link Date}
     * @param timeFrameEnd as {@link Date}
     * @return list of ObsObject's, never null
     * @throws SdkClientException
     * @throws ObsServiceException
     * @throws IllegalArgumentException
     */
    List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(ProductFamily obsFamily, Date timeFrameBegin, Date timeFrameEnd)
            throws SdkClientException, ObsServiceException;

    List<File> download(final List<ObsDownloadObject> objects, final ReportingFactory reportingFactory) throws AbstractCodedException;

    void upload(final List<FileObsUploadObject> objects, final ReportingFactory reportingFactory) throws AbstractCodedException, ObsEmptyFileException;

    void uploadStreams(final List<StreamObsUploadObject> objects, final ReportingFactory reportingFactory) throws AbstractCodedException, ObsEmptyFileException;

    void move(final ObsObject from, final ProductFamily to) throws ObsException, ObsServiceException;
    
    void delete(final ObsObject object) throws ObsException, ObsServiceException; 

    Map<String,ObsObject> listInterval(final ProductFamily family, Date intervalStart, Date intervalEnd) throws SdkClientException;

    List<String> list(final ProductFamily family, final String keyPrefix) throws SdkClientException;

    InputStream getAsStream(final ProductFamily family, final String key) throws SdkClientException;

    /**
     * Performing a validation check on the given product. All checksum of the product manifest are verified and it
     * is checked if there are superfluous files stored for directory products.
     * @param object
     * A ObsObject containing family and key of the object that shall be validated
     * @throws ObsServiceException
     * If a consistency issue is found an exception is raised providing the product name it occured
     * and the violation being found
     * @throws ObsValidationException
     * @throws IllegalArgumentException
     */
    void validate(ObsObject object) throws ObsServiceException, ObsValidationException;

    /**
     * Returns the size of the OBS object requested
     * @param object
     * The target OBS object
     * @return
     * The size of the object
     * @throws ObsException
     * Can be thrown if anything with the communication is not working as expected
     * or an attempt is being made to query the size of a directory
     */
    long size(final ObsObject object) throws ObsException;

    /**
     * Returns the eTag md5 checksum of the OBJ object requested
     * @param object
     * The target OBS object
     * @return
     * The MD5 sum of the object
     * @throws ObsException
     * Can be thrown if anything with the communication is not working as expected
     * or an attempt is being made to query the size of a directory
     */
    String getChecksum(final ObsObject object) throws ObsException;
    
    /**
     * Returns the date of the checksum for the obs file of the given representation.
     * 
     * @param object representation of the file in obs or its md5sum file
     * @return the date for the obs file of the given representation
     * @throws ObsException on error
     */
    Instant getChecksumDate(final ObsObject object) throws ObsException;

    /**
     * Sets an expiration time for an {@link ObsObject} to mark an object for automatic deletion after given expiration time
     *
     * @param object         the OBS object to set the expiration time for
     * @param expirationTime expiration time
     */
    void setExpirationTime(final ObsObject object, final Instant expirationTime) throws ObsServiceException;

    ObsObjectMetadata getMetadata(final ObsObject object) throws ObsServiceException;

    URL createTemporaryDownloadUrl(ObsObject object, long expirationTimeInSeconds) throws ObsException, ObsServiceException;
}
