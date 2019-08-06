package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

/**
 * <p>
 * Implement of the object storage client using AmazonS3
 * </p>
 * To configure, use the file {@link S3Configuration#CONFIG_FILE}
 * 
 * @author Viveris Technologies
 */
public class S3ObsClient extends AbstractObsClient {

	private static final Logger LOGGER = LogManager.getLogger(S3ObsClient.class);

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
		AmazonS3 client = configuration.defaultS3Client();
		TransferManager manager = configuration.defaultS3TransferManager(client);
		s3Services = new S3ObsServices(client, manager,
				configuration.getIntOfConfiguration("retry-policy.condition.max-retries"),
				configuration.getIntOfConfiguration("retry-policy.backoff.throttled-base-delay-ms"));
	}

	/**
	 * Constructor using fields
	 * 
	 * @param configuration
	 * @param s3Services
	 * @throws ObsServiceException
	 */
	protected S3ObsClient(final S3Configuration configuration, final S3ObsServices s3Services)
			throws ObsServiceException {
		super();
		this.configuration = configuration;
		this.s3Services = s3Services;
	}

	/**
	 * @see ObsClient#doesObjectExist(ObsObject)
	 */
	@Override
	public boolean doesObjectExist(final ObsObject object) throws SdkClientException, ObsServiceException {
		return s3Services.exist(configuration.getBucketForFamily(object.getFamily()), object.getKey());
	}

	/**
	 * @see ObsClient#doesPrefixExist(ObsObject)
	 */
	@Override
	public boolean doesPrefixExist(final ObsObject object) throws SdkClientException, ObsServiceException {
		return s3Services.getNbObjects(configuration.getBucketForFamily(object.getFamily()), object.getKey()) > 0;
	}

	/**
	 * @see ObsClient#downloadObject(ObsDownloadObject)
	 */
	@Override
	public int downloadObject(final ObsDownloadObject object) throws SdkClientException, ObsServiceException {
		return s3Services.downloadObjectsWithPrefix(configuration.getBucketForFamily(object.getFamily()),
				object.getKey(), object.getTargetDir(), object.isIgnoreFolders());
	}

	/**
	 * @see ObsClient#doesObjectExist(ObsObject)
	 */
	@Override
	public int uploadObject(final ObsUploadObject object) throws SdkClientException, ObsServiceException {
		int nbUpload;
		if (object.getFile().isDirectory()) {
			nbUpload = s3Services.uploadDirectory(configuration.getBucketForFamily(object.getFamily()), object.getKey(),
					object.getFile());
		} else {
			s3Services.uploadFile(configuration.getBucketForFamily(object.getFamily()), object.getKey(),
					object.getFile());
			nbUpload = 1;
		}
		return nbUpload;
	}

	/**
	 * @see ObsClient#getShutdownTimeoutS()
	 */
	@Override
	public int getShutdownTimeoutS() throws ObsServiceException {
		return configuration.getIntOfConfiguration(S3Configuration.TM_S_SHUTDOWN);
	}

	/**
	 * @see ObsClient#getDownloadExecutionTimeoutS()
	 */
	@Override
	public int getDownloadExecutionTimeoutS() throws ObsServiceException {
		return configuration.getIntOfConfiguration(S3Configuration.TM_S_DOWN_EXEC);
	}

	/**
	 * @see ObsClient#getUploadExecutionTimeoutS()
	 */
	@Override
	public int getUploadExecutionTimeoutS() throws ObsServiceException {
		return configuration.getIntOfConfiguration(S3Configuration.TM_S_UP_EXEC);
	}

	@Override
	public void moveFile(ProductFamily from, ProductFamily to, String key) throws ObsException {
		try {
			s3Services.moveFile(new CopyObjectRequest(configuration.getBucketForFamily(from), key,
					configuration.getBucketForFamily(to), key));
		} catch (S3SdkClientException | ObsServiceException e) {
			throw new ObsException(from, key, e);
		}
	}

	/**
	 * 
	 */
	@Override
	public List<ObsObject> getListOfObjectsOfTimeFrameOfFamily(final Date timeFrameBegin, final Date timeFrameEnd,
			final ProductFamily family) throws SdkClientException, ObsServiceException {

		long methodStartTime = System.currentTimeMillis();
		List<ObsObject> objectsOfTimeFrame = new ArrayList<>();
		String bucket = configuration.getBucketForFamily(family);
		LOGGER.debug(String.format("listing objects in OBS from bucket %s within last modification time %s to %s"),
				bucket, timeFrameBegin, timeFrameEnd);
		ObjectListing objListing = s3Services.listObjectsFromBucket(bucket);
		boolean truncated = false;

		do {
			if (objListing == null) {
				break;
			}

			List<S3ObjectSummary> objSum = objListing.getObjectSummaries();

			if (objSum == null || objSum.size() == 0) {
				break;
			}

			for (S3ObjectSummary s : objSum) {
				Date lastModified = s.getLastModified();

				if (lastModified.after(timeFrameBegin) && lastModified.before(timeFrameEnd)) {
					ObsObject obsObj = new ObsObject(s.getKey(), family);
					objectsOfTimeFrame.add(obsObj);
				}
			}

			truncated = objListing.isTruncated();
			if (truncated) {
				objListing = s3Services.listNextBatchOfObjectsFromBucket(bucket, objListing);
			}

		} while (truncated);

		float methodDuration = (System.currentTimeMillis() - methodStartTime) / 1000f;
		LOGGER.debug(String.format("Time for OBS listing objects from bucket %s within time frame: %.2fs", bucket,
				methodDuration));

		return objectsOfTimeFrame;
	}

}
