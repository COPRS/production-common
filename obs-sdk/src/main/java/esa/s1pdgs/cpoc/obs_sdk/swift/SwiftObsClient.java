package esa.s1pdgs.cpoc.obs_sdk.swift;

import java.io.InputStream;
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

	public boolean doesContainerExist(ProductFamily family) throws ObsServiceException {
		return swiftObsServices.containerExist(configuration.getContainerForFamily(family));
	}
	
	public int numberOfObjects(ProductFamily family, String prefixKey) throws SwiftSdkClientException, ObsServiceException {
		return swiftObsServices.getNbObjects(configuration.getContainerForFamily(family), prefixKey);
	}
    
	@Override
	public boolean doesObjectExist(ObsObject object) throws SdkClientException, ObsServiceException {
		return swiftObsServices.exist(configuration.getContainerForFamily(object.getFamily()), object.getKey());
	}

	@Override
	public boolean doesPrefixExist(ObsObject object) throws SdkClientException, ObsServiceException {
		return swiftObsServices.getNbObjects(
                configuration.getContainerForFamily(object.getFamily()),
                object.getKey()) > 0;
	}

	@Override
	public int downloadObject(ObsDownloadObject object) throws SdkClientException, ObsServiceException {
		return swiftObsServices.downloadObjectsWithPrefix(
                configuration.getContainerForFamily(object.getFamily()),
                object.getKey(), object.getTargetDir(),
                object.isIgnoreFolders());
	}

	@Override
	public int uploadObject(ObsUploadObject object) throws SdkClientException, ObsServiceException {
		int nbUpload;
        if (object.getFile().isDirectory()) {
            nbUpload = swiftObsServices.uploadDirectory(
                    configuration.getContainerForFamily(object.getFamily()),
                    object.getKey(), object.getFile());
        } else {
            swiftObsServices.uploadFile(
                    configuration.getContainerForFamily(object.getFamily()),
                    object.getKey(), object.getFile());
            nbUpload = 1;
        }
        return nbUpload;
	}
	
	public void createContainer(ProductFamily family) throws SwiftSdkClientException, ObsServiceException {
		swiftObsServices.createContainer(configuration.getContainerForFamily(family));
	}
	
	public void deleteObject(final ProductFamily family, final String key) throws SwiftSdkClientException, ObsServiceException {
		swiftObsServices.delete(configuration.getContainerForFamily(family), key);
	}

	/**
     * @see ObsClient#getShutdownTimeoutS()
     */
    @Override
    public int getShutdownTimeoutS() throws ObsServiceException {
        return configuration
                .getIntOfConfiguration(SwiftConfiguration.TM_S_SHUTDOWN);
    }

    /**
     * @see ObsClient#getDownloadExecutionTimeoutS()
     */
    @Override
    public int getDownloadExecutionTimeoutS() throws ObsServiceException {
        return configuration
                .getIntOfConfiguration(SwiftConfiguration.TM_S_DOWN_EXEC);
    }

    /**
     * @see ObsClient#getUploadExecutionTimeoutS()
     */
    @Override
    public int getUploadExecutionTimeoutS() throws ObsServiceException {
        return configuration
                .getIntOfConfiguration(SwiftConfiguration.TM_S_UP_EXEC);
    }

	@Override
	public List<ObsObject> getListOfObjectsOfTimeFrameOfFamily(Date timeFrameBegin, Date timeFrameEnd,
			ProductFamily family) throws SdkClientException, ObsServiceException {
		long methodStartTime = System.currentTimeMillis();

		List<ObsObject> objectsOfTimeFrame = new ArrayList<>();
		String container = configuration.getContainerForFamily(family);
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
					ObsObject obsObj = new ObsObject(o.getName(), family);
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
		final String bucket = configuration.getContainerForFamily(family);
		LOGGER.debug("Getting all files in bucket {} with prefix {}", bucket, keyPrefix);		
		final Map<String, InputStream> result = swiftObsServices.getAllAsInputStream(bucket, keyPrefix);
		LOGGER.debug("Found {} elements in bucket {} with prefix {}", result.size(), bucket, keyPrefix);		
		return result;
	}

}
