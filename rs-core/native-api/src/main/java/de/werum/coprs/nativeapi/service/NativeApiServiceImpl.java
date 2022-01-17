package de.werum.coprs.nativeapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.werum.coprs.nativeapi.config.NativeApiProperties;

@Service
public class NativeApiServiceImpl implements NativeApiService {

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
