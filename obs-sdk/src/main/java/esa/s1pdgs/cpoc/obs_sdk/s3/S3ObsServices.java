package esa.s1pdgs.cpoc.obs_sdk.s3;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import com.amazonaws.util.IOUtils;

import esa.s1pdgs.cpoc.obs_sdk.Md5;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;

/**
 * Provides services to manage objects in the object storage wia the AmazonS3
 * APIs
 * 
 * @author Viveris Technologies
 */
public class S3ObsServices {
	static final class S3ObsInputStream extends InputStream {
		private final S3Object obj;
		private final InputStream in;

		public S3ObsInputStream(final S3Object obj, final InputStream in) {
			this.obj = obj;
			this.in = in;
		}

		@Override
		public int read() throws IOException {
			return in.read();
		}

		@Override
		public void close() throws IOException {
			IOUtils.drainInputStream(in);
			IOUtils.closeQuietly(in, null);
			IOUtils.closeQuietly(obj, null);
		}
	}

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(S3ObsServices.class);

	/**
	 * Amazon S3 client
	 */
	public final AmazonS3 s3client;

	/**
	 * Amazon S3 client
	 */
	protected final TransferManager s3tm;

	/**
	 * Number of retries until client error
	 */
	private final int numRetries;

	/**
	 * Delay before retrying
	 */
	private final int retryDelay;

	/**
	 */
	public S3ObsServices(final AmazonS3 s3client, final TransferManager s3tm, final int numRetries,
			final int retryDelay) {
		this.s3client = s3client;
		this.s3tm = s3tm;
		this.numRetries = numRetries;
		this.retryDelay = retryDelay;
	}

	/**
	 * Internal function to log messages
	 * 
	 */
	private void log(final String message) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(message);
		}
	}

	/**
	 * Check if object with such key in bucket exists
	 * 
	 */
	public boolean exist(final String bucketName, final String keyName)
			throws S3ObsServiceException, S3SdkClientException {
		for (int retryCount = 1;; retryCount++) {
			try {
				return s3client.doesObjectExist(bucketName, keyName);		
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(format("Checking object existance %s failed: Attempt : %d / %d", keyName,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, keyName,
								format("Checking object existance fails: %s", sce.getMessage()), sce);
					}
				} else {
					throw new S3SdkClientException(bucketName, keyName,
							format("Checking object existance fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 * Check if bucket exists
	 * 
	 */
	public boolean bucketExist(final String bucketName) throws S3SdkClientException, S3ObsServiceException {
		for (int retryCount = 1;; retryCount++) {
			try {
				return s3client.doesBucketExistV2(bucketName);
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(format("Checking bucket existance %s failed: Attempt : %d / %d", bucketName,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, "",
								format("Checking bucket existance fails: %s", sce.getMessage()), sce);
					}
				} else {
					throw new S3SdkClientException(bucketName, "",
							format("Checking bucket existance fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 * Get the number of objects in the bucket whose key matches with prefix
	 * 
	 */
	public int getNbObjects(final String bucketName, final String prefixKey)
			throws S3ObsServiceException, S3SdkClientException {
		for (int retryCount = 1;; retryCount++) {
			int nbObj = 0;
			try {
				final ObjectListing objectListing = s3client.listObjects(bucketName, prefixKey);
				if (objectListing != null && !CollectionUtils.isEmpty(objectListing.getObjectSummaries())) {
					for (final S3ObjectSummary s : objectListing.getObjectSummaries()) {
						if (!s.getKey().endsWith(Md5.MD5SUM_SUFFIX)) {
							nbObj++;
						}
					}
				}
				return nbObj;
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(format("Getting number of objects %s failed: Attempt : %d / %d", prefixKey,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, prefixKey,
								format("Getting number of objects fails: %s", sce.getMessage()), sce);
					}
				} else {
					throw new S3SdkClientException(bucketName, prefixKey,
							format("Getting number of objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 * Download objects of the given bucket with a key matching the prefix
	 * 
	 */
	public List<File> downloadObjectsWithPrefix(final String bucketName, final String prefixKey,
			final String directoryPath, final boolean ignoreFolders)
			throws S3ObsServiceException, S3SdkClientException {
		log(format("Downloading objects with prefix %s from bucket %s in %s", prefixKey, bucketName,
				directoryPath));
		final List<File> files = new ArrayList<>();
		int nbObj;
		for (int retryCount = 1;; retryCount++) {
			nbObj = 0;
			// List all objects with given prefix
			try {
				final List<String> expectedFiles = getExpectedFiles(bucketName, prefixKey);
				log(format("Expected files for prefix %s is %s", prefixKey, String.join(", ", expectedFiles)));
				// TODO: How to handle MD5 SUM files???

				for (final String key : expectedFiles) {
					// only download md5sum files if it has been explicitly asked for a md5sum file
					if (!prefixKey.endsWith(Md5.MD5SUM_SUFFIX)
							&& key.endsWith(Md5.MD5SUM_SUFFIX)) {
						continue;
					}

					// Build temporarly filename
					String targetDir = directoryPath;
					if (!targetDir.endsWith(File.separator)) {
						targetDir += File.separator;
					}

					final String localFilePath = targetDir + key;
					// Download object
					log(format("Downloading object %s from bucket %s in %s", key, bucketName, localFilePath));
					File localFile = new File(localFilePath);
					if (localFile.getParentFile() != null) {
						localFile.getParentFile().mkdirs();
					}
					try {
						localFile.createNewFile();
					} catch (final IOException ioe) {
						throw new S3ObsServiceException(bucketName, key,
								"Directory creation fails for " + localFilePath, ioe);
					}
					s3client.getObject(new GetObjectRequest(bucketName, key), localFile);
					// If needed move in the target directory
					if (ignoreFolders) {
						String filename = key;
						final int lastIndex = key.lastIndexOf('/');
						if (lastIndex != -1) {
							filename = key.substring(lastIndex + 1);
						}
						if (!key.equals(filename)) {
							log(format("==debug filename=%s, key=%s", filename, key));
							final File fTo = new File(targetDir + filename);
							localFile.renameTo(fTo);
							localFile = fTo;
						}
					}
					files.add(localFile);
					nbObj++;
				}

				log(format("Download %d objects with prefix %s from bucket %s in %s succeeded", nbObj, prefixKey,
						bucketName, directoryPath));
				return files;
			} catch (final com.amazonaws.SdkClientException ase) {
				if (retryCount <= numRetries) {
					LOGGER.warn(
							format("Download objects with prefix %s from bucket %s failed: Attempt : %d / %d",
									prefixKey, bucketName, retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);						
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, prefixKey,
								format("Download in %s fails: %s", directoryPath, ase.getMessage()), ase);
					}
				} else {
					throw new S3SdkClientException(bucketName, prefixKey,
							format("Download in %s fails: %s", directoryPath, ase.getMessage()), ase);
				}
			}
		}
	}
	
	List<String> getExpectedFiles(final String bucketName, final String prefixKey) throws S3ObsServiceException {

		final String md5FileName = Md5.md5KeyFor(prefixKey);
		log(format("Try to list expected files from file %s", md5FileName));

		final S3Object md5file = s3client.getObject(bucketName, md5FileName);
		if (md5file == null) {
			throw new com.amazonaws.SdkClientException(
					format("Tried to access md5sum file %s, but it odes not exist", md5FileName));
		}
		final S3ObsInputStream md5stream = new S3ObsInputStream(md5file, md5file.getObjectContent());
		try {
			return readMd5StreamAndGetFiles(prefixKey, md5stream);

		} catch (final IOException e) {
			throw new S3ObsServiceException(bucketName, prefixKey,
					format("Error getting expected files from file %s", md5FileName), e);
		}
	}
	
	List<String> readMd5StreamAndGetFiles(final String prefixKey, final InputStream md5stream) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(md5stream))) {
			final List<String> result = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				final Optional<Md5.Entry> entry = Md5.parseIfPossible(line);

				if(!entry.isPresent()) {
					continue; //silently
				}
				/*
				 * If the prefix is found in the key it is valid. This should work for single
				 * files as well as directories
				 */
				if(entry.get().getFileName().contains(prefixKey)) {
					result.add(entry.get().getFileName());
				}
			}
			return result;
		}
	}

	private List<S3ObjectSummary> getAll(final String bucketName, final String prefix) {
		final List<S3ObjectSummary> result = new ArrayList<>();
		ObjectListing listing = null;
		do {
			listing = listing == null ? s3client.listObjects(bucketName, prefix)
					: s3client.listNextBatchOfObjects(listing);
			for (final S3ObjectSummary object : listing.getObjectSummaries()) {
				// only download md5sum files if it has been explicitly asked for a md5sum file
				if (!prefix.endsWith(Md5.MD5SUM_SUFFIX)
						&& object.getKey().endsWith(Md5.MD5SUM_SUFFIX)) {
					continue;
				}
				result.add(object);
			}
		} while (listing.isTruncated());

		return result;
	}

	public final Map<String, InputStream> getAllAsInputStream(final String bucketName, final String prefix)
			throws S3ObsServiceException, S3SdkClientException {
		final Map<String, InputStream> result = new LinkedHashMap<>();

		try {
			for (final S3ObjectSummary summary : getAll(bucketName, prefix)) {
				final String key = summary.getKey();
				final S3Object obj = s3client.getObject(bucketName, key);
				result.put(key, new S3ObsInputStream(obj, obj.getObjectContent()));
			}
			return result;
		} catch (final com.amazonaws.SdkClientException e) {
			throw new S3ObsServiceException(bucketName, prefix, format("Listing fails: %s", e.getMessage()), e);
		}

	}

	public final Map<String, String> collectETags(final String bucketName, final String prefix)
			throws S3ObsServiceException, S3SdkClientException {
		Map<String, String> result;
		for (int retryCount = 1;; retryCount++) {
			result = new HashMap<>();
			try {
				for (final S3ObjectSummary summary : getAll(bucketName, prefix)) {
					final String key = summary.getKey();
					if (!key.endsWith(Md5.MD5SUM_SUFFIX)) {
						final ObjectMetadata objectMetadata = s3client.getObjectMetadata(bucketName, key);
						result.put(key, objectMetadata.getETag());
					}
				}
				return result;
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(format("Listing prefixed objects %s from bucket %s failed: Attempt : %d / %d",
							prefix, bucketName, retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, prefix,
								format("Listing fails: %s", sce.getMessage()), sce);
					}
				} else {
					throw new S3SdkClientException(bucketName, prefix,
							format("Upload fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 */
	public Md5.Entry uploadFile(final String bucketName, final String keyName, final File uploadFile)
			throws S3ObsServiceException, S3SdkClientException {
		InputStream in  = null;
		try {
			in = new FileInputStream(uploadFile);
			return uploadStream(bucketName, keyName, in, uploadFile.length());
		} catch (FileNotFoundException e) {
			throw new S3SdkClientException(bucketName, keyName, "could not create input stream for file "+ uploadFile, e);
		} finally {
			IOUtils.closeQuietly(in, null);
		}
	}

	/**
	 */
	public List<Md5.Entry> uploadDirectory(final String bucketName, final String keyName, final File uploadDirectory)
			throws S3ObsServiceException, S3SdkClientException {
		final List<Md5.Entry> fileList = new ArrayList<>();
		if (uploadDirectory.isDirectory()) {
			final File[] childs = uploadDirectory.listFiles();
			if (childs != null) {
				for (final File child : childs) {
					if (child.isDirectory()) {
						fileList.addAll(uploadDirectory(bucketName, keyName + "/" + child.getName(), child));
					} else {
						fileList.add(uploadFile(bucketName, keyName + "/" + child.getName(), child));
					}
				}
			}
		} else {
			fileList.add(uploadFile(bucketName, keyName, uploadDirectory));
		}
		return fileList;
	}

	public Md5.Entry uploadStream(final String bucketName, final String keyName, final InputStream in, final long contentLength) throws S3ObsServiceException, S3SdkClientException {
		{
			for (int retryCount = 1;; retryCount++) {
				try {
					log(format("Uploading object %s in bucket %s", keyName, bucketName));

					final DigestInputStream digestInputStream = new DigestInputStream(in, MessageDigest.getInstance("MD5"));

					final ObjectMetadata metadata = new ObjectMetadata();
					metadata.setContentLength(contentLength);
					final Upload upload = s3tm.upload(bucketName, keyName, digestInputStream, metadata);
					upload.addProgressListener(
							(final ProgressEvent progressEvent)
									-> LOGGER.trace(format("Uploading object %s in bucket %s: progress %s",
									keyName,
									bucketName,
									progressEvent.toString())));

					try {
						final UploadResult result = upload.waitForUploadResult();
						log(format("Upload object %s in bucket %s succeeded", keyName, bucketName));

						return new Md5.Entry(md5Of(digestInputStream), result.getETag(), keyName);
					} catch (final InterruptedException e) {
						throw new S3ObsServiceException(bucketName, keyName,
								"Upload fails: interrupted during waiting multipart upload completion", e);
					}

				} catch (final com.amazonaws.SdkClientException sce) {
					if (retryCount <= numRetries) {						
						if (retryCount == 1) {
							LOGGER.warn("Upload object {} to bucket {} failed: Attempt : {}/{}", keyName,
									bucketName, retryCount, numRetries);																					
							LOGGER.debug("Exception is", sce);
						}
						else {
							LOGGER.warn("Upload object {} to bucket {} failed: Attempt : {}/{}", keyName,
									bucketName, retryCount, numRetries);
						}						
						try {
							Thread.sleep(retryDelay);
						} catch (final InterruptedException e) {
							throw new S3SdkClientException(bucketName, keyName,
									format("Upload fails: %s", sce.getMessage()), sce);
						}
					} else {
						throw new S3SdkClientException(bucketName, keyName,
								format("Upload fails: %s", sce.getMessage()), sce);
					}
				} catch (NoSuchAlgorithmException e) {
					//thrown by MessageDigest.getInstance("MD5"), which should not happen
					throw new S3SdkClientException(bucketName, keyName,
							format("Upload fails: %s", e.getMessage()), e);
				}
			}
		}
	}

	private String md5Of(DigestInputStream digestInputStream) {
		final MessageDigest messageDigest = digestInputStream.getMessageDigest();
		final byte[] hash = messageDigest.digest();

		return new BigInteger(1, hash).toString(16);
	}

	public void setExpirationTime(final String bucketName, final String prefix, final Instant expirationDate) {
		BucketLifecycleConfiguration lifecycleConfiguration = s3client.getBucketLifecycleConfiguration(new GetBucketLifecycleConfigurationRequest(bucketName));

		if (lifecycleConfiguration == null) {
			lifecycleConfiguration = new BucketLifecycleConfiguration();
		}

		if (lifecycleConfiguration.getRules() == null) {
			lifecycleConfiguration.withRules(new ArrayList<>());
		}

		final Optional<BucketLifecycleConfiguration.Rule> optionalRule = lifecycleConfiguration.getRules().stream().filter(r -> r.getId().equals(prefix)).findAny();

		final BucketLifecycleConfiguration.Rule rule;
		if (!optionalRule.isPresent()) {
			rule = new BucketLifecycleConfiguration.Rule();
			lifecycleConfiguration.getRules().add(rule);
		} else {
			rule = optionalRule.get();
		}

		rule.withId(prefix)
				.withPrefix(prefix) //filter does not work
				.withExpirationDate(new Date(expirationDate.truncatedTo(ChronoUnit.DAYS).toEpochMilli()))
				.withStatus(BucketLifecycleConfiguration.ENABLED);

		s3client.setBucketLifecycleConfiguration(new SetBucketLifecycleConfigurationRequest(bucketName, lifecycleConfiguration));
	}

	public ObjectMetadata getObjectMetadata(String bucketName, String key) {
		return s3client.getObjectMetadata(bucketName, key);
	}

	public void createBucket(final String bucketName)
			throws ObsServiceException, S3SdkClientException {
		for (int retryCount = 1;; retryCount++) {
			try {
				s3client.createBucket(bucketName);
				break;
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(format("Checking bucket existance %s failed: Attempt : %d / %d", bucketName,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, "",
								format("Bucket creation fails: %s", sce.getMessage()), sce);
					}
				} else {
					throw new S3SdkClientException(bucketName, "",
							format("Bucket creation fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 */
	public ObjectListing listObjectsFromBucket(final String bucketName)
			throws S3ObsServiceException, S3SdkClientException {

		for (int retryCount = 1;; retryCount++) {
			try {
				log(format("Listing objects from bucket %s", bucketName));
				return s3client.listObjects(bucketName);
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(format("Listing objects from bucket %s failed: Attempt : %d / %d", bucketName,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, "",
								format("Listing objects fails: %s", sce.getMessage()), sce);
					}
				} else {
					throw new S3SdkClientException(bucketName, "",
							format("Listing objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 */
	public ObjectListing listNextBatchOfObjectsFromBucket(final String bucketName,
			final ObjectListing previousObjectListing) throws S3ObsServiceException, S3SdkClientException {

		for (int retryCount = 1;; retryCount++) {
			try {
				log(format("Listing next batch of objects from bucket %s", bucketName));
				return s3client.listNextBatchOfObjects(previousObjectListing);
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(format("Listing next batch of objects from bucket %s failed: Attempt : %d / %d",
							bucketName, retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, "",
								format("Listing next batch of objects fails: %s", sce.getMessage()), sce);
					}
				} else {
					throw new S3SdkClientException(bucketName, "",
							format("Listing next batch of objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	public void moveFile(final CopyObjectRequest request) throws S3ObsServiceException, S3SdkClientException {

		for (int retryCount = 1;; retryCount++) {
			try {
				log(format("Performing %s", request));
				s3client.copyObject(request);
				break;
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(format("Move of objects from bucket %s failed: Attempt : %d / %d",
							request.getSourceBucketName(), retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(request.getSourceBucketName(), request.getSourceKey(),
								format("Move of objects fails: %s", sce.getMessage()), sce);
					}
				} else {
					throw new S3SdkClientException(request.getSourceBucketName(), request.getSourceKey(),
							format("Move of objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	public long size(final String bucketName, final String prefix) throws S3SdkClientException {
		log(format("Get size of object %s from bucket %s", prefix, bucketName));
		final List<S3ObjectSummary> results = getAll(bucketName, prefix);
		if (results.size() != 1) {
			throw new S3SdkClientException(bucketName, prefix, format(
					"Size query for object %s from bucket %s returned %s results", prefix, bucketName, results.size()));
		}
		return results.get(0).getSize();
	}

	public String getChecksum(final String bucketName, final String prefix) throws S3SdkClientException {
		log(format("Get checksum of object %s from bucket %s", prefix, bucketName));
		final List<S3ObjectSummary> results = getAll(bucketName, prefix);
		if (results.size() != 1) {
			throw new S3SdkClientException(bucketName, prefix,
					format("Checksum query for object %s from bucket %s returned %s results", prefix, bucketName,
							results.size()));
		}
		return results.get(0).getETag();
	}

	public URL createTemporaryDownloadUrl(final String bucketName, final String key, final long expirationTimeInSeconds)
			throws S3SdkClientException {
		try {
			final java.util.Date expiration = new java.util.Date();
			long expTimeMillis = expiration.getTime();
			expTimeMillis += 1000 * expirationTimeInSeconds;
			expiration.setTime(expTimeMillis);

			final GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key)
					.withMethod(HttpMethod.GET).withExpiration(expiration);
			return s3client.generatePresignedUrl(generatePresignedUrlRequest);
		} catch (final AmazonServiceException e) {
			throw new S3SdkClientException(bucketName, key, "Could not create temporary download URL");
		}
	}

}
