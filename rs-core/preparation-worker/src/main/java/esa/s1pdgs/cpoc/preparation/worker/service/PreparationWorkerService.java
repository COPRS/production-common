package esa.s1pdgs.cpoc.preparation.worker.service;

import java.util.List;
import java.util.function.Function;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfExecutionJob;

public class PreparationWorkerService implements Function<CatalogEvent, List<IpfExecutionJob>> {

	@Override
	public List<IpfExecutionJob> apply(CatalogEvent t) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
