package de.werum.csgrs.nativeapi.service;

import java.util.List;
import de.werum.csgrs.nativeapi.rest.model.PripMetadataResponse;

public interface NativeApiService {

	String getNativeApiVersion();

	List<String> getMissions();

	List<String> getProductTypes(final String missionName);

	default List<String> getAttributes(final String missionName) {
		return this.getAttributes(missionName, null);
	}

	List<String> getAttributes(final String missionName, final String productType);

	PripMetadataResponse findProduct(final String missionName, final String productId);

	default List<PripMetadataResponse> findWithFilters(final String missionName, final String filterStr) {
		return this.findWithFilters(missionName, null, filterStr);
	}

	List<PripMetadataResponse> findWithFilters(final String missionName, final String productType, final String filterStr);

	byte[] downloadProduct(final String missionName, final String productId);

}
