package esa.s1pdgs.cpoc.dlq.manager.model.routing;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.dlq.manager.config.TestConfig;

@RunWith(SpringRunner.class)
@SpringBootTest	
@ComponentScan("esa.s1pdgs.cpoc")
@Import(TestConfig.class)
class ITRoutingTable {
	
	@Autowired
	RoutingTable routingTable;
	
	@Test
	void testAutomaticLoading() {
		assertEquals(8, routingTable.size());
	}

}
