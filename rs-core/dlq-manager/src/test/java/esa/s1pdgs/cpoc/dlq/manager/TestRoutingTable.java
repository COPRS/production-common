package esa.s1pdgs.cpoc.dlq.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.dlq.manager.config.TestConfig;
import esa.s1pdgs.cpoc.dlq.manager.model.routing.RoutingTable;

@RunWith(SpringRunner.class)
@SpringBootTest	
@ComponentScan("esa.s1pdgs.cpoc")
@Import(TestConfig.class)
class TestRoutingTable {
	
	@Autowired
	RoutingTable routingTable;
	
	@Test
	void testAutomaticLoading() {
		assertEquals(8, routingTable.size());
	}

}
