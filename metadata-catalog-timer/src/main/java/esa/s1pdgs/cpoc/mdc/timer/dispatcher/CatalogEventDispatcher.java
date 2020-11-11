package esa.s1pdgs.cpoc.mdc.timer.dispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface CatalogEventDispatcher extends Runnable {
	static final Logger LOGGER = LogManager.getLogger(CatalogEventDispatcher.class);
}
