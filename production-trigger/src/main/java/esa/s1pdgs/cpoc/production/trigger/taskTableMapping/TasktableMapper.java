package esa.s1pdgs.cpoc.production.trigger.taskTableMapping;

import java.util.List;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

@FunctionalInterface
public interface TasktableMapper {	
	public List<String> tasktableFor(CatalogEvent product);
}
