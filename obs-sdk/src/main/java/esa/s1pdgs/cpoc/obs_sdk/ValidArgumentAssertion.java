package esa.s1pdgs.cpoc.obs_sdk;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class ValidArgumentAssertion {

	public static void assertValidArgument(Collection<? extends ObsObject> collection) {
    	if (null == collection) {
    		throw new IllegalArgumentException("Invalid object: null");
    	}
    	if (collection.size() == 0) {
    		throw new IllegalArgumentException("Invalid collection (empty)");
    	}
    	collection.stream().forEach(e -> assertValidArgument(e));
	}

	public static void assertValidArgument(ObsObject obsObject) {		
    	if (null == obsObject) {
    		throw new IllegalArgumentException("Invalid object: null");
    	}
    	
    	assertValidArgument(obsObject.getFamily());
    	assertValidKeyArgument(obsObject.getKey());
    	
    	if (obsObject instanceof ObsDownloadObject) {
    		String targetDir = ((ObsDownloadObject) obsObject).getTargetDir();
        	if (null == targetDir) {
        		throw new IllegalArgumentException("Invalid targetDir: null");
        	}
        	if (targetDir.isEmpty()) {
        		throw new IllegalArgumentException("Invalid targetDir (empty)");
        	}
    	} else if (obsObject instanceof ObsUploadObject) {
    		File f = ((ObsUploadObject) obsObject).getFile();
    		if (null == f) {
    			throw new IllegalArgumentException("Invalid file: null");
    		}
    		if (f.getAbsolutePath().isEmpty()) {
    			throw new IllegalArgumentException("Invalid file (empty)");
    		}
    	}
	}
	
	public static void assertValidArgument(ProductFamily productFamily) {
		if (null == productFamily) {
			throw new IllegalArgumentException("Invalid product family: null");
		}
	}
	
	public static void assertValidArgument(Date date) {
		if (null == date) {
    		throw new IllegalArgumentException("Invalid date: null");
    	}
	}

	public static void assertValidKeyArgument(String key) {
		if (null == key) {
    		throw new IllegalArgumentException("Invalid key: null");
    	}
    	if (key.isEmpty()) {
    		throw new IllegalArgumentException("Invalid key: (empty)");
    	}
	}

	public static void assertValidPrefixArgument(String prefix) {
		if (null == prefix) {
    		throw new IllegalArgumentException("Invalid prefix: null");
    	}
    	if (prefix.isEmpty()) {
    		throw new IllegalArgumentException("Invalid prefix: (empty)");
    	}
	}	
}
