package esa.s1pdgs.cpoc.production.trigger.consumption;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

public interface ProductTypeConsumptionHandler {	
	String type();
	AppDataJobProduct newProductFor(final GenericMessageDto<CatalogEvent> mqiMessage);
}
