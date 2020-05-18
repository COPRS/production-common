package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

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
	 * @param s3client
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
	 * @param message
	 */
	private void log(final String message) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(message);
		}
	}

	/**
	 * Check if object with such key in bucket exists
	 * 
	 * @param bucketName
	 * @param keyName
	 * @return
	 * @throws SdkClientException
	 */
	public boolean exist(final String bucketName, final String keyName)
			throws S3ObsServiceException, S3SdkClientException {
		for (int retryCount = 1;; retryCount++) {
			try {
				return s3client.doesObjectExist(bucketName, keyName);		
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Checking object existance %s failed: Attempt : %d / %d", keyName,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, keyName,
								String.format("Checking object existance fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, keyName,
							String.format("Checking object existance fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 * Check if bucket exists
	 * 
	 * @param bucketName
	 * @return
	 * @throws S3SdkClientException
	 * @throws S3ObsServiceException
	 */
	public boolean bucketExist(final String bucketName) throws S3SdkClientException, S3ObsServiceException {
		for (int retryCount = 1;; retryCount++) {
			try {
				return s3client.doesBucketExistV2(bucketName);
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Checking bucket existance %s failed: Attempt : %d / %d", bucketName,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, "",
								String.format("Checking bucket existance fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, "",
							String.format("Checking bucket existance fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 * Get the number of objects in the bucket whose key matches with prefix
	 * 
	 * @param bucketName
	 * @param prefixKey
	 * @return
	 * @throws SdkClientException
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
					LOGGER.warn(String.format("Getting number of objects %s failed: Attempt : %d / %d", prefixKey,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, prefixKey,
								String.format("Getting number of objects fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, prefixKey,
							String.format("Getting number of objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 * Download objects of the given bucket with a key matching the prefix
	 * 
	 * @param bucketName
	 * @param prefixKey
	 * @param directoryPath
	 * @param ignoreFolders
	 * @return the download files
	 * @throws SdkClientException
	 * @throws ObsServiceException
	 */
	public List<File> downloadObjectsWithPrefix(final String bucketName, final String prefixKey,
			final String directoryPath, final boolean ignoreFolders)
			throws S3ObsServiceException, S3SdkClientException {
		log(String.format("Downloading objects with prefix %s from bucket %s in %s", prefixKey, bucketName,
				directoryPath));
		final List<File> files = new ArrayList<>();
		int nbObj;
		for (int retryCount = 1;; retryCount++) {
			nbObj = 0;
			// List all objects with given prefix
			try {
				final List<String> expectedFiles = getExpectedFiles(bucketName, prefixKey);
				log(String.format("Expected files for prefix %s is %s", prefixKey, String.join(", ", expectedFiles)));
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
					log(String.format("Downloading object %s from bucket %s in %s", key, bucketName, localFilePath));
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
							log(String.format("==debug filename=%s, key=%s", filename, key));
							final File fTo = new File(targetDir + filename);
							localFile.renameTo(fTo);
							localFile = fTo;
						}
					}
					files.add(localFile);
					nbObj++;
				}

				log(String.format("Download %d objects with prefix %s from bucket %s in %s succeeded", nbObj, prefixKey,
						bucketName, directoryPath));
				return files;
			} catch (final com.amazonaws.SdkClientException ase) {
				if (retryCount <= numRetries) {
					LOGGER.warn(
							String.format("Download objects with prefix %s from bucket %s failed: Attempt : %d / %d",
									prefixKey, bucketName, retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);						
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, prefixKey,
								String.format("Download in %s fails: %s", directoryPath, ase.getMessage()), ase);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, prefixKey,
							String.format("Download in %s fails: %s", directoryPath, ase.getMessage()), ase);
				}
			}
		}
	}
	
	List<String> getExpectedFiles(final String bucketName, final String prefixKey) throws S3ObsServiceException {

		final String md5FileName = Md5.md5KeyFor(prefixKey);
		log(String.format("Try to list expected files from file %s", md5FileName));

		final S3Object md5file = s3client.getObject(bucketName, md5FileName);
		if (md5file == null) {
			throw new com.amazonaws.SdkClientException(
					String.format("Tried to access md5sum file %s, but it odes not exist", md5FileName));
		}
		final S3ObsInputStream md5stream = new S3ObsInputStream(md5file, md5file.getObjectContent());
		List<String> result = new ArrayList<>();
		try {
			result = readMd5StreamAndGetFiles(prefixKey, md5stream);

		} catch (final IOException e) {
			throw new S3ObsServiceException(bucketName, prefixKey,
					String.format("Error getting expected files from file %s", md5FileName), e);
		}
		return result;
	}
	
	List<String> readMd5StreamAndGetFiles(final String prefixKey, final InputStream md5stream) throws IOException {

		final List<String> result = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(md5stream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				final int idx = line.indexOf("  ");
				if (idx >= 0 && line.length() > (idx + 2)) {
					final String key = line.substring(idx + 2);
					/*
					 * If the prefix is found in the key it is valid. This should work for single
					 * files as well as directories
					 */
					if (key.contains(prefixKey)) {
						result.add(key);
					}
				}
			}
		}

		return result;
	}

	private final List<S3ObjectSummary> getAll(final String bucketName, final String prefix) {
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
			throw new S3ObsServiceException(bucketName, prefix, String.format("Listing fails: %s", e.getMessage()), e);
		}

	}

	public final Map<String, String> collectMd5Sums(final String bucketName, final String prefix)
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
					LOGGER.warn(String.format("Listing prefixed objects %s from bucket %s failed: Attempt : %d / %d",
							prefix, bucketName, retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, prefix,
								String.format("Listing fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, prefix,
							String.format("Upload fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 * @param bucketName
	 * @param keyName
	 * @param uploadFile
	 * @throws S3SdkClientException
	 */
	public String uploadFile(final String bucketName, final String keyName, final File uploadFile)
			throws S3ObsServiceException, S3SdkClientException {
		String md5 = null;
		for (int retryCount = 1;; retryCount++) {
			try {
				log(String.format("Uploading object %s in bucket %s", keyName, bucketName));

				final Upload upload = s3tm.upload(bucketName, keyName, uploadFile);
				upload.addProgressListener((final ProgressEvent progressEvent) -> {
					LOGGER.trace(String.format("Uploading object %s in bucket %s: progress %s", keyName, bucketName,
							progressEvent.toString()));
				});

				try {
					final UploadResult uploadResult = upload.waitForUploadResult();
					md5 = uploadResult.getETag();
				} catch (final InterruptedException e) {
					throw new S3ObsServiceException(bucketName, keyName,
							"Upload fails: interrupted during waiting multipart upload completion", e);
				}

				log(String.format("Upload object %s in bucket %s succeeded", keyName, bucketName));
				break;
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Upload object %s from bucket %s failed: Attempt : %d / %d", keyName,
							bucketName, retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, keyName,
								String.format("Upload fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, keyName,
							String.format("Upload fails: %s", sce.getMessage()), sce);
				}
			}
		}
		return md5 + "  " + keyName;
	}

	/**
	 * @param bucketName
	 * @param keyName
	 * @param uploadDirectory
	 * @return
	 * @throws S3ObsServiceException
	 * @throws S3SdkClientException
	 */
	public List<String> uploadDirectory(final String bucketName, final String keyName, final File uploadDirectory)
			throws S3ObsServiceException, S3SdkClientException {
		final List<String> fileList = new ArrayList<>();
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

	public String uploadStream(final String bucketName, final String keyName, final InputStream in, final long contentLength) throws S3ObsServiceException, S3SdkClientException {
		{
			String md5 = null;
			for (int retryCount = 1;; retryCount++) {
				try {
					log(String.format("Uploading object %s in bucket %s", keyName, bucketName));

					final ObjectMetadata metadata = new ObjectMetadata();
					metadata.setContentLength(contentLength);
					final Upload upload = s3tm.upload(bucketName, keyName, in, metadata);
					upload.addProgressListener((final ProgressEvent progressEvent) -> {
						LOGGER.trace(String.format("Uploading object %s in bucket %s: progress %s", keyName, bucketName,
								progressEvent.toString()));
					});

					try {
						final UploadResult uploadResult = upload.waitForUploadResult();
						md5 = uploadResult.getETag();
					} catch (final InterruptedException e) {
						throw new S3ObsServiceException(bucketName, keyName,
								"Upload fails: interrupted during waiting multipart upload completion", e);
					}

					log(String.format("Upload object %s in bucket %s succeeded", keyName, bucketName));
					break;
				} catch (final com.amazonaws.SdkClientException sce) {
					if (retryCount <= numRetries) {						
						if (retryCount == 1) {
							LOGGER.warn("Upload object {} to bucket {} failed: Attempt : {}/{}", keyName,
									bucketName, retryCount, numRetries);																					
							LOGGER.debug("Exception is: {}", sce);							
						}
						else {
							LOGGER.warn("Upload object {} to bucket {} failed: Attempt : {}/{}", keyName,
									bucketName, retryCount, numRetries);
						}						
						try {
							Thread.sleep(retryDelay);
						} catch (final InterruptedException e) {
							throw new S3SdkClientException(bucketName, keyName,
									String.format("Upload fails: %s", sce.getMessage()), sce);
						}
						continue;
					} else {
						throw new S3SdkClientException(bucketName, keyName,
								String.format("Upload fails: %s", sce.getMessage()), sce);
					}
				}
			}
			return md5 + "  " + keyName;
		}
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

	public void createBucket(final String bucketName)
			throws ObsServiceException, S3SdkClientException {
		for (int retryCount = 1;; retryCount++) {
			try {
				s3client.createBucket(bucketName);
				break;
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Checking bucket existance %s failed: Attempt : %d / %d", bucketName,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, "",
								String.format("Bucket creation fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, "",
							String.format("Bucket creation fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 * @param bucketName
	 * @return
	 * @throws S3ObsServiceException
	 * @throws S3SdkClientException
	 */
	public ObjectListing listObjectsFromBucket(final String bucketName)
			throws S3ObsServiceException, S3SdkClientException {

		for (int retryCount = 1;; retryCount++) {
			try {
				log(String.format("Listing objects from bucket %s", bucketName));
				return s3client.listObjects(bucketName);
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Listing objects from bucket %s failed: Attempt : %d / %d", bucketName,
							retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, "",
								String.format("Listing objects fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, "",
							String.format("Listing objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	/**
	 * @param bucketName
	 * @param previousObjectListing
	 * @return
	 * @throws S3ObsServiceException
	 * @throws S3SdkClientException
	 */
	public ObjectListing listNextBatchOfObjectsFromBucket(final String bucketName,
			final ObjectListing previousObjectListing) throws S3ObsServiceException, S3SdkClientException {

		for (int retryCount = 1;; retryCount++) {
			try {
				log(String.format("Listing next batch of objects from bucket %s", bucketName));
				return s3client.listNextBatchOfObjects(previousObjectListing);
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Listing next batch of objects from bucket %s failed: Attempt : %d / %d",
							bucketName, retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(bucketName, "",
								String.format("Listing next batch of objects fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(bucketName, "",
							String.format("Listing next batch of objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	public void moveFile(final CopyObjectRequest request) throws S3ObsServiceException, S3SdkClientException {

		for (int retryCount = 1;; retryCount++) {
			try {
				log(String.format("Performing %s", request));
				s3client.copyObject(request);
				break;
			} catch (final com.amazonaws.SdkClientException sce) {
				if (retryCount <= numRetries) {
					LOGGER.warn(String.format("Move of objects from bucket %s failed: Attempt : %d / %d",
							request.getSourceBucketName(), retryCount, numRetries));
					try {
						Thread.sleep(retryDelay);
					} catch (final InterruptedException e) {
						throw new S3SdkClientException(request.getSourceBucketName(), request.getSourceKey(),
								String.format("Move of objects fails: %s", sce.getMessage()), sce);
					}
					continue;
				} else {
					throw new S3SdkClientException(request.getSourceBucketName(), request.getSourceKey(),
							String.format("Move of objects fails: %s", sce.getMessage()), sce);
				}
			}
		}
	}

	public long size(final String bucketName, final String prefix) throws S3SdkClientException {
		log(String.format("Get size of object %s from bucket %s", prefix, bucketName));
		final List<S3ObjectSummary> results = getAll(bucketName, prefix);
		if (results.size() != 1) {
			throw new S3SdkClientException(bucketName, prefix, String.format(
					"Size query for object %s from bucket %s returned %s results", prefix, bucketName, results.size()));
		}
		return results.get(0).getSize();
	}

	public String getChecksum(final String bucketName, final String prefix) throws S3SdkClientException {
		log(String.format("Get checksum of object %s from bucket %s", prefix, bucketName));
		final List<S3ObjectSummary> results = getAll(bucketName, prefix);
		if (results.size() != 1) {
			throw new S3SdkClientException(bucketName, prefix,
					String.format("Checksum query for object %s from bucket %s returned %s results", prefix, bucketName,
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
