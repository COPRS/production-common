package de.werum.coprs.nativeapi.service;

import de.werum.coprs.nativeapi.rest.model.stac.StacItemCollection;

public interface NativeApiStacService {

	StacItemCollection find(final String datetime);

}
