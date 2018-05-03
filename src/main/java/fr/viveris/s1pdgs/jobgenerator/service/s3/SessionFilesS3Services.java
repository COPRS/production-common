package fr.viveris.s1pdgs.jobgenerator.service.s3;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import fr.viveris.s1pdgs.jobgenerator.exception.ObsS3Exception;

@Service
public class SessionFilesS3Services implements S3Services {

	private static final Logger LOGGER = LogManager.getLogger(SessionFilesS3Services.class);

	@Autowired
	private AmazonS3 s3client;

	@Value("${storage.buckets.edrs-sessions}")
	private String bucketName;

	@Override
	public void downloadFile(String keyName, File output) throws ObsS3Exception {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Downloading object {} from bucket {}", keyName, bucketName);
			}
			s3client.getObject(new GetObjectRequest(bucketName, keyName), output);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Download object {} from bucket {} succeeded", keyName, bucketName);
			}
		} catch (AmazonServiceException ase) {
			throw new ObsS3Exception(keyName, bucketName, ase);
		} catch (AmazonClientException sce) {
			throw new ObsS3Exception(keyName, bucketName, sce);
		}
	}
	
	@Override
	public File getFile(String keyName, String expectedFilePath) throws ObsS3Exception {
		try {
			File f = new File(expectedFilePath);
			f.createNewFile();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Downloading object {} from bucket {}", keyName, bucketName);
			}
			s3client.getObject(new GetObjectRequest(bucketName, keyName), f);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Download object {} from bucket {} succeeded", keyName, bucketName);
			}
			return f;
		} catch (SdkClientException sce) {
			throw new ObsS3Exception(keyName, bucketName, sce);
		} catch (IOException e) {
			throw new ObsS3Exception(keyName, bucketName, e);
		}
	}

	@Override
	public void uploadFile(String keyName, File uploadFile) throws ObsS3Exception {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Uploading object {} in bucket {}", keyName, bucketName);
			}
			s3client.putObject(new PutObjectRequest(bucketName, keyName, uploadFile));
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Upload object {} in bucket {} succeeded", keyName, bucketName);
			}
		} catch (SdkClientException sce) {
			throw new ObsS3Exception(keyName, bucketName, sce);
		}

	}

	@Override
	public boolean exist(String keyName) throws ObsS3Exception {
		try {
			return s3client.doesObjectExist(bucketName, keyName);
		} catch (SdkClientException sce) {
			throw new ObsS3Exception(keyName, bucketName, sce);
		}
	}

}
