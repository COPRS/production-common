package esa.s1pdgs.cpoc.production.trigger.consumption;

import java.util.Optional;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.production.trigger.appcat.AppCatAdapter;

public interface ProductTypeConsumptionHandler {	
	String type();

	boolean isReady(final AppDataJob job, final String productName);

	Optional<AppDataJob> findAssociatedJobFor(final AppCatAdapter appCat, final CatalogEventAdapter catEvent) throws AbstractCodedException;

	AppDataJobProduct newProductFor(final GenericMessageDto<CatalogEvent> mqiMessage);
}
