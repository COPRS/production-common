package fr.viveris.s1pdgs.level0.wrapper.services.s3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;

@Service
public class SessionFilesS3Services extends AbstractS3Services {

	@Autowired
	public SessionFilesS3Services(AmazonS3 s3client, @Value("${storage.buckets.edrs-sessions}") String bucketName) {
		super(s3client, bucketName);
	}
}
