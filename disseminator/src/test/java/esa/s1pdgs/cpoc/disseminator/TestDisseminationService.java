package esa.s1pdgs.cpoc.disseminator;

import org.junit.Test;

import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.mqi.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;

public class TestDisseminationService {

	
	@Test
	public final void testNewMqiListener() {
		final DisseminationProperties properties = new DisseminationProperties();
		
		final DisseminationService uut = new DisseminationService(null, null, properties, ErrorRepoAppender.NULL);
		
		final MqiListener<ProductDto> mqiListener = uut.newMqiListener(typeConfig);
	}
	
}
