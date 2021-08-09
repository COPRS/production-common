package de.werum.csgrs.nativeapi.service;

import java.util.List;
import java.util.Map;

public interface NativeApiService {

	String getNativeApiVersion();

	List<String> getMissions();

	List<String> getProductTypes(final String missionName);

	Map<String, String> getAttributes(final String missionName, final String productType);

}
