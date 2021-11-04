package de.werum.coprs.nativeapi.service;

import java.util.List;

import de.werum.coprs.nativeapi.rest.model.PripMetadataResponse;
import de.werum.coprs.nativeapi.service.helper.DownloadUrl;

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

	DownloadUrl provideTemporaryProductDonwload(final String missionName, final String productId);

}
