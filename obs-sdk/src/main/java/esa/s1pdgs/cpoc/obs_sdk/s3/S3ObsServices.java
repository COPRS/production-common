package esa.s1pdgs.cpoc.obs_sdk.s3;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.amazonaws.services.s3.model.DeleteObjectRequest;
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

import esa.s1pdgs.cpoc.common.steps.UndoableStep;
import esa.s1pdgs.cpoc.common.steps.UndoableStepsHandler;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.obs_sdk.Md5;

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

	private final Path localFilesLocation;

	/**
	 */
	public S3ObsServices(final AmazonS3 s3client, final TransferManager s3tm, final int numRetries,
			final int retryDelay, final Path localFilesManager) {
		this.s3client = s3client;
		this.s3tm = s3tm;
		this.numRetries = numRetries;
		this.retryDelay = retryDelay;
		this.localFilesLocation = localFilesManager;
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

		try {
			return Retries.performWithRetries(
					() -> s3client.doesObjectExist(bucketName, keyName),
					format("does object %s/%s exist", bucketName, keyName),
					numRetries,
					retryDelay);
		} catch (Exception e) {
			throw new S3SdkClientException(bucketName, keyName,
					format("Checking object existence fails: %s", e.getMessage()), e);
		}
	}

	/**
	 * Check if bucket exists
	 * 
	 */
	public boolean bucketExist(final String bucketName) throws S3SdkClientException {
		try {
			return Retries.performWithRetries(
					() -> s3client.doesBucketExistV2(bucketName),
					format("does bucket %s exist", bucketName),
					numRetries,
					retryDelay);
		} catch (Exception e) {
			throw new S3SdkClientException(bucketName, "",
					format("Checking bucket existence fails: %s", e.getMessage()), e);
		}
	}

	/**
	 * Get the number of objects in the bucket whose key matches with prefix
	 * 
	 */
	public int getNbObjects(final String bucketName, final String prefixKey)
			throws S3ObsServiceException, S3SdkClientException {

		try {
			return Retries.performWithRetries(
					() -> {				int nbObj = 0;
						final ObjectListing objectListing = s3client.listObjects(bucketName, prefixKey);
						if (objectListing != null && !CollectionUtils.isEmpty(objectListing.getObjectSummaries())) {
							for (final S3ObjectSummary s : objectListing.getObjectSummaries()) {
								if (!s.getKey().endsWith(Md5.MD5SUM_SUFFIX)) {
									nbObj++;
								}
							}
						}
						return nbObj;
					},
					format("number of objects %s/%s", bucketName, prefixKey),
					numRetries,
					retryDelay);
		} catch (Exception e) {
			throw new S3SdkClientException(bucketName, prefixKey,
					format("Getting number of objects fails: %s", e.getMessage()), e);
		}
	}

	/**
	 * Download objects of the given bucket with a key matching the prefix
	 * 
	 */
	public List<File> downloadObjectsWithPrefix(final String bucketName, final String prefixKey,
			final String directoryPath, final boolean ignoreFolders)
			throws S3SdkClientException {
		log(format("Downloading objects with prefix %s from bucket %s in %s", prefixKey, bucketName,
				directoryPath));

		try {
			return Retries.performWithRetries(
					() -> {
						// List all objects with given prefix
						final List<File> files = new ArrayList<>();
						int nbObj = 0;

						final List<String> expectedFiles = getExpectedFiles(bucketName, prefixKey);
						log(format("Expected files for prefix %s is %s", prefixKey, String.join(", ", expectedFiles)));
						// TODO: How to handle MD5 SUM files???

						for (final String key : expectedFiles) {
							// only download md5sum files if it has been explicitly asked for a md5sum file
							if (!prefixKey.endsWith(Md5.MD5SUM_SUFFIX)
									&& key.endsWith(Md5.MD5SUM_SUFFIX)) {
								continue;
							}

							// Build temporary filename
							String targetDir = directoryPath;
							if (!targetDir.endsWith(File.separator)) {
								targetDir += File.separator;
							}

							final String localFilePath = targetDir + key;
							// Download object
							log(format("Downloading object %s from bucket %s in %s", key, bucketName, localFilePath));
							File localFile = new File(localFilePath);
							try {
								if (localFile.getParentFile() != null) {
									Files.createDirectories(localFile.getParentFile().toPath());
								}
								Files.createFile(localFile.toPath());
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
									Files.move(localFile.toPath(), fTo.toPath());
									localFile = fTo;
								}
							}
							files.add(localFile);
							nbObj++;
						}

						log(format("Download %d objects with prefix %s from bucket %s in %s succeeded", nbObj, prefixKey,
								bucketName, directoryPath));
						return files;
					},
					format("download objects %s/%s to %s", bucketName, prefixKey, directoryPath),
					numRetries,
					retryDelay);
		} catch (Exception e) {
			throw new S3SdkClientException(bucketName, prefixKey,
					format("Download in %s fails: %s", directoryPath, e.getMessage()), e);

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

	public List<S3ObjectSummary> getAll(final String bucketName, final String prefix) {
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

	public final InputStream getAsInputStream(final String bucketName, final String key) throws S3ObsServiceException {
		try {
			final S3Object obj = s3client.getObject(bucketName, key);
			return new S3ObsInputStream(obj, obj.getObjectContent());
		} catch (final com.amazonaws.SdkClientException e) {
			throw new S3ObsServiceException(bucketName, key, format("Reading fails: %s", e.getMessage()), e);
		}
	}

	public final Map<String, String> collectETags(final String bucketName, final String prefix)
			throws S3ObsServiceException, S3SdkClientException {
		try {
			return Retries.performWithRetries(
					() -> {
						final Map<String, String> result = new HashMap<>();
						for (final S3ObjectSummary summary : getAll(bucketName, prefix)) {
							final String key = summary.getKey();
							if (!key.endsWith(Md5.MD5SUM_SUFFIX)) {
								final ObjectMetadata objectMetadata = s3client.getObjectMetadata(bucketName, key);
								result.put(key, objectMetadata.getETag());
							}
						}
						return result;
					},
					format("listing objects %s/%s", bucketName, prefix),
					numRetries,
					retryDelay);
		} catch (Exception e) {
			throw new S3SdkClientException(bucketName, prefix,
					format("Listing fails: %s", e.getMessage()), e);
		}
	}

	/**
	 */
	public Md5.Entry uploadFile(final String bucketName, final String keyName, final File uploadFile)
			throws S3ObsServiceException, S3SdkClientException {
		InputStream in  = null;
		try {
			in = new FileInputStream(uploadFile);
			return uploadStream(bucketName, keyName, in);
		} catch (final FileNotFoundException e) {
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
			final File[] children = uploadDirectory.listFiles();
			if (children != null) {
				for (final File child : children) {
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

	public Md5.Entry uploadStream(final String bucketName, final String keyName, final InputStream in) throws S3SdkClientException, S3ObsUnrecoverableException {
		try {
			final Path lastPathElement = Paths.get(keyName).getFileName();

			final Md5SumCalculationHelper md5SumCalculationHelper = Md5SumCalculationHelper.createFor(in);
			final Path localFilePath = localFilesLocation.resolve(lastPathElement);
			final DownloadFileStep downloadFileStep = new DownloadFileStep(localFilePath, md5SumCalculationHelper.getInputStream(), this);
			final UploadFileStep uploadFileStep = new UploadFileStep(this, localFilePath.toFile(), keyName, bucketName);
			final DeleteFileStep deleteFileStep = new DeleteFileStep(localFilePath.toFile(), this);

			new UndoableStepsHandler(downloadFileStep, uploadFileStep, deleteFileStep).perform();

			return new Md5.Entry(md5SumCalculationHelper.getMd5Sum(), uploadFileStep.uploadResult().getETag(), keyName); //TODO filename?
		} catch (FileDeletionException e) {
			throw new S3ObsUnrecoverableException(bucketName, keyName, "could not delete local file", e);
		} catch (Exception e) {
			throw new S3SdkClientException(bucketName, keyName, "error during uploading file", e);
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

	public ObjectMetadata getObjectMetadata(final String bucketName, final String key) {
		return s3client.getObjectMetadata(bucketName, key);
	}

	public void createBucket(final String bucketName)
			throws S3SdkClientException {
		try {
			Retries.performWithRetries(() -> {
				s3client.createBucket(bucketName);
				return null;
			}, format("create bucket %s", bucketName), numRetries, retryDelay);
		} catch (Exception e) {
			throw new S3SdkClientException(bucketName, "",
					format("Bucket creation fails: %s", e.getMessage()), e);

		}
	}

	/**
	 */
	public ObjectListing listObjectsFromBucket(final String bucketName)
			throws S3SdkClientException {
		try {
			log(format("Listing objects from bucket %s", bucketName));
			return Retries.performWithRetries(
					() -> s3client.listObjects(bucketName),
					format("listing objects in %s", bucketName),
					numRetries,
					retryDelay);
		} catch (Exception e) {
			throw new S3SdkClientException(bucketName, "",
					format("Listing objects fails: %s", e.getMessage()), e);

		}
	}

	/**
	 */
	public ObjectListing listNextBatchOfObjectsFromBucket(final String bucketName,
			final ObjectListing previousObjectListing) throws S3SdkClientException {
		try {
			log(format("Listing next batch of objects from bucket %s", bucketName));
			return Retries.performWithRetries(
					() -> s3client.listNextBatchOfObjects(previousObjectListing),
					format("list next batch from %s", bucketName),
					numRetries,
					retryDelay);
		} catch (Exception e) {
			throw new S3SdkClientException(bucketName, "",
					format("Listing next batch of objects fails: %s", e.getMessage()), e);
		}
	}

	public void moveFile(final CopyObjectRequest request) throws S3ObsServiceException, S3SdkClientException {
		try {
			log(format("Performing %s", request));
			Retries.performWithRetries(
					() -> {
						s3client.copyObject(request);
						return null;
					},
					format("move objects %s/%s to %s/%s",
							request.getSourceBucketName(),
							request.getSourceKey(),
							request.getDestinationBucketName(),
							request.getDestinationKey()),
					numRetries,
					retryDelay);
		} catch (Exception e) {
			throw new S3SdkClientException(request.getSourceBucketName(), request.getSourceKey(),
					format("Move of objects fails: %s", e.getMessage()), e);
		}
	}
	
	public void deleteFile(DeleteObjectRequest deleteObjectRequest) throws S3ObsServiceException, S3SdkClientException {
		try {
			Retries.performWithRetries(() -> {
						s3client.deleteObject(deleteObjectRequest);
						return null;
					},
					format("deletion of %s/%s", deleteObjectRequest.getBucketName(), deleteObjectRequest.getKey()),
					numRetries,
					retryDelay);
		} catch (Exception e) {
			throw new S3SdkClientException(deleteObjectRequest.getBucketName(),
					deleteObjectRequest.getKey(), format("Deletion of object fails: %s", e.getMessage()),
					e);

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

	public static class DownloadFileStep implements UndoableStep {

		private final Path destination;
		private final InputStream inputStream;
		private final S3ObsServices s3ObsServices;

		public DownloadFileStep(Path destination, InputStream inputStream, S3ObsServices s3ObsServices) {
			this.destination = destination;
			this.inputStream = inputStream;
			this.s3ObsServices = s3ObsServices;
		}

		@Override
		public void perform() {
			try {
				Retries.performWithRetries(
						() -> Files.copy(inputStream, destination),
						"download to " + destination,
						s3ObsServices.numRetries, s3ObsServices.retryDelay);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void undo() {
			DeleteFile.withRetries(destination, s3ObsServices.numRetries, s3ObsServices.retryDelay);
		}

		@Override
		public String toString() {
			return "download of " + destination;
		}
	}

	public static class UploadFileStep implements  UndoableStep {

		final S3ObsServices s3Services;
		final File file;
		final String keyName;
		final String bucketName;
		UploadResult uploadResult = null;

		public UploadFileStep(S3ObsServices s3Services, File file, String keyName, String bucketName) {
			this.s3Services = s3Services;
			this.file = file;
			this.keyName = keyName;
			this.bucketName = bucketName;
		}

		@Override
		public void perform() {
			try {
				Retries.performWithRetries(() -> {
							s3Services.log(format("Uploading object %s in bucket %s", keyName, bucketName));

							final Upload upload = s3Services.s3tm.upload(bucketName, keyName, file);

							upload.addProgressListener(
									(final ProgressEvent progressEvent)
											-> LOGGER.trace(format("Uploading object %s in bucket %s: progress %s",
											keyName,
											bucketName,
											progressEvent.toString())));

							try {
								uploadResult = upload.waitForUploadResult();
								s3Services.log(format("Upload object %s in bucket %s succeeded", keyName, bucketName));
								return null;
							} catch (final InterruptedException e) {
								throw new S3ObsServiceException(bucketName, keyName,
										"Upload fails: interrupted during waiting multipart upload completion", e);
							}
						},
						format("upload to %s/%s", bucketName, keyName),
						s3Services.numRetries,
						s3Services.retryDelay);
			} catch (Exception e) {
				throw new RuntimeException(new S3SdkClientException(bucketName, keyName,
						format("Upload fails: %s", e.getMessage()), e));
			}
		}

		@Override
		public void undo() {
			try {
				s3Services.deleteFile(new DeleteObjectRequest(bucketName, keyName));
			} catch (S3ObsServiceException | S3SdkClientException e) {
				throw new RuntimeException("could not delete s3://" + bucketName + "/" + keyName);
			}
		}

		public UploadResult uploadResult() {
			return uploadResult;
		}
	}

	public static class DeleteFileStep implements UndoableStep {

		private final Path filePath;
		private final S3ObsServices s3ObsServices;

		public DeleteFileStep(File file, S3ObsServices s3ObsServices) {
			this.filePath = file.toPath();
			this.s3ObsServices = s3ObsServices;
		}

		@Override
		public void perform() {
			DeleteFile.withRetries(filePath, s3ObsServices.numRetries, s3ObsServices.retryDelay);
		}

		@Override
		public void undo() {
			//nothing to do here
		}
	}

	private static class DeleteFile {
		static void withRetries(final Path path, final int numRetries, final long retryDelay) {
			try {
				Retries.performWithRetries(() ->
				{
					if (Files.exists(path)) {
						Files.delete(path);
					}
					return null;
				}, "name", numRetries, retryDelay);
			} catch (Exception e) {
				throw new FileDeletionException(path + "could not be deleted", e);
			}
		}
	}

	static class FileDeletionException extends RuntimeException {
		public FileDeletionException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}