package esa.s1pdgs.cpoc.obs_sdk.swift;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.StoredObject;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

public class SwiftObsClient extends AbstractObsClient {

    protected final SwiftConfiguration configuration;
    protected final SwiftObsServices swiftObsServices;
    
    private static final Logger LOGGER =
            LogManager.getLogger(SwiftObsClient.class);
        
	/**
     * Default constructor
     * 
     * @throws ObsServiceException
     */
    public SwiftObsClient() throws ObsServiceException {
        super();
        configuration = new SwiftConfiguration();
        Account client = configuration.defaultClient();
        swiftObsServices = new SwiftObsServices(client,
        		configuration.getIntOfConfiguration("retry-policy.condition.max-retries"),
        		configuration.getIntOfConfiguration("retry-policy.backoff.throttled-base-delay-ms"));
    }
    
    @Override
	protected String getBucketFor(ProductFamily family) throws ObsServiceException {
		return configuration.getContainerForFamily(family);
	}

	/**
     * Constructor using fields
     * 
     * @param configuration
     * @param swiftObsServices
     * @throws ObsServiceException
     */
    protected SwiftObsClient(final SwiftConfiguration configuration,
            final SwiftObsServices swiftObsServices) throws ObsServiceException {
        super();
        this.configuration = configuration;
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
		return swiftObsServices.exist(getBucketFor(object.getFamily()), object.getKey());
	}

	@Override
	public boolean prefixExists(ObsObject object) throws SdkClientException, ObsServiceException {
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
        if (object.getFile().isDirectory()) {
        	List<String> fileList = new ArrayList<>();
        	fileList.addAll(swiftObsServices.uploadDirectory(
                    getBucketFor(object.getFamily()),
                    object.getKey(), object.getFile()));
			if (object.getFamily().equals(ProductFamily.EDRS_SESSION)) {
				// TODO check DSIB file list, upload md5sum when product is complete
			} else {
				uploadMd5Sum(object, fileList);
			}

        } else {
        	swiftObsServices.uploadFile(getBucketFor(object.getFamily()), object.getKey(), object.getFile());
        }
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
			throw new SwiftObsServiceException(configuration.getContainerForFamily(object.getFamily()), object.getKey(), "Could not store md5sum temp file", e);
		}
		swiftObsServices.uploadFile(getBucketFor(object.getFamily()), object.getKey() + AbstractObsClient.MD5SUM_SUFFIX, file);
	}
	
	public void createContainer(ProductFamily family) throws SwiftSdkClientException, ObsServiceException {
		swiftObsServices.createContainer(getBucketFor(family));
	}
	
	public void deleteObject(final ProductFamily family, final String key) throws SwiftSdkClientException, ObsServiceException {
		swiftObsServices.delete(getBucketFor(family), key);
	}

	/**
     * @see ObsClient#getShutdownTimeoutS()
     */
    public int getShutdownTimeoutS() throws ObsServiceException {
        return configuration
                .getIntOfConfiguration(SwiftConfiguration.TM_S_SHUTDOWN);
    }

    /**
     * @see ObsClient#getDownloadExecutionTimeoutS()
     */
    public int getDownloadExecutionTimeoutS() throws ObsServiceException {
        return configuration
                .getIntOfConfiguration(SwiftConfiguration.TM_S_DOWN_EXEC);
    }

    /**
     * @see ObsClient#getUploadExecutionTimeoutS()
     */
    public int getUploadExecutionTimeoutS() throws ObsServiceException {
        return configuration
                .getIntOfConfiguration(SwiftConfiguration.TM_S_UP_EXEC);
    }

	@Override
	public List<ObsObject> getObsObjectsOfFamilyWithinTimeFrame(ProductFamily family, Date timeFrameBegin, Date timeFrameEnd)
			throws SdkClientException, ObsServiceException {
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
		final String bucket = getBucketFor(family);
		LOGGER.debug("Getting all files in bucket {} with prefix {}", bucket, keyPrefix);		
		final Map<String, InputStream> result = swiftObsServices.getAllAsInputStream(bucket, keyPrefix);
		LOGGER.debug("Found {} elements in bucket {} with prefix {}", result.size(), bucket, keyPrefix);		
		return result;
	}

	@Override
	public Map<String,String> collectMd5Sums(ObsObject object) throws ObsException {
		try {
			return swiftObsServices.collectMd5Sums(getBucketFor(object.getFamily()), object.getKey());
		} catch (SwiftSdkClientException | ObsServiceException e) {
			throw new ObsException(object.getFamily(), object.getKey(), e);
		}
	}
}
