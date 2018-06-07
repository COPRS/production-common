package fr.viveris.s1pdgs.mdcatalog.services.s3;

import java.io.File;
import java.io.IOException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;

import fr.viveris.s1pdgs.mdcatalog.model.exception.ObjectStorageException;

/**
 * Implementation of the service for accessing to the object storage with the
 * API Amazon S3
 * 
 * @author Cyrielle Gailliard
 *
 */
public class AbstractS3ObsServices implements S3Services {

	/**
	 * Amazon S3 client
	 */
	protected final AmazonS3 s3client;

	/**
	 * Bucket name
	 */
	protected final String bucketName;

	/**
	 * Constructor
	 * 
	 * @param s3client
	 * @param bucketName
	 */
	public AbstractS3ObsServices(final AmazonS3 s3client, final String bucketName) {
		this.s3client = s3client;
		this.bucketName = bucketName;
	}

	/**
	 * 
	 */
	@Override
	public void uploadFile(final String keyName, final File uploadFile) throws ObjectStorageException {
		try {
			s3client.putObject(bucketName, keyName, uploadFile);
		} catch (AmazonServiceException sce) {
			throw new ObjectStorageException(keyName, keyName, bucketName, sce);
		} catch (SdkClientException sce) {
			throw new ObjectStorageException(keyName, keyName, bucketName, sce);
		}

	}

	/**
	 * 
	 */
	@Override
	public boolean exist(final String keyName) throws ObjectStorageException {
		try {
			return s3client.doesObjectExist(bucketName, keyName);
		} catch (AmazonServiceException sce) {
			throw new ObjectStorageException(keyName, keyName, bucketName, sce);
		} catch (SdkClientException sce) {
			throw new ObjectStorageException(keyName, keyName, bucketName, sce);
		}
	}

	/**
	 * 
	 */
	@Override
	public File getFile(String keyName, String expectedFilePath) throws ObjectStorageException {
		try {
			File f = new File(expectedFilePath);
			if (f.getParentFile()!=null) {
				f.getParentFile().mkdirs();
			}
			f.createNewFile();
			s3client.getObject(new GetObjectRequest(bucketName, keyName), f);
			return f;
		} catch (SdkClientException | IOException sce) {
			throw new ObjectStorageException(keyName, keyName, bucketName, sce);
		}
	}

	/**
	 * @return the bucketName
	 */
	public String getBucketName() {
		return bucketName;
	}

}
