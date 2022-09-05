package esa.s1pdgs.cpoc.obs_sdk.s3;

import static java.lang.String.format;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.FileObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.Md5;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObjectMetadata;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.StreamObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.ValidArgumentAssertion;
import esa.s1pdgs.cpoc.obs_sdk.report.ReportingProductFactory;

/**
 * <p>
 * Implement of the object storage client using AmazonS3
 * </p>
 *
 * @author Viveris Technologies
 */
public class S3ObsClient extends AbstractObsClient {

	public static final int ADDITIONAL_BUFFER = 1024;

	public static final class Factory implements ObsClient.Factory {
		
		private static final Logger LOGGER = LogManager.getLogger(Factory.class);
		
		@Override
		public final ObsClient newObsClient(final ObsConfigurationProperties config, final ReportingProductFactory factory) {
			final BasicAWSCredentials awsCreds = new BasicAWSCredentials(config.getUserId(), config.getUserSecret());
			final ClientConfiguration clientConfig = new ClientConfiguration();
			clientConfig.setProtocol(Protocol.HTTP);

			// set proxy if defined in environmental
			final String proxyConfig = System.getenv("https_proxy");

			if (proxyConfig != null && !proxyConfig.equals("")) {
				final String removedProtocol = proxyConfig.replaceAll(Pattern.quote("http://"), "")
						.replaceAll(Pattern.quote("https://"), "").replaceAll(Pattern.quote("/"), ""); // remove
																										// trailing
																										// slash

				final String host = removedProtocol.substring(0, removedProtocol.indexOf(':'));
				final int port = Integer.parseInt(
						removedProtocol.substring(removedProtocol.indexOf(':') + 1));
				clientConfig.setProxyHost(host);
				clientConfig.setProxyPort(port);
			}

			final AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard()
					.withClientConfiguration(clientConfig)
					.withEndpointConfiguration(
							new EndpointConfiguration(config.getEndpoint(), config.getEndpointRegion()))
					.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
					.withPathStyleAccessEnabled(true);
			
			LOGGER.info("Disable chunked encoding: {}", config.getDisableChunkedEncoding());
			if (config.getDisableChunkedEncoding()) {
				clientBuilder.disableChunkedEncoding();
			}
			final AmazonS3 client = clientBuilder.build();

			final long minimumUploadPartSize = config.getMinUploadPartSize() * 1024 * 1024;
			final long multipartUploadThreshold = config.getMultipartUploadThreshold() * 1024 * 1024;

			final TransferManager manager = TransferManagerBuilder.standard()
					.withMinimumUploadPartSize(minimumUploadPartSize)
					.withMultipartUploadThreshold(multipartUploadThreshold)
					.withS3Client(client)
					.build();

			LOGGER.info(
					"created transferManager with minimumUploadPartSize: {} multipartUploadThreshold: {}",
					minimumUploadPartSize,
					multipartUploadThreshold);

			final int maxObsRetries = config.getMaxObsRetries();
			final int backoffThrottledBaseDelay = config.getBackoffThrottledBaseDelay();
			final Path uploadCacheLocation = config.getUploadCacheLocation();

			final S3ObsServices s3Services = new S3ObsServices(
					client,
					manager,
					maxObsRetries,
					backoffThrottledBaseDelay,
					uploadCacheLocation);

			LOGGER.info(
					"created s3ObsServices with maxRetries: {} retriesDelay: {} uploadCacheLocation: {}",
					maxObsRetries,
					backoffThrottledBaseDelay,
					uploadCacheLocation);
			
			return new S3ObsClient(config, s3Services, factory);
		}
	}

	public static final String BACKEND_NAME = "aws-s3";

	final S3ObsServices s3Services;

	S3ObsClient(final ObsConfigurationProperties configuration, final S3ObsServices s3Services, final ReportingProductFactory factory) {
		super(configuration, factory);
		this.s3Services = s3Services;
	}
	
	public boolean bucketExists(final ProductFamily family) throws ObsServiceException, S3SdkClientException {
		return s3Services.bucketExist(getBucketFor(family));
	}

	@Override
	public boolean exists(final ObsObject object) throws SdkClientException {
		ValidArgumentAssertion.assertValidArgument(object);
		return s3Services.exist(getBucketFor(object.getFamily()), object.getKey());
	}
	
	@Override
	public boolean existsWithSameSize(ObsObject obsObject, long size) throws SdkClientException, ObsException {

		
		LOGGER.debug("checking if OBS object {} exists and has the size {}", obsObject, size);
		long totalSize = -1;

		if (this.exists(obsObject)) {
			totalSize = this.size(obsObject);
			LOGGER.debug("OBS object {} is a file and exists with size {}", obsObject, totalSize);

		} else if (this.prefixExists(obsObject)) {

			List<String> list = this.list(obsObject.getFamily(), obsObject.getKey());
			totalSize = 0;
			for (String key : list) {
				totalSize += this.size(new ObsObject(obsObject.getFamily(), key));
			}
			LOGGER.debug("OBS object {} is a directory and exists with size {}", obsObject, totalSize);
		}

		if (totalSize < 0 || totalSize != size) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean prefixExists(final ObsObject object) throws SdkClientException {
		ValidArgumentAssertion.assertValidArgument(object);
		return s3Services.getNbObjects(getBucketFor(object.getFamily()), object.getKey()) > 0;
	}

	@Override
	public List<File> downloadObject(final ObsDownloadObject object) throws SdkClientException {
		final String bucket = getBucketFor(object.getFamily());
		LOGGER.debug("downloadObjectsWithPrefix from bucket {} with prefix {}", bucket, object.getKey());
		final List<File> res = s3Services.downloadObjectsWithPrefix(bucket, object.getKey(), object.getTargetDir(),
				object.isIgnoreFolders());
		LOGGER.debug("downloadObjectsWithPrefix from bucket {} with prefix {} got {} results", bucket, object.getKey(),
				res.size());
		return res;
	}

	@Override
	public void uploadObject(final FileObsUploadObject object)
			throws SdkClientException, ObsException {
		if (object.getFile().isDirectory()) {
			uploadMd5Sum(object, s3Services.uploadDirectory(getBucketFor(object.getFamily()), object.getKey(), object.getFile()));
		} else {
			uploadMd5Sum(object, Collections.singletonList(s3Services.uploadFile(getBucketFor(object.getFamily()), object.getKey(), object.getFile())));
		}
	}

	/**
	 *
	 * Note: The stream is not closed here. It should be closed after upload.
	 *
	 */
	@Override
	public Md5.Entry uploadObject(final StreamObsUploadObject object) throws ObsServiceException, S3SdkClientException {
		return s3Services.uploadStream(getBucketFor(object.getFamily()), object.getKey(), maybeWithBuffer(object));
	}

	/**
	 * If chunked encoding is not allowed the whole content has to be buffered in order to read the stream twice to calculate content hash
	 * See S1PRO-1441 (S1SYS-724)
	 * The awsClient however is able to handle this with {@link FileInputStream} input
	 */
	private InputStream maybeWithBuffer(final StreamObsUploadObject object) throws ObsServiceException {
		if (getConfiguration().getDisableChunkedEncoding() && 
				!(object.getInput() instanceof FileInputStream) && // don't apply if product is in local filesystem
				!(object.getInput() instanceof ByteArrayInputStream) // don't cache twice
		) {
			if (object.getContentLength() > getConfiguration().getMaxInputStreamBufferSize()) {
				throw new S3ObsServiceException(getBucketFor(object.getFamily()),
						object.getKey(),
						format("Actual content length %s is greater than max allowed input stream buffer size %s",
								object.getContentLength(),
								getConfiguration().getMaxInputStreamBufferSize()));
			}
			return new BufferedInputStream(object.getInput(), (int) object.getContentLength() + ADDITIONAL_BUFFER);
		}
		return object.getInput();
	}

	@Override
	public final void uploadMd5Sum(final ObsObject object, final List<Md5.Entry> fileList)
			throws ObsServiceException, S3SdkClientException {
		File file;
		try {
			file = File.createTempFile(object.getKey(), Md5.MD5SUM_SUFFIX);
			try (PrintWriter writer = new PrintWriter(file)) {
				for (final Md5.Entry fileInfo : fileList) {
					writer.println(fileInfo);
				}
			}
		} catch (final IOException e) {
			throw new S3ObsServiceException(getBucketFor(object.getFamily()), object.getKey(),
					"Could not store md5sum temp file", e);
		}
		s3Services.uploadFile(getBucketFor(object.getFamily()),  Md5.md5KeyFor(object), file);

		try {
			Files.delete(file.toPath());
		} catch (final IOException e) {
			file.deleteOnExit();
		}
	}

	@Override
	public void move(final ObsObject from, final ProductFamily to) throws ObsException {
		ValidArgumentAssertion.assertValidArgument(from);
		ValidArgumentAssertion.assertValidArgument(to);
		try {
			s3Services.moveFile(new CopyObjectRequest(getBucketFor(from.getFamily()), from.getKey(), getBucketFor(to),
					from.getKey()));
		} catch (S3SdkClientException | ObsServiceException e) {
			throw new ObsException(from.getFamily(), from.getKey(), e);
		}
	}
	
	@Override
	public void delete(ObsObject object) throws ObsException, ObsServiceException {
		ValidArgumentAssertion.assertValidArgument(object);
		String bucket = getBucketFor(object.getFamily());
		String keyPrefix = object.getKey();
		try {
			LOGGER.info("Deleting all files in bucket {} with prefix {}", bucket, keyPrefix);
			final List<String> result = s3Services.getAll(bucket, keyPrefix).stream().map(S3ObjectSummary::getKey)
					.collect(Collectors.toList());
			for (String key : result) {
				LOGGER.debug("Deleting file {} in bucket {}", key, bucket);
				s3Services.deleteFile(new DeleteObjectRequest(bucket, key));
			}
			String md5sumfile = keyPrefix + Md5.MD5SUM_SUFFIX;
			if (s3Services.exist(bucket, md5sumfile)) {
				LOGGER.info("Deleting md5sum file {} in bucket {}", md5sumfile, bucket);
				s3Services.deleteFile(new DeleteObjectRequest(bucket, md5sumfile));
			} else {
				LOGGER.warn("No md5sum file exist for file {} in bucket {}", md5sumfile, bucket);
			}
		} catch (S3SdkClientException | ObsServiceException e) {
			throw new ObsException(object.getFamily(), object.getKey(), e);
		}
	}
	
	public void createBucket(final ProductFamily family) throws ObsServiceException, S3SdkClientException {
		s3Services.createBucket(getBucketFor(family));
	}

	@Override
	public List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(final ProductFamily family, final Date timeFrameBegin,
			final Date timeFrameEnd) throws SdkClientException {
		ValidArgumentAssertion.assertValidArgument(family);
		ValidArgumentAssertion.assertValidArgument(timeFrameBegin);
		ValidArgumentAssertion.assertValidArgument(timeFrameEnd);

		final long methodStartTime = System.currentTimeMillis();
		final List<ObsObject> objectsOfTimeFrame = new ArrayList<>();
		final String bucket = getBucketFor(family);
		LOGGER.debug(format("listing objects in OBS from bucket %s within last modification time %s to %s",
				bucket, timeFrameBegin, timeFrameEnd));
		ObjectListing objListing = s3Services.listObjectsFromBucket(bucket);
		boolean truncated;

		do {
			if (objListing == null) {
				break;
			}

			final List<S3ObjectSummary> objSum = objListing.getObjectSummaries();

			if (objSum == null || objSum.size() == 0) {
				break;
			}

			for (final S3ObjectSummary s : objSum) {
				if (s.getKey().endsWith(Md5.MD5SUM_SUFFIX)) {
					continue;
				}

				final Date lastModified = s.getLastModified();

				if (lastModified.after(timeFrameBegin) && lastModified.before(timeFrameEnd)) {
					final ObsObject obsObj = new ObsObject(family, s.getKey());
					objectsOfTimeFrame.add(obsObj);
				}
			}

			truncated = objListing.isTruncated();
			if (truncated) {
				objListing = s3Services.listNextBatchOfObjectsFromBucket(bucket, objListing);
			}

		} while (truncated);

		final float methodDuration = (System.currentTimeMillis() - methodStartTime) / 1000f;
		LOGGER.debug(format("Time for OBS listing objects from bucket %s within time frame: %.2fs", bucket,
				methodDuration));

		return objectsOfTimeFrame;
	}

	@Override
	public List<String> list(final ProductFamily family, final String keyPrefix) throws SdkClientException {
		ValidArgumentAssertion.assertValidArgument(family);
		ValidArgumentAssertion.assertValidPrefixArgument(keyPrefix);
		final String bucket = getBucketFor(family);
		LOGGER.debug("Getting all files in bucket {} with prefix {}", bucket, keyPrefix);
		final List<String> result = s3Services.getAll(bucket, keyPrefix)
				.stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
		LOGGER.debug("Found {} elements in bucket {} with prefix {}", result.size(), bucket, keyPrefix);
		return result;
	}

	@Override
	public InputStream getAsStream(final ProductFamily family, final String key) throws SdkClientException {
		ValidArgumentAssertion.assertValidArgument(family);
		ValidArgumentAssertion.assertValidPrefixArgument(key);
		final String bucket = getBucketFor(family);
		return s3Services.getAsInputStream(bucket, key);
	}

	@Override
	protected Map<String, String> collectETags(final ObsObject object) throws ObsException {
		try {
			return s3Services.collectETags(getBucketFor(object.getFamily()), object.getKey());
		} catch (S3SdkClientException | ObsServiceException e) {
			throw new ObsException(object.getFamily(), object.getKey(), e);
		}
	}

	@Override
	public long size(final ObsObject object) throws ObsException {
		ValidArgumentAssertion.assertValidArgument(object);
		try {
			final String bucketName = getBucketFor(object.getFamily());
			/*
			 * This method is supposed to return the size of exactly one object. If more than
			 * one is returned the object is not unique and very likely not the full name of it or
			 * a directory. We are not supporting this and thus operations fails
			 */
			if (s3Services.getNbObjects(bucketName, object.getKey()) != 1) {
				throw new IllegalArgumentException(format(
						"Unable to determinate size of object '%s' (family:%s) as more than one result is returned (is a directory?)",
						object.getKey(), object.getFamily()));
			}
			
			// return the size of the object
			return s3Services.size(bucketName, object.getKey());						
		} catch (final SdkClientException ex) {
			throw new ObsException(object.getFamily(), object.getKey(), ex);
		}
	}
	
	@Override
	public void setExpirationTime(final ObsObject object, final Instant expirationTime) throws ObsServiceException {
		s3Services.setExpirationTime(getBucketFor(object.getFamily()), object.getKey(), expirationTime);
	}

	@Override
	public ObsObjectMetadata getMetadata(final ObsObject object) throws ObsServiceException {
		final ObjectMetadata metadata = s3Services.getObjectMetadata(getBucketFor(object.getFamily()), object.getKey());
		return new ObsObjectMetadata(object.getKey(), metadata.getLastModified().toInstant());
	}

	@Override
	public URL createTemporaryDownloadUrl(final ObsObject object, final long expirationTimeInSeconds) throws ObsException, ObsServiceException {
		ValidArgumentAssertion.assertValidArgument(object);
		try {
			return s3Services.createTemporaryDownloadUrl(getBucketFor(object.getFamily()), object.getKey(), expirationTimeInSeconds);			
		} catch (final S3SdkClientException ex) {			
			throw new ObsException(object.getFamily(), object.getKey(), ex);			
		}
	}

	@Override
	public String getAbsoluteStoragePath(ProductFamily family, String keyObs) {
		return "s3://" + getBucketFor(family) + "/" + keyObs;
	}
}
