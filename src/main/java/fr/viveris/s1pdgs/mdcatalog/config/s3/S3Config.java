package fr.viveris.s1pdgs.mdcatalog.config.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * Configuration for accessing to object storage with Amazon S3 API
 * @author Cyrielle Gailliard
 *
 */
@Configuration
public class S3Config {

	// FTP server configuration
	// -------------------------------------
	/**
	 * Access key
	 */
	@Value("${storage.user.id}")
	private String awsId;
	/**
	 * Access secret
	 */
	@Value("${storage.user.secret}")
	private String awsKey;
	/**
	 * Access endpoint
	 */
	@Value("${storage.endpoint}")
	private String endpoint;
	/**
	 * Access region
	 */
	@Value("${storage.region}")
	private String region;
 
	/**
	 * Amazon S3 client
	 * @return
	 */
	@Bean
	public AmazonS3 s3client() {
		
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsId, awsKey);
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                				.withClientConfiguration(clientConfig)
                				.withEndpointConfiguration(new EndpointConfiguration(endpoint, region))
		                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
		                        .build();
		return s3Client;
	}
}