package esa.s1pdgs.cpoc.mqi.client;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public interface MessageFilter {
	boolean accept(AbstractMessage message);
}
