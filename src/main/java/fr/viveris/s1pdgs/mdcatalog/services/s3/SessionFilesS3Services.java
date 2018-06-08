package fr.viveris.s1pdgs.mdcatalog.services.s3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;

/**
 * Implementation of OBS services for the EDRS session files
 * @author Cyrielle Gailliard
 *
 */
@Service
public class SessionFilesS3Services extends AbstractS3ObsServices {

	/**
	 * Constructor
	 * 
	 * @param s3client
	 * @param bucketName
	 */
	@Autowired
	public SessionFilesS3Services(final AmazonS3 s3client,
			@Value("${storage.buckets.session-files}") final String bucketName) {
		super(s3client, bucketName);
	}

}
