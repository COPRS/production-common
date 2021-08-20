package de.werum.csgrs.nativeapi.service;

import java.util.List;

public interface NativeApiService {

	String getNativeApiVersion();

	List<String> getMissions();

	List<String> getProductTypes(final String missionName);

	List<String> getAttributes(final String missionName, final String productType);

	Long pripCount();

}
