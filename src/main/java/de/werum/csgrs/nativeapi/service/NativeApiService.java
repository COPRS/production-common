package de.werum.csgrs.nativeapi.service;

import java.util.List;

import de.werum.csgrs.nativeapi.rest.model.PripMetadataResponse;

public interface NativeApiService {

	String getNativeApiVersion();

	List<String> getMissions();

	List<String> getProductTypes(final String missionName);

	List<String> getAttributes(final String missionName, final String productType);

	List<PripMetadataResponse> findAll(final String missionName, final String productType);

}
