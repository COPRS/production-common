package esa.s1pdgs.cpoc.errorrepo.service;

import java.util.List;

import esa.s1pdgs.cpoc.mqi.model.queue.ErrorDto;

public class ErrorRepositoryImpl implements ErrorRepository {

	@Override
	public List<ErrorDto> getFailedProcessings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ErrorDto getFailedProcessingsById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restartAndDeleteFailedProcessing(String id) {
		// TODO Auto-generated method stub
	}

}
