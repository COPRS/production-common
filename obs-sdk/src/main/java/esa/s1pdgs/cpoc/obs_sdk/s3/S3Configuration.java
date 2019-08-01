package esa.s1pdgs.cpoc.obs_sdk.s3;

import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.retry.PredefinedBackoffStrategies;
import com.amazonaws.retry.RetryPolicy;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.s3.retry.SDKCustomDefaultRetryCondition;

/**
 * The amazon S3 keys
 * 
 * @author Viveris Technologies
 */
public class S3Configuration {

    /**
     * Configuration file name
     */
    public static final String CONFIG_FILE = "obs-aws-s3.properties";

    /**
     * Configuration
     */
    private final PropertiesConfiguration configuration;

    /**
     * A unique string that identified the user <code>user.id</code>
     */
    public static final String USER_ID = "user.id";

    /**
     * <code>user.secret</code>
     */
    public static final String USER_SECRET = "user.secret";

    /**
     * <code>endpoint</code>
     */
    public static final String ENDPOINT = "endpoint";

    /**
     * <code>endpoint.region</code>
     */
    public static final String ENDPOINT_REGION = "endpoint.region";

    /**
     * <code>endpoint</code>
     */
    public static final String TM_MP_UPLOAD_TH_MB =
            "transfer.manager.multipart-upload-threshold-mb";

    /**
     * <code>endpoint.region</code>
     */
    public static final String TM_MIN_UPLOAD_PART_SIZE_MB =
            "transfer.manager.minimum-upload-part-size-mb";

    /**
     * Timeout in second for shutdown a thread
     */
    public static final String TM_S_SHUTDOWN = "timeout-s.shutdown";

    /**
     * Timeout in second of a download execution
     */
    public static final String TM_S_DOWN_EXEC = "timeout-s.down-exec";

    /**
     * Timeout in second of a upload execution
     */
    public static final String TM_S_UP_EXEC = "timeout-s.up-exec";

    /**
     * Number of max retries
     */
    public static final String RETRY_POLICY_MAX_RETRIES =
            "retry-policy.condition.max-retries";

    /**
     * Time in millisecond of the delay
     */
    public static final String RETRY_POLICY_BASE_DELAY_MS =
            "retry-policy.backoff.base-delay-ms";

    /**
     * Time in millisecond of the throttled delay
     */
    public static final String RETRY_POLICY_THROTTLED_BASE_DELAY_MS =
            "retry-policy.backoff.throttled-base-delay-ms";

    /**
     * Time in millisecond of max backoff
     */
    public static final String RETRY_POLICY_MAX_BACKOFF_MS =
            "retry-policy.backoff.max-backoff-ms";

    /**
     * @throws ConfigurationException
     */
    public S3Configuration() throws ObsServiceException {
        try {
            configuration = new PropertiesConfiguration(CONFIG_FILE);
            configuration
                    .setReloadingStrategy(new FileChangedReloadingStrategy());
        } catch (ConfigurationException confEx) {
            throw new ObsServiceException(
                    "Properties extraction fails: " + confEx.getMessage(),
                    confEx);
        }
    }

    /**
     * Get a configured value in int format
     * 
     * @param key
     * @return
     * @throws ObsServiceException
     */
    public int getIntOfConfiguration(final String key)
            throws ObsServiceException {
        try {
            return configuration.getInt(key);
        } catch (ConversionException convE) {
            throw new ObsServiceException("Cannot get configuration value of "
                    + key + ": " + convE.getMessage(), convE);
        }
    }
    
    /**
     * Get a configured value as String
     * 
     * @param key
     * @return
     * @throws ObsServiceException
     */
    public String getStringOfConfiguration(final String key) throws ObsServiceException {
    	return configuration.getString(key);
    }

    /**
     * Get the name of the bucket to use according the OBS family
     * 
     * @param family
     * @return
     * @throws ObsServiceException
     */
    public String getBucketForFamily(final ProductFamily family)
            throws ObsServiceException {    	
    	return configuration.getString("bucket." + family.toString().toLowerCase().replace('_', '-'));
    }

    /**
     * Build the default Amazon s3 client
     * 
     * @return
     */
    public AmazonS3 defaultS3Client() {
        // Credentials
        BasicAWSCredentials awsCreds =
                new BasicAWSCredentials(configuration.getString(USER_ID),
                        configuration.getString(USER_SECRET));

        // Client configuration (protocol and retry policy)
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);
       
        // set proxy if defined in environmental
        final String proxyConfig = System.getenv("https_proxy");
        
		if (proxyConfig != null && !proxyConfig.equals("")) {
			final String removedProtocol = proxyConfig
					.replaceAll(Pattern.quote("http://"), "")
					.replaceAll(Pattern.quote("https://"), "")
					.replaceAll(Pattern.quote("/"), ""); // remove trailing slash

			final String host = removedProtocol.substring(0, removedProtocol.indexOf(':'));
			final int port = Integer.parseInt(removedProtocol.substring(removedProtocol.indexOf(':') + 1, 
					removedProtocol.length()));
			clientConfig.setProxyHost(host);
	        clientConfig.setProxyPort(port);			
		}
       
        RetryPolicy retryPolicy = new RetryPolicy(
                new SDKCustomDefaultRetryCondition(
                        configuration.getInt(RETRY_POLICY_MAX_RETRIES)),
                new PredefinedBackoffStrategies.SDKDefaultBackoffStrategy(
                        configuration.getInt(RETRY_POLICY_BASE_DELAY_MS),
                        configuration
                                .getInt(RETRY_POLICY_THROTTLED_BASE_DELAY_MS),
                        configuration.getInt(RETRY_POLICY_MAX_BACKOFF_MS)),
                configuration.getInt(RETRY_POLICY_MAX_RETRIES), true);
        clientConfig.setRetryPolicy(retryPolicy);

        // Amazon s3 client
        return AmazonS3ClientBuilder.standard()
                .withClientConfiguration(clientConfig)
                .withEndpointConfiguration(new EndpointConfiguration(
                        configuration.getString(ENDPOINT),
                        configuration.getString(ENDPOINT_REGION)))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    public TransferManager defaultS3TransferManager(AmazonS3 client) {
        return TransferManagerBuilder.standard()
                .withMinimumUploadPartSize(
                        configuration.getLong(TM_MIN_UPLOAD_PART_SIZE_MB) * 1024 * 1024)
                .withMultipartUploadThreshold(
                        configuration.getLong(TM_MP_UPLOAD_TH_MB) * 1024 * 1024)
                .withS3Client(client).build();
    }
}
