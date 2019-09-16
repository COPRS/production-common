package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
	
	@Override
	protected String getBucketFor(ProductFamily family) throws ObsServiceException {
		return configuration.getBucketForFamily(family);
	}

	/**
	 * @see ObsClient#doesObjectExist(ObsObject)
	 */
	@Override
	public boolean exists(final ObsObject object) throws SdkClientException, ObsServiceException {
		return s3Services.exist(getBucketFor(object.getFamily()), object.getKey());
	}

	/**
	 * @see ObsClient#doesPrefixExist(ObsObject)
	 */
	@Override
	public boolean prefixExists(final ObsObject object) throws SdkClientException, ObsServiceException {
		return s3Services.getNbObjects(getBucketFor(object.getFamily()), object.getKey()) > 0;
	}

	/**
	 * @see ObsClient#downloadObject(ObsDownloadObject)
	 */
	@Override
	public List<File> downloadObject(final ObsDownloadObject object) throws SdkClientException, ObsServiceException {
		final String bucket = getBucketFor(object.getFamily());
		LOGGER.debug("downloadObjectsWithPrefix from bucket {} with prefix {}", bucket, object.getKey());
		final List<File> res = s3Services.downloadObjectsWithPrefix(bucket,
				object.getKey(), object.getTargetDir(), object.isIgnoreFolders());
		LOGGER.debug("downloadObjectsWithPrefix from bucket {} with prefix {} got {} results", bucket, object.getKey(), 
				res.size());
		return res;
	}

	@Override
	public void uploadObject(final ObsUploadObject object) throws SdkClientException, ObsServiceException, ObsException {
		List<String> fileList = new ArrayList<>();
		if (object.getFile().isDirectory()) {
			fileList.addAll(s3Services.uploadDirectory(getBucketFor(object.getFamily()), object.getKey(),
					object.getFile()));
		} else {
			fileList.add(s3Services.uploadFile(getBucketFor(object.getFamily()), object.getKey(), object.getFile()));
		}
		uploadMd5Sum(object, fileList);
	}
	
	private void uploadMd5Sum(final ObsObject object, final List<String> fileList) throws ObsServiceException, S3SdkClientException {
		File file;
		try {
			file = File.createTempFile(object.getKey(), MD5SUM_SUFFIX);
			try(PrintWriter writer = new PrintWriter(file)) {
				for (String fileInfo : fileList) {
					writer.println(fileInfo);
				}
			}
		} catch (IOException e) {
			throw new S3ObsServiceException(configuration.getBucketForFamily(object.getFamily()), object.getKey(), "Could not store md5sum temp file", e);
		}
		s3Services.uploadFile(getBucketFor(object.getFamily()), object.getKey() + MD5SUM_SUFFIX, file);
		
		try {
			Files.delete(file.toPath());
		} catch(IOException e) {
			file.deleteOnExit();
		}
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
	public void move(ObsObject from, ProductFamily to) throws ObsException {
		try {
			s3Services.moveFile(new CopyObjectRequest(getBucketFor(from.getFamily()), from.getKey(),
					getBucketFor(to), from.getKey()));
		} catch (S3SdkClientException | ObsServiceException e) {
			throw new ObsException(from.getFamily(), from.getKey(), e);
		}
	}

	/**
	 * 
	 */
	@Override
	public List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(final ProductFamily family, final Date timeFrameBegin, final Date timeFrameEnd)
			throws SdkClientException, ObsServiceException {

		long methodStartTime = System.currentTimeMillis();
		List<ObsObject> objectsOfTimeFrame = new ArrayList<>();
		String bucket = getBucketFor(family);
		LOGGER.debug(String.format("listing objects in OBS from bucket %s within last modification time %s to %s",
				bucket, timeFrameBegin, timeFrameEnd));
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
				if (s.getKey().endsWith(MD5SUM_SUFFIX)) {
					continue;
				}
				
				Date lastModified = s.getLastModified();

				if (lastModified.after(timeFrameBegin) && lastModified.before(timeFrameEnd)) {
					ObsObject obsObj = new ObsObject(family, s.getKey());
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

	@Override
	public Map<String, InputStream> getAllAsInputStream(ProductFamily family, String keyPrefix) throws SdkClientException {
		final String bucket = getBucketFor(family);
		LOGGER.debug("Getting all files in bucket {} with prefix {}", bucket, keyPrefix);
		final Map<String, InputStream> result = s3Services.getAllAsInputStream(bucket, keyPrefix);
		LOGGER.debug("Found {} elements in bucket {} with prefix {}", result.size(), bucket, keyPrefix);		
		return result;
	}

	@Override
	public Map<String,String> collectMd5Sums(ObsObject object) throws ObsException {
		try {
			return s3Services.collectMd5Sums(getBucketFor(object.getFamily()), object.getKey());
		} catch (S3SdkClientException | ObsServiceException e) {
			throw new ObsException(object.getFamily(), object.getKey(), e);
		}
	}
}
