package esa.s1pdgs.cpoc.production.trigger.taskTableMapping;

import java.util.List;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;

public interface TasktableMapper {	
	public List<String> tasktableFor(AppDataJobProduct product);
}
