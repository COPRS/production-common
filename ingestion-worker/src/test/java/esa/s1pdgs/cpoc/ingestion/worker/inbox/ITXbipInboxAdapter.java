package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties
@ComponentScan("esa.s1pdgs.cpoc.xbip")
public class ITXbipInboxAdapter {

	@Autowired
	private XbipClientFactory xbipFactory;
	
	@Test
	public final void test() throws Exception {
		System.out.println(xbipFactory);
		
		final XbipInboxAdapter uut = new XbipInboxAdapter(xbipFactory);
		
		final List<InboxAdapterEntry> entries = uut.read(
				new URI("https://cgs01.sentinel1.eo.esa.int/NOMINAL/S1A/DCS_04_20200403151525031965_dat/"), 
				"DCS_04_20200403151525031965_dat"
		);
		entries.forEach(e -> System.out.println(e));
	}
	
}
