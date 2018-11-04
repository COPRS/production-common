package esa.s1pdgs.cpoc.mqi.server.publication.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class RouteTest {

    
    @Test
    public void testConstructor() {
        Route rte = new Route();
        assertEquals(rte.getOutputKey(), "BLANK");
        
        rte = new Route("inputKey", "L1_SLICE", new RouteTo());
        assertEquals(rte.getInputKey(), "inputKey");
        assertEquals(rte.getOutputKey(), "L1_SLICE");
        assertEquals(rte.getRouteTo().getTopic(), "");
        
        RouteTo rte1 = new RouteTo();
        rte1.setTopic("topic-l1-slices");
        rte.setInputKey("input-Key");
        rte.setOutputKey("L1_ACN");
        rte.setRouteTo(rte1);
        assertEquals(rte.getInputKey(), "input-Key");
        assertEquals(rte.getOutputKey(), "L1_ACN");
        assertEquals(rte.getRouteTo().getTopic(), "topic-l1-slices");
    }
    
    @Test
    public void testToString() {
        RouteTo rte1 = new RouteTo();
        rte1.setTopic("topic-l1-slices");
        Route rte = new Route("inputKey", "L1_SLICE", rte1);
        String str = rte.toString();
        assertTrue(str.contains("inputKey="+"inputKey"));
        assertTrue(str.contains("outputKey="+"L1_SLICE"));
        assertTrue(str.contains("routeTo={topic: "+rte1.getTopic()));
        
    }
    
    /**
     * Check equals and hashcode methods
     */
    @Test
    public void checkEquals() {
        EqualsVerifier.forClass(Route.class).usingGetClass()
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
