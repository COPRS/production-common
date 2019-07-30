package esa.s1pdgs.cpoc.obs_sdk.swift;

import java.util.Date;
import java.util.List;

import org.javaswift.joss.model.Account;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.obs_sdk.AbstractObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsDownloadObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsFamily;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.ObsUploadObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

public class SwiftObsClient extends AbstractObsClient {

    protected final SwiftConfiguration configuration;
    protected final SwiftObsServices swiftObsServices;
    
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
    
	@Override
	public boolean doesObjectExist(ObsObject object) throws SdkClientException, ObsServiceException {
		return swiftObsServices.exist(configuration.getContainerForFamily(object.getFamily()), object.getKey());
	}

	@Override
	public boolean doesPrefixExist(ObsObject object) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		//return false;
	}

	@Override
	public int downloadObject(ObsDownloadObject object) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		//return 0;
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
	
	public void deleteObject(final ProductFamily family, final String key) throws SwiftSdkClientException, ObsServiceException {
		swiftObsServices.delete(configuration.getContainerForFamily(getObsFamily(family)), key);
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
			ObsFamily obsFamily) throws SdkClientException, ObsServiceException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		//return null;
	}

}
