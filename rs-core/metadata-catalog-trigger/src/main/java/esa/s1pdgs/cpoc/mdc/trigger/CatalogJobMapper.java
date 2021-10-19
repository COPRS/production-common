package esa.s1pdgs.cpoc.mdc.trigger;

import java.util.UUID;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogJob;

@FunctionalInterface
public interface CatalogJobMapper<E extends AbstractMessage> {
	CatalogJob toCatJob(E input, UUID reportingId);
}
