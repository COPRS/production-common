package esa.s1pdgs.cpoc.mdc.timer.publish;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

public interface Publisher {
	
	public void publish(CatalogEvent event) throws Exception;
}
