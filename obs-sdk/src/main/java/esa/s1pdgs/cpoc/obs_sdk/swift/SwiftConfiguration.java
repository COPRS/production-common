package esa.s1pdgs.cpoc.obs_sdk.swift;

import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.model.Account;

import esa.s1pdgs.cpoc.obs_sdk.ObsFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;

public class SwiftConfiguration {
    
    /**
     * Logger
     */
    private static final Log LOGGER = LogFactory.getLog(SwiftConfiguration.class);
    
	/**
	 * Configuration file name
	 */
	public static final String CONFIG_FILE = "obs-swift.properties";

	/**
     * Configuration
     */
    private final PropertiesConfiguration configuration;
    
    /**
     * A unique string that identified the user <code>obs.swift.user.name</code>
     */
    public static final String USER_NAME = "obs.swift.user.name";

    /**
     * <code>obs.swift.user.password</code>
     */
    public static final String USER_PASSWORD = "obs.swift.user.password";

    /**
     * A unique string that identifies the tenant <code>obs.swift.tenant.id</code>
     */
    public static final String TENANT_ID = "obs.swift.tenant.id";
    
    /**
     * A unique string that identified the tenant <code>obs.swift.tenant.name</code>
     */
    public static final String TENANT_NAME = "obs.swift.tenant.name";
    
    /**
     * <code>obs.swift.auth-url</code>
     */
    public static final String AUTH_URL = "obs.swift.auth-url";
    
    /**
     * <code>obs.swift.authentication-method</code>
     */
    public static final String AUTHENTICATION_METHOD = "obs.swift.authentication-method";

    /**
     * <code>obs.swift.region.name</code>
     */
    public static final String REGION_NAME = "obs.swift.region.name";

    /**
     * Name of the bucket dedicated to the family
     * {@link ObsFamily#AUXILIARY_FILE}
     */
    public static final String BCK_AUX_FILES = "bucket.auxiliary-files";

    /**
     * Name of the bucket dedicated to the family {@link ObsFamily#EDRS_SESSION}
     */
    public static final String BCK_EDRS_SESSIONS = "bucket.edrs-sessions";

    /**
     * Name of the bucket dedicated to the family {@link ObsFamily#L0_SLICE}
     */
    public static final String BCK_L0_SLICES = "bucket.l0-slices";

    /**
     * Name of the bucket dedicated to the family {@link ObsFamily#L0_ACN}
     */
    public static final String BCK_L0_ACNS = "bucket.l0-acns";

    /**
     * Name of the bucket dedicated to the family {@link ObsFamily#L1_SLICE}
     */
    public static final String BCK_L1_SLICES = "bucket.l1-slices";

    /**
     * Name of the bucket dedicated to the family {@link ObsFamily#L1_ACN}
     */
    public static final String BCK_L1_ACNS = "bucket.l1-acns";

    /**
     * Name of the bucket dedicated to the family {@link ObsFamily#L0_SEGMENT}
     */
    public static final String BCK_L0_SEGMENT = "bucket.l0-segments";

    /**
     * Name of the bucket dedicated to the family {@link ObsFamily#L0_BLANK}
     */
    public static final String BCK_L0_BLANK = "bucket.l0-blanks";
    
    /**
     * Name of the bucket dedicated to the family {@link ObsFamily#L2_SLICE}
     */
    public static final String BCK_L2_SLICES = "bucket.l2-slices";
    /**
     * Name of the bucket dedicated to the family {@link ObsFamily#L2_ACN}
     */
    public static final String BCK_L2_ACNS = "bucket.l2-acns";
    
    public static final String BCK_AUX_FILE_ZIP = "bucket.auxiliary-files-zip";
    public static final String BCK_L0_SEGMENT_ZIP = "bucket.l0-segments-zip";
    
    public static final String BCK_L0_ACNS_ZIP = "bucket.l0-acns-zip";
    public static final String BCK_L1_ACNS_ZIP = "bucket.l1-acns-zip";
    public static final String BCK_L2_ACNS_ZIP = "bucket.l2-acns-zip";
    
    public static final String BCK_L0_SLICES_ZIP = "bucket.l0-slices-zip";
    public static final String BCK_L1_SLICES_ZIP = "bucket.l1-slices-zip";
    public static final String BCK_L2_SLICES_ZIP = "bucket.l2-slices-zip";
    public static final String BCK_L0_BLANK_ZIP = "bucket.l0-blanks-zip";
    
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
    public SwiftConfiguration() throws ObsServiceException {
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
     * Get the name of the container to use according the OBS family
     * 
     * @param family
     * @return
     * @throws ObsServiceException
     */
    public String getContainerForFamily(final ObsFamily family)
            throws ObsServiceException {
        String bucket;
        switch (family) {
            case AUXILIARY_FILE:
                bucket = configuration.getString(BCK_AUX_FILES);
                break;
            case EDRS_SESSION:
                bucket = configuration.getString(BCK_EDRS_SESSIONS);
                break;
            case L0_ACN:
                bucket = configuration.getString(BCK_L0_ACNS);
                break;
            case L0_SLICE:
                bucket = configuration.getString(BCK_L0_SLICES);
                break;
            case L1_ACN:
                bucket = configuration.getString(BCK_L1_ACNS);
                break;
            case L1_SLICE:
                bucket = configuration.getString(BCK_L1_SLICES);
                break;
            case L0_SEGMENT:
                bucket = configuration.getString(BCK_L0_SEGMENT);
                break;
            case L0_BLANK:
                bucket = configuration.getString(BCK_L0_BLANK);
                break;
            case L2_SLICE:
            	bucket = configuration.getString(BCK_L2_SLICES);
            	break;
            case L2_ACN:
            	bucket = configuration.getString(BCK_L2_ACNS);
            	break;
            // ZIP Buckets
            case L0_ACN_ZIP:
            	bucket = configuration.getString(BCK_L0_ACNS_ZIP);
            	break;
            case L1_ACN_ZIP:
            	bucket = configuration.getString(BCK_L1_ACNS_ZIP);
            	break;
            case L2_ACN_ZIP:
            	bucket = configuration.getString(BCK_L2_ACNS_ZIP);
            	break;
            case L0_SLICE_ZIP:
            	bucket = configuration.getString(BCK_L0_SLICES_ZIP);
            	break;
            case L1_SLICE_ZIP:
            	bucket = configuration.getString(BCK_L1_SLICES_ZIP);
            	break;
            case L2_SLICE_ZIP:
            	bucket = configuration.getString(BCK_L2_SLICES_ZIP);
            	break;
            case L0_SEGMENT_ZIP:
            	bucket = configuration.getString(BCK_L0_SEGMENT_ZIP);
            	break;
            case AUXILIARY_FILE_ZIP:
            	bucket = configuration.getString(BCK_AUX_FILE_ZIP);
            	break;
            case L0_BLANK_ZIP:
                bucket = configuration.getString(BCK_L0_BLANK_ZIP);
                break;            	
            default:
                throw new ObsServiceException(
                        "Invalid object storage family " + family);
        }
        return bucket;
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
     * Build the default swift client
     * 
     * @return
     */
    public Account defaultClient() {
    	AccountConfig config = new AccountConfig();
    	config.setUsername(configuration.getString(USER_NAME));
        config.setPassword(configuration.getString(USER_PASSWORD));
        config.setAuthUrl(configuration.getString(AUTH_URL));
        
        if (null == configuration.getString(AUTHENTICATION_METHOD)) {
        	config.setAuthenticationMethod(AuthenticationMethod.KEYSTONE);
        } else {
        	config.setAuthenticationMethod(configuration.getString(AUTHENTICATION_METHOD));
        }
        
        // either tenant id or tenant name must be supplied
        if (null != configuration.getString(TENANT_ID) && !"".equals(configuration.getString(TENANT_ID))) {
        	config.setTenantId(configuration.getString(TENANT_ID));
        }
        
        if (null != configuration.getString(TENANT_NAME) && !"".equals(configuration.getString(TENANT_NAME))) {
        	config.setTenantName(configuration.getString(TENANT_NAME));
        }

        if (null != configuration.getString(REGION_NAME) && !"".equals(configuration.getString(REGION_NAME))) {
        	config.setPreferredRegion(configuration.getString(REGION_NAME));
        }
        
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
			config.setProxyHost(host);
	        config.setProxyPort(port);
	        config.setUseProxy(true);
		}
		
//		TODO: Translate the following S3 Retry Policy setup code to some JOSS equivalent 
//		RetryPolicy retryPolicy = new RetryPolicy(
//                new SDKCustomDefaultRetryCondition(
//                        configuration.getInt(RETRY_POLICY_MAX_RETRIES)),
//                new PredefinedBackoffStrategies.SDKDefaultBackoffStrategy(
//                        configuration.getInt(RETRY_POLICY_BASE_DELAY_MS),
//                        configuration
//                                .getInt(RETRY_POLICY_THROTTLED_BASE_DELAY_MS),
//                        configuration.getInt(RETRY_POLICY_MAX_BACKOFF_MS)),
//                configuration.getInt(RETRY_POLICY_MAX_RETRIES), true);
//        client.setRetryPolicy(retryPolicy);
		
		Account account = new AccountFactory(config).createAccount();
		
		if (null != account.getPreferredRegion() && !"".equals(account.getPreferredRegion())) {
			account.getAccess().setPreferredRegion(account.getPreferredRegion());
		}
		return account;
    }
	
}
