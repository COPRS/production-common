package de.werum.csgrs.nativeapi.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.werum.csgrs.nativeapi.config.NativeApiProperties;

@Service
public class NativeApiServiceImpl implements NativeApiService {

	private static final Logger LOG = LogManager.getLogger(NativeApiServiceImpl.class);

	private final NativeApiProperties apiProperties;

	@Autowired
	public NativeApiServiceImpl(final NativeApiProperties apiProperties) {

		this.apiProperties = apiProperties;
	}

	@Override
	public String getNativeApiVersion() {
		return this.apiProperties.getVersion();
	}

}
