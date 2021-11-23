package de.werum.coprs.ddip.frontend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.werum.coprs.ddip.frontend.config.DdipProperties;

@Service
public class DdipServiceImpl implements DdipService {

	private final DdipProperties ddipProperties;

	@Autowired
	public DdipServiceImpl(final DdipProperties ddipProperties) {

		this.ddipProperties = ddipProperties;
	}

	@Override
	public String getDdipVersion() {
		return this.ddipProperties.getVersion();
	}

}
