package esa.s1pdgs.cpoc.obs_sdk.swift;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.StoredObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsConfigurationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.ValidArgumentAssertion;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

public class SwiftObsClient extends AbstractObsClient {
	public static final class Factory implements ObsClient.Factory {

		@Override
		public final ObsClient newObsClient(ObsConfigurationProperties config) {
	    	final AccountConfig accConf = new AccountConfig();
	    	accConf.setUsername(config.getUserId());
	        accConf.setPassword(config.getUserSecret());
	        accConf.setAuthUrl(config.getEndpoint());
	        accConf.setAuthenticationMethod(config.getAuthMethod());
	        
	        // either tenant id or tenant name must be supplied
	        if (!config.getTenantId().equals(ObsConfigurationProperties.UNDEFINED)) {
	        	accConf.setTenantId(config.getTenantId());
	        }	        
	        if (!config.getTenantName().equals(ObsConfigurationProperties.UNDEFINED)) {
	        	accConf.setTenantName(config.getTenantName());
	        }	        
	        if (!config.getEndpointRegion().equals(ObsConfigurationProperties.UNDEFINED)) {
	        	accConf.setPreferredRegion(config.getEndpointRegion());
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
				accConf.setProxyHost(host);
		        accConf.setProxyPort(port);
		        accConf.setUseProxy(true);
			}
			
//			TODO: Translate the following S3 Retry Policy setup code to some JOSS equivalent 
//			RetryPolicy retryPolicy = new RetryPolicy(
//	                new SDKCustomDefaultRetryCondition(
//	                        configuration.getInt(RETRY_POLICY_MAX_RETRIES)),
//	                new PredefinedBackoffStrategies.SDKDefaultBackoffStrategy(
//	                        configuration.getInt(RETRY_POLICY_BASE_DELAY_MS),
//	                        configuration
//	                                .getInt(RETRY_POLICY_THROTTLED_BASE_DELAY_MS),
//	                        configuration.getInt(RETRY_POLICY_MAX_BACKOFF_MS)),
//	                configuration.getInt(RETRY_POLICY_MAX_RETRIES), true);
//	        client.setRetryPolicy(retryPolicy);
			
			Account account = new AccountFactory(accConf).createAccount();
			
			if (null != account.getPreferredRegion() && !"".equals(account.getPreferredRegion())) {
				account.getAccess().setPreferredRegion(account.getPreferredRegion());
			}			
			final SwiftObsServices services = new SwiftObsServices(
					account,
					config.getMaxRetries(),
					config.getBackoffThrottledBaseDelay()
			);
			return new SwiftObsClient(config, services);
		}		
	}
	
	public static final String BACKEND_NAME = "swift";

    protected final SwiftObsServices swiftObsServices;
     
	SwiftObsClient(final ObsConfigurationProperties configuration, final SwiftObsServices swiftObsServices) {
		super(configuration);
		this.swiftObsServices = swiftObsServices;
	}

	public boolean containerExists(ProductFamily family) throws ObsServiceException {
		return swiftObsServices.containerExist(getBucketFor(family));
	}
	
	public int numberOfObjects(ProductFamily family, String prefixKey) throws SwiftSdkClientException, ObsServiceException {
		return swiftObsServices.getNbObjects(getBucketFor(family), prefixKey);
	}
    
	@Override
	public boolean exists(ObsObject object) throws SdkClientException, ObsServiceException {
		ValidArgumentAssertion.assertValidArgument(object);
		return swiftObsServices.exist(getBucketFor(object.getFamily()), object.getKey());
	}

	@Override
	public boolean prefixExists(ObsObject object) throws SdkClientException, ObsServiceException {
		ValidArgumentAssertion.assertValidArgument(object);
		return swiftObsServices.getNbObjects(
                getBucketFor(object.getFamily()),
                object.getKey()) > 0;
	}

	@Override
	public List<File> downloadObject(ObsDownloadObject object) throws SdkClientException, ObsServiceException {
		return swiftObsServices.downloadObjectsWithPrefix(
                getBucketFor(object.getFamily()),
                object.getKey(), object.getTargetDir(),
                object.isIgnoreFolders());
	}

	@Override
	public void uploadObject(ObsUploadObject object) throws SdkClientException, ObsServiceException, ObsException {
		List<String> fileList = new ArrayList<>();
        if (object.getFile().isDirectory()) {
        	fileList.addAll(swiftObsServices.uploadDirectory(
                    getBucketFor(object.getFamily()),
                    object.getKey(), object.getFile()));
        } else {
        	fileList.add(swiftObsServices.uploadFile(getBucketFor(object.getFamily()), object.getKey(), object.getFile()));
        }
        uploadMd5Sum(object, fileList);
	}
	
	private void uploadMd5Sum(final ObsObject object, final List<String> fileList) throws ObsServiceException, SwiftSdkClientException {
		File file;
		try {
			file = File.createTempFile(object.getKey(), AbstractObsClient.MD5SUM_SUFFIX);
			try(PrintWriter writer = new PrintWriter(file)) {
				for (String fileInfo : fileList) {
					writer.println(fileInfo);
				}
			}
		} catch (IOException e) {
			throw new SwiftObsServiceException(getBucketFor(object.getFamily()), object.getKey(), "Could not store md5sum temp file", e);
		}
		swiftObsServices.uploadFile(getBucketFor(object.getFamily()), object.getKey() + AbstractObsClient.MD5SUM_SUFFIX, file);
		
		try {
			Files.delete(file.toPath());
		} catch(IOException e) {
			file.deleteOnExit();
		}
	}	
	
	@Override
	public void move(ObsObject from, ProductFamily to) throws ObsException, ObsServiceException {
		ValidArgumentAssertion.assertValidArgument(from);
		ValidArgumentAssertion.assertValidArgument(to);
		swiftObsServices.move(from.getKey(), getBucketFor(from.getFamily()), getBucketFor(to));
	}

	public void createContainer(ProductFamily family) throws SwiftSdkClientException, ObsServiceException {
		swiftObsServices.createContainer(getBucketFor(family));
	}
	
	public void deleteObject(final ProductFamily family, final String key) throws SwiftSdkClientException, ObsServiceException {
		swiftObsServices.delete(getBucketFor(family), key);
	}

	@Override
	public List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(ProductFamily family, Date timeFrameBegin, Date timeFrameEnd)
			throws SdkClientException, ObsServiceException {
		ValidArgumentAssertion.assertValidArgument(family);
		ValidArgumentAssertion.assertValidArgument(timeFrameBegin);
		ValidArgumentAssertion.assertValidArgument(timeFrameEnd);

		long methodStartTime = System.currentTimeMillis();
		List<ObsObject> objectsOfTimeFrame = new ArrayList<>();
		String container = getBucketFor(family);
		Collection<StoredObject> objListing = swiftObsServices.listObjectsFromContainer(container);
		boolean possiblyTruncated = false;
		String marker = "";
		do {
			if (objListing == null || objListing.size() == 0) {
				break;
			}
			
			for (StoredObject o : objListing) {
				marker = o.getName();
				Date lastModified = o.getLastModifiedAsDate();
				if (lastModified.after(timeFrameBegin) && lastModified.before(timeFrameEnd)) {
					ObsObject obsObj = new ObsObject(family, o.getName());
					objectsOfTimeFrame.add(obsObj);
				}
			}

			possiblyTruncated = objListing.size() == swiftObsServices.MAX_RESULTS_PER_LIST;
			if (possiblyTruncated) {
				objListing = swiftObsServices.listNextBatchOfObjectsFromContainer(container, marker);
			}

		} while (possiblyTruncated);

		float methodDuration = (System.currentTimeMillis() - methodStartTime) / 1000f;
		LOGGER.debug(String.format("Time for OBS listing objects from bucket %s within time frame: %.2fs", container,
				methodDuration));

		return objectsOfTimeFrame;
	}
	
	@Override
	public Map<String, InputStream> getAllAsInputStream(ProductFamily family, String keyPrefix) throws SdkClientException {
		ValidArgumentAssertion.assertValidArgument(family);
		ValidArgumentAssertion.assertValidPrefixArgument(keyPrefix);
		final String bucket = getBucketFor(family);
		LOGGER.debug("Getting all files in bucket {} with prefix {}", bucket, keyPrefix);		
		final Map<String, InputStream> result = swiftObsServices.getAllAsInputStream(bucket, keyPrefix);
		LOGGER.debug("Found {} elements in bucket {} with prefix {}", result.size(), bucket, keyPrefix);		
		return result;
	}

	@Override
	protected Map<String,String> collectMd5Sums(ObsObject object) throws ObsException {
		try {
			return swiftObsServices.collectMd5Sums(getBucketFor(object.getFamily()), object.getKey());
		} catch (SwiftSdkClientException | ObsServiceException e) {
			throw new ObsException(object.getFamily(), object.getKey(), e);
		}
	}

	@Override
	public long size(ObsObject object) throws ObsException {
		try {
			String bucketName = getBucketFor(object.getFamily());
			/*
			 * This method is supposed to return the size of exactly one object. If more than
			 * one is returned the object is not unique and very likely not the full name of it or
			 * a directory. We are not supporting this and thus operations fails
			 */
			if (swiftObsServices.getNbObjects(bucketName, object.getKey()) != 1) {
				throw new IllegalArgumentException(String.format(
						"Unable to determinate size of object '%s' (family:%s) (is a directory or not exist?)",
						object.getKey(), object.getFamily()));
			}
			
			// return the size of the object
			return swiftObsServices.size(bucketName, object.getKey());						
		} catch (SdkClientException ex) {
			throw new ObsException(object.getFamily(), object.getKey(), ex);
		}
	}

	@Override
	public String getChecksum(ObsObject object) throws ObsException {
		try {
			String bucketName = getBucketFor(object.getFamily());
			/*
			 * This method is supposed to return the size of exactly one object. If more than
			 * one is returned the object is not unique and very likely not the full name of it or
			 * a directory. We are not supporting this and thus operations fails
			 */
			if (swiftObsServices.getNbObjects(bucketName, object.getKey()) != 1) {
				throw new IllegalArgumentException(String.format(
						"Unable to determinate checksum of object '%s' (family:%s) (is a directory or not exist?)",
						object.getKey(), object.getFamily()));
			}
			
			// return the checksum of the object
			return swiftObsServices.getChecksum(bucketName, object.getKey());
		} catch (SdkClientException ex) {
			throw new ObsException(object.getFamily(), object.getKey(), ex);
		}
	}

	@Override
	public URL createTemporaryDownloadUrl(ObsObject object, long expirationTimeInSeconds) throws ObsException {
		URL url;
		try {
			url = swiftObsServices.createTemporaryDownloadUrl(getBucketFor(object.getFamily()), object.getKey(), expirationTimeInSeconds);
		} catch (SdkClientException ex) {
			throw new ObsException(object.getFamily(), object.getKey(), ex);
		}
		final Reporting reporting = new LoggerReporting.Factory("CreateTemporaryDownloadUrl").newReporting(0);
     	reporting.intermediate(new ReportingMessage(size(object), "Created temporary download URL for username '{}' for product '{}'", "anonymous", object.getKey()));
     	return url;
	}
}
