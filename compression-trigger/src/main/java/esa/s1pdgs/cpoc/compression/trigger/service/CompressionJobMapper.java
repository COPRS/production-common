package esa.s1pdgs.cpoc.compression.trigger.service;

import java.util.UUID;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CompressionJob;

@FunctionalInterface
public interface CompressionJobMapper<E extends AbstractMessage> {
	CompressionJob toCompressionJob(E input, UUID reportingId);
}
