package esa.s1pdgs.cpoc.preparation.worker.tasktable;

import java.util.List;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

@FunctionalInterface
public interface TasktableMapper {	
	public List<String> tasktableFor(CatalogEvent product);
}
