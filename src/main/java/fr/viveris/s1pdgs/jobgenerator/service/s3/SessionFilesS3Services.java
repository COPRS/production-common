package fr.viveris.s1pdgs.jobgenerator.service.s3;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;

import fr.viveris.s1pdgs.jobgenerator.exception.ObsS3Exception;

/**
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class SessionFilesS3Services implements S3Services {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(SessionFilesS3Services.class);

	/**
	 * Client to access to obs via S3 API
	 */
	private final AmazonS3 s3client;

	/**
	 * Name of the bucket
	 */
	private final String bucketName;

	/**
	 * 
	 * @param s3client
	 * @param bucketName
	 */
	@Autowired
	public SessionFilesS3Services(final AmazonS3 s3client,
			@Value("${storage.buckets.edrs-sessions}") final String bucketName) {
		this.s3client = s3client;
		this.bucketName = bucketName;
	}

	/**
	 * 
	 */
	@Override
	public File getFile(final String keyName, final String expectedFilePath) throws ObsS3Exception {
		try {
			File file = new File(expectedFilePath);
			file.createNewFile();

			LOGGER.debug("Downloading object {} from bucket {}", keyName, bucketName);
			s3client.getObject(new GetObjectRequest(bucketName, keyName), file);

			LOGGER.debug("Download object {} from bucket {} succeeded", keyName, bucketName);
			return file;
		} catch (SdkClientException sce) {
			throw new ObsS3Exception(keyName, bucketName, sce);
		} catch (IOException e) {
			throw new ObsS3Exception(keyName, bucketName, e);
		}
	}

}
