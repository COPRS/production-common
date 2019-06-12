package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.errorrepo.ApplicationProperties;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;

public class ErrorRepositoryImpl implements ErrorRepository {
	
	private final GenericAppCatalogMqiService<ErrorDto> persistErrorsService;
	private final ApplicationProperties appProperties;

	@Autowired
	public ErrorRepositoryImpl(final ApplicationProperties appProperties, final GenericAppCatalogMqiService<ErrorDto> persistErrorsService) {
		this.appProperties = appProperties;
		this.persistErrorsService = persistErrorsService;
	}

	@Override
	public List<FailedProcessing> getFailedProcessings() {
		// TODO
		return null;
		
	}

	@Override
	public FailedProcessing getFailedProcessingsById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restartAndDeleteFailedProcessing(String id) {
		// TODO Auto-generated method stub
	}

}
