package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.retry.PredefinedBackoffStrategies;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.ValidArgumentAssertion;
import esa.s1pdgs.cpoc.obs_sdk.s3.retry.SDKCustomDefaultRetryCondition;
import esa.s1pdgs.cpoc.obs_sdk.swift.SwiftSdkClientException;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

/**
 * <p>
 * Implement of the object storage client using AmazonS3
 * </p>
 * To configure, use the file {@link S3Configuration#CONFIG_FILE}
 * 
 * @author Viveris Technologies
 */
public class S3ObsClient extends AbstractObsClient {

	public static final class Factory implements ObsClient.Factory {
		@Override
		public final ObsClient newObsClient(final ObsConfigurationProperties config) {
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
						removedProtocol.substring(removedProtocol.indexOf(':') + 1, removedProtocol.length()));
				clientConfig.setProxyHost(host);
				clientConfig.setProxyPort(port);
			}

			final RetryPolicy retryPolicy = new RetryPolicy(new SDKCustomDefaultRetryCondition(config.getMaxRetries()),
					new PredefinedBackoffStrategies.SDKDefaultBackoffStrategy(config.getBackoffBaseDelay(),
							config.getBackoffThrottledBaseDelay(), config.getBackoffMaxDelay()),
					config.getMaxRetries(), true);
			clientConfig.setRetryPolicy(retryPolicy);

			final AmazonS3 client = AmazonS3ClientBuilder.standard().withClientConfiguration(clientConfig)
					.withEndpointConfiguration(
							new EndpointConfiguration(config.getEndpoint(), config.getEndpointRegion()))
					.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

			final TransferManager manager = TransferManagerBuilder.standard()
					.withMinimumUploadPartSize(config.getMinUploadPartSize() * 1024 * 1024)
					.withMultipartUploadThreshold(config.getMultipartUploadThreshold() * 1024 * 1024)
					.withS3Client(client).build();

			final S3ObsServices s3Services = new S3ObsServices(client, manager, config.getMaxRetries(),
					config.getBackoffThrottledBaseDelay());
			return new S3ObsClient(config, s3Services);
		}
	}

	public static final String BACKEND_NAME = "aws-s3";

	private final S3ObsServices s3Services;

	S3ObsClient(final ObsConfigurationProperties configuration, final S3ObsServices s3Services) {
		super(configuration);
		this.s3Services = s3Services;
	}
	
	public boolean bucketExists(final ProductFamily family) throws ObsServiceException, S3SdkClientException {
		return s3Services.bucketExist(getBucketFor(family));
	}

	/**
	 * @see ObsClient#doesObjectExist(ObsObject)
	 */
	@Override
	public boolean exists(final ObsObject object) throws SdkClientException, ObsServiceException {
		ValidArgumentAssertion.assertValidArgument(object);
		return s3Services.exist(getBucketFor(object.getFamily()), object.getKey());
	}

	/**
	 * @see ObsClient#doesPrefixExist(ObsObject)
	 */
	@Override
	public boolean prefixExists(final ObsObject object) throws SdkClientException, ObsServiceException {
		ValidArgumentAssertion.assertValidArgument(object);
		return s3Services.getNbObjects(getBucketFor(object.getFamily()), object.getKey()) > 0;
	}

	/**
	 * @see ObsClient#downloadObject(ObsDownloadObject)
	 */
	@Override
	public List<File> downloadObject(final ObsDownloadObject object) throws SdkClientException, ObsServiceException {
		final String bucket = getBucketFor(object.getFamily());
		LOGGER.debug("downloadObjectsWithPrefix from bucket {} with prefix {}", bucket, object.getKey());
		final List<File> res = s3Services.downloadObjectsWithPrefix(bucket, object.getKey(), object.getTargetDir(),
				object.isIgnoreFolders());
		LOGGER.debug("downloadObjectsWithPrefix from bucket {} with prefix {} got {} results", bucket, object.getKey(),
				res.size());
		return res;
	}

	@Override
	public void uploadObject(final ObsUploadObject object)
			throws SdkClientException, ObsServiceException, ObsException {
		final List<String> fileList = new ArrayList<>();
		if (object.getFile().isDirectory()) {
			fileList.addAll(
					s3Services.uploadDirectory(getBucketFor(object.getFamily()), object.getKey(), object.getFile()));
		} else {
			fileList.add(s3Services.uploadFile(getBucketFor(object.getFamily()), object.getKey(), object.getFile()));
		}
		uploadMd5Sum(object, fileList);
	}

	private void uploadMd5Sum(final ObsObject object, final List<String> fileList)
			throws ObsServiceException, S3SdkClientException {
		File file;
		try {
			file = File.createTempFile(object.getKey(), MD5SUM_SUFFIX);
			try (PrintWriter writer = new PrintWriter(file)) {
				for (final String fileInfo : fileList) {
					writer.println(fileInfo);
				}
			}
		} catch (final IOException e) {
			throw new S3ObsServiceException(getBucketFor(object.getFamily()), object.getKey(),
					"Could not store md5sum temp file", e);
		}
		s3Services.uploadFile(getBucketFor(object.getFamily()), object.getKey() + MD5SUM_SUFFIX, file);

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
	
	public void createBucket(final ProductFamily family) throws SwiftSdkClientException, ObsServiceException, S3SdkClientException {
		s3Services.createBucket(getBucketFor(family));
	}

	/**
	 * 
	 */
	@Override
	public List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(final ProductFamily family, final Date timeFrameBegin,
			final Date timeFrameEnd) throws SdkClientException, ObsServiceException {
		ValidArgumentAssertion.assertValidArgument(family);
		ValidArgumentAssertion.assertValidArgument(timeFrameBegin);
		ValidArgumentAssertion.assertValidArgument(timeFrameEnd);

		final long methodStartTime = System.currentTimeMillis();
		final List<ObsObject> objectsOfTimeFrame = new ArrayList<>();
		final String bucket = getBucketFor(family);
		LOGGER.debug(String.format("listing objects in OBS from bucket %s within last modification time %s to %s",
				bucket, timeFrameBegin, timeFrameEnd));
		ObjectListing objListing = s3Services.listObjectsFromBucket(bucket);
		boolean truncated = false;

		do {
			if (objListing == null) {
				break;
			}

			final List<S3ObjectSummary> objSum = objListing.getObjectSummaries();

			if (objSum == null || objSum.size() == 0) {
				break;
			}

			for (final S3ObjectSummary s : objSum) {
				if (s.getKey().endsWith(MD5SUM_SUFFIX)) {
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
		LOGGER.debug(String.format("Time for OBS listing objects from bucket %s within time frame: %.2fs", bucket,
				methodDuration));

		return objectsOfTimeFrame;
	}

	@Override
	public Map<String, InputStream> getAllAsInputStream(final ProductFamily family, final String keyPrefix)
			throws SdkClientException {
		ValidArgumentAssertion.assertValidArgument(family);
		ValidArgumentAssertion.assertValidPrefixArgument(keyPrefix);
		final String bucket = getBucketFor(family);
		LOGGER.debug("Getting all files in bucket {} with prefix {}", bucket, keyPrefix);
		final Map<String, InputStream> result = s3Services.getAllAsInputStream(bucket, keyPrefix);
		LOGGER.debug("Found {} elements in bucket {} with prefix {}", result.size(), bucket, keyPrefix);
		return result;
	}

	@Override
	protected Map<String, String> collectMd5Sums(final ObsObject object) throws ObsException {
		try {
			return s3Services.collectMd5Sums(getBucketFor(object.getFamily()), object.getKey());
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
				throw new IllegalArgumentException(String.format(
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
	public String getChecksum(final ObsObject object) throws ObsException {
		ValidArgumentAssertion.assertValidArgument(object);
		try {
			final String bucketName = getBucketFor(object.getFamily());
			/*
			 * This method is supposed to return the size of exactly one object. If more than
			 * one is returned the object is not unique and very likely not the full name of it or
			 * a directory. We are not supporting this and thus operations fails
			 */
			if (s3Services.getNbObjects(bucketName, object.getKey()) != 1) {
				throw new IllegalArgumentException(String.format(
						"Unable to determinate checksum of object '%s' (family:%s) (is a directory?)",
						object.getKey(), object.getFamily()));
			}
			
			// return the checksum of the object
			return s3Services.getChecksum(bucketName, object.getKey());
		} catch (final SdkClientException ex) {
			throw new ObsException(object.getFamily(), object.getKey(), ex);
		}
	}

	@Override
	public URL createTemporaryDownloadUrl(final ObsObject object, final long expirationTimeInSeconds) throws ObsException, ObsServiceException {
		ValidArgumentAssertion.assertValidArgument(object);
		URL url;		
		final Reporting reporting = ReportingUtils.newReportingBuilderFor("CreateTemporaryDownloadUrl")
				.newReporting();
		reporting.begin(new ReportingMessage(size(object), "Start creating temporary download URL for username '{}' for product '{}'", "anonymous", object.getKey()));

		try {
			url = s3Services.createTemporaryDownloadUrl(getBucketFor(object.getFamily()), object.getKey(), expirationTimeInSeconds);
			reporting.end(new ReportingMessage(size(object), "End creating temporary download URL for username '{}' for product '{}'", "anonymous", object.getKey()));				
		} catch (final S3SdkClientException ex) {
			reporting.error(new ReportingMessage(size(object), "Error on creating temporary download URL for username '{}' for product '{}'", "anonymous", object.getKey()));				
			throw new ObsException(object.getFamily(), object.getKey(), ex);			
		}
     	return url;
	}
}
