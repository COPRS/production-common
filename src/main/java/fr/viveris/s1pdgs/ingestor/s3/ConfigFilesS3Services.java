package fr.viveris.s1pdgs.ingestor.s3;

import java.io.File;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import fr.viveris.s1pdgs.ingestor.s3.exceptions.ObjectStorageException;

@Service
public class ConfigFilesS3Services implements S3Services {
	private static final Logger LOGGER = LogManager.getLogger(ConfigFilesS3Services.class);

	@Autowired
	private AmazonS3 s3client;

	@Value("${storage.buckets.auxiliary-files}")
	private String bucketName;

	@Override
	public void downloadFile(String keyName) throws ObjectStorageException {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Downloading object {} from bucket {}", keyName, bucketName);
			}
			s3client.getObject(new GetObjectRequest(bucketName, keyName));
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Download object {} from bucket {} succeeded", keyName, bucketName);
			}
		} catch (SdkClientException sce) {
			throw new ObjectStorageException(keyName, keyName, bucketName, sce);
		}

	}

	@Override
	public void uploadFile(String keyName, File uploadFile) throws ObjectStorageException {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Uploading object {} in bucket {}", keyName, bucketName);
			}
			s3client.putObject(new PutObjectRequest(bucketName, keyName, uploadFile));
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Upload object {} in bucket {} succeeded", keyName, bucketName);
			}
		} catch (SdkClientException sce) {
			throw new ObjectStorageException(keyName, keyName, bucketName, sce);
		}

	}

	@Override
	public boolean exist(String keyName) throws ObjectStorageException {
		try {
			return s3client.doesObjectExist(bucketName, keyName);
		} catch (SdkClientException sce) {
			throw new ObjectStorageException(keyName, keyName, bucketName, sce);
		}
	}
}
