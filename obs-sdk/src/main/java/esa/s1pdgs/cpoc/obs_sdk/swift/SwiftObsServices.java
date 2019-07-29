package esa.s1pdgs.cpoc.obs_sdk.swift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;

import com.amazonaws.services.s3.model.ObjectListing;

import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsServices;

public class SwiftObsServices {

	/**
     * Logger
     */
    private static final Log LOGGER = LogFactory.getLog(SwiftObsServices.class);

    /**
     * Swift client
     */
    protected final Account client;
    
    public SwiftObsServices(Account client) {
    	this.client = client;
	}
    
	/**
     * Check if object with such key in container exists
     * 
     * @param containerName
     * @param keyName
     * @return
	 * @throws SwiftSdkClientException
     */
	public boolean exist(final String containerName, final String keyName) {
		Container container = client.getContainer(containerName);
		return container.exists() && container.getObject(keyName).exists();
	}

	/**
     * Get the number of objects in the container whose key matches with prefix
     * 
     * @param containerName
     * @param prefixKey
     * @return
	 * @throws SwiftSdkClientException
     */
	public int getNbObjects(String containerName, String prefixKey) {
		Container container = client.getContainer(containerName);
		
		
		// TODO
		return 0;
	}

	/**
     * Download objects of the given container with a key matching the prefix
     * 
     * @param containerName
     * @param prefixKey
     * @param directoryPath
     * @param ignoreFolders
     * @return the number of download objects
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
     */
	public int downloadObjectsWithPrefix(String containerName, String prefixKey, String directoryPath, boolean ignoreFolders) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param containerName
	 * @param keyName
	 * @param uploadDirectory
	 * @return
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
	 */
	public void uploadFile(String containerName, String keyName, Object uploadDirectory) {
		// TODO Auto-generated method stub
		
	}

	/**
     * @param containerName
     * @param keyName
     * @param uploadFile
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
     */
	public int uploadDirectory(String containerName, String keyName, Object uploadFile) {
		// TODO Auto-generated method stub
		return 0;
		
	}

	/**
	 * @param containerName
	 * @return
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
	 */
	public ObjectListing listObjectsFromContainer(String containerName) {
		// TODO Auto-generated method stub
		return null;
		
	}

	/**
	 * @param containerName
	 * @param previousObjectListing
	 * @return
	 * @throws SwiftObsServiceException
	 * @throws SwiftSdkClientException
	 */
	public ObjectListing listNextBatchOfObjectsFromContainer(String containerName, ObjectListing previousObjectListing) {
		// TODO Auto-generated method stub
		return null;
		
	}

}
