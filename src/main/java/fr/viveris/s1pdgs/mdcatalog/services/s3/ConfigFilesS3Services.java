package fr.viveris.s1pdgs.mdcatalog.services.s3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;

/**
 * Implementation of OBS services for the auxiliary files
 * @author Cyrielle Gailliard
 *
 */
@Service
public class ConfigFilesS3Services extends AbstractS3ObsServices {
	
	/**
	 * Constructor
	 * 
	 * @param s3client
	 * @param bucketName
	 */
	@Autowired
	public ConfigFilesS3Services(final AmazonS3 s3client,
			@Value("${storage.buckets.auxiliary-files}") final String bucketName) {
		super(s3client, bucketName);
	}
	
}
