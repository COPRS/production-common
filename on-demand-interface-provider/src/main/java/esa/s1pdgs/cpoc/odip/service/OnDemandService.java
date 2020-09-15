package esa.s1pdgs.cpoc.odip.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

@Service
public class OnDemandService {
	public static final Logger LOGGER = LogManager.getLogger(OnDemandService.class);

	public void submit(final String topic, final AbstractMessage dto) {
		LOGGER.info("Resubmitting following message to topic '{}': {}", topic, dto);
	}

}
