package fr.viveris.s1pdgs.jobgenerator.service.s3;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import fr.viveris.s1pdgs.jobgenerator.exception.ObjectStorageException;

@Service
public class SessionFilesS3Services implements S3Services {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionFilesS3Services.class);

	@Autowired
	private AmazonS3 s3client;

	@Value("${storage.buckets.edrs-sessions}")
	private String bucketName;

	@Override
	public void downloadFile(String keyName, File output) throws ObjectStorageException {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Downloading object {} from bucket {}", keyName, bucketName);
			}
			s3client.getObject(new GetObjectRequest(bucketName, keyName), output);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Download object {} from bucket {} succeeded", keyName, bucketName);
			}
		} catch (SdkClientException sce) {
			throw new ObjectStorageException(keyName, keyName, bucketName, sce);
		}
	}
	
	@Override
	public File getFile(String keyName, String expectedFilePath) throws ObjectStorageException {
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
		} catch (SdkClientException | IOException sce) {
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
	
	/**
	 *     private PutObjectResult upload(InputStream inputStream, String uploadKey) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, uploadKey, inputStream, new ObjectMetadata());

        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);

        PutObjectResult putObjectResult = amazonS3.putObject(putObjectRequest);

        IOUtils.closeQuietly(inputStream);

        return putObjectResult;
    }

    public List<PutObjectResult> upload(MultipartFile[] multipartFiles) {
        List<PutObjectResult> putObjectResults = new ArrayList<>();

        Arrays.stream(multipartFiles)
                .filter(multipartFile -> !StringUtils.isEmpty(multipartFile.getOriginalFilename()))
                .forEach(multipartFile -> {
                    try {
                        putObjectResults.add(upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return putObjectResults;
    }

    public ResponseEntity<byte[]> download(String key) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);

        S3Object s3Object = amazonS3.getObject(getObjectRequest);

        S3ObjectInputStream objectInputStream = s3Object.getObjectContent();

        byte[] bytes = IOUtils.toByteArray(objectInputStream);

        String fileName = URLEncoder.encode(key, "UTF-8").replaceAll("\\+", "%20");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentLength(bytes.length);
        httpHeaders.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }
    }**/

}
